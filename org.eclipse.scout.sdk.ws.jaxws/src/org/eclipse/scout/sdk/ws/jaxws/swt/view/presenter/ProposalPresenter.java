/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import java.util.Arrays;

import org.eclipse.core.resources.IMarker;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalEx;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ProposalPresenter<T extends IContentProposalEx> extends AbstractPropertyPresenter<T> {

  protected ProposalTextField m_proposalField;
  protected IContentProposalEx[] m_proposals;

  private IProposalAdapterListener m_proposalListener;

  public ProposalPresenter(Composite parent, FormToolkit toolkit) {
    super(parent, toolkit, true);
  }

  public ProposalPresenter(Composite parent, FormToolkit toolkit, boolean initialize) {
    super(parent, toolkit, initialize);
  }

  public void setProposals(IContentProposalEx[] proposals) {
    m_proposals = proposals;
    initializeProposalField(proposals);
  }

  private void initializeProposalField(IContentProposalEx[] proposals) {
    if (isControlCreated()) {
      setStateChanging(true);
      try {
        DefaultProposalProvider provider = new DefaultProposalProvider();
        provider.setShortList(m_proposals);
        m_proposalField.setContentProposalProvider(provider);
      }
      finally {
        setStateChanging(false);
      }
      if (!isStateChanging()) {
        setInput(getValue());
      }
    }
  }

  @Override
  protected Control createContent(Composite parent) {
    DefaultProposalProvider provider = new DefaultProposalProvider();
    provider.setShortList(m_proposals);

    m_proposalField = new ProposalTextField(parent, provider, ProposalTextField.TYPE_NO_LABEL);
    m_proposalListener = new P_ProposalListener();
    m_proposalField.addProposalAdapterListener(m_proposalListener);
    return m_proposalField;
  }

  @Override
  protected void setInputInternal(T input) {
    m_proposalField.removeProposalAdapterListener(m_proposalListener);
    try {
      if (input != null && m_proposals != null && Arrays.asList(m_proposals).contains(input)) {
        m_proposalField.acceptProposal(input);
      }
      else {
        m_proposalField.acceptProposal(null);
      }
    }
    finally {
      m_proposalField.addProposalAdapterListener(m_proposalListener);
    }
  }

  private final class P_ProposalListener implements IProposalAdapterListener {

    @SuppressWarnings("unchecked")
    @Override
    public void proposalAccepted(ContentProposalEvent event) {
      T proposal = (T) event.proposal;
      if (proposal != null || isAcceptNullValue()) {
        setValueFromUI(proposal);
      }
      else {
        setInfo(IMarker.SEVERITY_ERROR, Texts.get("InvalidValue"));
      }
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    // to change background color in disabled state
    m_proposalField.setEnabled(enabled);
  }
}
