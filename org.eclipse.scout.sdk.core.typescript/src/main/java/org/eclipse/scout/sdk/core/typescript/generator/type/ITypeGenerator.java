/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator.type;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.generator.ITypeScriptElementGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.INodeElementGenerator;

/**
 * <h3>{@link ITypeGenerator}</h3>
 * <p>
 * An {@link ITypeScriptElementGenerator} that creates types, classes or interfaces.
 *
 * @since 13.0
 */
public interface ITypeGenerator<TYPE extends ITypeGenerator<TYPE>> extends INodeElementGenerator<TYPE> {

  /**
   * Marks this {@link ITypeGenerator} to be created as {@code class}.
   *
   * @return This generator.
   */
  TYPE asClass();

  /**
   * Marks this {@link ITypeGenerator} to be created as {@code interface}.
   *
   * @return This generator.
   */
  TYPE asInterface();

  /**
   * @return The super class name.
   */
  Optional<String> superClass();

  /**
   * Sets the super class reference of this {@link ITypeGenerator}.
   *
   * @param superClass
   *          The super class reference or {@code null} if this {@link ITypeGenerator} should not have a super class.
   * @return This generator.
   */
  TYPE withSuperClass(String superClass);

  /**
   * @return A {@link Stream} returning all {@link IFieldGenerator}s of this {@link ITypeGenerator}.
   */
  Stream<IFieldGenerator<?>> fields();

  /**
   * Adds the specified field to this {@link ITypeGenerator}.
   *
   * @param generator
   *          The {@link IFieldGenerator} to add. Must not be {@code null}.
   * @param sortObject
   *          Optional elements used to define the position of the {@link IFieldGenerator} within this
   *          {@link ITypeGenerator}. May be {@code null} or omitted (in that case a default position is calculated).
   *          The generators are sorted according to the natural order of the elements specified.
   * @return This generator.
   * @see FieldGenerator#create()
   */
  TYPE withField(IFieldGenerator<?> generator, Object... sortObject);

  /**
   * Removes all {@link IFieldGenerator IFieldGenerators} for which the specified {@link Predicate} returns
   * {@code true}.
   *
   * @param removalFilter
   *          A {@link Predicate} that decides if a field should be removed. May be {@code null}. In that case all
   *          fields are removed.
   * @return This generator.
   */
  TYPE withoutField(Predicate<IFieldGenerator<?>> removalFilter);
}
