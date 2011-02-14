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
package org.eclipse.scout.sdk.ui.internal.wizard.newproject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.project.NewBsiCaseGroupStep1Operation;
import org.eclipse.scout.sdk.operation.project.NewScoutProjectStep2Operation;
import org.eclipse.scout.sdk.operation.project.template.IScoutProjectTemplateOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.IScoutConstants;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class ScoutProjectNewWizard extends Wizard implements INewWizard {

  private ScoutProjectNewWizardPage m_page1;
  private ScoutProjectTemplateWizardPage m_page2;

  public ScoutProjectNewWizard() {
    setWindowTitle("New Scout Project");
  }

  @Override
  public void setContainer(IWizardContainer wizardContainer) {
    super.setContainer(wizardContainer);
  }

  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_page1 = new ScoutProjectNewWizardPage();
    // m_page1.setProjectCoding(ScoutIdeProperties.BUNDLE_TYPE_CLIENT | ScoutIdeProperties.BUNDLE_TYPE_CLIENT_APPLICATION | ScoutIdeProperties.BUNDLE_TYPE_SERVER | ScoutIdeProperties.BUNDLE_TYPE_SHARED);
    addPage(m_page1);
    m_page2 = new ScoutProjectTemplateWizardPage();
    addPage(m_page2);
  }

  @Override
  public final boolean performFinish() {
    new P_PerformFinishJob(getContainer().getShell().getDisplay()).schedule();
//    OperationJob operationJob = new OperationJob(new P_PerformFinishOperation(getContainer().getShell().getDisplay()));
//    operationJob.schedule();
    return true;
  }

  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    try {
      m_page1.performFinish(monitor, workingCopyManager);
      return true;
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not create scout project.", e);
      return false;
    }
  }

  @Override
  public boolean canFinish() {
    return m_page1.isPageComplete();
  }

  private class P_PerformFinishOperation implements IOperation {
    private Display m_display;
    private boolean m_success;

    public P_PerformFinishOperation(Display display) {
      m_display = display;
    }

    public boolean isSuccess() {
      return m_success;
    }

    public String getOperationName() {
      return getWindowTitle();
    }

    @Override
    public void validate() throws IllegalArgumentException {
    }

    public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
      m_success = performFinish(monitor, workingCopyManager);
      if (m_success) {
        m_display.asyncExec(new Runnable() {
          @Override
          public void run() {
            BasicNewProjectResourceWizard.updatePerspective(new P_ScoutPerspectiveConfigElement());
          }
        });
      }
    }
  } // end class P_PerformFinishOperation

  private class P_PerformFinishJob extends Job {

    private final Display m_display;

    /**
     * @param name
     */
    public P_PerformFinishJob(Display display) {
      super("Create new Scout project...");
      m_display = display;

    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      P_CreateProjectOperation createProjectOperation = new P_CreateProjectOperation();
      OperationJob createProjectJob = new OperationJob(createProjectOperation);
      if (scheduleAndWait(createProjectJob, 0)) {
        IScoutProjectTemplateOperation template = m_page2.getSelectedTemplate();
        if (template != null) {
          IProject shared = createProjectOperation.getSharedProject();
          if (shared != null) {
            template.setScoutProject(ScoutSdk.getDefault().getScoutWorkspace().getScoutBundle(shared).getScoutProject());
            OperationJob applyTemplateJob = new OperationJob(new P_ApplyTemplateOperation(template));

            scheduleAndWait(applyTemplateJob, 600);
          }
        }
        // switch to scout perspective
        m_display.asyncExec(new Runnable() {
          @Override
          public void run() {
            BasicNewProjectResourceWizard.updatePerspective(new P_ScoutPerspectiveConfigElement());
          }
        });

      }
      return Status.OK_STATUS;
    }

    public boolean scheduleAndWait(OperationJob job, long delay) {
      job.schedule(delay);
      try {
        job.join();
        return job.getResult().isOK();
      }
      catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return false;
      }
    }

  } // end class  P_PerformFinishJob

  private class P_CreateProjectOperation implements IOperation {
    private IProject m_sharedProject;

    @Override
    public String getOperationName() {
      return "Create projects...";
    }

    public void validate() throws IllegalArgumentException {
    }

    @Override
    public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      TemplateVariableSet variables = TemplateVariableSet.createNew(m_page1.getProjectName(), m_page1.getPostFix(), m_page1.getProjectAlias());
      NewBsiCaseGroupStep1Operation op1 = new NewBsiCaseGroupStep1Operation(variables);
      op1.setCreateUiSwing(m_page1.isCreateUiSwing());
      op1.setCreateUiSwt(m_page1.isCreateUiSwt());
      op1.setCreateClient(m_page1.isCreateClient());
      op1.setCreateShared(m_page1.isCreateShared());
      op1.setCreateServer(m_page1.isCreateServer());
      op1.setProjectName(m_page1.getProjectName());
      op1.setProjectNamePostfix(m_page1.getPostFix());
      op1.setProjectAlias(m_page1.getProjectAlias());
      op1.validate();
      op1.run(monitor, workingCopyManager);
      m_sharedProject = op1.getSharedProject();
      NewScoutProjectStep2Operation op2 = new NewScoutProjectStep2Operation(op1, variables);
      op2.validate();
      op2.run(monitor, workingCopyManager);
    }

    /**
     * @return the sharedProject
     */
    public IProject getSharedProject() {
      return m_sharedProject;
    }
  } // end class P_CreateProjectOperation

  private class P_ApplyTemplateOperation implements IOperation {
    private final IScoutProjectTemplateOperation m_template;

    public P_ApplyTemplateOperation(IScoutProjectTemplateOperation template) {
      m_template = template;
    }

    @Override
    public String getOperationName() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void validate() throws IllegalArgumentException {
      // TODO Auto-generated method stub
    }

    @Override
    public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      m_template.run(monitor, workingCopyManager);
    }

  }

  private class P_ScoutPerspectiveConfigElement implements IConfigurationElement {

    @Override
    public String getAttribute(String name) throws InvalidRegistryObjectException {
      if (name.equals("finalPerspective")) {
        return IScoutConstants.SCOUT_PERSPECTIVE_ID;
      }
      else if (name.equals("preferredPerspectives")) {
        return IScoutConstants.SCOUT_PERSPECTIVE_ID;
      }
      return null;
    }

    @Override
    public Object createExecutableExtension(String propertyName) throws CoreException {
      return null;
    }

    public String getAttribute(String attrName, String locale) throws InvalidRegistryObjectException {
      return null;
    }

    public String getValue(String locale) throws InvalidRegistryObjectException {
      return null;
    }

    @Override
    public String getAttributeAsIs(String name) throws InvalidRegistryObjectException {
      return null;
    }

    @Override
    public String[] getAttributeNames() throws InvalidRegistryObjectException {
      return null;
    }

    @Override
    public IConfigurationElement[] getChildren() throws InvalidRegistryObjectException {
      return null;
    }

    @Override
    public IConfigurationElement[] getChildren(String name) throws InvalidRegistryObjectException {
      return null;
    }

    @Override
    public IContributor getContributor() throws InvalidRegistryObjectException {
      return null;
    }

    @Override
    public IExtension getDeclaringExtension() throws InvalidRegistryObjectException {
      return null;
    }

    @Override
    public String getName() throws InvalidRegistryObjectException {
      return null;
    }

    @Override
    public String getNamespace() throws InvalidRegistryObjectException {
      return null;
    }

    @Override
    public String getNamespaceIdentifier() throws InvalidRegistryObjectException {
      return null;
    }

    @Override
    public Object getParent() throws InvalidRegistryObjectException {

      return null;
    }

    @Override
    public String getValue() throws InvalidRegistryObjectException {

      return null;
    }

    @Override
    public String getValueAsIs() throws InvalidRegistryObjectException {

      return null;
    }

    @Override
    public boolean isValid() {

      return false;
    }

  }

}
