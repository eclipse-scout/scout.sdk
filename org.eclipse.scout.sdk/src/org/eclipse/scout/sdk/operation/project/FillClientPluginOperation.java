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
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class FillClientPluginOperation extends AbstractScoutProjectNewOperation {

  public final static String PROP_INSTALL_CLIENT_SESSION = "INSTALL_CLIENT_SESSION";
  public final static String PROP_INSTALL_DESKTOP_EXT = "INSTALL_DESKTOP_EXT";

  private IProject m_clientProject;

  @Override
  public String getOperationName() {
    return "Fill Scout Client Plugin";
  }

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateClientPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    String clientPluginName = getProperties().getProperty(CreateClientPluginOperation.PROP_BUNDLE_CLIENT_NAME, String.class);
    m_clientProject = getCreatedBundle(clientPluginName).getProject();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (m_clientProject == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
  }

  private boolean isInstallDesktopExtension() {
    Boolean b = getProperties().getProperty(PROP_INSTALL_DESKTOP_EXT, Boolean.class);
    return b != null && b.booleanValue();
  }

  private boolean isInstallClientSession() {
    Boolean b = getProperties().getProperty(PROP_INSTALL_CLIENT_SESSION, Boolean.class);
    return b == null || b.booleanValue();
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String destPathPref = SdkProperties.DEFAULT_SOURCE_FOLDER_NAME + "/" + m_clientProject.getName().replace('.', '/') + "/";
    Map<String, String> props = getStringProperties();
    new InstallJavaFileOperation("templates/client/src/Activator.java", destPathPref + "Activator.java", m_clientProject, props).run(monitor, workingCopyManager);
    if (isInstallClientSession()) {
      new InstallJavaFileOperation("templates/client/src/ClientSession.java", destPathPref + "ClientSession.java", m_clientProject, props).run(monitor, workingCopyManager);
    }
    if (isInstallDesktopExtension()) {
      new InstallJavaFileOperation("templates/client/src/ui/desktop/DesktopExtension.java", destPathPref + "ui/desktop/DesktopExtension.java", m_clientProject, props).run(monitor, workingCopyManager);
    }
    else {
      new InstallJavaFileOperation("templates/client/src/ui/desktop/Desktop.java", destPathPref + "ui/desktop/Desktop.java", m_clientProject, props).run(monitor, workingCopyManager);
    }
    m_clientProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
  }
}
