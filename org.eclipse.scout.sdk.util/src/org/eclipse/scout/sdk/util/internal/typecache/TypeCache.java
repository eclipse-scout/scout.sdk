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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeCache;

public final class TypeCache implements ITypeCache {

  private static final TypeCache INSTANCE = new TypeCache();
  private static final Comparator<IType> COMPARATOR = new P_TypeMatchComparator();

  private final HashMap<String, TreeSet<IType>> m_cache;
  private final P_ResourceListener m_resourceChangeListener;

  public static TypeCache getInstance() {
    return INSTANCE;
  }

  private TypeCache() {
    m_cache = new HashMap<String, TreeSet<IType>>();
    m_resourceChangeListener = new P_ResourceListener();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(m_resourceChangeListener);
  }

  @Override
  public void dispose() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_resourceChangeListener);
    clearCache();
  }

  private synchronized void clearCache() {
    m_cache.clear();
  }

  public synchronized Set<IType> getAllCachedTypes() {
    Set<IType> types = new HashSet<IType>();
    for (Set<IType> lists : m_cache.values()) {
      types.addAll(lists);
    }
    return types;
  }

  @Override
  public IType getType(String typeName) {
    TreeSet<IType> types = getTypesInternal(typeName);
    if (types != null && types.size() > 0) {
      return types.first();
    }
    return null;
  }

  @Override
  public Set<IType> getTypes(String typeName) {
    TreeSet<IType> types = getTypesInternal(typeName);
    if (types == null) {
      return new TreeSet<IType>();
    }
    return new TreeSet<IType>(types);
  }

  private TreeSet<IType> getTypesInternal(String typeName) {
    if (StringUtility.isNullOrEmpty(typeName)) {
      return null;
    }

    typeName = typeName.replace('$', '.');

    TreeSet<IType> types = null;
    synchronized (this) {
      types = m_cache.get(typeName);
      if (types != null) {
        if (types.size() > 0) {
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
          m_cache.remove(typeName);
          types = null;
        }
      }
    }

    if (types == null) {
      // search the type
      try {
        types = resolveType(typeName);
        synchronized (this) {
          if (types != null && types.size() > 0) {
            m_cache.put(typeName, types);
          }
        }
      }
      catch (CoreException e) {
        SdkUtilActivator.logError("error resolving type '" + typeName + "'.", e);
      }
    }
    return types;
  }

  private TreeSet<IType> resolveType(final String fqn) throws CoreException {
    //speed tuning, only search for last component of pattern, remaining checks are done in accept
    String fastPat = Signature.getSimpleName(fqn);
    if (!StringUtility.hasText(fastPat)) {
      return null;
    }

    final TreeSet<IType> matchList = new TreeSet<IType>(COMPARATOR);
    new SearchEngine().search(
        SearchPattern.createPattern(fastPat, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH),
        new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
        SearchEngine.createWorkspaceScope(),
        new SearchRequestor() {
          @Override
          public final void acceptSearchMatch(SearchMatch match) throws CoreException {
            Object element = match.getElement();
            if (element instanceof IType) {
              IType t = (IType) element;
              if (t.getFullyQualifiedName('.').indexOf(fqn) >= 0) {
                matchList.add(t);
              }
            }
          }
        },
        null
        );
    return matchList;
  }

  private static final class P_TypeMatchComparator implements Comparator<IType> {
    @Override
    public int compare(IType o1, IType o2) {
      boolean b1 = o1.isBinary();
      boolean b2 = o2.isBinary();

      if (b1 != b2) {
        if (b1) {
          return 1;
        }
        else {
          return -1;
        }
      }

      return o1.getFullyQualifiedName().compareTo(o2.getFullyQualifiedName());
    }
  }

  private final class P_ResourceListener implements IResourceChangeListener {
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
              }
              return resource.getType() > IResource.PROJECT;// stop visiting at project level
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
