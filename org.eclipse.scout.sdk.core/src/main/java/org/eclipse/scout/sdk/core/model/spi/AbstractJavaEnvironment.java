/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.spi;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.internal.AbstractJavaElementImplementor;
import org.eclipse.scout.sdk.core.model.api.internal.JavaEnvironmentImplementor;

/**
 * <h3>{@link AbstractJavaEnvironment}</h3>
 *
 * @since 7.0.0
 */
public abstract class AbstractJavaEnvironment implements JavaEnvironmentSpi {

  private static final Object NULL_OBJECT = new Object();

  private final AtomicInteger m_hashSeq;
  private final Map<String, Object> m_typeCache;
  private final JavaEnvironmentImplementor m_api;
  private final Object m_instanceLock;
  private final Map<AbstractJavaElementImplementor<JavaElementSpi>, Object> m_detachedApis;

  protected AbstractJavaEnvironment() {
    m_instanceLock = new Object();
    m_typeCache = new HashMap<>();
    m_detachedApis = new WeakHashMap<>();
    //noinspection ThisEscapedInObjectConstruction
    m_api = new JavaEnvironmentImplementor(this);
    m_hashSeq = new AtomicInteger();
  }

  @Override
  public IJavaEnvironment wrap() {
    return m_api;
  }

  public int nextHashCode() {
    return m_hashSeq.getAndIncrement();
  }

  @Override
  public TypeSpi findType(String fqn) {
    var elem = m_typeCache.get(fqn); // fast check without synchronizing
    if (elem == null) {
      synchronized (lock()) {
        elem = m_typeCache.computeIfAbsent(fqn, this::doFindTypeInternal);
      }
    }
    if (elem == NULL_OBJECT) {
      return null;
    }
    return (TypeSpi) elem;
  }

  private Object doFindTypeInternal(String fqn) {
    var result = doFindType(fqn);
    if (result == null) {
      return NULL_OBJECT;
    }
    return result;
  }

  /**
   * Performs a search for the given fqn
   *
   * @param fqn
   *          The fully qualified type name
   * @return The {@link TypeSpi} or {@code null} if it could not be found.
   */
  protected abstract TypeSpi doFindType(String fqn);

  /**
   * @return All {@link JavaElementSpi}s (except {@link IType}s which are stored in the subclass. May be a live list.
   */
  protected abstract Collection<JavaElementSpi> allElements();

  protected void cleanup() {
    synchronized (lock()) { // ensure instance lock is acquired because the clear of the map uses its own lock as well which might lead to deadlocks
      m_typeCache.clear();
      m_api.spiChanged();
    }
  }

  protected void onReloadStart() {
    cleanup();
  }

  protected void onReloadEnd() {
  }

  protected Object removeTypeFromCache(String fqn) {
    return m_typeCache.remove(fqn);
  }

  /**
   * @return The instance to used for java environment wide locking. Must never be {@code null}.
   */
  public Object lock() {
    return m_instanceLock;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void reload() {
    synchronized (lock()) {
      // this includes all TypeSPIs as well. So no need to include m_typeCache from here.
      Iterable<JavaElementSpi> detachedSpiElements = new ArrayList<>(allElements()); // create a new list here because the elements are cleared afterwards in reinitialize

      // includes all previously detached APIs including a reference to an SPI element that belongs to the currently closing compiler instance.
      var detachedApisBySpi = m_detachedApis
          .keySet().stream()
          .collect(groupingBy(IJavaElement::unwrap));

      try {
        onReloadStart();
        // reconnect all new SPI/API mappings
        for (var old : detachedSpiElements) {
          var oldSpiElement = (AbstractSpiElement<?>) old;
          var apiElement = (AbstractJavaElementImplementor<JavaElementSpi>) oldSpiElement.getExistingApi(); // do not call wrap() to never create a new one
          if (apiElement == null) {
            continue; // there is no api element. no need to resolve it anymore
          }

          var newSpiElement = oldSpiElement.internalFindNewElement();
          apiElement.internalSetSpi(newSpiElement);
          if (newSpiElement != null) {
            var newAbsSpiElement = (AbstractSpiElement<IJavaElement>) newSpiElement;
            var previousApi = (AbstractJavaElementImplementor<JavaElementSpi>) newAbsSpiElement.internalSetApi(apiElement);
            if (previousApi != null && previousApi != apiElement) {
              // An existing API has been overwritten. This would mean it is getting lost (the next reload cannot find it anymore and would not refresh its SPI).
              // To prevent this: remember for future reloads
              // This may happen if the internalFindNewElement returns the same new element for different old SPI elements.
              // Usually such collisions should not happen.
              m_detachedApis.put(previousApi, null);
            }
          }

          var detachedApisToUpdate = detachedApisBySpi.get(oldSpiElement);
          if (detachedApisToUpdate != null && !detachedApisToUpdate.isEmpty()) {
            detachedApisToUpdate.forEach(api -> api.internalSetSpi(newSpiElement));
          }

          oldSpiElement.internalSetApi(null); // detach old SPI from API
        }
      }
      finally {
        onReloadEnd();
      }
    }
  }
}
