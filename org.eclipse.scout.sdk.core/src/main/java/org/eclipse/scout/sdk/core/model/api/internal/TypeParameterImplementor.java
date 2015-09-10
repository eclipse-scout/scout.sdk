/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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

import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.sugar.AnnotationQuery;
import org.eclipse.scout.sdk.core.signature.Signature;

/**
 * <h3>{@link TypeParameterImplementor}</h3>
 *
 * @author Ivan Motsch
 * @since 4.1.0 09.11.2014
 */
public class TypeParameterImplementor extends AbstractAnnotatableImplementor<TypeParameterSpi>implements ITypeParameter {

  private String m_signature;

  public TypeParameterImplementor(TypeParameterSpi spi) {
    super(spi);
  }

  @Override
  public IMember getDeclaringMember() {
    return m_spi.getDeclaringMember().wrap();
  }

  @Override
  public List<IType> getBounds() {
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
  public String getSignature() {
    if (m_signature == null) {
      List<IType> bounds = getBounds();
      String[] boundSignatures = new String[bounds.size()];
      for (int i = 0; i < boundSignatures.length; i++) {
        boundSignatures[i] = bounds.get(i).getSignature();
      }
      m_signature = Signature.createTypeParameterSignature(getElementName(), boundSignatures);
    }
    return m_signature;
  }

  @Override
  public AnnotationQuery<IAnnotation> annotations() {
    IMember decl = getDeclaringMember();
    IType containerType = decl instanceof IType ? (IType) decl : decl.getDeclaringType();
    return new AnnotationQuery<>(containerType, this);
  }

}
