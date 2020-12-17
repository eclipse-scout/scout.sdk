/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.query.FieldQuery;
import org.eclipse.scout.sdk.core.model.api.query.HierarchyInnerTypeQuery;
import org.eclipse.scout.sdk.core.model.api.query.MethodQuery;
import org.eclipse.scout.sdk.core.model.api.query.SuperTypeQuery;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;

/**
 * <h3>{@link IType}</h3>
 * <p>
 * Represents a java data type. This includes classes, interfaces, enums, primitives, array types, the void-type & the
 * wildcard-type ("?").
 *
 * @since 5.1.0
 */
public interface IType extends IMember {

  /**
   * Gets the {@link IPackage} of this {@link IType}.<br>
   * For primitives, the void-type and the wildcard-type this method returns the default package
   * ({@link IPackage#elementName()} is {@code null}).
   *
   * @return The {@link IPackage} of this {@link IType}.
   */
  IPackage containingPackage();

  /**
   * Gets the fully qualified name of this {@link IType}.<br>
   * Inner types are separated by '$'.<br>
   * <br>
   * <b>Example: </b>{@code org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass}.<br>
   *
   * @return The fully qualified name of this {@link IType}.
   */
  String name();

  /**
   * Gets the simple name of this {@link IType}.
   *
   * @return The simple name of this {@link IType}.
   */
  @Override
  String elementName();

  /**
   * Gets all arguments passed to the type parameters of this {@link IType}.<br>
   * See {@link #typeParameters()} for more details.
   *
   * @return A {@link Stream} holding all {@link IType}s arguments.
   * @see #typeParameters()
   */
  Stream<IType> typeArguments();

  /**
   * Specifies if this is a parameter type.<br>
   * A parameter type is an {@link IType} that represents a type parameter placeholder (e.g. "T").
   *
   * @return {@code true} if it is a parameter type, {@code false} otherwise.
   */
  boolean isParameterType();

  /**
   * Gets the super {@link IType} of this {@link IType} or an empty {@link Optional} if this {@link IType} is
   * {@link Object}.<br>
   * The super class of an interface is {@link Object}.
   *
   * @return The super {@link IType}.
   * @see #requireSuperClass()
   */
  Optional<IType> superClass();

  /**
   * Same as {@link #superClass()} but throws an {@link IllegalArgumentException} if this type is {@link Object}.
   *
   * @return The super class of this type.
   * @throws IllegalArgumentException
   *           if this type is {@link Object}.
   * @see #superClass()
   */
  IType requireSuperClass();

  /**
   * Gets the direct super interfaces of this {@link IType} in the order as they appear in the source or class file.
   *
   * @return A {@link Stream} containing the direct super interfaces of this {@link IType}.
   */
  Stream<IType> superInterfaces();

  /**
   * @return the source of the static initializer with the {@code static} keyword and the leading and trailing brackets.
   */
  Optional<ISourceRange> sourceOfStaticInitializer();

  /**
   * Gets if this {@link IType} represents a primitive type.
   *
   * @return {@code true} if this {@link IType} represents a primitive type, {@code false} otherwise.
   */
  boolean isPrimitive();

  /**
   * Gets if this {@link IType} represents an array type.<br>
   * If the result is {@code true} this means the array dimension is &gt; 0 (see {@link #arrayDimension()}).
   *
   * @return {@code true} if this {@link IType} represents an array type, {@code false} otherwise.
   */
  boolean isArray();

  /**
   * Gets the number of array dimensions this {@link IType} represents.<br>
   * An array dimension of zero means no array.<br>
   * <br>
   * <b>Example: </b><br>
   * {@code Object[][]: getArrayDimension() = 2}
   *
   * @return The array dimension of this {@link IType}.
   */
  int arrayDimension();

  /**
   * Gets the base {@link IType} of an array type. Only valid on arrays (see {@link #isArray()}).
   * <p>
   * The leaf component {@link IType} is the type of a single element in the array. This means e.g. the leaf component
   * type of {@code Long[][]} is {@code Long}.
   *
   * @return the leaf component {@link IType} of this array type. If this {@link IType} is no array type, this method
   *         returns an empty {@link Optional}.
   * @see #isArray()
   */
  Optional<IType> leafComponentType();

  /**
   * Gets if this {@link IType} represents a wildcard type ("?").
   *
   * @return {@code true} if this {@link IType} represents a wildcard type, {@code false} otherwise.
   */
  boolean isWildcardType();

  /**
   * Gets the {@link ICompilationUnit} of this {@link IType}.
   * <p>
   * For primitive-types, array-types, wildcard-types and the void-type this method returns an empty {@link Optional}.
   * <p>
   * Binary types return a compilation unit with {@link ICompilationUnit#isSynthetic()} = {@code true}
   *
   * @return The {@link ICompilationUnit} that belongs to this {@link IType}.
   * @see #requireCompilationUnit()
   */
  Optional<ICompilationUnit> compilationUnit();

  /**
   * Same as {@link #compilationUnit()} but throws an {@link IllegalArgumentException} if this {@link IType} is a
   * primitive-, array- or wildcard type as these types do not have a compilation unit.
   *
   * @return The {@link ICompilationUnit} of this {@link IType} (may be a synthetic compilation unit for binary types).
   * @throws IllegalArgumentException
   *           if this type is a primitive-, array- or wildcard-type.
   * @see #compilationUnit()
   */
  ICompilationUnit requireCompilationUnit();

  /**
   * @return {@code true} if this type is the (primitive) void type, {@code false} otherwise.
   */
  boolean isVoid();

  /**
   * @return {@code true} if this {@link IType} is an interface, {@code false} otherwise.
   */
  boolean isInterface();

  /**
   * @return The full reference of this {@link IType} including all type arguments.
   */
  String reference();

  /**
   * @param erasureOnly
   *          If {@code true}, no type arguments are included. If {@code false}, all type arguments are part of the
   *          reference.
   * @return The reference of this {@link IType}. Optionally including all type arguments.
   */
  String reference(boolean erasureOnly);

  /**
   * Gets a {@link SuperTypeQuery} to retrieve super {@link IType}s of this {@link IType}.<br>
   * By default this query returns all super {@link IType}s of this {@link IType} including this start {@link IType}
   * itself. If the super hierarchy contains interfaces multiple times, every interface is only returned once.
   *
   * @return A new {@link SuperTypeQuery} for this {@link IType}.
   */
  SuperTypeQuery superTypes();

  /**
   * Gets a {@link HierarchyInnerTypeQuery} to retrieve inner types of this {@link IType}.<br>
   * By default this query returns all direct inner {@link IType}s of this {@link IType}.
   *
   * @return A new {@link HierarchyInnerTypeQuery} for inner {@link IType}s of this {@link IType}.
   */
  HierarchyInnerTypeQuery innerTypes();

  /**
   * Gets a {@link MethodQuery} to retrieve {@link IMethod}s of this {@link IType}.<br>
   * By default this query returns all {@link IMethod}s directly declared in this {@link IType}.
   *
   * @return A new {@link MethodQuery} for {@link IMethod}s in this {@link IType}.
   */
  MethodQuery methods();

  /**
   * Gets a {@link FieldQuery} to retrieve {@link IField}s of this {@link IType}.<br>
   * By default this query returns all fields directly declared in this {@link IType}.
   *
   * @return A new {@link FieldQuery} for {@link IField}s of this {@link IType}.
   */
  FieldQuery fields();

  /**
   * Checks if the receiver has the given queryType in its super hierarchy.
   *
   * @param queryType
   *          The fully qualified name of the super type to check.
   * @return {@code true} if the given fully qualified name exists in the super hierarchy of this {@link IType}.
   *         {@code false} otherwise.
   */
  boolean isInstanceOf(String queryType);

  /**
   * Checks if the receiver has the given {@link IClassNameSupplier#fqn()} in its super hierarchy.
   * 
   * @param typeName
   *          The {@link IClassNameSupplier} to check. Must not be {@code null}.
   * @return {@code true} if the given fully qualified name exists in the super hierarchy of this {@link IType}.
   *         {@code false} otherwise.
   */
  boolean isInstanceOf(IClassNameSupplier typeName);

  /**
   * Checks if the given {@link IType} has the receiver (this) in its super hierarchy. see
   * {@link Class#isAssignableFrom(Class)}
   *
   * @return {@code true} if the declaration {@code BaseClass a = (SpecificClass)s;} is valid, where this is the base
   *         class
   */
  boolean isAssignableFrom(IType specificClass);

  /**
   * @return If this is a primitive type then its boxed type is returned. Otherwise {@code this} type itself is
   *         returned.
   */
  IType boxPrimitiveType();

  /**
   * @return If this is a boxed type then its primitive type is returned. Otherwise the type itself is returned.
   */
  IType unboxToPrimitive();

  /**
   * Gets the primary {@link IType} of this given {@link IType}. If the receiver is already the primary type,
   * {@code this} is returned.
   *
   * @return The primary {@link IType}.
   */
  IType primary();

  /**
   * Gets the bounds (according to the receiver context {@link IType}) of the type argument with the given index.<br>
   * If there is no type argument with given index (e.g. because it was not specified), an empty {@link Optional} is
   * returned.
   *
   * @param typeParamIndex
   *          The index of the type argument.
   * @return A {@link Stream} with all bounds of the type argument at the given index.
   */
  Optional<Stream<IType>> resolveTypeParamValue(int typeParamIndex);

  /**
   * Gets the bounds of the type argument of the given super type name at the given index in the context of this
   * {@link IType}.<br>
   * If there is no type argument with given index (e.g. because it was not specified), an empty {@link Optional} is
   * returned.
   *
   * @param typeParamIndex
   *          The index of the type argument as defined on the level of the given super type name.
   * @param levelFqn
   *          The fully qualified name of the super type that defines the type argument with the given index.
   * @return A {@link Stream} with all bounds of the type argument at the given index.
   */
  Optional<Stream<IType>> resolveTypeParamValue(int typeParamIndex, String levelFqn);

  /**
   * Tries to resolve the simple specified from the view of this {@link IType}.
   * <p>
   * The implementation tries to find the given simple name by looking at the following places:
   * <ol>
   * <li>The imports (explicit or wildcard) of the {@link ICompilationUnit} of this {@link IType}. Please note that this
   * only works if imports are available in the {@link ICompilationUnit} which is only the case if it is based on source
   * code not on a binary class.</li>
   * <li>The package of this {@link IType}.</li>
   * <li>The {@code java.lang} package</li>
   * <li>Resolving the given name directly assuming it is already fully qualified</li>
   * </ol>
   * Therefore the resulting {@link Optional} only contains a result if this {@link IType} actually contains a reference
   * to the simple name or can be found in the places as explained above. An empty {@link Optional} does not necessarily
   * mean that there is no {@link IType} with that simple name.
   * 
   * @param simpleName
   *          The simple name to resolve. May be {@code null}, empty {@link String}, a simple name or a fully qualified
   *          name.
   * @return An {@link Optional} holding the {@link IType} the given simple name points to as seen from this
   *         {@link IType}.
   */
  Optional<IType> resolveSimpleName(String simpleName);

  /**
   * @return The qualifier of this {@link IType}.<br>
   *         If this is a nested type, the fully qualified name of the declaring {@link IType} is returned. In this case
   *         inner classes are separated by {@code $}.<br>
   *         If this is a primary type the containing package or an empty {@link String} for the default package is
   *         returned.
   */
  String qualifier();

  @Override
  TypeSpi unwrap();

  @Override
  ITypeGenerator<?> toWorkingCopy();

  @Override
  ITypeGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer);
}
