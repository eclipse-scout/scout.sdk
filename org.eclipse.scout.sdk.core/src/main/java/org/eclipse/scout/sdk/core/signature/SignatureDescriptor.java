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
    return "SignatureDescriptor [" + (m_signature != null ? "m_signature=" + m_signature + ", " : "") + (m_packageName != null ? "m_packageName=" + m_packageName + ", " : "")
        + (m_simpleName != null ? "m_simpleName=" + m_simpleName + ", " : "") + (m_qualifier != null ? "m_qualifier=" + m_qualifier + ", " : "") + (m_qualifiedName != null ? "m_qualifiedName=" + m_qualifiedName + ", " : "")
        + "isBaseType()=" + isBaseType() + ", isUnresolved()=" + isUnresolved() + "]";
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
    if (!(obj instanceof SignatureDescriptor)) {
      return false;
    }
    SignatureDescriptor other = (SignatureDescriptor) obj;
    return Objects.equals(m_signature, other.m_signature);
  }
}
