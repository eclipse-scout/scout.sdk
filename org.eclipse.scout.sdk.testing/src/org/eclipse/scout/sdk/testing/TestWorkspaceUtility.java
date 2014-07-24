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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
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
import org.eclipse.scout.sdk.jdt.compile.ICompileResult;
import org.eclipse.scout.sdk.jdt.compile.ScoutSeverityManager;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.testing.internal.SdkTestingApi;
import org.eclipse.scout.sdk.util.internal.typecache.HierarchyCache;
import org.eclipse.scout.sdk.util.internal.typecache.ICacheableTypeHierarchyResult;
import org.eclipse.scout.sdk.util.internal.typecache.TypeCache;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.junit.Assert;
import org.osgi.framework.Bundle;

/**
 * <h3>{@link TestWorkspaceUtility}</h3>Contains helper methods that affect the running workspace.
 *
 * @author Andreas Hoegger
 * @since 3.9.0 15.03.2013
 */
public final class TestWorkspaceUtility {

  private TestWorkspaceUtility() {
  }

  /**
   * Copies and creates the given Eclipse projects in the current workspace.
   *
   * @param resourceBundle
   *          The bundle that contains the project resources.
   * @param baseFolder
   *          The folder (inside the bundle) that contains the project folders
   * @param projects
   *          Array of project names (folder names inside the base folder that contain a .project file).
   * @throws CoreException
   * @throws IOException
   */
  public static void setupWorkspace(Bundle resourceBundle, String baseFolder, String... projects) throws CoreException, IOException {
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
   * @throws CoreException
   * @throws Exception
   */
  public static void clearWorkspace() throws CoreException {
    Job delJob = new Job("") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
          try {
            p.refreshLocal(IResource.DEPTH_INFINITE, monitor);
            p.delete(true, true, monitor);
          }
          catch (Exception e) {
            SdkTestingApi.logWarning("Unable to delete project '" + p + "'.", e);
          }
        }
        return Status.OK_STATUS;
      }
    };
    delJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
    delJob.schedule();
    try {
      delJob.join();
    }
    catch (InterruptedException e) {
      throw new CoreException(new ScoutStatus(e));
    }

    IProgressMonitor monitor = new NullProgressMonitor();
    ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
    JdtUtility.waitForSilentWorkspace();

    ResourcesPlugin.getWorkspace().forgetSavedTree(null);
    ResourcesPlugin.getWorkspace().getRoot().clearHistory(monitor);
  }

  /**
   * Executes the given operations and builds the workspace afterwards. This method blocks until the workspace build has
   * completed.
   *
   * @param ops
   *          The operations to execute.
   * @throws CoreException
   */
  public static void executeAndBuildWorkspace(IOperation... ops) throws CoreException {
    OperationJob job = new OperationJob(ops);
    job.schedule();
    try {
      job.join();
    }
    catch (InterruptedException e) {
      throw new CoreException(new ScoutStatus(e));
    }
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

  public static void buildWorkspaceAndAssertNoCompileErrors() throws CoreException {
    buildWorkspace();
    assertNoCompileErrors();
  }

  /**
   * Recompiles the entire workspace. This method blocks until the build has been completed.
   *
   * @throws CoreException
   */
  public static void buildWorkspace() throws CoreException {
    final IProgressMonitor monitor = new NullProgressMonitor();

    ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
    waitForNotifyJob();
    JdtUtility.waitForSilentWorkspace();

    ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
    JdtUtility.waitForSilentWorkspace();
    waitForNotifyJob();

    ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
    JdtUtility.waitForSilentWorkspace();
    waitForNotifyJob();
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

  public static void assertNoCompileErrors() {
    ICompileResult result = ScoutSeverityManager.getInstance().getCompileResult(ResourcesPlugin.getWorkspace().getRoot());
    if (result.getSeverity() >= IMarker.SEVERITY_ERROR) {
      StringBuilder builder = new StringBuilder("Compile errors:\n");
      List<IMarker> errorMarkers = result.getErrorMarkers();
      for (int i = 0; i < errorMarkers.size(); i++) {
        if (i > 0) {
          builder.append("\n");
        }
        IMarker m = errorMarkers.get(i);
        int severity = m.getAttribute(IMarker.SEVERITY, -1);
        switch (severity) {
          case IMarker.SEVERITY_INFO:
            builder.append("INFO: ");
            break;
          case IMarker.SEVERITY_WARNING:
            builder.append("WARNING: ");
            break;
          case IMarker.SEVERITY_ERROR:
            builder.append("ERROR: ");
            break;
          default:
            builder.append("UNDEFINED SEVERITY: ");
            break;
        }
        IResource resource = m.getResource();
        if (ResourceUtility.exists(resource)) {
          builder.append(resource.getName()).append(" - ").append(resource.getParent().getProjectRelativePath()).append(" - ");
        }
        builder.append("line:").append(m.getAttribute(IMarker.LINE_NUMBER, -1)).append(" - ");
        builder.append(m.getAttribute(IMarker.MESSAGE, "")).append(" ");
      }
      Assert.fail(builder.toString());
    }
  }

  protected static void copyProject(Bundle resourceBundle, String... pathElements) throws IOException {
    URL resource = FileLocator.find(resourceBundle, createPath(pathElements), null);
    if (resource != null) {
      String path = FileLocator.toFileURL(resource).getPath();
      copyFilesRecursive(path, ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
    }
  }

  /**
   * Imports and opens the given project in the workspace.
   *
   * @param projectName
   *          The name of the project
   * @return The created project.
   * @throws CoreException
   */
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
        if (!subDir.mkdir()) {
          throw new IOException("Unable to create file directory '" + subDir.getAbsolutePath() + "'.");
        }
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
            catch (Exception t) {
              // nop
            }
          }
          if (out != null) {
            try {
              out.close();
            }
            catch (Exception t) {
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
    for (ICacheableTypeHierarchyResult h : HierarchyCache.getInstance().getAllCachedHierarchies()) {
      out.println(" - " + h.getBaseType().getFullyQualifiedName());
    }
    out.println("TypeCache: (size='" + TypeCache.getInstance().getAllCachedTypes().size() + "')");

    out.println("--- END [" + title + "] ----");
  }

  /**
   * Appends the content of a file in the workspace to a {@link StringBuilder}.
   *
   * @param project
   *          The project that contains the file.
   * @param ressourcePath
   *          The project relative path to the file. Must exist.
   * @param source
   *          The builder to which the content of the given file should be appended. Must not be null.
   * @param lineDelimiter
   *          The line delimiter that should be used when appending lines to the builder.
   * @throws CoreException
   */
  public static void copyTemplateToBuffer(IProject project, String ressourcePath, StringBuilder source, String lineDelimiter) throws CoreException {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(((IFile) project.findMember(ressourcePath)).getContents()));
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        source.append(line).append(lineDelimiter);
      }
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus(e));
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
