/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.builder;

import java.nio.CharBuffer;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link AbstractSourceBuilder}</h3>
 *
 * @since 6.1.0
 */
public abstract class AbstractSourceBuilder<TYPE extends ISourceBuilder<TYPE>> implements ISourceBuilder<TYPE> {

  private final IBuilderContext m_context;

  protected AbstractSourceBuilder(IBuilderContext context) {
    m_context = Ensure.notNull(context);
  }

  @SuppressWarnings("unchecked")
  protected TYPE thisInstance() {
    return (TYPE) this;
  }

  @Override
  public IBuilderContext context() {
    return m_context;
  }

  @Override
  public TYPE append(String s) {
    return append((CharSequence) s);
  }

  @Override
  public TYPE appendLine(CharSequence s) {
    return append(s).nl();
  }

  @Override
  public TYPE append(boolean b) {
    return append(Boolean.toString(b));
  }

  @Override
  public TYPE append(double d) {
    return append(Double.toString(d));
  }

  @Override
  public TYPE append(float f) {
    return append(Float.toString(f));
  }

  @Override
  public TYPE append(int i) {
    return append(Integer.toString(i));
  }

  @Override
  public TYPE append(long l) {
    return append(Long.toString(l));
  }

  @Override
  public TYPE append(char[] c) {
    return append(CharBuffer.wrap(c));
  }

  @Override
  public TYPE space() {
    return append(' ');
  }

  @Override
  public TYPE nl() {
    return append(context().lineDelimiter());
  }

  @Override
  public TYPE parenthesisOpen() {
    return append('(');
  }

  @Override
  public TYPE parenthesisClose() {
    return append(')');
  }

  @Override
  public TYPE equalSign() {
    return append(" = ");
  }

  @Override
  public TYPE dot() {
    return append('.');
  }

  @Override
  public TYPE colon() {
    return append(':');
  }

  @Override
  public TYPE comma() {
    return append(',');
  }

  @Override
  public TYPE semicolon() {
    return append(';');
  }

  @Override
  public TYPE append(ISourceGenerator<ISourceBuilder<?>> generator) {
    Ensure.notNull(generator).generate(this);
    return thisInstance();
  }

  @Override
  public <T extends ISourceGenerator<ISourceBuilder<?>>> Collector<T, ISourceBuilder<?>, ISourceBuilder<?>> collector(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    return new SourceCollector<>(this, prefix, delimiter, suffix);
  }

  @Override
  public TYPE append(Stream<? extends ISourceGenerator<ISourceBuilder<?>>> generators, CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    //noinspection ResultOfMethodCallIgnored
    Ensure.notNull(generators).collect(collector(prefix, delimiter, suffix));
    return thisInstance();
  }

  @Override
  public TYPE append(Optional<? extends ISourceGenerator<ISourceBuilder<?>>> opt) {
    Ensure.notNull(opt).ifPresent(this::append);
    return thisInstance();
  }
}
