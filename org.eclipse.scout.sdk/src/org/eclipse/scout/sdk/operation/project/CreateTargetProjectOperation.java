/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.PlatformVersionUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.project.add.ScoutProjectAddOperation;
import org.eclipse.scout.sdk.operation.util.InstallTargetPlatformFileOperation;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Version;

/**
 * <h3>{@link CreateTargetProjectOperation}</h3>
 * 
 * @author Matthias Villiger
 * @since 4.0.0 04.04.2014
 */
public class CreateTargetProjectOperation extends AbstractCreateEclipseProjectOperation {

  public static final String TARGET_PROJECT_NAME_SUFFIX = ".target";

  public static final String PROP_TARGET_FILE = "targatFile";

  public static final String SCOUT_RT_FEATURE = "org.eclipse.scout.rt.source.feature.group";
  public static final String ECLIPSE_PLATFORM_FEATURE = "org.eclipse.platform.feature.group";
  public static final String ECLIPSE_RPC_FEATURE = "org.eclipse.rcp.feature.group";
  public static final String ECLIPSE_EQUINOX_SDK_FEATURE = "org.eclipse.equinox.sdk.feature.group";
  public static final String ECLIPSE_RPC_E4_FEATURE = "org.eclipse.e4.rcp.source.feature.group";
  public static final String ECLIPSE_EMF_ECORE_FEATURE = "org.eclipse.emf.ecore.feature.group";
  public static final String ECLIPSE_EMF_COMMON_FEATURE = "org.eclipse.emf.common.feature.group";

  @Override
  public String getOperationName() {
    return "Create Target Project";
  }

  @Override
  public boolean isRelevant() {
    return getProperties().getProperty(ScoutProjectAddOperation.PROP_EXISTING_BUNDLE) == null && !isKeepCurrentTarget();
  }

  @Override
  public void init() {
    setSymbolicName(getPluginName(TARGET_PROJECT_NAME_SUFFIX));
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();

    String fileName = getProjectAlias() + ".target";
    InstallTargetPlatformFileOperation op = new InstallTargetPlatformFileOperation(project, fileName, getStringProperties());

    Version targetVersion = getTargetPlatformVersion();
    Version platformVersion = PlatformVersionUtility.getPlatformVersion();
    boolean useCurrent = platformVersion.getMajor() == targetVersion.getMajor() && platformVersion.getMinor() == targetVersion.getMinor();
    if (useCurrent) {
      // use local eclipse
      op.addRunningEclipseEntries();

      // add scout if the running eclipse has no scout installed
      if (!JdtUtility.areAllPluginsInstalled(IRuntimeClasses.ScoutClientBundleId, IRuntimeClasses.ScoutServerBundleId,
          IRuntimeClasses.ScoutSharedBundleId, IRuntimeClasses.ScoutUiSwingBundleId, IRuntimeClasses.ScoutUiSwtBundleId)) {
        op.addUpdateSite(UPDATE_SITE_URL_LUNA, SCOUT_RT_FEATURE, null);
      }
    }
    else {
      // use remote target
      String url = getUpdateSiteUrl();
      op.addUpdateSite(UPDATE_SITE_URL_LUNA, SCOUT_RT_FEATURE, null);
      op.addUpdateSite(url, ECLIPSE_PLATFORM_FEATURE, null);
      op.addUpdateSite(url, ECLIPSE_RPC_FEATURE, null);
      op.addUpdateSite(url, ECLIPSE_EQUINOX_SDK_FEATURE, null);
      if (PlatformVersionUtility.isE4(targetVersion)) {
        op.addUpdateSite(url, ECLIPSE_RPC_E4_FEATURE, null);
        op.addUpdateSite(url, ECLIPSE_EMF_ECORE_FEATURE, null);
        op.addUpdateSite(url, ECLIPSE_EMF_COMMON_FEATURE, null);
      }
    }

    op.validate();
    op.run(monitor, workingCopyManager);

    getProperties().setProperty(PROP_TARGET_FILE, op.getCreatedFile());
    getProperties().setProperty(NewProjectLoadTargetOperation.PROP_TARGET_PLATFORM_RELOAD_NECESSARY, Boolean.TRUE);
  }
}
