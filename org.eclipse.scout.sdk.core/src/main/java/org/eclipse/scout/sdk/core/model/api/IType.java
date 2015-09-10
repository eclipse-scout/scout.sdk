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
 * void-type ({@link #VOID}) & the wildcard-type ("?").
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IType extends IMember {

  /**
   * Gets the {@link IPackage} of this {@link IType}.<br>
   * For primitives, the void-type and the wildcard-type this method returns the {@link IPackage#DEFAULT_PACKAGE}.
   *
   * @return The {@link IPackage} of this {@link IType} or {@link IPackage#DEFAULT_PACKAGE} for the default package.
   *         Never returns <code>null</code>.
   */
  IPackage getPackage();

  /**
   * Gets the simple name of this {@link IType}.
   *
   * @return The simple name of this {@link IType}.
   */
  String getSimpleName();

  /**
   * Gets the fully qualified name of this {@link IType}.<br>
   * Inner types are separated by '$'.<br>
   * <br>
   * <b>Example: </b><code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass</code>.<br>
   *
   * @return The fully qualified name of this {@link IType}.
   */
  String getName();

  /**
   * Gets all arguments passed to the type parameters of this {@link IType}.<br>
   * See {@link #getTypeParameters()} for more details.
   *
   * @return A {@link List} holding all {@link IType}s arguments.
   * @see #getTypeParameters()
   */
  List<IType> getTypeArguments();

  /**
   * If this {@link IType} is a synthetic parameterized type (for example the super class of a parameterized type with
   * applied type arguments) then this method returns the original type without the type arguments applied.
   * <p>
   * Otherwise this is returned
   */
  IType getOriginalType();

  /**
   * Specifies if this is an anonymous class. If <code>true</code> the {@link #getSimpleName()} will return an empty
   * {@link String} and {@link #getName()} will have no last segment.
   *
   * @return <code>true</code> if it is an anonymous class, <code>false</code> otherwise.
   */
  boolean isAnonymous();

  /**
   * Gets the super {@link IType} of this {@link IType} or <code>null</code> if this {@link IType} is {@link Object}.
   *
   * @return The super {@link IType} or <code>null</code>.
   */
  IType getSuperClass();

  /**
   * Gets all direct super interfaces of this {@link IType} in the order as they appear in the source or class file.
   *
   * @return A {@link List} containing all direct super interfaces of this {@link IType}.
   */
  List<IType> getSuperInterfaces();

  /**
   * @return the source of the static initializer without the { and } brackets
   */
  ISourceRange getSourceOfStaticInitializer();

  /**
   * Gets all direct member {@link IType}s of this {@link IType} in the order as they appear in the source or class
   * file.
   *
   * @return A {@link List} holding all member {@link IType}s.
   */
  List<IType> getTypes();

  /**
   * Gets the {@link IField}s of this {@link IType} in the order as they are defined in the source or class file.
   *
   * @return A {@link List} holding all {@link IField}s of this {@link IType}.
   */
  List<IField> getFields();

  /**
   * Gets all direct member {@link IMethod}s of this {@link IType} in the order as they appear in the source or class
   * file.
   *
   * @return A {@link List} holding all member {@link IMethod}s.
   */
  List<IMethod> getMethods();

  /**
   * Gets if this {@link IType} represents a primitive type. See also {@link #primitiveType()}
   *
   * @return <code>true</code> if this {@link IType} represents a primitive type, <code>false</code> otherwise.
   */
  boolean isPrimitive();

  /**
   * Gets if this {@link IType} represents an array type.<br>
   * If the result is <code>true</code> this means the array dimension is &gt; 0 (see {@link #getArrayDimension()}).
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
  int getArrayDimension();

  /**
   * Only valid on arrays {@link #isArray()}
   *
   * @return the leaf component type of the array that is the type without []
   */
  IType getLeafComponentType();

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
   * Binary types return a compilation unit with {@link ICompilationUnit#isSynthetic()} = true
   *
   * @return The {@link ICompilationUnit} that belongs to this {@link IType} <code>null</code>.
   */
  ICompilationUnit getCompilationUnit();

  @Override
  TypeSpi unwrap();

  //additional convenience methods
  /**
   * @return true if this type is the void type
   */
  boolean isVoid();

  boolean isInterface();

  String getSignature();

  SuperTypeQuery superTypes();

  TypeQuery innerTypes();

  MethodQuery methods();

  FieldQuery fields();

  /**
   * Checks if the given {@link IType} has the given queryType in its super hierarchy.
   *
   * @param typeToCheck
   *          The {@link IType} to check.
   * @param queryType
   *          The fully qualified name of the super type to check.
   */
  boolean isInstanceOf(String queryType);

  /**
   * see {@link Class#isAssignableFrom(Class)}
   *
   * @return true if the declaration <code>BaseClass a = (SpecificClass)s;</code> is valid, where this is the base class
   */
  boolean isAssignableFrom(IType specificClass);

  /**
   * If type is a primitive type then its boxed type is returned. Otherwise type itself is returned.
   */
  IType boxPrimitiveType();

  /**
   * If type is a boxed type then its primitive type is returned. Otherwise the type itself is returned.
   */
  IType unboxPrimitiveType();

}
