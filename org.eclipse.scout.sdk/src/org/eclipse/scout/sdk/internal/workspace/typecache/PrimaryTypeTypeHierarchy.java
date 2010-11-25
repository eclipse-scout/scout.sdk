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

import java.util.Comparator;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;

/**
 *
 */
public class PrimaryTypeTypeHierarchy extends CachedTypeHierarchy implements IPrimaryTypeTypeHierarchy {

  private ITypeFilter m_primaryTypeFilter = new P_PrimaryTypeFilter();

  /**
   * @param type
   */
  public PrimaryTypeTypeHierarchy(IType type) {
    super(type);
  }

  @Override
  public IType[] getAllSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = m_primaryTypeFilter;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, m_primaryTypeFilter);
    }
    return super.getAllSubtypes(type, internalFilter, comparator);
  }

  @Override
  public IType[] getAllClasses(ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = m_primaryTypeFilter;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, m_primaryTypeFilter);
    }
    return super.getAllClasses(internalFilter, comparator);
  }

  @Override
  public IType[] getAllInterfaces(ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = m_primaryTypeFilter;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, m_primaryTypeFilter);
    }
    return super.getAllInterfaces(internalFilter, comparator);
  }

  @Override
  public IType[] getAllSuperclasses(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = m_primaryTypeFilter;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, m_primaryTypeFilter);
    }
    return super.getAllSuperclasses(type, internalFilter, comparator);
  }

  @Override
  public IType[] getAllSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = m_primaryTypeFilter;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, m_primaryTypeFilter);
    }
    return super.getAllSuperInterfaces(type, internalFilter, comparator);
  }

  @Override
  public IType[] getAllSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = m_primaryTypeFilter;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, m_primaryTypeFilter);
    }
    return super.getAllSupertypes(type, internalFilter, comparator);
  }

  @Override
  public IType[] getAllTypes(ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = m_primaryTypeFilter;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, m_primaryTypeFilter);
    }
    return super.getAllTypes(internalFilter, comparator);
  }

  @Override
  public IType[] getSubclasses(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = m_primaryTypeFilter;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, m_primaryTypeFilter);
    }
    return super.getSubclasses(type, internalFilter, comparator);
  }

  @Override
  public IType[] getSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = m_primaryTypeFilter;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, m_primaryTypeFilter);
    }
    return super.getSubtypes(type, internalFilter, comparator);
  }

  @Override
  public IType getSuperclass(IType type) {
    if (m_primaryTypeFilter.accept(type)) {
      return super.getSuperclass(type);
    }
    return null;
  }

  @Override
  public IType[] getSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = m_primaryTypeFilter;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, m_primaryTypeFilter);
    }
    return super.getSuperInterfaces(type, internalFilter, comparator);
  }

  @Override
  public IType[] getSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = m_primaryTypeFilter;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, m_primaryTypeFilter);
    }
    return super.getSupertypes(type, internalFilter, comparator);
  }

  @Override
  public org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy combinedTypeHierarchy(IRegion additionalRegion) {
    if (additionalRegion == null) {
      throw new IllegalArgumentException("additional region can not be null");
    }
    IRegion region = JavaCore.newRegion();
    for (IJavaElement e : getAllTypes()) {
      region.add(e);
    }
    for (IJavaElement e : additionalRegion.getElements()) {
      region.add(e);
    }
    try {
      ITypeHierarchy hierarchy = JavaCore.newTypeHierarchy(region, null, null);
      return new CombinedHierarchy(getType(), hierarchy);

    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not create combined type hierarchy for '" + getType().getFullyQualifiedName() + "'.", e);
      return null;
    }
  }

  @Override
  public org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy combinedTypeHierarchy(IJavaElement... additionalElements) {
    if (additionalElements == null) {
      throw new IllegalArgumentException("additional region can not be null");
    }
    IRegion region = JavaCore.newRegion();
    for (IJavaElement e : getAllTypes()) {
      region.add(e);
    }
    for (IJavaElement e : additionalElements) {
      region.add(e);
    }
    try {
      ITypeHierarchy hierarchy = JavaCore.newTypeHierarchy(region, null, null);
      return new CombinedHierarchy(getType(), hierarchy);

    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not create combined type hierarchy for '" + getType().getFullyQualifiedName() + "'.", e);
      return null;
    }
  }

//  @Override
//  public IType[] getAllSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator, IRegion additionalRegion) {
//    IRegion region = JavaCore.newRegion();
//    for (IJavaElement e : getAllTypes()) {
//      region.add(e);
//    }
//    for (IJavaElement e : additionalRegion.getElements()) {
//      region.add(e);
//    }
//    try {
//      ITypeHierarchy hierarchy = JavaCore.newTypeHierarchy(region, null, null);
//      IType[] cadidates = hierarchy.getAllSubtypes(type);
//      ArrayList<IType> unsortedTypes = new ArrayList<IType>();
//      for (IType candidate : cadidates) {
//        if (filter.accept(candidate)) {
//          unsortedTypes.add(candidate);
//        }
//      }
//      if (comparator == null) {
//        return unsortedTypes.toArray(new IType[unsortedTypes.size()]);
//      }
//      else {
//        TreeSet<IType> sortedTypes = new TreeSet<IType>(comparator);
//        sortedTypes.addAll(unsortedTypes);
//        return sortedTypes.toArray(new IType[sortedTypes.size()]);
//      }
//    }
//    catch (JavaModelException e) {
//      ScoutSdk.logError("could not create type hierarchy for '" + type.getFullyQualifiedName() + "'.", e);
//      return new IType[0];
//    }
//  }

  private class P_PrimaryTypeFilter implements ITypeFilter {
    @Override
    public boolean accept(IType type) {
      return type.getDeclaringType() == null;
    }
  } // end class P_PrimaryFilter
}
