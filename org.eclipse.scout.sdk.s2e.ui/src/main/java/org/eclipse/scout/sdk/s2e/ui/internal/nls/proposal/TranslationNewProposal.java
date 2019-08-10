/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.proposal;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
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
  private final TranslationStoreStack m_stack;

  public TranslationNewProposal(TranslationStoreStack project, String prefix, int initialOffset) {
    super(prefix, initialOffset);
    m_stack = project;
    m_image = S2ESdkUiActivator.getImage(ISdkIcons.TextAdd);
  }

  @Override
  public int getRelevance() {
    return 0;
  }

  @Override
  public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
    apply(viewer.getDocument(), trigger, offset);
  }

  @Override
  public void apply(IDocument document, char trigger, int offset) {
    String searchText = null;
    int initalOffset = getInitialOffset();
    try {
      searchText = document.get(initalOffset - getPrefix().length(), getPrefix().length() + offset - initalOffset);
    }
    catch (BadLocationException e1) {
      SdkLog.warning(e1);
    }

    String proposalFieldText;
    if (Strings.isBlank(searchText)) {
      proposalFieldText = "";
    }
    else {
      proposalFieldText = Strings.fromStringLiteral('"' + searchText + '"');
    }

    String key = m_stack.generateNewKey(proposalFieldText);
    Translation entry = new Translation(key);
    entry.putTranslation(Language.LANGUAGE_DEFAULT, proposalFieldText);

    TranslationNewAction action = new TranslationNewAction(Display.getDefault().getActiveShell(), m_stack, entry);
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
