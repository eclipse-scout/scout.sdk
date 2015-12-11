/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.api;

import java.util.List;

import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.model.sugar.FieldQuery;
import org.eclipse.scout.sdk.core.model.sugar.MethodQuery;
import org.eclipse.scout.sdk.core.model.sugar.SuperTypeQuery;
import org.eclipse.scout.sdk.core.model.sugar.TypeQuery;

/**
 * <h3>{@link IType}</h3> Represents a java data type. This includes classes, interfaces, enums, primitives, the
 * void-type & the wildcard-type ("?").
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IType extends IMember {

  /**
   * Gets the {@link IPackage} of this {@link IType}.<br>
   * For primitives, the void-type and the wildcard-type this method returns the default package.
   *
   * @return The {@link IPackage} of this {@link IType}. Never returns <code>null</code>.
   */
  IPackage containingPackage();

  /**
   * Gets the fully qualified name of this {@link IType}.<br>
   * Inner types are separated by '$'.<br>
   * <br>
   * <b>Example: </b><code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass</code>.<br>
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
   * @return A {@link List} holding all {@link IType}s arguments.
   * @see #typeParameters()
   */
  List<IType> typeArguments();

  /**
   * If this {@link IType} is a synthetic parameterized type (for example the super class of a parameterized type with
   * applied type arguments) then this method returns the original type without the type arguments applied.
   * <p>
   * Otherwise the receiver is returned.
   */
  IType originalType();

  /**
   * Specifies if this is a parameter type.<br>
   * A parameter type is an {@link IType} that represents a type parameter placeholder (e.g. "T").
   *
   * @return <code>true</code> if it is a parameter type, <code>false</code> otherwise.
   */
  boolean isParameterType();

  /**
   * Gets the super {@link IType} of this {@link IType} or <code>null</code> if this {@link IType} is {@link Object}.
   *
   * @return The super {@link IType} or <code>null</code>.
   */
  IType superClass();

  /**
   * Gets all direct super interfaces of this {@link IType} in the order as they appear in the source or class file.
   *
   * @return A {@link List} containing all direct super interfaces of this {@link IType}.
   */
  List<IType> superInterfaces();

  /**
   * @return the source of the static initializer without the { and } brackets. Never returns <code>null</code>. Use
   *         {@link ISourceRange#isAvailable()} to check if source is actually available for this element.
   */
  ISourceRange sourceOfStaticInitializer();

  /**
   * Gets if this {@link IType} represents a primitive type. See also {@link #primitiveType()}
   *
   * @return <code>true</code> if this {@link IType} represents a primitive type, <code>false</code> otherwise.
   */
  boolean isPrimitive();

  /**
   * Gets if this {@link IType} represents an array type.<br>
   * If the result is <code>true</code> this means the array dimension is &gt; 0 (see {@link #arrayDimension()}).
   *
   * @return <code>true</code> if this {@link IType} represents an array type, <code>false</code> otherwise.
   */
  boolean isArray();

  /**
   * Gets the number of array dimensions this {@link IType} represents.<br>
   * An array dimension of zero means no array.<br>
   * <br>
   * <b>Example: </b><br>
   * <code>Object[][]: getArrayDimension() = 2</code>
   *
   * @return The array dimension of this {@link IType}.
   */
  int arrayDimension();

  /**
   * Only valid on arrays (see {@link #isArray()}).
   *
   * @return the leaf component {@link IType} of the array that is the type without [].
   */
  IType leafComponentType();

  /**
   * Gets if this {@link IType} represents a wildcard type ("?").
   *
   * @return <code>true</code> if this {@link IType} represents a wildcard type, <code>false</code> otherwise.
   */
  boolean isWildcardType();

  /**
   * Gets the {@link ICompilationUnit} of this {@link IType}.
   * <p>
   * For primitives, the void-type and wildcard-types this method returns <code>null</code>.
   * <p>
   * Binary types return a compilation unit with {@link ICompilationUnit#isSynthetic()} = <code>true</code>
   *
   * @return The {@link ICompilationUnit} that belongs to this {@link IType} <code>null</code>.
   */
  ICompilationUnit compilationUnit();

  @Override
  TypeSpi unwrap();

  /**
   * @return <code>true</code> if this type is the void type, <code>false</code> otherwise.
   */
  boolean isVoid();

  /**
   * @return <code>true</code> if this {@link IType} is an interface, <code>false</code> otherwise.
   */
  boolean isInterface();

  /**
   * @return The full signature of this {@link IType} including all type arguments.
   */
  String signature();

  /**
   * Gets a {@link SuperTypeQuery} to retrieve super {@link IType}s of this {@link IType}.
   *
   * @return A new {@link SuperTypeQuery} for this {@link IType}.
   */
  SuperTypeQuery superTypes();

  /**
   * Gets a {@link TypeQuery} to retrieve inner types of this {@link IType}.
   *
   * @return A new {@link TypeQuery} for inner {@link IType}s of this {@link IType}.
   */
  TypeQuery innerTypes();

  /**
   * Gets a {@link MethodQuery} to retrieve {@link IMethod}s of this {@link IType}.
   *
   * @return A new {@link MethodQuery} for {@link IMethod}s in this {@link IType}.
   */
  MethodQuery methods();

  /**
   * Gets a {@link FieldQuery} to retrieve {@link IField}s of this {@link IType}.
   *
   * @return A new {@link FieldQuery} for {@link IField}s of this {@link IType}.
   */
  FieldQuery fields();

  /**
   * Checks if the receiver has the given queryType in its super hierarchy.
   *
   * @param queryType
   *          The fully qualified name of the super type to check.
   * @return <code>true</code> if the given fully qualified name exists in the super hierarchy of this {@link IType}.
   *         <code>false</code> otherwise.
   */
  boolean isInstanceOf(String queryType);

  /**
   * Checks if the given {@link IType} has the receiver (this) in its super hierarchy. see
   * {@link Class#isAssignableFrom(Class)}
   *
   * @return <code>true</code> if the declaration <code>BaseClass a = (SpecificClass)s;</code> is valid, where this is
   *         the base class
   */
  boolean isAssignableFrom(IType specificClass);

  /**
   * @return If this is a primitive type then its boxed type is returned. Otherwise type itself is returned.
   */
  IType boxPrimitiveType();

  /**
   * @return If this is a boxed type then its primitive type is returned. Otherwise the type itself is returned.
   */
  IType unboxPrimitiveType();

}
