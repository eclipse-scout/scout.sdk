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
package org.eclipse.scout.sdk.operation.util;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.compatibility.TargetPlatformUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class InstallTargetPlatformFileOperation extends InstallTextFileOperation {
  public static final String ECLIPSE_HOME_VAR = "${eclipse_home}";

  private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{[^\\{\\}\\$]*\\}");

  private final List<ITargetEntryContributor> m_entryList;

  protected interface ITargetEntryContributor {
    void contribute(IFile targetFile, IProgressMonitor monitor) throws CoreException;
  }

  public InstallTargetPlatformFileOperation(IProject dstProject, String targetFileName, Map<String, String> properties) {
    super("templates/all/template.target", targetFileName, ScoutSdk.getDefault().getBundle(), dstProject, properties);
    m_entryList = new LinkedList<ITargetEntryContributor>();
  }

  public void addEclipseHomeEntry() {
    addLocalDirectory(ECLIPSE_HOME_VAR);
  }

  public void addRunningEclipseEntries() {
    Set<String> folders = getRunningEclipseEntries();
    for (String s : folders) {
      addLocalDirectory(s);
    }
  }

  public static Set<String> getRunningEclipseEntries() {
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
    if (folders.size() > 0) {
      return folders;
    }
    else {
      // ensure that at least the eclipse home is set, even if there are no bundle locations found.
      folders.add(ECLIPSE_HOME_VAR);
    }
    return folders;
  }

  private static File getBundleContainer(BundleDescription bundle) {
    File location = getBundleLocation(bundle);
    if (location != null) {
      File plugins = location.getParentFile();
      if (plugins != null && "plugins".equalsIgnoreCase(plugins.getName())) {
        return plugins.getParentFile();
      }
    }
    return null;
  }

  private static File getBundleLocation(BundleDescription bundle) {
    if (bundle != null) {
      String location = bundle.getLocation();
      if (StringUtility.hasText(location)) {
        File eclipseInstallLocation = ResourceUtility.getEclipseInstallLocation();
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
            else if (eclipseInstallLocation != null) {
              // path relative to the eclipse home
              File f = new File(eclipseInstallLocation, location.substring(fileLocationScheme.length()));
              if (f.exists()) {
                return f;
              }
            }
          }
        }
        catch (Exception e) {
          ScoutSdk.logError(e);
        }
      }
    }
    return null;
  }

  public void addLocalDirectory(String dir) {
    File eclipseInstallLocation = ResourceUtility.getEclipseInstallLocation();
    if (eclipseInstallLocation != null && !VAR_PATTERN.matcher(dir).matches()) {
      String absDir = new File(dir).getAbsolutePath();
      String eclipseDir = eclipseInstallLocation.getAbsolutePath();
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
      public void contribute(IFile targetFile, IProgressMonitor monitor) throws CoreException {
        TargetPlatformUtility.addDirectoryToTarget(targetFile, new String[]{directory});
      }
    });
  }

  public void addUpdateSite(final String locationUrl, final String featureId, final String version) throws CoreException {
    m_entryList.add(new ITargetEntryContributor() {
      @Override
      public void contribute(IFile targetFile, IProgressMonitor monitor) throws CoreException {
        TargetPlatformUtility.addInstallableUnitToTarget(targetFile, featureId, version, locationUrl, monitor);
      }
    });
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IFile targetFile = getCreatedFile();

    for (ITargetEntryContributor entry : m_entryList) {
      entry.contribute(targetFile, monitor);
    }
  }
}
