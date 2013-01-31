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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.StringUtility;
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
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsdlLocationWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.PathNormalizer;

public class WsdlLocationWizard extends AbstractWorkspaceWizard {

  private IScoutBundle m_bundle;
  private BuildJaxWsBean m_buildJaxWsBean;
  private SunJaxWsBean m_sunJaxWsBean;
  private String m_wsdlFileName;

  private WsdlLocationWizardPage m_wsdlLocationWizardPage;

  private ExternalFileCopyOperation[] m_copyOperations;
  private WsStubGenerationOperation m_stubGenerationOperation;
  private IFolder m_wsdlFolder;

  /**
   * @param bundle
   * @param buildJaxWsBean
   * @param sunJaxWsBean
   *          is only used by webservice providers
   * @param oldWsdlFolder
   */
  public WsdlLocationWizard(IScoutBundle bundle, BuildJaxWsBean buildJaxWsBean, SunJaxWsBean sunJaxWsBean) {
    m_bundle = bundle;
    m_copyOperations = new ExternalFileCopyOperation[0];
    m_buildJaxWsBean = buildJaxWsBean;
    m_sunJaxWsBean = sunJaxWsBean;
    setWindowTitle(Texts.get("BrowseForWsdlFile"));
  }

  @Override
  public void addPages() {
    IFolder oldWsdlFolder = null;
    IFolder wsdlRootFolder = null;
    String wsdlLocation = null;
    if (m_sunJaxWsBean != null) {
      wsdlLocation = m_sunJaxWsBean.getWsdl();
      oldWsdlFolder = JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_PROVIDER, false);
      wsdlRootFolder = JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_PROVIDER, false);
    }
    else {
      wsdlLocation = m_buildJaxWsBean.getWsdl();
      oldWsdlFolder = JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_CONSUMER, false);
      wsdlRootFolder = JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_CONSUMER, false);
    }

    if (StringUtility.hasText(wsdlLocation)) {
      oldWsdlFolder = JaxWsSdkUtility.getParentFolder(m_bundle, JaxWsSdkUtility.getFile(m_bundle, new Path(wsdlLocation), false));
    }

    m_wsdlLocationWizardPage = new WsdlLocationWizardPage(m_bundle);
    m_wsdlLocationWizardPage.setRebuildStubOptionVisible(true);
    m_wsdlLocationWizardPage.setRebuildStub(true);
    m_wsdlLocationWizardPage.setWsdlFolderVisible(true);
    m_wsdlLocationWizardPage.setWsdlFolder(oldWsdlFolder); // initial value
    m_wsdlLocationWizardPage.setRootWsdlFolder(wsdlRootFolder);
    addPage(m_wsdlLocationWizardPage);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    File wsdlFile = m_wsdlLocationWizardPage.getWsdlFile();
    m_wsdlFolder = m_wsdlLocationWizardPage.getWsdlFolder();
    m_wsdlFileName = wsdlFile.getName();

    List<ExternalFileCopyOperation> copyOperations = new LinkedList<ExternalFileCopyOperation>();
    if (!JaxWsSdkUtility.existsFileInProject(m_bundle, m_wsdlFolder, wsdlFile)) {
      ExternalFileCopyOperation op = new ExternalFileCopyOperation();
      op.setBundle(m_bundle);
      op.setOverwrite(true);
      op.setExternalFile(m_wsdlLocationWizardPage.getWsdlFile());
      op.setWorkspacePath(m_wsdlFolder.getProjectRelativePath());
      copyOperations.add(op);
    }

    for (File file : m_wsdlLocationWizardPage.getAdditionalFiles()) {
      if (!JaxWsSdkUtility.existsFileInProject(m_bundle, m_wsdlFolder, file)) {
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

    String wsdlFilePath = PathNormalizer.toWsdlPath(m_wsdlFolder.getProjectRelativePath().append(m_wsdlFileName).toString());
    if (m_sunJaxWsBean != null) { // webservice provider
      // update entry in sunJaxWs.xml
      m_sunJaxWsBean.setWsdl(wsdlFilePath);
      ResourceFactory.getSunJaxWsResource(m_bundle).storeXml(m_sunJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_SUNJAXWS_WSDL_CHANGED, monitor, m_sunJaxWsBean.getAlias());
    }
    else { // webservice consumer
      // update entry in buildJaxWs.xml
      m_buildJaxWsBean.setWsdl(wsdlFilePath);
      ResourceFactory.getBuildJaxWsResource(m_bundle).storeXml(m_buildJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_BUILDJAXWS_WSDL_CHANGED, monitor, m_buildJaxWsBean.getAlias());
    }

    if (m_stubGenerationOperation != null) {
      m_stubGenerationOperation.run(monitor, workingCopyManager);
    }
    return true;
  }
}
