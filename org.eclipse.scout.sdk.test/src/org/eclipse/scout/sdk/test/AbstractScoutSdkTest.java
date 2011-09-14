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
package org.eclipse.scout.sdk.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.scout.sdk.internal.test.Activator;
import org.junit.Assert;

public abstract class AbstractScoutSdkTest {

  private static final String RESOURCES_FOLDER_NAME = "resources";

  protected static void setupWorkspace(String baseFolder, String... projects) throws Exception {
    disableAutoBuild();
    Assert.assertNotNull("baseFolder must not be null", baseFolder);

    if (projects == null || projects.length == 0) {
      projects = new String[]{null};
    }

    for (String project : projects) {
      IProject javaProject = createProject(project);
      copyProject(RESOURCES_FOLDER_NAME, baseFolder, project);
      javaProject.close(null);
      javaProject.open(null);
      refreshAndBuildProject(javaProject);
    }

  }

  private static void disableAutoBuild() throws CoreException {
    IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
    if (description.isAutoBuilding()) {
      description.setAutoBuilding(false);
      ResourcesPlugin.getWorkspace().setDescription(description);
    }
  }

  protected static void deleteProjects(final String... projectNames) throws Exception {
    if (projectNames == null || projectNames.length == 0) {
      return;
    }
    ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
      @Override
      public void run(IProgressMonitor monitor) throws CoreException {
        for (String project : projectNames) {
          try {
            deleteProject(project);
          }
          catch (Exception e) {
            // nop
          }
        }
      }
    }, null);
  }

  protected static void deleteProject(String name) throws Exception {
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    if (project == null) {
      return;
    }
    if (project.exists() && !project.isOpen()) {
      project.open(null);
    }
    deleteAndWaitUntilDeleted(project);
  }

  public static void refreshAndBuildProject(IProject javaProject) throws Exception {
    refreshProject(javaProject);
    autoBuildProject(javaProject);
  }

  /**
   * @param javaProject
   * @throws CoreException
   */
  private static void refreshProject(IProject javaProject) throws CoreException {
    Assert.assertNotNull(javaProject);
    javaProject.refreshLocal(IResource.DEPTH_INFINITE, null);
    waitForManualRefresh();
  }

  /**
   * @param javaProject
   * @throws CoreException
   */
  public static void autoBuildProject(IProject javaProject) throws CoreException {
    Assert.assertNotNull(javaProject);
    javaProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
    waitForManualBuild();
    waitForFamily(ResourcesPlugin.FAMILY_AUTO_BUILD);

  }

  public static void waitUntilIndexesReady() {
    // dummy query for waiting until the indexes are ready
    SearchEngine engine = new SearchEngine();
    IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
    try {
      engine.searchAllTypeNames(
          null,
          SearchPattern.R_EXACT_MATCH,
          "!@$#!@".toCharArray(),
          SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
          IJavaSearchConstants.CLASS,
          scope,
          new TypeNameRequestor() {
            @Override
            public void acceptType(
                int modifiers,
                char[] packageName,
                char[] simpleTypeName,
                char[][] enclosingTypeNames,
                String path) {
            }
          },
          IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
          null);
    }
    catch (CoreException e) {
    }
  }

  public static void waitForManualBuild() {
    waitForFamily(ResourcesPlugin.FAMILY_MANUAL_BUILD);
  }

  public static void waitForManualRefresh() {
    waitForFamily(ResourcesPlugin.FAMILY_MANUAL_REFRESH);
  }

  private static void waitForFamily(Object family) {
    boolean wasInterrupted = false;
    do {
      try {
        Job.getJobManager().join(family, null);
        wasInterrupted = false;
      }
      catch (OperationCanceledException e) {
        e.printStackTrace();
      }
      catch (InterruptedException e) {
        wasInterrupted = true;
      }
    }
    while (wasInterrupted);
  }

  public static boolean isResourceDeleted(IResource resource) {
    IContainer parent = resource.getParent();
    if (resource.isAccessible() || parent == null || !parent.exists()) {
      return false;
    }
    try {
      IResource[] members = parent.members();
      if (members != null) {
        for (IResource member : members) {
          if (resource.equals(member) || member.getFullPath().equals(resource.getFullPath())) {
            return false;
          }
        }
      }
    }
    catch (CoreException ce) {
      // nop
    }
    return true;
  }

  public static boolean isFileDeleted(File file) {
    File parent = file.getParentFile();
    if (file.exists() || parent == null || !parent.exists()) {
      return false;
    }
    if (parent.listFiles() != null) {
      for (File siblingFile : parent.listFiles()) {
        if (file.equals(siblingFile) || file.getPath().equals(siblingFile.getPath())) {
          return false;
        }
      }
    }
    return true;
  }

  public static void deleteAndWaitUntilDeleted(IResource resource) throws Exception {
    CoreException exception = null;
    File file = resource.getLocation().toFile();
    int delay = 10;
    for (int i = 1; i < 15; i++) {
      try {
        if (resource.isAccessible()) {
          resource.delete(true, null);
        }
        if (isResourceDeleted(resource) && isFileDeleted(file)) {
          return;
        }
      }
      catch (CoreException e) {
        exception = e;
      }
      if (i % 5 == 0) {
        delay *= 10;
      }
      Thread.sleep(delay);
      System.gc();
    }
    if (exception != null) {
      throw exception;
    }
  }

  protected static void copyProject(String... pathElements) throws IOException {
    URL resource = FileLocator.find(Platform.getBundle(Activator.PLUGIN_ID), createPath(pathElements), null);
    if (resource != null) {
      String path = FileLocator.toFileURL(resource).getPath();
      copyFilesRecursive(path, ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
    }
  }

  protected static InputStream getInputStream(String pathRelativeToResources) throws IOException {
    URL resource = FileLocator.find(Platform.getBundle(Activator.PLUGIN_ID), createPath(RESOURCES_FOLDER_NAME + "/" + pathRelativeToResources), null);
    InputStream is = null;
    if (resource != null) {
      is = resource.openStream();
    }
    return is;
  }

  protected static IProject createProject(String projectName) throws CoreException {
    IProgressMonitor monitor = new NullProgressMonitor();
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    project.create(monitor);
    project.open(monitor);
    return project;
  }

  private static Path createPath(String... pathElements) {
    StringBuilder builder = new StringBuilder();
    for (String e : pathElements) {
      if (e != null) {
        builder.append("/");
        builder.append(e);
      }
    }
    return new Path(builder.toString());
  }

  private static void copyFilesRecursive(String from, String toDir) throws IOException {
    File fromFile = new File(from);
    if (!fromFile.exists()) {
      return;
    }
    if (fromFile.isDirectory()) {
      File subDir = new File(toDir, fromFile.getName());
      if (!subDir.exists()) {
        subDir.mkdir();
      }
      toDir = toDir + "/" + fromFile.getName();
    }
    File[] fileList = fromFile.listFiles();
    if (fileList == null || fileList.length == 0) {
      return;
    }
    for (File file : fileList) {
      if (file.isDirectory()) {
        copyFilesRecursive(from + "/" + file.getName(), toDir);
      }
      else {
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        InputStream in = null;
        OutputStream out = null;

        try {
          in = new BufferedInputStream(new FileInputStream(file));
          out = new BufferedOutputStream(new FileOutputStream((toDir + "/" + file.getName())));
          while ((bytesRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
          }
        }
        finally {
          if (in != null) {
            try {
              in.close();
            }
            catch (Throwable t) {
              // nop
            }
          }
          if (out != null) {
            try {
              out.close();
            }
            catch (Throwable t) {
              // nop
            }
          }
        }
      }
    }
  }

  protected static boolean equalContents(InputStream a, InputStream b) {
    if (a == null) {
      return b == null;
    }
    if (b == null) {
      return a == null;
    }
    int lineNr = 1;
    BufferedReader readerA = null;
    BufferedReader readerB = null;
    try {
      readerA = new BufferedReader(new InputStreamReader(a));
      readerB = new BufferedReader(new InputStreamReader(b));
      String lineA = readerA.readLine();
      String lineB = readerB.readLine();
      while (lineA != null) {
        lineA = replaceUserName(lineA);
        lineB = replaceUserName(lineB);
        if (lineA.equals(lineB)) {
          lineA = readerA.readLine();
          lineB = readerB.readLine();
        }
        else {
          Activator.logWarning("the following lines['" + lineNr + "'] are not equal:\n'" + lineA + "'\n'" + lineB + "'");
          return false;
        }
        lineNr++;
      }
      return lineB == null;

    }
    catch (IOException e) {
      Activator.logWarning("could not copare files.", e);
      return false;
    }
    finally {
      try {
        if (readerA != null) {
          readerA.close();
        }
        if (readerB != null) {
          readerB.close();
        }
      }
      catch (IOException e) {
        Activator.logWarning("could not close readers.", e);
      }
    }

  }

  private static String replaceUserName(String s) {
    return s.replaceAll("\\$\\{user.name\\}", System.getProperty("user.name"));
  }

  /**
   * @return returns the project with the given name.
   */
  protected static IProject getProject(String projectName) {
    return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
  }
}
