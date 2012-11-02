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
package org.eclipse.scout.sdk.ui.internal.view.properties.model;

import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.PageFilter;

public class EnableRegExProperty {
  private AbstractPage m_page;
  private PageFilter m_filter;

  public EnableRegExProperty(AbstractPage page, PageFilter filter) {
    m_page = page;
    m_filter = filter;
  }

  public void setRegExFilter(boolean b) {
    m_filter.setRegExFilter(b);
    m_page.refreshFilteredChildren();
  }

  public boolean isRegExFilter() {
    return m_filter.isRegExFilter();
  }

}
