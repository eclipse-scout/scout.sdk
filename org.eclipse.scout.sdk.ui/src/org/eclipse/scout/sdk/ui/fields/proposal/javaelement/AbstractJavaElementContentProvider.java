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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.MoreElementsProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.styled.ISearchRangeConsumer;

/**
 * <h3>{@link AbstractJavaElementContentProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 09.02.2012
 */
public abstract class AbstractJavaElementContentProvider extends ContentProposalProvider {
  private final ILabelProvider m_labelProvider;
  private Object[][] m_allProposals;

  protected AbstractJavaElementContentProvider() {
    this(new JavaElementLabelProvider());
  }

  protected AbstractJavaElementContentProvider(ILabelProvider labelProvider) {
    m_labelProvider = labelProvider;
    m_allProposals = null;
  }

  public ILabelProvider getLabelProvider() {
    return m_labelProvider;
  }

  protected abstract Object[][] computeProposals();

  public synchronized void invalidateCache() {
    m_allProposals = null;
  }

  protected synchronized Object[][] getAllProposals() {
    if (m_allProposals == null) {
      m_allProposals = computeProposals();
    }
    return m_allProposals;
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    if (!StringUtility.hasText(searchPattern)) {
      searchPattern = "*";
    }
    NormalizedPattern pattern = createNormalizedSearchPattern(searchPattern);
    ArrayList<Object> result = new ArrayList<Object>();
    // avoid checking for every single entry instanceof ISearchRangeConsumer
    ISearchRangeConsumer searchRangeSupport = null;
    if (getLabelProvider() instanceof ISearchRangeConsumer) {
      searchRangeSupport = (ISearchRangeConsumer) getLabelProvider();
    }
    else {
      searchRangeSupport = new P_EmptySearchRangeSupport();
    }

    for (Object[] group : getAllProposals()) {
      List<Object> groupResult = getProposals(pattern, group, searchRangeSupport, monitor);
      if (result.size() > 0 && groupResult.size() > 0) {
        result.add(MoreElementsProposal.INSTANCE);
      }
      result.addAll(groupResult);
    }
    return result.toArray(new Object[result.size()]);
  }

  protected List<Object> getProposals(NormalizedPattern pattern, Object[] proposals, ISearchRangeConsumer searchRangeSupport, IProgressMonitor monitor) {
    if (proposals == null) {
      return new ArrayList<Object>(0);
    }
    List<Object> result = new ArrayList<Object>();
    for (Object proposal : proposals) {
      int[] matchingRegions = getMatchingRegions(proposal, getLabelProvider().getText(proposal), pattern);
      if (matchingRegions != null) {
        result.add(proposal);
        searchRangeSupport.addMatchRegions(proposal, matchingRegions);
      }
    }
    return result;
  }

  private final static class P_EmptySearchRangeSupport implements ISearchRangeConsumer {

    @Override
    public int[] getMatchRanges(Object element) {
      return null;
    }

    @Override
    public void startRecordMatchRegions() {
    }

    @Override
    public void addMatchRegions(Object element, int[] matchRegions) {
    }

    @Override
    public void endRecordMatchRegions() {
    }

    @Override
    public boolean isFormatConcatString() {
      return false;
    }
  }
}
