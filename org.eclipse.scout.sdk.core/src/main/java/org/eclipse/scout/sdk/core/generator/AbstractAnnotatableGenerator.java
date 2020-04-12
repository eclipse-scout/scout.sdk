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

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
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
        .map(a -> a.toWorkingCopy(transformer))
        .collect(toList());
  }

  @Override
  public TYPE withAnnotation(IAnnotationGenerator<?> generator) {
    if (generator != null) {
      m_annotations.add(generator);
    }
    return currentInstance();
  }

  @Override
  public TYPE withoutAnnotation(String annotationFqn) {
    Ensure.notNull(annotationFqn);
    m_annotations.removeIf(g -> annotationFqn.equals(g.elementName().orElse(null)));
    return currentInstance();
  }

  @Override
  public Stream<IAnnotationGenerator<?>> annotations() {
    return m_annotations.stream();
  }

  @Override
  public TYPE clearAnnotations() {
    m_annotations.clear();
    return currentInstance();
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
