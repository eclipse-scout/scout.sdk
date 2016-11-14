/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.ui.proposal;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
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
    Map<Language, String> allTranslations = m_nlsEntry.getAllTranslations();
    if (allTranslations == null || allTranslations.isEmpty()) {
      return null;
    }

    StringBuilder b = new StringBuilder();
    for (Entry<Language, String> e : allTranslations.entrySet()) {
      String text = e.getValue();
      if (text != null) {
        text = StringUtils.replaceEach(CoreUtils.escapeHtml(text), new String[]{
            "\n", "\r"
        }, new String[]{
            "<br>", ""
        });

        b.append("<b>").append(text).append("</b> [").append(e.getKey().getDispalyName()).append("]<br>");
      }
    }
    return b.toString();
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
      SdkLog.error(e);
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
        SdkLog.warning(e);
        return false;
      }
    }
    return false;
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