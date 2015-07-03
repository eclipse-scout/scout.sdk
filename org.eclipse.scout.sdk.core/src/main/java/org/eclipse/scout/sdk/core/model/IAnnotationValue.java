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
 * <h3>{@link IAnnotationValue}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IAnnotationValue {

  /**
   * Gets the name of the annotation attribute this {@link IAnnotationValue} belongs to.
   *
   * @return The name of the annotation attribute this {@link IAnnotationValue} belongs to. Never returns
   *         <code>null</code>.
   */
  String getName();

  /**
   * Gets the value of the annotation attribute. The data type of the value returned is defined by
   * {@link #getValueType()}.
   *
   * @return The real value of the annotation attribute or <code>null</code> if the value cannot be parsed.
   * @see ExpressionValueType
   * @see #getValueType()
   */
  Object getValue();

  /**
   * Gets the data type of this {@link IAnnotationValue}. If the data type cannot be determined, this method returns
   * {@link ExpressionValueType#Unknown}.
   *
   * @return The data type of this {@link IAnnotationValue}. Never returns <code>null</code>.
   */
  ExpressionValueType getValueType();

  /**
   * Gets the {@link IAnnotation} this {@link IAnnotationValue} belongs to.
   * 
   * @return The owner {@link IAnnotation}
   */
  IAnnotation getOwnerAnnotation();

}
