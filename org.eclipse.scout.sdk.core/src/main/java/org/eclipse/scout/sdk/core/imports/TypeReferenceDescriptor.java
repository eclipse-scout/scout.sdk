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
package org.eclipse.scout.sdk.core.imports;

import static org.eclipse.scout.sdk.core.util.JavaTypes.isPrimitive;
import static org.eclipse.scout.sdk.core.util.JavaTypes.qualifier;
import static org.eclipse.scout.sdk.core.util.JavaTypes.simpleName;
import static org.eclipse.scout.sdk.core.util.Strings.isBlank;
import static org.eclipse.scout.sdk.core.util.Strings.isEmpty;
import static org.eclipse.scout.sdk.core.util.Strings.replace;

import java.util.Objects;

import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link TypeReferenceDescriptor}</h3>
 *
 * @since 7.0.0
 */
class TypeReferenceDescriptor {
  private final String m_packageName;
  private final String m_simpleName;
  private final String m_qualifier;
  private final String m_qualifiedName;
  private final boolean m_isTypeArg;
  private final boolean m_isBaseType;

  TypeReferenceDescriptor(CharSequence fqn) {
    this(fqn, false);
  }

  TypeReferenceDescriptor(CharSequence fqn, boolean isTypeArg) {
    m_isTypeArg = isTypeArg;
    m_isBaseType = isPrimitive(fqn);
    if (isBaseType()) {
      m_packageName = null;
      m_simpleName = fqn.toString();
      m_qualifier = null;
      m_qualifiedName = m_simpleName;
    }
    else {
      var nameWithoutDollar = replace(fqn, JavaTypes.C_DOLLAR, JavaTypes.C_DOT);
      var qualifier = qualifier(fqn);
      var qualifierFromNameWithoutDollar = qualifier(nameWithoutDollar);
      m_qualifier = isBlank(qualifierFromNameWithoutDollar) ? null : qualifierFromNameWithoutDollar;
      m_packageName = isBlank(qualifier) ? null : qualifier;
      m_simpleName = simpleName(nameWithoutDollar);
      m_qualifiedName = isEmpty(m_qualifier) ? m_simpleName : (m_qualifier + JavaTypes.C_DOT + m_simpleName);
    }
  }

  /**
   * @return {@code true} if the reference described by this helper is a base type (primitive or void type).
   *         {@code false} otherwise.
   */
  public boolean isBaseType() {
    return m_isBaseType;
  }

  public boolean isTypeArg() {
    return m_isTypeArg;
  }

  /**
   * @return a.b.c.MyClass$InnerClass$SecondInner -> a.b.c or {@code null} if no package is present.
   */
  public String getPackageName() {
    return m_packageName;
  }

  /**
   * @return a.b.c.MyClass$InnerClass$SecondInner -> SecondInner
   */
  public String getSimpleName() {
    return m_simpleName;
  }

  /**
   * @return a.b.c.MyClass$InnerClass$SecondInner -> a.b.c.MyClass.InnerClass or {@code null} if no qualifier is
   *         present.
   */
  public String getQualifier() {
    return m_qualifier;
  }

  /**
   * @return a.b.c.MyClass$InnerClass$SecondInner -> a.b.c.MyClass.InnerClass.SecondInner
   */
  public String getQualifiedName() {
    return m_qualifiedName;
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append("TypeReferenceDescriptor [");
    if (getPackageName() != null) {
      builder.append("packageName=").append(getPackageName()).append(", ");
    }
    builder.append("simpleName=").append(getSimpleName()).append(", ");
    if (getQualifier() != null) {
      builder.append("qualifier=").append(getQualifier()).append(", ");
    }
    builder.append("qualifiedName=").append(getQualifiedName()).append(", ")
        .append("isTypeArg=").append(isTypeArg()).append(", isBaseType=").append(isBaseType()).append(']');
    return builder.toString();
  }

  @Override
  public int hashCode() {
    var prime = 31;
    var result = 1;
    result = prime * result + (m_isTypeArg ? 1231 : 1237);
    result = prime * result + ((m_qualifier == null) ? 0 : m_qualifier.hashCode());
    result = prime * result + m_simpleName.hashCode();
    result = prime * result + ((m_packageName == null) ? 0 : m_packageName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    var other = (TypeReferenceDescriptor) obj;
    return m_isTypeArg == other.m_isTypeArg
        && Objects.equals(m_qualifier, other.m_qualifier)
        && Objects.equals(m_simpleName, other.m_simpleName)
        && Objects.equals(m_packageName, other.m_packageName);
  }

}
