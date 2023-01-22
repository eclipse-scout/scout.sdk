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

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.builder.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.SimpleGenerators;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class AnnotationElementImplementor extends AbstractJavaElementImplementor<AnnotationElementSpi> implements IAnnotationElement {

  public AnnotationElementImplementor(AnnotationElementSpi spi) {
    super(spi);
  }

  @Override
  public IMetaValue value() {
    return m_spi.getMetaValue();
  }

  @Override
  public IAnnotation declaringAnnotation() {
    return m_spi.getDeclaringAnnotation().wrap();
  }

  @Override
  public boolean isDefault() {
    return m_spi.isDefaultValue();
  }

  @Override
  public Optional<SourceRange> sourceOfExpression() {
    return Optional.ofNullable(m_spi.getSourceOfExpression());
  }

  @Override
  public Stream<IJavaElement> children() {
    return value().children();
  }

  @Override
  public ISourceGenerator<ISourceBuilder<?>> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return SimpleGenerators.createAnnotationElementGenerator(this, transformer).generalize(ExpressionBuilder::create);
  }

  @Override
  public ISourceGenerator<ISourceBuilder<?>> toWorkingCopy() {
    return toWorkingCopy(null);
  }
}
