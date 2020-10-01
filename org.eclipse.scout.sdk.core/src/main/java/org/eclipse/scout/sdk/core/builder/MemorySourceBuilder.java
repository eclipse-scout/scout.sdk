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

  public MemorySourceBuilder() {
    this(new BuilderContext());
  }

  public MemorySourceBuilder(IBuilderContext context) {
    super(context);
    m_builder = new StringBuilder(256);
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
