/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.util.Arrays;

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
    var proposals = m_provider.getProposals();
    return Arrays.stream(proposals)
        .map(proposal -> new LinkedPositionProposalImpl(proposal, m_model))
        .toArray(LinkedPositionProposalImpl[]::new);
  }

  @Override
  public int hashCode() {
    var prime = 31;
    var result = super.hashCode();
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
    var other = (AsyncProposalPosition) obj;
    return m_model.equals(other.m_model)
        && m_provider.equals(other.m_provider);
  }
}
