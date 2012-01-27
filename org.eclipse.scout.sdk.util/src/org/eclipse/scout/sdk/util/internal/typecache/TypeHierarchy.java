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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 *
 */
public class TypeHierarchy implements org.eclipse.scout.sdk.util.typecache.ITypeHierarchy {

  private ITypeHierarchy m_hierarchy;
  private final IType m_type;

  TypeHierarchy(IType type) {
    this(type, null);
  }

  TypeHierarchy(IType type, ITypeHierarchy jdtHierarchy) {
    setJdtHierarchy(jdtHierarchy);
    m_type = type;

  }

  @Override
  public ITypeHierarchy getJdtHierarchy() {
    return m_hierarchy;
  }

  protected void setJdtHierarchy(ITypeHierarchy hierarchy) {
    m_hierarchy = hierarchy;
  }

  @Override
  public boolean contains(IType type) {
    return contains(type, null);
  }

  public boolean contains(IType type, IProgressMonitor monitor) {
    revalidate(monitor);
    return TypeUtility.exists(type) && m_hierarchy.contains(type);
  }

  void revalidate(IProgressMonitor monitor) {
    // void here
  }

  @Override
  public IType getType() {
    return m_type;
  }

  /**
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getAllClasses()
   */
  @Override
  public IType[] getAllClasses() {
    return getAllClasses(null);
  }

  @Override
  public IType[] getAllClasses(ITypeFilter filter) {
    return getAllClasses(filter, null);
  }

  @Override
  public IType[] getAllClasses(ITypeFilter filter, Comparator<IType> comparator) {
    revalidate(null);
    IType[] classes = m_hierarchy.getAllClasses();
    HashSet<IType> unsortedResult = new HashSet<IType>();
    for (IType candidate : classes) {
      if (TypeUtility.exists(candidate)) {
        if (filter == null || filter.accept(candidate)) {
          unsortedResult.add(candidate);
        }
      }
    }
    if (comparator != null) {
      TreeSet<IType> sortedResult = new TreeSet<IType>(comparator);
      sortedResult.addAll(unsortedResult);
      return sortedResult.toArray(new IType[sortedResult.size()]);
    }
    else {
      return unsortedResult.toArray(new IType[unsortedResult.size()]);
    }
  }

  @Override
  public IType[] getAllInterfaces() {
    return getAllInterfaces(null);
  }

  @Override
  public IType[] getAllInterfaces(ITypeFilter filter) {
    return getAllInterfaces(filter, null);
  }

  @Override
  public IType[] getAllInterfaces(ITypeFilter filter, Comparator<IType> comparator) {
    revalidate(null);
    IType[] types = m_hierarchy.getAllInterfaces();
    HashSet<IType> unsortedResult = new HashSet<IType>();
    for (IType candidate : types) {
      if (TypeUtility.exists(candidate)) {
        if (filter == null || filter.accept(candidate)) {
          unsortedResult.add(candidate);
        }
      }
    }
    if (comparator != null) {
      TreeSet<IType> sortedResult = new TreeSet<IType>(comparator);
      sortedResult.addAll(unsortedResult);
      return sortedResult.toArray(new IType[sortedResult.size()]);
    }
    else {
      return unsortedResult.toArray(new IType[unsortedResult.size()]);
    }
  }

  @Override
  public boolean isSubtype(IType type, IType potentialSubtype) {
    HashSet<IType> allSubTypes = new HashSet<IType>(Arrays.asList(getAllSubtypes(type)));
    allSubTypes.add(type);
    return allSubTypes.contains(potentialSubtype);
  }

  @Override
  public boolean containsInSubhierarchy(IType type, IType[] potentialSubtypes) {
    HashSet<IType> allSubTypes = new HashSet<IType>(Arrays.asList(getAllSubtypes(type)));
    allSubTypes.add(type);
    for (IType pt : potentialSubtypes) {
      if (allSubTypes.contains(pt)) {
        return true;
      }
    }
    return false;

  }

  @Override
  public IType[] getAllSubtypes(IType type) {
    return getAllSubtypes(type, null);
  }

  @Override
  public IType[] getAllSubtypes(IType type, ITypeFilter filter) {
    return getAllSubtypes(type, filter, null);
  }

  @Override
  public IType[] getAllSubtypes(IType type, ITypeFilter filter, Comparator<IType> typeComparator) {
    revalidate(null);
    IType[] subtypes = m_hierarchy.getAllSubtypes(type);
    HashSet<IType> unsortedResult = new HashSet<IType>();
    for (IType candidate : subtypes) {
      if (TypeUtility.exists(candidate)) {
        if (filter == null || filter.accept(candidate)) {
          unsortedResult.add(candidate);
        }
      }
    }
    if (typeComparator != null) {
      TreeSet<IType> sortedResult = new TreeSet<IType>(typeComparator);
      sortedResult.addAll(unsortedResult);
      return sortedResult.toArray(new IType[sortedResult.size()]);
    }
    else {
      return unsortedResult.toArray(new IType[unsortedResult.size()]);
    }
  }

  @Override
  public IType[] getAllSuperclasses(IType type) {
    return getAllSuperclasses(type, null);
  }

  @Override
  public IType[] getAllSuperclasses(IType type, ITypeFilter filter) {
    return getAllSuperclasses(type, filter, null);
  }

  @Override
  public IType[] getAllSuperclasses(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidate(null);
    IType[] types = m_hierarchy.getAllSuperclasses(type);
    HashSet<IType> unsortedResult = new HashSet<IType>();
    for (IType candidate : types) {
      if (TypeUtility.exists(candidate)) {
        if (filter == null || filter.accept(candidate)) {
          unsortedResult.add(candidate);
        }
      }
    }
    if (comparator != null) {
      TreeSet<IType> sortedResult = new TreeSet<IType>(comparator);
      sortedResult.addAll(unsortedResult);
      return sortedResult.toArray(new IType[sortedResult.size()]);
    }
    else {
      return unsortedResult.toArray(new IType[unsortedResult.size()]);
    }
  }

  @Override
  public IType[] getAllSuperInterfaces(IType type) {
    return getAllSuperInterfaces(type, null);
  }

  @Override
  public IType[] getAllSuperInterfaces(IType type, ITypeFilter filter) {
    return getAllSuperInterfaces(type, filter, null);
  }

  @Override
  public IType[] getAllSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidate(null);
    IType[] types = m_hierarchy.getAllSuperInterfaces(type);
    HashSet<IType> unsortedResult = new HashSet<IType>();
    for (IType candidate : types) {
      if (TypeUtility.exists(candidate)) {
        if (filter == null || filter.accept(candidate)) {
          unsortedResult.add(candidate);
        }
      }
    }
    if (comparator != null) {
      TreeSet<IType> sortedResult = new TreeSet<IType>(comparator);
      sortedResult.addAll(unsortedResult);
      return sortedResult.toArray(new IType[sortedResult.size()]);
    }
    else {
      return unsortedResult.toArray(new IType[unsortedResult.size()]);
    }
  }

  @Override
  public IType[] getAllSupertypes(IType type) {
    return getAllSupertypes(type, null);
  }

  @Override
  public IType[] getAllSupertypes(IType type, ITypeFilter filter) {
    return getAllSupertypes(type, filter, null);
  }

  @Override
  public IType[] getAllSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidate(null);
    IType[] types = m_hierarchy.getAllSupertypes(type);
    HashSet<IType> unsortedResult = new HashSet<IType>();
    for (IType candidate : types) {
      if (TypeUtility.exists(candidate)) {
        if (filter == null || filter.accept(candidate)) {
          unsortedResult.add(candidate);
        }
      }
    }
    if (comparator != null) {
      TreeSet<IType> sortedResult = new TreeSet<IType>(comparator);
      sortedResult.addAll(unsortedResult);
      return sortedResult.toArray(new IType[sortedResult.size()]);
    }
    else {
      return unsortedResult.toArray(new IType[unsortedResult.size()]);
    }
  }

  /**
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getAllTypes()
   */
  @Override
  public IType[] getAllTypes() {
    return getAllTypes(null);
  }

  @Override
  public IType[] getAllTypes(ITypeFilter filter) {
    return getAllTypes(filter, null);
  }

  @Override
  public IType[] getAllTypes(ITypeFilter filter, Comparator<IType> comparator) {
    revalidate(null);
    IType[] types = m_hierarchy.getAllTypes();
    HashSet<IType> unsortedResult = new HashSet<IType>();
    for (IType candidate : types) {
      if (TypeUtility.exists(candidate)) {
        if (filter == null || filter.accept(candidate)) {
          unsortedResult.add(candidate);
        }
      }
    }
    if (comparator != null) {
      TreeSet<IType> sortedResult = new TreeSet<IType>(comparator);
      sortedResult.addAll(unsortedResult);
      return sortedResult.toArray(new IType[sortedResult.size()]);
    }
    else {
      return unsortedResult.toArray(new IType[unsortedResult.size()]);
    }
  }

  /**
   * @param type
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSubclasses(org.eclipse.jdt.core.IType)
   */
  @Override
  public IType[] getSubclasses(IType type) {
    return getSubclasses(type, null);
  }

  @Override
  public IType[] getSubclasses(IType type, ITypeFilter filter) {
    return getSubclasses(type, filter, null);
  }

  @Override
  public IType[] getSubclasses(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidate(null);
    IType[] types = m_hierarchy.getSubclasses(type);
    HashSet<IType> unsortedResult = new HashSet<IType>();
    for (IType candidate : types) {
      if (TypeUtility.exists(candidate)) {
        if (filter == null || filter.accept(candidate)) {
          unsortedResult.add(candidate);
        }
      }
    }
    if (comparator != null) {
      TreeSet<IType> sortedResult = new TreeSet<IType>(comparator);
      sortedResult.addAll(unsortedResult);
      return sortedResult.toArray(new IType[sortedResult.size()]);
    }
    else {
      return unsortedResult.toArray(new IType[unsortedResult.size()]);
    }
  }

  @Override
  public IType[] getSubtypes(IType type) {
    return getSubtypes(type, null);
  }

  @Override
  public IType[] getSubtypes(IType type, ITypeFilter filter) {
    return getSubtypes(type, filter, null);
  }

  @Override
  public IType[] getSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidate(null);
    IType[] types = m_hierarchy.getSubtypes(type);
    HashSet<IType> unsortedResult = new HashSet<IType>();
    for (IType candidate : types) {
      if (TypeUtility.exists(candidate)) {
        if (filter == null || filter.accept(candidate)) {
          unsortedResult.add(candidate);
        }
      }
    }
    if (comparator != null) {
      TreeSet<IType> sortedResult = new TreeSet<IType>(comparator);
      sortedResult.addAll(unsortedResult);
      return sortedResult.toArray(new IType[sortedResult.size()]);
    }
    else {
      return unsortedResult.toArray(new IType[unsortedResult.size()]);
    }
  }

  /**
   * @param type
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSuperclass(org.eclipse.jdt.core.IType)
   */
  @Override
  public IType getSuperclass(IType type) {
    revalidate(null);
    IType superclass = m_hierarchy.getSuperclass(type);
    if (!TypeUtility.exists(superclass)) {
      superclass = null;
    }
    return superclass;
  }

  /**
   * @param type
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSuperInterfaces(org.eclipse.jdt.core.IType)
   */
  @Override
  public IType[] getSuperInterfaces(IType type) {
    return getSuperInterfaces(type, null);
  }

  @Override
  public IType[] getSuperInterfaces(IType type, ITypeFilter filter) {
    return getSuperInterfaces(type, filter, null);
  }

  @Override
  public IType[] getSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidate(null);
    IType[] types = m_hierarchy.getSuperInterfaces(type);
    HashSet<IType> unsortedResult = new HashSet<IType>();
    for (IType candidate : types) {
      if (TypeUtility.exists(candidate)) {
        if (filter == null || filter.accept(candidate)) {
          unsortedResult.add(candidate);
        }
      }
    }
    if (comparator != null) {
      TreeSet<IType> sortedResult = new TreeSet<IType>(comparator);
      sortedResult.addAll(unsortedResult);
      return sortedResult.toArray(new IType[sortedResult.size()]);
    }
    else {
      return unsortedResult.toArray(new IType[unsortedResult.size()]);
    }
  }

  /**
   * @param type
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSupertypes(org.eclipse.jdt.core.IType)
   */
  @Override
  public IType[] getSupertypes(IType type) {
    return getSupertypes(type, null);
  }

  @Override
  public IType[] getSupertypes(IType type, ITypeFilter filter) {
    return getSupertypes(type, filter, null);
  }

  @Override
  public IType[] getSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidate(null);
    IType[] types = m_hierarchy.getSupertypes(type);
    HashSet<IType> unsortedResult = new HashSet<IType>();
    for (IType candidate : types) {
      if (TypeUtility.exists(candidate)) {
        if (filter == null || filter.accept(candidate)) {
          unsortedResult.add(candidate);
        }
      }
    }
    if (comparator != null) {
      TreeSet<IType> sortedResult = new TreeSet<IType>(comparator);
      sortedResult.addAll(unsortedResult);
      return sortedResult.toArray(new IType[sortedResult.size()]);
    }
    else {
      return unsortedResult.toArray(new IType[unsortedResult.size()]);
    }
  }

}
