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
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 *
 */
public final class PrimaryTypeHierarchy extends AbstractCachedTypeHierarchy {

  PrimaryTypeHierarchy(IType type) {
    super(type);
  }

  @Override
  public boolean isTypeAccepted(IType candidate, Set<IType> candidateSuperTypes) {
    if (TypeUtility.exists(candidate.getDeclaringType())) {
      return false; // don't accept inner types
    }

    return candidateSuperTypes.contains(getBaseType());
  }

  @Override
  protected void revalidate() {
    try {
      setJdtHierarchy(getBaseType().newTypeHierarchy(null));
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logError("Unable to create type hierarchy for type " + getBaseType().getFullyQualifiedName(), e);
    }
  }

  @Override
  public IType getSuperclass(IType type) {
    if (TypeFilters.getPrimaryTypeFilter().accept(type)) {
      return super.getSuperclass(type);
    }
    return null;
  }

  @Override
  protected Set<IType> getTypesFilteredAndSorted(IType[] types, ITypeFilter filter, Comparator<IType> comparator) {
    if (filter == null) {
      filter = TypeFilters.getPrimaryTypeFilter();
    }
    else {
      filter = TypeFilters.getMultiTypeFilter(TypeFilters.getPrimaryTypeFilter(), filter);
    }
    return super.getTypesFilteredAndSorted(types, filter, comparator);
  }
}
