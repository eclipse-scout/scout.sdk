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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * Type Hierarchy implementation
 */
public class TypeHierarchy implements org.eclipse.scout.sdk.util.typecache.ITypeHierarchy {

  private ITypeHierarchy m_hierarchy;
  private IType m_type;

  TypeHierarchy(IType type) {
    this(type, null);
  }

  TypeHierarchy(IType type, ITypeHierarchy jdtHierarchy) {
    m_hierarchy = jdtHierarchy;
    m_type = type;
  }

  protected ITypeHierarchy getJdtHierarchy() {
    return m_hierarchy;
  }

  protected void setJdtHierarchy(ITypeHierarchy hierarchy) {
    m_hierarchy = hierarchy;
  }

  @Override
  public boolean contains(IType type) {
    if (type == null) {
      return false;
    }
    return m_hierarchy.contains(type);
  }

  @Override
  public IType getBaseType() {
    return m_type;
  }

  /**
   * Does not set a new hierarchy!
   *
   * @param newBaseType
   */
  protected void setBaseType(IType newBaseType) {
    m_type = newBaseType;
  }

  /**
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getAllClasses()
   */
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
    IType[] classes = m_hierarchy.getAllClasses();
    return getTypesFilteredAndSorted(classes, filter, comparator);
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
    IType[] types = m_hierarchy.getAllInterfaces();
    return getTypesFilteredAndSorted(types, filter, comparator);
  }

  @Override
  public Deque<IType> getSuperClassStack(IType startType, boolean includeStartType) {
    LinkedList<IType> result = new LinkedList<IType>();
    if (startType == null) {
      return result;
    }
    IType cur = null;
    if (includeStartType) {
      cur = startType;
    }
    else {
      cur = getSuperclass(startType);
    }
    while (TypeUtility.exists(cur) && !Object.class.getName().equals(cur.getFullyQualifiedName())) {
      result.add(cur);
      cur = getSuperclass(cur);
    }
    return result;
  }

  @Override
  public Deque<IType> getSuperClassStack(IType startType) {
    return getSuperClassStack(startType, true);
  }

  @Override
  public boolean isSubtype(IType type, IType potentialSubtype) {
    if (type == null || potentialSubtype == null) {
      return false;
    }
    if (CompareUtility.equals(type, potentialSubtype)) {
      return true;
    }

    IType[] subtypes = m_hierarchy.getAllSubtypes(type);
    for (IType t : subtypes) {
      if (CompareUtility.equals(t, potentialSubtype)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Set<IType> getAllSubtypes(IType type) {
    return getAllSubtypes(type, null);
  }

  @Override
  public Set<IType> getAllSubtypes(IType type, ITypeFilter filter) {
    return getAllSubtypes(type, filter, null);
  }

  @Override
  public Set<IType> getAllSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    IType[] subtypes = m_hierarchy.getAllSubtypes(type);
    return getTypesFilteredAndSorted(subtypes, filter, comparator);
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
    IType[] types = m_hierarchy.getAllSuperclasses(type);
    return getTypesFilteredAndSorted(types, filter, comparator);
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
    IType[] types = m_hierarchy.getAllSuperInterfaces(type);
    return getTypesFilteredAndSorted(types, filter, comparator);
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
    IType[] types = m_hierarchy.getAllSupertypes(type);
    return getTypesFilteredAndSorted(types, filter, comparator);
  }

  /**
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getAllTypes()
   */
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
    IType[] types = m_hierarchy.getAllTypes();
    return getTypesFilteredAndSorted(types, filter, comparator);
  }

  /**
   * @param type
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSubclasses(org.eclipse.jdt.core.IType)
   */
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
    IType[] types = m_hierarchy.getSubclasses(type);
    return getTypesFilteredAndSorted(types, filter, comparator);
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
    IType[] types = m_hierarchy.getSubtypes(type);
    return getTypesFilteredAndSorted(types, filter, comparator);
  }

  /**
   * @param type
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSuperclass(org.eclipse.jdt.core.IType)
   */
  @Override
  public IType getSuperclass(IType type) {
    IType superclass = m_hierarchy.getSuperclass(type);
    if (!TypeUtility.exists(superclass)) {
      return null;
    }
    return superclass;
  }

  /**
   * @param type
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSuperInterfaces(org.eclipse.jdt.core.IType)
   */
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
    IType[] types = m_hierarchy.getSuperInterfaces(type);
    return getTypesFilteredAndSorted(types, filter, comparator);
  }

  /**
   * @param type
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSupertypes(org.eclipse.jdt.core.IType)
   */
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
    IType[] types = m_hierarchy.getSupertypes(type);
    return getTypesFilteredAndSorted(types, filter, comparator);
  }

  protected Set<IType> getTypesFilteredAndSorted(IType[] types, ITypeFilter filter, Comparator<IType> comparator) {
    Set<IType> result = null;
    if (comparator == null) {
      result = new HashSet<IType>(types.length);
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

  @Override
  public Iterator<IType> iterator() {
    return getAllTypes().iterator();
  }
}
