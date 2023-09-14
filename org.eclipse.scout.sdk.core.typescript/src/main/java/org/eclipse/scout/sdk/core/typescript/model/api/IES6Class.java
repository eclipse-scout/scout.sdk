/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.query.FieldQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.query.FunctionQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.query.SupersQuery;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;

/**
 * Represents a JavaScript or TypeScript class, a TypeScript interface, enum or type alias.
 */
public interface IES6Class extends IDataType {
  @Override
  ES6ClassSpi spi();

  /**
   * Gets a {@link FieldQuery} to retrieve {@link IField}s of this {@link IES6Class}.<br>
   * By default, the query returns all fields directly declared in this {@link IES6Class}.
   *
   * @return A new {@link FieldQuery} for {@link IField}s of this {@link IES6Class}.
   */
  FieldQuery fields();

  /**
   * Gets the first {@link IField} having the given name.
   *
   * @param name
   *          The name of the field to search. If {@code null}, the first field (regardless of its name) is returned (if
   *          existing).
   * @return The first {@link IField} having the given name or an empty {@link Optional} if no such field exists.
   */
  Optional<IField> field(String name);

  /**
   * Gets a {@link FunctionQuery} to retrieve {@link IFunction}s of this {@link IES6Class}.<br>
   * By default, the query returns all functions directly declared in this {@link IES6Class}.
   *
   * @return A new {@link FunctionQuery} for {@link IFunction}s of this {@link IES6Class}.
   */
  FunctionQuery functions();

  /**
   * Gets the first {@link IFunction} having the given name (regardless of the argument list in case there are
   * overloads).
   *
   * @param name
   *          The name of the function to search. If {@code null}, the first function (regardless of its name) is
   *          returned (if existing).
   * @return The first {@link IFunction} having the given name or an empty {@link Optional} if no such function exists.
   */
  Optional<IFunction> function(String name);

  /**
   * @return {@code true} if this class is a TypeScript enum.
   */
  boolean isEnum();

  /**
   * @return {@code true} if this {@link IES6Class} represents a TypeScript type alias.
   * @see #aliasedDataType()
   */
  boolean isTypeAlias();

  /**
   * @return {@code true} if this {@link IES6Class} represents a TypeScript interface.
   */
  boolean isInterface();

  /**
   * @return Gets the optional super class. If this class is an interface (see {@link #isInterface()}), the
   *         {@link Optional} is always empty.
   * @see #superInterfaces()
   * @see #supers()
   */
  Optional<IES6Class> superClass();

  /**
   * Gets a {@link SupersQuery} to retrieve {@link IES6Class super classes or interfaces} of this {@link IES6Class}.<br>
   * By default, this query returns all super classes and super interfaces recursively without this start
   * {@link IES6Class} itself.<br>
   * If the super hierarchy contains interfaces multiple times, every interface is only returned once.
   *
   * @return A new {@link SupersQuery} for this {@link IES6Class}.
   */
  SupersQuery supers();

  /**
   * @return All super interfaces of this class or interface.
   */
  Stream<IES6Class> superInterfaces();

  /**
   * @return The right-hand-side of the assignment if this {@link IES6Class} is a TypeScript type alias (see
   *         {@link #isTypeAlias()}). An empty {@link Optional} otherwise.
   */
  Optional<IDataType> aliasedDataType();

  /**
   * Checks if this {@link IES6Class} has the given modifier.
   * 
   * @param modifier
   *          The {@link Modifier} to check. May be {@code null} (then always {@code false} is returned).
   * @return {@code true} if the given modifier is present.
   */
  boolean hasModifier(Modifier modifier);

  /**
   * @return The raw type of this {@link IES6Class}. If this {@link IES6Class} has no type arguments, this instance is
   *         returned.
   */
  IES6Class withoutTypeArguments();

  /**
   * Checks if this {@link IES6Class} is instanceof the given class.<br>
   * In other words: checks if the given class is this instance or one of its supers.
   * 
   * @param es6Class
   *          The class to check. May be {@code null}.
   * @return {@code true} if this instance or one of its supers is the given class (ignoring type arguments).
   */
  boolean isInstanceOf(IES6Class es6Class);

  /**
   * @return The type parameters declared by this {@link IES6Class}.
   */
  Stream<ITypeParameter> typeParameters();
}
