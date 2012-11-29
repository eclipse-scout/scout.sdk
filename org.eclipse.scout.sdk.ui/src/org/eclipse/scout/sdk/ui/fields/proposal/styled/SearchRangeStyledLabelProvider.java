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
package org.eclipse.scout.sdk.ui.fields.proposal.styled;

import java.util.HashMap;

import org.eclipse.scout.sdk.ui.fields.proposal.SelectionStateLabelProvider;

/**
 * <h3>{@link SearchRangeStyledLabelProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 17.02.2012
 */
public class SearchRangeStyledLabelProvider extends SelectionStateLabelProvider implements ISearchRangeConsumer {

  private HashMap<Object, int[]> m_searchRanges = new HashMap<Object, int[]>();

  @Override
  public int[] getMatchRanges(Object element) {
    return m_searchRanges.get(element);
  }

  @Override
  public void startRecordMatchRegions() {
    m_searchRanges.clear();
  }

  @Override
  public void addMatchRegions(Object element, int[] matchRegions) {
    m_searchRanges.put(element, matchRegions);
  }

  @Override
  public void endRecordMatchRegions() {
  }

  @Override
  public void dispose() {
    super.dispose();
    m_searchRanges.clear();
  }

  @Override
  public boolean isFormatConcatString() {
    return true;
  }
}
