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
package org.eclipse.scout.sdk.ui.internal.extensions.export;

import org.eclipse.scout.sdk.ui.extensions.export.IExportScoutProjectEntryHandler;

public class ExportScoutProjectEntry implements Comparable<ExportScoutProjectEntry> {
  private String m_id, m_name, m_icon;
  private int m_order;
  private IExportScoutProjectEntryHandler m_handler;

  public ExportScoutProjectEntry(String id, String name, int order, String icon, IExportScoutProjectEntryHandler handler) {
    m_id = id;
    m_name = name;
    m_icon = icon;
    m_order = order;
    m_handler = handler;
  }

  @Override
  public int hashCode() {
    Integer o = Integer.valueOf(getOrder());
    return o.hashCode() ^ getName().hashCode();
  }

  @Override
  public int compareTo(ExportScoutProjectEntry o) {
    int ret = Integer.valueOf(getOrder()).compareTo(o.getOrder());
    if (ret == 0) {
      return getName().compareTo(o.getName());
    }
    else {
      return ret;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ExportScoutProjectEntry)) return false;

    ExportScoutProjectEntry o = (ExportScoutProjectEntry) obj;
    return getId().equals(o.getId()) &&
        getName().equals(o.getName()) &&
        Integer.valueOf(getOrder()).equals(o.getOrder()) &&
        getIcon().equals(o.getIcon());
  }

  public String getId() {
    return m_id;
  }

  public void setId(String id) {
    m_id = id;
  }

  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  public String getIcon() {
    return m_icon;
  }

  public void setIcon(String icon) {
    m_icon = icon;
  }

  public int getOrder() {
    return m_order;
  }

  public void setOrder(int order) {
    m_order = order;
  }

  public void setHandler(IExportScoutProjectEntryHandler provider) {
    m_handler = provider;
  }

  public IExportScoutProjectEntryHandler getHandler() {
    return m_handler;
  }
}
