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
    return thisInstance();
  }

  @Override
  public TYPE append(char[] c) {
    inner().append(c);
    return thisInstance();
  }

  @Override
  public TYPE append(String s) {
    inner().append(s);
    return thisInstance();
  }

  @Override
  public TYPE append(char c) {
    inner().append(c);
    return thisInstance();
  }

  @Override
  public TYPE append(CharSequence cs) {
    inner().append(cs);
    return thisInstance();
  }

  @Override
  public TYPE append(boolean b) {
    inner().append(b);
    return thisInstance();
  }

  @Override
  public TYPE append(double d) {
    inner().append(d);
    return thisInstance();
  }

  @Override
  public TYPE append(float f) {
    inner().append(f);
    return thisInstance();
  }

  @Override
  public TYPE append(int i) {
    inner().append(i);
    return thisInstance();
  }

  @Override
  public TYPE append(long l) {
    inner().append(l);
    return thisInstance();
  }
}
