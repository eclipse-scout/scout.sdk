package org.eclipse.scout.sdk.ui.internal.extensions.ear;

import org.eclipse.scout.sdk.ui.extensions.ear.IEarEntryHandler;

public class EarEntry implements Comparable<EarEntry> {
  private String m_id, m_name, m_icon;
  private int m_order;
  private IEarEntryHandler m_handler;

  public EarEntry(String id, String name, int order, String icon, IEarEntryHandler handler) {
    m_id = id;
    m_name = name;
    m_icon = icon;
    m_order = order;
    m_handler = handler;
  }

  @Override
  public int compareTo(EarEntry o) {
    int ret = new Integer(getOrder()).compareTo(o.getOrder());
    if (ret == 0) {
      return getName().compareTo(o.getName());
    }
    else {
      return ret;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof EarEntry)) return false;

    EarEntry o = (EarEntry) obj;
    return getId().equals(o.getId()) &&
        getName().equals(o.getName()) &&
        new Integer(getOrder()).equals(o.getOrder()) &&
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

  public void setHandler(IEarEntryHandler provider) {
    m_handler = provider;
  }

  public IEarEntryHandler getHandler() {
    return m_handler;
  }
}
