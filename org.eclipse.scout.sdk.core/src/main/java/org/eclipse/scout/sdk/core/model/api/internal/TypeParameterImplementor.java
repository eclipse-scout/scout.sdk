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

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.signature.Signature;

/**
 * <h3>{@link TypeParameterImplementor}</h3>
 *
 * @author Ivan Motsch
 * @since 4.1.0 09.11.2014
 */
public class TypeParameterImplementor extends AbstractJavaElementImplementor<TypeParameterSpi> implements ITypeParameter {

  private String m_signature;

  public TypeParameterImplementor(TypeParameterSpi spi) {
    super(spi);
  }

  @Override
  public IMember declaringMember() {
    return m_spi.getDeclaringMember().wrap();
  }

  @Override
  public List<IType> bounds() {
    return new WrappedList<>(m_spi.getBounds());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    JavaModelPrinter.print(this, sb);
    return sb.toString();
  }

  @Override
  public void internalSetSpi(JavaElementSpi spi) {
    super.internalSetSpi(spi);
    m_signature = null;
  }

  //additional convenience methods

  @Override
  public String signature() {
    if (m_signature == null) {
      List<IType> bounds = bounds();
      String[] boundSignatures = new String[bounds.size()];
      for (int i = 0; i < boundSignatures.length; i++) {
        boundSignatures[i] = bounds.get(i).signature();
      }
      m_signature = Signature.createTypeParameterSignature(elementName(), boundSignatures);
    }
    return m_signature;
  }
}
