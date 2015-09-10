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
    m_name = type.getName();
    m_package = type.getPackage();
    m_simpleName = type.getSimpleName();
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
  public String getName() {
    return m_name;
  }

  @Override
  public IPackage getPackage() {
    return m_package;
  }

  /**
   * @return the simple name
   */
  @Override
  public String getSimpleName() {
    return m_simpleName;
  }

  @Override
  public String getSignature() {
    return Signature.createTypeSignature(getName());
  }

  /**
   * @return true if {@link #getClassX()} returns not null
   */
  @Override
  public boolean exists() {
    return getType() != null;
  }

  @Override
  public IType getType() {
    return m_type;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
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
    return this.getName().equals(other.getName());
  }

}
