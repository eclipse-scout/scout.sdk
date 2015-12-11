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

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.scout.sdk.s2e.ui.internal.template.LinkedAsyncProposalModelPresenter.LinkedPositionProposalImpl;

/**
 * <h3>{@link AsyncProposalPosition}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class AsyncProposalPosition extends ProposalPosition {

  private final ICompletionProposalProvider m_provider;
  private final LinkedModeModel m_model;

  public AsyncProposalPosition(IDocument document, int offset, int length, int sequence, ICompletionProposalProvider provider, LinkedModeModel model) {
    super(document, offset, length, sequence, null);
    m_provider = Validate.notNull(provider);
    m_model = Validate.notNull(model);
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
    return 31 * super.hashCode() + m_provider.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof AsyncProposalPosition)) {
      return false;
    }
    AsyncProposalPosition other = (AsyncProposalPosition) obj;
    return m_provider.equals(other.m_provider);
  }
}
