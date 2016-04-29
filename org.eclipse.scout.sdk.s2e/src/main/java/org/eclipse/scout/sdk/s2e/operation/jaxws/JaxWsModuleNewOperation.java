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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsModuleNewHelper;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.operation.project.ScoutProjectNewOperation;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutStatus;
import org.xml.sax.SAXException;

/**
 * <h3>{@link JaxWsModuleNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class JaxWsModuleNewOperation implements IOperation {

  // in
  private IJavaProject m_serverModule;
  private String m_artifactId;

  // out
  private IProject m_createdProject;

  @Override
  public String getOperationName() {
    return "Create new Jax-Ws Module";
  }

  @Override
  public void validate() {
    Validate.notNull(getServerModule(), "Target module pom file must be specified.");
    Validate.isTrue(getServerModule().exists(), "Target module pom file could not be found.");
    Validate.isTrue(StringUtils.isNotBlank(getArtifactId()));
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 100);

    try {
      // get maven settings from workspace
      String globalSettings = ScoutProjectNewOperation.getMavenSettings(MavenPlugin.getMavenConfiguration().getGlobalSettingsFile());
      String settings = ScoutProjectNewOperation.getMavenSettings(MavenPlugin.getMavenConfiguration().getUserSettingsFile());

      // get pom from target project
      IFile pomFile = getServerModule().getProject().getFile(IMavenConstants.POM);
      if (!pomFile.isAccessible()) {
        throw new CoreException(new ScoutStatus(IMavenConstants.POM + " could not be found in module '" + getServerModule().getElementName() + "'."));
      }
      if (progress.isCanceled()) {
        return;
      }
      progress.worked(5);

      // create project on disk (using archetype)
      File createdProjectDir = JaxWsModuleNewHelper.createModule(pomFile.getLocation().toFile(), getArtifactId(), globalSettings, settings);
      progress.worked(10);

      // import into workspace
      setCreatedProject(importIntoWorkspace(createdProjectDir, progress.newChild(70)));

      // refresh modified resources
      Set<IProject> modifiedProjects = getModifiedResources(createdProjectDir);
      modifiedProjects.add(getCreatedProject());
      modifiedProjects.add(getServerModule().getProject());

      // run 'maven update' on created project because we modified the parent and the dependencies
      S2eUtils.mavenUpdate(modifiedProjects, false, true, false, true, progress.newChild(15));
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus("Unable to create Jax-Ws Module.", e));
    }
  }

  protected IProject importIntoWorkspace(File createdProjectDir, IProgressMonitor monitor) throws CoreException {
    Set<MavenProjectInfo> projects = Collections.singleton(new MavenProjectInfo(createdProjectDir.getName(), new File(createdProjectDir, IMavenConstants.POM), null, null));
    List<IMavenProjectImportResult> importedProjects = MavenPlugin.getProjectConfigurationManager().importProjects(projects, new ProjectImportConfiguration(), monitor);
    if (importedProjects == null || importedProjects.isEmpty()) {
      throw new CoreException(new ScoutStatus("Unable to import newly created project into workspace."));
    }
    return importedProjects.iterator().next().getProject();
  }

  protected Set<IProject> getModifiedResources(File createdProjectDir) throws CoreException {
    try {
      File parentPom = JaxWsModuleNewHelper.getParentPomOf(new File(createdProjectDir, IMavenConstants.POM));
      if (parentPom == null) {
        return Collections.emptySet();
      }

      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IFile[] resources = root.findFilesForLocationURI(parentPom.toURI());
      Set<IProject> result = new HashSet<>(resources.length);
      for (IFile f : resources) {
        result.add(f.getProject());
      }
      return result;
    }
    catch (IOException | ParserConfigurationException | SAXException e) {
      throw new CoreException(new ScoutStatus(e));
    }
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
}
