/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.model.api.internal;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.IUnresolvedType;
import org.eclipse.scout.sdk.core.signature.Signature;

public class UnresolvedTypeImplementor implements IUnresolvedType {
  private final IType m_type;
  private final String m_name;
  private final IPackage m_package;
  private final String m_simpleName;

  UnresolvedTypeImplementor(IType type) {
    m_type = type;
    m_name = type.name();
    m_package = type.containingPackage();
    m_simpleName = type.elementName();
  }

  UnresolvedTypeImplementor(IJavaEnvironment env, String name) {
    m_type = null;
    m_name = name;
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

  /**
   * @return the fully qualified name
   */
  @Override
  public String name() {
    return m_name;
  }

  @Override
  public IPackage containingPackage() {
    return m_package;
  }

  /**
   * @return the simple name
   */
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

}
