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
package org.eclipse.scout.sdk.ui.fields.proposal.javaelement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.ISeparatorProposal;

/**
 * <h3>{@link JavaElementContentProvider}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 09.02.2012
 */
public class JavaElementContentProvider extends ContentProposalProvider {
  private Object SEPERATOR = new ISeparatorProposal() {
  };
  private final ILabelProvider m_labelProvider;
  private final Object[][] m_allProposals;

  public JavaElementContentProvider(ILabelProvider labelProvider, Object[] proposals) {
    this(labelProvider, new Object[][]{{proposals}});
  }

  public JavaElementContentProvider(ILabelProvider labelProvider, Object[]... proposalGroups) {
    m_labelProvider = labelProvider;
    m_allProposals = proposalGroups;
  }

  public ILabelProvider getLabelProvider() {
    return m_labelProvider;
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    if (searchPattern == null) {
      searchPattern = "*";
    }
    else {
      searchPattern = searchPattern.replaceAll("\\*$", "") + "*";
    }
    char[] pattern = CharOperation.toLowerCase(searchPattern.toCharArray());
    ArrayList<Object> result = new ArrayList<Object>();
    for (Object[] group : m_allProposals) {
      Collection<Object> groupResult = getProposals(pattern, group, monitor);
      if (result.size() > 0 && groupResult.size() > 0) {
        result.add(SEPERATOR);
      }
      result.addAll(groupResult);
    }
    return result.toArray(new Object[result.size()]);
  }

  private Collection<Object> getProposals(char[] pattern, Object[] proposals, IProgressMonitor monitor) {
    if (proposals == null) {
      return new ArrayList<Object>(0);
    }
    List<Object> result = new ArrayList<Object>();
    for (Object proposal : proposals) {
      if (CharOperation.match(pattern, getLabelProvider().getText(proposal).toCharArray(), false)) {
        result.add(0, proposal);
      }
    }
    return result;
  }
}
