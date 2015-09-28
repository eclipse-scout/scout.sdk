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

import java.util.Map;

import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;

/**
 * <h3>{@link IAnnotation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IAnnotation extends IJavaElement {

  /**
   * @return the fully qualified type name of the annotations {@link #type()}
   */
  String name();

  /**
   * Gets all attributes of this {@link IAnnotation}.<br>
   * The {@link Map} iterates over the attributes in the order as they appear in the source or class file.
   *
   * @return A {@link Map} containing the attribute name ({@link IAnnotationValue#name()}) as key and the
   *         {@link IAnnotationValue} as value. Never returns <code>null</code>.
   */
  Map<String, IAnnotationValue> values();

  /**
   * Gets the {@link IAnnotationValue} of the annotation attribute with the given name.
   *
   * @param name
   *          The name of the {@link IAnnotationValue} to return.
   * @return The {@link IAnnotationValue} with the given name or <code>null</code> if no attribute with given name
   *         exists.
   */
  IAnnotationValue value(String name);

  /**
   * Gets the object on which this {@link IAnnotation} is defined.
   *
   * @return The owner {@link IAnnotatable} of this {@link IAnnotation}. Never returns <code>null</code>.
   */
  IAnnotatable owner();

  /**
   * Gets the annotation definition {@link IType}.<br>
   * Returns e.g. the type {@link Override}.
   *
   * @return The annotation definition type. Never returns <code>null</code>.
   */
  IType type();

  @Override
  AnnotationSpi unwrap();
}
