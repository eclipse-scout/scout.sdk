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

import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IField;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.java.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.query.AnnotationQuery;
import org.eclipse.scout.sdk.core.java.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;

public class FieldImplementor extends AbstractMemberImplementor<FieldSpi> implements IField {

  public FieldImplementor(FieldSpi spi) {
    super(spi);
  }

  @Override
  public IType requireDeclaringType() {
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
    return new AnnotationQuery<>(requireDeclaringType(), m_spi);
  }

  @Override
  public IFieldGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return FieldGenerator.create(this, transformer);
  }

  @Override
  public IFieldGenerator<?> toWorkingCopy() {
    return toWorkingCopy(null);
  }
}
