/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.pde;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutStatus;

/**
 * Pde handling using eclipse environment IProject
 * NOTE When including jars
 * If jar must be included in the delivered plugin jar file:
 * 1. add jar to MANIFEST.MF as
 * Bundle-ClassPath: my.jar
 * 2. add jar to build.properties as
 * bin.includes = my.jar
 * If jar is only used to compile and should not be delivered in the final plugin jar file:
 * 1. add jar to build.properties as
 * jars.extra.classpath = my.jar
 */
public final class PdeUtility {

  private PdeUtility() {
  }

  /**
   * @return the appended values
   */
  public static String[] addExportPackage(IProject project, String packageName) throws CoreException, IOException {
    return addExportPackages(project, new String[]{packageName});
  }

  public static String[] getExportPackages(IProject project) throws CoreException, IOException {
    return getAttributeList(project, "Export-Package");
  }

  public static String[] getImportPackages(IProject project) throws CoreException, IOException {
    return getAttributeList(project, "Import-Package");
  }

  /**
   * @return the appended values
   */
  public static String[] addExportPackages(IProject project, String[] packageNames) throws CoreException, IOException {
    return extendAttributeList(project, "Export-Package", packageNames);
  }

  /**
   * @return the appended values
   */
  public static String[] removeExportPackage(IProject project, String packageName) throws CoreException, IOException {
    return removeExportPackages(project, new String[]{packageName});
  }

  /**
   * @return the appended values
   */
  public static String[] removeExportPackages(IProject project, String[] packageNames) throws CoreException, IOException {
    return reduceAttributeList(project, "Export-Package", packageNames);
  }

  /**
   * @return the appended values
   */
  public static String[] addBundleClasspath(IProject project, String path) throws CoreException, IOException {
    return addBundleClasspaths(project, new String[]{path});
  }

  /**
   * add paths to Manifest.mf Bundle-ClassPath
   * 
   * @return the appended values
   */
  public static String[] addBundleClasspaths(IProject project, String[] paths) throws CoreException, IOException {
    String[] changedPaths = extendAttributeList(project, "Bundle-ClassPath", paths);
    return changedPaths;
  }

  public static String[] getBundleClasspaths(IProject project) throws CoreException, IOException {
    return getAttributeList(project, "Bundle-ClassPath");
  }

  /**
   * add paths to .classpath of the project
   */
  public static void addProjectClasspaths(IProject project, String[] paths) throws CoreException, IOException {
    ClasspathXml classpathFile = new ClasspathXml(project);
    for (String path : paths) {
      classpathFile.addEntry(ClasspathEntry.KIND_LIB, path, null, true);
    }
    classpathFile.store(null);
  }

  /**
   * add paths to build.properties of the project
   */
  public static void addBuildPropertiesFiles(IProject project, String propertyName, String[] files) throws CoreException, IOException {
    BuildProperties buildProps = new BuildProperties(project);
    buildProps.addFiles(propertyName, files);
    buildProps.store(null);
  }

  /**
   * remove paths from build.properties of the project
   */
  public static void removeBuildPropertiesFiles(IProject project, String propertyName, String[] files) throws CoreException, IOException {
    BuildProperties buildProps = new BuildProperties(project);
    buildProps.removeFiles(propertyName, files);
    buildProps.store(null);
  }

  /**
   * add paths to build.properties of the project
   */
  public static void addBuildPropertiesDirectories(IProject project, String propertyName, String[] dirs) throws CoreException, IOException {
    BuildProperties buildProps = new BuildProperties(project);
    buildProps.addDirectories(propertyName, dirs);
    buildProps.store(null);
  }

  /**
   * remove paths from build.properties of the project
   */
  public static void removeBuildPropertiesDirectories(IProject project, String propertyName, String[] dirs) throws CoreException, IOException {
    BuildProperties buildProps = new BuildProperties(project);
    buildProps.removeDirectories(propertyName, dirs);
    buildProps.store(null);
  }

  /**
   * @return the appended values
   */
  public static String[] extendAttributeList(IProject project, String attributeName, String additionalValue) throws CoreException, IOException {
    return extendAttributeList(project, attributeName, new String[]{additionalValue});
  }

  /**
   * @return the appended values
   */
  public static String[] extendAttributeList(IProject project, String attributeName, String[] additionalValues) throws CoreException, IOException {
    Manifest mf = new Manifest();
    mf.read(project);
    String[] appendedValues = RawPdeUtility.extendAttributeList(mf, attributeName, additionalValues);
    if (appendedValues.length > 0) {
      mf.write(project);
    }
    return appendedValues;
  }

  public static String[] getAttributeList(IProject project, String attributeName) throws CoreException, IOException {
    Manifest mf = new Manifest();
    mf.read(project);
    return RawPdeUtility.getAttributeList(mf, attributeName);
  }

  /**
   * @return the appended values
   */
  public static String[] reduceAttributeList(IProject project, String attributeName, String[] additionalValues) throws CoreException, IOException {
    Manifest mf = new Manifest();
    mf.read(project);
    String[] appendedValues = RawPdeUtility.reduceAttributList(mf, attributeName, additionalValues);
    if (appendedValues.length > 0) {
      mf.write(project);
    }
    return appendedValues;
  }

  /**
   * @return the appended values
   */
  public static String[] addDependencies(IProject project, String[] d) throws CoreException, IOException {
    return extendAttributeList(project, "Require-Bundle", d);
  }

  public static String[] getDependencies(IProject project) throws CoreException, IOException {
    return getAttributeList(project, "Require-Bundle");
  }

  /**
   * @return the appended values
   */
  public static String[] removeDependencies(IProject project, String[] d) throws CoreException, IOException {
    return reduceAttributeList(project, "Require-Bundle", d);
  }

  /**
   * @param jarFiles
   *          array of jar files (example: lib/jdbc-1.4/oracle-10g/ojdbc14.jar)
   * @param includeInBinaryBuild
   *          true add to Manifest:Bundle-ClassPath and to build.properties:bin.includes / false only add to
   *          jarsbuild.properties:jars.extra.classpath
   * @param addToProjectClasspath
   *          true append to .classpath file
   * @return installed jarFiles
   * @throws CoreException
   * @throws IOException
   */
  public static String[] installLibraryFilesTo(IProject project, String[] jarFiles, boolean includeInBinaryBuild, IProgressMonitor p) throws CoreException, IOException {
    ArrayList<String> importedJarFiles = new ArrayList<String>();
    for (int i = 0; i < jarFiles.length; i++) {
      URL url = ScoutSdk.getDefault().getBundle().getResource(jarFiles[i]);
      if (url == null) {
        ScoutSdk.logWarning("Cannot find " + jarFiles[i] + " in bundle " + ScoutSdk.getDefault().getBundle().getSymbolicName());
      }
      else {
        IFile file = project.getFile(jarFiles[i]);
        if (!file.exists()) {
          InputStream in = url.openStream();
          try {
            createFolder(project.getFolder(new Path(jarFiles[i]).removeLastSegments(1)));
            file.create(in, true, p);
          }
          finally {
            in.close();
          }
          importedJarFiles.add(jarFiles[i]);
        }
      }
    }
    if (includeInBinaryBuild) {
      PdeUtility.addBundleClasspaths(project, jarFiles);
      PdeUtility.addBuildPropertiesFiles(project, BuildProperties.PROP_BIN_INCLUDES, jarFiles);
    }
    else {
      PdeUtility.addBuildPropertiesFiles(project, BuildProperties.PROP_JARS_EXTRA_CLASSPATH, jarFiles);
    }
    return importedJarFiles.toArray(new String[0]);
  }

  /**
   * creates a folder recursively
   * 
   * @param folder
   * @throws CoreException
   */
  public static void createFolder(IContainer folder) throws CoreException {
    if (!folder.exists()) {
      IContainer parent = folder.getParent();
      if (parent instanceof IFolder) {
        createFolder(parent);
      }
      if (folder instanceof IFolder) {
        ((IFolder) folder).create(true, true, null);
      }
    }
  }

  public static void createFile(IFile file, InputStream in, IProgressMonitor monitor) throws CoreException {
    if (file.exists()) {
      file.setContents(in, true, false, monitor);
    }
    else {
      createFolder(file.getParent());
      file.create(in, true, monitor);
    }
  }

  /**
   * called when the plugin closes
   */
  public static void dispose() {

  }

  @SuppressWarnings("restriction")
  public static String launchLocalJavaApplicationAndWait(String launchName, String projectName, String javaMainTypeName, String argumentList, boolean analyseConsoleOutputForException, IProgressMonitor monitor) throws CoreException {
    File consoleFile = null;
    try {
      consoleFile = File.createTempFile("console", ".log");
    }
    catch (Throwable t) {
      ScoutSdk.logError("creating console buffer for " + launchName, t);
    }
    //
    ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType type = lm.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
    ILaunchConfigurationWorkingCopy w = type.newInstance(null, launchName);
    w.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
    w.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, javaMainTypeName);
    w.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, argumentList);
    w.setAttribute(org.eclipse.debug.internal.core.LaunchConfiguration.ATTR_MAPPED_RESOURCE_TYPES, Arrays.asList(new String[]{"4"}));
    w.setAttribute(org.eclipse.debug.internal.core.LaunchConfiguration.ATTR_MAPPED_RESOURCE_PATHS, Arrays.asList(new String[]{"/" + projectName}));
    w.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
    if (consoleFile != null) {
      w.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, consoleFile.getAbsolutePath());
      w.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, false);
    }
    ILaunchConfiguration lc = w.doSave();
    ILaunch launch = lc.launch(ILaunchManager.RUN_MODE, monitor, true, true);
    while (!launch.isTerminated()) {
      try {
        Thread.sleep(200);
      }
      catch (InterruptedException e) {
      }
    }
    IProcess[] processes = launch.getProcesses();
    StringBuilder dump = new StringBuilder();
    dump.append("Project: " + projectName + "\n");
    dump.append("Program: " + javaMainTypeName + "\n");
    dump.append("Arguments: " + argumentList + "\n");
    if (processes != null) {
      for (IProcess p : processes) {
        if (consoleFile != null && consoleFile.exists()) {
          try {
            dump.append("\n");
            dump.append(IOUtility.getContent(new FileReader(consoleFile)));
          }
          catch (Exception e) {
            // nop
          }
        }
        if (p.getExitValue() != 0) {
          throw new CoreException(new ScoutStatus(dump.toString()));
        }
      }
    }
    String consoleText = dump.toString();
    if (analyseConsoleOutputForException) {
      Matcher m = Pattern.compile("\\tat [a-zA-Z0-9_.]+\\(").matcher(consoleText);
      if (m.find()) {
        throw new CoreException(new ScoutStatus(consoleText));
      }
    }
    return consoleText;
  }

}
