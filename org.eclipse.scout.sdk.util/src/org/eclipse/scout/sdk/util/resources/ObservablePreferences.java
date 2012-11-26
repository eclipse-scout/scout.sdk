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
package org.eclipse.scout.sdk.util.resources;

import java.util.EventListener;

import org.eclipse.scout.commons.EventListenerList;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * <h3>{@link ObservablePreferences}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 23.11.2012
 */
public class ObservablePreferences implements Preferences {

  private final Preferences m_instance;
  private final EventListenerList m_eventListeners;

  public interface IFlushListener extends EventListener {
    void prefsFlushed();
  }

  public ObservablePreferences(Preferences inner) {
    m_instance = inner;
    m_eventListeners = new EventListenerList();
  }

  public void addFlushListener(IFlushListener listener) {
    m_eventListeners.add(IFlushListener.class, listener);
  }

  public void removeFlushListener(IFlushListener listener) {
    m_eventListeners.remove(IFlushListener.class, listener);
  }

  private void notifyFlush() {
    for (IFlushListener l : m_eventListeners.getListeners(IFlushListener.class)) {
      l.prefsFlushed();
    }
  }

  @Override
  public void put(String key, String value) {
    m_instance.put(key, value);
  }

  @Override
  public String get(String key, String def) {
    return m_instance.get(key, def);
  }

  @Override
  public void remove(String key) {
    m_instance.remove(key);
  }

  @Override
  public void clear() throws BackingStoreException {
    m_instance.clear();
  }

  @Override
  public void putInt(String key, int value) {
    m_instance.putInt(key, value);
  }

  @Override
  public int getInt(String key, int def) {
    return m_instance.getInt(key, def);
  }

  @Override
  public void putLong(String key, long value) {
    m_instance.putLong(key, value);
  }

  @Override
  public long getLong(String key, long def) {
    return m_instance.getLong(key, def);
  }

  @Override
  public void putBoolean(String key, boolean value) {
    m_instance.putBoolean(key, value);
  }

  @Override
  public boolean getBoolean(String key, boolean def) {
    return m_instance.getBoolean(key, def);
  }

  @Override
  public void putFloat(String key, float value) {
    m_instance.putFloat(key, value);
  }

  @Override
  public float getFloat(String key, float def) {
    return m_instance.getFloat(key, def);
  }

  @Override
  public void putDouble(String key, double value) {
    m_instance.putDouble(key, value);
  }

  @Override
  public double getDouble(String key, double def) {
    return m_instance.getDouble(key, def);
  }

  @Override
  public void putByteArray(String key, byte[] value) {
    m_instance.putByteArray(key, value);
  }

  @Override
  public byte[] getByteArray(String key, byte[] def) {
    return m_instance.getByteArray(key, def);
  }

  @Override
  public String[] keys() throws BackingStoreException {
    return m_instance.keys();
  }

  @Override
  public String[] childrenNames() throws BackingStoreException {
    return m_instance.childrenNames();
  }

  @Override
  public Preferences parent() {
    return m_instance.parent();
  }

  @Override
  public Preferences node(String pathName) {
    return m_instance.node(pathName);
  }

  @Override
  public boolean nodeExists(String pathName) throws BackingStoreException {
    return m_instance.nodeExists(pathName);
  }

  @Override
  public void removeNode() throws BackingStoreException {
    m_instance.removeNode();
  }

  @Override
  public String name() {
    return m_instance.name();
  }

  @Override
  public String absolutePath() {
    return m_instance.absolutePath();
  }

  @Override
  public void flush() throws BackingStoreException {
    m_instance.flush();
    notifyFlush();
  }

  @Override
  public void sync() throws BackingStoreException {
    m_instance.sync();
  }
}
