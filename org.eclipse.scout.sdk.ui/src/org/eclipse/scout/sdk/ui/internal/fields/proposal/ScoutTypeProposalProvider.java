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
package org.eclipse.scout.sdk.ui.internal.fields.proposal;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalEx;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalProvider;

public class ScoutTypeProposalProvider implements IContentProposalProvider {

  private IContentProposalEx[] m_proposals;

  public ScoutTypeProposalProvider(IContentProposalEx[] proposals) {
    m_proposals = proposals;
  }

  @Override
  public IContentProposalEx[] getProposals(String content, int cursorPosition, IProgressMonitor monitor) {
    ArrayList<IContentProposalEx> props = new ArrayList<IContentProposalEx>();
    String matchString = content.substring(0, cursorPosition);
    matchString = matchString.toLowerCase() + "*";
    for (IContentProposalEx prop : m_proposals) {
      if (CharOperation.match(matchString.toCharArray(), prop.getLabel(false, false).toCharArray(), false)) {
        props.add(prop);
      }
    }
    return props.toArray(new IContentProposalEx[props.size()]);
  }

  @Override
  public IContentProposalEx[] getProposalsExpertMode(String content, int cursorPosition, IProgressMonitor monitor) {
    return null;
  }

  @Override
  public boolean supportsExpertMode() {
    return false;
  }

}
