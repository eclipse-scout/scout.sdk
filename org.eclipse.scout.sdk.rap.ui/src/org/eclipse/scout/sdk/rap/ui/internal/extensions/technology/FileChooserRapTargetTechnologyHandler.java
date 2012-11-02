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
package org.eclipse.scout.sdk.rap.ui.internal.extensions.technology;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.sdk.compatibility.License;
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.compatibility.TargetPlatformUtility;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;
import org.eclipse.scout.sdk.rap.operations.project.InstallTargetPlatformFileOperation;
import org.eclipse.scout.sdk.rap.ui.internal.extensions.UiRapBundleNodeFactory;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.extensions.technology.ScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.dialog.LicenseDialog;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.osgi.framework.Version;

/**
 * <h3>{@link FileChooserRapTargetTechnologyHandler}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 10.04.2012
 */
public class FileChooserRapTargetTechnologyHandler extends AbstractScoutTechnologyHandler {

  private final static String RAP_INCUBATOR_UPDATE_SITE_URL = "http://download.eclipse.org/rt/rap/1.5/incubator";
  private final static String RAP_INCUBATOR_FEATURE_NAME = "org.eclipse.rap.incubator.supplemental.fileupload.feature.feature.group";
  private final static Version RAP_INCUBATOR_FEATURE_VERSION = new Version(1, 5, 0, "20120220-1720"); // the supported version. must be present at the update site
  private final static String SCOUT_INCUBATOR_FEATURE_NAME = "org.eclipse.scout.rt.ui.rap.incubator.filechooser.feature.feature.group";
  private final static String SCOUT_INCUBATOR_UPDATE_SITE_URL = "http://download.eclipse.org/scout/releases/3.8";

  @Override
  public boolean preSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    if (!selected) {
      return true;
    }
    try {
      final BooleanHolder licAccepted = new BooleanHolder(false);
      final Map<String, License[]> lic = P2Utility.getLicense(RAP_INCUBATOR_FEATURE_NAME, new URI(RAP_INCUBATOR_UPDATE_SITE_URL), monitor);
      Map<String, License[]> licScout = P2Utility.getLicense(SCOUT_INCUBATOR_FEATURE_NAME, new URI(SCOUT_INCUBATOR_UPDATE_SITE_URL), monitor);
      lic.putAll(licScout);

      ScoutSdkUi.getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          LicenseDialog licDialog = new LicenseDialog(ScoutSdkUi.getShell(), lic);
          if (licDialog.open() == Dialog.OK) {
            licAccepted.setValue(true);
          }
        }
      });
      return licAccepted.getValue();
    }
    catch (URISyntaxException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String scoutIncubVersion = "0.0.0";
    if (selected) {
      try {
        scoutIncubVersion = P2Utility.getLatestVersion(SCOUT_INCUBATOR_FEATURE_NAME, new URI(SCOUT_INCUBATOR_UPDATE_SITE_URL), monitor);
      }
      catch (URISyntaxException e) {
        ScoutSdkRap.logError(e);
      }
    }

    for (IScoutTechnologyResource r : resources) {
      if (selected) {
        TargetPlatformUtility.addInstallableUnitsToTarget(r.getResource(),
              new String[]{RAP_INCUBATOR_FEATURE_NAME, SCOUT_INCUBATOR_FEATURE_NAME},
              new String[]{RAP_INCUBATOR_FEATURE_VERSION.toString(), scoutIncubVersion},
              new String[]{RAP_INCUBATOR_UPDATE_SITE_URL, SCOUT_INCUBATOR_UPDATE_SITE_URL});
      }
      else {
        TargetPlatformUtility.removeInstallableUnitsFromTarget(r.getResource(), new String[]{RAP_INCUBATOR_FEATURE_NAME, SCOUT_INCUBATOR_FEATURE_NAME});
      }
    }
    if (resources.length == 1) {
      TargetPlatformUtility.resolveTargetPlatform(resources[0].getResource(), true, monitor);
    }
  }

  @Override
  public TriState getSelection(IScoutProject project) {
    ScoutTechnologyResource[] targetFiles = getTargetFiles(project);
    if (targetFiles.length < 1) {
      return TriState.FALSE;
    }

    TriState ret = TriState.parseTriState(containsIncubator(targetFiles[0].getResource()));
    for (int i = 1; i < targetFiles.length; i++) {
      TriState tmp = TriState.parseTriState(containsIncubator(targetFiles[i].getResource()));
      if (ret != tmp) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  private boolean containsIncubator(IFile targetFile) {
    try {
      String content = ResourceUtility.getContent(targetFile);
      return content.contains(SCOUT_INCUBATOR_FEATURE_NAME) && content.contains(RAP_INCUBATOR_FEATURE_NAME);
    }
    catch (CoreException e) {
      ScoutSdkRap.logError(e);
      return false;
    }
  }

  @Override
  public boolean isActive(IScoutProject project) {
    IScoutBundle[] rapBundles = project.getAllBundles(UiRapBundleNodeFactory.BUNDLE_UI_RAP);
    return project.getClientBundle() != null && rapBundles != null && rapBundles.length > 0 && getTargetFiles(project).length > 0;
  }

  private static ScoutTechnologyResource[] getTargetFiles(IScoutProject project) {
    final ArrayList<ScoutTechnologyResource> ret = new ArrayList<ScoutTechnologyResource>();
    try {
      for (IScoutBundle b : project.getAllScoutBundles()) {
        final IScoutBundle bundle = b;
        bundle.getProject().accept(new IResourceVisitor() {
          @Override
          public boolean visit(IResource resource) throws CoreException {
            if (resource != null && resource.getType() == IResource.FILE && resource.exists()) {
              IFile f = (IFile) resource;
              String fileName = f.getName();
              if (fileName.toLowerCase().endsWith(".target")) {
                boolean checked = fileName.equals(InstallTargetPlatformFileOperation.TARGET_FILE_NAME);
                ret.add(new ScoutTechnologyResource(bundle, f, checked));
              }
            }
            return true;
          }
        }, IResource.DEPTH_INFINITE, false);
      }
    }
    catch (CoreException e) {
      ScoutSdkRap.logError(e);
    }
    return ret.toArray(new ScoutTechnologyResource[ret.size()]);
  }

  @Override
  protected void contributeResources(IScoutProject project, List<IScoutTechnologyResource> list) {
    for (ScoutTechnologyResource r : getTargetFiles(project)) {
      list.add(r);
    }
  }
}
