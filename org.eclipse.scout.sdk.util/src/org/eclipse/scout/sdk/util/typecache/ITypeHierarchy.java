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
import java.util.Deque;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.type.ITypeFilter;

/**
 *
 */
public interface ITypeHierarchy extends ITypeHierarchyResult {

  /**
   * @see ITypeHierarchy#getAllSubtypes(IType, ITypeFilter, Comparator)
   */
  Set<IType> getAllSubtypes(IType type);

  /**
   * @see ITypeHierarchy#getAllSubtypes(IType, ITypeFilter, Comparator)
   */
  Set<IType> getAllSubtypes(IType type, ITypeFilter filter);

  /**
   * Returns all resolved subtypes (direct and indirect) of the given type, ordered according to the given comparator
   * limited to the types in this type hierarchy's graph that accept the given {@link ITypeFilter}. An empty {@link Set}
   * is returned if there are no resolved subtypes for the given type and filter.
   * 
   * @param type
   *          the base type for which to get the subtypes
   * @param filter
   *          a type filter to reduce the result or null.
   * @param typeComparator
   *          a comparator to order the result or null.
   * @return all subtypes of the given type accepted by the type filter ordered according the type comparator.
   */
  Set<IType> getAllSubtypes(IType type, ITypeFilter filter, Comparator<IType> typeComparator);

  /**
   * @see ITypeHierarchy#getAllClasses(ITypeFilter, Comparator)
   */
  Set<IType> getAllClasses();

  /**
   * @see ITypeHierarchy#getAllClasses(ITypeFilter, Comparator)
   */
  Set<IType> getAllClasses(ITypeFilter filter);

  /**
   * Returns all resolved classes, ordered according to the given comparator limited to the types in this type
   * hierarchy's graph that accept the given {@link ITypeFilter}. An empty {@link Set} is returned if there are no
   * classes in this hierarchy that fulfill the given filter.<br>
   * All {@link IType}s that are no interface are considered to be a class.
   * 
   * @param filter
   *          a type filter to reduce the result or null.
   * @param comparator
   *          a comparator to order the result or null.
   * @return all classes in the hierarchy graph accepted by the type filter ordered according the type comparator.
   */
  Set<IType> getAllClasses(ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see ITypeHierarchy#getAllInterfaces(ITypeFilter, Comparator)
   */
  Set<IType> getAllInterfaces();

  /**
   * @see ITypeHierarchy#getAllInterfaces(ITypeFilter, Comparator)
   */
  Set<IType> getAllInterfaces(ITypeFilter filter);

  /**
   * Returns all interfaces in this type hierarchy's graph, ordered according to the given comparator limited to the
   * interfaces in this type hierarchy's graph that accept the given {@link ITypeFilter}.<br>
   * An empty {@link Set} is returned if there are no interfaces in this hierarchy that fulfill the given filter.<br>
   * Any interfaces in the creation region which were not resolved to have any subtypes or supertypes are not included
   * in the result.
   * 
   * @param filter
   *          a type filter to reduce the result or null.
   * @param comparator
   *          a comparator to order the result or null.
   * @return all interfaces in the hierarchy graph accepted by the type filter ordered according the type comparator.
   */
  Set<IType> getAllInterfaces(ITypeFilter filter, Comparator<IType> comparator);

  /**
   * Checks if the given potentialSubtype is a subtype of type.<br>
   * <br>
   * If type and potentialSubtype are equal, this method returns always {@code true}. <br>
   * Otherwise it checks if potentialSubtype is a subtype of type according to this hierarchy. This means that if type
   * or potentialSubtype are not part of the hierarchy, this method returns {@code false}, as long as the types are not
   * equal.
   * 
   * @param type
   *          The base type.
   * @param potentialSubtype
   *          The type that should be checked if it is a subtype of type.
   * @return {@code true} if type and potentialSubtype are equal or potentialSubtype is a subtype of type according to
   *         this hierarchy. {@code false} otherwise.
   */
  boolean isSubtype(IType type, IType potentialSubtype);

  /**
   * Builds the super classes stack for the given {@link IType} in bottom-up order.<br>
   * The given {@link IType} is always part of the {@link Deque} and can therefore be found at the first position.<br>
   * You can use {@link Deque#descendingIterator()} to loop through the classes in top-down order.<br>
   * Note: {@link java.lang.Object} is never part of the super classes stack.
   * 
   * @param startType
   *          The start {@link IType}. Will always be part of the resulting {@link Deque}.
   * @return The super classes stack in bottom-up order.
   * @see Deque
   * @see Deque#descendingIterator()
   */
  Deque<IType> getSuperClassStack(IType startType);

  /**
   * Builds the super classes stack for the given {@link IType} in bottom-up order.<br>
   * The given {@link IType} itself is only part of the {@link Deque} if includeStartType is set to <code>true</code>.
   * Then it can therefore be found at the first position.<br>
   * You can use {@link Deque#descendingIterator()} to loop through the classes in top-down order.<br>
   * Note: {@link java.lang.Object} is never part of the super classes stack.
   * 
   * @param startType
   *          The start {@link IType}.
   * @param includeStartType
   *          Specifies if the given startType should be part of the {@link Deque} (<code>true</code>) or not (
   *          <code>false</code>).
   * @return The super classes stack in bottom-up order.
   * @see Deque
   * @see Deque#descendingIterator()
   */
  Deque<IType> getSuperClassStack(IType startType, boolean includeStartType);

  /**
   * @see ITypeHierarchy#getAllSuperclasses(IType, ITypeFilter, Comparator)
   */
  Set<IType> getAllSuperclasses(IType type);

  /**
   * @see ITypeHierarchy#getAllSuperclasses(IType, ITypeFilter, Comparator)
   */
  Set<IType> getAllSuperclasses(IType type, ITypeFilter filter);

  /**
   * Returns all resolved superclasses of the given type, ordered according to the given comparator. An empty
   * {@link Set} is returned if there are no resolved superclasses for the given class that fulfill the given
   * {@link ITypeFilter}.<br>
   * NOTE: once a type hierarchy has been created, it is more efficient to query the hierarchy for superclasses than to
   * query a class recursively up the superclass chain. Querying an element performs a dynamic resolution, whereas the
   * hierarchy returns a pre-computed result.<br>
   * All {@link IType}s that are no interface are considered to be a class.
   * 
   * @param type
   *          the base for which to get the superclasses.
   * @param filter
   *          a type filter to reduce the result or null.
   * @param comparator
   *          a comparator to order the result or null.
   * @return all subclasses of the given type accepted by the type filter ordered according the type comparator.
   */
  Set<IType> getAllSuperclasses(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see ITypeHierarchy#getAllSuperInterfaces(IType, ITypeFilter, Comparator)
   */
  Set<IType> getAllSuperInterfaces(IType type);

  /**
   * @see ITypeHierarchy#getAllSuperInterfaces(IType, ITypeFilter, Comparator)
   */
  Set<IType> getAllSuperInterfaces(IType type, ITypeFilter filter);

  /**
   * Returns all resolved superinterfaces (direct and indirect) of the given type ordered by the given comparator. If
   * the given type is a class, this includes all superinterfaces of all superclasses. An empty {@link Set} is returned
   * if there are no resolved superinterfaces for the given type that fulfill the given {@link ITypeFilter}.<br>
   * NOTE: once a type hierarchy has been created, it is more efficient to query the hierarchy for superinterfaces than
   * to query a type recursively. Querying an element performs a dynamic resolution, whereas the hierarchy returns a
   * pre-computed result.
   * 
   * @param type
   *          the base type for which to get the superinterfaces.
   * @param filter
   *          a type filter to reduce the result or null.
   * @param comparator
   *          a comparator to order the result or null.
   * @return all super interfaces of the given type accepted by the type filter ordered according the type comparator.
   */
  Set<IType> getAllSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see ITypeHierarchy#getAllSupertypes(IType, ITypeFilter, Comparator)
   */
  Set<IType> getAllSupertypes(IType type);

  /**
   * @see ITypeHierarchy#getAllSupertypes(IType, ITypeFilter, Comparator)
   */
  Set<IType> getAllSupertypes(IType type, ITypeFilter filter);

  /**
   * Returns all resolved supertypes of the given type, ordered according to the given comparator. An empty {@link Set}
   * is returned if there are no resolved supertypes for the given type and {@link ITypeFilter}<br>
   * Note that java.lang.Object is NOT considered to be a supertype of any interface type.<br>
   * NOTE: once a type hierarchy has been created, it is more efficient to query the hierarchy for supertypes than to
   * query a type recursively up the supertype chain. Querying an element performs a dynamic resolution, whereas the
   * hierarchy returns a pre-computed result.
   * 
   * @param type
   *          the base type for which to get the supertypes.
   * @param filter
   *          a type filter to reduce the result or null.
   * @param comparator
   *          a comparator to order the result or null.
   * @return all supertypes of the given type accepted by the type filter ordered according the type comparator.
   */
  Set<IType> getAllSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see ITypeHierarchy#getSubclasses(IType, ITypeFilter, Comparator)
   */
  Set<IType> getSubclasses(IType type);

  /**
   * @see ITypeHierarchy#getSubclasses(IType, ITypeFilter, Comparator)
   */
  Set<IType> getSubclasses(IType type, ITypeFilter filter);

  /**
   * Returns the direct resolved subclasses of the given class, ordered according to the given comparator, limited to
   * the classes in this type hierarchy's graph and the given {@link ITypeFilter}. Returns an empty {@link Set} if the
   * given type is an interface, or if no classes were resolved to be subclasses of the given class according to the
   * given {@link ITypeFilter}.<br>
   * All {@link IType}s that are no interface are considered to be a class.
   * 
   * @param type
   *          the base type for which to get the subclasses
   * @param filter
   *          a type filter to reduce the result or null.
   * @param comparator
   *          a comparator to order the result or null.
   * @return all <b>direct</b> subclasses of the given type accepted by the type filter ordered according the type
   *         comparator.
   */
  Set<IType> getSubclasses(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see ITypeHierarchy#getSubtypes(IType, ITypeFilter, Comparator)
   */
  Set<IType> getSubtypes(IType type);

  /**
   * @see ITypeHierarchy#getSubtypes(IType, ITypeFilter, Comparator)
   */
  Set<IType> getSubtypes(IType type, ITypeFilter filter);

  /**
   * Returns the direct resolved subtypes of the given type, ordered according to the given comparator, limited to the
   * types in this type hierarchy's graph that are accepted by the given {@link ITypeFilter}.<br>
   * If the type is a class, this returns the resolved subclasses. If the type is an interface, this returns both the
   * classes which implement the interface and the interfaces which extend it.
   * 
   * @param type
   *          the context type.
   * @param filter
   *          a type filter to reduce the result or null.
   * @param comparator
   *          a comparator to order the result or null.
   * @return all <b>direct</b> subtypes of the given type accepted by the type filter ordered according the type
   *         comparator.
   */
  Set<IType> getSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * Returns the resolved superclass of the given class, or null if the given class has no existing superclass, the
   * superclass could not be resolved, or if the given type is an interface.
   * 
   * @param type
   *          The type for which to get the superclass.
   * @return The resolved superclass or null.
   */
  IType getSuperclass(IType type);

  /**
   * @see #getSuperInterfaces(IType, ITypeFilter, Comparator)
   */
  Set<IType> getSuperInterfaces(IType type);

  /**
   * @see #getSuperInterfaces(IType, ITypeFilter, Comparator)
   */
  Set<IType> getSuperInterfaces(IType type, ITypeFilter filter);

  /**
   * Returns the direct resolved interfaces that the given type implements or extends, ordered by the given comparator,
   * limited to the interfaces in this type hierarchy's graph that accept the given {@link ITypeFilter}.<br>
   * For classes, this gives the interfaces that the class implements. For interfaces, this gives the interfaces that
   * the interface extends.
   * 
   * @param type
   *          The type for which to get the super interfaces.
   * @param filter
   *          a type filter to reduce the result or null.
   * @param comparator
   *          a comparator to order the result or null.
   * @return all <b>direct</b> super interfaces of the given type accepted by the type filter ordered according the type
   *         comparator.
   */
  Set<IType> getSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator);

  /**
   * @see #getSupertypes(IType, ITypeFilter, Comparator)
   */
  Set<IType> getSupertypes(IType type);

  /**
   * @see #getSupertypes(IType, ITypeFilter, Comparator)
   */
  Set<IType> getSupertypes(IType type, ITypeFilter filter);

  /**
   * Returns the resolved supertypes of the given type, ordered by the given comparator, limited to the types in this
   * type hierarchy's graph that accept the given {@link ITypeFilter}.<br>
   * For classes, this returns its superclass and the interfaces that the class implements. For interfaces, this returns
   * the interfaces that the interface extends.<br>
   * As a consequence {@link Object} is NOT considered to be a supertype of any interface type.
   * 
   * @param type
   *          The type for which to get the supertypes.
   * @param filter
   *          a type filter to reduce the result or null.
   * @param comparator
   *          a comparator to order the result or null.
   * @return all <b>direct</b> supertypes of the given type accepted by the type filter ordered according the type
   *         comparator.
   */
  Set<IType> getSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator);

}
