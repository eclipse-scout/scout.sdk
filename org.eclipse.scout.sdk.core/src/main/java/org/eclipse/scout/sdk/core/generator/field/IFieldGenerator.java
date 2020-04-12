/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.generator.field;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.member.IMemberGenerator;

/**
 * <h3>{@link IFieldGenerator}</h3>
 * <p>
 * An {@link ISourceGenerator} that creates Java fields and static blocks.
 *
 * @since 6.1.0
 */
public interface IFieldGenerator<TYPE extends IFieldGenerator<TYPE>> extends IMemberGenerator<TYPE> {

  /**
   * @return The data type of this {@link IFieldGenerator}.
   */
  Optional<String> dataType();

  /**
   * Sets the data type of this {@link IFieldGenerator}.
   *
   * @param reference
   *          The data type reference. E.g. {@code java.util.List<java.lang.String>}
   * @return This generator.
   */
  TYPE withDataType(String reference);

  /**
   * @return The value {@link ISourceGenerator} of this {@link IFieldGenerator}.
   */
  Optional<ISourceGenerator<IExpressionBuilder<?>>> value();

  /**
   * Sets the value of this {@link IFieldGenerator}.
   * <p>
   * If this {@link IFieldGenerator} has an {@link #elementName()}, this specifies the initial value expression of the
   * field (in this case {@link #dataType()} is required).<br>
   * Otherwise the value is printed directly. This allows to create static constructors or blocks.
   *
   * @param valueGenerator
   *          The {@link ISourceGenerator} that creates the value of this {@link IFieldGenerator}.
   * @return This generator.
   */
  TYPE withValue(ISourceGenerator<IExpressionBuilder<?>> valueGenerator);

  /**
   * Marks this {@link IFieldGenerator} to create a {@code transient} field.
   *
   * @return This generator.
   */
  TYPE asTransient();

  /**
   * Marks this {@link IFieldGenerator} to create a {@code volatile} field.
   *
   * @return This generator.
   */
  TYPE asVolatile();
}
