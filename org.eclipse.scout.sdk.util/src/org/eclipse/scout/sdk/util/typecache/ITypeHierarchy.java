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
package org.eclipse.scout.sdk.util.typecache;

import java.util.Comparator;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.type.ITypeFilter;

/**
 *
 */
public interface ITypeHierarchy {

  /**
   * @return the type the hierarchy was created for (origin).
   */
  IType getType();

  /**
   * @param type
   * @return true if the given type is part of the hierarchy false otherwise
   */
  boolean contains(IType type);

  /**
   * @see ITypeHierarchy#getAllSubtypes(IType, ITypeFilter, Comparator)
   */
  IType[] getAllSubtypes(IType type);

  /**
   * @see ITypeHierarchy#getAllSubtypes(IType, ITypeFilter, Comparator)
   */
  IType[] getAllSubtypes(IType type, ITypeFilter typeFilter);

  /**
   * @param type
   *          the context type to get sub types.
   * @param typeFilter
   *          a type filter to reduce the result types or null.
   * @param typeComparator
   *          a comparator to order the result types or null.
   * @return all subtypes of the given type accepted by the type filter ordered according the type comparator.
   */
  IType[] getAllSubtypes(IType type, ITypeFilter typeFilter, Comparator<IType> typeComparator);

  /**
   * @see ITypeHierarchy#getAllClasses(ITypeFilter, Comparator)
   */
  IType[] getAllClasses();

  /**
   * @see ITypeHierarchy#getAllClasses(ITypeFilter, Comparator)
   */
  IType[] getAllClasses(ITypeFilter filter);

  /**
   * @param typeFilter
   *          a type filter to reduce the result types or null.
   * @param comparator
   *          a comparator to order the result types or null.
   * @return all classes in the hierarchy graph accepted by the type filter ordered according the type comparator.
   * @see org.eclipse.jdt.core.ITypeHierarchy#getAllClasses()
   */
  IType[] getAllClasses(ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see ITypeHierarchy#getAllInterfaces(ITypeFilter, Comparator)
   */
  IType[] getAllInterfaces();

  /**
   * @see ITypeHierarchy#getAllInterfaces(ITypeFilter, Comparator)
   */
  IType[] getAllInterfaces(ITypeFilter filter);

  /**
   * @param typeFilter
   *          a type filter to reduce the result types or null.
   * @param comparator
   *          a comparator to order the result types or null.
   * @return all interfaces in the hierarchy graph accepted by the type filter ordered according the type comparator.
   * @see org.eclipse.jdt.core.ITypeHierarchy#getAllInterfaces()
   */
  IType[] getAllInterfaces(ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @param type
   * @param potentialSubtype
   * @return true if the potentialSubtype is in the sub hierarchy of the given type.
   */
  boolean isSubtype(IType type, IType potentialSubtype);

  /**
   * @param type
   * @param potentialSubtypes
   * @return true if one of the potentialSubtypes is a subtype of the given type
   */
  boolean containsInSubHierarchy(IType type, IType[] potentialSubtypes);

  /**
   * @see ITypeHierarchy#getAllSuperclasses(IType, ITypeFilter, Comparator)
   */
  IType[] getAllSuperclasses(IType type);

  /**
   * @see ITypeHierarchy#getAllSuperclasses(IType, ITypeFilter, Comparator)
   */
  IType[] getAllSuperclasses(IType type, ITypeFilter filter);

  /**
   * @param type
   *          the context type.
   * @param typeFilter
   *          a type filter to reduce the result types or null.
   * @param comparator
   *          a comparator to order the result types or null.
   * @return all subclasses of the given type accepted by the type filter ordered according the type comparator.
   * @see org.eclipse.jdt.core.ITypeHierarchy#getAllSuperclasses(org.eclipse.jdt.core.IType)
   */
  IType[] getAllSuperclasses(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see ITypeHierarchy#getAllSuperInterfaces(IType, ITypeFilter, Comparator)
   */
  IType[] getAllSuperInterfaces(IType type);

  /**
   * @see ITypeHierarchy#getAllSuperInterfaces(IType, ITypeFilter, Comparator)
   */
  IType[] getAllSuperInterfaces(IType type, ITypeFilter filter);

  /**
   * @param type
   *          the context type.
   * @param typeFilter
   *          a type filter to reduce the result types or null.
   * @param comparator
   *          a comparator to order the result types or null.
   * @return all interfaces of the given type accepted by the type filter ordered according the type comparator.
   * @see org.eclipse.jdt.core.ITypeHierarchy#getAllSuperInterfaces(IType)
   */
  IType[] getAllSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see ITypeHierarchy#getAllSupertypes(IType, ITypeFilter, Comparator)
   */
  IType[] getAllSupertypes(IType type);

  /**
   * @see ITypeHierarchy#getAllSupertypes(IType, ITypeFilter, Comparator)
   */
  IType[] getAllSupertypes(IType type, ITypeFilter filter);

  /**
   * @param type
   *          the context type.
   * @param typeFilter
   *          a type filter to reduce the result types or null.
   * @param comparator
   *          a comparator to order the result types or null.
   * @return all super types of the given type accepted by the type filter ordered according the type comparator.
   * @see org.eclipse.jdt.core.ITypeHierarchy#getAllSupertypes(IType)
   */
  IType[] getAllSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see ITypeHierarchy#getAllTypes(ITypeFilter, Comparator)
   */
  IType[] getAllTypes();

  /**
   * @see ITypeHierarchy#getAllTypes(ITypeFilter, Comparator)
   */
  IType[] getAllTypes(ITypeFilter filter);

  /**
   * @param typeFilter
   *          a type filter to reduce the result types or null.
   * @param comparator
   *          a comparator to order the result types or null.
   * @return all types in the hierarchy graph accepted by the type filter ordered according the type comparator.
   * @see org.eclipse.jdt.core.ITypeHierarchy#getAllTypes()
   */
  IType[] getAllTypes(ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see ITypeHierarchy#getSubclasses(IType, ITypeFilter, Comparator)
   */
  IType[] getSubclasses(IType type);

  /**
   * @see ITypeHierarchy#getSubclasses(IType, ITypeFilter, Comparator)
   */
  IType[] getSubclasses(IType type, ITypeFilter filter);

  /**
   * @param type
   *          the context type.
   * @param typeFilter
   *          a type filter to reduce the result types or null.
   * @param comparator
   *          a comparator to order the result types or null.
   * @return all <b>direct</b> sub classes of the given type accepted by the type filter ordered according the type
   *         comparator.
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSubclasses(IType)
   */
  IType[] getSubclasses(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see ITypeHierarchy#getSubtypes(IType, ITypeFilter, Comparator)
   */
  IType[] getSubtypes(IType type);

  /**
   * @see ITypeHierarchy#getSubtypes(IType, ITypeFilter, Comparator)
   */
  IType[] getSubtypes(IType type, ITypeFilter filter);

  /**
   * @param type
   *          the context type.
   * @param typeFilter
   *          a type filter to reduce the result types or null.
   * @param comparator
   *          a comparator to order the result types or null.
   * @return all <b>direct</b> sub types of the given type accepted by the type filter ordered according the type
   *         comparator.
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSubtypes(IType)
   */
  IType[] getSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @param type
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSuperclass(org.eclipse.jdt.core.IType)
   */
  IType getSuperclass(IType type);

  /**
   * @param type
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSuperInterfaces(org.eclipse.jdt.core.IType)
   */
  IType[] getSuperInterfaces(IType type);

  IType[] getSuperInterfaces(IType type, ITypeFilter filter);

  IType[] getSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @return
   */
  org.eclipse.jdt.core.ITypeHierarchy getJdtHierarchy();

  /**
   * @param type
   * @return
   * @see org.eclipse.jdt.core.ITypeHierarchy#getSupertypes(org.eclipse.jdt.core.IType)
   */
  IType[] getSupertypes(IType type);

  /**
   * @param type
   * @param filter
   * @return
   */
  IType[] getSupertypes(IType type, ITypeFilter filter);

  /**
   * @param type
   * @param filter
   * @param comparator
   * @return
   */
  IType[] getSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator);

}
