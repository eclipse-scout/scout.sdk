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
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


/**
 * <h3>{@link MavenCliRunner}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings({"AccessOfSystemProperties", "CallToNativeMethodWhileLocked"})
public class MavenCliRunner implements IMavenRunnerSpi {

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
    var oldContextClassLoader = Thread.currentThread().getContextClassLoader(); // backup context class loader because maven-cli changes it
    var origSystemIn = System.in; // backup system.in and provide a dummy because maven-cli closes the stream.
    try {
      System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, workingDirectory.toAbsolutePath().toString());
      System.setIn(new ByteArrayInputStream(new byte[]{}));
      Thread.currentThread().setContextClassLoader(loader);
      //noinspection ResultOfMethodCallIgnored
      SdkConsole.getConsoleSpi(); // enforce init of logging SPI
      var mavenArgs = getMavenArgs(new LinkedHashSet<>(options), goals, new LinkedHashMap<>(props));
      runMavenInSandbox(mavenArgs, workingDirectory, loader);
    }
    finally {
      Thread.currentThread().setContextClassLoader(oldContextClassLoader);
      System.setIn(origSystemIn);
      if (oldMultiModuleProjectDir == null) {
        System.clearProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
      }
      else {
        System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, oldMultiModuleProjectDir);
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

  protected static void runMavenInSandbox(String[] mavenArgs, Path workingDirectory, ClassLoader loader) throws IOException {
    try {
      // start maven call
      var mavenCli = loader.loadClass(MavenCli.class.getName());
      var doMain = mavenCli.getMethod("doMain", String[].class, String.class, PrintStream.class, PrintStream.class);
      //noinspection UseOfSystemOutOrSystemErr
      var ret = doMain.invoke(mavenCli.getConstructor().newInstance(), mavenArgs, workingDirectory.toAbsolutePath().toString(), System.out, System.err);
      int result = (Integer) ret;
      if (result != 0) {
        throw new IOException(MAVEN_CALL_FAILED_MSG);
      }
    }
    catch (ReflectiveOperationException e) {
      throw new IOException(e);
    }
  }
}
