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
package org.eclipse.scout.sdk.s2e.operation.project;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.environment.CompilationUnitWriteOperation;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * <h3>{@link ScoutProjectNewOperation}</h3>
 *
 * @since 5.1.0
 */
public class ScoutProjectNewOperation implements BiConsumer<EclipseEnvironment, EclipseProgress> {

  public static final String TEMPLATE_VERSION = "org.eclipse.scout.archetype.version";
  protected static final String EXEC_ENV_PREFIX = "JavaSE-";
  protected static final String MIN_JVM_VERSION = "1.8";

  private String m_groupId;
  private String m_artifactId;
  private String m_displayName;
  private boolean m_useJsClient = true;
  private Path m_targetDirectory;
  private List<IProject> m_createdProjects;

  @Override
  public void accept(EclipseEnvironment env, EclipseProgress progress) {
    try {
      // get archetype settings
      BundleContext bundleContext = S2ESdkActivator.getDefault().getBundle().getBundleContext();
      String version = bundleContext.getProperty(TEMPLATE_VERSION);
      if (Strings.isBlank(version)) {
        version = ScoutProjectNewHelper.SCOUT_ARCHETYPES_VERSION;
      }

      String artifactId;
      if (isUseJsClient()) {
        artifactId = ScoutProjectNewHelper.SCOUT_ARCHETYPES_HELLOJS_ARTIFACT_ID;
      }
      else {
        artifactId = ScoutProjectNewHelper.SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID;
      }

      // create project on disk (using archetype)
      progress.init(100, toString());
      ScoutProjectNewHelper.createProject(getTargetDirectory(), getGroupId(), getArtifactId(), getDisplayName(), getDefaultWorkspaceJavaVersion(),
          ScoutProjectNewHelper.SCOUT_ARCHETYPES_GROUP_ID, artifactId, version, env, progress.newChild(5));

      // import into workspace
      m_createdProjects = importIntoWorkspace(progress.newChild(90));

      // format all compilation units with current workspace settings
      formatCreatedProjects(progress.newChild(5));
    }
    catch (IOException | CoreException e) {
      throw new SdkException("Unable to create Scout Project.", e);
    }
  }

  protected static String getDefaultWorkspaceJavaVersion() {
    return versionToString(computeDefaultWorkspaceJavaVersion());
  }

  /**
   * Converts the specified {@link Version} to a {@link String}. Only the major and minor parts are used. Trailing
   * zeroes are omitted.<br>
   *
   * @param version
   *          The {@link Version} to convert.
   * @return E.g. "1.8" or "9".
   */
  protected static String versionToString(Version version) {
    StringBuilder b = new StringBuilder(4);
    b.append(version.getMajor());
    if (version.getMinor() != 0) {
      b.append('.').append(version.getMinor());
    }
    return b.toString();
  }

  /**
   * Gets the default Java version supported by the current default JVM of the workspace.
   *
   * @return A {@link Version} like "1.8.0" or "9.0.0" with the latest version supported in the current default JVM.
   */
  protected static Version computeDefaultWorkspaceJavaVersion() {
    Version result = Version.parseVersion(MIN_JVM_VERSION);
    IVMInstall defaultVm = JavaRuntime.getDefaultVMInstall();
    if (defaultVm == null) {
      return result;
    }

    for (IExecutionEnvironment env : JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments()) {
      if (env.isStrictlyCompatible(defaultVm)) {
        Version cur = execEnvironmentToVersion(env.getId());
        if (cur.compareTo(result) > 0) {
          result = cur; // take the newest
        }
      }
    }
    return result;
  }

  /**
   * Takes a Java execution environment (e.g. "JavaSE-1.8" or "JavaSE-9") and converts it to a {@link Version}.<br>
   * If an invalid value is passed, always 1.8 is returned as minimal version.<br>
   *
   * @param executionEnvId
   *          The execution environment of the form "JavaSE-1.8" or "JavaSE-9 to parse.
   * @return The {@link Version} holding the decimal equivalent value. E.g. {@code 1.8.0} or {@code 9.0.0}.
   */
  protected static Version execEnvironmentToVersion(String executionEnvId) {
    if (executionEnvId != null && executionEnvId.startsWith(EXEC_ENV_PREFIX)) {
      String numPart = executionEnvId.substring(EXEC_ENV_PREFIX.length());
      if (Strings.hasText(numPart)) {
        try {
          return Version.parseVersion(numPart);
        }
        catch (IllegalArgumentException e) {
          SdkLog.warning("Invalid number part ({}) in execution environment {}.", numPart, executionEnvId, e);
        }
      }
    }
    return Version.parseVersion(MIN_JVM_VERSION);
  }

  protected void formatCreatedProjects(EclipseProgress progress) throws CoreException {
    progress.init(m_createdProjects.size(), "Format created projects");
    for (IProject createdProject : m_createdProjects) {
      if (createdProject.isAccessible() && createdProject.hasNature(JavaCore.NATURE_ID)) {
        IJavaProject jp = JavaCore.create(createdProject);
        if (JdtUtils.exists(jp)) {
          formatProject(progress.newChild(1), jp);
        }
      }
    }
  }

  protected static void formatProject(EclipseProgress progress, IJavaProject p) throws JavaModelException {
    for (IPackageFragment pck : p.getPackageFragments()) {
      for (ICompilationUnit u : pck.getCompilationUnits()) {
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
    try (Stream<Path> files = Files.list(getTargetDirectory().resolve(getArtifactId()))) {
      subFolders = files.collect(toList());
    }
    catch (IOException e) {
      throw new CoreException(new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Unable to list content of " + getTargetDirectory(), e));
    }

    Collection<MavenProjectInfo> projects = new ArrayList<>(subFolders.size());
    for (Path subFolder : subFolders) {
      Path pom = subFolder.resolve(IMavenConstants.POM);
      if (Files.isReadable(pom) && Files.isRegularFile(pom)) {
        projects.add(new MavenProjectInfo(subFolder.getFileName().toString(), pom.toFile(), null, null));
      }
    }

    List<IMavenProjectImportResult> importedProjects = MavenPlugin.getProjectConfigurationManager().importProjects(projects, new ProjectImportConfiguration(), progress.newChild(projects.size()).monitor());

    List<IProject> result = new ArrayList<>(importedProjects.size());
    for (IMavenProjectImportResult mavenProject : importedProjects) {
      if (mavenProject.getProject() != null) {
        result.add(mavenProject.getProject());
      }
    }
    return result;
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

  @Override
  public String toString() {
    return "Create new Scout Project";
  }
}
