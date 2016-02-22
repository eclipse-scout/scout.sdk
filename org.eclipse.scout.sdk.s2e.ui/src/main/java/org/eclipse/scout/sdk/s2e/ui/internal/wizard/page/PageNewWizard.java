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
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.scout.sdk.s2e.operation.page.PageNewOperation;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;

/**
 * <h3>{@link PageNewWizard}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PageNewWizard extends AbstractWizard implements INewWizard {

  public static Class<? extends PageNewWizardPage> pageClass = PageNewWizardPage.class;

  private PageNewWizardPage m_page1;
  private boolean m_executed = false;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    PackageContainer packageContainer = S2eUiUtils.getClientPackageOfSelection(selection);

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

  public void schedulePageCreation(final PageNewOperation op) {
    if (isExecuted()) {
      return; // no double runs
    }

    List<IResource> blockedFolders = new ArrayList<>(2);
    op.setClientSourceFolder(m_page1.getSourceFolder());
    blockedFolders.add(m_page1.getSourceFolder().getResource());
    op.setPackage(m_page1.getTargetPackage());
    op.setPageName(m_page1.getIcuName());
    IPackageFragmentRoot sharedSourceFolder = m_page1.getSharedSourceFolder();
    op.setSharedSourceFolder(sharedSourceFolder);
    if (S2eUtils.exists(sharedSourceFolder)) {
      blockedFolders.add(sharedSourceFolder.getResource());
    }
    op.setSuperType(m_page1.getSuperType());

    final Display d = getContainer().getShell().getDisplay();

    ResourceBlockingOperationJob job = new ResourceBlockingOperationJob(op, blockedFolders.toArray(new IResource[blockedFolders.size()]));
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
            try {
              JavaUI.openInEditor(createdPageType, true, true);
            }
            catch (PartInitException | JavaModelException e) {
              SdkLog.info("Unable to open type {} in editor.", createdPageType.getFullyQualifiedName(), e);
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
}