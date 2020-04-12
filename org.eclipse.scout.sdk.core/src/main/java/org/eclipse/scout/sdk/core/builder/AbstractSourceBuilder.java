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
package org.eclipse.scout.sdk.core.builder;

import java.nio.CharBuffer;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;

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
  protected TYPE currentInstance() {
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
    return append(JavaTypes.C_SPACE);
  }

  @Override
  public TYPE nl() {
    return append(context().lineDelimiter());
  }

  @Override
  public TYPE append(ISourceGenerator<ISourceBuilder<?>> generator) {
    Ensure.notNull(generator).generate(this);
    return currentInstance();
  }

  @Override
  public <T extends ISourceGenerator<ISourceBuilder<?>>> Collector<T, ISourceBuilder<?>, ISourceBuilder<?>> collector(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    return new SourceCollector<>(this, prefix, delimiter, suffix);
  }

  @Override
  public TYPE append(Stream<? extends ISourceGenerator<ISourceBuilder<?>>> generators, CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    //noinspection ResultOfMethodCallIgnored
    Ensure.notNull(generators).collect(collector(prefix, delimiter, suffix));
    return currentInstance();
  }

  @Override
  public TYPE append(Optional<? extends ISourceGenerator<ISourceBuilder<?>>> opt) {
    Ensure.notNull(opt).ifPresent(this::append);
    return currentInstance();
  }
}
