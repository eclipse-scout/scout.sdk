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
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.permission;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.operation.permission.PermissionNewOperation;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link PermissionNewWizard}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PermissionNewWizard extends AbstractWizard implements INewWizard {

  private static volatile Class<? extends PermissionNewWizardPage> pageClass = PermissionNewWizardPage.class;

  private PermissionNewWizardPage m_page1;
  private boolean m_executed = false;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    PackageContainer packageContainer = S2eUiUtils.getSharedPackageOfSelection(selection);

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

  public void schedulePermissionCreation(final PermissionNewOperation op) {
    if (isExecuted()) {
      return; // no double runs
    }

    op.setPackage(m_page1.getTargetPackage());
    op.setPermissionName(m_page1.getIcuName());
    op.setSharedSourceFolder(m_page1.getSourceFolder());
    op.setSuperType(m_page1.getSuperType());

    final Display d = getContainer().getShell().getDisplay();

    ResourceBlockingOperationJob job = new ResourceBlockingOperationJob(op, m_page1.getSourceFolder().getResource());
    job.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        d.asyncExec(new Runnable() {
          @Override
          public void run() {
            // show created permission in the java editor
            IType createdPermissionType = op.getCreatedPermission();
            if (!S2eUtils.exists(createdPermissionType)) {
              return;
            }
            S2eUiUtils.openInEditor(createdPermissionType);
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
      schedulePermissionCreation(new PermissionNewOperation());
    }

    return true;
  }

  public boolean isExecuted() {
    return m_executed;
  }

  protected void setExecuted(boolean executed) {
    m_executed = executed;
  }

  public static Class<? extends PermissionNewWizardPage> getPage1Class() {
    return pageClass;
  }

  public static void setPage1Class(Class<? extends PermissionNewWizardPage> page1Class) {
    pageClass = page1Class;
  }
}
