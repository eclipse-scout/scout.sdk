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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.styled.ISearchRangeConsumer;

/**
 * <h3>{@link StaticContentProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 12.02.2012
 */
public class StaticContentProvider extends ContentProposalProvider {

  private Object[] m_elements;
  private final ILabelProvider m_labelProvider;

  public StaticContentProvider(Object[] elements, ILabelProvider labelProvider) {
    m_elements = elements;
    m_labelProvider = labelProvider;
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    if (StringUtility.isNullOrEmpty(searchPattern)) {
      searchPattern = "*";
    }

    ArrayList<Object> props = new ArrayList<Object>();
    NormalizedPattern normalizedPattern = createNormalizedSearchPattern(searchPattern);
    Object[] elements = getElements();
    ISearchRangeConsumer searchRangeLabelProvider = null;
    if (getLabelProvider() instanceof ISearchRangeConsumer) {
      searchRangeLabelProvider = (ISearchRangeConsumer) getLabelProvider();
      searchRangeLabelProvider.startRecordMatchRegions();
    }
    if (elements != null) {
      for (Object prop : elements) {
        if (monitor.isCanceled()) {
          break;
        }
        int[] matchRegions = getMatchingRegions(prop, getLabelProvider().getText(prop), normalizedPattern);
        if (matchRegions != null) {
          props.add(prop);
        }
        if (searchRangeLabelProvider != null) {
          searchRangeLabelProvider.addMatchRegions(prop, matchRegions);
        }
      }
    }
    if (searchRangeLabelProvider != null) {
      searchRangeLabelProvider.endRecordMatchRegions();
    }
    return props.toArray(new Object[props.size()]);
  }

  public void setElements(Object[] elements) {
    m_elements = elements;
  }

  public Object[] getElements() {
    return m_elements;
  }

  public ILabelProvider getLabelProvider() {
    return m_labelProvider;
  }
}
