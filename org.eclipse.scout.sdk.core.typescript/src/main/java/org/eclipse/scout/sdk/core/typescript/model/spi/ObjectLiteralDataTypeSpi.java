/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.spi;

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.DataTypeImplementor;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class ObjectLiteralDataTypeSpi extends AbstractNodeElementSpi<IDataType> implements DataTypeSpi {

  private final String m_name;
  private final IObjectLiteral m_objectLiteral;

  public ObjectLiteralDataTypeSpi(NodeModuleSpi module, String name, IObjectLiteral objectLiteral) {
    super(module);
    m_name = Ensure.notNull(name);
    m_objectLiteral = Ensure.notNull(objectLiteral);
  }

  @Override
  protected IDataType createApi() {
    return new DataTypeImplementor<>(this);
  }

  @Override
  public String name() {
    return m_name;
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  public IObjectLiteral objectLiteral() {
    return m_objectLiteral;
  }

  @Override
  public Optional<SourceRange> source() {
    return m_objectLiteral.source();
  }
}
