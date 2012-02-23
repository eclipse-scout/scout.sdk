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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.MenuNewOperation;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>DesktopMenuNewWizard</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 30.06.2010
 */
public class DesktopMenuNewWizard extends AbstractWorkspaceWizard {

  private MenuNewWizardPage m_page1;
  private IType m_declaringType;
  private MenuNewOperation m_operation;

  public DesktopMenuNewWizard() {
    setWindowTitle(Texts.get("NewDesktopMenu"));
  }

  public void initWizard(IType declaringType) {
    m_declaringType = declaringType;
    m_page1 = new MenuNewWizardPage(getDeclaringType());
    addPage(m_page1);
  }

  public void setSuperType(IType superType) {
    m_page1.setSuperType(superType);
  }

  /**
   * @return the declaringType
   */
  public IType getDeclaringType() {
    return m_declaringType;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // create menu
    m_operation = new MenuNewOperation(getDeclaringType(), true);
    // write back members
    m_operation.setNlsEntry(m_page1.getNlsName());
    m_operation.setTypeName(m_page1.getTypeName());
    IType superTypeProp = m_page1.getSuperType();
    if (superTypeProp != null) {
      String signature = Signature.createTypeSignature(superTypeProp.getFullyQualifiedName(), true);
      m_operation.setSuperTypeSignature(signature);
    }
    if (m_page1.getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredDesktop(m_declaringType);
      m_operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_MENU));
    }
    else {
      m_operation.setSibling(m_page1.getSibling().getElement());
    }
    m_operation.setFormToOpen(m_page1.getFormToOpen());
    m_operation.run(monitor, workingCopyManager);
    return true;
  }

  @Override
  protected void postFinishDisplayThread() {
    IType createdField = m_operation.getCreatedMenu();
    if (TypeUtility.exists(createdField)) {
      ScoutSdkUi.showJavaElementInEditor(createdField, false);
    }
  }
}
