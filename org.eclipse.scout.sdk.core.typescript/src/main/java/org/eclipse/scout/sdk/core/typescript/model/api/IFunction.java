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

import org.eclipse.scout.sdk.core.typescript.model.spi.FunctionSpi;

/**
 * Represents a JavaScript or TypeScript (arrow-)function
 */
public interface IFunction extends INodeElement {
  @Override
  FunctionSpi spi();

  /**
   * Tries to detect if this function returns an object literal and returns it if found.
   * 
   * @return The {@link IObjectLiteral} that this function returns (if found).
   */
  Optional<IObjectLiteral> resultingObjectLiteral();
}
