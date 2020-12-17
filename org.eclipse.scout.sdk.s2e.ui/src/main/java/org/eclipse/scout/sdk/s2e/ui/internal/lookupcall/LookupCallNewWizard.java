/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.lookupcall;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.core.s.lookupcall.LookupCallNewOperation;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.ui.wizard.WizardFinishTask;
import org.eclipse.scout.sdk.s2e.ui.wizard.WizardFinishTask.PageToOperationMappingInput;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link LookupCallNewWizard}</h3>
 *
 * @since 5.2.0
 */
public class LookupCallNewWizard extends AbstractWizard implements INewWizard {

  private static volatile Class<? extends LookupCallNewWizardPage> pageClass = LookupCallNewWizardPage.class;

  private LookupCallNewWizardPage m_page1;
  private WizardFinishTask<LookupCallNewOperation> m_finishTask;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_page1 = initNewClassWizardWithPage(getPage1Class(), S2eUiUtils.getSharedPackageOfSelection(selection));
    m_finishTask = new WizardFinishTask<>(workbench.getDisplay());
    m_finishTask
        .withOperation(LookupCallNewOperation::new)
        .withMapper(this::mapPageToOperation)
        .withUiAction((op, d) -> d.asyncExec(() -> S2eUiUtils.openInEditor(op.getCreatedLookupCall(), false)));
  }

  @Override
  public WizardFinishTask<LookupCallNewOperation> getFinishTask() {
    return m_finishTask;
  }

  @SuppressWarnings("resource")
  protected void mapPageToOperation(PageToOperationMappingInput input, LookupCallNewOperation op) {
    op.setPackage(m_page1.getTargetPackage());
    op.setLookupCallName(m_page1.getIcuName());
    op.setSharedSourceFolder(input.environment().toScoutSourceFolder(m_page1.getSourceFolder()));
    op.setSuperType(m_page1.getSuperType().getFullyQualifiedName());
    op.setKeyType(m_page1.getKeyType().getFullyQualifiedName());
    var serverSourceFolder = m_page1.getServerSourceFolder();
    if (JdtUtils.exists(serverSourceFolder)) {
      op.setServerSourceFolder(input.environment().toScoutSourceFolder(serverSourceFolder));
    }
    op.setLookupServiceSuperType(m_page1.getServiceImplSuperType().getFullyQualifiedName());

    IJavaProject testProject;
    if (op.getTestSourceFolder() == null) {
      // calculate test source if not already set
      var testSourceFolder = S2eUiUtils.getTestSourceFolder(m_page1.getServerSourceFolder(), null /* validation is done in the operation */, "LookupCall test");
      if (testSourceFolder != null) {
        testProject = testSourceFolder.getJavaProject();
        op.setTestSourceFolder(input.environment().toScoutSourceFolder(testSourceFolder));
      }
      else {
        testProject = null;
      }
    }
    else {
      testProject = null;
    }

    op.setServerSession(S2eUtils.getSession(testProject, ScoutTier.Server, input.progress().monitor())
        .map(IType::getFullyQualifiedName)
        .orElse(null));
  }

  public static Class<? extends LookupCallNewWizardPage> getPage1Class() {
    return pageClass;
  }

  public static void setPage1Class(Class<? extends LookupCallNewWizardPage> page1Class) {
    pageClass = page1Class;
  }
}
