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
 * <h3>{@link NlsTextLabelProvider}</h3> ...
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
    else if (NlsTextContentProvider.NLS_NEW_PROPOSAL == element) {
      return Texts.get("Nls_newProposal_name");
    }
    else if (element instanceof INlsEntry) {
      INlsEntry entry = (INlsEntry) element;
      String text = entry.getTranslation(getNlsProject().getDevelopmentLanguage(), true);
      if (StringUtility.isNullOrEmpty(text)) {
        text = entry.getKey();
      }
      return text;
    }
    throw new IllegalArgumentException("expected instanceof INlsEntry, got '" + element + "'");
  }

  @Override
  public String getTextSelected(Object element) {
    if (element == NlsTextContentProvider.NLS_NEW_PROPOSAL) {
      return getText(element);
    }
    else if (element == null) {
      return "";
    }
    else {
      StringBuilder textBuilder = new StringBuilder();
      textBuilder.append(getText(element));
      // instance check is done in getText
      INlsEntry entry = (INlsEntry) element;
      textBuilder.append("  (").append(entry.getKey()).append(")");
      return textBuilder.toString();
    }
  }

  @Override
  public Image getImage(Object element) {
    if (NlsTextContentProvider.NLS_NEW_PROPOSAL == element) {
      return ScoutSdkUi.getImage(ScoutSdkUi.TextAdd);
    }
    else {
      return ScoutSdkUi.getImage(ScoutSdkUi.Text);
    }
  }

  @Override
  public Image getImageSelected(Object element) {
    return getImage(element);
  }

  public INlsProject getNlsProject() {
    return m_nlsProject;
  }
}
