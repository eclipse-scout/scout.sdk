/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.jdt;

import org.eclipse.jface.text.source.ILineRange;

public class LineRange implements ILineRange {

  private final int m_startLine;
  private final int m_numberOfLines;

  public LineRange(int startLine, int numberOfLines) {
    m_startLine = startLine;
    m_numberOfLines = numberOfLines;
  }

  @Override
  public int getNumberOfLines() {
    return m_numberOfLines;
  }

  @Override
  public int getStartLine() {
    return m_startLine;
  }

}
