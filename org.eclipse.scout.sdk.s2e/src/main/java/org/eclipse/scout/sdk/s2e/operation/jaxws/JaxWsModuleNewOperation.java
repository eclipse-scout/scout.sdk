/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.operation.jaxws;

import static java.util.Collections.singleton;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathAttribute;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.scout.sdk.core.java.ISourceFolders;
import org.eclipse.scout.sdk.core.log.SdkLog;
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
      var createdProject = importIntoWorkspace(createdProjectDir, progress.newChild(70));
      setCreatedProject(createdProject);

      // run 'maven update' on created project
      S2eUtils.mavenUpdate(Collections.singleton(createdProject), false, true, false, true, progress.newChild(15).monitor());
      ensureGeneratedFoldersInClasspath(createdProject);
    }
    catch (IOException | CoreException e) {
      throw new SdkException("Unable to create Jax-Ws Module.", e);
    }
  }

  protected static void ensureGeneratedFoldersInClasspath(IProject createdProject) {
    try {
      var jp = JavaCore.create(createdProject);
      if (!JdtUtils.exists(jp)) {
        return;
      }
      if (isGeneratedClasspathPresent(jp)) {
        return; // already present
      }

      var origCp = jp.getRawClasspath();
      Collection<IClasspathEntry> entries = new ArrayList<>(origCp.length + 2);
      entries.addAll(Arrays.asList(origCp));
      entries.add(createClasspathEntry(jp, ISourceFolders.GENERATED_WS_IMPORT_SOURCE_FOLDER));
      entries.add(createClasspathEntry(jp, ISourceFolders.GENERATED_ANNOTATIONS_SOURCE_FOLDER));
      jp.setRawClasspath(entries.toArray(IClasspathEntry[]::new), true, null);
    }
    catch (CoreException e) {
      SdkLog.info("Unable to compute classpath of created JAX-WS module '{}'.", createdProject.getName());
    }
  }

  protected static IClasspathEntry createClasspathEntry(IJavaElement jp, String projectRelPath) {
    var projectPath = jp.getPath();
    return new ClasspathEntry(IPackageFragmentRoot.K_SOURCE, IClasspathEntry.CPE_SOURCE, projectPath.append(projectRelPath),
        new IPath[0], new IPath[0], null, null, projectPath.append("target/classes"), false, null, false, new IClasspathAttribute[]{
            new ClasspathAttribute(IClasspathAttribute.OPTIONAL, Boolean.TRUE.toString()),
            new ClasspathAttribute("maven.pomderived", Boolean.TRUE.toString())
        });
  }

  protected static boolean isGeneratedClasspathPresent(IJavaProject jp) throws JavaModelException {
    return Arrays.stream(jp.getRawClasspath())
        .map(IClasspathEntry::getPath)
        .filter(Objects::nonNull)
        .map(IPath::toPortableString)
        .anyMatch(p -> p.contains(ISourceFolders.GENERATED_WS_IMPORT_SOURCE_FOLDER) || p.contains(ISourceFolders.GENERATED_ANNOTATIONS_SOURCE_FOLDER));
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
