/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <h3>{@link BasicPropertySupport}</h3> Property map supporting property change event listeners.
 *
 * @since 5.1.0
 * @see IWeakEventListener
 */
public class BasicPropertySupport {

  public static final int DEFAULT_INT_VALUE = 0;
  public static final int DEFAULT_DOUBLE_VALUE = 0;
  public static final Integer DEFAULT_INT = Integer.valueOf(DEFAULT_INT_VALUE);
  public static final Double DEFAULT_DOUBLE = Double.valueOf(DEFAULT_DOUBLE_VALUE);
  public static final long DEFAULT_LONG_VALUE = DEFAULT_INT_VALUE;
  public static final Long DEFAULT_LONG = Long.valueOf(DEFAULT_LONG_VALUE);
  public static final Boolean DEFAULT_BOOL = Boolean.FALSE;

  private final Map<String, Object> m_props;
  private final Object m_source;
  private final Object m_listenerLock;
  private List<Object> m_listeners; // lazy created
  private Map<String, List<Object>> m_childListeners;
  private int m_propertiesChanging;
  private List<PropertyChangeEvent> m_propertyEventBuffer;

  public BasicPropertySupport(Object sourceBean) {
    m_source = sourceBean;
    m_props = new HashMap<>();
    m_listenerLock = new Object();
  }

  public boolean isPropertiesChanging() {
    return m_propertiesChanging > 0;
  }

  public void setPropertiesChanging(boolean b) {
    // use a stack counter because setTableChanging might be called in nested loops
    if (b) {
      m_propertiesChanging++;
    }
    else {
      if (m_propertiesChanging > 0) {
        m_propertiesChanging--;
        if (m_propertiesChanging == 0) {
          processChangeBuffer();
        }
      }
    }
  }

  public void clearProperties() {
    m_props.clear();
  }

  public Map<String, Object> getPropertiesMap() {
    synchronized (m_listenerLock) {
      return new HashMap<>(m_props);
    }
  }

  public void putPropertiesMap(Map<String, Object> map) {
    m_props.putAll(map);
  }

  public boolean hasProperty(String name) {
    return m_props.containsKey(name);
  }

  public boolean setPropertyInt(String name, int i) {
    return setProperty(name, Integer.valueOf(i), DEFAULT_INT);
  }

  public int getPropertyInt(String name) {
    Number n = (Number) getProperty(name);
    if (n == null) {
      return DEFAULT_INT_VALUE;
    }
    return n.intValue();
  }

  public boolean setPropertyDouble(String name, double d) {
    return setProperty(name, new Double(d), DEFAULT_DOUBLE);
  }

  public double getPropertyDouble(String name) {
    Number n = (Number) getProperty(name);
    if (n == null) {
      return DEFAULT_DOUBLE_VALUE;
    }
    return n.doubleValue();
  }

  public boolean setPropertyLong(String name, long i) {
    return setProperty(name, Long.valueOf(i), DEFAULT_LONG);
  }

  public long getPropertyLong(String name) {
    Number n = (Number) getProperty(name);
    if (n == null) {
      return DEFAULT_LONG_VALUE;
    }
    return n.longValue();
  }

  public boolean setPropertyBool(String name, boolean b) {
    return setProperty(name, Boolean.valueOf(b), DEFAULT_BOOL);
  }

  public boolean getPropertyBool(String name) {
    Boolean b = (Boolean) getProperty(name);
    if (b == null) {
      return DEFAULT_BOOL.booleanValue();
    }
    return b.booleanValue();
  }

  public boolean setPropertyString(String name, String s) {
    return setProperty(name, s);
  }

  public String getPropertyString(String name) {
    String s = (String) getProperty(name);
    return s;
  }

  public Object getProperty(String name) {
    return m_props.get(name);
  }

  public <T> boolean setPropertyList(String name, List<T> newValue) {
    return setPropertyList(name, newValue, false);
  }

  public <T> boolean setPropertyListAlwaysFire(String name, List<T> newValue) {
    return setPropertyList(name, newValue, true);
  }

  private <T> boolean setPropertyList(String name, List<T> newValue, boolean alwaysFire) {
    Object oldValue = m_props.get(name);
    boolean propChanged = setPropertyNoFire(name, newValue);
    if (propChanged || alwaysFire) {
      Object eventOldValue = null;
      if (oldValue instanceof List) {
        eventOldValue = new ArrayList<>((List<?>) oldValue);
      }
      // fire a copy
      List<T> eventNewValue = null;
      if (newValue != null) {
        eventNewValue = new ArrayList<>(newValue);
      }
      firePropertyChangeImpl(name, eventOldValue, eventNewValue);
      return propChanged;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getPropertyList(String name) {
    return (List<T>) m_props.get(name);
  }

  public <T> boolean setPropertySet(String name, Set<T> newValue) {
    return setPropertySet(name, newValue, false);
  }

  public <T> boolean setPropertySetAlwaysFire(String name, Set<T> newValue) {
    return setPropertySet(name, newValue, true);
  }

  private <T> boolean setPropertySet(String name, Set<T> newValue, boolean alwaysFire) {
    Object oldValue = m_props.get(name);
    boolean propChanged = setPropertyNoFire(name, newValue);
    if (propChanged || alwaysFire) {
      Object eventOldValue = null;
      if (oldValue instanceof Set) {
        eventOldValue = new HashSet<>((Set<?>) oldValue);
      }
      // fire a copy
      Set<T> eventNewValue = null;
      if (newValue != null) {
        eventNewValue = new HashSet<>(newValue);
      }
      firePropertyChangeImpl(name, eventOldValue, eventNewValue);
      return propChanged;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <T> Set<T> getPropertySet(String name) {
    return (Set<T>) m_props.get(name);
  }

  public boolean setProperty(String name, Object newValue) {
    return setProperty(name, newValue, null);
  }

  public boolean/* changed */ setPropertyNoFire(String name, Object newValue) {
    Object oldValue = m_props.get(name);
    m_props.put(name, newValue);
    return !Objects.equals(oldValue, newValue);
  }

  /**
   * DESIGN: should remove property if set to null - see hasProperty() as well
   */
  public boolean setProperty(String name, Object newValue, Object defaultOldValueWhenNull) {
    Object oldValue = m_props.get(name);
    if (oldValue == null) {
      oldValue = defaultOldValueWhenNull;
    }
    m_props.put(name, newValue);
    if (Objects.equals(oldValue, newValue)) {
      // no change
      return false;
    }

    firePropertyChangeImpl(name, oldValue, newValue);
    return true;
  }

  /**
   * Implementation
   */

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    if (listener instanceof PropertyChangeListenerProxy) {
      PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
      // Call two argument add method.
      addPropertyChangeListener(proxy.getPropertyName(), proxy.getListener());
    }
    else {
      synchronized (m_listenerLock) {
        if (m_listeners == null) {
          m_listeners = new ArrayList<>();
        }
        if (listener instanceof IWeakEventListener) {
          m_listeners.add(new WeakReference<>(listener));
        }
        else {
          m_listeners.add(listener);
        }
      }
    }
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    if (listener instanceof PropertyChangeListenerProxy) {
      PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
      // Call two argument remove method.
      removePropertyChangeListener(proxy.getPropertyName(), proxy.getListener());
    }
    else {
      synchronized (m_listenerLock) {
        removeFromListNoLock(m_listeners, listener);
        if (m_childListeners != null) {
          for (List<Object> childList : m_childListeners.values()) {
            removeFromListNoLock(childList, listener);
          }
        }
      }
    }
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    synchronized (m_listenerLock) {
      if (m_childListeners == null) {
        m_childListeners = new HashMap<>();
      }
      List<Object> children = m_childListeners.get(propertyName);
      if (children == null) {
        children = new ArrayList<>();
        m_childListeners.put(propertyName, children);
      }
      if (listener instanceof IWeakEventListener) {
        children.add(new WeakReference<>(listener));
      }
      else {
        children.add(listener);
      }
    }
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    synchronized (m_listenerLock) {
      if (m_childListeners != null) {
        List<Object> childList = m_childListeners.get(propertyName);
        if (childList != null) {
          removeFromListNoLock(childList, listener);
        }
      }
    }
  }

  private static void removeFromListNoLock(List<Object> listeners, PropertyChangeListener listener) {
    if (listeners == null) {
      return;
    }
    if (listener instanceof IWeakEventListener) {
      for (int i = 0, n = listeners.size(); i < n; i++) {
        Object o = listeners.get(i);
        if (o instanceof WeakReference && ((WeakReference<?>) o).get() == listener) {
          listeners.remove(i);
          break;
        }
      }
    }
    else {
      listeners.remove(listener);
    }
    if (listeners.size() == 0 && listeners instanceof ArrayList) {
      ((ArrayList<?>) listeners).trimToSize();
    }
  }

  public void firePropertyChange(PropertyChangeEvent e) {
    firePropertyChangeImpl(e);
  }

  public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    if (oldValue == newValue) {
      return;
    }
    firePropertyChangeImpl(propertyName, Integer.valueOf(oldValue), Integer.valueOf(newValue));
  }

  public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    if (oldValue == newValue) {
      return;
    }
    firePropertyChangeImpl(propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
  }

  public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (Objects.equals(oldValue, newValue)) {
      return;
    }
    firePropertyChangeImpl(propertyName, oldValue, newValue);
  }

  private void firePropertyChangeImpl(String propertyName, Object oldValue, Object newValue) {
    List<Object> l = m_listeners;
    Map<String, List<Object>> m = m_childListeners;
    if ((l != null && l.size() > 0) || (m != null && m.size() > 0)) {
      PropertyChangeEvent e = new PropertyChangeEvent(m_source, propertyName, oldValue, newValue);
      firePropertyChangeImpl(e);
    }
  }

  private void firePropertyChangeImpl(PropertyChangeEvent e) {
    if (e == null) {
      return;
    }
    //
    if (isPropertiesChanging()) {
      // buffer the event for later batch firing
      synchronized (m_listenerLock) {
        if (m_propertyEventBuffer == null) {
          m_propertyEventBuffer = new ArrayList<>();
        }
        m_propertyEventBuffer.add(e);
      }
    }
    else {
      List<PropertyChangeListener> targets = new ArrayList<>(4);
      synchronized (m_listenerLock) {
        if (m_listeners != null) {
          for (Object o : m_listeners) {
            if (o instanceof WeakReference) {
              o = ((WeakReference<?>) o).get();
            }
            if (o != null) {
              targets.add((PropertyChangeListener) o);
            }
          }
        }
        String propertyName = e.getPropertyName();
        if (propertyName != null && m_childListeners != null) {
          List<Object> childListeners = m_childListeners.get(propertyName);
          if (childListeners != null) {
            for (Object o : childListeners) {
              if (o instanceof WeakReference) {
                o = ((WeakReference<?>) o).get();
              }
              if (o != null) {
                targets.add((PropertyChangeListener) o);
              }
            }
          }
        }
      }// end synchronized
      if (targets.size() > 0) {
        for (PropertyChangeListener listener : targets) {
          listener.propertyChange(e);
        }
      }
    }
  }

  private void processChangeBuffer() {
    /*
     * fire events property changes are finished now fire all buffered events in
     * one batch
     */
    PropertyChangeEvent[] a = null;
    synchronized (m_listenerLock) {
      if (m_propertyEventBuffer != null) {
        a = m_propertyEventBuffer.toArray(new PropertyChangeEvent[m_propertyEventBuffer.size()]);
      }
      m_propertyEventBuffer = null;
    }
    if (a != null && a.length > 0) {
      // coalesce by names
      List<PropertyChangeEvent> coalesceList = new LinkedList<>();
      Set<String> names = new HashSet<>();
      // reverse traversal
      for (int i = a.length - 1; i >= 0; i--) {
        if (!names.contains(a[i].getPropertyName())) {
          coalesceList.add(0, a[i]);
          names.add(a[i].getPropertyName());
        }
      }
      for (PropertyChangeEvent e : coalesceList) {
        firePropertyChangeImpl(e);
      }
    }
  }

  public boolean hasListeners(String propertyName) {
    synchronized (m_listenerLock) {
      List<Object> l0 = m_listeners;
      List<Object> l1 = null;
      if (propertyName != null) {
        l1 = m_childListeners.get(propertyName);
      }
      int count = (l0 != null ? l0.size() : 0) + (l1 != null ? l1.size() : 0);
      return count > 0;
    }
  }
}
