/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.sourcebuilder.type;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.importvalidator.ImportElementCandidate;
import org.eclipse.scout.sdk.core.importvalidator.WrappedImportValidator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.Signature;

/**
 * Do not instantiate this class directly, it is automatically created in
 * {@link ITypeSourceBuilder#createSource(StringBuilder, String, org.eclipse.scout.sdk.core.util.PropertyMap, IImportValidator)}
 * <p>
 * ignore imports when the referenced type is a member type of the enclosing type or superclasses of it
 */
public class EnclosingTypeScopedImportValidator extends WrappedImportValidator {
  private final String m_enclosingTypeName;
  private final Set<String> m_enclosedTypeNames;
  private final IType m_superType;

  public EnclosingTypeScopedImportValidator(IImportValidator inner, ITypeSourceBuilder enclosingTypeSrc) {
    super(inner);
    m_enclosingTypeName = enclosingTypeSrc.getElementName();
    m_enclosedTypeNames = new HashSet<>();
    for (ITypeSourceBuilder typeSrc : enclosingTypeSrc.getTypes()) {
      m_enclosedTypeNames.add(typeSrc.getElementName());
    }
    IJavaEnvironment env = getJavaEnvironment();
    String superSig = enclosingTypeSrc.getSuperTypeSignature();
    m_superType = (env != null && superSig != null ? env.findType(Signature.toString(superSig)) : null);
  }

  @Override
  public String getQualifier() {
    String q = super.getQualifier();
    return (q != null ? q + "." : "") + m_enclosingTypeName;
  }

  @Override
  public String checkCurrentScope(ImportElementCandidate cand) {
    //same qualifier
    if (Objects.equals(getQualifier(), cand.getQualifier())) {
      return cand.getSimpleName();
    }

    //same qualifier as superclass
    IType s = m_superType;
    while (s != null) {
      if (Objects.equals(s.getName(), cand.getQualifier())) {
        return cand.getSimpleName();
      }
      s = s.getSuperClass();
    }

    // check if simpleName (with other qualifier) exists in same enclosing type
    if (m_enclosedTypeNames.contains(cand.getSimpleName())) {
      //must qualify
      return cand.getQualifiedName();
    }

    return super.checkCurrentScope(cand);
  }

}
