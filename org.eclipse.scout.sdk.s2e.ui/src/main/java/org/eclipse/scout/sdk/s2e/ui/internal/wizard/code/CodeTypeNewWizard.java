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
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.code;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.operation.codetype.CodeTypeNewOperation;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link CodeTypeNewWizard}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class CodeTypeNewWizard extends AbstractWizard implements INewWizard {

  public static volatile Class<? extends CodeTypeNewWizardPage> pageClass = CodeTypeNewWizardPage.class;

  private CodeTypeNewWizardPage m_page1;
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

  public void scheduleCodeTypeCreation(final CodeTypeNewOperation op) {
    if (isExecuted()) {
      return; // no double runs
    }

    op.setCodeTypeName(m_page1.getIcuName());
    op.setPackage(m_page1.getTargetPackage());
    op.setSharedSourceFolder(m_page1.getSourceFolder());
    op.setCodeTypeIdSignature(m_page1.getCodeTypeIdDatatypeSignature());
    op.setSuperTypeSignature(m_page1.getSuperTypeSignature());

    final Display d = getContainer().getShell().getDisplay();

    ResourceBlockingOperationJob job = new ResourceBlockingOperationJob(op, m_page1.getSourceFolder().getResource());
    job.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        d.asyncExec(new Runnable() {
          @Override
          public void run() {
            // show created codetype in the java editor
            IType createdCodeType = op.getCreatedCodeType();
            if (!S2eUtils.exists(createdCodeType)) {
              return;
            }
            S2eUiUtils.openInEditor(createdCodeType);
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
      scheduleCodeTypeCreation(new CodeTypeNewOperation());
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
