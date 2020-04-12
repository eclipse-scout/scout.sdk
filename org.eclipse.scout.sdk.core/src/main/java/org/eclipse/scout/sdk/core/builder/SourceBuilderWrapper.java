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
 * <h3>{@link SourceBuilderWrapper}</h3>
 * <p>
 * A {@link ISourceBuilder} that wraps an existing {@link ISourceBuilder}.
 *
 * @since 6.1.0
 */
public class SourceBuilderWrapper<TYPE extends ISourceBuilder<TYPE>> extends AbstractSourceBuilder<TYPE> {

  private final ISourceBuilder<?> m_inner;

  public SourceBuilderWrapper(ISourceBuilder<?> inner) {
    super(Ensure.notNull(inner).context());
    m_inner = inner;
  }

  /**
   * @return The wrapped {@link ISourceBuilder}.
   */
  public ISourceBuilder<?> inner() {
    return m_inner;
  }

  @Override
  public String toString() {
    return inner().toString();
  }

  @Override
  public TYPE space() {
    inner().space();
    return currentInstance();
  }

  @Override
  public TYPE append(char[] c) {
    inner().append(c);
    return currentInstance();
  }

  @Override
  public TYPE append(String s) {
    inner().append(s);
    return currentInstance();
  }

  @Override
  public TYPE append(char c) {
    inner().append(c);
    return currentInstance();
  }

  @Override
  public TYPE append(CharSequence cs) {
    inner().append(cs);
    return currentInstance();
  }

  @Override
  public TYPE append(boolean b) {
    inner().append(b);
    return currentInstance();
  }

  @Override
  public TYPE append(double d) {
    inner().append(d);
    return currentInstance();
  }

  @Override
  public TYPE append(float f) {
    inner().append(f);
    return currentInstance();
  }

  @Override
  public TYPE append(int i) {
    inner().append(i);
    return currentInstance();
  }

  @Override
  public TYPE append(long l) {
    inner().append(l);
    return currentInstance();
  }
}
