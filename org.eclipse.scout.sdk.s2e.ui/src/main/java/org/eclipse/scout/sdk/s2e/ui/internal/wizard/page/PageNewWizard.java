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
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.page;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.operation.page.PageNewOperation;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link PageNewWizard}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PageNewWizard extends AbstractWizard implements INewWizard {

  private static volatile Class<? extends PageNewWizardPage> pageClass = PageNewWizardPage.class;

  private PageNewWizardPage m_page1;
  private boolean m_executed = false;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    PackageContainer packageContainer = S2eUiUtils.getClientPackageOfSelection(selection);

    try {
      m_page1 = getPage1Class().getConstructor(PackageContainer.class).newInstance(packageContainer);
      addPage(m_page1);

      setWindowTitle(m_page1.getTitle());
      setHelpAvailable(true);
      setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_NEWCLASS);
    }
    catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
      throw new SdkException(e);
    }
  }

  public void schedulePageCreation(final PageNewOperation op) {
    schedulePageCreation(op, null);
  }

  public void schedulePageCreation(final PageNewOperation op, Set<IResource> blockingFolders) {
    if (isExecuted()) {
      return; // no double runs
    }

    if (blockingFolders == null) {
      blockingFolders = new HashSet<>(5);
    }
    op.setClientSourceFolder(m_page1.getSourceFolder());
    blockingFolders.add(m_page1.getSourceFolder().getResource());
    op.setPackage(m_page1.getTargetPackage());
    op.setPageName(m_page1.getIcuName());
    IPackageFragmentRoot selectedSharedFolder = m_page1.getSharedSourceFolder();
    if (S2eUtils.exists(selectedSharedFolder)) {
      op.setSharedSourceFolder(selectedSharedFolder);
      blockingFolders.add(selectedSharedFolder.getResource());

      IPackageFragmentRoot dtoSourceFolder = S2eUtils.getDtoSourceFolder(selectedSharedFolder);
      op.setPageDataSourceFolder(dtoSourceFolder);
      blockingFolders.add(dtoSourceFolder.getResource());
    }

    IPackageFragmentRoot selectedServerFolder = m_page1.getServerSourceFolder();
    if (S2eUtils.exists(selectedServerFolder)) {
      op.setServerSourceFolder(selectedServerFolder);
      blockingFolders.add(selectedServerFolder.getResource());

      if (!S2eUtils.exists(op.getTestSourceFolder())) {
        IPackageFragmentRoot serviceTestSourceFolder = S2eUiUtils.getTestSourceFolder(selectedServerFolder, IScoutRuntimeTypes.ServerTestRunner, "service test");
        if (serviceTestSourceFolder != null) {
          op.setTestSourceFolder(serviceTestSourceFolder);
          blockingFolders.add(serviceTestSourceFolder.getResource());
        }
      }
    }

    op.setSuperType(m_page1.getSuperType());

    final Display d = getContainer().getShell().getDisplay();

    ResourceBlockingOperationJob job = new ResourceBlockingOperationJob(op, blockingFolders.toArray(new IResource[blockingFolders.size()]));
    job.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        d.asyncExec(new Runnable() {
          @Override
          public void run() {
            // show created page in the java editor
            IType createdPageType = op.getCreatedPage();
            if (!S2eUtils.exists(createdPageType)) {
              return;
            }
            S2eUiUtils.openInEditor(createdPageType);
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
      schedulePageCreation(new PageNewOperation());
    }

    return true;
  }

  public boolean isExecuted() {
    return m_executed;
  }

  protected void setExecuted(boolean executed) {
    m_executed = executed;
  }

  public static Class<? extends PageNewWizardPage> getPage1Class() {
    return pageClass;
  }

  public static void setPage1Class(Class<? extends PageNewWizardPage> page1Class) {
    pageClass = page1Class;
  }
}
