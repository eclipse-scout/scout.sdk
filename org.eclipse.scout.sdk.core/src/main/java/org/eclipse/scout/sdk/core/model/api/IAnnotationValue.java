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

import org.eclipse.scout.sdk.core.model.spi.AnnotationValueSpi;

/**
 * <h3>{@link IAnnotationValue}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IAnnotationValue extends IJavaElement {

  /**
   * Gets the value of the annotation attribute.
   *
   * @return never null
   */
  IMetaValue getMetaValue();

  /**
   * Gets the {@link IAnnotation} this {@link IAnnotationValue} belongs to.
   *
   * @return The declaring {@link IAnnotation}
   */
  IAnnotation getDeclaringAnnotation();

  /**
   * @return true if this {@link IAnnotationValue} was not declared in source code but is the default value of the
   *         annotation
   */
  boolean isSyntheticDefaultValue();

  ISourceRange getSourceOfExpression();

  @Override
  AnnotationValueSpi unwrap();

}