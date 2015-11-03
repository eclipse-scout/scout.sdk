/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.ui.proposal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.INlsIcons;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.action.NlsEntryNewAction;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
import org.eclipse.scout.sdk.s2e.nls.model.NlsEntry;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link NewNlsProposal}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 23.10.2013
 */
public class NewNlsProposal extends AbstractNlsProposal {
  private final Image m_image = NlsCore.getImage(INlsIcons.TEXT_ADD);
  private INlsEntry m_nlsEntry;
  private INlsProject m_project;

  public NewNlsProposal(INlsProject project, Shell shell, String prefix, int initialOffset) {
    super(prefix, initialOffset);
    m_project = project;
  }

  @Override
  public int getRelevance() {
    return 0;
  }

  @Override
  public boolean validate(IDocument document, int offset, DocumentEvent event) {
    try {
      Point keyRange = findKeyRange(document, offset);
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
    String proposalFieldText = "";

    if (StringUtils.isNotEmpty(searchText)) {
      proposalFieldText = CoreUtils.fromStringLiteral("\"" + searchText + "\"");
    }
    String key = m_project.generateNewKey(proposalFieldText);
    NlsEntry entry = new NlsEntry(key, m_project);
    Language devLang = m_project.getDevelopmentLanguage();
    entry.addTranslation(devLang, proposalFieldText);
    if (!Language.LANGUAGE_DEFAULT.equals(devLang)) {
      entry.addTranslation(Language.LANGUAGE_DEFAULT, proposalFieldText);
    }
    NlsEntryNewAction action = new NlsEntryNewAction(Display.getDefault().getActiveShell(), m_project, entry, true);
    action.run();
    try {
      action.join();
    }
    catch (InterruptedException e) {
      SdkLog.warning(e);
    }

    m_nlsEntry = action.getEntry();
    if (m_nlsEntry != null) {
      try {
        replaceWith(document, offset, m_nlsEntry.getKey());
      }
      catch (BadLocationException e) {
        SdkLog.error(e);
      }
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
  public String getAdditionalProposalInfo() {
    return null;
  }

  @Override
  public String getDisplayString() {
    return "New text...";
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
  public boolean isAutoInsertable() {
    return false;
  }

}
