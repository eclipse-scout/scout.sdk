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
package org.eclipse.scout.sdk.util.signature;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;

/**
 * <h3>{@link ITypeParameterMapping}</h3><br>
 * Describes resolved type parameters on a specific type.<br>
 * Such {@link ITypeParameterMapping}s can be obtained using {@link SignatureUtility#resolveTypeParameters(IType)} or
 * one of its overloads.
 *
 * @author Matthias Villiger
 * @since 4.2.0 10.12.2014
 * @see IResolvedTypeParameter
 */
public interface ITypeParameterMapping {

  /**
   * Gets all type parameters that are defined for the this type ({@link #getFullyQualifiedName()}).<br>
   *
   * @return A {@link Map} containing the type parameter name (e.g. "T") as key and the corresponding
   *         {@link IResolvedTypeParameter} as value. The map iterators return the parameters in the order as they are
   *         defined in the source code.
   */
  Map<String, IResolvedTypeParameter> getTypeParameters();

  /**
   * Gets the {@link IResolvedTypeParameter} at the given index.
   *
   * @param index
   *          The index (>=0) of the {@link ITypeParameter} that should be returned.
   * @return The {@link IResolvedTypeParameter} of the this type at the given position or <code>null</code> if there
   *         is no type parameter at the given position.
   */
  IResolvedTypeParameter getTypeParameter(int index);

  /**
   * Gets the {@link IResolvedTypeParameter} with the given name.
   *
   * @param name
   *          The name of the {@link IResolvedTypeParameter} on the this type (e.g. "T").
   * @return The {@link IResolvedTypeParameter} that belongs to the given name or <code>null</code> if no type parameter
   *         with the given name exists.
   */
  IResolvedTypeParameter getTypeParameter(String name);

  /**
   * Gets the bounds signatures of the type parameter at the given position.<br>
   * <br>
   * Type parameter bounds are the restrictions for a type parameter:<br>
   * <code>&lt;T extends {@link Comparable} & {@link Cloneable}&gt;</code><br>
   * In that case the bounds contains the signatures of {@link Comparable} and {@link Cloneable}.
   *
   * @param index
   *          The index (>=0) of the bounds that should be returned.
   * @return A {@link Set} containing the bounds of the type parameter at the given index. The resulting
   *         {@link Set#iterator()} returns the bounds in the order as they are defined in the source. If there is no
   *         type parameter at the given index, <code>null</code> is returned.
   */
  Set<String> getTypeParameterBounds(int index);

  /**
   * Gets the bounds signatures of the type parameter with the given name.<br>
   * <br>
   * Type parameter bounds are the restrictions for a type parameter:<br>
   * <code>&lt;T extends {@link Comparable} & {@link Cloneable}&gt;</code><br>
   * In that case the bounds contains the signatures of {@link Comparable} and {@link Cloneable}.
   *
   * @param name
   *          The name of the type parameter on the this type (e.g. "T").
   * @return A {@link Set} containing the bounds of the type parameter with the given name. The resulting
   *         {@link Set#iterator()} returns the bounds in the order as they are defined in the source. If there is no
   *         type parameter with given name, <code>null</code> is returned.
   */
  Set<String> getTypeParameterBounds(String name);

  /**
   * Gets the number of type parameters on this type.
   *
   * @return The number of type parameters on this type
   */
  int getParameterCount();

  /**
   * Gets the {@link IType} this {@link ITypeParameterMapping} has been calculated for.
   *
   * @return The {@link IType} of this mapping if it has been created based on an {@link IType}. <code>null</code> if
   *         this {@link ITypeParameterMapping} was created based on a signature.
   */
  IType getType();

  /**
   * Gets the fully qualified name of the type this {@link ITypeParameterMapping} was calculated for.
   *
   * @return The fully qualified name of the type this {@link ITypeParameterMapping} was calculated for. Never returns
   *         <code>null</code>.
   */
  String getFullyQualifiedName();

  /**
   * Gets the direct super {@link ITypeParameterMapping}s that exist for this type. This includes the mappings for the
   * super class and the direct super interfaces.
   *
   * @return A {@link Map} containing the fully qualified name of the super type as key and the corresponding
   *         {@link ITypeParameterMapping} as value.
   */
  Map<String, ITypeParameterMapping> getSuperMappings();

  /**
   * Gets the {@link ITypeParameterMapping} of the given direct super type.
   *
   * @param name
   *          The fully qualified name of the direct super mapping to return. This may be the super class or a direct
   *          super interface.
   * @return The super {@link ITypeParameterMapping} for the given type name or <code>null</code> if no such super
   *         mapping exists.
   */
  ITypeParameterMapping getSuperMapping(String fullyQualifiedName);

  /**
   * Gets the {@link ITypeParameterMapping} below the current mapping. Because {@link ITypeParameterMapping}s are always
   * created bottom-up this can only be one mapping.
   * 
   * @return The sub {@link ITypeParameterMapping} or <code>null</code> if no mapping exists below this (this means this
   *         is the bottom of the mapping tree).
   */
  ITypeParameterMapping getSubMapping();

}
