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
package org.eclipse.scout.nls.sdk.internal.ui.action;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <h4>NlsProposal</h4>
 * 
 * @author Andreas Hoegger
 * @since 1.1.0 (12.01.2011)
 */
public class NlsProposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension4 {
  private final INlsEntry m_nlsEntry;
  private final String m_prefix;
  private final int m_offset;
  private final Image m_image;

  public NlsProposal(INlsEntry nlsEntry, String prefix, int offset, Image image) {
    m_nlsEntry = nlsEntry;
    m_prefix = prefix;
    m_offset = offset;
    m_image = image;
  }

  @Override
  public void apply(IDocument document) {
    apply(document, '\0', m_offset);
  }

  @Override
  public Point getSelection(IDocument document) {
    return new Point(m_offset - m_prefix.length() + m_nlsEntry.getKey().length(), 0);
  }

  @Override
  public String getAdditionalProposalInfo() {
    // html
    Map<Language, String> allTranslations = m_nlsEntry.getAllTranslations();
    if (allTranslations != null && allTranslations.size() > 0) {
      StringBuilder b = new StringBuilder();
      for (Entry<Language, String> e : allTranslations.entrySet()) {
        b.append("'<b>" + e.getValue().replace("\n", "<br>") + "</b>' [" + e.getKey().getDispalyName() + "]<br>");
      }
      return b.toString();
    }
    else {
      return null;
    }

  }

  @Override
  public String getDisplayString() {
    return m_nlsEntry.getKey();
  }

  @Override
  public Image getImage() {
    return m_image;
  }

  @Override
  public IContextInformation getContextInformation() {
    return null;
  }

  @Override
  public void apply(IDocument document, char trigger, int offset) {
    int offDiff = offset - m_offset;
    ReplaceEdit replaceEdit = new ReplaceEdit(m_offset - m_prefix.length(), m_prefix.length() + offDiff, m_nlsEntry.getKey());
    try {
      replaceEdit.apply(document);
    }
    catch (Exception e) {
      NlsCore.logWarning(e);
    }
  }

  @Override
  public boolean isValidFor(IDocument document, int offset) {
    return validate(document, offset, null);
  }

  @Override
  public char[] getTriggerCharacters() {
    return null;
  }

  @Override
  public int getContextInformationPosition() {
    return 0;
  }

  @Override
  public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
    apply(viewer.getDocument(), trigger, offset);
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
      String prefix = m_prefix + document.get(m_offset, offset - m_offset);
      return m_nlsEntry.getKey().toLowerCase().startsWith(prefix.toLowerCase());
    }
    catch (BadLocationException e) {
      NlsCore.logWarning(e);
      return false;
    }
  }

  @Override
  public IInformationControlCreator getInformationControlCreator() {
    return null;
  }

  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    return m_nlsEntry.getKey();
  }

  @Override
  public int getPrefixCompletionStart(IDocument document, int completionOffset) {
    return m_offset - m_prefix.length();
  }

  @Override
  public boolean isAutoInsertable() {
    return true;
  }

}
