/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.ecj;

import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.AbstractSpiElement;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;

/**
 * <h3>{@link AbstractJavaElementWithEcj}</h3>Represents a Java element.
 *
 * @since 5.1.0
 */
public abstract class AbstractJavaElementWithEcj<API extends IJavaElement> extends AbstractSpiElement<API> implements JavaElementSpi {

  private final int m_hashCode;

  protected AbstractJavaElementWithEcj(AbstractJavaEnvironment env) {
    super(env);
    m_hashCode = env.nextHashCode();
  }

  @Override
  public final int hashCode() {
    return m_hashCode;
  }

  @Override
  public final boolean equals(Object obj) {
    return obj == this;
  }

  protected JavaEnvironmentWithEcj javaEnvWithEcj() {
    return (JavaEnvironmentWithEcj) getJavaEnvironment();
  }
}
