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

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer.transformAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.java.builder.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link AbstractAnnotatableGenerator}</h3>
 *
 * @since 6.1.0
 */
public abstract class AbstractAnnotatableGenerator<TYPE extends IAnnotatableGenerator<TYPE>> extends AbstractJavaElementGenerator<TYPE> implements IAnnotatableGenerator<TYPE> {

  private final List<IAnnotationGenerator<?>> m_annotations;

  protected AbstractAnnotatableGenerator() {
    m_annotations = new ArrayList<>();
  }

  protected AbstractAnnotatableGenerator(IAnnotatable element, IWorkingCopyTransformer transformer) {
    super(element);
    m_annotations = element.annotations().stream()
        .map(a -> transformAnnotation(a, transformer))
        .flatMap(Optional::stream)
        .collect(toList());
  }

  @Override
  public TYPE withAnnotation(IAnnotationGenerator<?> generator) {
    if (generator != null) {
      m_annotations.add(generator);
    }
    return thisInstance();
  }

  @Override
  public TYPE withoutAllAnnotations() {
    m_annotations.clear();
    return thisInstance();
  }

  @Override
  public TYPE withoutAnnotation(String annotationFqn) {
    return withoutAnnotation(g -> Objects.equals(annotationFqn, g.elementName().orElse(null)));
  }

  @Override
  public TYPE withoutAnnotation(Predicate<IAnnotationGenerator<?>> removalFilter) {
    if (removalFilter == null) {
      return withoutAllAnnotations();
    }
    m_annotations.removeIf(removalFilter);
    return thisInstance();
  }

  @Override
  public Optional<IAnnotationGenerator<?>> annotation(String annotationFqn) {
    Ensure.notNull(annotationFqn);
    return m_annotations.stream()
        .filter(g -> annotationFqn.equals(g.elementName().orElse(null)))
        .findAny();
  }

  @Override
  public Stream<IAnnotationGenerator<?>> annotations() {
    return m_annotations.stream();
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);
    createAnnotations(builder);
  }

  protected String annotationDelimiter(ISourceBuilder<?> builder) {
    return builder.context().lineDelimiter();
  }

  protected void createAnnotations(IJavaSourceBuilder<?> builder) {
    if (m_annotations.isEmpty()) {
      return;
    }

    // collect sources of all annotations and add the sources sorted by length
    m_annotations.stream()
        .map(g -> g.toJavaSource(builder.context()))
        .sorted(comparingInt(CharSequence::length).thenComparing(Strings::compareTo))
        .forEach(g -> builder.append(g).append(annotationDelimiter(builder)));
  }
}
