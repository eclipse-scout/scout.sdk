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
package org.eclipse.scout.sdk.jdt;

import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jface.text.Document;

public class SourceRange implements ISourceRange {

  private int m_offset;
  private int m_length;

  public SourceRange(int offset, int length) {
    m_offset = offset;
    m_length = length;

  }

  public static SourceRange getFullRange(Document document) {
    return new SourceRange(0, document.getLength());
  }

  @Override
  public int getLength() {
    return m_length;
  }

  public void setLength(int length) {
    m_length = length;
  }

  @Override
  public int getOffset() {
    return m_offset;
  }

  public void setOffset(int offset) {
    m_offset = offset;
  }

}
