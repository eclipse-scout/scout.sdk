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
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryNewAction;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalSelectionHandler;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 * <h3>{@link NlsTextSelectionHandler}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 10.02.2012
 */
public class NlsTextSelectionHandler implements IProposalSelectionHandler {

  private final INlsProject m_nlsProject;

  public NlsTextSelectionHandler(INlsProject nlsProject) {
    if (nlsProject == null) {
      throw new IllegalArgumentException("nls project must not be null!");
    }
    m_nlsProject = nlsProject;
  }

  public INlsProject getNlsProject() {
    return m_nlsProject;
  }

  @Override
  public void handleProposalAccepted(Object proposal, String searchText, ProposalTextField proposalTextField) {
    if (NlsTextContentProvider.NLS_NEW_PROPOSAL == proposal) {
      String proposalFieldText = "";

      if (!StringUtility.isNullOrEmpty(searchText)) {
        proposalFieldText = searchText;
      }
      String key = getNewKey(proposalFieldText);
      NlsEntry entry = new NlsEntry(key, getNlsProject());
      Language devLang = getNlsProject().getDevelopmentLanguage();
      entry.addTranslation(devLang, proposalFieldText);
      if (!Language.LANGUAGE_DEFAULT.equals(devLang)) {
        entry.addTranslation(Language.LANGUAGE_DEFAULT, proposalFieldText);
      }
      NlsEntryNewAction action = new NlsEntryNewAction(proposalTextField.getShell(), getNlsProject(), entry, true);
      action.run();
      try {
        action.join();
      }
      catch (InterruptedException e) {
        ScoutSdkUi.logWarning(e);
      }
      entry = action.getEntry();
      if (entry != null) {
        proposalTextField.acceptProposal(entry);
        return;
      }
      else {
        proposalTextField.acceptProposal(null);
      }
    }
    else {
      proposalTextField.acceptProposal(proposal);
    }
  }

  protected String getNewKey(String value) {
    return getNlsProject().generateNewKey(value);
  }
}
