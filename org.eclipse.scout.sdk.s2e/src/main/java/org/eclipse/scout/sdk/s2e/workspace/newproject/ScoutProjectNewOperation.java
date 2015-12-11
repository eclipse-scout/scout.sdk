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
package org.eclipse.scout.sdk.s2e.workspace.newproject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;
import org.eclipse.scout.sdk.s2e.workspace.IOperation;
import org.eclipse.scout.sdk.s2e.workspace.IWorkingCopyManager;

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

  private String m_symbolicName;
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
      // archetype settings
      String groupId = S2ESdkActivator.getDefault().getBundle().getBundleContext().getProperty(TEMPLATE_GROUP_ID);
      String artifactId = S2ESdkActivator.getDefault().getBundle().getBundleContext().getProperty(TEMPLATE_ARTIFACT_ID);
      String version = S2ESdkActivator.getDefault().getBundle().getBundleContext().getProperty(TEMPLATE_VERSION);
      if (StringUtils.isBlank(groupId) || StringUtils.isBlank(artifactId) || StringUtils.isBlank(version)) {
        // use default
        groupId = null;
        artifactId = null;
        version = null;
      }

      // maven settings
      String globalSettings = getMavenSettings(MavenPlugin.getMavenConfiguration().getGlobalSettingsFile());
      String settings = getMavenSettings(MavenPlugin.getMavenConfiguration().getUserSettingsFile());

      monitor.beginTask(getOperationName(), 100);
      ScoutProjectNewHelper.createProject(getTargetDirectory(), getSymbolicName(), getDisplayName(), getJavaVersion(), groupId, artifactId, version, globalSettings, settings);
      monitor.worked(10);

      m_createdProjects = importIntoWorkspace(SubMonitor.convert(monitor, 90));
    }
    catch (Exception e) {
      throw new CoreException(new ScoutStatus("Unable to create Scout Project.", e));
    }
    finally {
      monitor.done();
    }
  }

  protected static String getMavenSettings(String in) {
    if (StringUtils.isNotBlank(in)) {
      File p = new File(in);
      String absolutePath = p.getAbsolutePath();
      if (p.isFile()) {
        return absolutePath;
      }
      SdkLog.warning("Maven settings file '" + absolutePath + "' not found. Using empty settings.");
    }
    return null;
  }

  /**
   * Imports the extracted projects into the workspace using m2e import
   *
   * @throws CoreException
   */
  protected List<IProject> importIntoWorkspace(IProgressMonitor monitor) throws CoreException {
    File baseFolder = new File(getTargetDirectory(), getSymbolicName());
    File[] subFolders = baseFolder.listFiles();
    Collection<MavenProjectInfo> projects = new ArrayList<>(subFolders.length);
    for (File subFolder : subFolders) {
      File pom = new File(subFolder, "pom.xml");
      if (pom.isFile()) {
        projects.add(new MavenProjectInfo(subFolder.getName(), pom, null, null));
      }
    }

    List<IMavenProjectImportResult> importedProjects = MavenPlugin.getProjectConfigurationManager()
        .importProjects(projects, new ProjectImportConfiguration(), monitor);

    List<IProject> result = new ArrayList<>(importedProjects.size());
    for (IMavenProjectImportResult mavenProject : importedProjects) {
      if (mavenProject.getProject() != null) {
        result.add(mavenProject.getProject());
      }
    }
    return result;
  }

  public String getSymbolicName() {
    return m_symbolicName;
  }

  public void setSymbolicName(String symbolicName) {
    m_symbolicName = symbolicName;
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
}
