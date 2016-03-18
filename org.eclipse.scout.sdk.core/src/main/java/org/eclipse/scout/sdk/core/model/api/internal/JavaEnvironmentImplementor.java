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

import org.eclipse.scout.sdk.core.model.api.IFileLocator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.IUnresolvedType;
import org.eclipse.scout.sdk.core.model.api.MissingTypeException;
import org.eclipse.scout.sdk.core.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 * <h3>{@link JavaEnvironmentImplementor}</h3>
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class JavaEnvironmentImplementor implements IJavaEnvironment {
  private JavaEnvironmentSpi m_spi;

  public JavaEnvironmentImplementor(JavaEnvironmentSpi spi) {
    m_spi = spi;
  }

  @Override
  public IType findType(String fqn) {
    return wrapType(m_spi.findType(fqn));
  }

  static IType wrapType(TypeSpi spi) {
    return spi != null ? spi.wrap() : null;
  }

  @Override
  public IUnresolvedType findUnresolvedType(String fqn) {
    try {
      IType t = findType(fqn);
      if (t != null) {
        return new UnresolvedTypeImplementor(t);
      }
    }
    catch (MissingTypeException ex) {
      //nop
    }
    return new UnresolvedTypeImplementor(this, fqn);
  }

  @Override
  public IFileLocator getFileLocator() {
    return m_spi.getFileLocator();
  }

  @Override
  public void reload() {
    m_spi.reload();
  }

  @Override
  public boolean registerCompilationUnitOverride(String packageName, String fileName, StringBuilder buf) {
    char[] src = new char[buf.length()];
    buf.getChars(0, buf.length(), src, 0);
    return m_spi.registerCompilationUnitOverride(packageName, fileName, src);
  }

  @Override
  public String compileErrors(String fqn) {
    return m_spi.getCompileErrors(fqn);
  }

  @Override
  public JavaEnvironmentSpi unwrap() {
    return m_spi;
  }

  public void internalSetSpi(JavaEnvironmentSpi newSpi) {
    m_spi = newSpi;
  }

}
