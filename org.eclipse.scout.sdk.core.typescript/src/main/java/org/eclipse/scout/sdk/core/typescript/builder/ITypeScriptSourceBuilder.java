/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.builder;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.typescript.builder.imports.IES6ImportValidator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

/**
 * <h3>{@link ITypeScriptSourceBuilder}</h3>
 * <p>
 * An {@link ISourceBuilder} that provides methods to apply TypeScript source fragments.
 *
 * @since 13.0
 */
public interface ITypeScriptSourceBuilder<TYPE extends ITypeScriptSourceBuilder<TYPE>> extends ISourceBuilder<TYPE> {

  @Override
  ITypeScriptBuilderContext context();

  /**
   * Appends a new TypeScript block start: {
   *
   * @return This builder
   */
  TYPE blockStart();

  /**
   * Appends a new TypeScript block end: }
   *
   * @return This builder
   */
  TYPE blockEnd();

  /**
   * Appends an {@link IDataType} reference for the ref specified.
   * <p>
   * An {@link IES6ImportValidator} decides how the reference to this {@link IDataType} should be imported and the
   * necessary alias is appended to this builder.
   * <p>
   * If the specified {@link IDataType} contains type arguments or child types, these arguments will be appended too.
   *
   * @param ref
   *          The {@link IDataType} to reference. Nothing is appended if it is {@code null}.
   * @return This builder
   * @see IES6ImportValidator#use(IDataType)
   */
  TYPE ref(IDataType ref);
}
