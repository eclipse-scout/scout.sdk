/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.generator.annotation.IAnnotationGenerator;

/**
 * <h3>{@link IAnnotatableGenerator}</h3>
 * <p>
 * An {@link ISourceGenerator} that creates source for annotatable Java elements.
 *
 * @since 6.1.0
 */
public interface IAnnotatableGenerator<TYPE extends IAnnotatableGenerator<TYPE>> extends IJavaElementGenerator<TYPE> {

  /**
   * Adds the specified {@link IAnnotationGenerator} to this generator.
   *
   * @param generator
   *          The annotation generator to add. May be {@code null}, then nothing is added.
   * @return This generator
   */
  TYPE withAnnotation(IAnnotationGenerator<?> generator);

  /**
   * Removes all annotations from this generator.
   *
   * @return This generator
   */
  TYPE withoutAllAnnotations();

  /**
   * Removes the annotation with the given fully qualified name from this generator. Only annotations with context
   * independent names can be removed.
   * 
   * @param annotationFqn
   *          The fully qualified name of the annotation to remove. E.g. "{@code java.lang.Override}".
   * @return This generator.
   */
  TYPE withoutAnnotation(String annotationFqn);

  /**
   * Removes all {@link IAnnotationGenerator IAnnotationGenerators} for which the given {@link Predicate} returns
   * {@code true}.
   * 
   * @param removalFilter
   *          A {@link Predicate} that decides if a {@link IAnnotationGenerator} should be removed. May be {@code null}.
   *          In that case all {@link IAnnotationGenerator} are removed.
   * @return This generator.
   */
  TYPE withoutAnnotation(Predicate<IAnnotationGenerator<?>> removalFilter);

  /**
   * @return A {@link Stream} returning all {@link IAnnotationGenerator}s that exist for this generator.
   */
  Stream<IAnnotationGenerator<?>> annotations();

  /**
   * Gets the first {@link IAnnotationGenerator} having the given annotation name.<br>
   * <br>
   * <b>Note</b>: If the name of a {@link IAnnotationGenerator} is specified using
   * {@link IAnnotationGenerator#withElementNameFrom(Class, Function)} the name is API dependant and may therefore not
   * be found by this method.
   * 
   * @param annotationFqn
   *          The fully qualified annotation name to search. Must not be {@code null}.
   * @return An {@link Optional} containing the {@link IAnnotationGenerator} with given name or an empty
   *         {@link Optional}.
   */
  Optional<IAnnotationGenerator<?>> annotation(String annotationFqn);
}
