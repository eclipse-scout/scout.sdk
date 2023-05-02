/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.operation.project;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.environment.CompilationUnitWriteOperation;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.osgi.framework.Version;

/**
 * <h3>{@link ScoutProjectNewOperation}</h3>
 *
 * @since 5.1.0
 */
public class ScoutProjectNewOperation implements BiConsumer<EclipseEnvironment, EclipseProgress> {

  private String m_groupId;
  private String m_artifactId;
  private String m_displayName;
  private boolean m_useJsClient;
  private String m_scoutVersion;
  private Path m_targetDirectory;
  private List<IProject> m_createdProjects;

  @Override
  public void accept(EclipseEnvironment env, EclipseProgress progress) {
    try {
      // create project on disk (using archetype)
      progress.init(100, toString());
      ScoutProjectNewHelper.createProject(getTargetDirectory(), getGroupId(), getArtifactId(), getDisplayName(), isUseJsClient(), getJavaVersion(), getScoutVersion(), env, progress.newChild(5));

      // import into workspace
      m_createdProjects = importIntoWorkspace(progress.newChild(90));

      // format all compilation units with current workspace settings
      formatCreatedProjects(progress.newChild(5));
    }
    catch (Exception e) {
      throw new SdkException("Unable to create Scout Project.", e);
    }
  }

  /**
   * @return The newest major Java version (e.g. 8 or 11 or 17) which is installed in the Eclipse workspace and is
   *         supported by the {@link #getScoutVersion() selected Scout version}. If no of the JDKs registered in Eclipse
   *         is supported by the selected Scout version, the newest major Java version that is supported by the Scout
   *         version is returned. In this case the generated project is correct, but a matching JDK will be missing in
   *         the workspace.
   */
  protected String getJavaVersion() {
    var supportedJavaVersions = ScoutProjectNewHelper.getSupportedJavaVersions(getScoutVersion());
    var supportedJavaVersionSet = Arrays.stream(supportedJavaVersions).boxed().collect(toSet());
    var javaVersion = Arrays.stream(JavaRuntime.getVMInstallTypes())
        .filter(StandardVMType.class::isInstance)
        .flatMap(t -> Arrays.stream(t.getVMInstalls()))
        .filter(IVMInstall2.class::isInstance)
        .map(IVMInstall2.class::cast)
        .map(IVMInstall2::getJavaVersion)
        .filter(Strings::hasText)
        .map(Version::new)
        .mapToInt(v -> v.getMajor() >= 9 ? v.getMajor() : v.getMinor())
        .filter(supportedJavaVersionSet::contains)
        .max()
        .orElseGet(() -> Arrays.stream(supportedJavaVersions).max().orElseThrow());
    return Integer.toString(javaVersion);
  }

  protected void formatCreatedProjects(EclipseProgress progress) throws CoreException {
    progress.init(m_createdProjects.size(), "Format created projects");
    for (var createdProject : m_createdProjects) {
      if (createdProject.isAccessible() && createdProject.hasNature(JavaCore.NATURE_ID)) {
        var jp = JavaCore.create(createdProject);
        if (JdtUtils.exists(jp)) {
          formatProject(progress.newChild(1), jp);
        }
      }
    }
  }

  protected static void formatProject(EclipseProgress progress, IJavaProject p) throws JavaModelException {
    for (var pck : p.getPackageFragments()) {
      for (var u : pck.getCompilationUnits()) {
        // the cu write operation also formats the unit. just overwrite with itself.
        new CompilationUnitWriteOperation(u, u.getSource()).accept(progress);
      }
    }
  }

  /**
   * Imports the extracted projects into the workspace using m2e import
   */
  @SuppressWarnings({"findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "TypeMayBeWeakened"})
  protected List<IProject> importIntoWorkspace(EclipseProgress progress) throws CoreException {
    List<Path> subFolders;
    try (var files = Files.list(getTargetDirectory().resolve(getArtifactId()))) {
      subFolders = files.collect(toList());
    }
    catch (IOException e) {
      throw new CoreException(new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Unable to list content of " + getTargetDirectory(), e));
    }

    Collection<MavenProjectInfo> projects = new ArrayList<>(subFolders.size());
    for (var subFolder : subFolders) {
      var pom = subFolder.resolve(IMavenConstants.POM);
      if (Files.isReadable(pom) && Files.isRegularFile(pom)) {
        projects.add(new MavenProjectInfo(subFolder.getFileName().toString(), pom.toFile(), null, null));
      }
    }

    return MavenPlugin.getProjectConfigurationManager()
        .importProjects(projects, new ProjectImportConfiguration(), progress.newChild(projects.size()).monitor())
        .stream()
        .filter(Objects::nonNull)
        .map(IMavenProjectImportResult::getProject)
        .filter(Objects::nonNull)
        .collect(toList());
  }

  public String getDisplayName() {
    return m_displayName;
  }

  public void setDisplayName(String displayName) {
    m_displayName = displayName;
  }

  public Path getTargetDirectory() {
    return m_targetDirectory;
  }

  public void setTargetDirectory(Path targetDirectory) {
    m_targetDirectory = targetDirectory;
  }

  public List<IProject> getCreatedProjects() {
    return unmodifiableList(m_createdProjects);
  }

  public String getGroupId() {
    return m_groupId;
  }

  public void setGroupId(String groupId) {
    m_groupId = groupId;
  }

  public String getArtifactId() {
    return m_artifactId;
  }

  public void setArtifactId(String artifactId) {
    m_artifactId = artifactId;
  }

  public boolean isUseJsClient() {
    return m_useJsClient;
  }

  public void setUseJsClient(boolean useJsClient) {
    m_useJsClient = useJsClient;
  }

  public String getScoutVersion() {
    return m_scoutVersion;
  }

  public void setScoutVersion(String scoutVersion) {
    m_scoutVersion = scoutVersion;
  }

  @Override
  public String toString() {
    return "Create new Scout Project";
  }
}
