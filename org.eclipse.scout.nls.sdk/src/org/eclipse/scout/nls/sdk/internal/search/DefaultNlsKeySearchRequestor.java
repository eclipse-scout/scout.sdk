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
package org.eclipse.scout.nls.sdk.internal.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.search.ui.text.Match;

/** <h4>DefaultNlsKeySearchRequestor</h4> */
public class DefaultNlsKeySearchRequestor extends AbstractNlsKeySearchRequestor {
  private Map<String, List<Match>> m_matches;
  private EventListenerList m_eventListeners = new EventListenerList();

  /**
   * @param project
   */
  public DefaultNlsKeySearchRequestor(INlsProject project) {
    super(project);
  }

  public void addFindReferencesListener(INlsKeySearchListener listener) {
    m_eventListeners.add(INlsKeySearchListener.class, listener);
  }

  public void removeFindReferencesListener(INlsKeySearchListener listener) {
    m_eventListeners.remove(INlsKeySearchListener.class, listener);
  }

  @Override
  public void beginReporting() {
    m_matches = new HashMap<String, List<Match>>();
    for (INlsKeySearchListener l : m_eventListeners.getListeners(INlsKeySearchListener.class)) {
      l.beginReporting();
    }
  }

  @Override
  protected void acceptMatch(String statement, SearchMatch match) {
    String nlsKey = parseKey(statement, match);
    if (!StringUtility.isNullOrEmpty(nlsKey)) {
      Match nlsMatch = new Match(match.getResource(), match.getOffset(), statement.length());
      acceptNlsKeyMatch(nlsKey, nlsMatch);
    }

  }

  protected void acceptNlsKeyMatch(String nlsKey, Match match) {
    List<Match> list = m_matches.get(nlsKey);
    if (list == null) {
      list = new ArrayList<Match>();
      m_matches.put(nlsKey, list);
    }
    list.add(match);
    for (INlsKeySearchListener l : m_eventListeners.getListeners(INlsKeySearchListener.class)) {
      l.foundMatch(nlsKey, match);
    }
  }

  @Override
  public void endReporting() {
    for (INlsKeySearchListener l : m_eventListeners.getListeners(INlsKeySearchListener.class)) {
      l.endReporting();
    }
  }

  private String parseKey(String statement, SearchMatch match) {
    if (match.getElement() instanceof IImportDeclaration) {
      return null;
    }
    // statement = statement.replaceAll("\n", "");
    String elementName = getProject().getNlsAccessorType().getElementName();
    int startIndex = statement.indexOf(elementName) + elementName.length();
    if (startIndex > 0 && startIndex < statement.length()) {
      statement = statement.substring(startIndex);
    }
    Pattern p = Pattern.compile("\\A\\s*\\.\\s*get\\s*\\(\\s*\\\"([^\\\"]*)\\\"\\s*[\\)\\,\\s]{1}", Pattern.MULTILINE);
    // Create a matcher with an input string
    Matcher m = p.matcher(statement);
    if (m.find()) {
      return statement.substring(m.start(1), m.end(1));
    }
    return null;
  }

  public Match[] getMatches(String nlsKey) {
    List<Match> list = m_matches.get(nlsKey);
    if (list == null) {
      list = new ArrayList<Match>();
    }
    return list.toArray(new Match[list.size()]);
  }

  public Map<String, List<Match>> getAllMatches() {
    return new HashMap<String, List<Match>>(m_matches);
  }

}
