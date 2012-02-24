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
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SimpleLabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.SimpleProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.StaticContentProvider;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ProposalPresenter<T extends SimpleProposal> extends AbstractPropertyPresenter<T> {

  protected ProposalTextField m_proposalField;
  protected SimpleProposal[] m_proposals;

  private IProposalAdapterListener m_proposalListener;

  public ProposalPresenter(Composite parent, PropertyViewFormToolkit toolkit) {
    super(parent, toolkit, true);
  }

  public ProposalPresenter(Composite parent, PropertyViewFormToolkit toolkit, boolean initialize) {
    super(parent, toolkit, initialize);
  }

  @Override
  protected Control createContent(Composite parent) {
    m_proposalField = getToolkit().createProposalField(parent, "", ProposalTextField.STYLE_NO_LABEL);
    initializeProposalField(getProposals());
    m_proposalListener = new P_ProposalListener();
    m_proposalField.addProposalAdapterListener(m_proposalListener);
    return m_proposalField;
  }

  public void setProposals(SimpleProposal[] proposals) {
    m_proposals = proposals;
    initializeProposalField(proposals);
  }

  private void initializeProposalField(SimpleProposal[] proposals) {
    setStateChanging(true);
    try {
      if (isControlCreated()) {
        StaticContentProvider provider = new StaticContentProvider(m_proposals, new SimpleLabelProvider());
        m_proposalField.setContentProvider(provider);
        if (!isStateChanging()) {
          setInput(getValue());
        }
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public SimpleProposal[] getProposals() {
    return m_proposals;
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
