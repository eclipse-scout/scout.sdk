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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchyResult;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyResult;

/**
 * <h3>{@link ICacheableTypeHierarchyResult}</h3> Only used internally for cache handling.
 * 
 * @author Matthias Villiger
 * @since 4.0.0 16.05.2014
 */
public interface ICacheableTypeHierarchyResult extends ICachedTypeHierarchyResult {

  /**
   * @return true if the hierarchy is created false otherwise
   */
  boolean isCreated();

  /**
   * Specifies if the given type would be part of the {@link ITypeHierarchyResult}. This method should not actually use
   * the content of the {@link ITypeHierarchyResult} but instead should check according to the constraints that define
   * the hierarchy.
   * 
   * @param candidate
   *          The candidate to check if it fulfills the {@link ITypeHierarchyResult} constraints.
   * @param candidateSuperTypes
   *          All super types of the given candidate.
   * @return true if the given type fulfills the constraints of this {@link ITypeHierarchyResult} and would therefore be
   *         part of the hierarchy. False otherwise.
   */
  boolean isTypeAccepted(IType candidate, Set<IType> candidateSuperTypes);

}
