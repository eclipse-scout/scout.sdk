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
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
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

public class StubRebuildAction extends AbstractLinkAction {

  private IScoutBundle m_bundle;
  private WsdlResource m_wsdlResource;
  private String m_markerGroupUUID;
  private WebserviceEnum m_webserviceEnum;
  private BuildJaxWsBean m_buildJaxWsBean;

  public StubRebuildAction() {
    super(Texts.get("RebuildWebserviceStub"), JaxWsSdk.getImageDescriptor(JaxWsIcons.RebuildWsStub));
    setLinkText(Texts.get("RebuildWebserviceStub"));
    setToolTip(Texts.get("ClickToGenerateWsStub"));
  }

  public void init(IScoutBundle bundle, BuildJaxWsBean buildJaxWsBean, WsdlResource wsdlResource, String markerGroupUUID, WebserviceEnum webserviceEnum) {
    m_bundle = bundle;
    m_buildJaxWsBean = buildJaxWsBean;
    m_wsdlResource = wsdlResource;
    m_markerGroupUUID = markerGroupUUID;
    m_webserviceEnum = webserviceEnum;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    WsStubGenerationOperation op = new WsStubGenerationOperation();
    op.setBundle(m_bundle);
    op.setAlias(m_buildJaxWsBean.getAlias());
    IPath wsdlFolderPath = m_wsdlResource.getFile().getProjectRelativePath().removeLastSegments(1);// get folder of WSDL file
    op.setWsdlFolder(JaxWsSdkUtility.getFolder(m_bundle, wsdlFolderPath, false));
    op.setProperties(m_buildJaxWsBean.getPropertiers());
    op.addOperationFinishedListener(new P_OperationFinishedListener());
    op.setWsdlFileName(m_wsdlResource.getFile().getName());
    OperationJob job = new OperationJob(op);
    job.setSystem(false);
    job.setUser(false);
    job.setPriority(Job.INTERACTIVE);
    job.schedule();
    return null;
  }

  private class P_OperationFinishedListener implements IOperationFinishedListener {

    @Override
    public void operationFinished(final boolean success, final Throwable e) {
      if (m_webserviceEnum == WebserviceEnum.Provider) {
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

  private String getStacktrace(Throwable t) {
    if (t == null) {
      return null;
    }
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }
}
