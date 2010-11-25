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
import java.util.Collections;
import java.util.List;

public abstract class AbstractPropertyPageSection implements IPropertyPageSection {
  private String m_name;
  private String m_desc;
  private int m_logicalHeight = 1;
  private boolean m_expanded = true;
  private ArrayList<Object> m_properties = new ArrayList<Object>();

  public AbstractPropertyPageSection(String name, String description, int logicalHeight) {
    m_name = name;
    m_desc = description;
    m_logicalHeight = logicalHeight;
  }

  public String getName() {
    return m_name;
  }

  public String getDescription() {
    return m_desc;
  }

  public int getLogicalHeight() {
    return m_logicalHeight;
  }

  /**
   * @return read only list of attributes, list may be modified by caller
   */
  public List<Object> getProperties() {
    return Collections.unmodifiableList(m_properties);
  }

  public int getPropertyCount() {
    return m_properties.size();
  }

  public void addProperty(Object p) {
    m_properties.add(p);
  }

  public boolean isExpanded() {
    return m_expanded;
  }

  public void setExpanded(boolean expanded) {
    m_expanded = expanded;
  }

}
