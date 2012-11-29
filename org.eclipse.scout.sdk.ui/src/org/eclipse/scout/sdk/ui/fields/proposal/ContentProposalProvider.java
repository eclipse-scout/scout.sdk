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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.StringUtility;

/**
 * <h3>{@link ContentProposalProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 13.02.2012
 */
public class ContentProposalProvider implements ILazyProposalContentProvider {
  private static final char END_SYMBOL = '<';
  private static final char ANY_STRING = '*';
  private static final char BLANK = ' ';

  @Override
  public void dispose() {
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    return null;
  }

  public static int[] getMatchingRegions(Object element, String elementText, NormalizedPattern pattern) {
    if (element instanceof ISeparatorProposal) {
      return new int[0];
    }
    if (elementText == null) {
      return null;
    }
    String filterText = elementText;
    if (!StringUtility.isNullOrEmpty(filterText)) {
      int index = filterText.indexOf(JavaElementLabels.CONCAT_STRING);
      if (index > -1) {
        filterText = filterText.substring(0, index);
      }
    }
    int[] matchingRegions = SearchPattern.getMatchingRegions(pattern.getPattern(), filterText, pattern.getMatchKind());
    return matchingRegions;
  }

  protected static NormalizedPattern createNormalizedSearchPattern(String pattern) {
    int length = pattern.length();
    if (length == 0) {
      return new NormalizedPattern(pattern, SearchPattern.R_EXACT_MATCH);
    }
    char last = pattern.charAt(length - 1);
    if (pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1) {

      switch (last) {
        case END_SYMBOL:
        case BLANK:
          return new NormalizedPattern(pattern.substring(0, length - 1), SearchPattern.R_PATTERN_MATCH);
        case ANY_STRING:
          return new NormalizedPattern(pattern, SearchPattern.R_PATTERN_MATCH);
        default:
          return new NormalizedPattern(pattern + ANY_STRING, SearchPattern.R_PATTERN_MATCH);
      }
    }

    if (last == END_SYMBOL || last == BLANK) {
      pattern = pattern.substring(0, length - 1);
      if (SearchPattern.validateMatchRule(pattern, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH) == SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH) {
        return new NormalizedPattern(pattern, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
      }
      else {
        return new NormalizedPattern(pattern, SearchPattern.R_EXACT_MATCH);
      }
    }
    if (SearchPattern.validateMatchRule(pattern, SearchPattern.R_CAMELCASE_MATCH) == SearchPattern.R_CAMELCASE_MATCH) {
      return new NormalizedPattern(pattern, SearchPattern.R_CAMELCASE_MATCH);
    }
    return new NormalizedPattern(pattern, SearchPattern.R_PREFIX_MATCH);
  }

  public static class NormalizedPattern {
    private final int m_matchKind;
    private final String m_pattern;

    public NormalizedPattern(String pattern, int matchKind) {
      m_pattern = pattern;
      m_matchKind = matchKind;
    }

    public String getPattern() {
      return m_pattern;
    }

    public int getMatchKind() {
      return m_matchKind;
    }
  }
}
