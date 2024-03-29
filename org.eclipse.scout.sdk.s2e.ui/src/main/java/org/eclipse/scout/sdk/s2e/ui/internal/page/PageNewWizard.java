/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.page;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.page.PageNewOperation;
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
 * <h3>{@link PageNewWizard}</h3>
 *
 * @since 5.2.0
 */
public class PageNewWizard extends AbstractWizard implements INewWizard {

  private static volatile Class<? extends PageNewWizardPage> pageClass = PageNewWizardPage.class;

  private PageNewWizardPage m_page1;
  private WizardFinishTask<PageNewOperation> m_finishTask;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_page1 = initNewClassWizardWithPage(getPage1Class(), S2eUiUtils.getClientPackageOfSelection(selection));
    m_finishTask = new WizardFinishTask<>(workbench.getDisplay());
    m_finishTask
        .withOperation(PageNewOperation::new)
        .withMapper(this::mapPageToOperation)
        .withUiAction((op, d) -> {
          var type = op.getCreatedPage().result();
          d.asyncExec(() -> S2eUiUtils.openInEditor(type, false));
        });
  }

  @Override
  public WizardFinishTask<PageNewOperation> getFinishTask() {
    return m_finishTask;
  }

  protected void mapPageToOperation(PageToOperationMappingInput input, PageNewOperation op) {
    op.setCreateAbstractPage(m_page1.isCreateAbstractPage());
    op.setClientSourceFolder(input.environment().toScoutSourceFolder(m_page1.getSourceFolder()));
    op.setPackage(m_page1.getTargetPackage());
    op.setPageName(m_page1.getIcuName());
    var selectedSharedFolder = m_page1.getSharedSourceFolder();
    if (JdtUtils.exists(selectedSharedFolder)) {
      op.setSharedSourceFolder(input.environment().toScoutSourceFolder(selectedSharedFolder));

      var dtoSourceFolder = S2eUtils.getDtoSourceFolder(selectedSharedFolder);
      op.setPageDataSourceFolder(input.environment().toScoutSourceFolder(dtoSourceFolder));
    }
    var selectedServerFolder = m_page1.getServerSourceFolder();
    IJavaProject serverProject;
    if (JdtUtils.exists(selectedServerFolder)) {
      serverProject = selectedServerFolder.getJavaProject();
      var serverSourceFolder = input.environment().toScoutSourceFolder(selectedServerFolder);
      op.setServerSourceFolder(serverSourceFolder);
      var scoutApi = serverSourceFolder.javaEnvironment().requireApi(IScoutApi.class);
      if (op.getTestSourceFolder() == null) {
        var serviceTestSourceFolder = S2eUiUtils.getTestSourceFolder(selectedServerFolder, scoutApi.ServerTestRunner().fqn(), "service test");
        if (serviceTestSourceFolder != null) {
          op.setTestSourceFolder(input.environment().toScoutSourceFolder(serviceTestSourceFolder));
        }
      }
    }
    else {
      serverProject = null;
    }
    op.setSuperType(m_page1.getSuperType().getFullyQualifiedName());
    op.setServerSession(
        S2eUtils.getSession(serverProject, ScoutTier.Server, input.progress().monitor())
            .map(IType::getFullyQualifiedName)
            .orElse(null));
  }

  public static Class<? extends PageNewWizardPage> getPage1Class() {
    return pageClass;
  }

  public static void setPage1Class(Class<? extends PageNewWizardPage> page1Class) {
    pageClass = page1Class;
  }
}
