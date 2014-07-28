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
package org.eclipse.scout.sdk.util.typecache;

import java.util.Comparator;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.type.ITypeFilter;

/**
 * <h3>{@link ITypeHierarchyResult}</h3> Stores the result of a type hierarchy. This may include internal hierarchical
 * graph information or not.
 *
 * @author Matthias Villiger
 * @since 4.0.0 14.05.2014
 */
public interface ITypeHierarchyResult extends Iterable<IType> {

  /**
   * @see #getAllTypes(ITypeFilter, Comparator)
   */
  Set<IType> getAllTypes();

  /**
   * @see #getAllTypes(ITypeFilter, Comparator)
   */
  Set<IType> getAllTypes(ITypeFilter filter);

  /**
   * Returns all types in this hierarchy result, ordered according to the given comparator.
   *
   * @param typeFilter
   *          a type filter to reduce the result or null.
   * @param comparator
   *          a comparator to order the result or null.
   * @return all types in the hierarchy result accepted by the type filter ordered according the type comparator.
   */
  Set<IType> getAllTypes(ITypeFilter filter, Comparator<IType> comparator);

  /**
   * Returns whether the given type is part of this hierarchy result.
   *
   * @param type
   *          The type to search
   * @return true if the given type is part of the hierarchy result. false otherwise.
   */
  boolean contains(IType type);

  /**
   * Gets the {@link IType} this hierarchy result was focused on.
   *
   * @return the {@link IType} the hierarchy was created for (origin) or null if the hierarchy results not from a
   *         {@link IType}.
   */
  IType getBaseType();
}
