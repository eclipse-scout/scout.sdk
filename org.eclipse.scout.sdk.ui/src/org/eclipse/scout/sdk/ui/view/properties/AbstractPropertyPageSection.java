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
package org.eclipse.scout.sdk.ui.view.properties;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPropertyPageSection implements IPropertyPageSection {
  private final String m_name;
  private final String m_desc;
  private final int m_logicalHeight;
  private final List<Object> m_properties;
  private boolean m_expanded;

  public AbstractPropertyPageSection(String name, String description, int logicalHeight) {
    m_name = name;
    m_desc = description;
    m_logicalHeight = logicalHeight;
    m_expanded = true;
    m_properties = new ArrayList<Object>();
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public String getDescription() {
    return m_desc;
  }

  @Override
  public int getLogicalHeight() {
    return m_logicalHeight;
  }

  /**
   * @return copy of attributes, list may be modified by caller
   */
  @Override
  public List<Object> getProperties() {
    return new ArrayList<Object>(m_properties);
  }

  @Override
  public int getPropertyCount() {
    return m_properties.size();
  }

  @Override
  public void addProperty(Object p) {
    m_properties.add(p);
  }

  @Override
  public boolean isExpanded() {
    return m_expanded;
  }

  @Override
  public void setExpanded(boolean expanded) {
    m_expanded = expanded;
  }

}
