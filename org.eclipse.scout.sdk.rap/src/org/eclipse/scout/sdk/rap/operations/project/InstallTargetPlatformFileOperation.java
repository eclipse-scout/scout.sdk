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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.compatibility.TargetPlatformUtility;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class InstallTargetPlatformFileOperation extends InstallTextFileOperation {
  private static final String TARGET_FILE_NAME = "ScoutRAP.target";
  private static final String VARIABLE_RAP_LOCATION = "RAP_LOCATION";

  private static final String SCOUT_RT_RAP_FEATURE_URL = "http://download.eclipse.org/scout/nightly/update"; //TODO: change to juno download page
  private static final String SCOUT_RT_RAP_FEATURE = "org.eclipse.scout.rt.rap.feature.feature.group";

  // requirement as defined by Scout RT RAP
  private static final String ECLIPSE_RT_RAP_FEATURE_URL = "http://download.eclipse.org/rt/rap/1.5/runtime";
  private static final String ECLIPSE_RT_RAP_FEATURE = "org.eclipse.rap.runtime.feature.group";
  //private static final String ECLIPSE_RT_RAP_REQ_FEATURE = "org.eclipse.rap.runtime.requirements.feature.group";

  private static final String ECLIPSE_RT_RAP_INCUB_FEATURE_URL = "http://download.eclipse.org/rt/rap/1.5/incubator";
  private static final String ECLIPSE_RT_RAP_INCUB_FEATURE = "org.eclipse.rap.incubator.feature.feature.group";

  private String m_rapTargetLocalFolder;
  private Map<String, String> m_properties;

  public InstallTargetPlatformFileOperation(IProject dstProject) {
    super("templates/ui.rap/ScoutRAP.target", TARGET_FILE_NAME, ScoutSdkRap.getDefault().getBundle(), dstProject, new HashMap<String, String>());
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String location = null;
    if (getRapTargetLocalFolder() != null) {
      location = "<location path=\"" + getRapTargetLocalFolder() + "\" type=\"Directory\"/>";
    }
    else {
      try {
        String latestScoutRapVersion = P2Utility.getLatestVersion(SCOUT_RT_RAP_FEATURE, new URI(SCOUT_RT_RAP_FEATURE_URL), monitor);
        String latestEclipseRapVersion = P2Utility.getLatestVersion(ECLIPSE_RT_RAP_FEATURE, new URI(ECLIPSE_RT_RAP_FEATURE_URL), monitor);
        String latestEclipseRapIncubVersion = P2Utility.getLatestVersion(ECLIPSE_RT_RAP_INCUB_FEATURE, new URI(ECLIPSE_RT_RAP_INCUB_FEATURE_URL), monitor);
        if (latestScoutRapVersion != null && latestEclipseRapVersion != null && latestEclipseRapIncubVersion != null) {
          StringBuilder remoteLocationBuilder = new StringBuilder();
          remoteLocationBuilder.append("<location includeAllPlatforms=\"false\" includeMode=\"slicer\" type=\"InstallableUnit\">\n");
          remoteLocationBuilder.append("  <unit id=\"" + SCOUT_RT_RAP_FEATURE + "\" version=\"" + latestScoutRapVersion + "\"/>\n");
          remoteLocationBuilder.append("  <repository location=\"" + SCOUT_RT_RAP_FEATURE_URL + "\"/>\n");
          remoteLocationBuilder.append("</location>\n");
          remoteLocationBuilder.append("<location includeAllPlatforms=\"false\" includeMode=\"slicer\" type=\"InstallableUnit\">\n");
          remoteLocationBuilder.append("  <unit id=\"" + ECLIPSE_RT_RAP_FEATURE + "\" version=\"" + latestEclipseRapVersion + "\"/>\n");
          remoteLocationBuilder.append("  <repository location=\"" + ECLIPSE_RT_RAP_FEATURE_URL + "\"/>\n");
          remoteLocationBuilder.append("</location>\n");
          remoteLocationBuilder.append("<location includeAllPlatforms=\"false\" includeMode=\"slicer\" type=\"InstallableUnit\">\n");
          remoteLocationBuilder.append("  <unit id=\"" + ECLIPSE_RT_RAP_INCUB_FEATURE + "\" version=\"" + latestEclipseRapIncubVersion + "\"/>\n");
          remoteLocationBuilder.append("  <repository location=\"" + ECLIPSE_RT_RAP_INCUB_FEATURE_URL + "\"/>\n");
          remoteLocationBuilder.append("</location>\n");
          location = remoteLocationBuilder.toString();
        }
      }
      catch (URISyntaxException e) {
        ScoutSdkRap.logError("could not install rap target file.", e);
      }
      catch (IllegalArgumentException e) {
        ScoutSdkRap.logError("could not parse scout rap remote version.", e);
      }
    }

    if (location != null) {
      getProperties().put(VARIABLE_RAP_LOCATION, location); // used as replacement in the parent call
      super.run(monitor, workingCopyManager);

      try {
        TargetPlatformUtility.resolveTargetPlatform(getCreatedFile(), monitor);
      }
      catch (CoreException e) {
        ScoutSdkRap.logError("could not set target to file '" + getCreatedFile().getProjectRelativePath().toString() + "'.", e);
      }
    }
  }

  public String getRapTargetLocalFolder() {
    return m_rapTargetLocalFolder;
  }

  public void setRapTargetLocalFolder(String rapTargetLocalFolder) {
    m_rapTargetLocalFolder = rapTargetLocalFolder;
  }
}
