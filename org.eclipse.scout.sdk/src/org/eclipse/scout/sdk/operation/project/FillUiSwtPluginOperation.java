/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.project;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.template.InstallJavaFileOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * creates a swt application plugin with an application and a product
 * depends on a scout client project with a ClientSession object
 * for example
 * com.bsiag.rcp.ui.swt
 */
public class FillUiSwtPluginOperation extends AbstractScoutProjectNewOperation {

  private IProject m_project;

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiSwtPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    String swtPluginName = getProperties().getProperty(CreateUiSwtPluginOperation.PROP_BUNDLE_SWT_NAME, String.class);
    m_project = getCreatedBundle(swtPluginName).getProject();
  }

  @Override
  public String getOperationName() {
    return "Fill UI SWT Plugin";
  }

  @Override
  public void validate() {
    super.validate();
    if (m_project == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    Map<String, String> props = getStringProperties();
    String destPathPref = TypeUtility.DEFAULT_SOURCE_FOLDER_NAME + "/" + m_project.getName().replace('.', '/') + "/";

    new InstallJavaFileOperation("templates/ui.swt/src/Activator.java", destPathPref + "Activator.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/SwtEnvironment.java", destPathPref + "SwtEnvironment.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/SwtStartup.java", destPathPref + "SwtStartup.java", m_project, props).run(monitor, workingCopyManager);

    // perspective
    new InstallJavaFileOperation("templates/ui.swt/src/perspective/Perspective.java", destPathPref + "perspective/Perspective.java", m_project, props).run(monitor, workingCopyManager);

    // editor part
    new InstallJavaFileOperation("templates/ui.swt/src/editor/ScoutEditorPart.java", destPathPref + "editor/ScoutEditorPart.java", m_project, props).run(monitor, workingCopyManager);

    // views
    new InstallJavaFileOperation("templates/ui.swt/src/views/CenterView.java", destPathPref + "views/CenterView.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/views/EastView.java", destPathPref + "views/EastView.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/views/NorthEastView.java", destPathPref + "views/NorthEastView.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/views/NorthView.java", destPathPref + "views/NorthView.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/views/NorthWestView.java", destPathPref + "views/NorthWestView.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/views/SouthEastView.java", destPathPref + "views/SouthEastView.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/views/SouthView.java", destPathPref + "views/SouthView.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/views/SouthWestView.java", destPathPref + "views/SouthWestView.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/views/WestView.java", destPathPref + "views/WestView.java", m_project, props).run(monitor, workingCopyManager);

    // application
    new InstallJavaFileOperation("templates/ui.swt/src/application/Application.java", destPathPref + "application/Application.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/application/ApplicationWorkbenchAdvisor.java", destPathPref + "application/ApplicationWorkbenchAdvisor.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/application/ApplicationWorkbenchWindowAdvisor.java", destPathPref + "application/ApplicationWorkbenchWindowAdvisor.java", m_project, props).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/application/menu/DesktopMenuBar.java", destPathPref + "application/menu/DesktopMenuBar.java", m_project, props).run(monitor, workingCopyManager);

    m_project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
  }
}
