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

import java.util.Map;

import org.eclipse.scout.sdk.core.model.api.IAnnotation;

/**
 * <h3>{@link AnnotationSpi}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface AnnotationSpi extends JavaElementSpi {

  /**
   * Gets all attributes of this {@link AnnotationSpi}.<br>
   * The {@link Map} iterates over the attributes in the order as they appear in the source or class file.
   *
   * @return A {@link Map} containing the attribute name ({@link AnnotationElementSpi#name()}) as key and the
   *         {@link AnnotationElementSpi} as value. Never returns <code>null</code>.
   */
  Map<String, AnnotationElementSpi> getValues();

  /**
   * Gets the {@link AnnotationElementSpi} of the annotation attribute with the given name.
   *
   * @param name
   *          The name of the {@link AnnotationElementSpi} to return.
   * @return The {@link AnnotationElementSpi} with the given name or <code>null</code> if no attribute with given name
   *         exists.
   */
  AnnotationElementSpi getValue(String name);

  /**
   * Gets the object on which this {@link AnnotationSpi} is defined.
   *
   * @return The owner {@link AnnotatableSpi} of this {@link AnnotationSpi}. Never returns <code>null</code>.
   */
  AnnotatableSpi getOwner();

  /**
   * Gets the annotation definition {@link TypeSpi}.<br>
   * Returns e.g. the type {@link Override}.
   *
   * @return The annotation definition type. Never returns <code>null</code>.
   */
  TypeSpi getType();

  @Override
  IAnnotation wrap();
}
