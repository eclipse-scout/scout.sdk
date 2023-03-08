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

import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.DataTypeImplementor;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class SimpleDataTypeSpi extends AbstractNodeElementSpi<IDataType> implements DataTypeSpi {

  private final String m_dataType;

  public SimpleDataTypeSpi(NodeModuleSpi module, String dataType) {
    super(module);
    m_dataType = Ensure.notNull(dataType);
  }

  @Override
  protected IDataType createApi() {
    return new DataTypeImplementor<>(this);
  }

  @Override
  public String name() {
    return m_dataType;
  }

  @Override
  public boolean isPrimitive() {
    return TypeScriptTypes.isPrimitive(m_dataType);
  }

  @Override
  public Optional<SourceRange> source() {
    return Optional.of(new SourceRange(name(), 0));
  }
}
