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
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.compatibility.TargetPlatformUtility;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class InstallTargetPlatformFileOperation extends InstallTextFileOperation {
  private static final String TARGET_FILE_NAME = "ScoutRAP.target";
  private static final String VARIABLE_RAP_LOCATION = "RAP_LOCATION";

  // Juno Updatesite
  private static final String JUNO_UPDATE_SITE_URL = "http://download.eclipse.org/releases/juno";

  // Scout RAP runtime
  public static final String SCOUT_RT_RAP_FEATURE_URL = JUNO_UPDATE_SITE_URL;
  public static final String SCOUT_RT_RAP_FEATURE = "org.eclipse.scout.rt.rap.feature.feature.group";

  // Eclipse RAP runtime
  public static final String ECLIPSE_RT_RAP_FEATURE_URL = JUNO_UPDATE_SITE_URL;
  public static final String ECLIPSE_RT_RAP_FEATURE = "org.eclipse.rap.runtime.feature.group";

  // Eclipse RAP Incubator
  public static final String ECLIPSE_RT_RAP_INCUB_FEATURE_URL = "http://download.eclipse.org/rt/rap/1.5/incubator";
  public static final String ECLIPSE_RT_RAP_INCUB_FEATURE = "org.eclipse.rap.incubator.supplemental.fileupload.feature.feature.group";

  private final ArrayList<ITargetEntryContributor> m_entryList;

  protected static interface ITargetEntryContributor {
    void contributeXml(StringBuilder sb, IProgressMonitor monitor) throws CoreException;
  }

  public InstallTargetPlatformFileOperation(IProject dstProject) {
    super("templates/ui.rap/ScoutRAP.target", TARGET_FILE_NAME, ScoutSdkRap.getDefault().getBundle(), dstProject, new HashMap<String, String>());
    m_entryList = new ArrayList<ITargetEntryContributor>();
  }

  public void addEclipseHomeEntry() {
    m_entryList.add(new ITargetEntryContributor() {
      @Override
      public void contributeXml(StringBuilder sb, IProgressMonitor monitor) {
        sb.append("    <location path=\"${eclipse_home}\" type=\"Profile\"/>\n");
      }
    });
  }

  public void addLocalDirectory(final String dir) {
    m_entryList.add(new ITargetEntryContributor() {
      @Override
      public void contributeXml(StringBuilder sb, IProgressMonitor monitor) {
        sb.append("    <location path=\"" + dir + "\" type=\"Directory\"/>\n");
      }
    });
  }

  public void addUpdateSite(final String locationUrl, final String featureId) throws CoreException {
    ITargetEntryContributor entry = new ITargetEntryContributor() {
      @Override
      public void contributeXml(StringBuilder sb, IProgressMonitor monitor) throws CoreException {
        try {
          String version = P2Utility.getLatestVersion(featureId, new URI(locationUrl), monitor);
          sb.append("    <location includeAllPlatforms=\"false\" includeMode=\"slicer\" type=\"InstallableUnit\">\n");
          sb.append("      <unit id=\"" + featureId + "\" version=\"" + version + "\"/>\n");
          sb.append("      <repository location=\"" + locationUrl + "\"/>\n");
          sb.append("    </location>\n");
        }
        catch (URISyntaxException e) {
          throw new CoreException(new ScoutStatus(e));
        }
      }
    };
    m_entryList.add(entry);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    StringBuilder locations = new StringBuilder(1024);
    for (ITargetEntryContributor entry : m_entryList) {
      entry.contributeXml(locations, monitor);
    }

    String location = locations.toString();
    if (StringUtility.hasText(location)) {
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
}
