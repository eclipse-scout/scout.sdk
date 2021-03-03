/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.form;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.form.FormNewOperation;
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
 * <h3>{@link FormNewWizard}</h3>
 *
 * @since 5.2.0
 */
public class FormNewWizard extends AbstractWizard implements INewWizard {

  private static volatile Class<? extends FormNewWizardPage> pageClass = FormNewWizardPage.class;

  private FormNewWizardPage m_page1;
  private WizardFinishTask<FormNewOperation> m_finishTask;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_page1 = initNewClassWizardWithPage(getPage1Class(), S2eUiUtils.getClientPackageOfSelection(selection));
    m_finishTask = new WizardFinishTask<>(workbench.getDisplay());
    m_finishTask
        .withOperation(FormNewOperation::new)
        .withMapper(this::mapPageToOperation)
        .withUiAction((op, d) -> {
          var type = op.getCreatedForm().result();
          d.asyncExec(() -> S2eUiUtils.openInEditor(type, false));
        });
  }

  @Override
  public WizardFinishTask<FormNewOperation> getFinishTask() {
    return m_finishTask;
  }

  protected void mapPageToOperation(PageToOperationMappingInput input, FormNewOperation op) {
    var page = m_page1;

    op.setClientPackage(page.getTargetPackage());
    var clientSourceFolder = input.environment().toScoutSourceFolder(page.getSourceFolder());
    var scoutApi = clientSourceFolder.javaEnvironment().requireApi(IScoutApi.class);
    op.setClientSourceFolder(clientSourceFolder);
    var formTestSourceFolder = S2eUiUtils.getTestSourceFolder(page.getSourceFolder(), scoutApi.ClientTestRunner().fqn(), "form test");
    if (JdtUtils.exists(formTestSourceFolder)) {
      op.setClientTestSourceFolder(input.environment().toScoutSourceFolder(formTestSourceFolder));
    }
    op.setCreateFormData(page.isCreateFormData());
    op.setCreatePermissions(page.isCreatePermissions());
    op.setCreateService(page.isCreateService());
    op.setFormName(page.getIcuName());

    IJavaProject serverProject;
    if (page.isCreateService()) {
      op.setServerSourceFolder(input.environment().toScoutSourceFolder(page.getServerSourceFolder()));
      serverProject = page.getServerSourceFolder().getJavaProject();
      var serviceTestSourceFolder = S2eUiUtils.getTestSourceFolder(page.getServerSourceFolder(), scoutApi.ServerTestRunner().fqn(), "service test");
      if (JdtUtils.exists(serviceTestSourceFolder)) {
        op.setServerTestSourceFolder(input.environment().toScoutSourceFolder(serviceTestSourceFolder));
      }
    }
    else {
      serverProject = null;
    }
    if (page.isCreateFormData()) {
      var sharedSourceFolder = S2eUtils.getDtoSourceFolder(page.getSharedSourceFolder());
      op.setFormDataSourceFolder(input.environment().toScoutSourceFolder(sharedSourceFolder));
    }
    if (page.isCreatePermissions() || page.isCreateService()) {
      op.setSharedSourceFolder(input.environment().toScoutSourceFolder(page.getSharedSourceFolder()));
    }
    op.setSuperType(page.getSuperType().getFullyQualifiedName());
    op.setServerSession(S2eUtils.getSession(serverProject, ScoutTier.Server, input.progress().monitor())
        .map(IType::getFullyQualifiedName)
        .orElse(null));
  }

  public static Class<? extends FormNewWizardPage> getPage1Class() {
    return pageClass;
  }

  public static void setPage1Class(Class<? extends FormNewWizardPage> page1Class) {
    pageClass = page1Class;
  }
}
