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

/**
 * <h3>{@link IResolvedTypeParameter}</h3><br>
 * Describes a resolved type parameter that is part of an {@link ITypeParameterMapping}.<br>
 * {@link ITypeParameterMapping}s can be obtained using {@link SignatureUtility#resolveTypeParameters(IType)} or one of
 * its overloads.
 *
 * @author Matthias Villiger
 * @since 4.2.0 10.12.2014
 * @see ITypeParameterMapping
 */
public interface IResolvedTypeParameter {

  /**
   * Gets the name of the type parameter (e.g. "T").
   *
   * @return The name of the type parameter.
   */
  String getTypeParameterName();

  /**
   * Gets the bounds signatures of this type parameter.<br>
   * <br>
   * Type parameter bounds are the restrictions for a type parameter:<br>
   * <code>&lt;T extends {@link Comparable} & {@link Cloneable}&gt;</code><br>
   * In that case the bounds contains the signatures of {@link Comparable} and {@link Cloneable}.<br>
   * <br>
   * <b>Note:</b><br>
   * The signature for a type parameter depends on which focus type the mappings have been created for (see
   * {@link SignatureUtility#resolveTypeParameters(IType)}) and are valid in the context of the focus type.
   *
   * @return A {@link Set} containing the bounds of this type parameter. The resulting {@link Set#iterator()} returns
   *         the bounds in the order as they are defined in the source. Never returns <code>null</code>
   */
  Set<String> getBoundsSignatures();

  /**
   * Gets the {@link ITypeParameterMapping} this parameter belongs to. This also defines to which type this parameter
   * belongs to.
   *
   * @return The {@link ITypeParameterMapping} that holds this {@link IResolvedTypeParameter}.
   */
  ITypeParameterMapping getOwnerMapping();

  /**
   * Gets the {@link IResolvedTypeParameter} on the given type that resolves into this type parameter when followed up
   * the hierarchy.<br>
   * In other words: the sub type parameter of this {@link IResolvedTypeParameter} on the given type according to the
   * calculated {@link ITypeParameterMapping} tree.
   *
   * @param level
   *          The {@link IType} that defines the level on which the corresponding {@link IResolvedTypeParameter} should
   *          be.
   * @return The {@link IResolvedTypeParameter} defined on the given type that resolves into this type parameter when
   *         followed up the hierarchy or <code>null</code> if no such type parameter can be found. This can happen if
   *         this type parameter does no longer exist on the given level (because it was finally defined in the super
   *         hierarchy of the given type), the given type does not exist or the given type is not part of the
   *         calculated {@link ITypeParameterMapping} hierarchy.
   */
  IResolvedTypeParameter getCorrespondingTypeParameterOnSubLevel(IType level);

  /**
   * Gets the {@link IResolvedTypeParameter} on the given type name that resolves into this type parameter when followed
   * up the hierarchy.<br>
   * In other words: the sub type parameter of this {@link IResolvedTypeParameter} on the given type name according to
   * the calculated {@link ITypeParameterMapping} tree.
   *
   * @param levelFullyQualifiedName
   *          The fully qualified type name that defines the level on which the corresponding
   *          {@link IResolvedTypeParameter} should be.
   * @return The {@link IResolvedTypeParameter} defined on the given type name that resolves into this type parameter
   *         when followed up the hierarchy or <code>null</code> if no such type parameter can be found. This can happen
   *         if this type parameter does no longer exist on the given level (because it was finally defined in the super
   *         hierarchy of the given type), the given type does not exist or the given type is not part of the
   *         calculated {@link ITypeParameterMapping} hierarchy.
   */
  IResolvedTypeParameter getCorrespondingTypeParameterOnSubLevel(String levelFullyQualifiedName);

  /**
   * Gets the zero based position (index) of this {@link IResolvedTypeParameter} on its owner.
   *
   * @return The index of this type parameter on its defining type.
   * @see #getOwnerMapping()
   */
  int getOrdinal();

  /**
   * Gets the direct super references to this {@link IResolvedTypeParameter} by owner type.
   *
   * @return A {@link Map} containing all references to this type parameter. The key of the map defines the fully
   *         qualified name of the super type whose parameters have references to this type parameter. The values
   *         contain all {@link IResolvedTypeParameter}s on the corresponding type that reference to this type
   *         parameter.
   */
  Map<String, Set<IResolvedTypeParameter>> getSuperReferences();

  /**
   * Gets all {@link IResolvedTypeParameter}s on the given type that directly reference to this type parameter (the
   * super parameters).
   *
   * @param ownerTypeFqn
   *          The fully qualified name of the super type for which the references should be returned.
   * @return A {@link Set} containing all {@link IResolvedTypeParameter}s on the given type that reference to this type
   *         parameter.
   */
  Set<IResolvedTypeParameter> getSuperReferences(String ownerTypeFqn);

  /**
   * Gets the direct child {@link IResolvedTypeParameter} on the given type that is referencing to this type parameter
   * (the sub
   * parameter).
   *
   * @param ownerTypeFqn
   *          The fully qualified name of the sub type for which the reference should be returned.
   * @return The child {@link IResolvedTypeParameter}s on the given type that reference to this type parameter.
   */
  IResolvedTypeParameter getSubReference(String ownerTypeFqn);

  /**
   * Gets the direct child {@link IResolvedTypeParameter}s that are referencing to this type parameter (the sub
   * parameters) by owner type.
   *
   * @return A {@link Map} containing all sub type parameters. The key of the map is the fully qualified name of the sub
   *         type. The values contain the {@link IResolvedTypeParameter} on the corresponding type that is referencing
   *         to this type parameter.
   */
  Map<String, IResolvedTypeParameter> getSubReferences();
}
