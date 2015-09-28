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
package org.eclipse.scout.sdk.core.model.api.internal;

import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;

/**
 * <h3>{@link AbstractJavaElementImplementor}</h3>Represents a Java element.
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public abstract class AbstractJavaElementImplementor<SPI extends JavaElementSpi> implements IJavaElement {
  protected SPI m_spi;

  protected AbstractJavaElementImplementor(SPI spi) {
    m_spi = spi;
  }

  @Override
  public IJavaEnvironment javaEnvironment() {
    return m_spi.getJavaEnvironment().wrap();
  }

  @Override
  public final String elementName() {
    return m_spi.getElementName();
  }

  @Override
  public ISourceRange source() {
    return m_spi.getSource();
  }

  @Override
  public SPI unwrap() {
    return m_spi;
  }

  @Override
  public final int hashCode() {
    return m_spi.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    return obj == this;
  }

  @Override
  public abstract String toString();

  @SuppressWarnings("unchecked")
  public void internalSetSpi(JavaElementSpi spi) {
    m_spi = (SPI) spi;
  }

}
