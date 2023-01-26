/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.internal;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class DataTypeImplementor implements IDataType {

  private final DataTypeSpi m_spi;

  public DataTypeImplementor(DataTypeSpi spi) {
    m_spi = Ensure.notNull(spi);
  }

  @Override
  public DataTypeSpi spi() {
    return m_spi;
  }

  @Override
  public String name() {
    return spi().name();
  }

  @Override
  public boolean isPrimitive() {
    return spi().isPrimitive();
  }
}
