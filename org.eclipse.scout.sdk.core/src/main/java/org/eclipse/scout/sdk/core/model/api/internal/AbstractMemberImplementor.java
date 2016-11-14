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

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.model.spi.MemberSpi;

public abstract class AbstractMemberImplementor<SPI extends MemberSpi> extends AbstractAnnotatableImplementor<SPI> implements IMember {

  protected AbstractMemberImplementor(SPI spi) {
    super(spi);
  }

  @Override
  public IType declaringType() {
    return JavaEnvironmentImplementor.wrapType(m_spi.getDeclaringType());
  }

  @Override
  public int flags() {
    return m_spi.getFlags();
  }

  @Override
  public List<ITypeParameter> typeParameters() {
    return new WrappedList<>(m_spi.getTypeParameters());
  }

  @Override
  public boolean hasTypeParameters() {
    return m_spi.hasTypeParameters();
  }

  @Override
  public ISourceRange javaDoc() {
    return m_spi.getJavaDoc();
  }

}