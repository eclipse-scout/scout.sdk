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
import org.eclipse.scout.sdk.core.builder.SourceBuilderWrapper;

/**
 * <h3>{@link TypeScriptSourceBuilder}</h3>
 *
 * @since 13.0
 */
public class TypeScriptSourceBuilder extends SourceBuilderWrapper<TypeScriptSourceBuilder> implements ITypeScriptSourceBuilder<TypeScriptSourceBuilder> {

  protected TypeScriptSourceBuilder(ISourceBuilder<?> inner) {
    super(inner);
  }

  /**
   * Creates a new {@link ITypeScriptSourceBuilder} wrapping the given inner {@link ISourceBuilder}.
   *
   * @param inner
   *          The inner {@link ISourceBuilder}. Must not be {@code null}.
   * @return A new {@link ITypeScriptSourceBuilder} instance.
   */
  public static ITypeScriptSourceBuilder<?> create(ISourceBuilder<?> inner) {
    return new TypeScriptSourceBuilder(inner);
  }

  @Override
  public TypeScriptSourceBuilder blockStart() {
    return append('{');
  }

  @Override
  public TypeScriptSourceBuilder blockEnd() {
    return append('}');
  }

  @Override
  public TypeScriptSourceBuilder parenthesisOpen() {
    return append('(');
  }

  @Override
  public TypeScriptSourceBuilder parenthesisClose() {
    return append(')');
  }

  @Override
  public TypeScriptSourceBuilder equalSign() {
    return append(" = ");
  }

  @Override
  public TypeScriptSourceBuilder dot() {
    return append('.');
  }

  @Override
  public TypeScriptSourceBuilder colon() {
    return append(':');
  }

  @Override
  public TypeScriptSourceBuilder comma() {
    return append(',');
  }

  @Override
  public TypeScriptSourceBuilder semicolon() {
    return append(';');
  }
}
