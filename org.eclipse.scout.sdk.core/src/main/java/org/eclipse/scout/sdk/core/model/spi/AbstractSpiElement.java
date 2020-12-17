/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.spi;

import java.lang.ref.WeakReference;

/**
 * <h3>{@link AbstractSpiElement}</h3>
 *
 * @since 7.0.0
 */
public abstract class AbstractSpiElement<API> {
  private final JavaEnvironmentSpi m_env;

  private WeakReference<API> m_apiRef;

  protected AbstractSpiElement(JavaEnvironmentSpi env) {
    m_env = env;
  }

  public JavaEnvironmentSpi getJavaEnvironment() {
    return m_env;
  }

  public API wrap() {
    API api = null;
    var apiRef = m_apiRef;
    if (apiRef != null) {
      api = apiRef.get();
    }
    if (api == null) {
      api = internalCreateApi();
      internalSetApi(api);
    }
    return api;
  }

  public final void internalSetApi(API api) {
    m_apiRef = new WeakReference<>(api);
  }

  protected abstract API internalCreateApi();

  public abstract JavaElementSpi internalFindNewElement();
}
