/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

/**
 * <h3>{@link EventListenerList}</h3>
 * <p>
 * List of {@link EventListener}s supporting weak listeners by implementing {@link IWeakEventListener}. This class is
 * thread-safe.
 *
 * @since 7.0.0
 */
public final class EventListenerList {

  private List<Object> m_listeners;
  private final Object m_lock = new Object();

  private List<Object> getOrCreateList() {
    if (m_listeners == null) {
      m_listeners = new ArrayList<>();
    }
    return m_listeners;
  }

  /**
   * Removes all listeners.
   */
  public void clear() {
    synchronized (m_lock) {
      if (m_listeners == null) {
        return;
      }
      m_listeners.clear();
      m_listeners = null;
    }
  }

  /**
   * Adds the specified listener. If the specified listener implements {@link IWeakEventListener} it is added weakly.
   *
   * @param listener
   *          The listener to add. Must not be {@code null}.
   */
  public void add(EventListener listener) {
    synchronized (m_lock) {
      if (listener instanceof IWeakEventListener) {
        getOrCreateList().add(new WeakReference<>(listener));
      }
      else {
        getOrCreateList().add(Ensure.notNull(listener));
      }
    }
  }

  /**
   * @return Gets all {@link EventListener}s as {@link Collection} of {@link EventListener}s.
   */
  public Collection<EventListener> get() {
    return get(EventListener.class);
  }

  /**
   * Gets all listeners of the specified type.
   *
   * @param type
   *          The type or {@code null} if all should be returned.
   * @return The matching listeners as unmodifiable {@link Collection}.
   */
  @SuppressWarnings("unchecked")
  public <T extends EventListener> Collection<T> get(Class<T> type) {
    Collection<Object> candidates;
    synchronized (m_lock) {
      if (m_listeners == null) {
        return emptyList();
      }

      candidates = new ArrayList<>(m_listeners.size());
      Iterator<Object> iterator = m_listeners.iterator();
      while (iterator.hasNext()) {
        Object entry = iterator.next();
        if (entry instanceof Reference<?>) {
          Object inner = ((Reference<?>) entry).get();
          if (inner == null) {
            iterator.remove(); // housekeeping: remove weak-listener that was reclaimed
          }
          else {
            candidates.add(inner);
          }
        }
        else {
          candidates.add(entry);
        }
      }
    }

    Collection<T> result = new ArrayList<>(candidates.size());
    for (Object candidate : candidates) {
      if (type == null || type.isAssignableFrom(candidate.getClass())) {
        result.add((T) candidate);
      }
    }
    return unmodifiableCollection(result);
  }

  /**
   * Removes the specified listener from this list.
   *
   * @param listenerToRemove
   *          The listener to remove. This method has no effect if the listener is {@code null}.
   * @return {@code true} if it was removed successfully. {@code false} otherwise.
   */
  public boolean remove(EventListener listenerToRemove) {
    synchronized (m_lock) {
      if (m_listeners == null || listenerToRemove == null) {
        return false;
      }

      boolean removed = false;
      Iterator<Object> iterator = m_listeners.iterator();
      while (iterator.hasNext()) {
        Object entry = iterator.next();
        if (entry == listenerToRemove) {
          iterator.remove();
          removed = true;
        }
        else if (entry instanceof Reference<?>) {
          Object inner = ((Reference<?>) entry).get();
          if (inner == null || inner == listenerToRemove) {
            iterator.remove(); // housekeeping: remove weak-listener that was reclaimed
            removed = true;
          }
        }
      }

      if (removed && m_listeners.isEmpty()) {
        m_listeners = null;
      }
      return removed;
    }
  }

  /**
   * @return The number of listeners in this list.
   */
  public int size() {
    synchronized (m_lock) {
      if (m_listeners == null) {
        return 0;
      }
      return m_listeners.size();
    }
  }

  /**
   * @return {@code true} if there are no listeners in this list. {@code false} otherwise.
   */
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * <h3>{@link IWeakEventListener}</h3>
   * <p>
   * Marker interface for {@link EventListener}s that should be added as {@link WeakReference}. Such listeners will
   * automatically be removed from the list as soon as they are no longer referenced.
   * <p>
   * <b>Important:</b><br>
   * Ensure to keep an explicit reference to the listener as long as needed. Otherwise it will be collected too early!
   *
   * @since 7.0.0
   */
  public interface IWeakEventListener extends EventListener {
  }
}
