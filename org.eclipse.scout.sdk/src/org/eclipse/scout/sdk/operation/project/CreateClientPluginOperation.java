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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.template.InstallBinaryFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class CreateClientPluginOperation extends AbstractCreateScoutBundleOperation {

  public static final String PROP_BUNDLE_CLIENT_NAME = "BUNDLE_CLIENT_NAME";
  public static final String PROP_INSTALL_ICONS = "INSTALL_ICONS";

  public static final String CLIENT_PROJECT_NAME_SUFFIX = ".client";
  public static final String BUNDLE_ID = "org.eclipse.scout.sdk.ui.ClientBundle";

  @Override
  public String getOperationName() {
    return "Create Client Plugin";
  }

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateClientPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    setSymbolicName(getPluginName(CLIENT_PROJECT_NAME_SUFFIX));
  }

  private boolean isInstallIcons() {
    Boolean b = getProperties().getProperty(PROP_INSTALL_ICONS, Boolean.class);
    return b == null || b.booleanValue();
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    getProperties().setProperty(PROP_BUNDLE_CLIENT_NAME, getSymbolicName());
    Map<String, String> props = getStringProperties();
    try {
      new InstallTextFileOperation("templates/client/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, props).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/client/plugin.xml", "plugin.xml", project, props).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/client/build.properties", "build.properties", project, props).run(monitor, workingCopyManager);
      if (isInstallIcons()) {
        new InstallBinaryFileOperation("templates/client/resources/icons/eye.png", project, "resources/icons/eye.png").run(monitor, workingCopyManager);
        new InstallBinaryFileOperation("templates/client/resources/icons/eclipse_scout.gif", project, "resources/icons/eclipse_scout.gif").run(monitor, workingCopyManager);
      }
    }
    catch (MalformedURLException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not install files in '" + project.getName() + "'.", e));
    }
    catch (URISyntaxException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not install files in '" + project.getName() + "'.", e));
    }
  }
}
