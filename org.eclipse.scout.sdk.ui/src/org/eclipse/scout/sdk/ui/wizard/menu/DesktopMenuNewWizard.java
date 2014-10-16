/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.wizard.menu;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.MenuNewOperation;
import org.eclipse.scout.sdk.ui.extensions.AbstractInnerTypeWizard;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>DesktopMenuNewWizard</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 30.06.2010
 */
public class DesktopMenuNewWizard extends AbstractInnerTypeWizard {

  private MenuNewWizardPage m_page1;
  private MenuNewOperation m_operation;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("NewDesktopMenu"));

    m_page1 = new MenuNewWizardPage(getDeclaringType());
    if (getSiblingProposal() != null) {
      m_page1.setSibling(getSiblingProposal());
    }
    if (getTypeName() != null) {
      m_page1.setTypeName(getTypeName());
    }
    if (getSuperType() != null) {
      m_page1.setSuperType(getSuperType());
    }
    addPage(m_page1);
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // create menu
    m_operation = new MenuNewOperation(m_page1.getTypeName(), getDeclaringType(), true);

    // write back members
    m_operation.setNlsEntry(m_page1.getNlsName());
    IType superTypeProp = m_page1.getSuperType();
    if (superTypeProp != null) {
      String signature = SignatureCache.createTypeSignature(superTypeProp.getFullyQualifiedName());
      m_operation.setSuperTypeSignature(signature);
    }
    if (m_page1.getSibling() == SiblingProposal.SIBLING_END || m_page1.getSibling() == null) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredDesktop(getDeclaringType());
      m_operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_MENU));
    }
    else {
      m_operation.setSibling(m_page1.getSibling().getElement());
    }
    m_operation.setFormToOpen(m_page1.getFormToOpen());
    m_operation.setFormHandler(m_page1.getHandler());
    m_operation.run(monitor, workingCopyManager);
    return true;
  }

  @Override
  protected void postFinishDisplayThread() {
    ScoutSdkUi.showJavaElementInEditor(m_operation.getCreatedMenu(), false);
  }
}
