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

import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.sdk.core.java.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;

/**
 * <h3>{@link IAnnotation}</h3><br>
 * Represents an annotation declaration.
 *
 * @since 5.1.0
 */
public interface IAnnotation extends IJavaElement {

  /**
   * @return the fully qualified type name of the annotations {@link #type()}
   */
  String name();

  /**
   * Gets all elements of this {@link IAnnotation}. This includes the elements that are not explicitly specified in the
   * source file.<br>
   * The {@link Map} iterates over the elements in the order as they appear in the annotation type declaration source or
   * class file. The order of the specific annotation occurrence has no influence.
   *
   * @return An unmodifiable {@link Map} containing the element name ({@link IAnnotationElement#elementName()}) as key
   *         and the {@link IAnnotationElement} as value.
   */
  Map<String, IAnnotationElement> elements();

  /**
   * Gets the {@link IAnnotationElement} of the annotation attribute with the given name.
   *
   * @param name
   *          The name of the {@link IAnnotationElement} to return.
   * @return The {@link IAnnotationElement} with the given name or an empty {@link Optional} if no element with given
   *         name exists.
   */
  Optional<IAnnotationElement> element(String name);

  /**
   * Gets the object on which this {@link IAnnotation} is defined.
   *
   * @return The owner {@link IAnnotatable} of this {@link IAnnotation}.
   */
  IAnnotatable owner();

  /**
   * Gets the annotation definition {@link IType}.<br>
   * Returns e.g. the type {@link Override}.
   *
   * @return The annotation definition type.
   */
  IType type();

  /**
   * Wraps this {@link IAnnotation} into the given {@link AbstractManagedAnnotation} class.
   *
   * @param managedAnnotationType
   *          The managed annotation class. The class must match this {@link IAnnotation} type.
   * @return The newly created managed annotation.
   */
  <A extends AbstractManagedAnnotation> A wrap(Class<A> managedAnnotationType);

  @Override
  AnnotationSpi unwrap();

  @Override
  IAnnotationGenerator<?> toWorkingCopy();

  @Override
  IAnnotationGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer);
}
