/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.testing;

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
import java.io.PrintStream;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.junit.Assert;
import org.osgi.framework.Bundle;

public final class TestWorkspaceUtility {

  private TestWorkspaceUtility() {
  }

  public static void setupWorkspace(Bundle resourceBundle, String baseFolder, String... projects) throws Exception {
    Assert.assertNotNull("baseFolder must not be null", baseFolder);
    if (projects == null || projects.length == 0) {
      projects = new String[]{null};
    }
    for (String project : projects) {
      IProject javaProject = createProject(project);
      copyProject(resourceBundle, baseFolder, project);
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

  public static void executeAndBuildWorkspace(IOperation... ops) throws Exception {
    OperationJob job = new OperationJob(ops);
    job.schedule();
    job.join();
    if (!job.getResult().isOK()) {
      StringBuilder statusBuilder = new StringBuilder();
      createStatusLog(job.getResult(), statusBuilder, 0);
      Assert.fail(statusBuilder.toString());
    }
    Assert.assertTrue(job.getResult().isOK());
    buildWorkspace();
  }

  private static void createStatusLog(IStatus status, StringBuilder builder, int indent) {
    for (int i = 0; i < indent; i++) {
      builder.append("  ");
    }
    if (status.isMultiStatus()) {
      builder.append("Multi Status:").append("\n");
      for (IStatus childStatus : ((MultiStatus) status).getChildren()) {
        createStatusLog(childStatus, builder, indent + 1);
      }
    }
    switch (status.getSeverity()) {
      case IStatus.CANCEL:
        builder.append("CANCEL: ");
        break;
      case IStatus.ERROR:
        builder.append("ERROR: ");
        break;
      case IStatus.WARNING:
        builder.append("WARNING: ");
        break;
    }
    builder.append(status.getMessage());
  }

  public static void buildWorkspace() throws CoreException {
    ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    JdtUtility.waitForRefresh();
    waitForNotifyJob();
    JdtUtility.waitForIndexesReady();
    ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
    ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
    JdtUtility.waitForSilentWorkspace();
  }

  private static void waitForNotifyJob() throws CoreException {
    final IWorkspaceRunnable noop = new IWorkspaceRunnable() {
      @Override
      public void run(IProgressMonitor monitor) {
        // do nothing
      }
    };
    ResourcesPlugin.getWorkspace().run(noop, null, IResource.NONE, null);
  }

  protected static void copyProject(Bundle resourceBundle, String... pathElements) throws IOException {
    URL resource = FileLocator.find(resourceBundle, createPath(pathElements), null);
    if (resource != null) {
      String path = FileLocator.toFileURL(resource).getPath();
      copyFilesRecursive(path, ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
    }
  }

  public static IProject createProject(String projectName) throws CoreException {
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

  private static void copyFilesRecursive(String from, String toDir) throws IOException {
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

  /**
   * @return returns the project with the given name.
   */
  public static IProject getProject(String projectName) {
    return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
  }

  public void printWorkspace(String title, PrintStream out) {

    out.println("---print WS [" + title + "] ----");
    out.println("Bundles:");
    for (IScoutBundle b : ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getAllBundlesFilter())) {
      if (b != null) {
        out.println(" - " + b.getSymbolicName());
      }
      else {
        out.println(" - Null bundle");
      }
    }
    out.println("Hierarchies:");
    for (IPrimaryTypeTypeHierarchy h : TypeUtility.getAllCachedPrimaryTypeHierarchies()) {
      out.println(" - " + h.getType().getFullyQualifiedName());
    }
    out.println("TypeCache: (size='" + TypeUtility.getAllCachedTypes().length + "')");

    out.println("--- END [" + title + "] ----");
  }

  public static void copyTemplateToBuffer(IProject project, String ressourcePath, StringBuilder source, String lineDelimiter) throws CoreException {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(((IFile) project.findMember(ressourcePath)).getContents()));
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        source.append(line).append(lineDelimiter);
      }
    }
    catch (IOException e) {
      Assert.fail(e.getMessage());
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException e) {
          // void
        }
      }
    }
  }

}
