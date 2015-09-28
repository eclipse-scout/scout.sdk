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

import org.eclipse.jdt.internal.compiler.batch.ClasspathLocation;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;

public class ClasspathWithJdt implements ClasspathSpi {
  private final ClasspathEntry m_cp;

  ClasspathWithJdt(ClasspathEntry cp) {
    m_cp = cp;
  }

  @Override
  public boolean isSource() {
    //1=source
    //2=binary
    //3=mixed or unknown (target/classes = 2 or 3)
    return (m_cp.getClasspath() instanceof ClasspathLocation) ? ((ClasspathLocation) m_cp.getClasspath()).getMode() == 1 : false;
  }

  @Override
  public String getPath() {
    return m_cp.getClasspath().getPath();
  }

  @Override
  public String getEncoding() {
    return m_cp.getEncoding();
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
    return this.isSource() == other.isSource() && this.getPath().equals(other.getPath());
  }
}
