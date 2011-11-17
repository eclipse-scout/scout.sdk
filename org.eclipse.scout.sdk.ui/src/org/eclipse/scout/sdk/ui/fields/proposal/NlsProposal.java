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
package org.eclipse.scout.sdk.ui.fields.proposal;

import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>NlsProposal</h3> ...
 */
public class NlsProposal implements IContentProposalEx {

  private final INlsEntry m_entry;
  private final String m_key;
  private final String m_translation;

  public NlsProposal(INlsEntry entry, Language language) {
    m_entry = entry;
    m_key = entry.getKey();
    m_translation = entry.getTranslation(language, true);
  }

  @Override
  public int getCursorPosition(boolean selected, boolean expertMode) {
    return m_translation.length();
  }

  @Override
  public Image getImage(boolean selected, boolean expertMode) {
    return ScoutSdkUi.getImage(ScoutSdkUi.Text);
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    String value = m_translation;
    if (selected) {
      value += "  (" + m_key + ")";
    }
    return value;
  }

  public INlsEntry getNlsEntry() {
    return m_entry;
  }

}
