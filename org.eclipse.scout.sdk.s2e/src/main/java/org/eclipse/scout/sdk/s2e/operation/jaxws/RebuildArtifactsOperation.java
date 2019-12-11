/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.operation.jaxws;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.toScoutProgress;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild;
import org.eclipse.scout.sdk.core.s.util.maven.MavenRunner;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link RebuildArtifactsOperation}</h3>
 *
 * @since 5.2.0
 */
public class RebuildArtifactsOperation implements BiConsumer<IEnvironment, IProgress> {

  private final IJavaProject m_javaProject;

  public RebuildArtifactsOperation(IJavaProject javaProject) {
    m_javaProject = Ensure.notNull(javaProject);
  }

  @Override
  public void accept(IEnvironment env, IProgress p) {
    Ensure.isTrue(JdtUtils.exists(getJavaProject()), "Java Project must exist.");
    EclipseProgress progress = toScoutProgress(p).init(toString(), 100);

    try {
      // refresh project
      IProject project = getJavaProject().getProject();
      project.refreshLocal(IResource.DEPTH_INFINITE, progress.newChild(2).monitor());

      // delete /target folder contents
      deleteOutputFolderContents(project, progress.newChild(44).monitor());

      rebuildArtifacts(project, env, progress.newChild(52));

      // refresh the project to 'see' the new artifacts
      progress.monitor().setTaskName("Refresh project");
      project.refreshLocal(IResource.DEPTH_INFINITE, progress.newChild(2).monitor());
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
  }

  protected static void rebuildArtifacts(IResource workingDir, IEnvironment env, IProgress p) {
    try {
      // schedule maven build 'clean process-resources'
      MavenRunner.execute(new MavenBuild()
          .withGoal("clean")
          .withGoal("process-resources")
          .withWorkingDirectory(workingDir.getLocation().toFile().toPath()), env, p);
    }
    catch (RuntimeException e) {
      SdkLog.error("Unable to rebuild artifacts. See maven console for details.", e);
    }
  }

  protected static void deleteOutputFolderContents(IProject project, SubMonitor progress) throws CoreException {
    IFolder outFolder = project.getFolder("target");
    if (!outFolder.exists()) {
      return;
    }

    Collection<IResource> resourcesToDelete = new HashSet<>();
    outFolder.accept(resource -> {
      boolean isOutFolder = Objects.equals(outFolder, resource);
      if (!isOutFolder) {
        resourcesToDelete.add(resource);
      }
      return isOutFolder;
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

  @Override
  public String toString() {
    return "Rebuild Web Service Artifacts";
  }
}
