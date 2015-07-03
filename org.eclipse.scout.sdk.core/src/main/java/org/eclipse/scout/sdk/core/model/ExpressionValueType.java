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

/**
 * <h3>{@link ExpressionValueType}</h3>
 * Defines the data type of an annotation attribute value. For the specific types (all except {@link #Unknown}) it is
 * safe to cast them to the corresponding java data type (see description of the specific values below).
 *
 * @author Matthias Villiger
 * @since 5.1.0
 * @see IAnnotationValue
 */
public enum ExpressionValueType {
  /**
   * An annotation value of type {@link Character}.
   */
  Char,

  /**
   * An annotation value of type {@link Byte}.
   */
  Byte,

  /**
   * An annotation value of type {@link Short}.
   */
  Short,

  /**
   * An annotation value of type {@link Boolean}.
   */
  Bool,

  /**
   * An annotation value of type {@link Long}.
   */
  Long,

  /**
   * An annotation value of type {@link Double}.
   */
  Double,

  /**
   * An annotation value of type {@link Float}.
   */
  Float,

  /**
   * An annotation value of type {@link Integer}.
   */
  Int,

  /**
   * An annotation value of type {@link String}.
   */
  String,

  /**
   * An annotation value of type {@link IType}.
   */
  Type,

  /**
   * The annotation value is an array of values. Each array element is of type {@link IAnnotationValue}. The array
   * itself is safe to cast to {@link IAnnotationValue}[]
   */
  Array,

  /**
   * An annotation value of type {@link IAnnotation}.
   */
  Annotation,

  /**
   * An unknown annotation value type.
   */
  Unknown
}
