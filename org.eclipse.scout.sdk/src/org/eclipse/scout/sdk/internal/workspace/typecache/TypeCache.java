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
package org.eclipse.scout.sdk.internal.workspace.typecache;

import java.util.HashMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.sdk.ScoutSdk;

/**
 *
 */
public class TypeCache {

  private Object m_cacheLock;
  private HashMap<String, IType> m_cache;

  public TypeCache() {
    m_cache = new HashMap<String, IType>();
    m_cacheLock = new Object();
  }

  public void dispose() {
    synchronized (m_cacheLock) {
      m_cache.clear();
    }
  }

  public IType getType(String fullyQualifiedName) {
    fullyQualifiedName = fullyQualifiedName.replaceAll("\\$", "\\.");
    IType type = null;
    synchronized (m_cacheLock) {
      type = m_cache.get(fullyQualifiedName);
      // keep cache clean
      if (type != null && !type.exists()) {
        m_cache.remove(fullyQualifiedName);
        type = null;
      }
    }
    if (type == null) {
      try {
        type = resolveType(fullyQualifiedName);
      }
      catch (CoreException e) {
        ScoutSdk.logError("error during resolving type '" + fullyQualifiedName + "'.", e);
      }
      if (type != null) {
        synchronized (m_cacheLock) {
          m_cache.put(fullyQualifiedName, type);
        }
      }
    }
    return type;
  }

  private IType resolveType(final String fqn) throws CoreException {
    final TreeMap<CompositeLong, IType> matchList = new TreeMap<CompositeLong, IType>();
    //speed tuning, only search for last component of pattern, remaining checks are done in accept
    String fastPat = fqn;
    int i = fastPat.lastIndexOf('.');
    if (i >= 0) {
      fastPat = fastPat.substring(i + 1);
    }
    new SearchEngine().search(
        SearchPattern.createPattern(fastPat, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH),
        new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
        SearchEngine.createWorkspaceScope(),
        new SearchRequestor() {
          @Override
          public final void acceptSearchMatch(SearchMatch match) throws CoreException {
            if (match instanceof TypeDeclarationMatch) {
              TypeDeclarationMatch typeMatch = (TypeDeclarationMatch) match;

              IType t = (IType) typeMatch.getElement();
//              matchList.put(new CompositeLong(t.isBinary() ? 1 : 0, matchList.size()), t);
              if (t.getFullyQualifiedName('.').indexOf(fqn) >= 0) {

                matchList.put(new CompositeLong(t.isBinary() ? 1 : 0, matchList.size()), t);
              }
            }
          }
        },
        null
        );
    if (matchList.size() > 1) {
      ScoutSdk.logWarning("found more than one type matches for '" + fqn + "' (matches: '" + matchList.size() + "').");
    }
    else if (matchList.size() < 1) {
      ScoutSdk.logWarning("found no type matches for '" + fqn + "'.");
      return null;
    }
    return matchList.firstEntry().getValue();
  }

}
