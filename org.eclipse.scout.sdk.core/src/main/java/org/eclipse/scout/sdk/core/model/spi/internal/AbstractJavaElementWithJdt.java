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
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.lang.ref.WeakReference;

import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaEnvironmentSpi;

/**
 * <h3>{@link AbstractJavaElementWithJdt}</h3>Represents a Java element.
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public abstract class AbstractJavaElementWithJdt<API extends IJavaElement> implements JavaElementSpi {
  protected final JavaEnvironmentWithJdt m_env;
  private WeakReference<API> m_apiRef;
  private final int m_hashCode;

  protected AbstractJavaElementWithJdt(JavaEnvironmentWithJdt env) {
    m_env = env;
    m_hashCode = m_env.nextHashCode();
  }

  @Override
  public JavaEnvironmentSpi getJavaEnvironment() {
    return m_env;
  }

  @Override
  public final int hashCode() {
    return m_hashCode;
  }

  @Override
  public final boolean equals(Object obj) {
    return obj == this;
  }

  @Override
  public API wrap() {
    API api = m_apiRef != null ? m_apiRef.get() : null;
    if (api == null) {
      api = internalCreateApi();
      internalSetApi(api);
    }
    return api;
  }

  protected final void internalSetApi(API api) {
    m_apiRef = new WeakReference<>(api);
  }

  protected abstract API internalCreateApi();

  protected abstract JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv);

}
