package org.eclipse.scout.sdk.core.importvalidator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;

public class ImportElementCandidate {
  private final String m_signature;
  private final String m_packageName;
  private final String m_simpleName;
  private final String m_qualifier;
  private final String m_qualifiedName;

  public ImportElementCandidate(String signature) {
    m_signature = signature;
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
      m_qualifiedName = StringUtils.isEmpty(m_qualifier) ? m_simpleName : m_qualifier + "." + m_simpleName;
    }
  }

  public boolean isBaseType() {
    return Signature.getTypeSignatureKind(m_signature) == ISignatureConstants.BASE_TYPE_SIGNATURE;
  }

  public boolean isUnresolved() {
    return SignatureUtils.isUnresolved(m_signature);
  }

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
}
