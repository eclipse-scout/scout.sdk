/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api;

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.spi.TypeOfSpi;

public interface ITypeOf extends IDataType, IDataTypeOwner {
  @Override
  TypeOfSpi spi();

  /**
   * @return The element this typeof operator points to. This is the element referenced after the {@code typeof}
   *         keyword.
   */
  Optional<IDataTypeOwner> dataTypeOwner();
}
