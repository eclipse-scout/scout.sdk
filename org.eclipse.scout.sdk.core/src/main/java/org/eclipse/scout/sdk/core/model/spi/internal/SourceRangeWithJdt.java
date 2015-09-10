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
package org.eclipse.scout.sdk.core.model.spi.internal;

import org.eclipse.scout.sdk.core.model.api.ISourceRange;

/**
 * <h3>{@link SourceRangeWithJdt}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class SourceRangeWithJdt implements ISourceRange {
  private final org.eclipse.jdt.internal.compiler.env.ICompilationUnit m_sourceUnit;
  private final int m_start;
  private final int m_end;

  public SourceRangeWithJdt(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, int start, int end) {
    m_sourceUnit = sourceUnit;
    m_start = start;
    m_end = end;
  }

  @Override
  public String toString() {
    return new String(m_sourceUnit.getContents(), m_start, m_end - m_start + 1);
  }
}
