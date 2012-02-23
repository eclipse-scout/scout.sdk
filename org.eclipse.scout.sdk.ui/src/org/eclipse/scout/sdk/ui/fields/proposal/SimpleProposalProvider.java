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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.scout.commons.CompareUtility;

public class SimpleProposalProvider extends ContentProposalProvider {

  private SimpleProposal[] m_proposals;

  public SimpleProposalProvider(SimpleProposal[] proposals) {
    m_proposals = proposals;
  }

  public SimpleProposalProvider(List<SimpleProposal> proposals) {
    this(proposals.toArray(new SimpleProposal[proposals.size()]));
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    if (searchPattern == null) {
      searchPattern = "*";
    }
    else {
      searchPattern = searchPattern.replaceAll("\\*$", "") + "*";
    }
    searchPattern = searchPattern.toLowerCase() + "*";

    ArrayList<Object> props = new ArrayList<Object>();
    for (SimpleProposal prop : getProposals()) {
      if (CharOperation.match(searchPattern.toCharArray(), prop.getText().toCharArray(), false)) {
        props.add(prop);
      }
    }
    return props.toArray(new Object[props.size()]);
  }

  public SimpleProposal[] getProposals() {
    return m_proposals;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SimpleProposalProvider) {
      SimpleProposalProvider compTo = (SimpleProposalProvider) obj;
      if (CompareUtility.equals(getProposals(), compTo.getProposals())) {
        return true;
      }
    }
    return false;
  }

}
