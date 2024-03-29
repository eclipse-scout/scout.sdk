/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.proposal;

import static org.eclipse.scout.sdk.core.util.Strings.escapeHtml;
import static org.eclipse.scout.sdk.core.util.Strings.replaceEach;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.swt.graphics.Image;

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
    var allTranslations = m_translation.texts();
    if (allTranslations.isEmpty()) {
      return null;
    }

    var b = new StringBuilder();
    for (var e : allTranslations.entrySet()) {
      //noinspection HardcodedLineSeparator
      var text = replaceEach(escapeHtml(e.getValue()), new String[]{"\n", "\r"}, new String[]{"<br>", ""});
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
        var keyRange = findKeyRange(document, offset);
        if (keyRange == null) {
          return false;
        }

        var prefix = document.get(keyRange.x, offset - keyRange.x);
        return Strings.startsWith(m_translation.key(), prefix, false);
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
