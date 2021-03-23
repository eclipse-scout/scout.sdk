/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api.internal;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.query.AnnotationQuery;
import org.eclipse.scout.sdk.core.model.api.query.MethodParameterQuery;
import org.eclipse.scout.sdk.core.model.api.query.SuperMethodQuery;
import org.eclipse.scout.sdk.core.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 *
 */
public class MethodImplementor extends AbstractMemberImplementor<MethodSpi> implements IMethod {

  public MethodImplementor(MethodSpi spi) {
    super(spi);
  }

  @Override
  public IType requireDeclaringType() {
    return m_spi.getDeclaringType().wrap();
  }

  @Override
  public boolean isConstructor() {
    return m_spi.isConstructor();
  }

  @Override
  public Optional<IType> returnType() {
    return Optional
        .ofNullable(m_spi.getReturnType())
        .map(TypeSpi::wrap);
  }

  @Override
  public Stream<? extends IJavaElement> children() {
    return Stream.concat(Stream.concat(annotations().stream(), typeParameters()), parameters().stream());
  }

  @Override
  public IType requireReturnType() {
    return returnType()
        .orElseThrow(() -> newFail("Method {} in type {} is a constructor and therefore has no return type.", identifier(), requireDeclaringType().name()));
  }

  @Override
  public Stream<IType> exceptionTypes() {
    return WrappingSpliterator.stream(m_spi.getExceptionTypes());
  }

  @Override
  public Optional<ISourceRange> sourceOfBody() {
    return Optional.ofNullable(m_spi.getSourceOfBody());
  }

  @Override
  public Optional<ISourceRange> sourceOfDeclaration() {
    return Optional.ofNullable(m_spi.getSourceOfDeclaration());
  }

  @Override
  public SuperMethodQuery superMethods() {
    return new SuperMethodQuery(this);
  }

  @Override
  public AnnotationQuery<IAnnotation> annotations() {
    return new AnnotationQuery<>(requireDeclaringType(), m_spi);
  }

  @Override
  public MethodParameterQuery parameters() {
    return new MethodParameterQuery(m_spi);
  }

  @Override
  public String identifier(boolean includeTypeArguments) {
    var parameterTypes = parameters().stream()
        .map(IMethodParameter::dataType)
        .map(p -> p.reference(!includeTypeArguments))
        .collect(toList());
    return JavaTypes.createMethodIdentifier(elementName(), parameterTypes);
  }

  @Override
  public String identifier() {
    return identifier(false);
  }

  @Override
  public IMethodGenerator<?, ?> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return MethodGenerator.create(this, transformer);
  }

  @Override
  public IMethodGenerator<?, ?> toWorkingCopy() {
    return toWorkingCopy(null);
  }
}
