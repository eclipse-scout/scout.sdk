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
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * <h4>NlsProposal</h4>
 * 
 * @author Andreas Hoegger
 * @since 1.1.0 (12.01.2011)
 */
public class NlsProposal extends AbstractNlsProposal {
  private final INlsEntry m_nlsEntry;

  private final Image m_image;

  public NlsProposal(INlsEntry nlsEntry, String prefix, int initialOffset, Image image) {
    super(prefix, initialOffset);
    m_nlsEntry = nlsEntry;
    m_image = image;
  }

  @Override
  public int getRelevance() {
    return 1;
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
    try {
      replaceWith(document, offset, m_nlsEntry.getKey());
    }
    catch (BadLocationException e) {
      NlsCore.logError(e);
    }
  }

  @Override
  public boolean validate(IDocument document, int offset, DocumentEvent event) {
    if (super.validate(document, offset, event)) {
      try {
        Point keyRange = findKeyRange(document, offset);

        String prefix = document.get(keyRange.x, offset - keyRange.x);
        return m_nlsEntry.getKey().toLowerCase().startsWith(prefix.toLowerCase());
      }
      catch (BadLocationException e) {
        NlsCore.logWarning(e);
        return false;
      }
    }
    else {
      return false;
    }
  }

  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    return m_nlsEntry.getKey();
  }

  @Override
  public boolean isAutoInsertable() {
    return true;
  }

}
