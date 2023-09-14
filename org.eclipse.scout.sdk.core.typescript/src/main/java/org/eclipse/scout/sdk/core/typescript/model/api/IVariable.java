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

import org.eclipse.scout.sdk.core.typescript.model.spi.VariableSpi;

/**
 * Represents a JavaScript or TypeScript variable.
 */
public interface IVariable extends INodeElement, IDataTypeOwner {
  @Override
  VariableSpi spi();

  /**
   * Checks if this {@link IVariable} has the given modifier.
   *
   * @param modifier
   *          The {@link Modifier} to check. May be {@code null} (then always {@code false} is returned).
   * @return {@code true} if the given modifier is present.
   */
  boolean hasModifier(Modifier modifier);

  /**
   * Tries to compute a constant value this {@link IVariable} is initialized with.
   * 
   * @return The constant value of the variable initializer (the part after the equals sign) as {@link IConstantValue}
   *         if present.
   */
  IConstantValue constantValue();
}
