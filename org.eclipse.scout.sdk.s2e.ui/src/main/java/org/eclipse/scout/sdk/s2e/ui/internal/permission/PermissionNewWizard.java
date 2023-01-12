/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.permission;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.core.s.permission.PermissionNewOperation;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.ui.wizard.WizardFinishTask;
import org.eclipse.scout.sdk.s2e.ui.wizard.WizardFinishTask.PageToOperationMappingInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link PermissionNewWizard}</h3>
 *
 * @since 5.2.0
 */
public class PermissionNewWizard extends AbstractWizard implements INewWizard {

  private static volatile Class<? extends PermissionNewWizardPage> pageClass = PermissionNewWizardPage.class;

  private PermissionNewWizardPage m_page1;
  private WizardFinishTask<PermissionNewOperation> m_finishTask;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_page1 = initNewClassWizardWithPage(getPage1Class(), S2eUiUtils.getSharedPackageOfSelection(selection));
    m_finishTask = new WizardFinishTask<>(workbench.getDisplay());
    m_finishTask
        .withOperation(PermissionNewOperation::new)
        .withMapper(this::mapPageToOperation)
        .withUiAction((op, d) -> d.asyncExec(() -> S2eUiUtils.openInEditor(op.getCreatedPermission(), false)));
  }

  @Override
  public WizardFinishTask<PermissionNewOperation> getFinishTask() {
    return m_finishTask;
  }

  protected void mapPageToOperation(PageToOperationMappingInput input, PermissionNewOperation op) {
    op.setPackage(m_page1.getTargetPackage());
    op.setPermissionName(m_page1.getIcuName());
    op.setSharedSourceFolder(input.environment().toScoutSourceFolder(m_page1.getSourceFolder()));
    op.setSuperType(m_page1.getSuperType().getFullyQualifiedName());
  }

  public static Class<? extends PermissionNewWizardPage> getPage1Class() {
    return pageClass;
  }

  public static void setPage1Class(Class<? extends PermissionNewWizardPage> page1Class) {
    pageClass = page1Class;
  }
}
