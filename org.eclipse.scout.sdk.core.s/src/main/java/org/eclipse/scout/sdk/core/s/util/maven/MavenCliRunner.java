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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.cli.CLIManager;
import org.apache.maven.cli.MavenCli;
import org.eclipse.scout.sdk.core.util.SdkConsole;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link MavenCliRunner}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class MavenCliRunner implements IMavenRunnerSpi {

  private static final String MAVEN_CALL_FAILED_MSG = "Maven call failed.";

  @Override
  public void execute(MavenBuild build) {
    Validate.notNull(build);
    try (URLClassLoader loader = MavenSandboxClassLoaderFactory.build()) {
      SdkLog.debug("Executing embedded {}", build.toString());
      execute(Validate.notNull(build.getWorkingDirectory()), build.getOptions(), build.getGoals(), build.getProperties(), loader);
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  protected static synchronized void execute(File workingDirectory, Set<String> options, Set<String> goals, Map<String, String> props, ClassLoader loader) throws IOException {
    String oldMultiModuleProjectDir = System.getProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
    ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader(); // backup context class loader because maven-cli changes it
    InputStream origSystemIn = System.in; // backup system.in and provide a dummy because maven-cli closes the stream.
    try {
      System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, workingDirectory.getAbsolutePath());
      System.setIn(new ByteArrayInputStream(new byte[]{}));
      Thread.currentThread().setContextClassLoader(loader);

      String[] mavenArgs = getMavenArgs(new LinkedHashSet<>(options), goals, new LinkedHashMap<>(props));
      runMavenInSandbox(mavenArgs, workingDirectory, SdkLog.getLogLevel(), loader);
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

  protected static String[] getMavenArgs(Set<String> options, Set<String> goals, Map<String, String> props) {
    final String mavenExtClassPath = "maven.ext.class.path";
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
    return args.toArray(new String[args.size()]);
  }

  protected static void runMavenInSandbox(String[] mavenArgs, File workingDirectory, Level level, ClassLoader loader) throws IOException {
    String charset = StandardCharsets.UTF_8.name();
    try (ByteArrayOutputStream bOut = new ByteArrayOutputStream(); PrintStream out = new PrintStream(bOut, true, charset); ByteArrayOutputStream bErr = new ByteArrayOutputStream(); PrintStream err = new PrintStream(bErr, true, charset)) {
      loader.loadClass(SdkConsole.class.getName()).getMethod("getConsoleSpi").invoke(null); // enforce init of out streams before they are changed by maven-cli
      loader.loadClass(SdkLog.class.getName()).getMethod("setLogLevel", Level.class).invoke(null, level); // copy log level to new classloader

      // start maven call
      Class<?> mavenCli = loader.loadClass(MavenCli.class.getName());
      Method doMain = mavenCli.getMethod("doMain", new Class[]{String[].class, String.class, PrintStream.class, PrintStream.class});
      Object ret = doMain.invoke(mavenCli.newInstance(), mavenArgs, workingDirectory.getAbsolutePath(), out, err);

      logStream(Level.INFO, bOut, charset);
      int result = ((Integer) ret).intValue();
      if (result != 0) {
        logStream(Level.SEVERE, bErr, charset);
        throw new IOException(MAVEN_CALL_FAILED_MSG);
      }
    }
    catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
      throw new IOException(e);
    }
  }

  protected static void logStream(Level level, ByteArrayOutputStream stream, String charset) throws UnsupportedEncodingException {
    String outString = stream.toString(charset);
    if (StringUtils.isNotBlank(outString)) {
      SdkLog.log(level, "Output of embedded Maven call:\nMVN-BEGIN\n{}\nMVN-END\n", outString);
    }
  }
}
