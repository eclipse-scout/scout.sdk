/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.api.internal;

import org.eclipse.scout.sdk.core.model.api.ISourceRange;

/**
 * <h3>{@link SourceRange}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class SourceRange implements ISourceRange {

  private final String m_src;
  private final int m_start;
  private final int m_end;

  public SourceRange(String src, int start, int end) {
    m_src = src;
    m_start = start;
    m_end = end;
  }

  @Override
  public int start() {
    return m_start;
  }

  @Override
  public int length() {
    if (m_src == null) {
      return -1;
    }
    return m_src.length();
  }

  @Override
  public int end() {
    return m_end;
  }

  @Override
  public String toString() {
    return m_src;
  }

  @Override
  public boolean isAvailable() {
    return m_src != null && m_start >= 0 && m_end > m_start;
  }
}
