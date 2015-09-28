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
package org.eclipse.scout.sdk.core.model.spi;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;

/**
 * <h3>{@link TypeSpi}</h3> Represents a java data type. This includes classes, interfaces, enums, primitives, the
 * void-type & the wildcard-type ("?").
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface TypeSpi extends MemberSpi {

  /**
   * Gets the {@link PackageSpi} of this {@link TypeSpi}.<br>
   * For primitives, the void-type and the wildcard-type this method returns the {@link PackageSpi#DEFAULT_PACKAGE}.
   *
   * @return The {@link PackageSpi} of this {@link TypeSpi} or {@link PackageSpi#DEFAULT_PACKAGE} for the default
   *         package. Never returns <code>null</code>.
   */
  PackageSpi getPackage();

  /**
   * Gets the fully qualified name of this {@link TypeSpi}.<br>
   * Inner types are separated by '$'.<br>
   * <br>
   * <b>Example: </b><code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass</code>.<br>
   *
   * @return The fully qualified name of this {@link TypeSpi}.
   */
  String getName();

  /**
   * Gets all arguments passed to the type parameters of this {@link TypeSpi}.<br>
   * See {@link #getTypeParameters()} for more details.
   *
   * @return A {@link List} holding all {@link TypeSpi}s arguments.
   * @see #getTypeParameters()
   */
  List<TypeSpi> getTypeArguments();

  /**
   * If this {@link TypeSpi} is a synthetic parameterized type (for example the super class of a parameterized type with
   * applied type arguments) then this method returns the original type without the type arguments applied.
   * <p>
   * Otherwise this is returned
   */
  TypeSpi getOriginalType();

  /**
   * Specifies if this is an anonymous class. If <code>true</code> the {@link #simpleName()} will return an empty
   * {@link String} and {@link #getName()} will have no last segment.
   *
   * @return <code>true</code> if it is an anonymous class, <code>false</code> otherwise.
   */
  boolean isAnonymous();

  /**
   * Gets the super {@link TypeSpi} of this {@link TypeSpi} or <code>null</code> if this {@link TypeSpi} is
   * {@link Object}.
   *
   * @return The super {@link TypeSpi} or <code>null</code>.
   */
  TypeSpi getSuperClass();

  /**
   * Gets all direct super interfaces of this {@link TypeSpi} in the order as they appear in the source or class file.
   *
   * @return A {@link List} containing all direct super interfaces of this {@link TypeSpi}.
   */
  List<TypeSpi> getSuperInterfaces();

  /**
   * @return the source of the static initializer without the { and } brackets
   */
  ISourceRange getSourceOfStaticInitializer();

  /**
   * Gets all direct member {@link TypeSpi}s of this {@link TypeSpi} in the order as they appear in the source or class
   * file.
   *
   * @return A {@link List} holding all member {@link TypeSpi}s.
   */
  List<TypeSpi> getTypes();

  /**
   * Gets the {@link FieldSpi}s of this {@link TypeSpi} in the order as they are defined in the source or class file.
   *
   * @return A {@link List} holding all {@link FieldSpi}s of this {@link TypeSpi}.
   */
  List<FieldSpi> getFields();

  /**
   * Gets all direct member {@link MethodSpi}s of this {@link TypeSpi} in the order as they appear in the source or
   * class file.
   *
   * @return A {@link List} holding all member {@link MethodSpi}s.
   */
  List<MethodSpi> getMethods();

  /**
   * Gets if this {@link TypeSpi} represents a primitive type.
   *
   * @return <code>true</code> if this {@link TypeSpi} represents a primitive type, <code>false</code> otherwise.
   */
  boolean isPrimitive();

  /**
   * Gets if this {@link TypeSpi} represents an array type.<br>
   * If the result is <code>true</code> this means the array dimension is &gt; 0 (see {@link #getArrayDimension()}).
   *
   * @return <code>true</code> if this {@link TypeSpi} represents an array type, <code>false</code> otherwise.
   */
  boolean isArray();

  /**
   * Gets the number of array dimensions this {@link TypeSpi} represents.<br>
   * An array dimension of zero means no array.<br>
   * <br>
   * <b>Example: </b><br>
   * <code>Object[][]: getArrayDimension() = 2</code>
   *
   * @return The array dimension of this {@link TypeSpi}.
   */
  int getArrayDimension();

  /**
   * Only valid on arrays {@link #isArray()}
   *
   * @return the leaf component type of the array that is the type without []
   */
  TypeSpi getLeafComponentType();

  /**
   * Gets if this {@link TypeSpi} represents a wildcard type ("?").
   *
   * @return <code>true</code> if this {@link TypeSpi} represents a wildcard type, <code>false</code> otherwise.
   */
  boolean isWildcardType();

  /**
   * Gets the {@link CompilationUnitSpi} of this {@link TypeSpi}.
   * <p>
   * For primitives, the void-type and wildcard-types this method returns <code>null</code>.
   * <p>
   * Binary types return a compilation unit with {@link CompilationUnitSpi#isSynthetic()} = true
   *
   * @return The {@link CompilationUnitSpi} that belongs to this {@link TypeSpi} <code>null</code>.
   */
  CompilationUnitSpi getCompilationUnit();

  @Override
  IType wrap();
}
