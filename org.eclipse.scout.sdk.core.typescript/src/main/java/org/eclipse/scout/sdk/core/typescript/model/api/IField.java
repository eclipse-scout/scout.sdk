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

import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi;

/**
 * Represents a field in an {@link IES6Class}.
 */
public interface IField extends IVariable {
  @Override
  FieldSpi spi();

  /**
   * @return {@code true} if this is an optional TypeScript field (a field having '?' after its name).
   */
  boolean isOptional();

  /**
   * @return The {@link IES6Class} in which this {@link IField} is declared.
   */
  IES6Class declaringClass();
}
