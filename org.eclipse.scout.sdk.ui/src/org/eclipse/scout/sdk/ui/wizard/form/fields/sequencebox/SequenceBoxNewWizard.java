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
package org.eclipse.scout.sdk.ui.wizard.form.fields.sequencebox;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.form.field.SequenceBoxNewOperation;
import org.eclipse.scout.sdk.ui.extensions.AbstractInnerTypeWizard;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.ui.IWorkbench;

public class SequenceBoxNewWizard extends AbstractInnerTypeWizard {

  private SequenceBoxNewWizardPage m_page1;
  private SequenceBoxTemplateWizardPage m_templatePage;
  private SequenceBoxNewOperation m_operation;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("NewSequenceBox"));

    m_page1 = new SequenceBoxNewWizardPage(getDeclaringType());
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
    m_templatePage = new SequenceBoxTemplateWizardPage(getDeclaringType());
    addPage(m_templatePage);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_operation = new SequenceBoxNewOperation(m_page1.getTypeName(), getDeclaringType(), true);
    // write back members
    if (m_page1.getNlsName() != null) {
      m_operation.setNlsEntry(m_page1.getNlsName());
    }
    if (m_page1.getSuperType() != null) {
      m_operation.setSuperTypeSignature(SignatureCache.createTypeSignature(m_page1.getSuperType().getFullyQualifiedName()));
    }
    if (m_page1.getSibling() == SiblingProposal.SIBLING_END || m_page1.getSibling() == null) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredCompositeField(getDeclaringType());
      m_operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_FORM_FIELD));
    }
    else {
      m_operation.setSibling(m_page1.getSibling().getElement());
    }

    m_operation.setContentTemplate(m_templatePage.getSelectedTemplate());
    try {
      m_operation.validate();
    }
    catch (IllegalArgumentException e) {
      ScoutSdkUi.logWarning(e.getMessage(), e);
      return false;
    }
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    try {
      m_operation.run(monitor, workingCopyManager);
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not create sequence box.", e);
      return false;
    }
    return true;
  }

  @Override
  protected void postFinishDisplayThread() {
    ScoutSdkUi.showJavaElementInEditor(m_operation.getCreatedField(), false);
  }
}
