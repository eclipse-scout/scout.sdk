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

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link MemorySourceBuilder}</h3>
 * <p>
 * A {@link ISourceBuilder} that collects the source in memory using a {@link StringBuilder}.
 *
 * @since 6.1.0
 */
public class MemorySourceBuilder extends AbstractSourceBuilder<MemorySourceBuilder> {

  private final StringBuilder m_builder;

  protected MemorySourceBuilder(IBuilderContext context) {
    super(context);
    m_builder = new StringBuilder(256);
  }

  /**
   * Creates a new {@link MemorySourceBuilder} using the {@link IBuilderContext} given.
   * 
   * @param context
   *          The {@link IBuilderContext} to use. Must not be {@code null}.
   * @return a new {@link MemorySourceBuilder}
   */
  public static MemorySourceBuilder create(IBuilderContext context) {
    return new MemorySourceBuilder(context);
  }

  /**
   * Creates a new {@link MemorySourceBuilder} using a default {@link IBuilderContext} which uses {@code \n} as line
   * separator.
   * 
   * @return a new {@link MemorySourceBuilder}
   */
  public static MemorySourceBuilder create() {
    return create(new BuilderContext());
  }

  /**
   * @return The (modifiable) {@link StringBuilder} that holds the content of this {@link ISourceBuilder}.
   */
  public StringBuilder source() {
    return m_builder;
  }

  @Override
  public MemorySourceBuilder append(String s) {
    source().append(Ensure.notNull(s));
    return thisInstance();
  }

  @Override
  public MemorySourceBuilder append(char c) {
    source().append(c);
    return thisInstance();
  }

  @Override
  public MemorySourceBuilder append(CharSequence cs) {
    source().append(Ensure.notNull(cs));
    return thisInstance();
  }

  @Override
  public MemorySourceBuilder append(char[] c) {
    source().append(Ensure.notNull(c));
    return thisInstance();
  }

  @Override
  public MemorySourceBuilder append(boolean b) {
    source().append(b);
    return thisInstance();
  }

  @Override
  public MemorySourceBuilder append(double d) {
    source().append(d);
    return thisInstance();
  }

  @Override
  public MemorySourceBuilder append(float f) {
    source().append(f);
    return thisInstance();
  }

  @Override
  public MemorySourceBuilder append(int i) {
    source().append(i);
    return thisInstance();
  }

  @Override
  public MemorySourceBuilder append(long l) {
    source().append(l);
    return thisInstance();
  }

  @Override
  public String toString() {
    return source().toString();
  }
}
