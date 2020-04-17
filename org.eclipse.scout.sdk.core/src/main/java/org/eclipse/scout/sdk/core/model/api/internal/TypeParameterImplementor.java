/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api.internal;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.TypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;

/**
 * <h3>{@link TypeParameterImplementor}</h3>
 *
 * @since 4.1.0 2014-11-09
 */
public class TypeParameterImplementor extends AbstractJavaElementImplementor<TypeParameterSpi> implements ITypeParameter {

  public TypeParameterImplementor(TypeParameterSpi spi) {
    super(spi);
  }

  @Override
  public IMember declaringMember() {
    return m_spi.getDeclaringMember().wrap();
  }

  @Override
  public Stream<IType> bounds() {
    return WrappingSpliterator.stream(m_spi.getBounds());
  }

  @Override
  public Stream<? extends IJavaElement> children() {
    return Stream.empty();
  }

  @Override
  public ITypeParameterGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return TypeParameterGenerator.create(this);
  }

  @Override
  public ITypeParameterGenerator<?> toWorkingCopy() {
    return toWorkingCopy(null);
  }
}
