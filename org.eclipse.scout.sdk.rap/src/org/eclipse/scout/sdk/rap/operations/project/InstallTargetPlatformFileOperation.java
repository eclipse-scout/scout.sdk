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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.compatibility.TargetPlatformUtility;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class InstallTargetPlatformFileOperation extends InstallTextFileOperation {
  public static final String TARGET_FILE_NAME = "ScoutRAP.target";
  private static final String VARIABLE_RAP_LOCATION = "RAP_LOCATION";
  public static final String ECLIPSE_HOME_VAR = "${eclipse_home}";

  private final ArrayList<ITargetEntryContributor> m_entryList;
  private final File m_installLocation;

  protected static interface ITargetEntryContributor {
    void contributeXml(StringBuilder sb, IProgressMonitor monitor) throws CoreException;
  }

  public InstallTargetPlatformFileOperation(IProject dstProject) {
    super("templates/ui.rap/ScoutRAP.target", TARGET_FILE_NAME, ScoutSdkRap.getDefault().getBundle(), dstProject, new HashMap<String, String>());
    m_entryList = new ArrayList<ITargetEntryContributor>();
    m_installLocation = ResourceUtility.getEclipseInstallLocation();
  }

  public void addEclipseHomeEntry() {
    addLocalDirectory(ECLIPSE_HOME_VAR);
  }

  public void addRunningEclipseEntries() {
    HashSet<String> folders = new HashSet<String>();
    for (BundleDescription bundle : Platform.getPlatformAdmin().getState().getBundles()) {
      File bundleContainer = getBundleContainer(bundle);
      if (bundleContainer != null) {
        folders.add(bundleContainer.getAbsolutePath());
      }
      for (BundleDescription fragment : bundle.getFragments()) {
        File fragContainer = getBundleContainer(fragment);
        if (fragContainer != null) {
          folders.add(fragContainer.getAbsolutePath());
        }
      }
    }
    for (String dir : folders) {
      addLocalDirectory(dir);
    }
  }

  private File getBundleContainer(BundleDescription bundle) {
    File location = getBundleLocation(bundle);
    if (location != null) {
      File plugins = location.getParentFile();
      if (plugins != null && "plugins".equalsIgnoreCase(plugins.getName())) {
        return plugins.getParentFile();
      }
    }
    return null;
  }

  private File getBundleLocation(BundleDescription bundle) {
    if (bundle != null) {
      String location = bundle.getLocation();
      if (StringUtility.hasText(location)) {
        try {
          final String URI_INITIAL_SCHEME = "initial@";
          if (location.startsWith(URI_INITIAL_SCHEME)) {
            location = location.substring(URI_INITIAL_SCHEME.length());
          }
          final String URI_REFERENCE_SCHEME = "reference:";
          if (location.startsWith(URI_REFERENCE_SCHEME)) {
            location = location.substring(URI_REFERENCE_SCHEME.length());
          }
          String fileLocationScheme = "file:";
          if (location.startsWith(fileLocationScheme)) {
            URI uri = URIUtil.fromString(location);
            if (uri.isAbsolute()) {
              // absolute path
              if (!uri.isOpaque()) {
                File f = new File(uri);
                if (f.exists()) {
                  return f;
                }
              }
            }
            else if (m_installLocation != null) {
              // path relative to the eclipse home
              File f = new File(m_installLocation, location.substring(fileLocationScheme.length()));
              if (f.exists()) {
                return f;
              }
            }
          }
        }
        catch (Exception e) {
          ScoutSdkRap.logError(e);
        }
      }
    }
    return null;
  }

  public void addLocalDirectory(String dir) {
    if (m_installLocation != null) {
      String absDir = new File(dir).getAbsolutePath();
      String eclipseDir = m_installLocation.getAbsolutePath();
      if (absDir.startsWith(eclipseDir)) {
        String relPath = absDir.substring(eclipseDir.length());
        if (!relPath.startsWith(File.separator)) {
          relPath = File.separator + relPath;
        }
        dir = ECLIPSE_HOME_VAR + relPath;
      }
    }

    final String directory = dir;
    m_entryList.add(new ITargetEntryContributor() {
      @Override
      public void contributeXml(StringBuilder sb, IProgressMonitor monitor) {
        sb.append("    <location path=\"" + directory + "\" type=\"Directory\"/>\n");
      }
    });
  }

  public void addUpdateSite(final String locationUrl, final String featureId) throws CoreException {
    ITargetEntryContributor entry = new ITargetEntryContributor() {
      @Override
      public void contributeXml(StringBuilder sb, IProgressMonitor monitor) throws CoreException {
        try {
          String version = P2Utility.getLatestVersion(featureId, new URI(locationUrl), monitor);
          sb.append("<location includeAllPlatforms=\"false\" includeConfigurePhase=\"true\" includeMode=\"slicer\" includeSource=\"true\" type=\"InstallableUnit\">\n");
          sb.append("<unit id=\"" + featureId + "\" version=\"" + version + "\"/>\n");
          sb.append("<repository location=\"" + locationUrl + "\"/>\n");
          sb.append("</location>\n");
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
