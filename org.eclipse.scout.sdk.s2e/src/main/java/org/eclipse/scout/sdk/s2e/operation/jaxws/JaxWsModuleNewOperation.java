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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.m2e.core.MavenPlugin;
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
    progress.init(100, toString());

    try {
      // get pom from target project
      var pomFile = getServerModule().getProject().getFile(IMavenConstants.POM);
      if (!pomFile.isAccessible()) {
        throw new SdkException("{} could not be found in module '{}'.", IMavenConstants.POM, getServerModule().getElementName());
      }
      progress.worked(5);

      // create project on disk (using archetype)
      var createdProjectDir = JaxWsModuleNewHelper.createModule(pomFile.getLocation().toFile().toPath(), getArtifactId(), env, progress.newChild(10));

      // import into workspace
      setCreatedProject(importIntoWorkspace(createdProjectDir, progress.newChild(70)));

      // refresh modified resources
      var modifiedProjects = getAffectedProjects(createdProjectDir);
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
    var parentPom = JaxWsModuleNewHelper.getParentPomOf(createdProjectDir.resolve(IMavenConstants.POM));
    if (parentPom == null) {
      return emptySet();
    }

    var parentReference = parentPom.normalize();
    Collection<Path> modulesWithModifiedParent = new HashSet<>();
    var root = ResourcesPlugin.getWorkspace().getRoot();
    for (var candidate : root.getProjects()) {
      var pom = candidate.getFile(IMavenConstants.POM);
      if (pom != null && pom.exists()) {
        var location = pom.getLocation();
        if (location != null) {
          var moduleLocation = location.toFile().toPath();
          var parent = JaxWsModuleNewHelper.getParentPomOf(moduleLocation);
          if (parent != null) {
            var canonicalFile = parent.normalize();
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
        .flatMap(uri -> Arrays.stream(root.findFilesForLocationURI(uri)))
        .map(IResource::getProject)
        .collect(toSet());
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected static IProject importIntoWorkspace(Path createdProjectDir, EclipseProgress progress) throws CoreException {
    var projects = singleton(new MavenProjectInfo(createdProjectDir.getFileName().toString(), createdProjectDir.resolve(IMavenConstants.POM).toFile(), null, null));
    var importedProjects = MavenPlugin.getProjectConfigurationManager().importProjects(projects, new ProjectImportConfiguration(), progress.monitor());
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
