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
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;

/**
 * <h3>{@link CachedPrimaryTypeHierarchy}</h3>
 * <p>
 * Wraps a {@link CachedTypeHierarchy} and filters the result such that it contains only primary types (i.e.
 * {@link IType}s for which <code>{@link IType#getDeclaringType()} == null</code>)
 * 
 * @author Michael Schaufelberger
 * @since 4.1.0 04.06.2014
 */
public final class CachedPrimaryTypeHierarchy implements ICachedTypeHierarchy {

  private final ICachedTypeHierarchy m_cachedTypeHierarchy;

  CachedPrimaryTypeHierarchy(ICachedTypeHierarchy cachedTypeHierarchy) {
    m_cachedTypeHierarchy = cachedTypeHierarchy;
  }

  @Override
  public IType getSuperclass(IType type) {
    if (isPrimary(type)) {
      return m_cachedTypeHierarchy.getSuperclass(type);
    }
    return null;
  }

  private static boolean isPrimary(IType t) {
    return TypeFilters.getPrimaryTypeFilter().accept(t);
  }

  @Override
  public boolean isSubtype(IType type, IType potentialSubtype) throws IllegalArgumentException {
    if (!contains(type)) {
      TypeHierarchy.throwTypeUnknown(type);
    }
    if (!isPrimary(potentialSubtype)) {
      return false;
    }
    return m_cachedTypeHierarchy.isSubtype(type, potentialSubtype);
  }

  @Override
  public boolean contains(IType type) {
    if (!isPrimary(type)) {
      return false;
    }
    return m_cachedTypeHierarchy.contains(type);
  }

  protected ITypeFilter getPrimaryTypeFilter(ITypeFilter existingFilter) {
    if (existingFilter == null) {
      return TypeFilters.getPrimaryTypeFilter();
    }
    return TypeFilters.getMultiTypeFilterAnd(TypeFilters.getPrimaryTypeFilter(), existingFilter);
  }

  ////////////////////////////////////////////////////////////////////
  //                                                                //
  //                                                                //
  //                                                                //
  // delegation only (with added PrimaryTypeFilter) below this line //
  //                                                                //
  //                                                                //
  //                                                                //
  ////////////////////////////////////////////////////////////////////

  @Override
  public Set<IType> getAllSubtypes(IType type) {
    return getAllSubtypes(type, null);
  }

  @Override
  public Set<IType> getAllSubtypes(IType type, ITypeFilter filter) {
    return getAllSubtypes(type, filter, null);
  }

  @Override
  public Set<IType> getAllSubtypes(IType type, ITypeFilter filter, Comparator<IType> typeComparator) {
    return m_cachedTypeHierarchy.getAllSubtypes(type, getPrimaryTypeFilter(filter), typeComparator);
  }

  @Override
  public Set<IType> getAllClasses() {
    return getAllClasses(null);
  }

  @Override
  public Set<IType> getAllClasses(ITypeFilter filter) {
    return getAllClasses(filter, null);
  }

  @Override
  public Set<IType> getAllClasses(ITypeFilter filter, Comparator<IType> comparator) {
    return m_cachedTypeHierarchy.getAllClasses(getPrimaryTypeFilter(filter), comparator);
  }

  @Override
  public Set<IType> getAllInterfaces() {
    return getAllInterfaces(null);
  }

  @Override
  public Set<IType> getAllInterfaces(ITypeFilter filter) {
    return getAllInterfaces(filter, null);
  }

  @Override
  public Set<IType> getAllInterfaces(ITypeFilter filter, Comparator<IType> comparator) {
    return m_cachedTypeHierarchy.getAllInterfaces(getPrimaryTypeFilter(filter), comparator);
  }

  @Override
  public Set<IType> getAllSuperclasses(IType type) {
    return getAllSuperclasses(type, null);
  }

  @Override
  public Set<IType> getAllSuperclasses(IType type, ITypeFilter filter) {
    return getAllSuperclasses(type, filter, null);
  }

  @Override
  public Set<IType> getAllSuperclasses(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    return m_cachedTypeHierarchy.getAllSuperclasses(type, getPrimaryTypeFilter(filter), comparator);
  }

  @Override
  public Set<IType> getAllSuperInterfaces(IType type) {
    return getAllSuperInterfaces(type, null);
  }

  @Override
  public Set<IType> getAllSuperInterfaces(IType type, ITypeFilter filter) {
    return getAllSuperInterfaces(type, filter, null);
  }

  @Override
  public Set<IType> getAllSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    return m_cachedTypeHierarchy.getAllSuperInterfaces(type, getPrimaryTypeFilter(filter), comparator);
  }

  @Override
  public Set<IType> getAllSupertypes(IType type) {
    return getAllSupertypes(type, null);
  }

  @Override
  public Set<IType> getAllSupertypes(IType type, ITypeFilter filter) {
    return getAllSupertypes(type, filter, null);
  }

  @Override
  public Set<IType> getAllSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    return m_cachedTypeHierarchy.getAllSupertypes(type, getPrimaryTypeFilter(filter), comparator);
  }

  @Override
  public Set<IType> getSubclasses(IType type) {
    return getSubclasses(type, null);
  }

  @Override
  public Set<IType> getSubclasses(IType type, ITypeFilter filter) {
    return getSubclasses(type, filter, null);
  }

  @Override
  public Set<IType> getSubclasses(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    return m_cachedTypeHierarchy.getSubclasses(type, getPrimaryTypeFilter(filter), comparator);
  }

  @Override
  public Set<IType> getSubtypes(IType type) {
    return getSubtypes(type, null);
  }

  @Override
  public Set<IType> getSubtypes(IType type, ITypeFilter filter) {
    return getSubtypes(type, filter, null);
  }

  @Override
  public Set<IType> getSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    return m_cachedTypeHierarchy.getSubtypes(type, getPrimaryTypeFilter(filter), comparator);
  }

  @Override
  public Set<IType> getSuperInterfaces(IType type) {
    return getSuperInterfaces(type, null);
  }

  @Override
  public Set<IType> getSuperInterfaces(IType type, ITypeFilter filter) {
    return getSuperInterfaces(type, filter, null);
  }

  @Override
  public Set<IType> getSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    return m_cachedTypeHierarchy.getSuperInterfaces(type, getPrimaryTypeFilter(filter), comparator);
  }

  @Override
  public Set<IType> getSupertypes(IType type) {
    return getSupertypes(type, null);
  }

  @Override
  public Set<IType> getSupertypes(IType type, ITypeFilter filter) {
    return getSupertypes(type, filter, null);
  }

  @Override
  public Set<IType> getSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    return m_cachedTypeHierarchy.getSupertypes(type, getPrimaryTypeFilter(filter), comparator);
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
    return m_cachedTypeHierarchy.getAllTypes(getPrimaryTypeFilter(filter), comparator);
  }

  @Override
  public IType getBaseType() {
    return m_cachedTypeHierarchy.getBaseType();
  }

  @Override
  public Iterator<IType> iterator() {
    return m_cachedTypeHierarchy.iterator();
  }

  @Override
  public Deque<IType> getSuperClassStack(IType startType) {
    return m_cachedTypeHierarchy.getSuperClassStack(startType);
  }

  @Override
  public Deque<IType> getSuperClassStack(IType startType, boolean includeStartType) {
    return m_cachedTypeHierarchy.getSuperClassStack(startType, includeStartType);
  }

  @Override
  public void addHierarchyListener(ITypeHierarchyChangedListener listener) {
    m_cachedTypeHierarchy.addHierarchyListener(listener);
  }

  @Override
  public void removeHierarchyListener(ITypeHierarchyChangedListener listener) {
    m_cachedTypeHierarchy.removeHierarchyListener(listener);
  }

  @Override
  public void invalidate() {
    m_cachedTypeHierarchy.invalidate();
  }

}
