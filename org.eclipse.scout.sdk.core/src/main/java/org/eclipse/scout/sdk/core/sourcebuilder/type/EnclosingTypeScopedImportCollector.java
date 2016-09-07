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
package org.eclipse.scout.sdk.core.sourcebuilder.type;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.importcollector.WrappedImportCollector;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureDescriptor;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;

/**
 * Do not instantiate this class directly, it is automatically created in
 * {@link ITypeSourceBuilder#createSource(StringBuilder, String, org.eclipse.scout.sdk.core.util.PropertyMap, IImportCollector)}
 * <p>
 * ignore imports when the referenced type is a member type of the enclosing type or superclasses of it
 */
public class EnclosingTypeScopedImportCollector extends WrappedImportCollector {
  private final String m_enclosingQualifier;
  //as far as these types exist already!
  private final Set<String> m_enclosingQualifiers = new HashSet<>();
  //as far as these types exist already!
  private final Set<String> m_enclosedSimpleNames = new HashSet<>();

  public EnclosingTypeScopedImportCollector(IImportCollector inner, ITypeSourceBuilder enclosingTypeSrc) {
    super(inner);
    m_enclosingQualifier = enclosingTypeSrc.getFullyQualifiedName();
    //self
    m_enclosingQualifiers.add(m_enclosingQualifier);

    //declared inner types
    for (ITypeSourceBuilder typeSrc : enclosingTypeSrc.getTypes()) {
      m_enclosedSimpleNames.add(typeSrc.getElementName());
    }

    //super types
    IJavaEnvironment env = getJavaEnvironment();
    if (env == null) {
      return;
    }

    Set<String> superSignatures = new HashSet<>(enclosingTypeSrc.getInterfaceSignatures().size() + 1);
    superSignatures.addAll(enclosingTypeSrc.getInterfaceSignatures());
    if (enclosingTypeSrc.getSuperTypeSignature() != null) {
      superSignatures.add(enclosingTypeSrc.getSuperTypeSignature());
    }

    for (String sig : superSignatures) {
      String qname = SignatureUtils.toFullyQualifiedName(Signature.getTypeErasure(sig));
      m_enclosingQualifiers.add(qname);
      IType t = env.findType(qname);
      if (t != null) {
        for (IType s : t.superTypes().list()) {
          String fqnName = s.name();
          if (IJavaRuntimeTypes.Object.equals(fqnName)) {
            continue;
          }
          m_enclosingQualifiers.add(fqnName);
          for (IType i : s.innerTypes().list()) {
            m_enclosedSimpleNames.add(i.elementName());
          }
        }
      }
    }
  }

  @Override
  public String getQualifier() {
    return m_enclosingQualifier;
  }

  @Override
  public String checkCurrentScope(SignatureDescriptor cand) {
    //same qualifier
    if (m_enclosingQualifiers.contains(cand.getQualifier())) {
      return cand.getSimpleName();
    }

    // check if simpleName (with other qualifier) exists in same enclosing type
    if (m_enclosedSimpleNames.contains(cand.getSimpleName())) {
      //must qualify
      return cand.getQualifiedName();
    }

    return super.checkCurrentScope(cand);
  }

}
