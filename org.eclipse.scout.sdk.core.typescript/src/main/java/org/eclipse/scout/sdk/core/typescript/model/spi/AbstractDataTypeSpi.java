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

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.util.FinalValue;

public abstract class AbstractDataTypeSpi<API extends IDataType> implements DataTypeSpi {
  private final FinalValue<API> m_api = new FinalValue<>();

  protected abstract API createApi();

  @Override
  public API api() {
    return m_api.computeIfAbsentAndGet(this::createApi);
  }
}
