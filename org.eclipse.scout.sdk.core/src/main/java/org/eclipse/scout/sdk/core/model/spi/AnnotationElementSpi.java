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

import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;

/**
 * <h3>{@link AnnotationElementSpi}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface AnnotationElementSpi extends JavaElementSpi {

  /**
   * Gets the value of the annotation attribute.
   *
   * @return never null
   */
  IMetaValue getMetaValue();

  /**
   * Gets the {@link AnnotationSpi} this {@link AnnotationElementSpi} belongs to.
   *
   * @return The declaring {@link AnnotationSpi}
   */
  AnnotationSpi getDeclaringAnnotation();

  /**
   * @return true if this {@link AnnotationElementSpi} was not declared in source code but is the default value of the
   *         annotation
   */
  boolean isDefaultValue();

  ISourceRange getSourceOfExpression();

  @Override
  IAnnotationElement wrap();
}
