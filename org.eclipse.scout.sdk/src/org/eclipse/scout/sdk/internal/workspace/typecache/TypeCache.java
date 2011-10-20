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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 *
 */
public class TypeCache {

  private Object m_cacheLock;
  private HashMap<String, List<IType>> m_cache;
  private P_ResourceListener m_resourceChangeListener;

  public TypeCache() {
    m_cache = new HashMap<String, List<IType>>();
    m_cacheLock = new Object();
    m_resourceChangeListener = new P_ResourceListener();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(m_resourceChangeListener);
  }

  public void dispose() {
    clearCache();
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_resourceChangeListener);
    m_resourceChangeListener = null;
  }

  private void clearCache() {
    synchronized (m_cacheLock) {
      m_cache.clear();
    }
  }

  public IType getType(String fullyQualifiedName) {
    IType[] types = getTypes(fullyQualifiedName);
    if (types.length == 1) {
      return types[0];
    }
    else if (types.length > 1) {
      ScoutSdk.logWarning("found more than one type matches for '" + fullyQualifiedName + "' (matches: '" + types.length + "').");
      return types[0];
    }
    return null;
  }

  public IType[] getTypes(String fullyQualifiedName) {
    if (StringUtility.isNullOrEmpty(fullyQualifiedName)) {
      return new IType[0];
    }
    fullyQualifiedName = fullyQualifiedName.replaceAll("\\$", "\\.");
    List<IType> types = new ArrayList<IType>();
    synchronized (m_cacheLock) {
      types = m_cache.get(fullyQualifiedName);
      if (types == null) {
        types = new ArrayList<IType>();
      }
      // keep cache clean
      if (types.size() > 0) {
        Iterator<IType> it = types.iterator();
        while (it.hasNext()) {
          IType type = it.next();
          if (type != null && !type.exists()) {
            it.remove();
          }
        }
      }
      if (types.size() == 0) {
        m_cache.remove(fullyQualifiedName);
      }
    }
    if (types.size() == 0) {
      try {
        types = resolveType(fullyQualifiedName);
      }
      catch (CoreException e) {
        ScoutSdk.logError("error during resolving type '" + fullyQualifiedName + "'.", e);
      }
      synchronized (m_cacheLock) {
        if (types.size() > 0) {
          m_cache.put(fullyQualifiedName, types);
        }
        else {
          m_cache.remove(fullyQualifiedName);
          ScoutSdk.logWarning("could not resolve type '" + fullyQualifiedName + "'.");
        }
      }
    }
    return types.toArray(new IType[types.size()]);
  }

  public boolean existsType(String fullyQualifiedName) {
    if (StringUtility.isNullOrEmpty(fullyQualifiedName)) {
      return false;
    }
    return TypeUtility.exists(getType(fullyQualifiedName));
  }

  private List<IType> resolveType(final String fqn) throws CoreException {
    if (StringUtility.isNullOrEmpty(fqn)) {
      return new ArrayList<IType>();
    }

    final TreeMap<CompositeLong, IType> matchList = new TreeMap<CompositeLong, IType>();
    //speed tuning, only search for last component of pattern, remaining checks are done in accept
    String fastPat = fqn.replaceAll("\\$", "\\.");
    int i = fastPat.lastIndexOf('.');
    if (i >= 0) {
      fastPat = fastPat.substring(i + 1);
    }
    if (!StringUtility.hasText(fastPat)) {
      return new ArrayList<IType>();
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

    return new ArrayList<IType>(matchList.values());
  }

  private class P_ResourceListener implements IResourceChangeListener {
    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
      IResourceDelta delta = event.getDelta();
      try {
        if (delta != null) {
          delta.accept(new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta visitDelta) {
              IResource resource = visitDelta.getResource();
              if (resource.getType() == IResource.PROJECT && ((visitDelta.getFlags() & (IResourceDelta.OPEN | IResourceDelta.REMOVED)) != 0)) {
                clearCache();
                return false;
              }
              return true;
            }

          });
        }
        else if (event.getType() == IResourceChangeEvent.PRE_DELETE && event.getResource().getType() == IResource.PROJECT) {
          clearCache();
        }
      }
      catch (CoreException e) {
        ScoutSdk.logWarning(e);
      }
    }
  }

}
