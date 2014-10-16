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
package org.eclipse.scout.sdk.ui.extensions;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.executor.selection.ScoutStructuredSelection;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.ui.IWorkbench;

public abstract class AbstractInnerTypeWizard extends AbstractWorkspaceWizard {

  private IType m_declaringType;
  private IType m_superType;
  private SiblingProposal m_siblingProposal;
  private String m_typeName;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("NewFormField"));

    m_declaringType = UiUtility.getTypeFromSelection(selection);

    if (selection instanceof ScoutStructuredSelection) {
      ScoutStructuredSelection scoutStructuredSel = (ScoutStructuredSelection) selection;
      m_siblingProposal = scoutStructuredSel.getSibling();
      m_superType = scoutStructuredSel.getSuperType();
      m_typeName = scoutStructuredSel.getTypeName();
    }
  }

  public SiblingProposal getSiblingProposal() {
    return m_siblingProposal;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public IType getSuperType() {
    return m_superType;
  }

  public String getTypeName() {
    return m_typeName;
  }
}
