/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.ui.text.Match;

/**
 * <h3>{@link NlsKeySearchRequestor}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 07.11.2013
 */
public class NlsKeySearchRequestor extends TextSearchRequestor {
  private Map<String, List<Match>> m_matches;
  private EventListenerList m_eventListeners = new EventListenerList();

  @Override
  public void beginReporting() {
    m_matches = new HashMap<String, List<Match>>();
    for (INlsKeySearchListener l : m_eventListeners.getListeners(INlsKeySearchListener.class)) {
      try {
        l.beginReporting();
      }
      catch (Exception t) {
        NlsCore.logError("error during listener notification.", t);
      }
    }
  }

  @Override
  public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {
    Match match = new Match(matchAccess.getFile(), matchAccess.getMatchOffset(), matchAccess.getMatchLength());
    String key = matchAccess.getFileContent(matchAccess.getMatchOffset(), matchAccess.getMatchLength());
    key = key.replaceAll("\\\"", "");
    acceptNlsKeyMatch(key, match);
    return true;
  }

  protected void acceptNlsKeyMatch(String nlsKey, Match match) {
    List<Match> list = m_matches.get(nlsKey);
    if (list == null) {
      list = new ArrayList<Match>();
      m_matches.put(nlsKey, list);
    }
    list.add(match);
    for (INlsKeySearchListener l : m_eventListeners.getListeners(INlsKeySearchListener.class)) {
      try {
        l.foundMatch(nlsKey, match);
      }
      catch (Exception t) {
        NlsCore.logError("error during listener notification.", t);
      }
    }
  }

  @Override
  public void endReporting() {
    for (INlsKeySearchListener l : m_eventListeners.getListeners(INlsKeySearchListener.class)) {
      try {
        l.endReporting();
      }
      catch (Exception t) {
        NlsCore.logError("error during listener notification.", t);
      }
    }
  }

  public void addFindReferencesListener(INlsKeySearchListener listener) {
    m_eventListeners.add(INlsKeySearchListener.class, listener);
  }

  public void removeFindReferencesListener(INlsKeySearchListener listener) {
    m_eventListeners.remove(INlsKeySearchListener.class, listener);
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
