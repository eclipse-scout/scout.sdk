/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.builder;

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;

/**
 * <h3>{@link ISourceBuilder}</h3>
 * <p>
 * Represents a language independent builder for source code.
 * <p>
 * Typically instances of this interface are used in {@link ISourceGenerator}s.
 *
 * @since 6.1.0
 * @see ISourceGenerator
 */
public interface ISourceBuilder<TYPE extends ISourceBuilder<TYPE>> {

  /**
   * Appends the specified {@link String} to this {@link ISourceBuilder}.
   *
   * @param s
   *          The {@link String}. Must not be {@code null}.
   * @return This builder
   */
  TYPE append(String s);

  /**
   * Appends the specified {@code char} to this {@link ISourceBuilder}.
   *
   * @param c
   *          The character to append.
   * @return This builder
   */
  TYPE append(char c);

  /**
   * Appends the specified {@link CharSequence} to this {@link ISourceBuilder}.
   *
   * @param seq
   *          The {@link CharSequence}. Must not be {@code null}.
   * @return This builder
   */
  TYPE append(CharSequence seq);

  /**
   * Appends the specified characters to this {@link ISourceBuilder}.
   *
   * @param c
   *          The characters to append. Must not be {@code null}.
   * @return This builder
   */
  TYPE append(char[] c);

  /**
   * Appends the {@link String} representation of the {@code boolean} argument to this {@link ISourceBuilder}.
   * <p>
   * The overall effect is exactly as if the argument were converted to a {@link String} by the method
   * {@link String#valueOf(boolean)}, and the characters of that {@link String} were then {@link #append(String)
   * appended}.
   *
   * @param b
   *          a {@code boolean}.
   * @return This builder
   */
  TYPE append(boolean b);

  /**
   * Appends the {@link String} representation of the {@code double} argument to this {@link ISourceBuilder}.
   * <p>
   * The overall effect is exactly as if the argument were converted to a {@link String} by the method
   * {@link Double#toString(double)}, and the characters of that {@link String} were then {@link #append(String)
   * appended}.
   *
   * @param d
   *          a {@code double}.
   * @return This builder
   */
  TYPE append(double d);

  /**
   * Appends the {@link String} representation of the {@code float} argument to this {@link ISourceBuilder}.
   * <p>
   * The overall effect is exactly as if the argument were converted to a string by the method
   * {@link Float#toString(float)}, and the characters of that {@link String} were then {@link #append(String)
   * appended}.
   *
   * @param f
   *          a {@code float}.
   * @return This builder
   */
  TYPE append(float f);

  /**
   * Appends the {@link String} representation of the {@code int} argument to this {@link ISourceBuilder}.
   * <p>
   * The overall effect is exactly as if the argument were converted to a {@link String} by the method
   * {@link Integer#toString(int)}, and the characters of that {@link String} were then {@link #append(String)
   * appended}.
   *
   * @param i
   *          an {@code int}.
   * @return This builder
   */
  TYPE append(int i);

  /**
   * Appends the {@link String} representation of the {@code long} argument to this {@link ISourceBuilder}.
   * <p>
   * The overall effect is exactly as if the argument were converted to a string by the method
   * {@link Long#toString(long)}, and the characters of that {@link String} were then {@link #append(String) appended}.
   *
   * @param l
   *          a {@code long}.
   * @return This builder
   */
  TYPE append(long l);

  /**
   * Appends a newline delimiter to this {@link ISourceBuilder}. The delimiter is specified by the
   * {@link IBuilderContext#lineDelimiter()} associated with this {@link ISourceBuilder}.
   *
   * @return This builder
   * @see IBuilderContext
   */
  TYPE nl();

  /**
   * Appends a whitespace to this {@link ISourceBuilder}.
   *
   * @return This builder
   */
  TYPE space();

  /**
   * Appends the specified {@link ISourceGenerator} to this {@link ISourceBuilder} by calling
   * {@link ISourceGenerator#generate(ISourceBuilder)} using this instance as argument.
   *
   * @param generator
   *          The {@link ISourceGenerator} to append. Must not be {@code null}.
   * @return This builder
   */
  TYPE append(ISourceGenerator<ISourceBuilder<?>> generator);

  /**
   * Appends the specified {@link ISourceGenerator} to this {@link ISourceBuilder} by calling
   * {@link ISourceGenerator#generate(ISourceBuilder)} using this instance as argument.
   *
   * @param opt
   *          An {@link Optional} holding the {@link ISourceGenerator} to append. This method has no effect if the
   *          {@link Optional} is empty.
   * @return This builder
   */
  TYPE append(Optional<? extends ISourceGenerator<ISourceBuilder<?>>> opt);

  /**
   * Appends the specified generators to this {@link ISourceBuilder}.
   *
   * @param generators
   *          A {@link Stream} holding all {@link ISourceGenerator}s to append. Must not be {@code null}.
   * @param prefix
   *          The {@link CharSequence} to be appended before the first {@link ISourceGenerator} from the {@link Stream}.
   *          If the {@link Stream} does not provide any {@link ISourceGenerator}s, the prefix is not appended. If the
   *          prefix is {@code null}, nothing will be appended before the first {@link ISourceGenerator}.
   * @param delimiter
   *          The {@link CharSequence} to be appended between two {@link ISourceGenerator} from the {@link Stream}. If
   *          the {@link Stream} is empty or does only contain one {@link ISourceGenerator}, no delimiter is appended.
   *          If the delimiter is {@code null}, nothing will be appended between two {@link ISourceGenerator}s.
   * @param suffix
   *          The {@link CharSequence} to be appended after the last {@link ISourceGenerator} from the {@link Stream}.
   *          If the {@link Stream} does not provide any {@link ISourceGenerator}s, the suffix is not appended. If the
   *          suffix is {@code null}, nothing will be appended after the last {@link ISourceGenerator}.
   * @return This builder
   */
  TYPE append(Stream<? extends ISourceGenerator<ISourceBuilder<?>>> generators, CharSequence prefix, CharSequence delimiter, CharSequence suffix);

  /**
   * Returns a {@link Collector} that collects {@link ISourceGenerator}s into this {@link ISourceBuilder}. See
   * {@link #append(Stream, CharSequence, CharSequence, CharSequence)} for details.
   *
   * @return This builder
   */
  <T extends ISourceGenerator<ISourceBuilder<?>>> Collector<T, ISourceBuilder<?>, ISourceBuilder<?>> collector(CharSequence prefix, CharSequence delimiter, CharSequence suffix);

  /**
   * @return The {@link IBuilderContext} of this {@link ISourceBuilder}.
   */
  IBuilderContext context();
}
