/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.eclipse.scout.sdk.core.util.EventListenerList.IWeakEventListener;

/**
 * <h3>{@link PropertySupport}</h3> Property map supporting property change event listeners.
 *
 * @see IWeakEventListener
 * @see EventListenerList
 * @since 5.1.0
 */
public class PropertySupport {

  private final Map<String, Object> m_props;
  private final Object m_source;

  private EventListenerList m_listeners; // listeners for all properties. lazy created
  private Map<String, EventListenerList> m_childListeners; // listeners for specific property. lazy created.

  public PropertySupport() {
    this(null);
  }

  public PropertySupport(Object source) {
    if (source == null) {
      m_source = this;
    }
    else {
      m_source = source;
    }
    m_props = new HashMap<>();
  }

  public PropertySupport(int size) {
    this(null, size);
  }

  public PropertySupport(Object source, int size) {
    if (source == null) {
      m_source = this;
    }
    else {
      m_source = source;
    }
    m_props = new HashMap<>(size);
  }

  /**
   * @return A modifiable copy of the properties map.
   */
  public Map<String, Object> getPropertiesMap() {
    synchronized (m_props) {
      return new HashMap<>(m_props);
    }
  }

  public boolean hasProperty(String name) {
    return m_props.containsKey(name);
  }

  /**
   * @return The number of properties
   */
  public int size() {
    return m_props.size();
  }

  /**
   * @see #setProperty(String, Object)
   */
  public boolean setPropertyInt(String name, int i) {
    return setProperty(name, i);
  }

  public int getPropertyInt(String name, int defaultValue) {
    Number n = getProperty(name, Number.class);
    if (n == null) {
      return defaultValue;
    }
    return n.intValue();
  }

  /**
   * @see #setProperty(String, Object)
   */
  public boolean setPropertyDouble(String name, double d) {
    return setProperty(name, d);
  }

  public double getPropertyDouble(String name, double defaultValue) {
    Number n = getProperty(name, Number.class);
    if (n == null) {
      return defaultValue;
    }
    return n.doubleValue();
  }

  /**
   * @see #setProperty(String, Object)
   */
  public boolean setPropertyLong(String name, long i) {
    return setProperty(name, i);
  }

  public long getPropertyLong(String name, long defaultValue) {
    Number n = getProperty(name, Number.class);
    if (n == null) {
      return defaultValue;
    }
    return n.longValue();
  }

  /**
   * @see #setProperty(String, Object)
   */
  public boolean setPropertyBool(String name, boolean b) {
    return setProperty(name, b);
  }

  public boolean getPropertyBool(String name, boolean defaultValue) {
    Boolean b = getProperty(name, Boolean.class);
    if (b == null) {
      return defaultValue;
    }
    return b;
  }

  /**
   * @see #setProperty(String, Object)
   */
  public boolean setPropertyString(String name, String s) {
    return setProperty(name, s);
  }

  public String getPropertyString(String name) {
    return getProperty(name, String.class);
  }

  public <T> T getProperty(String name, Class<T> type) {
    return type.cast(m_props.get(name));
  }

  /**
   * Sets the given property to the given value.
   *
   * @param name
   *          The name of the property. Must not be {@code null}.
   * @param newValue
   *          The new value of the property. If the value is {@code null} the property will be removed.
   * @return {@code true} if the property was changed (and therefore if the change events have been fired).
   *         {@code false} if the property already had that value and nothing was changed.
   */
  public boolean setProperty(String name, Object newValue) {
    Ensure.notBlank(name);

    Object oldValue;
    if (newValue == null) {
      oldValue = m_props.remove(name);
    }
    else {
      oldValue = m_props.put(name, newValue);
    }

    return firePropertyChange(name, oldValue, newValue);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    if (listener instanceof PropertyChangeListenerProxy) {
      PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
      addPropertyChangeListener(proxy.getPropertyName(), proxy.getListener());
      return;
    }

    synchronized (m_props) {
      if (m_listeners == null) {
        m_listeners = new EventListenerList();
      }
      m_listeners.add(listener);
    }
  }

  public boolean removePropertyChangeListener(PropertyChangeListener listener) {
    if (listener instanceof PropertyChangeListenerProxy) {
      PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
      // Call two argument remove method.
      return removePropertyChangeListener(proxy.getPropertyName(), proxy.getListener());
    }

    synchronized (m_props) {
      boolean removed = false;
      if (m_listeners != null) {
        removed = m_listeners.remove(listener);
        if (m_listeners.isEmpty()) {
          m_listeners = null; // not used anymore
        }
      }

      if (m_childListeners != null) {
        Iterator<EventListenerList> iterator = m_childListeners.values().iterator();
        while (iterator.hasNext()) {
          EventListenerList list = iterator.next();
          if (list.remove(listener) && list.isEmpty()) {
            iterator.remove();
            removed = true;
          }
        }

        if (m_childListeners.isEmpty()) {
          m_childListeners = null;
        }
      }
      return removed;
    }
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    Ensure.notBlank(propertyName);
    Ensure.notNull(listener);

    synchronized (m_props) {
      if (m_childListeners == null) {
        m_childListeners = new HashMap<>();
      }
      m_childListeners
          .computeIfAbsent(propertyName, key -> new EventListenerList())
          .add(listener);
    }
  }

  public boolean removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    Ensure.notBlank(propertyName);
    Ensure.notNull(listener);

    synchronized (m_props) {
      if (m_childListeners == null) {
        return false;
      }

      EventListenerList childList = m_childListeners.get(propertyName);
      boolean removed = childList.remove(listener);
      if (removed && childList.isEmpty()) {
        // remove the map entry as there are no more listeners for that property
        m_childListeners.remove(propertyName);

        if (m_childListeners.isEmpty()) {
          m_childListeners = null; // not needed anymore
        }
      }
      return removed;
    }
  }

  public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (Objects.equals(oldValue, newValue)) {
      return false;
    }
    PropertyChangeEvent e = new PropertyChangeEvent(m_source, propertyName, oldValue, newValue);
    firePropertyChange(e);
    return true;
  }

  public void firePropertyChange(PropertyChangeEvent e) {
    if (e == null) {
      return;
    }

    Collection<PropertyChangeListener> targets = new ArrayList<>();
    String propertyName = e.getPropertyName();

    synchronized (m_props) {
      if (m_listeners != null) {
        if (m_listeners.isEmpty()) {
          m_listeners = null;
        }
        else {
          targets.addAll(m_listeners.get(PropertyChangeListener.class));
        }
      }
      if (propertyName != null && m_childListeners != null) {
        EventListenerList propertyListenerList = m_childListeners.get(propertyName);
        if (propertyListenerList != null) {
          targets.addAll(propertyListenerList.get(PropertyChangeListener.class));
        }
      }
    }

    if (targets.isEmpty()) {
      return;
    }

    for (PropertyChangeListener listener : targets) {
      listener.propertyChange(e);
    }
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + m_props.hashCode();
    result = prime * result + (m_source == this ? 0 : m_source.hashCode());
    return result;
  }

  protected boolean isSourceThis() {
    return m_source == this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    PropertySupport other = (PropertySupport) obj;
    if (!m_props.equals(other.m_props)) {
      return false;
    }
    if (isSourceThis() != other.isSourceThis()) {
      return false;
    }
    if (isSourceThis()) {
      return true;
    }
    return Objects.equals(m_source, other.m_source);
  }
}
