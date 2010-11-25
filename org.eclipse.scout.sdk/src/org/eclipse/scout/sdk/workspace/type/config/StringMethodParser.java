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
package org.eclipse.scout.sdk.workspace.type.config;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;

/**
 *
 */
public class StringMethodParser {

  private final IMethod m_method;
  private String m_returnValue;

  public StringMethodParser(IMethod method) {
    m_method = method;
    try {
      parseBody(method.getSource());
    }
    catch (Exception e) {

      e.printStackTrace();
    }
  }

  private void parseBody(String source) throws IOException {
    Matcher matcher = Pattern.compile("\\sreturn\\s", Pattern.MULTILINE).matcher(source);
    if (matcher.find()) {
      parseValue(source.substring(matcher.end()));
    }
  }

  private void parseValue(String value) throws IOException {
    boolean stringMode = false;
    boolean escapeNext = false;
    StringBuilder builder = new StringBuilder();
    StringReader reader = new StringReader(value);
    int c = reader.read();
    while (c != -1) {
      switch (c) {
        case ';':
          c = -1;
          break;
        case '\\':
          escapeNext = !escapeNext;
          break;
        case '"':
          if (!escapeNext) {
            stringMode = !stringMode;
            c = reader.read();
          }
          break;
      }
      if (stringMode) {
        builder.append((char) c);
      }
      c = reader.read();
    }
    m_returnValue = builder.toString();
  }

  public String getReturnValue() {
    return m_returnValue;
  }
}
