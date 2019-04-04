/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link AsyncProposalPosition}</h3>
 *
 * @since 5.2.0
 */
public class AsyncProposalPosition extends ProposalPosition {

  private final ICompletionProposalProvider m_provider;
  private final LinkedModeModel m_model;

  public AsyncProposalPosition(IDocument document, int rangeOffset, int rangeLength, int sequence, ICompletionProposalProvider provider, LinkedModeModel model) {
    super(document, rangeOffset, rangeLength, sequence, null);
    m_provider = Ensure.notNull(provider);
    m_model = Ensure.notNull(model);
  }

  @Override
  public ICompletionProposal[] getChoices() {
    Proposal[] proposals = m_provider.getProposals();
    LinkedPositionProposalImpl[] proposalImpls = new LinkedPositionProposalImpl[proposals.length];
    for (int i = 0; i < proposals.length; i++) {
      proposalImpls[i] = new LinkedPositionProposalImpl(proposals[i], m_model);
    }
    return proposalImpls;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = super.hashCode();
    result = prime * result + m_model.hashCode();
    result = prime * result + m_provider.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AsyncProposalPosition other = (AsyncProposalPosition) obj;
    return m_model.equals(other.m_model)
        && m_provider.equals(other.m_provider);
  }
}
