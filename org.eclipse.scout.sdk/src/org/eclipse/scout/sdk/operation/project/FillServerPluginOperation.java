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

public class FillServerPluginOperation extends AbstractScoutProjectNewOperation {
  private IProject m_project;

  public static final String PROP_INSTALL_SERVER_APP_CLASS = "INSTALL_SERVER_APP_CLASS";
  public static final String PROP_INSTALL_SERVER_SESSION_CLASS = "INSTALL_SERVER_SESSION_CLASS";
  public static final String PROP_INSTALL_ACCESS_CONTROL_SVC_CLASS = "INSTALL_ACCESS_CONTROL_SVC_CLASS";

  @Override
  public String getOperationName() {
    return "Fill Scout Server Plugin";
  }

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateServerPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    String serverPluginName = getProperties().getProperty(CreateServerPluginOperation.PROP_BUNDLE_SERVER_NAME, String.class);
    m_project = getCreatedBundle(serverPluginName).getProject();
  }

  @Override
  public void validate() {
    super.validate();
    if (m_project == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
  }

  private boolean isInstallApp() {
    Boolean b = getProperties().getProperty(PROP_INSTALL_SERVER_APP_CLASS, Boolean.class);
    return b == null || b.booleanValue();
  }

  private boolean isInstallSession() {
    Boolean b = getProperties().getProperty(PROP_INSTALL_SERVER_SESSION_CLASS, Boolean.class);
    return b == null || b.booleanValue();
  }

  private boolean isInstallAccessControlSvc() {
    Boolean b = getProperties().getProperty(PROP_INSTALL_ACCESS_CONTROL_SVC_CLASS, Boolean.class);
    return b == null || b.booleanValue();
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String destPathPref = TypeUtility.DEFAULT_SOURCE_FOLDER_NAME + "/" + m_project.getName().replace('.', '/') + "/";
    Map<String, String> props = getStringProperties();
    new InstallJavaFileOperation("templates/server/src/Activator.java", destPathPref + "Activator.java", m_project, props).run(monitor, workingCopyManager);
    if (isInstallApp()) {
      new InstallJavaFileOperation("templates/server/src/ServerApplication.java", destPathPref + "ServerApplication.java", m_project, props).run(monitor, workingCopyManager);
    }
    if (isInstallSession()) {
      new InstallJavaFileOperation("templates/server/src/ServerSession.java", destPathPref + "ServerSession.java", m_project, props).run(monitor, workingCopyManager);
    }
    if (isInstallAccessControlSvc()) {
      new InstallJavaFileOperation("templates/server/src/AccessControlService.java",
          destPathPref + "services/common/security/AccessControlService.java", m_project, props).run(monitor, workingCopyManager);
    }
    m_project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
  }
}
