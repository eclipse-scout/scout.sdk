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

import org.eclipse.scout.sdk.core.model.spi.AnnotationElementSpi;

/**
 * <h3>{@link IAnnotationElement}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IAnnotationElement extends IJavaElement {

  /**
   * Gets the value of this annotation element.
   *
   * @return The {@link IMetaValue} of this element.
   */
  IMetaValue value();

  /**
   * Gets the {@link IAnnotation} this {@link IAnnotationElement} belongs to.
   *
   * @return The declaring {@link IAnnotation}.
   */
  IAnnotation declaringAnnotation();

  /**
   * Gets if this {@link IAnnotationElement} has been explicitly specified on the {@link IAnnotation} source code or if
   * it is the default inherited from the annotation declaration.
   *
   * @return <code>true</code> if this {@link IAnnotationElement} was not declared in source code but is the default of
   *         the annotation. <code>false</code> otherwise.
   */
  boolean isDefault();

  /**
   * Gets the source of the value expression of this {@link IAnnotationElement}.<br>
   * <br>
   * <b>Examples:</b><br>
   * <ul>
   * <li>@Annotation(type = Integer.class) -> sourceOfExpression() = "Integer.class"</li>
   * <li>@Annotation(mode = RoundingMode.HALF_UP) -> sourceOfExpression() = "RoundingMode.HALF_UP"</li>
   * <li>@Annotation(anno = @Generated("g3")) -> sourceOfExpression() = "@Generated("g1")"</li>
   * </ul>
   *
   * @return The {@link ISourceRange} of the value expression of this {@link IAnnotationElement}. Never returns
   *         <code>null</code>. Use {@link ISourceRange#isAvailable()} to check if source is actually available for this
   *         element.
   */
  ISourceRange sourceOfExpression();

  @Override
  AnnotationElementSpi unwrap();

}
