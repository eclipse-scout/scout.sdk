/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testing.maven;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.apache.maven.cli.CLIManager;
import org.apache.maven.cli.MavenCli;
import org.eclipse.scout.sdk.core.log.SdkConsole;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenRunnerSpi;
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

import okhttp3.internal.connection.RealConnectionPool;

/**
 * <h3>{@link MavenCliRunner}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings({"AccessOfSystemProperties", "CallToNativeMethodWhileLocked"})
public class MavenCliRunner implements IMavenRunnerSpi {

  private static final String OK_HTTP_KEEP_ALIVE = "http.keepAlive";
  private static final String MAVEN_CALL_FAILED_MSG = "Maven call failed.";

  @Override
  public void execute(MavenBuild build, IEnvironment env, IProgress progress) {
    assertNotNull(build);
    assertNotNull(build.getWorkingDirectory());

    try (var loader = MavenSandboxClassLoaderFactory.build()) {
      SdkLog.debug("Executing embedded {}", build);
      execute(build.getWorkingDirectory(), build.getOptions(), build.getGoals(), build.getProperties(), loader);
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  protected static synchronized void execute(Path workingDirectory, Set<String> options, Collection<String> goals, Map<String, String> props, ClassLoader loader) throws IOException {
    var oldMultiModuleProjectDir = System.getProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
    var oldHttpKeepAlive = System.getProperty(OK_HTTP_KEEP_ALIVE);
    var oldContextClassLoader = Thread.currentThread().getContextClassLoader(); // backup context class loader because maven-cli changes it
    var origSystemIn = System.in; // backup system.in and provide a dummy because maven-cli closes the stream.
    try {
      System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, workingDirectory.toAbsolutePath().toString());
      System.setProperty(OK_HTTP_KEEP_ALIVE, Boolean.toString(false)); // disable OkHttp keepAlive
      System.setIn(new ByteArrayInputStream(new byte[]{}));
      Thread.currentThread().setContextClassLoader(loader);
      //noinspection ResultOfMethodCallIgnored
      SdkConsole.getConsoleSpi(); // enforce init of logging SPI
      var mavenArgs = getMavenArgs(new LinkedHashSet<>(options), goals, new LinkedHashMap<>(props));
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
        System.clearProperty(OK_HTTP_KEEP_ALIVE);
      }
      else {
        System.setProperty(OK_HTTP_KEEP_ALIVE, oldHttpKeepAlive);
      }
    }
  }

  protected static String[] getMavenArgs(Collection<String> options, Collection<String> goals, Map<String, String> props) {
    var mavenExtClassPath = "maven.ext.class.path";
    if (!props.containsKey(mavenExtClassPath)) {
      props.put(mavenExtClassPath, "");
    }

    var testForkJvmMemoryConfigKey = "master_test_jvmMemory";
    if (!props.containsKey(testForkJvmMemoryConfigKey)) {
      // reduce default memory consumption in forked tests
      var testForkJvmMemoryConfigValue = System.getProperty(testForkJvmMemoryConfigKey);
      if (Strings.isBlank(testForkJvmMemoryConfigValue)) {
        testForkJvmMemoryConfigValue = "-Xms256m -Xmx768m";
      }
      props.put(testForkJvmMemoryConfigKey, testForkJvmMemoryConfigValue);
    }

    if (SdkLog.isDebugEnabled()) {
      options.add(Character.toString(CLIManager.DEBUG));
      options.add(Character.toString(CLIManager.ERRORS));
    }

    List<String> args = new ArrayList<>(goals.size() + options.size() + props.size());
    args.addAll(goals);
    for (var option : options) {
      args.add('-' + option);
    }
    for (var prop : MavenBuild.getMapAsList(props)) {
      args.add("-D" + prop);
    }
    return args.toArray(new String[0]);
  }

  protected static void runMavenInSandbox(String[] mavenArgs, Path workingDirectory, Level level, ClassLoader loader) throws IOException {
    var charset = StandardCharsets.UTF_8.name();
    try (var bOut = new ByteArrayOutputStream();
         var out = new PrintStream(bOut, true, charset);
         var bErr = new ByteArrayOutputStream();
         var err = new PrintStream(bErr, true, charset)) {
      loader.loadClass(SdkConsole.class.getName()).getMethod("getConsoleSpi").invoke(null); // enforce init of out streams before they are changed by maven-cli
      loader.loadClass(SdkLog.class.getName()).getMethod("setLogLevel", Level.class).invoke(null, level); // copy log level to new classloader

      // start maven call
      var mavenCli = loader.loadClass(MavenCli.class.getName());
      var doMain = mavenCli.getMethod("doMain", String[].class, String.class, PrintStream.class, PrintStream.class);
      var ret = doMain.invoke(mavenCli.getConstructor().newInstance(), mavenArgs, workingDirectory.toAbsolutePath().toString(), out, err);

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
      var poolClass = loader.loadClass(RealConnectionPool.class.getName());
      var field = poolClass.getDeclaredField("executor");
      field.setAccessible(true);
      var executor = (ExecutorService) field.get(null);
      executor.shutdownNow();

      for (var candidate : Thread.getAllStackTraces().keySet()) {
        var threadName = candidate.getName();
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
    var outString = stream.toString(charset);
    if (Strings.hasText(outString)) {
      //noinspection HardcodedLineSeparator
      SdkLog.log(level, "Output of embedded Maven call:\nMVN-BEGIN\n{}\nMVN-END\n", outString);
    }
  }
}
