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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

/**
 * <h3>{@link IHierarchyCache}</h3> Factory class to create {@link ITypeHierarchyResult}s. Some of them may be cached
 * for faster re-use.
 * 
 * @author Matthias Villiger
 * @since 3.6.0
 * @see ITypeHierarchy
 * @see ICachedTypeHierarchy
 * @see ICachedTypeHierarchyResult
 */
public interface IHierarchyCache {

  /**
   * Creates a type hierarchy containing this type, all of its supertypes, and all its subtypes.
   * 
   * @param type
   *          The base type of the type hierarchy.
   * @return The type hierarchy. The hierarchy will only be initialized with values on first use and will be cached for
   *         later re-use.
   */
  ICachedTypeHierarchy getTypeHierarchy(IType type);

  /**
   * Creates a primary type hierarchy only containing primary {@link IType}s.<br>
   * Primary types are all except nested types. Or more formally: {@link IType}s for which
   * <code>{@link IType#getDeclaringType()} == null</code>.
   * 
   * @param type
   *          The base type of the primary type hierarchy.
   * @return The primary type hierarchy. The hierarchy will only be initialized with values on first use and will be
   *         cached for later re-use.
   * @throws IllegalArgumentException
   *           if the given type is not a primary type.
   */
  ICachedTypeHierarchy getPrimaryTypeHierarchy(IType type);

  /**
   * Creates a new supertype hierarchy for the given {@link IType}.
   * 
   * @param type
   *          The base type of the supertype hierarchy.
   * @return The new supertype hierarchy of the given type or null if there was an error creating the hierarchy.
   */
  ITypeHierarchy getSupertypeHierarchy(IType type);

  /**
   * Creates a new type hierarchy for the {@link IJavaElement}s in the given {@link IRegion}.<br>
   * Use {@link JavaCore#newRegion()} to create {@link IRegion}s.<br>
   * Note: The resulting {@link ITypeHierarchy} has not base type ({@link ITypeHierarchy#getBaseType()}).
   * 
   * @param region
   *          The {@link IRegion} to create a type hierarchy for.
   * @return The new type hierarchy or null if there was an error creating the hierarchy.
   */
  ITypeHierarchy getLocalTypeHierarchy(IRegion region);

  /**
   * Creates a project context hierarchy for the given search constraints.<br>
   * This is defined as a {@link ITypeHierarchyResult} limited to the given {@link TypeHierarchyConstraints}.<br>
   * Unlike {@link ITypeHierarchy}s {@link ITypeHierarchyResult}s have no level information anymore.<br>
   * This hierarchy result should be preferred over creating an entire primary type hierarchy if the resulting
   * {@link IType}s should be limited to the classpath of a given {@link IJavaProject}.
   * 
   * @param constraints
   *          The constraints that define the hierarchy.
   * @return The project context type hierarchy.
   */
  ICachedTypeHierarchyResult getProjectContextTypeHierarchy(TypeHierarchyConstraints constraints);

  /**
   * Removes and invalidates all cached {@link ICachedTypeHierarchyResult}s.
   */
  void dispose();

  /**
   * Invalidates (but keeps) all hierarchies that are managed by this cache.
   */
  void invalidateAll();

}
