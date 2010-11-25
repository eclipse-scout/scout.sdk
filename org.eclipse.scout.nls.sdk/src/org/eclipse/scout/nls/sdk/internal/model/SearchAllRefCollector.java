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
package org.eclipse.scout.nls.sdk.internal.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.JavaFileInputReader;

public class SearchAllRefCollector extends SearchRequestor {
  private HashMap<String, List<SearchMatch>> m_matches = new HashMap<String, List<SearchMatch>>();
  private String m_className;
  private List<ISearchKeyRefListener> m_listeners = new LinkedList<ISearchKeyRefListener>();

  public SearchAllRefCollector(String className) {
    m_className = className;
  }

  public void addSearchKeyRefListener(ISearchKeyRefListener listener) {
    m_listeners.add(listener);
  }

  public void removeSearchKeyRefListener(ISearchKeyRefListener listener) {
    m_listeners.remove(listener);
  }

  @Override
  public void beginReporting() {
    // m_matches = new ArrayList<SearchMatch>();
  }

  @Override
  public void acceptSearchMatch(SearchMatch match) throws CoreException {
    if (!(match.getResource() instanceof IFile)) {
      return;
    }
    JavaFileInputReader reader = new JavaFileInputReader((IFile) match.getResource());
    try {
      int in = -1;
      for (int i = 0; i < match.getOffset(); i++) {
        in = reader.read();
      }
      if (reader.isCommentBlock()) {
        // nobody is interessted in this match
        return;
      }
      StringBuffer buffer = new StringBuffer();
      // record till end of the statement
      while (!(in < 0) && !(in == ';')) {
        in = reader.read();
        buffer.append((char) in);
      }
      String key = findKey(buffer.toString(), match);
      if (key != null) {
        for (ISearchKeyRefListener listener : m_listeners) {
          listener.keyFound(key);
          // SearchMatch xx = new SearchMatch()
        }
      }
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    finally {
      try {
        reader.close();
      }
      catch (IOException e) {
        // TODO Auto-generated catch block
        NlsCore.logWarning(e);
      }
    }
  }

  @Override
  public void endReporting() {
    for (ISearchKeyRefListener listener : m_listeners) {
      listener.endSearch();
    }
  }

  private String m_regexp = "\\b[A-Za-z][a-zA-Z0-9_]{0,200}";

  private String findKey(String statement, SearchMatch match) {
    statement = statement.replaceAll("\n", "");
    int startIndex = statement.indexOf(m_className) + m_className.length();
    if (startIndex > 0 && startIndex < statement.length()) {
      statement = statement.substring(startIndex);
      if (!statement.startsWith(".")) {
        return null;
      }
      statement = statement.substring(1);
      Pattern p = Pattern.compile(m_regexp);
      // Create a matcher with an input string
      Matcher m = p.matcher(statement);
      if (m.find()) {
        return statement.substring(m.start(), m.end());
      }
    }
    return null;
  }

}
