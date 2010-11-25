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
package org.eclipse.scout.sdk.ui.internal.extensions.codecompletion;

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
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <h3>SqlBindProposal</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public class SqlBindProposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension4 {

  private final String m_prefix;
  private final int m_offset;
  private final Image m_image;
  private final String m_elementName;

  public SqlBindProposal(String elementName, String prefix, int offset, Image image) {
    m_elementName = elementName;
    m_prefix = prefix;
    m_offset = offset;
    m_image = image;
  }

  public void apply(IDocument document) {
    apply(null, '\0', 0, m_offset);
  }

  public Point getSelection(IDocument document) {
    return new Point(m_offset - m_prefix.length() + getElementName().length(), 0);
  }

  public String getAdditionalProposalInfo() {

    return null;
  }

  public String getDisplayString() {
    return getCompletionText();
  }

  public Image getImage() {
    return m_image;
  }

  public IContextInformation getContextInformation() {
    return null;
  }

  public void apply(IDocument document, char trigger, int offset) {
    int offDiff = offset - m_offset;
    ReplaceEdit replaceEdit = new ReplaceEdit(m_offset - m_prefix.length(), m_prefix.length() + offDiff, getCompletionText());
    try {
      replaceEdit.apply(document);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("could not apply proposal.", e);
    }
  }

  public boolean isValidFor(IDocument document, int offset) {
    return validate(document, offset, null);
  }

  public char[] getTriggerCharacters() {
    return null;
  }

  public int getContextInformationPosition() {
    return 0;
  }

  public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
    apply(viewer.getDocument(), trigger, offset);
  }

  public void selected(ITextViewer viewer, boolean smartToggle) {
  }

  public void unselected(ITextViewer viewer) {
  }

  public boolean validate(IDocument document, int offset, DocumentEvent event) {
    try {
      String prefix = m_prefix + document.get(m_offset, offset - m_offset);
      return getElementName().toLowerCase().startsWith(prefix.toLowerCase());
    }
    catch (BadLocationException e) {
      ScoutSdkUi.logWarning("could not validate proposal.", e);
      return false;
    }
  }

  public IInformationControlCreator getInformationControlCreator() {
    return null;
  }

  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    return getCompletionText();
  }

  private String getCompletionText() {
    String simpleName = getElementName();
    if (simpleName.length() > 1) {
      simpleName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
    return simpleName;

  }

  /**
   * @return the elementName
   */
  public String getElementName() {
    return m_elementName;
  }

  public int getPrefixCompletionStart(IDocument document, int completionOffset) {
    return m_offset - m_prefix.length();
  }

  public boolean isAutoInsertable() {
    return true;
  }

}
