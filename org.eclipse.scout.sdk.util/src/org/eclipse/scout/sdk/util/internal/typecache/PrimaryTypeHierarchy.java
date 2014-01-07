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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;

/**
 *
 */
public final class PrimaryTypeHierarchy extends CachedTypeHierarchy implements IPrimaryTypeTypeHierarchy {

  private final static ITypeFilter PRIMARY_TYPE_FILTER = new P_PrimaryTypeFilter();

  /**
   * @param type
   */
  public PrimaryTypeHierarchy(IType type) {
    super(type);
  }

  @Override
  public IType[] getAllSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = PRIMARY_TYPE_FILTER;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, PRIMARY_TYPE_FILTER);
    }
    return super.getAllSubtypes(type, internalFilter, comparator);
  }

  @Override
  public IType[] getAllClasses(ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = PRIMARY_TYPE_FILTER;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, PRIMARY_TYPE_FILTER);
    }
    return super.getAllClasses(internalFilter, comparator);
  }

  @Override
  public IType[] getAllInterfaces(ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = PRIMARY_TYPE_FILTER;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, PRIMARY_TYPE_FILTER);
    }
    return super.getAllInterfaces(internalFilter, comparator);
  }

  @Override
  public IType[] getAllSuperclasses(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = PRIMARY_TYPE_FILTER;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, PRIMARY_TYPE_FILTER);
    }
    return super.getAllSuperclasses(type, internalFilter, comparator);
  }

  @Override
  public IType[] getAllSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = PRIMARY_TYPE_FILTER;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, PRIMARY_TYPE_FILTER);
    }
    return super.getAllSuperInterfaces(type, internalFilter, comparator);
  }

  @Override
  public IType[] getAllSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = PRIMARY_TYPE_FILTER;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, PRIMARY_TYPE_FILTER);
    }
    return super.getAllSupertypes(type, internalFilter, comparator);
  }

  @Override
  public IType[] getAllTypes(ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = PRIMARY_TYPE_FILTER;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, PRIMARY_TYPE_FILTER);
    }
    return super.getAllTypes(internalFilter, comparator);
  }

  @Override
  public IType[] getSubclasses(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = PRIMARY_TYPE_FILTER;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, PRIMARY_TYPE_FILTER);
    }
    return super.getSubclasses(type, internalFilter, comparator);
  }

  @Override
  public IType[] getSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = PRIMARY_TYPE_FILTER;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, PRIMARY_TYPE_FILTER);
    }
    return super.getSubtypes(type, internalFilter, comparator);
  }

  @Override
  public IType getSuperclass(IType type) {
    if (PRIMARY_TYPE_FILTER.accept(type)) {
      return super.getSuperclass(type);
    }
    return null;
  }

  @Override
  public IType[] getSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = PRIMARY_TYPE_FILTER;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, PRIMARY_TYPE_FILTER);
    }
    return super.getSuperInterfaces(type, internalFilter, comparator);
  }

  @Override
  public IType[] getSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    ITypeFilter internalFilter = null;
    if (filter == null) {
      internalFilter = PRIMARY_TYPE_FILTER;
    }
    else {
      internalFilter = TypeFilters.getMultiTypeFilter(filter, PRIMARY_TYPE_FILTER);
    }
    return super.getSupertypes(type, internalFilter, comparator);
  }

  @Override
  public org.eclipse.scout.sdk.util.typecache.ITypeHierarchy combinedTypeHierarchy(IRegion additionalRegion) {
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
      SdkUtilActivator.logError("could not create combined type hierarchy for '" + getType().getFullyQualifiedName() + "'.", e);
      return null;
    }
  }

  @Override
  public org.eclipse.scout.sdk.util.typecache.ITypeHierarchy combinedTypeHierarchy(IJavaElement... additionalElements) {
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
      SdkUtilActivator.logError("could not create combined type hierarchy for '" + getType().getFullyQualifiedName() + "'.", e);
      return null;
    }
  }

  private final static class P_PrimaryTypeFilter implements ITypeFilter {
    @Override
    public boolean accept(IType type) {
      return type.getDeclaringType() == null;
    }
  } // end class P_PrimaryFilter
}
