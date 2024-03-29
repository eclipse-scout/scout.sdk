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

import org.eclipse.scout.sdk.core.java.model.api.IMember;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.java.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.java.model.spi.MemberSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.SourceRange;

public abstract class AbstractMemberImplementor<SPI extends MemberSpi> extends AbstractAnnotatableImplementor<SPI> implements IMember {

  protected AbstractMemberImplementor(SPI spi) {
    super(spi);
  }

  @Override
  public Optional<IType> declaringType() {
    return Optional.ofNullable(m_spi.getDeclaringType())
        .map(TypeSpi::wrap);
  }

  @Override
  public int flags() {
    return m_spi.getFlags();
  }

  @Override
  public Stream<ITypeParameter> typeParameters() {
    return WrappingSpliterator.stream(m_spi.getTypeParameters());
  }

  @Override
  public boolean hasTypeParameters() {
    return m_spi.hasTypeParameters();
  }

  @Override
  public Optional<SourceRange> javaDoc() {
    return Optional.ofNullable(m_spi.getJavaDoc());
  }
}
