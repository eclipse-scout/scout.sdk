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
package org.eclipse.scout.sdk.core.model.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

  protected AbstractJavaEnvironment() {
    m_instanceLock = new Object();
    m_typeCache = new ConcurrentHashMap<>();
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
    Object elem = m_typeCache.computeIfAbsent(fqn, this::doFindTypeInternal);
    if (elem == NULL_OBJECT) {
      return null;
    }
    return (TypeSpi) elem;
  }

  private Object doFindTypeInternal(String fqn) {
    TypeSpi result = doFindType(fqn);
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

  protected void reinitialize() {
    m_typeCache.clear();
    m_api.spiChanged();
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

  private static JavaElementSpi rootOf(TypeSpi type) {
    CompilationUnitSpi compilationUnit = type.getCompilationUnit();
    if (compilationUnit != null) {
      return compilationUnit;
    }
    return type;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void reload() {
    synchronized (lock()) {

      // this includes all TypeSPIs as well. So no need to include m_typeCache from here.
      Iterable<JavaElementSpi> detachedSpiElements = new ArrayList<>(allElements()); // create a new list here because the elements are cleared afterwards in reinitialize

      reinitialize();
      m_api.spiChanged(); // flush caches

      // reconnect all new SPI/API mappings
      for (JavaElementSpi old : detachedSpiElements) {
        AbstractSpiElement<? extends IJavaElement> oldSpiElement = (AbstractSpiElement<? extends IJavaElement>) old;
        AbstractJavaElementImplementor<JavaElementSpi> apiElement = (AbstractJavaElementImplementor<JavaElementSpi>) oldSpiElement.wrap();
        JavaElementSpi newSpiElement = oldSpiElement.internalFindNewElement();

        apiElement.internalSetSpi(newSpiElement);
        if (newSpiElement != null) {
          ((AbstractSpiElement<IJavaElement>) newSpiElement).internalSetApi(apiElement);
        }

        oldSpiElement.internalSetApi(null); // detach old SPI from API
      }
    }
  }
}
