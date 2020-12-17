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
package org.eclipse.scout.sdk.s2e.ui.internal.nls.proposal;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link AbstractTranslationProposal}</h3>
 *
 * @since 3.10.0 2013-10-24
 */
public abstract class AbstractTranslationProposal implements IJavaCompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension4 {

  private final String m_searchText;
  private final int m_initialOffset;
  private Point m_selection;

  protected AbstractTranslationProposal(String searchText, int initialOffset) {
    m_searchText = searchText;
    m_initialOffset = initialOffset;
  }

  public int getInitialOffset() {
    return m_initialOffset;
  }

  public String getPrefix() {
    return m_searchText;
  }

  @Override
  public int getRelevance() {
    return -1;
  }

  @Override
  public IInformationControlCreator getInformationControlCreator() {
    return null;
  }

  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    return null;
  }

  @Override
  public int getPrefixCompletionStart(IDocument document, int completionOffset) {
    return 0;
  }

  @Override
  public void selected(ITextViewer viewer, boolean smartToggle) {
  }

  @Override
  public void unselected(ITextViewer viewer) {
  }

  @Override
  public boolean validate(IDocument document, int offset, DocumentEvent event) {
    try {
      var keyRange = findKeyRange(document, offset);
      if (keyRange != null) {
        return keyRange.x < offset && keyRange.y >= offset;
      }
    }
    catch (BadLocationException e) {
      SdkLog.error(e);
    }
    return false;
  }

  @Override
  public void apply(IDocument document) {
  }

  @Override
  public void apply(IDocument document, char trigger, int offset) {
  }

  @Override
  public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
    apply(viewer.getDocument(), trigger, offset);
  }

  protected static Point findKeyRange(IDocument document, int offset) throws BadLocationException {
    var lineRange = document.getLineInformationOfOffset(offset);
    // find start
    var startOffset = -1;
    var index = offset - 1;
    while (index > 0 && index > lineRange.getOffset()) {
      if (document.getChar(index) == '"') {
        if (index > 1) {
          if (document.getChar(index - 1) != '\\') {
            startOffset = index + 1;
            break;
          }
        }
        else {
          startOffset = index + 1;
          break;
        }
      }
      index--;
    }

    // find end
    var endOffset = -1;
    index = offset;
    var masked = false;
    while ((document.getLength() > index && index < (lineRange.getOffset() + lineRange.getLength()))) {
      if (masked) {
        masked = false; // the current character is masked -> ignore
      }
      else if (document.getChar(index) == '\\') {
        masked = true; // the next character is masked and must therefore be ignored
      }
      else if (document.getChar(index) == '"') {
        endOffset = index;
        break;
      }
      index++;
    }

    if (startOffset > -1) {
      if (endOffset < 0) {
        // no end found: use the line end
        endOffset = lineRange.getOffset() + lineRange.getLength();
      }
      return new Point(startOffset, endOffset);
    }
    return null;
  }

  protected void replaceWith(IDocument document, int offset, String replacement) throws BadLocationException {
    var keyRange = findKeyRange(document, offset);
    if (keyRange != null) {
      m_selection = new Point(keyRange.x, replacement.length());
      TextEdit replaceEdit = new ReplaceEdit(keyRange.x, keyRange.y - keyRange.x, replacement);
      try {
        replaceEdit.apply(document);
      }
      catch (BadLocationException | MalformedTreeException e) {
        SdkLog.warning(e);
      }
    }
  }

  @Override
  public boolean isValidFor(IDocument document, int offset) {
    return validate(document, offset, null);
  }

  @Override
  @SuppressWarnings("squid:S1168") // should return null as by javadoc
  public char[] getTriggerCharacters() {
    return null;
  }

  @Override
  public int getContextInformationPosition() {
    return 0;
  }

  @Override
  public Point getSelection(IDocument document) {
    return m_selection;
  }

  @Override
  public String getAdditionalProposalInfo() {
    return null;
  }

  @Override
  public IContextInformation getContextInformation() {
    return null;
  }

  @Override
  public boolean isAutoInsertable() {
    return false;
  }
}
