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

import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.query.AnnotationQuery;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;

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
    return new AnnotationQuery<>(declaringMethod().declaringType(), m_spi);
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
