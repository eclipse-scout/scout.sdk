/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.internal.AbstractJavaElementImplementor;
import org.eclipse.scout.sdk.core.model.api.internal.JavaEnvironmentImplementor;

/**
 * <h3>{@link AbstractJavaEnvironment}</h3>
 *
 * @since 7.0.0
 */
public abstract class AbstractJavaEnvironment implements JavaEnvironmentSpi {
  protected static final Object NULL_OBJECT = new Object();

  private final AtomicInteger m_hashSeq;
  private final Map<String, Object> m_typeCache;
  private final JavaEnvironmentImplementor m_api;

  protected AbstractJavaEnvironment() {
    m_typeCache = new HashMap<>();
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

  /**
   * Performs a search for the given fqn
   *
   * @param fqn
   *          The fully qualified type name
   * @return The {@link TypeSpi} or {@code null} if it could not be found.
   */
  protected abstract TypeSpi doFindType(String fqn);

  protected Object doFindTypeInternal(String fqn) {
    Object result = doFindType(fqn);
    if (result == null) {
      return NULL_OBJECT;
    }
    return result;
  }

  protected Map<String, Object> getTypeCache() {
    return m_typeCache;
  }

  @Override
  public synchronized TypeSpi findType(String fqn) {
    Object elem = m_typeCache.computeIfAbsent(fqn, this::doFindTypeInternal);
    if (elem == NULL_OBJECT) {
      return null;
    }
    return (TypeSpi) elem;
  }

  protected abstract Collection<JavaElementSpi> allElements();

  protected abstract void reinitialize();

  @Override
  @SuppressWarnings("unchecked")
  public synchronized void reload() {
    Iterable<JavaElementSpi> detachedSpiElements = new ArrayList<>(allElements());

    reinitialize();
    m_api.spiChanged(); // flush caches

    // reconnect all new SPI/API mappings
    for (JavaElementSpi old : detachedSpiElements) {
      AbstractSpiElement<? extends IJavaElement> oldSpiElement = (AbstractSpiElement<? extends IJavaElement>) old;
      JavaElementSpi newSpiElement = oldSpiElement.internalFindNewElement();

      AbstractJavaElementImplementor<JavaElementSpi> apiElement = (AbstractJavaElementImplementor<JavaElementSpi>) oldSpiElement.wrap();
      apiElement.internalSetSpi(newSpiElement);
      if (newSpiElement != null) {
        ((AbstractSpiElement<IJavaElement>) newSpiElement).internalSetApi(apiElement);
      }

      oldSpiElement.internalSetApi(null); // detach old SPI from API
    }
  }
}
