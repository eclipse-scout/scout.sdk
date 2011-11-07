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
package org.eclipse.scout.nls.sdk.internal.jdt;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class JavaFileInputReader {
  private static final int TYPE_NONE = -1;
  private static final int TYPE_MULTILINE_COMMENT = 1;
  private static final int TYPE_SINGLELINE_COMMENT = 2;

  private int m_type = TYPE_NONE;
  private int m_counter = 0;
  private char[] m_buf = new char[2];
  private String m_startMulti = "/*";
  private String m_endMulti = "*/";
  private String m_startSingle = "//";
  private String m_endSingle = "\n";
  private InputStream m_inputStream;

  public JavaFileInputReader(IFile file) throws CoreException {
    m_inputStream = file.getContents();
  }

  public int read() throws IOException {
    int in = m_inputStream.read();
    m_buf[m_counter % 2] = (char) in;
    m_counter++;
    m_type = checkComment();
    return in;
  }

  public boolean isCommentBlock() {
    return m_type == TYPE_MULTILINE_COMMENT || m_type == TYPE_SINGLELINE_COMMENT;
  }

  public void close() throws IOException {
    m_inputStream.close();
  }

  private int checkComment() {
    String inSt = new String(m_buf);
    if (inSt.equals(m_startMulti)) {
      return TYPE_MULTILINE_COMMENT;
    }
    if (inSt.equals(m_endMulti)) {
      return TYPE_NONE;
    }
    if (inSt.equals(m_startSingle)) {
      return TYPE_SINGLELINE_COMMENT;
    }
    if (inSt.contains(m_endSingle)) {
      return TYPE_NONE;
    }
    return TYPE_NONE;
  }

}
