/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

class LinkedPositionProposalImpl implements ICompletionProposalExtension2, IJavaCompletionProposal {

  private final LinkedProposalPositionGroup.Proposal m_proposal;
  private final LinkedModeModel m_linkedPositionModel;

  LinkedPositionProposalImpl(LinkedProposalPositionGroup.Proposal proposal, LinkedModeModel model) {
    m_proposal = proposal;
    m_linkedPositionModel = model;
  }

  @Override
  public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
    IDocument doc = viewer.getDocument();
    LinkedPosition position = m_linkedPositionModel.findPosition(new LinkedPosition(doc, offset, 0));
    if (position == null) {
      return;
    }

    try {
      TextEdit edit = m_proposal.computeEdits(offset, position, trigger, stateMask, m_linkedPositionModel);
      if (edit != null) {
        edit.apply(position.getDocument(), 0);
      }
    }
    catch (MalformedTreeException | BadLocationException | CoreException e) {
      SdkLog.info("Unable to apply text edit.", e);
    }
  }

  @Override
  public String getDisplayString() {
    return m_proposal.getDisplayString();
  }

  @Override
  public Image getImage() {
    return m_proposal.getImage();
  }

  @Override
  public int getRelevance() {
    return m_proposal.getRelevance();
  }

  @Override
  public void apply(IDocument document) {
    // not called
  }

  @Override
  public String getAdditionalProposalInfo() {
    return m_proposal.getAdditionalProposalInfo();
  }

  @Override
  public Point getSelection(IDocument document) {
    return null;
  }

  @Override
  public IContextInformation getContextInformation() {
    return null;
  }

  @Override
  public void selected(ITextViewer viewer, boolean smartToggle) {
    // nop
  }

  @Override
  public void unselected(ITextViewer viewer) {
    // nop
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#validate(org.eclipse.jface.text.IDocument, int, org.eclipse.jface.text.DocumentEvent)
   */
  @Override
  public boolean validate(IDocument document, int offset, DocumentEvent event) {
    // ignore event
    String insert = getDisplayString();

    int off;
    LinkedPosition pos = m_linkedPositionModel.findPosition(new LinkedPosition(document, offset, 0));
    if (pos != null) {
      off = pos.getOffset();
    }
    else {
      off = Math.max(0, offset - insert.length());
    }
    int length = offset - off;

    if (offset <= document.getLength()) {
      try {
        String content = document.get(off, length);
        if (insert.startsWith(content)) {
          return true;
        }
      }
      catch (BadLocationException e) {
        JavaPlugin.log(e);
        // and ignore and return false
      }
    }
    return false;
  }
}
