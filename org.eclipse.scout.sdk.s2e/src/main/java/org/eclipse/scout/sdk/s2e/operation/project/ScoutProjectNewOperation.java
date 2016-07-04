/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.operation.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.operation.CompilationUnitWriteOperation;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutStatus;

/**
 * <h3>{@link ScoutProjectNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class ScoutProjectNewOperation implements IOperation {

  public static final String TEMPLATE_GROUP_ID = "org.eclipse.scout.archetype.groupId";
  public static final String TEMPLATE_ARTIFACT_ID = "org.eclipse.scout.archetype.artifactId";
  public static final String TEMPLATE_VERSION = "org.eclipse.scout.archetype.version";

  private String m_groupId;
  private String m_artifactId;
  private String m_displayName;
  private String m_javaVersion;
  private File m_targetDirectory;
  private List<IProject> m_createdProjects;

  @Override
  public String getOperationName() {
    return "Creating new Scout project...";
  }

  @Override
  public void validate() {
    // is done in ScoutProjectNewHelper
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      // get archetype settings
      String groupId = S2ESdkActivator.getDefault().getBundle().getBundleContext().getProperty(TEMPLATE_GROUP_ID);
      String artifactId = S2ESdkActivator.getDefault().getBundle().getBundleContext().getProperty(TEMPLATE_ARTIFACT_ID);
      String version = S2ESdkActivator.getDefault().getBundle().getBundleContext().getProperty(TEMPLATE_VERSION);
      if (StringUtils.isBlank(groupId) || StringUtils.isBlank(artifactId) || StringUtils.isBlank(version)) {
        // use default
        groupId = null;
        artifactId = null;
        version = null;
      }

      if (monitor.isCanceled()) {
        return;
      }

      // create project on disk (using archetype)
      SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 100);
      ScoutProjectNewHelper.createProject(getTargetDirectory(), getGroupId(), getArtifactId(), getDisplayName(), getJavaVersion(), groupId, artifactId, version);
      progress.worked(5);

      // import into workspace
      m_createdProjects = importIntoWorkspace(progress.newChild(90));

      // format all compilation units with current workspace settings
      formatCreatedProjects(progress.newChild(5), workingCopyManager);
    }
    catch (Exception e) {
      throw new CoreException(new ScoutStatus("Unable to create Scout Project.", e));
    }
  }

  protected void formatCreatedProjects(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    monitor.beginTask("Format created projects", m_createdProjects.size());
    for (IProject createdProject : m_createdProjects) {
      if (createdProject.isAccessible() && createdProject.hasNature(JavaCore.NATURE_ID)) {
        IJavaProject jp = JavaCore.create(createdProject);
        if (S2eUtils.exists(jp)) {
          formatProject(monitor, workingCopyManager, jp);
        }
      }
      monitor.worked(1);
    }
  }

  protected static void formatProject(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager, IJavaProject p) throws CoreException {
    for (IPackageFragment pck : p.getPackageFragments()) {
      for (ICompilationUnit u : pck.getCompilationUnits()) {
        // the cu write operation also formats the unit. just overwrite with itself.
        CompilationUnitWriteOperation w = new CompilationUnitWriteOperation(u, u.getSource());
        w.validate();
        w.run(monitor, workingCopyManager);
      }
    }
  }

  /**
   * Imports the extracted projects into the workspace using m2e import
   *
   * @throws CoreException
   */
  protected List<IProject> importIntoWorkspace(IProgressMonitor monitor) throws CoreException {
    File baseFolder = new File(getTargetDirectory(), getArtifactId());
    File[] subFolders = baseFolder.listFiles();
    if (subFolders == null) {
      return Collections.emptyList();
    }

    Collection<MavenProjectInfo> projects = new ArrayList<>(subFolders.length);
    for (File subFolder : subFolders) {
      File pom = new File(subFolder, IMavenConstants.POM);
      if (pom.isFile()) {
        projects.add(new MavenProjectInfo(subFolder.getName(), pom, null, null));
      }
    }

    List<IMavenProjectImportResult> importedProjects = MavenPlugin.getProjectConfigurationManager().importProjects(projects, new ProjectImportConfiguration(), monitor);

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

  public File getTargetDirectory() {
    return m_targetDirectory;
  }

  public void setTargetDirectory(File targetDirectory) {
    m_targetDirectory = targetDirectory;
  }

  public String getJavaVersion() {
    return m_javaVersion;
  }

  public void setJavaVersion(String javaVersion) {
    m_javaVersion = javaVersion;
  }

  public List<IProject> getCreatedProjects() {
    return m_createdProjects;
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
}
