/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.spi;

import java.util.List;

import org.eclipse.scout.sdk.core.java.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link TypeSpi}</h3> Represents a java data type. This includes classes, interfaces, enums, primitives, the
 * void-type & the wildcard-type ("?").
 *
 * @since 5.1.0
 */
public interface TypeSpi extends MemberSpi {

  /**
   * Gets the {@link PackageSpi} of this {@link TypeSpi}.<br>
   * For primitives, the void-type and the wildcard-type this method returns the default package
   * ({@link PackageSpi#getElementName()} is {@code null}).
   *
   * @return The {@link PackageSpi} of this {@link TypeSpi}.
   */
  PackageSpi getPackage();

  /**
   * Gets the fully qualified name of this {@link TypeSpi}.<br>
   * Inner types are separated by '$'.<br>
   * <br>
   * <b>Example: </b>{@code org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass}.<br>
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
   * Specifies if this is an anonymous class. If {@code true} the {@link #getElementName()} will return an empty
   * {@link String} and {@link #getName()} will have no last segment.
   *
   * @return {@code true} if it is an anonymous class, {@code false} otherwise.
   */
  boolean isAnonymous();

  /**
   * Gets the super {@link TypeSpi} of this {@link TypeSpi} or {@code null} if this {@link TypeSpi} is {@link Object}.
   *
   * @return The super {@link TypeSpi} or {@code null}.
   */
  TypeSpi getSuperClass();

  /**
   * Gets all direct super interfaces of this {@link TypeSpi} in the order as they appear in the source or class file.
   *
   * @return A {@link List} containing all direct super interfaces of this {@link TypeSpi}.
   */
  List<TypeSpi> getSuperInterfaces();

  /**
   * @return the source of the static initializer.
   */
  SourceRange getSourceOfStaticInitializer();

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
   * @return {@code true} if this {@link TypeSpi} represents a primitive type, {@code false} otherwise.
   */
  boolean isPrimitive();

  /**
   * Gets the number of array dimensions this {@link TypeSpi} represents.<br>
   * An array dimension of zero means no array.<br>
   * <br>
   * <b>Example: </b><br>
   * {@code Object[][]: getArrayDimension() = 2}
   *
   * @return The array dimension of this {@link TypeSpi}.
   */
  int getArrayDimension();

  /**
   * Only valid on arrays ({@link #getArrayDimension()} &gt; 0).
   *
   * @return the leaf component type of the array that is the type without []
   */
  TypeSpi getLeafComponentType();

  /**
   * Gets if this {@link TypeSpi} represents a wildcard type ("?").
   *
   * @return {@code true} if this {@link TypeSpi} represents a wildcard type, {@code false} otherwise.
   */
  boolean isWildcardType();

  /**
   * Gets the {@link CompilationUnitSpi} of this {@link TypeSpi}.
   * <p>
   * For primitives, the void-type and wildcard-types this method returns {@code null}.
   * <p>
   * Binary types return a compilation unit with {@link CompilationUnitSpi#isSynthetic()} = true
   *
   * @return The {@link CompilationUnitSpi} that belongs to this {@link TypeSpi} or {@code null}.
   */
  CompilationUnitSpi getCompilationUnit();

  @Override
  default TreeVisitResult acceptPreOrder(IDepthFirstJavaElementVisitor visitor, int level, int index) {
    return visitor.preVisit(wrap(), level, index);
  }

  @Override
  default boolean acceptPostOrder(IDepthFirstJavaElementVisitor visitor, int level, int index) {
    return visitor.postVisit(wrap(), level, index);
  }

  @Override
  default TreeVisitResult acceptLevelOrder(IBreadthFirstJavaElementVisitor visitor, int level, int index) {
    return visitor.visit(wrap(), level, index);
  }

  @Override
  IType wrap();
}
