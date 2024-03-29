/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.jaxws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceNewOperation;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.jaxws.WebServiceNewWizardPage.WebServiceType;
import org.eclipse.scout.sdk.s2e.ui.internal.jaxws.editor.WebServiceEditor;
import org.eclipse.scout.sdk.s2e.ui.internal.jaxws.editor.WebServiceEditorInput;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.ui.wizard.WizardFinishTask;
import org.eclipse.scout.sdk.s2e.ui.wizard.WizardFinishTask.PageToOperationMappingInput;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link WebServiceNewWizard}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceNewWizard extends AbstractWizard implements INewWizard {

  private WebServiceNewWizardPage m_page1;
  private WizardFinishTask<WebServiceNewOperation> m_finishTask;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_page1 = new WebServiceNewWizardPage();
    addPage(getWebServiceNewWizardPage());

    m_finishTask = new WizardFinishTask<>(workbench.getDisplay());
    m_finishTask
        .withOperation(WebServiceNewOperation::new)
        .withMapper(this::mapPageToOperation)
        .withUiAction(WebServiceNewWizard::afterOperation);

    setWindowTitle(getWebServiceNewWizardPage().getTitle());
    setHelpAvailable(true);
    setDefaultPageImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.ScoutProjectNewWizBanner));
  }

  /**
   * Fills the operation with the values from the UI. This method is called in a worker thread.
   *
   * @param input
   *          The mapping input.
   * @param op
   *          The operation to fill
   */
  protected void mapPageToOperation(PageToOperationMappingInput input, WebServiceNewOperation op) {
    var webServiceType = getWebServiceNewWizardPage().getWebServiceType();
    var createConsumer = WebServiceType.CONSUMER_FROM_EXISTING_WSDL == webServiceType;
    var createFromEmptyWsdl = WebServiceType.PROVIDER_FROM_EMPTY_WSDL == webServiceType;

    op.setCreateConsumer(createConsumer);
    op.setCreateEmptyWsdl(createFromEmptyWsdl);
    if (createFromEmptyWsdl) {
      op.setWsdlName(getWebServiceNewWizardPage().getWsdlName());
    }
    else {
      try {
        if (createConsumer) {
          op.setWsdlUrl(new URL(getWebServiceNewWizardPage().getConsumerWsdlUrl()));
        }
        else {
          op.setWsdlUrl(new URL(getWebServiceNewWizardPage().getProviderWsdlUrl()));
        }
      }
      catch (MalformedURLException e) {
        SdkLog.error("Invalid URL.", e);
      }
    }
    op.setCreateNewModule(getWebServiceNewWizardPage().isCreateNewProject());
    if (getWebServiceNewWizardPage().isCreateNewProject()) {
      op.setServerModule(getWebServiceNewWizardPage().getServerProject());
      op.setArtifactId(getWebServiceNewWizardPage().getArtifactId());
    }
    else {
      op.setJaxWsProject(getWebServiceNewWizardPage().getExistingJaxWsProject());
    }
    op.setPackage(getWebServiceNewWizardPage().getTargetPackage());
  }

  protected static void afterOperation(WebServiceNewOperation op, Display d) {
    showJaxwsEditor(op, d);
    WebServiceMessageDialog.open(op, d);
  }

  protected static void showJaxwsEditor(WebServiceNewOperation operation, Display d) {
    var jaxWsProject = operation.getJaxWsProject();
    if (!JdtUtils.exists(jaxWsProject)) {
      return;
    }
    var jaxwsFile = findJaxwsFileIn(jaxWsProject.getProject());
    if (jaxwsFile == null) {
      return;
    }
    @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    var wsdlName = operation.getCreatedWsdlFile().getFileName().toString();
    d.asyncExec(() -> openJaxwsEditor(jaxwsFile, wsdlName));
  }

  protected static IFile findJaxwsFileIn(IResource owner) {
    try {
      var result = new IFile[1];
      owner.accept((IResourceProxyVisitor) proxy -> {
        if (result[0] == null && proxy.getType() == IResource.FILE && proxy.getName().toLowerCase(Locale.US).endsWith('.' + WebServiceEditor.WEB_SERVICE_FILE_EXTENSION)) {
          var resource = (IFile) proxy.requestResource();
          if (resource.exists()) {
            result[0] = resource;
            return false;
          }
        }
        return true;
      }, IResource.DEPTH_ONE, 0);
      return result[0];
    }
    catch (CoreException e) {
      SdkLog.info("Unable to search for thet jaxws file in project.", e);
      return null;
    }
  }

  protected static void openJaxwsEditor(IFile jaxwsFile, String wsdlName) {
    S2eUiUtils.openInEditor(new WebServiceEditorInput(jaxwsFile, wsdlName), WebServiceEditor.WEB_SERVICE_EDITOR_ID, true);
  }

  @Override
  public WizardFinishTask<WebServiceNewOperation> getFinishTask() {
    return m_finishTask;
  }

  public WebServiceNewWizardPage getWebServiceNewWizardPage() {
    return m_page1;
  }
}
