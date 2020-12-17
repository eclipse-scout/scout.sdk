/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api.internal;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.model.spi.MemberSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

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
  public Optional<ISourceRange> javaDoc() {
    return Optional.ofNullable(m_spi.getJavaDoc());
  }
}
