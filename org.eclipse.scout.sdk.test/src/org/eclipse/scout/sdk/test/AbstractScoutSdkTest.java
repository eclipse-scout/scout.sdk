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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.test.Activator;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.Assert;
import org.osgi.framework.Bundle;

public abstract class AbstractScoutSdkTest {

  private static final String RESOURCES_FOLDER_NAME = "resources";

  protected static void showEgitMessageBoxes(boolean show) {
    try {
      // preference store as defined in org.eclipse.ui.plugin.AbstractUIPlugin.getPreferenceStore()
      IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), "org.eclipse.egit.ui");

      // following constants are coming from class org.eclipse.egit.ui.UIPreferences:
      store.setValue("show_detached_head_warning", show);
      store.setValue("show_git_prefix_warning", show);
      store.setValue("show_home_drive_warning", show);
      store.setValue("show_initial_config_dialog", show);
      store.setValue("show_rebase_confirm", show);
    }
    catch (Throwable e) {
      //NOP
    }
  }

  protected static void setupWorkspace(String baseFolder, String... projects) throws Exception {
    setupWorkspace(Platform.getBundle(Activator.PLUGIN_ID), baseFolder, projects);
  }

  protected static void setupWorkspace(Bundle resourceBundle, String baseFolder, String... projects) throws Exception {
    showEgitMessageBoxes(false);
    JdtUtility.setWorkspaceAutoBuilding(false);
    setAutoUpdateFormData(false);
    Assert.assertNotNull("baseFolder must not be null", baseFolder);
    if (projects == null || projects.length == 0) {
      projects = new String[]{null};
    }
    for (String project : projects) {
      IProject javaProject = createProject(project);
      copyProject(resourceBundle, RESOURCES_FOLDER_NAME, baseFolder, project);
      javaProject.close(null);
      javaProject.open(null);
    }
    buildWorkspace();
  }

  /**
   * deletes all workspace projects and waits for a silent workspace
   * 
   * @throws Exception
   */
  public static void clearWorkspace() throws Exception {
    Job delJob = new Job("") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            p.refreshLocal(IResource.DEPTH_INFINITE, monitor);
            p.delete(true, true, monitor);
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        return Status.OK_STATUS;
      }
    };
    delJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
    delJob.schedule();
    delJob.join();
    ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    JdtUtility.waitForSilentWorkspace();
  }

  protected static void setAutoUpdateFormData(boolean autoBuild) {
    ScoutSdk.getDefault().setFormDataAutoUpdate(autoBuild);
  }

  protected static void executeAndBuildWorkspace(IOperation... ops) throws Exception {
    OperationJob job = new OperationJob(ops);
    job.schedule();
    job.join();
    buildWorkspace();
  }

  public static void buildWorkspace() throws Exception {
    ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    JdtUtility.waitForRefresh();
    waitForNotifyJob();
    JdtUtility.waitForIndexesReady();
    ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
    ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
    JdtUtility.waitForSilentWorkspace();
  }

  public static void waitForNotifyJob() throws Exception {
    final IWorkspaceRunnable noop = new IWorkspaceRunnable() {
      @Override
      public void run(IProgressMonitor monitor) {
        // do nothing
      }
    };
    ResourcesPlugin.getWorkspace().run(noop, null, IResource.NONE, null);
//    System.out.println("start wait " + System.currentTimeMillis());
//    NotificationLock.waitForNotification();
//    System.out.println("end wait " + System.currentTimeMillis());
  }

  protected static void copyProject(Bundle resourceBundle, String... pathElements) throws IOException {
    URL resource = FileLocator.find(resourceBundle, createPath(pathElements), null);
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

  protected static Path createPath(String... pathElements) {
    StringBuilder builder = new StringBuilder();
    for (String e : pathElements) {
      if (e != null) {
        builder.append("/");
        builder.append(e);
      }
    }
    return new Path(builder.toString());
  }

  protected static void copyFilesRecursive(String from, String toDir) throws IOException {
    File fromFile = new File(from);
    if (!fromFile.exists()) {
      return;
    }
    if (fromFile.isDirectory()) {
      if (".svn".equalsIgnoreCase(fromFile.getName())) {
        return;
      }
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
      Activator.logWarning("could not compare files.", e);
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

  protected void printWorkspace(String title) {
    System.out.println("---print WS [" + title + "] ----");
    System.out.println("Bundles:");
    for (IScoutBundle b : ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getAllBundlesFilter())) {
      if (b != null) {
        System.out.println(" - " + b.getSymbolicName());
      }
      else {
        System.out.println(" - Null bundle");
      }
    }
    System.out.println("Hierarchies:");
    for (IPrimaryTypeTypeHierarchy h : TypeUtility.getAllCachedPrimaryTypeHierarchies()) {
      System.out.println(" - " + h.getType().getFullyQualifiedName());
    }
    System.out.println("TypeCache: (size='" + TypeUtility.getAllCachedTypes().length + "')");

    System.out.println("--- END [" + title + "] ----");
  }

}
