package org.eclipse.scout.sdk.util;

import java.beans.PropertyChangeListener;
import java.util.Map;

import org.eclipse.scout.commons.beans.BasicPropertySupport;

public class PropertyMap {
  private final BasicPropertySupport m_props;

  public PropertyMap() {
    m_props = new BasicPropertySupport(null);
  }

  public Object getProperty(String name) {
    return getProperty(name, Object.class);
  }

  @SuppressWarnings("unchecked")
  public <T extends Object> T getProperty(String name, Class<T> type) {
    return (T) m_props.getProperty(name);
  }

  public void setProperty(String name, Object value) {
    m_props.setProperty(name, value);
  }

  public Map<String, Object> getPropertiesMap() {
    return m_props.getPropertiesMap();
  }

  public boolean hasProperty(String name) {
    return m_props.hasProperty(name);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_props.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_props.addPropertyChangeListener(propertyName, listener);
  }
}
