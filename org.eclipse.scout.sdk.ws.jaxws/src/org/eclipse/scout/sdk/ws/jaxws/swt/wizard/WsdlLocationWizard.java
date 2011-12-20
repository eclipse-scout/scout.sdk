/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.ExternalFileCopyOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsStubGenerationOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsdlLocationWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SeparatorType;

public class WsdlLocationWizard extends AbstractWorkspaceWizard {

  private IScoutBundle m_bundle;
  private BuildJaxWsBean m_buildJaxWsBean;
  private String m_wsdlFileName;

  private WsdlLocationWizardPage m_wsdlLocationWizardPage;

  private ExternalFileCopyOperation[] m_copyOperations;
  private WsStubGenerationOperation m_stubGenerationOperation;
  private IFolder m_wsdlFolder;

  public WsdlLocationWizard(IScoutBundle bundle, BuildJaxWsBean buildJaxWsBean) {
    m_bundle = bundle;
    m_copyOperations = new ExternalFileCopyOperation[0];
    m_buildJaxWsBean = buildJaxWsBean;
    setWindowTitle(Texts.get("BrowseForWsdlFile"));
  }

  @Override
  public void addPages() {
    m_wsdlLocationWizardPage = new WsdlLocationWizardPage(m_bundle);
    m_wsdlLocationWizardPage.setRebuildStubOptionVisible(true);
    m_wsdlLocationWizardPage.setRebuildStub(true);
    m_wsdlLocationWizardPage.setWsdlFolderVisible(true);
    m_wsdlLocationWizardPage.setWsdlFolder(JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL, false)); // initial value
    addPage(m_wsdlLocationWizardPage);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    File wsdlFile = m_wsdlLocationWizardPage.getWsdlFile();
    m_wsdlFolder = m_wsdlLocationWizardPage.getWsdlFolder();
    m_wsdlFileName = wsdlFile.getName();

    List<ExternalFileCopyOperation> copyOperations = new LinkedList<ExternalFileCopyOperation>();
    if (isCopyRequired(m_wsdlFolder, wsdlFile)) {
      ExternalFileCopyOperation op = new ExternalFileCopyOperation();
      op.setBundle(m_bundle);
      op.setOverwrite(true);
      op.setExternalFile(m_wsdlLocationWizardPage.getWsdlFile());
      op.setWorkspacePath(m_wsdlFolder.getProjectRelativePath());
      copyOperations.add(op);
    }

    for (File file : m_wsdlLocationWizardPage.getAdditionalFiles()) {
      if (isCopyRequired(m_wsdlFolder, file)) {
        ExternalFileCopyOperation op = new ExternalFileCopyOperation();
        op.setBundle(m_bundle);
        op.setOverwrite(true);
        op.setExternalFile(file);
        op.setWorkspacePath(m_wsdlFolder.getProjectRelativePath());
        copyOperations.add(op);
      }
    }
    m_copyOperations = copyOperations.toArray(new ExternalFileCopyOperation[copyOperations.size()]);

    if (m_wsdlLocationWizardPage.isRebuildStub()) {
      m_stubGenerationOperation = new WsStubGenerationOperation();
      m_stubGenerationOperation.setBundle(m_bundle);
      m_stubGenerationOperation.setAlias(m_buildJaxWsBean.getAlias());
      m_stubGenerationOperation.setWsdlFolder(m_wsdlFolder);
      m_stubGenerationOperation.setProperties(m_buildJaxWsBean.getPropertiers());
      m_stubGenerationOperation.setWsdlFileName(wsdlFile.getName());
    }

    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    for (ExternalFileCopyOperation op : m_copyOperations) {
      op.validate();
      op.run(monitor, workingCopyManager);
    }

    // update entry in BuildJaxWs.xml
    m_buildJaxWsBean.setWsdl(JaxWsSdkUtility.normalizePath(m_wsdlFolder.getProjectRelativePath().append(m_wsdlFileName).toPortableString(), SeparatorType.None));
    ResourceFactory.getBuildJaxWsResource(m_bundle).storeXml(m_buildJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_BUILDJAXWS_WSDL_CHANGED, monitor, m_buildJaxWsBean.getAlias());

    if (m_stubGenerationOperation != null) {
      m_stubGenerationOperation.run(monitor, workingCopyManager);
    }
    return true;
  }

  private boolean isCopyRequired(IFolder wsdlFolder, File wsdlFile) {
    IFile potentialSameFile = JaxWsSdkUtility.getFile(m_bundle, wsdlFolder.getProjectRelativePath().toPortableString(), wsdlFile.getName(), false);

    if (potentialSameFile != null && potentialSameFile.exists()) {
      IPath potentialSameFilePath = new Path(potentialSameFile.getLocationURI().getRawPath());
      IPath wsdlFilePath = new Path(wsdlFile.getAbsolutePath());

      if (potentialSameFilePath.equals(wsdlFilePath)) {
        return false;
      }
    }

    return true;
  }
}
