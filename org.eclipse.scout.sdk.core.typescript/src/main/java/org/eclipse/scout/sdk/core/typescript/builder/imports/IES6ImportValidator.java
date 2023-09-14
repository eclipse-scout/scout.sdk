/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.builder.imports;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

/**
 * Validator that parses an {@link IDataType}, collects all necessary imports for it and transforms it into an alias to
 * be used in the source code.
 */
public interface IES6ImportValidator {
  /**
   * Adds the necessary imports for the given {@link IDataType} and its type arguments and child types to the
   * {@link IES6ImportCollector} of this validator and returns the alias to use in the source code.
   * 
   * @param dataType
   *          The {@link IDataType} to use.
   * @return The alias to use in the source code or {@code null} if the data type is {@code null}.
   */
  CharSequence use(IDataType dataType);

  /**
   * @return The {@link IES6ImportCollector} to which necessary imports are added.
   */
  IES6ImportCollector importCollector();
}
