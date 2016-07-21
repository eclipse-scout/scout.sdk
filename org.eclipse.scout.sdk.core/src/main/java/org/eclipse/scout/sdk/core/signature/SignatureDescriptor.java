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
package org.eclipse.scout.sdk.core.signature;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * <h3>{@link SignatureDescriptor}</h3> Helper class to access signature meta data.
 *
 * @author Ivan Motsch
 * @since 5.2.0
 */
public class SignatureDescriptor {
  private final String m_signature;
  private final String m_packageName;
  private final String m_simpleName;
  private final String m_qualifier;
  private final String m_qualifiedName;

  public SignatureDescriptor(String signature) {
    m_signature = Validate.notNull(signature);
    if (isBaseType()) {
      m_packageName = null;
      m_simpleName = Signature.getSignatureSimpleName(signature);
      m_qualifier = null;
      m_qualifiedName = m_simpleName;
    }
    else if (isUnresolved()) {
      m_packageName = null;
      m_simpleName = Signature.toString(signature);
      m_qualifier = null;
      m_qualifiedName = m_simpleName;
    }
    else {
      String sigWithoutDollar = signature.replace(ISignatureConstants.C_DOLLAR, ISignatureConstants.C_DOT);
      m_qualifier = Signature.getSignatureQualifier(sigWithoutDollar);
      m_packageName = Signature.getSignatureQualifier(signature);
      m_simpleName = Signature.getSignatureSimpleName(sigWithoutDollar);
      m_qualifiedName = StringUtils.isEmpty(m_qualifier) ? m_simpleName : m_qualifier + '.' + m_simpleName;
    }
  }

  /**
   * @return <code>true</code> if the signature described by this helper is a base type (primitive or void type).
   *         <code>false</code> otherwise.
   */
  public boolean isBaseType() {
    return Signature.getTypeSignatureKind(m_signature) == ISignatureConstants.BASE_TYPE_SIGNATURE;
  }

  /**
   * @return <code>true</code> if the signature described by this helper is unresolved. <code>false</code> otherwise.
   */
  public boolean isUnresolved() {
    return SignatureUtils.isUnresolved(m_signature);
  }

  /**
   * Gets the original wrapped signature.
   *
   * @return The signature this helper was created with.
   */
  public String getSignature() {
    return m_signature;
  }

  /**
   * @return a.b.c
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
   * @return a.b.c.MyClass$InnerClass$SecondInner -> a.b.c.MyClass.InnerClass
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
    StringBuilder builder = new StringBuilder();
    builder.append("SignatureDescriptor [");
    builder.append("signature=").append(m_signature).append(", ");
    if (m_packageName != null) {
      builder.append("packageName=").append(m_packageName).append(", ");
    }
    if (m_simpleName != null) {
      builder.append("simpleName=").append(m_simpleName).append(", ");
    }
    if (m_qualifier != null) {
      builder.append("qualifier=").append(m_qualifier).append(", ");
    }
    if (m_qualifiedName != null) {
      builder.append("qualifiedName=").append(m_qualifiedName);
    }
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    return m_signature.hashCode();
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
    SignatureDescriptor other = (SignatureDescriptor) obj;
    return Objects.equals(m_signature, other.m_signature);
  }
}
