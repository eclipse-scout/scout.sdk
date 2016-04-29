/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.jaxws;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceNewOperation;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.editor.jaxws.WebServiceEditor;
import org.eclipse.scout.sdk.s2e.ui.internal.editor.jaxws.WebServiceEditorInput;
import org.eclipse.scout.sdk.s2e.ui.internal.wizard.jaxws.WebServiceNewWizardPage.WebServiceType;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link WebServiceNewWizard}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WebServiceNewWizard extends AbstractWizard implements INewWizard {

  private WebServiceNewWizardPage m_page1;
  private IWebServiceNewWizardFinishedAction m_finishAction;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_page1 = new WebServiceNewWizardPage();
    addPage(m_page1);

    setWindowTitle(m_page1.getTitle());
    setHelpAvailable(true);
    setDefaultPageImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.ScoutProjectNewWizBanner));
    setFinishAction(new P_ShowJaxwsEditorAction());
  }

  @Override
  public boolean performFinish() {
    if (!super.performFinish()) {
      return false;
    }

    try {
      WebServiceType webServiceType = m_page1.getWebServiceType();
      final boolean createConsumer = WebServiceType.CONSUMER_FROM_EXISTING_WSDL.equals(webServiceType);
      final boolean createFromEmptyWsdl = WebServiceType.PROVIDER_FROM_EMPTY_WSDL.equals(webServiceType);

      final WebServiceNewOperation op = new WebServiceNewOperation();
      op.setCreateConsumer(createConsumer);
      op.setCreateEmptyWsdl(createFromEmptyWsdl);
      if (createFromEmptyWsdl) {
        op.setWsdlName(m_page1.getWsdlName());
      }
      else {
        if (createConsumer) {
          op.setWsdlUrl(new URL(m_page1.getConsumerWsdlUrl()));
        }
        else {
          op.setWsdlUrl(new URL(m_page1.getProviderWsdlUrl()));
        }
      }
      op.setCreateNewModule(m_page1.isCreateNewProject());
      if (m_page1.isCreateNewProject()) {
        op.setServerModule(m_page1.getServerProject());
        op.setArtifactId(m_page1.getArtifactId());
      }
      else {
        op.setJaxWsProject(m_page1.getExistingJaxWsProject());
      }
      op.setPackage(m_page1.getTargetPackage());

      final Display d = getContainer().getShell().getDisplay();

      ResourceBlockingOperationJob job = new ResourceBlockingOperationJob(op);
      job.addJobChangeListener(new JobChangeAdapter() {
        @Override
        public void done(IJobChangeEvent event) {
          if (!event.getResult().isOK()) {
            // operation ended with error. do not call post-action
            return;
          }

          IWebServiceNewWizardFinishedAction finishAction = getFinishAction();
          if (finishAction == null) {
            return;
          }
          finishAction.operationFinished(op, d);
          d.asyncExec(new Runnable() {
            @Override
            public void run() {
              Shell shell = JavaPlugin.getActiveWorkbenchShell();
              if (shell == null) {
                return;
              }
              WebServiceMessageDialog.open(shell, op);
            }
          });
        }
      });
      job.schedule();

      return true;
    }
    catch (MalformedURLException e) {
      SdkLog.error("Invalid URL.", e);
      return false;
    }
  }

  public interface IWebServiceNewWizardFinishedAction {
    /**
     * Hook to execute code when the web service operation is done. This callback is executed in a worker thread. To
     * update UI use the given {@link Display}
     *
     * @param operation
     *          The operation that just finished.
     * @param d
     *          The {@link Display} to update UI components
     */
    void operationFinished(WebServiceNewOperation operation, Display d);
  }

  protected static class P_ShowJaxwsEditorAction implements IWebServiceNewWizardFinishedAction {
    @Override
    public void operationFinished(WebServiceNewOperation operation, Display d) {
      IJavaProject jaxWsProject = operation.getJaxWsProject();
      if (!S2eUtils.exists(jaxWsProject)) {
        return;
      }
      final IFile jaxwsFile = findJaxwsFile(jaxWsProject.getProject());
      if (jaxwsFile == null) {
        return;
      }
      final IFile wsdl = operation.getCreatedWsdlFile();
      d.asyncExec(new Runnable() {
        @Override
        public void run() {
          showJaxwsEditor(jaxwsFile, wsdl);
        }
      });
    }
  }

  protected static void showJaxwsEditor(IFile jaxwsFile, IFile wsdl) {
    WebServiceEditorInput input = new WebServiceEditorInput(jaxwsFile);
    if (wsdl != null && wsdl.exists()) {
      input.setPageIdToActivate(wsdl.getName());
    }
    S2eUiUtils.openInEditor(input, WebServiceEditor.WEB_SERVICE_EDITOR_ID);
  }

  protected static IFile findJaxwsFile(IProject owner) {
    try {
      final IFile[] result = new IFile[1];
      owner.accept(new IResourceProxyVisitor() {
        @Override
        public boolean visit(IResourceProxy proxy) throws CoreException {
          if (result[0] == null && proxy.getType() == IResource.FILE && proxy.getName().toLowerCase().endsWith('.' + WebServiceEditor.WEB_SERVICE_FILE_EXTENSION)) {
            IFile resource = (IFile) proxy.requestResource();
            if (resource.exists()) {
              result[0] = resource;
              return false;
            }
          }
          return true;
        }
      }, IResource.DEPTH_ONE, 0);
      return result[0];
    }
    catch (CoreException e) {
      SdkLog.info("Unable to search for thet jaxws file in project.", e);
      return null;
    }
  }

  public WebServiceNewWizardPage getWebServiceNewWizardPage() {
    return m_page1;
  }

  public IWebServiceNewWizardFinishedAction getFinishAction() {
    return m_finishAction;
  }

  public void setFinishAction(IWebServiceNewWizardFinishedAction finishAction) {
    m_finishAction = finishAction;
  }
}
