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
package org.eclipse.scout.sdk.operation.template;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class InstallBinaryFileOperation implements IOperation {

  private final URL m_sourceUrl;
  private final IProject m_destinationProject;
  private final String m_destinationPath;

  public InstallBinaryFileOperation(String srcPath, IProject destinationProject, String destinationPath) throws MalformedURLException, URISyntaxException {
    this(URIUtil.toURL(URIUtil.fromString("platform:/plugin/" + ScoutSdk.PLUGIN_ID + "/" + srcPath.replaceAll("^[\\\\\\/]*", ""))), destinationProject, destinationPath);
  }

  public InstallBinaryFileOperation(String sourceBundleId, String srcPath, IProject destinationProject, String destinationPath) throws MalformedURLException, URISyntaxException {
    this(URIUtil.toURL(URIUtil.fromString("platform:/plugin/" + sourceBundleId + "/" + srcPath.replaceAll("^[\\\\\\/]*", ""))), destinationProject, destinationPath);
  }

  public InstallBinaryFileOperation(URL sourceUrl, IProject destinationProject, String destinationPath) {
    m_sourceUrl = sourceUrl;
    m_destinationProject = destinationProject;
    m_destinationPath = destinationPath;
  }

  @Override
  public String getOperationName() {
    return "Install binary file " + getSourceUrl().toString() + " to /" + getDestinationProject().getName() + "/" + getDestinationPath();
  }

  @Override
  public void validate() {
    if (getSourceUrl() == null) {
      throw new IllegalArgumentException("source URL can not be null!");
    }
    if (getDestinationProject() == null) {
      throw new IllegalArgumentException("destination Project can not be null!");
    }
    if (StringUtility.isNullOrEmpty(getDestinationPath())) {
      throw new IllegalArgumentException("destination Path can not be null or empty!");
    }
  }

  @SuppressWarnings("resource")
  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      URL absSourceUrl = FileLocator.resolve(getSourceUrl());
      byte[] data = IOUtility.getContent(absSourceUrl.openStream(), true);
      IPath destPath = getDestinationProject().getLocation().append(getDestinationPath());
      File f = new File(destPath.toOSString());
      f.getParentFile().mkdirs();
      IOUtility.writeContent(new FileOutputStream(f), data, true);
      getDestinationProject().getFile(getDestinationPath()).refreshLocal(IResource.DEPTH_ZERO, monitor);
    }
    catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "", e));
    }
  }

  /**
   * @return the sourceUrl
   * @see http://lmap.blogspot.com/2008/03/platform-scheme-uri.html
   */
  public URL getSourceUrl() {
    return m_sourceUrl;
  }

  /**
   * @return the destinationProject
   */
  public IProject getDestinationProject() {
    return m_destinationProject;
  }

  /**
   * @return the destinationFile
   */
  public String getDestinationPath() {
    return m_destinationPath;
  }
}
