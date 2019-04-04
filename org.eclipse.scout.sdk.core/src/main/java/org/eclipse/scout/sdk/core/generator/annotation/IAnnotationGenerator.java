/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.generator.annotation;

import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link IAnnotationGenerator}</h3>
 *
 * @since 6.1.0
 */
public interface IAnnotationGenerator<TYPE extends IAnnotationGenerator<TYPE>> extends IJavaElementGenerator<TYPE> {

  /**
   * Gets the fully qualified name of the annotation type.
   * <p>
   * E.g. {@code java.lang.Override}
   *
   * @return the fully qualified name of the annotation or an empty {@link Optional} if it is not yet set.
   */
  @Override
  Optional<String> elementName();

  /**
   * Adds a new element to this {@link IAnnotationGenerator} using the specified name and raw value. If there exists a
   * value with given name already, the existing one is replaced.
   *
   * @param name
   *          The name of the annotation element. Must not be blank (see {@link Strings#isBlank(CharSequence)}.
   * @param rawSrc
   *          The raw value as Java source. Must not be {@code null}.
   * @return This generator
   * @see ISourceGenerator#raw(CharSequence)
   */
  TYPE withElement(String name, String rawSrc);

  /**
   * Adds a new element to this {@link IAnnotationGenerator} using the specified name and value generator. If there
   * exists a value with given name already, the existing one is replaced.
   *
   * @param name
   *          The name of the annotation element. Must not be blank (see {@link Strings#isBlank(CharSequence)}.
   * @param value
   *          The {@link ISourceGenerator} that creates the element value. If the generator is {@code null}, this method
   *          does nothing.
   * @return This generator
   */
  TYPE withElement(String name, ISourceGenerator<IExpressionBuilder<?>> value);

  /**
   * Gets the {@link ISourceGenerator} for the specified element.
   *
   * @param name
   *          The name for which the associated {@link ISourceGenerator} should be returned.
   * @return An {@link Optional} holding the {@link ISourceGenerator} for this element name.
   */
  Optional<ISourceGenerator<IExpressionBuilder<?>>> element(String name);

  /**
   * Gets a {@link Map} with all elements of this {@link IAnnotationGenerator}.
   * <p>
   * The key is the element name. The value is the {@link ISourceGenerator} generating the value source for the
   * corresponding element.
   * <p>
   * The {@link Map} iterates through the elements in the order in which they have been added.
   *
   * @return An unmodifiable {@link Map} with the elements and the associated {@link ISourceGenerator}s.
   */
  Map<String, ISourceGenerator<IExpressionBuilder<?>>> elements();

  /**
   * Removes the element with specified name from this {@link IAnnotationGenerator}.
   * 
   * @param name
   *          The name of the element to remove.
   * @return This generator
   */
  TYPE withoutElement(String name);
}
