/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.internal;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.typeparam.TypeParameterGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IMember;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.java.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.java.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;

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
