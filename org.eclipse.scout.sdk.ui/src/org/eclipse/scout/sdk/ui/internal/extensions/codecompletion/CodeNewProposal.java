/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.extensions.codecompletion;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.SdkIcons;
import org.eclipse.scout.sdk.ui.operation.sourceedit.SourceEditOperation;
import org.eclipse.scout.sdk.ui.wizard.IWorkspaceWizard;
import org.eclipse.scout.sdk.ui.wizard.ScoutWizardDialog;
import org.eclipse.scout.sdk.ui.wizard.code.CodeNewWizard;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <h3>{@link CodeNewProposal}</h3>
 * 
 *  @author Andreas Hoegger
 * @since 3.10.0 25.10.2013
 */
public class CodeNewProposal extends AbstractSdkProposal {

  private IType m_declaringType;

  CodeNewProposal(IType declaringType) {
    m_declaringType = declaringType;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public String getDisplayString() {
    return Texts.get("Action_newTypeX", "Code");
  }

  @Override
  public Image getImage() {
    return ScoutSdkUi.getImage(SdkIcons.CodeAdd);
  }

  @Override
  public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
    CodeNewWizard wizard = new CodeNewWizard();

    String codeName = null;
    try {
      IDocument document = viewer.getDocument();
      Point wordRange = findTriggerWordRange(document, offset);
      if (wordRange != null) {
        codeName = document.get(wordRange.x, wordRange.y - wordRange.x);
        // remove proposal text
        SourceEditOperation removeProposalTextOp = new SourceEditOperation(new ReplaceEdit(wordRange.x, wordRange.y - wordRange.x, ""), viewer.getDocument(), ScoutSdkUi.getDisplay());
        wizard.addAdditionalPerformFinishOperation(removeProposalTextOp, IWorkspaceWizard.ORDER_BEFORE_WIZARD);
      }
    }
    catch (BadLocationException e) {
      ScoutSdkUi.logWarning(e);
    }

    wizard.initWizard(getDeclaringType());
    if (codeName != null) {
      wizard.getCodeNewWizardPage().setTypeName(codeName.trim());
    }
    // sibling check
    try {
      IJavaElement sibling = findSibling(getDeclaringType(), offset, TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.ICode), TypeUtility.getLocalTypeHierarchy(getDeclaringType())));
      if (sibling != null) {
        wizard.getCodeNewWizardPage().setSibling(new SiblingProposal(sibling));
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logWarning("could not find sibling.", e);
    }

    ScoutWizardDialog wizardDialog = new ScoutWizardDialog(wizard);
    wizardDialog.open();
  }

  @Override
  public void selected(ITextViewer viewer, boolean smartToggle) {
  }

  @Override
  public boolean validate(IDocument document, int offset, DocumentEvent event) {
    return true;
  }

}
