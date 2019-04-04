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

import static org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.transformAnnotationElement;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.spi.AnnotationElementSpi;

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
  public Optional<ISourceRange> sourceOfExpression() {
    return Optional.ofNullable(m_spi.getSourceOfExpression());
  }

  @Override
  public Stream<IJavaElement> children() {
    return Stream.empty();
  }

  @Override
  public ISourceGenerator<ISourceBuilder<?>> toWorkingCopy(IWorkingCopyTransformer transformer) {
    if (isDefault()) {
      return ISourceGenerator.empty();
    }
    ISourceGenerator<IExpressionBuilder<?>> g = b -> b.append(elementName()).equalSign().append(transformAnnotationElement(this, transformer)
        .generalize(ExpressionBuilder::create));
    return g.generalize(ExpressionBuilder::create);
  }

  @Override
  public ISourceGenerator<ISourceBuilder<?>> toWorkingCopy() {
    return toWorkingCopy(null);
  }
}
