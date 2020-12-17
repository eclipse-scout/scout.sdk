/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.imports;

import java.util.function.Function;

/**
 * <h3>{@link IImportValidator}</h3> Validates imports to fully qualified references and returns the type reference to
 * use in the source code.
 *
 * @since 5.2.0
 */
public interface IImportValidator {

  /**
   * Gets the reference to the given fully qualified names.
   *
   * @param fullyQualifiedNames
   *          The fully qualified names. <br>
   *          E.g. {@code java.lang.Long} or {@code java.util.List<java.lang.String>}.
   * @return The references to the given names to use in the source code.
   */
  String useReference(CharSequence fullyQualifiedNames);

  /**
   * @return The {@link IImportCollector} responsible to collect all used imports.
   */
  IImportCollector importCollector();

  /**
   * Executes the given {@link Runnable} with this {@link IImportValidator} using the {@link IImportCollector} returned
   * by the specified provider function.
   *
   * @param r
   *          The {@link Runnable} to execute. Must not be {@code null}.
   * @param wrappingCollectorProvider
   *          A function returning the {@link IImportCollector} to use for the specified {@link Runnable}. The input of
   *          the function is the currently used {@link IImportCollector}. Must not be {@code null}.
   */
  void runWithImportCollector(Runnable r, Function<IImportCollector, IImportCollector> wrappingCollectorProvider);
}
