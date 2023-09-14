/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator;

import static java.util.function.Function.identity;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.typescript.builder.ITypeScriptBuilderContext;
import org.eclipse.scout.sdk.core.typescript.builder.TypeScriptBuilderContext;

/**
 * <h3>{@link ITypeScriptElementGenerator}</h3>
 * <p>
 * An {@link ISourceGenerator} that creates source for TypeScript elements.
 *
 * @since 13.0
 */
public interface ITypeScriptElementGenerator<TYPE extends ITypeScriptElementGenerator<TYPE>> extends ISourceGenerator<ISourceBuilder<?>> {

  /**
   * @return The name of the element if present. Otherwise, an empty {@link Optional} is returned.
   */
  Optional<String> elementName();

  /**
   * Sets the name of this {@link ITypeScriptElementGenerator}.
   *
   * @param newName
   *          The new name or {@code null}.
   * @return This generator
   */
  TYPE withElementName(String newName);

  /**
   * Executes this {@link ITypeScriptElementGenerator} and creates its source in memory.
   *
   * @return A {@link StringBuilder} holding the content of the created source.
   */
  default StringBuilder toTypeScriptSource() {
    return toTypeScriptSource(new TypeScriptBuilderContext(new BuilderContext()));
  }

  /**
   * Executes this {@link ITypeScriptElementGenerator} using the context given.
   *
   * @param context
   *          The {@link ITypeScriptBuilderContext} in which the source is created. Must not be {@code null}.
   * @return A {@link StringBuilder} holding the content of the created source.
   * @see #toTypeScriptSource()
   */
  default StringBuilder toTypeScriptSource(ITypeScriptBuilderContext context) {
    return toSource(identity(), context);
  }
}
