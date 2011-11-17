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

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.IScoutConstants;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.scout.sdk.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.ui.wizard.project.AbstractProjectNewWizardPage;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizardPage;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class ScoutProjectNewWizard extends AbstractWizard implements INewWizard, IScoutProjectWizard {

  private ScoutProjectNewWizardPage m_page1;
  private ScoutProjectTemplateWizardPage m_page2;
  private IScoutProject m_createdProject;
  private IWizardPage m_page;
  private List<IJavaProject> m_createdBundles;

  public ScoutProjectNewWizard() {
    setWindowTitle(Texts.get("NewScoutProjectNoPopup"));
    m_createdBundles = new ArrayList<IJavaProject>();
  }

  @Override
  public void setContainer(IWizardContainer wizardContainer) {
    super.setContainer(wizardContainer);
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_page1 = new ScoutProjectNewWizardPage();
    // m_page1.setProjectCoding(ScoutIdeProperties.BUNDLE_TYPE_CLIENT | ScoutIdeProperties.BUNDLE_TYPE_CLIENT_APPLICATION | ScoutIdeProperties.BUNDLE_TYPE_SERVER | ScoutIdeProperties.BUNDLE_TYPE_SHARED);
    addPage(m_page1);
    m_page2 = new ScoutProjectTemplateWizardPage();
    addPage(m_page2);
  }

  /**
   * @param javaProject
   */
  public void addCreatedBundle(IJavaProject javaProject) {
    m_createdBundles.add(javaProject);
  }

  /**
   * @param javaProject
   */
  public IJavaProject getCreatedBundle(String bundleName) {
    for (IJavaProject p : m_createdBundles) {
      if (p.getElementName().equals(bundleName)) {
        return p;
      }
    }
    return null;
  }

  public IScoutProject getCreatedProject() {
    return m_createdProject;
  }

  void setCreatedProject(IScoutProject createdProject) {
    m_createdProject = createdProject;
  }

  @Override
  public final boolean performFinish() {
    new P_PerformFinishJob(getContainer().getShell().getDisplay()).schedule();
    return true;
  }

//  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
//    try {
//      m_page1.performFinish(monitor, workingCopyManager);
//      return true;
//    }
//    catch (Exception e) {
//      ScoutSdkUi.logError("could not create scout project.", e);
//      return false;
//    }
//  }

  @Override
  public boolean canFinish() {
//    boolean canFinish = true;
//    for (IWizardPage p : getPages()) {
//      if (!p.isPageComplete()) {
//        canFinish = false;
//      }
//    }
//    return canFinish;
    return super.canFinish();
  }

  @Override
  public IScoutProjectWizardPage getProjectWizardPage() {
    return m_page1;
  }

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
      for (IWizardPage p : getPages()) {
        if (p instanceof AbstractProjectNewWizardPage) {
          if (!((AbstractProjectNewWizardPage) p).performFinish(monitor)) {
            return Status.CANCEL_STATUS;
          }
        }
      }
      m_display.asyncExec(new Runnable() {
        @Override
        public void run() {
          BasicNewProjectResourceWizard.updatePerspective(new P_ScoutPerspectiveConfigElement());
          IScoutExplorerPart ex = ScoutSdkUi.getExplorer(true);
          if (ex != null)
            ex.expandAndSelectProjectLevel();
          }
      });
      return Status.OK_STATUS;
    }

//    protected IStatus run1(IProgressMonitor monitor) {
//      P_CreateProjectOperation createProjectOperation = new P_CreateProjectOperation();
//      OperationJob createProjectJob = new OperationJob(createProjectOperation);
//      if (scheduleAndWait(createProjectJob, 0)) {
//        IScoutProjectTemplateOperation template = m_page2.getSelectedTemplate();
//        if (template != null) {
//          try {
//            Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_REFRESH, monitor);
//            Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
//          }
//          catch (Exception e) {
//            ScoutSdkUi.logError("error during waiting for auto build and refresh");
//          }
//          IProject shared = createProjectOperation.getSharedProject();
//          if (shared != null) {
//            template.setScoutProject(ScoutSdk.getScoutWorkspace().getScoutBundle(shared).getScoutProject());
//            OperationJob applyTemplateJob = new OperationJob(new P_ApplyTemplateOperation(template));
//
//            scheduleAndWait(applyTemplateJob, 0);
//          }
//        }
//
//        IScoutProject[] pr = ScoutSdk.getScoutWorkspace().getRootProjects();
//        if (pr.length > 0) {
//          IScoutProject sp = pr[0];
//          IFile[] products = TreeUtility.getAllProductFiles(sp);
//          ArrayList<IFile> dev = new ArrayList<IFile>(products.length);
//          for (IFile f : products) {
//            if (f.getFullPath().toString().toLowerCase().contains("development")) {
//              dev.add(f);
//            }
//          }
//          ScoutProjectPropertyPart.saveProductLaunchers(sp, dev.toArray(new IFile[dev.size()]));
//        }
//
//        // switch to scout perspective
//        m_display.asyncExec(new Runnable() {
//          @Override
//          public void run() {
//            BasicNewProjectResourceWizard.updatePerspective(new P_ScoutPerspectiveConfigElement());
//            IScoutExplorerPart ex = ScoutSdkUi.getExplorer(true);
//            if (ex != null)
//              ex.expandAndSelectProjectLevel();
//            }
//        });
//      }
//      return Status.OK_STATUS;
//    }

    public boolean scheduleAndWait(OperationJob job, long delay) {
      job.schedule(delay);
      try {
        job.join();
        return job.getResult().isOK();
      }
      catch (InterruptedException e) {
        ScoutSdkUi.logWarning(e);
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

    @Override
    public void validate() throws IllegalArgumentException {
    }

    @Override
    public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      // XXX
//      TemplateVariableSet variables = TemplateVariableSet.createNew(m_page1.getProjectName(), m_page1.getProjectNamePostfix(), m_page1.getProjectAlias());
//      NewBsiCaseGroupStep1Operation op1 = new NewBsiCaseGroupStep1Operation(variables);
//      op1.setCreateUiSwing(m_page1.isCreateUiSwing());
//      op1.setCreateUiSwt(m_page1.isCreateUiSwt());
//      op1.setCreateClient(m_page1.isCreateClient());
//      op1.setCreateShared(m_page1.isCreateShared());
//      op1.setCreateServer(m_page1.isCreateServer());
//      op1.setProjectName(m_page1.getProjectName());
//      op1.setProjectNamePostfix(m_page1.getPostFix());
//      op1.setProjectAlias(m_page1.getProjectAlias());
//      op1.validate();
//      op1.run(monitor, workingCopyManager);
//      m_sharedProject = op1.getSharedProject();
//      NewScoutProjectStep2Operation op2 = new NewScoutProjectStep2Operation(op1, variables);
//      op2.validate();
//      op2.run(monitor, workingCopyManager);
    }

    /**
     * @return the sharedProject
     */
    public IProject getSharedProject() {
      return m_sharedProject;
    }
  } // end class P_CreateProjectOperation

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

    @Override
    public String getAttribute(String attrName, String locale) throws InvalidRegistryObjectException {
      return null;
    }

    @Override
    public String getValue(String locale) throws InvalidRegistryObjectException {
      return null;
    }

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
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
