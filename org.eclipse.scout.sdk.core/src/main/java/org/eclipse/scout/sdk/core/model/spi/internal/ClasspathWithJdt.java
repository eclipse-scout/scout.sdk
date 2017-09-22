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

import java.nio.file.Path;

import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;

public class ClasspathWithJdt implements ClasspathSpi {
  private final ClasspathEntry m_cp;

  ClasspathWithJdt(ClasspathEntry cp) {
    m_cp = cp;
  }

  @Override
  public int getMode() {
    return m_cp.mode();
  }

  @Override
  public Path getPath() {
    return m_cp.path();
  }

  @Override
  public String getEncoding() {
    return m_cp.encoding();
  }

  @Override
  public int hashCode() {
    return getPath().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    ClasspathWithJdt other = (ClasspathWithJdt) obj;
    return this.m_cp.equals(other.m_cp);
  }
}
