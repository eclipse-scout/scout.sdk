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

import org.eclipse.scout.sdk.core.java.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.query.AnnotationQuery;
import org.eclipse.scout.sdk.core.java.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;

/**
 * <h3>{@link MethodParameterImplementor}</h3>
 *
 * @since 3.8.0 2012-12-06
 */
public class MethodParameterImplementor extends AbstractAnnotatableImplementor<MethodParameterSpi> implements IMethodParameter {

  public MethodParameterImplementor(MethodParameterSpi spi) {
    super(spi);
  }

  @Override
  public IMethod declaringMethod() {
    return m_spi.getDeclaringMethod().wrap();
  }

  @Override
  public IType dataType() {
    return m_spi.getDataType().wrap();
  }

  @Override
  public Stream<? extends IJavaElement> children() {
    return annotations().stream();
  }

  @Override
  public int flags() {
    return m_spi.getFlags();
  }

  @Override
  public int index() {
    return m_spi.getIndex();
  }

  @Override
  public AnnotationQuery<IAnnotation> annotations() {
    return new AnnotationQuery<>(declaringMethod().requireDeclaringType(), m_spi);
  }

  @Override
  public IMethodParameterGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return MethodParameterGenerator.create(this, transformer);
  }

  @Override
  public IMethodParameterGenerator<?> toWorkingCopy() {
    return toWorkingCopy(null);
  }
}
