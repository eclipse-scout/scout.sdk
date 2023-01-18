/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.spi;

import java.lang.ref.WeakReference;
import java.util.function.Function;

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

  public final API wrap() {
    var api = getExistingApi();
    if (api == null) {
      api = internalCreateApi();
      internalSetApi(api);
    }
    return api;
  }

  /**
   * Sets a new api element for this spi.
   * 
   * @param api
   *          The new api element. May be {@code null}.
   * @return The previous api element or {@code null}.
   */
  public final API internalSetApi(API api) {
    var previous = getExistingApi();
    if (api == null) {
      m_apiRef = null;
    }
    else {
      m_apiRef = new WeakReference<>(api);
    }
    return previous;
  }

  /**
   * Resolves this element new using {@link #internalFindNewElement()} and calls the {@link Function} given if an
   * element was found.
   * 
   * @param function
   *          The function to call. The input to the function is never {@code null}.
   * @return The result of the function call.
   */
  @SuppressWarnings("unchecked")
  protected <T, R> R withNewElement(Function<T, R> function) {
    var newSpi = internalFindNewElement();
    if (newSpi == null) {
      // after a new resolve the item can always be missing afterwards (e.g. it was deleted in the source)
      return null;
    }
    return function.apply((T) newSpi);
  }

  /**
   * @return The existing api element for this SPI if existing. {@code null} otherwise.
   */
  public final API getExistingApi() {
    var existing = m_apiRef;
    if (existing == null) {
      return null;
    }
    return existing.get();
  }

  protected abstract API internalCreateApi();

  public abstract JavaElementSpi internalFindNewElement();
}
