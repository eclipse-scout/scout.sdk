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
package org.eclipse.scout.sdk.ui.view.outline.pages;

import java.util.regex.Pattern;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.PageFilterExpressionPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.PageFilterRegExPresenter;

/**
 * filter the contents of a table page
 * 
 * @see PageFilterExpressionPresenter
 * @see PageFilterRegExPresenter
 */
public class PageFilter implements IPageFilter {
  private String m_filterExpression;
  private boolean m_regExFilter;
  // cache
  private Pattern m_patternInternal;

  public PageFilter() {
    createPattern();
  }

  @Override
  public String getFilterExpression() {
    return m_filterExpression;
  }

  public void setFilterExpression(String s) {
    if (s != null && s.trim().length() == 0) {
      s = null;
    }
    m_filterExpression = s;
    createPattern();
  }

  public boolean isRegExFilter() {
    return m_regExFilter;
  }

  public void setRegExFilter(boolean b) {
    m_regExFilter = b;
    createPattern();
  }

  private void createPattern() {
    String regex = getFilterExpression();
    if (regex != null) {
      regex = regex.toLowerCase();
      try {
        if (!isRegExFilter()) {
          if (!regex.endsWith("*")) {
            regex += "*";
          }
          regex = regex.replaceAll("\\?", ".").replaceAll("\\*", ".*");
        }
        // match
        if (!regex.endsWith("$")) {
          regex += ".*$";
        }
        m_patternInternal = Pattern.compile(regex);
      }
      catch (Exception e) {
        m_patternInternal = null;
      }
    }
    else {
      m_patternInternal = Pattern.compile(".*");
    }
  }

  @Override
  public boolean isEmpty() {
    return m_filterExpression == null;
  }

  @Override
  public boolean accept(IPage page) {
    return m_patternInternal == null || m_patternInternal.matcher(page.getName().toLowerCase()).matches();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PageFilter) {
      PageFilter other = (PageFilter) obj;
      return this.m_regExFilter == other.m_regExFilter &&
          CompareUtility.equals(this.m_filterExpression, other.m_filterExpression);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 1;
  }
}
