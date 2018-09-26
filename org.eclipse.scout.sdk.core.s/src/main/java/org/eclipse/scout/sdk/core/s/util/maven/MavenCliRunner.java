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
package org.eclipse.scout.sdk.core.s.util.maven;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.cli.CLIManager;
import org.apache.maven.cli.MavenCli;
import org.eclipse.scout.sdk.core.util.SdkConsole;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;

import okhttp3.ConnectionPool;

/**
 * <h3>{@link MavenCliRunner}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class MavenCliRunner implements IMavenRunnerSpi {

  private static final String OKHTTP_KEEP_ALIVE = "http.keepAlive";
  private static final String MAVEN_CALL_FAILED_MSG = "Maven call failed.";

  @Override
  public void execute(final MavenBuild build) {
    Validate.notNull(build);
    try (final URLClassLoader loader = MavenSandboxClassLoaderFactory.build()) {
      SdkLog.debug("Executing embedded {}", build.toString());
      Validate.notNull(build.getWorkingDirectory());
      execute(build.getWorkingDirectory(), build.getOptions(), build.getGoals(), build.getProperties(), loader);
    }
    catch (final IOException e) {
      throw new SdkException(e);
    }
  }

  protected static synchronized void execute(final File workingDirectory, final Set<String> options, final Collection<String> goals, final Map<String, String> props, final ClassLoader loader) throws IOException {
    final String oldMultiModuleProjectDir = System.getProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
    final String oldHttpKeepAlive = System.getProperty(OKHTTP_KEEP_ALIVE);
    final ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader(); // backup context class loader because maven-cli changes it
    final InputStream origSystemIn = System.in; // backup system.in and provide a dummy because maven-cli closes the stream.
    try {
      System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, workingDirectory.getAbsolutePath());
      System.setProperty(OKHTTP_KEEP_ALIVE, Boolean.toString(false)); // disable OkHttp keepAlive
      System.setIn(new ByteArrayInputStream(new byte[]{}));
      Thread.currentThread().setContextClassLoader(loader);
      SdkConsole.getConsoleSpi(); // enforce init of logging SPI
      final String[] mavenArgs = getMavenArgs(new LinkedHashSet<>(options), goals, new LinkedHashMap<>(props));
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

  protected static String[] getMavenArgs(final Collection<String> options, final Collection<String> goals, final Map<String, String> props) {
    final String mavenExtClassPath = "maven.ext.class.path";
    if (!props.containsKey(mavenExtClassPath)) {
      props.put(mavenExtClassPath, "");
    }
    if (SdkLog.isDebugEnabled()) {
      options.add(Character.toString(CLIManager.DEBUG));
      options.add(Character.toString(CLIManager.ERRORS));
    }

    final List<String> args = new ArrayList<>(goals.size() + options.size() + props.size());
    args.addAll(goals);
    for (final String option : options) {
      args.add('-' + option);
    }
    for (final String prop : MavenBuild.getMapAsList(props)) {
      args.add("-D" + prop);
    }
    return args.toArray(new String[0]);
  }

  protected static void runMavenInSandbox(final String[] mavenArgs, final File workingDirectory, final Level level, final ClassLoader loader) throws IOException {
    final String charset = StandardCharsets.UTF_8.name();
    try (final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(bOut, true, charset);
        final ByteArrayOutputStream bErr = new ByteArrayOutputStream();
        final PrintStream err = new PrintStream(bErr, true, charset)) {
      loader.loadClass(SdkConsole.class.getName()).getMethod("getConsoleSpi").invoke(null); // enforce init of out streams before they are changed by maven-cli
      loader.loadClass(SdkLog.class.getName()).getMethod("setLogLevel", Level.class).invoke(null, level); // copy log level to new classloader

      // start maven call
      final Class<?> mavenCli = loader.loadClass(MavenCli.class.getName());
      final Method doMain = mavenCli.getMethod("doMain", String[].class, String.class, PrintStream.class, PrintStream.class);
      final Object ret = doMain.invoke(mavenCli.getConstructor().newInstance(), mavenArgs, workingDirectory.getAbsolutePath(), out, err);

      logStream(Level.INFO, bOut, charset);
      final int result = (Integer) ret;
      if (result != 0) {
        logStream(Level.SEVERE, bErr, charset);
        throw new IOException(MAVEN_CALL_FAILED_MSG);
      }
    }
    catch (final ReflectiveOperationException e) {
      throw new IOException(e);
    }
  }

  /**
   * Tries to stop the OkHttp resources.
   */
  @SuppressWarnings({"deprecation", "squid:S1181"}) // Throwable and Error should not be caught
  protected static void stopOkHttp(final ClassLoader loader) {
    try {
      final Class<?> poolClass = loader.loadClass(ConnectionPool.class.getName());
      final Field field = poolClass.getDeclaredField("executor");
      field.setAccessible(true);
      final Object executor = field.get(null);
      ThreadPoolExecutor.class.getMethod("shutdownNow").invoke(executor);

      for (final Thread candidate : Thread.getAllStackTraces().keySet()) {
        final String threadName = candidate.getName();
        if ("Okio Watchdog".equals(threadName) || "OkHttp ConnectionPool".equals(threadName)) {
          candidate.setUncaughtExceptionHandler((t, e) -> SdkLog.debug("Okio Thread terminated", e));
          candidate.stop();
        }
      }
    }
    catch (final Throwable e) {
      SdkLog.error("Potential Memory-Leak: Cannot stop OkHttp client!", e);
    }
  }

  protected static void logStream(final Level level, final ByteArrayOutputStream stream, final String charset) throws UnsupportedEncodingException {
    final String outString = stream.toString(charset);
    if (StringUtils.isNotBlank(outString)) {
      SdkLog.log(level, "Output of embedded Maven call:\nMVN-BEGIN\n{}\nMVN-END\n", outString);
    }
  }
}
