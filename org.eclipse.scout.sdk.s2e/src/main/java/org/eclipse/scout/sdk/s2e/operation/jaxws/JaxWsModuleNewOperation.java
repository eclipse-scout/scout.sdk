/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.operation.jaxws;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsModuleNewHelper;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link JaxWsModuleNewOperation}</h3>
 *
 * @since 5.2.0
 */
public class JaxWsModuleNewOperation implements BiConsumer<EclipseEnvironment, EclipseProgress> {

  // in
  private IJavaProject m_serverModule;
  private String m_artifactId;

  // out
  private IProject m_createdProject;

  @Override
  public void accept(EclipseEnvironment env, EclipseProgress progress) {
    Ensure.isTrue(JdtUtils.exists(getServerModule()), "Target module pom file could not be found.");
    Ensure.notBlank(getArtifactId());
    progress.init(toString(), 100);

    try {
      // get pom from target project
      IFile pomFile = getServerModule().getProject().getFile(IMavenConstants.POM);
      if (!pomFile.isAccessible()) {
        throw new SdkException("{} could not be found in module '{}'.", IMavenConstants.POM, getServerModule().getElementName());
      }
      progress.worked(5);

      // create project on disk (using archetype)
      Path createdProjectDir = JaxWsModuleNewHelper.createModule(pomFile.getLocation().toFile().toPath(), getArtifactId(), env, progress.newChild(10));

      // import into workspace
      setCreatedProject(importIntoWorkspace(createdProjectDir, progress.newChild(70)));

      // refresh modified resources
      Set<IProject> modifiedProjects = getAffectedProjects(createdProjectDir);
      modifiedProjects.add(getCreatedProject()); // ensure the created project is in the set
      modifiedProjects.add(getServerModule().getProject()); // ensure the modified server project is in the set

      // run 'maven update' on created project because the parent and the dependencies have been modified
      S2eUtils.mavenUpdate(modifiedProjects, false, true, false, false, progress.newChild(15).monitor());
    }
    catch (IOException | CoreException e) {
      throw new SdkException("Unable to create Jax-Ws Module.", e);
    }
  }

  @SuppressWarnings("MethodMayBeStatic")
  protected Set<IProject> getAffectedProjects(Path createdProjectDir) throws IOException {
    Path parentPom = JaxWsModuleNewHelper.getParentPomOf(createdProjectDir.resolve(IMavenConstants.POM));
    if (parentPom == null) {
      return emptySet();
    }

    Path parentReference = parentPom.normalize();
    Collection<Path> modulesWithModifiedParent = new HashSet<>();
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (IProject candidate : root.getProjects()) {
      IFile pom = candidate.getFile(IMavenConstants.POM);
      if (pom != null && pom.exists()) {
        IPath location = pom.getLocation();
        if (location != null) {
          Path moduleLocation = location.toFile().toPath();
          Path parent = JaxWsModuleNewHelper.getParentPomOf(moduleLocation);
          if (parent != null) {
            Path canonicalFile = parent.normalize();
            if (parentReference.equals(canonicalFile)) {
              modulesWithModifiedParent.add(moduleLocation.resolve(IMavenConstants.POM));
            }
          }
        }
      }
    }
    modulesWithModifiedParent.add(parentReference);

    return modulesWithModifiedParent.stream()
        .map(Path::toUri)
        .flatMap(uri -> Stream.of(root.findFilesForLocationURI(uri)))
        .map(IResource::getProject)
        .collect(toSet());
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected static IProject importIntoWorkspace(Path createdProjectDir, EclipseProgress progress) throws CoreException {
    Set<MavenProjectInfo> projects = singleton(new MavenProjectInfo(createdProjectDir.getFileName().toString(), createdProjectDir.resolve(IMavenConstants.POM).toFile(), null, null));
    List<IMavenProjectImportResult> importedProjects = MavenPlugin.getProjectConfigurationManager().importProjects(projects, new ProjectImportConfiguration(), progress.monitor());
    if (importedProjects == null || importedProjects.isEmpty()) {
      throw newFail("Unable to import newly created project into workspace.");
    }
    return importedProjects.iterator().next().getProject();
  }

  public IJavaProject getServerModule() {
    return m_serverModule;
  }

  public void setServerModule(IJavaProject serverModule) {
    m_serverModule = serverModule;
  }

  public IProject getCreatedProject() {
    return m_createdProject;
  }

  protected void setCreatedProject(IProject createdProject) {
    m_createdProject = createdProject;
  }

  public String getArtifactId() {
    return m_artifactId;
  }

  public void setArtifactId(String artifactId) {
    m_artifactId = artifactId;
  }

  @Override
  public String toString() {
    return "Create new JAX-WS Module";
  }
}
