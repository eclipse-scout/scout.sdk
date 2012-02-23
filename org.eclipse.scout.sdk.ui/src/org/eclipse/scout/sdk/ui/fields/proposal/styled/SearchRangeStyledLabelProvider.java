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

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ISelectionStateLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link SearchRangeStyledLabelProvider}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 17.02.2012
 */
public class SearchRangeStyledLabelProvider implements ISelectionStateLabelProvider, ISearchRangeConsumer {

  private HashMap<Object, int[]> m_searchRanges = new HashMap<Object, int[]>();

  @Override
  public Image getImage(Object element) {
    return null;
  }

  @Override
  public Image getImageSelected(Object element) {
    return getImage(element);
  }

  @Override
  public String getText(Object element) {
    if (element != null) {
      return element.toString();
    }
    return null;
  }

  @Override
  public String getTextSelected(Object element) {
    return getText(element);
  }

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
    m_searchRanges.clear();
  }

  @Override
  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  @Override
  public void addListener(ILabelProviderListener listener) {
  }

  @Override
  public void removeListener(ILabelProviderListener listener) {
  }

}
