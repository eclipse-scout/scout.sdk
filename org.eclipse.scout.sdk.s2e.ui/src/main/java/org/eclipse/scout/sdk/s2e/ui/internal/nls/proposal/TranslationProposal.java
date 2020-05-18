/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.proposal;

import static org.eclipse.scout.sdk.core.util.Strings.escapeHtml;
import static org.eclipse.scout.sdk.core.util.Strings.replaceEach;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * <h4>NlsProposal</h4>
 *
 * @since 1.1.0 (12.01.2011)
 */
public class TranslationProposal extends AbstractTranslationProposal {

  private final ITranslation m_translation;
  private final Image m_image;

  public TranslationProposal(ITranslation nlsEntry, String prefix, int initialOffset) {
    super(prefix, initialOffset);
    m_translation = nlsEntry;
    m_image = S2ESdkUiActivator.getImage(ISdkIcons.Comment);
  }

  @Override
  public int getRelevance() {
    return 1;
  }

  @Override
  public String getAdditionalProposalInfo() {
    Map<Language, String> allTranslations = m_translation.texts();
    if (allTranslations.isEmpty()) {
      return null;
    }

    StringBuilder b = new StringBuilder();
    for (Entry<Language, String> e : allTranslations.entrySet()) {
      //noinspection HardcodedLineSeparator
      CharSequence text = replaceEach(escapeHtml(e.getValue()), new String[]{"\n", "\r"}, new String[]{"<br>", ""});
      b.append("<b>").append(text).append("</b> [").append(escapeHtml(e.getKey().displayName())).append("]<br>");
    }
    return b.toString();
  }

  @Override
  public String getDisplayString() {
    return m_translation.key();
  }

  @Override
  public Image getImage() {
    return m_image;
  }

  @Override
  public void apply(IDocument document, char trigger, int offset) {
    try {
      replaceWith(document, offset, m_translation.key());
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
        if (keyRange == null) {
          return false;
        }

        String prefix = document.get(keyRange.x, offset - keyRange.x);
        return m_translation.key().toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
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
    return m_translation.key();
  }

  @Override
  public boolean isAutoInsertable() {
    return true;
  }
}
