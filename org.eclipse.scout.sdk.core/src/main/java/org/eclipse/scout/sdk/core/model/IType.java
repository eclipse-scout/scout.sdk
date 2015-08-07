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
package org.eclipse.scout.sdk.core.model;

import java.util.List;

import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;

/**
 * <h3>{@link IType}</h3> Represents a java data type. This includes classes, interfaces, enums, primitives, the
 * void-type ({@link #VOID}) & the wildcard-type ("?").
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IType extends IMember {

  /**
   * The shared instance representing the "void" data type.
   */
  IType VOID = VoidType.INSTANCE;

  /**
   * Gets the {@link IPackage} of this {@link IType}.<br>
   * For primitives, the void-type and the wildcard-type this method returns the {@link IPackage#DEFAULT_PACKAGE}.
   *
   * @return The {@link IPackage} of this {@link IType} or {@link IPackage#DEFAULT_PACKAGE} for the default package.
   *         Never returns <code>null</code>.
   */
  IPackage getPackage();

  /**
   * Gets the {@link ICompilationUnit} of this {@link IType}. For binary {@link IType}s, primitives, the void-type and
   * wildcard-types this method returns <code>null</code>.
   *
   * @return The {@link ICompilationUnit} that belongs to this {@link IType} or <code>null</code>.
   */
  ICompilationUnit getCompilationUnit();

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
  @Override
  String getName();

  /**
   * Gets all {@link ITypeParameter}s defined by this {@link IType} in the order as they appear in the source or class
   * file.<br>
   * <br>
   * Type parameters are declarations as defined by the hosting {@link IType}. They may have minimal bounds defined.<br>
   * The difference to {@link #getTypeArguments()} is that {@link #getTypeParameters()} returns the parameter as they
   * are declared by the class file while {@link #getTypeArguments()} holds the currently bound real {@link IType}s.<br>
   * <br>
   * <b>Example: </b><br>
   * <code>public class NumberList&lt;T extends java.lang.Number&gt; {}</code><br>
   * <code>public static NumberList&lt;java.lang.Double&gt; getDoubleValues() {}</code><br>
   * <br>
   * The return {@link IType} of the {@link IMethod} "getDoubleValues" would return the following values:<br>
   * <code>getDoubleValues.getReturnType().getTypeParameters().getBounds() = java.lang.Number</code><br>
   * <code>getDoubleValues.getReturnType().getTypeArguments() = java.lang.Double</code>
   *
   * @return
   */
  List<ITypeParameter> getTypeParameters();

  /**
   * Specifies if this {@link IType} has {@link ITypeParameter}s.
   *
   * @return <code>true</code> if this is a parameterized {@link IType} (using generics), <code>false</code> otherwise.
   */
  boolean hasTypeParameters();

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
   * Gets all direct member {@link IType}s of this {@link IType} in the order as they appear in the source or class
   * file.
   *
   * @return A {@link List} holding all member {@link IType}s.
   */
  List<IType> getTypes();

  /**
   * Gets all direct member {@link IMethod}s of this {@link IType} in the order as they appear in the source or class
   * file.
   *
   * @return A {@link List} holding all member {@link IMethod}s.
   */
  List<IMethod> getMethods();

  /**
   * Gets all arguments passed to the type parameters of this {@link IType}.<br>
   * See {@link #getTypeParameters()} for more details.
   *
   * @return A {@link List} holding all {@link IType}s arguments.
   * @see #getTypeParameters()
   */
  List<IType> getTypeArguments();

  /**
   * Gets if this {@link IType} represents a primitive type.
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
   * Gets the {@link IField}s of this {@link IType} in the order as they are defined in the source or class file.
   *
   * @return A {@link List} holding all {@link IField}s of this {@link IType}.
   */
  List<IField> getFields();

  /**
   * Gets if this {@link IType} represents a wildcard type ("?").
   *
   * @return <code>true</code> if this {@link IType} represents a wildcard type, <code>false</code> otherwise.
   */
  boolean isWildcardType();

  /**
   * Gets the {@link ILookupEnvironment} (classpath) this {@link IType} belongs to.
   *
   * @return The {@link ILookupEnvironment} this {@link IType} belongs to.
   */
  ILookupEnvironment getLookupEnvironment();
}
