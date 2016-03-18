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
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.lookupcall;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.operation.lookupcall.LookupCallNewOperation;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;

/**
 * <h3>{@link LookupCallNewWizard}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class LookupCallNewWizard extends AbstractWizard implements INewWizard {

  public static Class<? extends LookupCallNewWizardPage> pageClass = LookupCallNewWizardPage.class;

  private LookupCallNewWizardPage m_page1;
  private boolean m_executed = false;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    PackageContainer packageContainer = S2eUiUtils.getSharedPackageOfSelection(selection);

    try {
      m_page1 = pageClass.getConstructor(PackageContainer.class).newInstance(packageContainer);
      addPage(m_page1);

      setWindowTitle(m_page1.getTitle());
      setHelpAvailable(true);
      setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_NEWCLASS);
    }
    catch (InvocationTargetException e) {
      throw new SdkException(e.getCause());
    }
    catch (Exception e) {
      throw new SdkException(e);
    }
  }

  public void scheduleLookupCallCreation(final LookupCallNewOperation op) {
    scheduleLookupCallCreation(op, null);
  }

  public void scheduleLookupCallCreation(final LookupCallNewOperation op, Set<IResource> blockingFolders) {
    if (isExecuted()) {
      return; // no double runs
    }
    if (blockingFolders == null) {
      blockingFolders = new HashSet<>(3);
    }
    op.setPackage(m_page1.getTargetPackage());
    op.setLookupCallName(m_page1.getIcuName());
    op.setSharedSourceFolder(m_page1.getSourceFolder());
    blockingFolders.add(m_page1.getSourceFolder().getResource());
    op.setSuperType(m_page1.getSuperType());
    op.setKeyType(m_page1.getKeyType());
    IPackageFragmentRoot serverSourceFolder = m_page1.getServerSourceFolder();
    if (S2eUtils.exists(serverSourceFolder)) {
      blockingFolders.add(serverSourceFolder.getResource());
    }
    op.setServerSourceFolder(serverSourceFolder);
    op.setLookupServiceSuperType(m_page1.getServiceImplSuperType());

    if (!S2eUtils.exists(op.getTestSourceFolder())) {
      // calculate test source if not already set
      IPackageFragmentRoot testSourceFolder = S2eUiUtils.getTestSourceFolder(m_page1.getServerSourceFolder());
      if (testSourceFolder != null) {
        op.setTestSourceFolder(testSourceFolder);
        blockingFolders.add(testSourceFolder.getResource());
      }
    }

    final Display d = getContainer().getShell().getDisplay();

    ResourceBlockingOperationJob job = new ResourceBlockingOperationJob(op, blockingFolders.toArray(new IResource[blockingFolders.size()]));
    job.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        d.asyncExec(new Runnable() {
          @Override
          public void run() {
            // show created lookup call in the java editor
            IType createdLookupCallType = op.getCreatedLookupCall();
            if (!S2eUtils.exists(createdLookupCallType)) {
              return;
            }
            try {
              JavaUI.openInEditor(createdLookupCallType, true, true);
            }
            catch (PartInitException | JavaModelException e) {
              SdkLog.info("Unable to open type {} in editor.", createdLookupCallType.getFullyQualifiedName(), e);
            }
          }
        });
      }
    });
    job.schedule();
    setExecuted(true);
  }

  @Override
  public boolean performFinish() {
    if (!super.performFinish()) {
      return false;
    }

    if (!isExecuted()) {
      scheduleLookupCallCreation(new LookupCallNewOperation());
    }

    return true;
  }

  public boolean isExecuted() {
    return m_executed;
  }

  protected void setExecuted(boolean executed) {
    m_executed = executed;
  }
}
