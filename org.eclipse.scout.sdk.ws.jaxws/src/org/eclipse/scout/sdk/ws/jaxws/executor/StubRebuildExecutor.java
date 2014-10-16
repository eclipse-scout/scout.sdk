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
package org.eclipse.scout.sdk.ws.jaxws.executor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsStubGenerationOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ErrorDialog;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceConsumerNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IOperationFinishedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link StubRebuildExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class StubRebuildExecutor extends AbstractExecutor {

  private IScoutBundle m_bundle;
  private String m_markerGroupUUID;
  private WebserviceEnum m_webserviceEnum;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_bundle = UiUtility.getScoutBundleFromSelection(selection);
    return isEditable(m_bundle);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    Object selected = selection.getFirstElement();
    WsdlResource wsdlResource;
    BuildJaxWsBean buildJaxWsBean;
    if (selected instanceof WebServiceConsumerNodePage) {
      WebServiceConsumerNodePage p = (WebServiceConsumerNodePage) selected;
      buildJaxWsBean = p.getBuildJaxWsBean();
      wsdlResource = p.getWsdlResource();
      m_markerGroupUUID = p.getMarkerGroupUUID();
      m_webserviceEnum = WebserviceEnum.CONSUMER;
    }
    else if (selected instanceof WebServiceProviderNodePage) {
      WebServiceProviderNodePage p = (WebServiceProviderNodePage) selected;
      buildJaxWsBean = p.getBuildJaxWsBean();
      wsdlResource = p.getWsdlResource();
      m_markerGroupUUID = p.getMarkerGroupUUID();
      m_webserviceEnum = WebserviceEnum.PROVIDER;
    }
    else {
      return null;
    }

    IPath wsdlFolderPath = wsdlResource.getFile().getProjectRelativePath().removeLastSegments(1);// get folder of WSDL file

    WsStubGenerationOperation op = new WsStubGenerationOperation();
    op.setBundle(m_bundle);
    op.setAlias(buildJaxWsBean.getAlias());
    op.setWsdlFolder(JaxWsSdkUtility.getFolder(m_bundle, wsdlFolderPath, false));
    op.setProperties(buildJaxWsBean.getPropertiers());
    op.addOperationFinishedListener(new P_OperationFinishedListener());
    op.setWsdlFileName(wsdlResource.getFile().getName());

    OperationJob job = new OperationJob(op);
    job.setSystem(false);
    job.setUser(false);
    job.setPriority(Job.SHORT);
    job.schedule();

    return null;
  }

  private class P_OperationFinishedListener implements IOperationFinishedListener {

    @Override
    public void operationFinished(final boolean success, final Throwable e) {
      if (m_webserviceEnum == WebserviceEnum.PROVIDER) {
        JaxWsSdk.getDefault().notifyPageReload(WebServiceProviderNodePage.class, m_markerGroupUUID, WebServiceProviderNodePage.DATA_STUB_FILES);
      }
      else {
        JaxWsSdk.getDefault().notifyPageReload(WebServiceConsumerNodePage.class, m_markerGroupUUID, WebServiceConsumerNodePage.DATA_STUB_FILES);
      }

      ScoutSdkUi.getDisplay().asyncExec(new Runnable() {

        @Override
        public void run() {
          if (success) {
            MessageBox messageBox = new MessageBox(ScoutSdkUi.getShell(), SWT.ICON_INFORMATION | SWT.OK);
            messageBox.setMessage(Texts.get("WsStubSuccessfullyGenerated"));
            messageBox.open();
          }
          else {
            ErrorDialog dialog = new ErrorDialog(Texts.get("StubGenerationFailed"));
            dialog.setError(Texts.get("WsStubGenerationFailed"), e);
            dialog.open();
          }
        }
      });
    }
  }
}
