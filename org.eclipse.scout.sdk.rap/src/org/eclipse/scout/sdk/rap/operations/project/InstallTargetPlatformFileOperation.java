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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;
import org.eclipse.scout.sdk.rap.internal.P2Utility;
import org.eclipse.scout.sdk.util.PlatformUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Version;

public class InstallTargetPlatformFileOperation extends InstallTextFileOperation {
  private static final String VARIABLE_RAP_LOCATION = "RAP_LOCATION";

  private static final String TARGET_FILE_NAME = "ScoutRAP.target";
  private String m_rapTargetLocalFolder;
  private Version m_rapVersion;
  private String m_rapTargetRemoteURL;
  private TemplateVariableSet m_variableSet;

  public InstallTargetPlatformFileOperation(IProject dstProject) {
    super("templates/ui.rap/ScoutRAP.target", TARGET_FILE_NAME, ScoutSdkRap.getDefault().getBundle(), dstProject, null);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    TemplateVariableSet variableSet = TemplateVariableSet.createNew();
    if (getRapTargetLocalFolder() != null) {
      variableSet.setVariable(VARIABLE_RAP_LOCATION, "<location path=\"" + getRapTargetLocalFolder() + "\" type=\"Directory\"/>");
    }
    else {
      try {
        String latestVersion = P2Utility.getLatestVersion("org.eclipse.rap.runtime.sdk.feature.group", new URI(getRapTargetRemoteURL()), monitor);
        if (latestVersion != null) {
          StringBuilder remoteLocationBuilder = new StringBuilder();
          remoteLocationBuilder.append("<location includeAllPlatforms=\"false\" includeMode=\"slicer\" type=\"InstallableUnit\">\n");
          remoteLocationBuilder.append("<unit id=\"org.eclipse.rap.runtime.sdk.feature.group\" version=\"" + latestVersion + "\"/>\n");
          remoteLocationBuilder.append("<repository location=\"" + getRapTargetRemoteURL() + "\"/>\n");
          remoteLocationBuilder.append("</location>");
          variableSet.setVariable(VARIABLE_RAP_LOCATION, remoteLocationBuilder.toString());
          m_rapVersion = Version.parseVersion(latestVersion);
        }
      }
      catch (URISyntaxException e) {
        ScoutSdkRap.logError("could not install rap target file.", e);
      }
      catch (IllegalArgumentException e) {
        ScoutSdkRap.logError("could parse rap remote version.", e);
      }
    }
    if (variableSet.getVariable(VARIABLE_RAP_LOCATION) != null) {
      m_variableSet = variableSet;
      super.run(monitor, workingCopyManager);
      PlatformUtility.resolveTargetPlatform(getCreatedFile(), monitor);
    }
    IFile productFile = getDstProject().getFile("products/development/app-rap-dev.product");
    if (productFile != null && productFile.exists()) {
      IOperation op = null;
      switch (m_rapVersion.getMinor()) {
        case 4:
          op = new UpdateProductRapV1_4(productFile);
          break;
        case 5:
          op = new UpdateProductRapV1_5(productFile);
          break;
      }
      if (op != null) {
        op.validate();
        op.run(monitor, workingCopyManager);
      }
    }
  }

  @Override
  public ITemplateVariableSet getTemplateBinding() {
    return m_variableSet;
  }

  public String getRapTargetLocalFolder() {
    return m_rapTargetLocalFolder;
  }

  public void setRapTargetLocalFolder(String rapTargetLocalFolder) {
    m_rapTargetLocalFolder = rapTargetLocalFolder;
  }

  public String getRapTargetRemoteURL() {
    return m_rapTargetRemoteURL;
  }

  public void setRapTargetRemoteURL(String rapTargetRemoteURL) {
    m_rapTargetRemoteURL = rapTargetRemoteURL;
  }

  public void setRapVersion(Version rapVersion) {
    m_rapVersion = rapVersion;
  }

  public Version getRapVersion() {
    return m_rapVersion;
  }
}
