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

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.IUnresolvedType;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.signature.Signature;

public class UnresolvedTypeImplementor implements IUnresolvedType {
  private final IType m_type;
  private final String m_name;
  private final IPackage m_package;
  private final String m_simpleName;
  private final IJavaEnvironment m_env;

  UnresolvedTypeImplementor(IType type) {
    m_type = type;
    m_name = type.name();
    m_package = type.containingPackage();
    m_simpleName = type.elementName();
    m_env = type.javaEnvironment();
  }

  UnresolvedTypeImplementor(IJavaEnvironment env, String name) {
    m_type = null;
    m_name = name;
    m_env = env;
    int dot = name.lastIndexOf('.');
    if (dot > 0) {
      m_package = env.unwrap().getPackage(name.substring(0, dot)).wrap();
      m_simpleName = name.substring(dot + 1);
    }
    else {
      m_package = env.unwrap().getPackage(null).wrap();
      m_simpleName = name;
    }
  }

  @Override
  public IJavaEnvironment javaEnvironment() {
    return m_env;
  }

  @Override
  public String name() {
    return m_name;
  }

  @Override
  public IPackage containingPackage() {
    return m_package;
  }

  @Override
  public String elementName() {
    return m_simpleName;
  }

  @Override
  public String signature() {
    return Signature.createTypeSignature(name());
  }

  /**
   * @return true if {@link #getClassX()} returns not null
   */
  @Override
  public boolean exists() {
    return type() != null;
  }

  @Override
  public IType type() {
    return m_type;
  }

  @Override
  public String toString() {
    return name();
  }

  @Override
  public int hashCode() {
    return name().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    UnresolvedTypeImplementor other = (UnresolvedTypeImplementor) obj;
    return this.name().equals(other.name());
  }

  @Override
  public ISourceRange source() {
    if (type() != null) {
      return type().source();
    }
    return null;
  }

  @Override
  public TypeSpi unwrap() {
    if (type() != null) {
      return type().unwrap();
    }
    return null;
  }
}
