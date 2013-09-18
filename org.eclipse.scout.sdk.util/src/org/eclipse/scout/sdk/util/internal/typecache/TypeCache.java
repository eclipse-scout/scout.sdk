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
package org.eclipse.scout.sdk.util.internal.typecache;

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
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeCache;

/**
 *
 */
public final class TypeCache implements ITypeCache {

  private final static TypeCache INSTANCE = new TypeCache();

  private final Object m_cacheLock;
  private HashMap<String, ArrayList<IType>> m_cache;
  private P_ResourceListener m_resourceChangeListener;

  public static TypeCache getInstance() {
    return INSTANCE;
  }

  private TypeCache() {
    m_cache = new HashMap<String, ArrayList<IType>>();
    m_cacheLock = new Object();
    m_resourceChangeListener = new P_ResourceListener();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(m_resourceChangeListener);
  }

  @Override
  public void dispose() {
    if (m_cache != null) {
      clearCache();
    }
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_resourceChangeListener);
    m_resourceChangeListener = null;
    m_cache = null;
  }

  private void clearCache() {
    synchronized (m_cacheLock) {
      m_cache.clear();
    }
  }

  @Override
  public IType[] getAllCachedTypes() {
    ArrayList<IType> types = new ArrayList<IType>();
    synchronized (m_cacheLock) {
      for (List<IType> lists : m_cache.values()) {
        for (IType t : lists) {
          types.add(t);
        }
      }
    }
    return types.toArray(new IType[types.size()]);
  }

  @Override
  public IType getType(String fullyQualifiedName) {
    ArrayList<IType> types = getTypesInternal(fullyQualifiedName);
    if (types != null) {
      if (types.size() == 1) {
        return types.get(0);
      }
      if (types.size() > 1) {
        // can happen e.g. when multiple jre versions are used in the workspace (e.g. a jre1.5 and a jre1.6 for other bundles).
        SdkUtilActivator.logInfo("found more than one type match for '" + fullyQualifiedName + "' (matches: '" + types.size() + "').");
        return types.get(0);
      }
    }
    return null;
  }

  @Override
  public IType[] getTypes(String fullyQualifiedName) {
    ArrayList<IType> types = getTypesInternal(fullyQualifiedName);
    if (types == null) {
      return new IType[]{};
    }
    return types.toArray(new IType[types.size()]);
  }

  private ArrayList<IType> getTypesInternal(String fullyQualifiedName) {
    if (StringUtility.isNullOrEmpty(fullyQualifiedName)) {
      return null;
    }
    fullyQualifiedName = fullyQualifiedName.replace('$', '.');
    ArrayList<IType> types = null;
    synchronized (m_cacheLock) {
      types = m_cache.get(fullyQualifiedName);
      if (types == null) {
        types = new ArrayList<IType>();
      }
      else if (types.size() > 0) {
        // keep cache clean
        Iterator<IType> it = types.iterator();
        while (it.hasNext()) {
          IType type = it.next();
          if (!TypeUtility.exists(type)) {
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
        SdkUtilActivator.logError("error resolving type '" + fullyQualifiedName + "'.", e);
      }
      synchronized (m_cacheLock) {
        if (types.size() > 0) {
          m_cache.put(fullyQualifiedName, types);
        }
        else {
          m_cache.remove(fullyQualifiedName);
        }
      }
    }
    return types;
  }

  @Override
  public boolean existsType(String fullyQualifiedName) {
    if (StringUtility.isNullOrEmpty(fullyQualifiedName)) {
      return false;
    }
    return TypeUtility.exists(getType(fullyQualifiedName));
  }

  private ArrayList<IType> resolveType(final String fqn) throws CoreException {
    final TreeMap<CompositeLong, IType> matchList = new TreeMap<CompositeLong, IType>();
    //speed tuning, only search for last component of pattern, remaining checks are done in accept
    String fastPat = fqn;
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
              if (t.exists() && t.getJavaProject().exists() && t.getFullyQualifiedName('.').indexOf(fqn) >= 0) {
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
        SdkUtilActivator.logWarning(e);
      }
    }
  }
}
