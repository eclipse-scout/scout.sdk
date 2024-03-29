/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import java.util.Optional;

import org.eclipse.scout.sdk.core.java.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.java.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.SourceRange;

/**
 * <h3>{@link IField}</h3> Represents a field in a java type.
 *
 * @since 5.1.0
 */
public interface IField extends IMember {

  /**
   * Gets the constant value of this {@link IField}.<br>
   * <br>
   * <b>Note:</b> The field must be initialized with a constant value so that it can be retrieved using this method.
   *
   * @return The constant value of this {@link IField} if it can be computed or an empty {@link Optional} if it cannot
   *         be computed or the field has no constant value assigned.
   */
  Optional<IMetaValue> constantValue();

  /**
   * Gets the data type of this {@link IField}.
   *
   * @return The {@link IType} describing the data type of this {@link IField}.
   */
  IType dataType();

  /**
   * Gets the source of this {@link IField} behind the "=" character.
   *
   * @return The initializer source.
   */
  Optional<SourceRange> sourceOfInitializer();

  /**
   * @return The {@link IType} this {@link IField} is declared in.
   */
  IType requireDeclaringType();

  @Override
  FieldSpi unwrap();

  @Override
  IFieldGenerator<?> toWorkingCopy();

  @Override
  IFieldGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer);
}
