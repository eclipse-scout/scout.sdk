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
package org.eclipse.scout.sdk.ui.fields.proposal.nls;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.proposal.styled.SearchRangeStyledLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link NlsTextLabelProvider}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 09.02.2012
 */
public class NlsTextLabelProvider extends SearchRangeStyledLabelProvider {

  private final INlsProject m_nlsProject;

  public NlsTextLabelProvider(INlsProject nlsProject) {
    m_nlsProject = nlsProject;
  }

  @Override
  public String getText(Object element) {
    if (element == null) {
      return "";
    }
    else if (element == NlsTextProposal.NEW_NLS_TEXT_PROPOSAL) {
      return Texts.get("Nls_newProposal_name");
    }
    else if (element instanceof NlsTextProposal) {
      NlsTextProposal proposal = (NlsTextProposal) element;
      return proposal.getDisplayText();
    }
    else if (element instanceof INlsEntry) {
      // happens after the selection handler is fired to show the selected proposal (only INlsEntry is passed to the UI)
      INlsEntry entry = (INlsEntry) element;
      String text = entry.getTranslation(entry.getProject().getDevelopmentLanguage(), true);
      if (!StringUtility.hasText(text)) {
        text = entry.getKey();
      }
      return text;
    }
    return null;
  }

  @Override
  public Image getImage(Object element) {
    if (element == NlsTextProposal.NEW_NLS_TEXT_PROPOSAL) {
      return ScoutSdkUi.getImage(ScoutSdkUi.TextAdd);
    }
    if (element instanceof NlsTextProposal) {
      NlsTextProposal p = (NlsTextProposal) element;
      switch (p.getMatchKind()) {
        case NlsTextProposal.MATCH_DEV_LANG_TRANSLATION:
          return ScoutSdkUi.getImage(ScoutSdkUi.Text);
        case NlsTextProposal.MATCH_KEY:
          return ScoutSdkUi.getImage(ScoutSdkUi.TextKey);
        case NlsTextProposal.MATCH_FOREIGN_LANG:
          return ScoutSdkUi.getImage(ScoutSdkUi.TextForeign);
      }
    }
    return null;
  }

  public INlsProject getNlsProject() {
    return m_nlsProject;
  }

  @Override
  public boolean isFormatConcatString() {
    return false;
  }
}
