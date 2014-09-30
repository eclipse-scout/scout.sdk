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
package org.eclipse.scout.sdk.util.internal.typecache;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyResult;
import org.eclipse.scout.sdk.util.typecache.TypeHierarchyConstraints;

/**
 * <h3>{@link ProjectContextTypeHierarchyResult}</h3> A hierarchy result that is limited to a specific
 * {@link IJavaProject} classpath.
 * Further {@link TypeHierarchyConstraints} can be specified limiting the content of the {@link ITypeHierarchyResult}.
 *
 * @author Matthias Villiger
 * @since 4.0.0 13.05.2014
 */
public final class ProjectContextTypeHierarchyResult implements ICacheableTypeHierarchyResult {

  private final EventListenerList m_hierarchyListeners;
  private final TypeHierarchyConstraints m_constraints;
  private final IType m_baseType;

  private Set<IType> m_cachedTypes;

  ProjectContextTypeHierarchyResult(TypeHierarchyConstraints constraints) {
    if (constraints == null) {
      throw new IllegalArgumentException("no constraints specified for type hierarchy!");
    }
    m_hierarchyListeners = new EventListenerList();
    m_baseType = constraints.getBaseType();
    m_constraints = constraints;
    m_cachedTypes = null;
  }

  private void fireHierarchyChanged() {
    for (ITypeHierarchyChangedListener l : m_hierarchyListeners.getListeners(ITypeHierarchyChangedListener.class)) {
      try {
        l.hierarchyInvalidated();
      }
      catch (Exception e) {
        SdkUtilActivator.logError("Error invoking hierarchy changed listener '" + l.getClass().getName() + "'.", e);
      }
    }
  }

  @Override
  public void addHierarchyListener(ITypeHierarchyChangedListener listener) {
    m_hierarchyListeners.add(ITypeHierarchyChangedListener.class, listener);
  }

  @Override
  public void removeHierarchyListener(ITypeHierarchyChangedListener listener) {
    m_hierarchyListeners.remove(ITypeHierarchyChangedListener.class, listener);
  }

  @Override
  public boolean isTypeAccepted(IType candidate, Set<IType> candidateSuperTypes) {
    return m_constraints.isTypeAccepted(candidate, candidateSuperTypes);
  }

  @Override
  public synchronized boolean isCreated() {
    return m_cachedTypes != null;
  }

  @Override
  public synchronized boolean contains(IType t) {
    getTypesCached(null); // load cache
    return m_cachedTypes.contains(t);
  }

  @Override
  public synchronized void invalidate() {
    boolean wasCreated = isCreated();
    m_cachedTypes = null;

    if (wasCreated) {
      // the hierarchy has changed from created (valid) to invalid.
      fireHierarchyChanged();
    }
  }

  @Override
  public IType getBaseType() {
    return m_baseType;
  }

  @Override
  public Set<IType> getAllTypes() {
    return getAllTypes(null);
  }

  @Override
  public Set<IType> getAllTypes(ITypeFilter filter) {
    return getAllTypes(filter, null);
  }

  @Override
  public Set<IType> getAllTypes(ITypeFilter filter, Comparator<IType> comparator) {
    Set<IType> types = null;
    synchronized (this) {
      types = getTypesCached(null);
    }

    if (types == null) {
      return CollectionUtility.hashSet();
    }

    Set<IType> result = null;
    if (comparator == null) {
      result = new HashSet<IType>(types.size());
    }
    else {
      result = new TreeSet<IType>(comparator);
    }

    if (filter == null) {
      for (IType candidate : types) {
        result.add(candidate);
      }
    }
    else {
      for (IType candidate : types) {
        if (filter.accept(candidate)) {
          result.add(candidate);
        }
      }
    }
    return result;
  }

  private Set<IType> getTypesCached(IProgressMonitor monitor) {
    if (m_cachedTypes == null) {
      try {
        final Set<IType> collector = new HashSet<IType>();
        SearchEngine e = new SearchEngine(JavaCore.getWorkingCopies(null));
        e.searchAllTypeNames(null, SearchPattern.R_EXACT_MATCH, null, SearchPattern.R_EXACT_MATCH, m_constraints.getSearchFor(),
            SearchEngine.createStrictHierarchyScope(m_constraints.getClasspath(), getBaseType(), true, m_constraints.isIncludeBaseType(), null),
            new TypeNameMatchRequestor() {
              @Override
              public void acceptTypeNameMatch(TypeNameMatch match) {
                if (m_constraints.modifiersAccepted(match.getModifiers())) {
                  collector.add(match.getType());
                }
              }
            }, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, monitor);
        m_cachedTypes = collector;
      }
      catch (JavaModelException e) {
        SdkUtilActivator.logError("Unable to create type hierarchy for type " + m_constraints.getBaseType().getFullyQualifiedName(), e);
      }
    }
    return m_cachedTypes;
  }

  @Override
  public Iterator<IType> iterator() {
    return getAllTypes().iterator();
  }
}
