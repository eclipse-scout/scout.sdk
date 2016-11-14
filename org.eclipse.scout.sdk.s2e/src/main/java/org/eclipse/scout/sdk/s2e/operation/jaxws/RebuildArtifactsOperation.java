/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.operation.jaxws;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild;
import org.eclipse.scout.sdk.core.s.util.maven.MavenRunner;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link RebuildArtifactsOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class RebuildArtifactsOperation implements IOperation {

  private IJavaProject m_javaProject;

  @Override
  public String getOperationName() {
    return "Rebuild all Web Service Artifacts of Project " + getJavaProject().getElementName();
  }

  @Override
  public void validate() {
    Validate.isTrue(S2eUtils.exists(getJavaProject()), "Java Project must exist.");
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 100);
    IProject project = getJavaProject().getProject();

    // refresh project
    project.refreshLocal(IResource.DEPTH_INFINITE, progress.newChild(2));
    if (progress.isCanceled()) {
      return;
    }

    // delete /target folder contents
    deleteOutputFolderContents(project, progress.newChild(4));
    if (progress.isCanceled()) {
      return;
    }

    // schedule maven build 'clean compile'
    progress.worked(44);

    try {
      MavenRunner.execute(new MavenBuild()
          .withGoal("clean")
          .withGoal("process-resources")
          .withWorkingDirectory(project.getLocation().toFile()));
    }
    catch (Exception e) {
      SdkLog.error("Unable to rebuild artifacts. See maven console for details.", e);
    }
    progress.worked(48);

    // refresh the project to 'see' the new artifacts
    progress.setTaskName("Refresh project");
    project.refreshLocal(IResource.DEPTH_INFINITE, progress.newChild(2));
  }

  protected void deleteOutputFolderContents(IProject project, SubMonitor progress) throws CoreException {
    final Set<IResource> resourcesToDelete = new HashSet<>();
    final IFolder outFolder = project.getFolder("target");
    if (!outFolder.exists()) {
      return;
    }

    outFolder.accept(new IResourceVisitor() {
      @Override
      public boolean visit(IResource resource) throws CoreException {
        boolean isOutFolder = Objects.equals(outFolder, resource);
        if (!isOutFolder) {
          resourcesToDelete.add(resource);
        }
        return isOutFolder;
      }
    });
    progress.beginTask("Delete existing Artifacts", resourcesToDelete.size());
    for (IResource r : resourcesToDelete) {
      try {
        r.delete(IResource.FORCE, progress.newChild(1));
      }
      catch (CoreException e) {
        SdkLog.warning("Unable to delete resource '{}'.", r.getFullPath().toOSString(), e);
      }
    }
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  public void setJavaProject(IJavaProject javaProject) {
    m_javaProject = javaProject;
  }
}
