/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.testing.compare.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.sdk.testing.compare.ICompareResult;

/**
 * <h3>{@link LineCompareResult}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.9.0 15.03.2013
 */
public class LineCompareResult implements ICompareResult<String> {

  private List<ICompareResult.IDifference<String>> m_differences;

  public LineCompareResult() {
    m_differences = new ArrayList<ICompareResult.IDifference<String>>();
  }

  public void addDifference(IDifference<String> diff) {
    m_differences.add(diff);
  }

  @Override
  public boolean isEqual() {
    return m_differences.isEmpty();
  }

  @Override
  public List<IDifference<String>> getDifferences() {
    return m_differences;
  }

  @Override
  public IDifference<String> getFirstDifference() {
    if (getDifferences().size() > 0) {
      return getDifferences().get(0);
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (isEqual()) {
      builder.append("No differences detected.");
    }
    else {
      builder.append("Differences: ");
      builder.append("\n");
      Iterator<IDifference<String>> diffIt = getDifferences().iterator();
      if (diffIt.hasNext()) {
        builder.append(diffIt.next().toString());
      }
      while (diffIt.hasNext()) {
        builder.append("\n");
        builder.append(diffIt.next().toString());
      }
    }
    return builder.toString();
  }

  public static class LineDifference implements IDifference<String> {
    private final String m_line01;
    private final String m_line02;
    private final int m_lineNr;

    public LineDifference(int lineNr, String line01, String line02) {
      m_lineNr = lineNr;
      m_line01 = line01;
      m_line02 = line02;

    }

    @Override
    public String getValue01() {
      return m_line01;
    }

    @Override
    public String getValue02() {
      return m_line02;
    }

    public int getLineNr() {
      return m_lineNr;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Diff (line:" + getLineNr() + "):\nline01: ").append(getValue01()).append("\nline02: ").append(getValue02());
      return builder.toString();
    }

  }

}
