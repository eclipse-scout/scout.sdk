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
package org.eclipse.scout.sdk.s2e.ui.internal.code;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.core.s.codetype.CodeTypeNewOperation;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.ui.wizard.WizardFinishTask;
import org.eclipse.scout.sdk.s2e.ui.wizard.WizardFinishTask.PageToOperationMappingInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link CodeTypeNewWizard}</h3>
 *
 * @since 5.2.0
 */
public class CodeTypeNewWizard extends AbstractWizard implements INewWizard {

  private static volatile Class<? extends CodeTypeNewWizardPage> pageClass = CodeTypeNewWizardPage.class;

  private CodeTypeNewWizardPage m_page1;
  private WizardFinishTask<CodeTypeNewOperation> m_finishTask;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_page1 = initNewClassWizardWithPage(getPage1Class(), S2eUiUtils.getSharedPackageOfSelection(selection));
    m_finishTask = new WizardFinishTask<>(workbench.getDisplay());
    m_finishTask
        .withOperation(CodeTypeNewOperation::new)
        .withMapper(this::mapPageToOperation)
        .withUiAction((op, d) -> d.asyncExec(() -> S2eUiUtils.openInEditor(op.getCreatedCodeType(), false)));
  }

  @SuppressWarnings("resource")
  protected void mapPageToOperation(PageToOperationMappingInput input, CodeTypeNewOperation op) {
    op.setCodeTypeName(m_page1.getIcuName());
    op.setPackage(m_page1.getTargetPackage());
    op.setSharedSourceFolder(input.environment().toScoutSourceFolder(m_page1.getSourceFolder()));
    op.setCodeTypeIdDataType(m_page1.getCodeTypeIdDataType(input.environment()));
    op.setSuperType(m_page1.getSuperClassReference(input.environment()));
  }

  @Override
  public WizardFinishTask<CodeTypeNewOperation> getFinishTask() {
    return m_finishTask;
  }

  public static Class<? extends CodeTypeNewWizardPage> getPage1Class() {
    return pageClass;
  }

  public static void setPage1Class(Class<? extends CodeTypeNewWizardPage> page1Class) {
    pageClass = page1Class;
  }
}
