/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.generator;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;

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
   * Removes all {@link IAnnotationGenerator}s having the specified name from this generator.
   *
   * @param annotationFqn
   *          The fully qualified name of the {@link IAnnotationGenerator}s to remove.
   * @return This generator
   */
  TYPE withoutAnnotation(Predicate<IAnnotationGenerator<?>> removalFilter);

  /**
   * @return A {@link Stream} returning all {@link IAnnotationGenerator}s that exist for this generator.
   */
  Stream<IAnnotationGenerator<?>> annotations();

  Optional<IAnnotationGenerator<?>> annotation(String annotationFqn);

  /**
   * Removes all annotations from this generator.
   *
   * @return This generator
   */
  TYPE clearAnnotations();
}
