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

/**
 * Represents all elements having an {@link IDataType} (like variables or a typeof data type).
 */
public interface IDataTypeOwner {

  /**
   * @return The {@link IDataType} of this element or an empty {@link Optional} if the data type cannot be computed.
   */
  Optional<IDataType> dataType();
}
