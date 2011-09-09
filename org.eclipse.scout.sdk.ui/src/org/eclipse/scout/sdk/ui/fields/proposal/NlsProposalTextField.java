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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryModifyAction;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryNewAction;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.nls.NlsNewProposal;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.nls.NlsTextProposalProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

public class NlsProposalTextField extends ProposalTextField {

  private IContentProposalEx m_selectedNlsProposal;
  private INlsProject m_nlsProject;

  public NlsProposalTextField(Composite parent) {
    this(parent, null);
  }

  public NlsProposalTextField(Composite parent, INlsProject nlsProject) {
    this(parent, nlsProject, 0);
  }

  public NlsProposalTextField(Composite parent, INlsProject nlsProject, int type) {
    super(parent, new NlsTextProposalProvider(), type);
    setNlsProject(nlsProject);
    MenuManager manager = new MenuManager();
    manager.setRemoveAllWhenShown(true);
    Menu menu = manager.createContextMenu(getTextComponent());
    manager.addMenuListener(new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager managerInside) {
        createContextMenu((MenuManager) managerInside);
      }
    });
    getTextComponent().setMenu(menu);
    setProposalDescriptionProvider(new NlsProposalDescriptionProvider());
  }

  @Override
  protected void handleCustomProposalSelected(ICustomProposal proposal) {
    // for new proposals
    if (proposal instanceof NlsNewProposal) {
      String proposalFieldText = "";
      if (getLastRequestPattern() != null) {
        proposalFieldText = getLastRequestPattern().getSearchText();
      }
      String key = getNewKey(proposalFieldText);
      NlsEntry row = new NlsEntry(key, getNlsProject());
      row.addTranslation(getNlsProject().getDevelopmentLanguage(), proposalFieldText);
      NlsEntryNewAction action = new NlsEntryNewAction(row, true, getNlsProject());
      action.run();
      try {
        action.join();
      }
      catch (InterruptedException e) {
        ScoutSdkUi.logWarning(e);
      }
      row = action.getEntry();
      if (row != null) {
        if (getNlsProject() != null) {
          getProposalProvider().setNlsEntries(getNlsProject().getAllEntries(), getNlsProject().getDevelopmentLanguage());
        }
        NlsProposal selectedProposal = new NlsProposal(row, getNlsProject().getDevelopmentLanguage());
        acceptProposal(selectedProposal);
        return;
      }
      else {
        return;
      }
    }
  }

  protected String getNewKey(String value) {
    List<String> existingKeys = Arrays.asList(getNlsProject().getAllKeys());// NlsCore.getAllEntries(getProjectGroup().getSharedProject().getNlsProject()).keySet();

    if (value == null || value.length() == 0) {
      return null;
    }
    else {
      String[] split = value.split(" ");
      value = "";
      for (String splitValue : split) {
        value = value + Character.toUpperCase(splitValue.charAt(0)) + ((splitValue.length() > 1) ? (splitValue.substring(1)) : (""));
      }
      String newKey = value;
      int i = 0;
      while (existingKeys.contains(newKey)) {
        newKey = value + i++;
      }
      return newKey;
    }
  }

  private void createContextMenu(MenuManager manager) {
    IContentProposalEx prop = getSelectedProposal();
    if (prop instanceof NlsProposal) {
      manager.add(new NlsEntryModifyAction(new NlsEntry(((NlsProposal) prop).getNlsEntry()), true, getNlsProject()));
    }
  }

  @Override
  public NlsTextProposalProvider getProposalProvider() {
    return (NlsTextProposalProvider) super.getProposalProvider();
  }

  @Override
  public NlsTextProposalProvider getContentProposalProvider() {
    return (NlsTextProposalProvider) super.getContentProposalProvider();
  }

  public void setNlsProject(INlsProject nlsProject) {
    if (!CompareUtility.equals(nlsProject, m_nlsProject)) {
      m_nlsProject = nlsProject;
      if (m_nlsProject != null) {
        getProposalProvider().setNlsEntries(m_nlsProject.getAllEntries(), getNlsProject().getDevelopmentLanguage());
      }
    }
  }

  public INlsProject getNlsProject() {
    return m_nlsProject;
  }
}
