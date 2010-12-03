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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.IScoutConstants;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class ScoutProjectNewWizard extends Wizard implements INewWizard {

  private ScoutProjectNewWizardPage m_page1;

  public ScoutProjectNewWizard() {
  }

  public static void createFolder(IFolder folder) throws CoreException {
    if (!folder.exists()) {
      IContainer parent = folder.getParent();
      if (parent instanceof IFolder) {
        createFolder((IFolder) parent);
      }
      folder.create(true, true, null);
    }
  }

  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_page1 = new ScoutProjectNewWizardPage();
    // m_page1.setProjectCoding(ScoutIdeProperties.BUNDLE_TYPE_CLIENT | ScoutIdeProperties.BUNDLE_TYPE_CLIENT_APPLICATION | ScoutIdeProperties.BUNDLE_TYPE_SERVER | ScoutIdeProperties.BUNDLE_TYPE_SHARED);
    addPage(m_page1);
  }

  @Override
  public final boolean performFinish() {
    OperationJob operationJob = new OperationJob(new P_PerformFinishOperation(getContainer().getShell().getDisplay()));
    operationJob.schedule();
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
