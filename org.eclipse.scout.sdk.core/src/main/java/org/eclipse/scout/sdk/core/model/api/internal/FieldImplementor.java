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

import static org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.transformField;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.query.AnnotationQuery;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;

public class FieldImplementor extends AbstractMemberImplementor<FieldSpi> implements IField {

  public FieldImplementor(FieldSpi spi) {
    super(spi);
  }

  @Override
  public IType declaringType() {
    return m_spi.getDeclaringType().wrap();
  }

  @Override
  public Optional<IMetaValue> constantValue() {
    return Optional.ofNullable(m_spi.getConstantValue());
  }

  @Override
  public IType dataType() {
    return m_spi.getDataType().wrap();
  }

  @Override
  public Stream<? extends IJavaElement> children() {
    return Stream.concat(annotations().stream(), typeParameters());
  }

  @Override
  public Optional<ISourceRange> sourceOfInitializer() {
    return Optional.ofNullable(m_spi.getSourceOfInitializer());
  }

  @Override
  public AnnotationQuery<IAnnotation> annotations() {
    return new AnnotationQuery<>(declaringType(), m_spi);
  }

  @Override
  public IFieldGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return transformField(this, transformer);
  }

  @Override
  public IFieldGenerator<?> toWorkingCopy() {
    return toWorkingCopy(null);
  }
}
