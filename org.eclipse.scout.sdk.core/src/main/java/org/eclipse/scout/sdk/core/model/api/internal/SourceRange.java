/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api.internal;

import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link SourceRange}</h3>
 *
 * @since 5.2.0
 */
public class SourceRange implements ISourceRange {

  private final CharSequence m_src;
  private final int m_start;
  private final int m_end;

  public SourceRange(CharSequence src, int start, int end) {
    m_src = Ensure.notNull(src);
    m_start = start;
    m_end = end;
  }

  @Override
  public int start() {
    return m_start;
  }

  @Override
  public int length() {
    return m_src.length();
  }

  @Override
  public int end() {
    return m_end;
  }

  @Override
  public CharSequence asCharSequence() {
    return m_src;
  }

  @Override
  public String toString() {
    return asCharSequence().toString();
  }
}
