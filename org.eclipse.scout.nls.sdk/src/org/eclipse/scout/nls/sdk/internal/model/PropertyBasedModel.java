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
package org.eclipse.scout.nls.sdk.internal.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.eclipse.scout.nls.sdk.model.util.AbstractChangeLogModel;

public class PropertyBasedModel extends AbstractChangeLogModel implements Comparable<PropertyBasedModel> {

  private final PropertySupport propertySupport = new PropertySupport(this);
  private PropertyBasedModel m_original;

  private P_ChangeLogPropertyListener m_changeLogPropertyListener = new P_ChangeLogPropertyListener();
  private boolean m_hasChanges;
  private boolean m_pauseChangeLog;

  public PropertyBasedModel() {
  }

  /**
   * may be overwritten used to get an order
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(PropertyBasedModel o) {
    return 1;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    for (Entry<String, Object> entry : getPropertiesMap().entrySet()) {
      hash ^= entry.hashCode();
    }
    return hash;
  }

  public boolean hasChanges() {
    return m_hasChanges;
  }

  protected boolean calculateChanges() {
    return !equals(m_original);
  }

  private void revalidateChangeLog() {
    boolean changed = calculateChanges();
    if (m_hasChanges != changed) {
      m_hasChanges = changed;
      fireModelChanged();
    }
  }

  protected void setPropertyString(String key, String value) {
    setProperty(key, value);
  }

  protected String getPropertyString(String key) {
    return (String) getProperty(key);
  }

  protected boolean getPropertyBool(String key) {
    Boolean val = (Boolean) getProperty(key);
    if (val == null) {
      return false;
    }
    return val.booleanValue();
  }

  protected void setPropertyBool(String key, boolean value) {
    setProperty(key, Boolean.valueOf(value));
  }

  protected void setProperty(String key, Object value) {
    if (m_pauseChangeLog && m_original != null) {
      m_original.setProperty(key, value);
    }
    propertySupport.setProperty(key, value);
  }

  protected Object getProperty(String key) {
    return propertySupport.getProperty(key);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  public void clearProperties() {
    if (m_pauseChangeLog && m_original != null) {
      m_original.clearProperties();
    }
    propertySupport.clearProperties();
  }

  public void clearPropertiesFire() {
    if (m_pauseChangeLog && m_original != null) {
      m_original.clearPropertiesFire();
    }
    propertySupport.clearPropertiesFire();
  }

  public void removeProperty(String key) {
    if (m_pauseChangeLog && m_original != null) {
      m_original.removeProperty(key);
    }
    propertySupport.removeProperty(key);
  }

  public Map<String, Object> getPropertiesMap() {
    return propertySupport.getPropertiesMap();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PropertyBasedModel)) {
      return false;
    }
    PropertyBasedModel toCompare = (PropertyBasedModel) obj;
    TreeSet<String> set = new TreeSet<String>();
    set.addAll(getPropertiesMap().keySet());
    set.addAll(toCompare.getPropertiesMap().keySet());
    for (String key : set) {
      Object o1 = getProperty(key);
      Object o2 = toCompare.getProperty(key);
      if (o1 == null && o2 == null) {
        continue;
      }
      else if (o1 == null || o2 == null) {
        return false;
      }
      else {
        if (!getProperty(key).equals(toCompare.getProperty(key))) {
          return false;
        }
      }
    }
    return true;
  }

  private class P_ChangeLogPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      revalidateChangeLog();
    }
  }
}
