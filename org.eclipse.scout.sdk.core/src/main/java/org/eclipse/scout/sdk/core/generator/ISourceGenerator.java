/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.generator;

import java.util.function.Function;

import org.eclipse.scout.sdk.core.builder.IBuilderContext;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.body.MethodBodyBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link ISourceGenerator}</h3>
 * <p>
 * A source generator is a class that appends source code to a {@link ISourceBuilder}.
 *
 * @since 6.1.0
 */
@FunctionalInterface
public interface ISourceGenerator<BUILDER extends ISourceBuilder<?>> {

  void generate(BUILDER builder);

  /**
   * Converts this {@link ISourceGenerator} to a {@link ISourceGenerator} that accepts the basic {@link ISourceBuilder}.
   *
   * @param specificBuilder
   *          The specific builder to use when this generator is used. Must not be {@code null}.
   * @return A generalized version of this {@link ISourceGenerator} that uses the given {@link ISourceBuilder} instead
   *         of the one that was originally passed to the resulting {@link ISourceGenerator#generate(ISourceBuilder)}
   *         method.
   */
  default ISourceGenerator<ISourceBuilder<?>> generalize(BUILDER specificBuilder) {
    return generalize(a -> Ensure.notNull(specificBuilder));
  }

  /**
   * Converts this {@link ISourceGenerator} to a {@link ISourceGenerator} that accepts the basic {@link ISourceBuilder}.
   * <p>
   * <b>Example:</b><br>
   * {@code ISourceGenerator<ISourceBuilder<?>> result = ISourceGenerator<IMethodBodyBuilder<?>>.generalize(MethodBodyBuilder::create)}
   *
   * @param fromGenericToSpecificBuilder
   *          The function to call when the passed generic {@link ISourceBuilder} needs to be converted to the specific
   *          one this {@link ISourceGenerator} requires. Must not be {@code null}.<br>
   *          Typically this is one of the #create methods of the specific source builders (e.g.
   *          {@link MethodBodyBuilder#create(ISourceBuilder)}).
   * @return A generalized version of this {@link ISourceGenerator}.
   */
  default ISourceGenerator<ISourceBuilder<?>> generalize(Function<ISourceBuilder<?>, BUILDER> fromGenericToSpecificBuilder) {
    return b -> generate(Ensure.notNull(fromGenericToSpecificBuilder).apply(b));
  }

  /**
   * Executes this {@link ISourceGenerator} and creates its source in memory.
   *
   * @param fromGenericToSpecificBuilder
   *          The function to call when the passed generic {@link ISourceBuilder} needs to be converted to the specific
   *          one this {@link ISourceGenerator} requires. Must not be {@code null}.<br>
   *          Typically this is one of the #create methods of the specific source builders (e.g.
   *          {@link MethodBodyBuilder#create(ISourceBuilder)}).
   * @param context
   *          The {@link IBuilderContext} to use during the generation process. Must not be {@code null}.
   * @return A {@link StringBuilder} holding the content of the created source.
   * @see ISourceGenerator#generalize(Function)
   */
  default StringBuilder toSource(Function<ISourceBuilder<?>, BUILDER> fromGenericToSpecificBuilder, IBuilderContext context) {
    MemorySourceBuilder out = new MemorySourceBuilder(context);
    generalize(fromGenericToSpecificBuilder).generate(out);
    return out.source();
  }

  /**
   * @return An {@link ISourceGenerator} that does nothing (no-op {@link ISourceGenerator}).
   */
  static <T extends ISourceBuilder<?>> ISourceGenerator<T> empty() {
    return b -> {
      // must be empty
    };
  }

  /**
   * Creates a {@link ISourceGenerator} that appends the specified {@link CharSequence} to the {@link ISourceBuilder}.
   * The source is directly appended. No processing is done on the input.
   *
   * @param source
   *          The source to append. Must not be {@code null}.
   * @return An {@link ISourceGenerator} that appends the specified {@link CharSequence}.
   */
  static <T extends ISourceBuilder<?>> ISourceGenerator<T> raw(CharSequence source) {
    Ensure.notNull(source);
    return b -> b.append(source);
  }
}
