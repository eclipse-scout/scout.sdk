/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.testing.maven;

import okhttp3.ConnectionPool;
import org.apache.maven.cli.CLIManager;
import org.apache.maven.cli.MavenCli;
import org.eclipse.scout.sdk.core.log.SdkConsole;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenRunnerSpi;
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <h3>{@link MavenCliRunner}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings({"AccessOfSystemProperties", "CallToNativeMethodWhileLocked"})
public class MavenCliRunner implements IMavenRunnerSpi {

  private static final String OKHTTP_KEEP_ALIVE = "http.keepAlive";
  private static final String MAVEN_CALL_FAILED_MSG = "Maven call failed.";

  @Override
  public void execute(MavenBuild build) {
    assertNotNull(build);
    try (URLClassLoader loader = MavenSandboxClassLoaderFactory.build()) {
      SdkLog.debug("Executing embedded {}", build.toString());
      assertNotNull(build.getWorkingDirectory());
      execute(build.getWorkingDirectory(), build.getOptions(), build.getGoals(), build.getProperties(), loader);
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  protected static synchronized void execute(Path workingDirectory, Set<String> options, Collection<String> goals, Map<String, String> props, ClassLoader loader) throws IOException {
    String oldMultiModuleProjectDir = System.getProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
    String oldHttpKeepAlive = System.getProperty(OKHTTP_KEEP_ALIVE);
    ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader(); // backup context class loader because maven-cli changes it
    InputStream origSystemIn = System.in; // backup system.in and provide a dummy because maven-cli closes the stream.
    try {
      System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, workingDirectory.toAbsolutePath().toString());
      System.setProperty(OKHTTP_KEEP_ALIVE, Boolean.toString(false)); // disable OkHttp keepAlive
      System.setIn(new ByteArrayInputStream(new byte[]{}));
      Thread.currentThread().setContextClassLoader(loader);
      //noinspection ResultOfMethodCallIgnored
      SdkConsole.getConsoleSpi(); // enforce init of logging SPI
      String[] mavenArgs = getMavenArgs(new LinkedHashSet<>(options), goals, new LinkedHashMap<>(props));
      runMavenInSandbox(mavenArgs, workingDirectory, SdkLog.getLogLevel(), loader);
    }
    finally {
      stopOkHttp(loader);
      Thread.currentThread().setContextClassLoader(oldContextClassLoader);
      System.setIn(origSystemIn);
      if (oldMultiModuleProjectDir == null) {
        System.clearProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
      }
      else {
        System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, oldMultiModuleProjectDir);
      }

      if (oldHttpKeepAlive == null) {
        System.clearProperty(OKHTTP_KEEP_ALIVE);
      }
      else {
        System.setProperty(OKHTTP_KEEP_ALIVE, oldHttpKeepAlive);
      }
    }
  }

  protected static String[] getMavenArgs(Collection<String> options, Collection<String> goals, Map<String, String> props) {
    String mavenExtClassPath = "maven.ext.class.path";
    if (!props.containsKey(mavenExtClassPath)) {
      props.put(mavenExtClassPath, "");
    }
    if (SdkLog.isDebugEnabled()) {
      options.add(Character.toString(CLIManager.DEBUG));
      options.add(Character.toString(CLIManager.ERRORS));
    }

    List<String> args = new ArrayList<>(goals.size() + options.size() + props.size());
    args.addAll(goals);
    for (String option : options) {
      args.add('-' + option);
    }
    for (String prop : MavenBuild.getMapAsList(props)) {
      args.add("-D" + prop);
    }
    return args.toArray(new String[0]);
  }

  protected static void runMavenInSandbox(String[] mavenArgs, Path workingDirectory, Level level, ClassLoader loader) throws IOException {
    String charset = StandardCharsets.UTF_8.name();
    try (ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bOut, true, charset);
        ByteArrayOutputStream bErr = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(bErr, true, charset)) {
      loader.loadClass(SdkConsole.class.getName()).getMethod("getConsoleSpi").invoke(null); // enforce init of out streams before they are changed by maven-cli
      loader.loadClass(SdkLog.class.getName()).getMethod("setLogLevel", Level.class).invoke(null, level); // copy log level to new classloader

      // start maven call
      Class<?> mavenCli = loader.loadClass(MavenCli.class.getName());
      Method doMain = mavenCli.getMethod("doMain", String[].class, String.class, PrintStream.class, PrintStream.class);
      Object ret = doMain.invoke(mavenCli.getConstructor().newInstance(), mavenArgs, workingDirectory.toAbsolutePath().toString(), out, err);

      logStream(Level.INFO, bOut, charset);
      int result = (Integer) ret;
      if (result != 0) {
        logStream(Level.SEVERE, bErr, charset);
        throw new IOException(MAVEN_CALL_FAILED_MSG);
      }
    }
    catch (ReflectiveOperationException e) {
      throw new IOException(e);
    }
  }

  /**
   * Tries to stop the OkHttp resources.
   */
  @SuppressWarnings({"deprecation", "squid:S1181"}) // Throwable and Error should not be caught
  protected static void stopOkHttp(ClassLoader loader) {
    try {
      Class<?> poolClass = loader.loadClass(ConnectionPool.class.getName());
      Field field = poolClass.getDeclaredField("executor");
      field.setAccessible(true);
      Object executor = field.get(null);
      ThreadPoolExecutor.class.getMethod("shutdownNow").invoke(executor);

      for (Thread candidate : Thread.getAllStackTraces().keySet()) {
        String threadName = candidate.getName();
        if ("Okio Watchdog".equals(threadName) || "OkHttp ConnectionPool".equals(threadName)) {
          candidate.setUncaughtExceptionHandler((t, e) -> SdkLog.debug("Okio Thread terminated", e));
          //noinspection CallToThreadStopSuspendOrResumeManager
          candidate.stop();
        }
      }
    }
    catch (Throwable e) {
      SdkLog.error("Potential Memory-Leak: Cannot stop OkHttp client!", e);
    }
  }

  protected static void logStream(Level level, ByteArrayOutputStream stream, String charset) throws UnsupportedEncodingException {
    String outString = stream.toString(charset);
    if (Strings.hasText(outString)) {
      //noinspection HardcodedLineSeparator
      SdkLog.log(level, "Output of embedded Maven call:\nMVN-BEGIN\n{}\nMVN-END\n", outString);
    }
  }
}
