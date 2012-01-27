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
import java.beans.PropertyChangeListenerProxy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.scout.nls.sdk.internal.NlsCore;

public class PropertySupport {

  public static final int DEFAULT_INT_VALUE = 0;
  public static final int DEFAULT_DOUBLE_VALUE = 0;
  public static final Integer DEFAULT_INT = new Integer(DEFAULT_INT_VALUE);
  public static final Double DEFAULT_DOUBLE = new Double(DEFAULT_DOUBLE_VALUE);
  public static final long DEFAULT_LONG_VALUE = DEFAULT_INT_VALUE;
  public static final Long DEFAULT_LONG = new Long(DEFAULT_LONG_VALUE);
  private static final Boolean DEFAULT_BOOL = new Boolean(false);
  private HashMap<String, Object> m_props = new HashMap<String, Object>();
  private Object m_source;
  private transient Vector<PropertyChangeListener> m_listeners;
  private Hashtable<String, Object> m_children;

  public PropertySupport(Object sourceBean) {
    m_source = sourceBean;
  }

  public void clearProperties() {
    m_props.clear();
  }

  public void clearPropertiesFire() {
    List<String> keys = new ArrayList<String>(m_props.keySet());
    for (String key : keys) {
      firePropertyChange(key, m_props.get(key), null);
      m_props.remove(key);
    }
  }

  public void removeProperty(String key) {
    m_props.remove(key);
    firePropertyChange(key, m_props.get(key), null);
  }

  public Map<String, Object> getPropertiesMap() {
    // loop and catch exception instead of using lock (better performance)
    for (int i = 0; i < 10; i++) {
      try {
        return new HashMap<String, Object>(m_props);
      }
      catch (ConcurrentModificationException cme) {
        NlsCore.logWarning(cme);
      }
    }
    return new HashMap<String, Object>(m_props);
  }

  public void putPropertiesMap(Map<String, Object> map) {
    m_props.putAll(map);
  }

  /** DESIGN: should return false if the asked property is set to null - see setProperty() as well. (tha, 16.2.6) */
  public boolean hasProperty(String name) {
    return m_props.containsKey(name);
  }

  public void setPropertyInt(String name, int i) {
    setProperty(name, new Integer(i), DEFAULT_INT);
  }

  public int getPropertyInt(String name) {
    Number n = (Number) getProperty(name);
    return n != null ? n.intValue() : 0;
  }

  public void setPropertyDouble(String name, double d) {
    setProperty(name, new Double(d), DEFAULT_DOUBLE);
  }

  public double getPropertyDouble(String name) {
    Number n = (Number) getProperty(name);
    return n != null ? n.doubleValue() : 0;
  }

  public void setPropertyFloat(String name, float d) {
    setProperty(name, new Float(d), DEFAULT_DOUBLE);
  }

  public float getPropertyFloat(String name) {
    Number n = (Number) getProperty(name);
    return n != null ? n.floatValue() : 0;
  }

  public void setPropertyLong(String name, long i) {
    setProperty(name, new Long(i), DEFAULT_LONG);
  }

  public long getPropertyLong(String name) {
    Number n = (Number) getProperty(name);
    return n != null ? n.longValue() : DEFAULT_LONG.longValue();
  }

  public boolean setPropertyBool(String name, boolean b) {
    return setProperty(name, new Boolean(b), DEFAULT_BOOL);
  }

  public boolean getPropertyBool(String name) {
    Boolean b = (Boolean) getProperty(name);
    return b != null ? b.booleanValue() : DEFAULT_BOOL.booleanValue();
  }

  public void setPropertyString(String name, String s) {
    setProperty(name, s);
  }

  public void setPropertyStringAlwaysFire(String name, String s) {
    setPropertyAlwaysFire(name, s);
  }

  public String getPropertyString(String name) {
    String s = (String) getProperty(name);
    return s;
  }

  public Object getProperty(String name) {
    return m_props.get(name);
  }

  public boolean setProperty(String name, Object newValue) {
    return setProperty(name, newValue, null);
  }

  public boolean/* changed */setPropertyNoFire(String name, Object newValue) {
    Object oldValue = m_props.get(name);
    m_props.put(name, newValue);
    if (oldValue == newValue || (oldValue != null && oldValue.equals(newValue))) {
      // no change
      return false;
    }
    else {
      return true;
    }
  }

  /** DESIGN: should remove property if set to null - see hasProperty() as well (tha, 16.2.6) */
  public boolean setProperty(String name, Object newValue, Object defaultOldValueWhenNull) {
    Object oldValue = m_props.get(name);
    if (oldValue == null) {
      oldValue = defaultOldValueWhenNull;
    }
    m_props.put(name, newValue);
    if (oldValue == newValue || (oldValue != null && oldValue.equals(newValue))) {
      // no change
      return false;
    }
    else {
      // if(logger.isInfo()) logger.info("fire "+m_source.getClass().getSimpleName()+" "+name+" "+oldValue+"->"+newValue);
      firePropertyChangeImpl(name, oldValue, newValue);
      return true;
    }
  }

  public void setPropertyAlwaysFire(String name, Object newValue) {
    Object oldValue = m_props.get(name);
    m_props.put(name, newValue);
    firePropertyChangeImpl(name, oldValue, newValue);
  }

  /**
   * Implementation
   */

  public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    if (listener instanceof PropertyChangeListenerProxy) {
      PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
      // Call two argument add method.
      addPropertyChangeListener(proxy.getPropertyName(), (PropertyChangeListener) proxy.getListener());
    }
    else {
      if (m_listeners == null) {
        m_listeners = new Vector<PropertyChangeListener>();
      }

      m_listeners.addElement(listener);
    }
  }

  public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    if (listener instanceof PropertyChangeListenerProxy) {
      PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
      // Call two argument remove method.
      removePropertyChangeListener(proxy.getPropertyName(), (PropertyChangeListener) proxy.getListener());
    }
    else {
      if (m_listeners == null) {
        return;
      }
      m_listeners.removeElement(listener);
    }
  }

  public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    if (m_children == null) {
      m_children = new java.util.Hashtable<String, Object>();
    }
    PropertySupport child = (PropertySupport) m_children.get(propertyName);
    if (child == null) {
      child = new PropertySupport(m_source);
      m_children.put(propertyName, child);
    }
    child.addPropertyChangeListener(listener);
  }

  public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    if (m_children == null) {
      return;
    }
    PropertySupport child = (PropertySupport) m_children.get(propertyName);
    if (child == null) {
      return;
    }
    child.removePropertyChangeListener(listener);
  }

  public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    if (oldValue == newValue) {
      return;
    }
    firePropertyChangeImpl(propertyName, new Integer(oldValue), new Integer(newValue));
  }

  public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    if (oldValue == newValue) {
      return;
    }
    firePropertyChangeImpl(propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
  }

  public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
      return;
    }
    firePropertyChangeImpl(propertyName, oldValue, newValue);
  }

  private void firePropertyChangeImpl(String propertyName, Object oldValue, Object newValue) {
    Vector targets = null;
    PropertySupport child = null;
    synchronized (this) {
      if (m_listeners != null) {
        targets = (java.util.Vector) m_listeners.clone();
      }
      if (m_children != null && propertyName != null) {
        child = (PropertySupport) m_children.get(propertyName);
      }
    }

    PropertyChangeEvent evt = null;

    if (targets != null) {
      if (evt == null) evt = new PropertyChangeEvent(m_source, propertyName, oldValue, newValue);
      for (int i = 0; i < targets.size(); i++) {
        Object o = targets.elementAt(i);
        PropertyChangeListener target = null;
        if (o instanceof WeakReference) {
          target = (PropertyChangeListener) ((WeakReference) o).get();
        }
        else {
          target = (PropertyChangeListener) o;
        }
        if (target != null) {
          target.propertyChange(evt);
        }
      }
    }

    if (child != null) {
      if (evt == null) evt = new PropertyChangeEvent(m_source, propertyName, oldValue, newValue);
      child.firePropertyChangeImpl(evt);
    }
  }

  private void firePropertyChangeImpl(PropertyChangeEvent evt) {
    Object oldValue = evt.getOldValue();
    Object newValue = evt.getNewValue();
    String propertyName = evt.getPropertyName();
    if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
      return;
    }

    java.util.Vector targets = null;
    PropertySupport child = null;
    synchronized (this) {
      if (m_listeners != null) {
        targets = (java.util.Vector) m_listeners.clone();
      }
      if (m_children != null && propertyName != null) {
        child = (PropertySupport) m_children.get(propertyName);
      }
    }

    if (targets != null) {
      for (int i = 0; i < targets.size(); i++) {
        Object o = targets.elementAt(i);
        PropertyChangeListener target = null;
        if (o instanceof WeakReference) {
          target = (PropertyChangeListener) ((WeakReference) o).get();
        }
        else {
          target = (PropertyChangeListener) o;
        }
        if (target != null) {
          target.propertyChange(evt);
        }
      }
    }
    if (child != null) {
      child.firePropertyChangeImpl(evt);
    }
  }

  public synchronized boolean hasListeners(String propertyName) {
    if (m_listeners != null && !m_listeners.isEmpty()) {
      // there is a generic listener
      return true;
    }
    if (m_children != null) {
      PropertySupport child = (PropertySupport) m_children.get(propertyName);
      if (child != null && child.m_listeners != null) {
        return !child.m_listeners.isEmpty();
      }
    }
    return false;
  }

}
