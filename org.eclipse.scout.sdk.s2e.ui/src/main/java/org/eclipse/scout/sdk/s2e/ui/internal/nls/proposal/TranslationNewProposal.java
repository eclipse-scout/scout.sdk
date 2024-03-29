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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.scout.sdk.core.java.JavaUtils;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.action.TranslationNewAction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * <h3>{@link TranslationNewProposal}</h3>
 *
 * @since 3.10.0 2013-10-23
 */
public class TranslationNewProposal extends AbstractTranslationProposal {

  private final Image m_image;
  private final TranslationManager m_manager;

  public TranslationNewProposal(TranslationManager manager, String prefix, int initialOffset) {
    super(prefix, initialOffset);
    m_manager = manager;
    m_image = S2ESdkUiActivator.getImage(ISdkIcons.TextAdd);
  }

  @Override
  public int getRelevance() {
    return 0;
  }

  @Override
  public void apply(IDocument document, char trigger, int offset) {
    String searchText = null;
    var initialOffset = getInitialOffset();
    try {
      searchText = document.get(initialOffset - getPrefix().length(), getPrefix().length() + offset - initialOffset);
    }
    catch (BadLocationException e1) {
      SdkLog.warning(e1);
    }

    String proposalFieldText;
    if (Strings.isBlank(searchText)) {
      proposalFieldText = "";
    }
    else {
      proposalFieldText = JavaUtils.fromStringLiteral('"' + searchText + '"').toString();
    }

    var key = m_manager.generateNewKey(proposalFieldText);
    var entry = new Translation(key);
    entry.putText(Language.LANGUAGE_DEFAULT, proposalFieldText);

    var action = new TranslationNewAction(Display.getDefault().getActiveShell(), m_manager, entry);
    action.run();
    action.getCreatedTranslation()
        .ifPresent(createdEntry -> {
          try {
            replaceWith(document, offset, createdEntry.key());
          }
          catch (BadLocationException e) {
            SdkLog.warning(e);
          }
        });
  }

  @Override
  public String getDisplayString() {
    return "New text...";
  }

  @Override
  public Image getImage() {
    return m_image;
  }
}
