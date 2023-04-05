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

/**
 * <h3>{@link ITypeScriptSourceBuilder}</h3>
 * <p>
 * An {@link ISourceBuilder} that provides methods to apply TypeScript source fragments.
 *
 * @since 13.0
 */
public interface ITypeScriptSourceBuilder<TYPE extends ITypeScriptSourceBuilder<TYPE>> extends ISourceBuilder<TYPE> {

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
   * Appends an opening parenthesis: (
   *
   * @return This builder
   */
  TYPE parenthesisOpen();

  /**
   * Appends a closing parenthesis: )
   *
   * @return This builder
   */
  TYPE parenthesisClose();

  /**
   * Appends the equal sign (=) including a leading and trailing space.
   *
   * @return This builder
   */
  TYPE equalSign();

  /**
   * Appends a dot: .
   *
   * @return This builder
   */
  TYPE dot();

  /**
   * Appends a colon: :
   *
   * @return This builder
   */
  TYPE colon();

  /**
   * Appends a comma: ,
   * 
   * @return This builder
   */
  TYPE comma();

  /**
   * Appends a semicolon: ;
   *
   * @return This builder
   */
  TYPE semicolon();
}
