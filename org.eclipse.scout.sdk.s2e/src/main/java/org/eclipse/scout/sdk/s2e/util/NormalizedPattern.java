/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.search.SearchPattern;

/**
 * <h3>{@link NormalizedPattern}</h3> A {@link String} matching pattern. Use {@link NormalizedPattern#build(String)}
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class NormalizedPattern {

  private static final char END_SYMBOL = '<';
  private static final char ANY_STRING = '*';
  private static final char ANY_CHAR = '?';
  private static final char BLANK = ' ';

  private final int m_matchRule;
  private final String m_pattern;

  /**
   * Creates a new {@link NormalizedPattern} for the given search string with respect to '*' and '?' as wildcards and '
   * ' and '<' as end delimiter.
   *
   * @param searchString
   *          The search pattern.
   * @return the created {@link NormalizedPattern}.
   */
  public static NormalizedPattern build(String searchString) {
    if (StringUtils.isBlank(searchString)) {
      return new NormalizedPattern(Character.toString(ANY_STRING), SearchPattern.R_PATTERN_MATCH);
    }

    int length = searchString.length();
    char last = searchString.charAt(length - 1);
    if (searchString.indexOf(ANY_STRING) != -1 || searchString.indexOf(ANY_CHAR) != -1) {
      switch (last) {
        case END_SYMBOL:
        case BLANK:
          return new NormalizedPattern(searchString.substring(0, length - 1), SearchPattern.R_PATTERN_MATCH);
        case ANY_STRING:
          return new NormalizedPattern(searchString, SearchPattern.R_PATTERN_MATCH);
        default:
          return new NormalizedPattern(searchString + ANY_STRING, SearchPattern.R_PATTERN_MATCH);
      }
    }

    if (last == END_SYMBOL || last == BLANK) {
      searchString = searchString.substring(0, length - 1);
      if (SearchPattern.validateMatchRule(searchString, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH) == SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH) {
        return new NormalizedPattern(searchString, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
      }
      return new NormalizedPattern(searchString, SearchPattern.R_EXACT_MATCH);
    }
    if (SearchPattern.validateMatchRule(searchString, SearchPattern.R_CAMELCASE_MATCH) == SearchPattern.R_CAMELCASE_MATCH) {
      return new NormalizedPattern(searchString, SearchPattern.R_CAMELCASE_MATCH);
    }
    return new NormalizedPattern(searchString, SearchPattern.R_PREFIX_MATCH);
  }

  /**
   * @return Gets if this pattern represents an empty search {@link String} that would accept all candidates. A
   *         wildcard-only pattern is also considered to be empty.
   */
  public boolean isEmpty() {
    String pattern = getPattern();
    if (StringUtils.isBlank(pattern)) {
      return true;
    }
    for (int i = 0; i < pattern.length(); i++) {
      if (pattern.charAt(i) != ANY_STRING && pattern.charAt(i) != ANY_CHAR) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets all matching positions of the given text.
   *
   * @param elementText
   * @return The matching regions of the given text or <code>null</code> if there are no regions. See
   *         {@link SearchPattern#getMatchingRegions(String, String, int)}.
   */
  public int[] getMatchingRegions(String elementText) {
    if (elementText == null) {
      return null;
    }
    return SearchPattern.getMatchingRegions(getPattern(), elementText, getMatchRule());
  }

  /**
   * Checks if the given text matches this {@link NormalizedPattern}.
   *
   * @param elementText
   * @return <code>true</code> if the given text matches this {@link NormalizedPattern}. <code>false</code> otherwise.
   */
  public boolean matches(String elementText) {
    return getMatchingRegions(elementText) != null;
  }

  protected NormalizedPattern(String pattern, int matchRule) {
    m_pattern = pattern;
    m_matchRule = matchRule;
  }

  /**
   * @return Gets the pattern string
   */
  public String getPattern() {
    return m_pattern;
  }

  /**
   * @return Gets the match rule.
   * @see SearchPattern and its constants for details.
   */
  public int getMatchRule() {
    return m_matchRule;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("NormalizedPattern [Pattern=").append(getPattern()).append(", MatchRule=").append(getMatchRule()).append(']');
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_matchRule;
    result = prime * result + ((m_pattern == null) ? 0 : m_pattern.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NormalizedPattern other = (NormalizedPattern) obj;
    if (m_matchRule != other.m_matchRule) {
      return false;
    }
    if (m_pattern == null) {
      if (other.m_pattern != null) {
        return false;
      }
    }
    else if (!m_pattern.equals(other.m_pattern)) {
      return false;
    }
    return true;
  }
}
