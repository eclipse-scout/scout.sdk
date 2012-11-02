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

import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.ScoutProjectNewOperation;
import org.eclipse.scout.sdk.ui.IScoutConstants;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.scout.sdk.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.ui.wizard.project.AbstractProjectNewWizardPage;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizardPage;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class ScoutProjectNewWizard extends AbstractWizard implements INewWizard, IScoutProjectWizard {

  private ScoutProjectNewWizardPage m_page1;
  private ScoutProjectTemplateWizardPage m_page2;

  public ScoutProjectNewWizard() {
    setWindowTitle(Texts.get("NewScoutProjectNoPopup"));
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_page1 = new ScoutProjectNewWizardPage();
    addPage(m_page1);
    m_page2 = new ScoutProjectTemplateWizardPage();
    addPage(m_page2);
  }

  @Override
  public boolean performFinish() {
    new P_PerformFinishJob(getContainer().getShell().getDisplay()).schedule();
    return true;
  }

  @Override
  public IScoutProjectWizardPage getProjectWizardPage() {
    return m_page1;
  }

  protected class P_PerformFinishJob extends Job {

    private final Display m_display;

    /**
     * @param name
     */
    public P_PerformFinishJob(Display display) {
      super("Creating new Scout project...");
      m_display = display;

    }

    protected IScoutProjectNewOperation getFinishOperation() {
      return new ScoutProjectNewOperation();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      // collect UI properties over all pages of the wizard
      PropertyMap properties = new PropertyMap();
      for (IWizardPage p : getPages()) {
        if (p instanceof AbstractProjectNewWizardPage) {
          ((AbstractProjectNewWizardPage) p).putProperties(properties);
        }
      }

      // execute project creation operations
      IScoutProjectNewOperation mainOperation = getFinishOperation();
      mainOperation.setProperties(properties);
      OperationJob job = new OperationJob(mainOperation);
      job.schedule();
      try {
        job.join();
      }
      catch (InterruptedException e) {
      }

      // wait until all jobs have finished
      try {
        Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_REFRESH, monitor);
        Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
      }
      catch (Exception e) {
        ScoutSdkUi.logError("error during waiting for auto build and refresh");
      }

      // rebuild bundle graph (the target platform could have been changed -> this could change the node types).
      ScoutWorkspace.getInstance().rebuildGraph();

      // switch to scout perspective and expand scout explorer
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

    @SuppressWarnings("all")
    // do not add an override annotation here (backwards compatibility)
    public String getAttribute(String attrName, String locale) throws InvalidRegistryObjectException {
      return null;
    }

    @SuppressWarnings("all")
    // do not add an override annotation here (backwards compatibility)
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
