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

import java.util.Set;

import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * The nls implementation shows all available translations for the chosen proposal.
 * This is useful while the user selects name keys for multilingual support.
 */
public class NlsProposalDescriptionProvider implements IProposalDescriptionProvider {

  @Override
  public Control createDescriptionContent(Composite parent, IContentProposalEx proposal) {

    if (proposal instanceof NlsProposal) {
      NlsProposal nlsProp = (NlsProposal) proposal;
      String key = nlsProp.getNlsEntry().getKey();
      if (key == null) {
        return null;
      }
      Composite rootArea = new Composite(parent, SWT.INHERIT_FORCE);
      rootArea.setBackground(parent.getBackground());
      Set<Language> lanuageSet = nlsProp.getNlsEntry().getAllTranslations().keySet();
      Language[] languageArr = lanuageSet.toArray(new Language[lanuageSet.size()]);
      // Arrays.sort(languageArr, NlsCore.getLanguageDefaultComparator());
      Label keyLabel = new Label(rootArea, SWT.NONE);
      keyLabel.setBackground(rootArea.getBackground());
      keyLabel.setFont(ScoutSdkUi.getFont(ScoutSdkUi.FONT_SYSTEM_BOLD));
      keyLabel.setText(key);
      keyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
      Canvas splitter = new Canvas(rootArea, SWT.NONE);
      splitter.setBackground(splitter.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
      GridData splitterData = new GridData(SWT.DEFAULT, 2);
      splitterData.grabExcessHorizontalSpace = true;
      splitterData.horizontalSpan = 2;
      splitterData.horizontalAlignment = SWT.FILL;
      splitter.setLayoutData(splitterData);
      for (Language lang : languageArr) {
        Label langLabel = new Label(rootArea, SWT.NONE);
        langLabel.setBackground(rootArea.getBackground());
        langLabel.setText(lang.getDispalyName() + ": ");
        Label transLabel = new Label(rootArea, SWT.NONE);
        String text = nlsProp.getNlsEntry().getTranslation(lang);
        if (text == null) {
          text = "";
        }
        transLabel.setText(text);
        transLabel.setBackground(rootArea.getBackground());
      }
      rootArea.setLayout(new GridLayout(2, false));
      return rootArea;
    }
    return null;
  }
}
