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
package org.eclipse.scout.sdk.rap.operations.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.operation.project.AbstractCreateScoutBundleOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.template.InstallJavaFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.osgi.framework.Bundle;

public class CreateMobileClientPluginOperation extends AbstractCreateScoutBundleOperation {

  public final static String PROP_MOBILE_BUNDLE_CLIENT_NAME = "BUNDLE_MOBILE_CLIENT_NAME";
  public final static String MOBILE_CLIENT_PROJECT_NAME_SUFFIX = ".client.mobile";

  @Override
  public String getOperationName() {
    return "Create Mobile Client Plugin";
  }

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    setSymbolicName(getPluginName(MOBILE_CLIENT_PROJECT_NAME_SUFFIX));
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    getProperties().setProperty(PROP_MOBILE_BUNDLE_CLIENT_NAME, getSymbolicName());
    Bundle uiRapBundle = ScoutSdkRap.getDefault().getBundle();

    new InstallTextFileOperation("templates/client.mobile/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", uiRapBundle, project, getStringProperties()).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/client.mobile/plugin.xml", "plugin.xml", uiRapBundle, project, getStringProperties()).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/client.mobile/build.properties", "build.properties", uiRapBundle, project, getStringProperties()).run(monitor, workingCopyManager);

    String destPathPref = SdkProperties.DEFAULT_SOURCE_FOLDER_NAME + "/" + getCreatedProject().getName().replace('.', '/') + "/";
    new InstallJavaFileOperation("templates/client.mobile/src/Activator.java", destPathPref + "Activator.java", uiRapBundle, getCreatedProject(), getStringProperties()).run(monitor, workingCopyManager);

    // Add logoff texts
    String sharedBundleName = getProperties().getProperty(CreateSharedPluginOperation.PROP_BUNDLE_SHARED_NAME, String.class);
    IScoutBundle shared = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(sharedBundleName);

    INlsProject nlsProject = shared.getNlsProject();
    NlsEntry entry = new NlsEntry("Logoff", nlsProject);
    entry.addTranslation(Language.LANGUAGE_DEFAULT, "Log off");
    nlsProject.updateRow(entry, monitor);
  }
}
