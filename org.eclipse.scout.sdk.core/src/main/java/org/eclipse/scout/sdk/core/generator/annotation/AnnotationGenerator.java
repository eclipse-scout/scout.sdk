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
package org.eclipse.scout.sdk.core.generator.annotation;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.transformAnnotationElement;
import static org.eclipse.scout.sdk.core.util.Ensure.failOnDuplicates;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.AbstractJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.model.annotation.GeneratedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link AnnotationGenerator}</h3>
 *
 * @since 6.1.0
 */
public class AnnotationGenerator<TYPE extends IAnnotationGenerator<TYPE>> extends AbstractJavaElementGenerator<TYPE> implements IAnnotationGenerator<TYPE> {

  private final Map<String, ISourceGenerator<IExpressionBuilder<?>>> m_values;

  protected AnnotationGenerator(IAnnotation annotation, IWorkingCopyTransformer transformer) {
    super(annotation);
    withElementName(annotation.type().name());
    m_values = annotation.elements().values().stream()
        .filter(ae -> !ae.isDefault())
        .collect(toMap(IJavaElement::elementName,
            ae -> transformAnnotationElement(ae, transformer),
            failOnDuplicates(),
            LinkedHashMap::new));
  }

  protected AnnotationGenerator() {
    m_values = new LinkedHashMap<>();
  }

  /**
   * Creates a new {@link IAnnotationGenerator} based on the given {@link IAnnotation}.
   * <p>
   * <b>Example:</b> See {@link IWorkingCopyTransformer}.
   *
   * @param annotation
   *          The {@link IAnnotation} that should be converted to an {@link IAnnotationGenerator}. Must not be
   *          {@code null}.
   * @param transformer
   *          An optional {@link IWorkingCopyTransformer} callback that is responsible for transforming the annotation
   *          to a working copy. May be {@code null} if no custom transformation is required and the annotation should
   *          be converted into a working copy without any modification.
   * @return A new {@link IAnnotationGenerator} initialized to generate source that is structurally similar to the one
   *         from the given {@link IAnnotation}.
   * @see DefaultWorkingCopyTransformer
   * @see SimpleWorkingCopyTransformerBuilder
   */
  public static IAnnotationGenerator<?> create(IAnnotation annotation, IWorkingCopyTransformer transformer) {
    return new AnnotationGenerator<>(annotation, transformer);
  }

  /**
   * @return A new empty {@link IAnnotationGenerator}.
   */
  public static IAnnotationGenerator<?> create() {
    return new AnnotationGenerator<>();
  }

  /**
   * @return A new {@link Override} {@link IAnnotationGenerator}.
   */
  public static IAnnotationGenerator<?> createOverride() {
    return new AnnotationGenerator<>()
        .withElementName(Override.class.getName());
  }

  /**
   * @param typeThatGeneratedTheCode
   *          The name of the class the generated (derived) element is based on. Must not be blank.
   * @return A new {@code Generated} {@link IAnnotationGenerator} with the specified value and a default comment.
   */
  public static IAnnotationGenerator<?> createGenerated(String typeThatGeneratedTheCode) {
    return createGenerated(typeThatGeneratedTheCode, "This class is auto generated. No manual modifications recommended.");
  }

  /**
   * @param typeThatGeneratedTheCode
   *          The name of the class the generated (derived) element is based on. Must not be blank.
   * @param comments
   *          The comment value of the {@code Generated} annotation. May be {@code null}.
   * @return A new {@code Generated} {@link IAnnotationGenerator} with the specified value and comment.
   */
  public static IAnnotationGenerator<?> createGenerated(String typeThatGeneratedTheCode, String comments) {
    IAnnotationGenerator<?> result = new AnnotationGenerator<>()
        .withElementName(GeneratedAnnotation.TYPE_NAME)
        .withElement(GeneratedAnnotation.VALUE_ELEMENT_NAME, b -> b.stringLiteral(Ensure.notBlank(typeThatGeneratedTheCode)));

    Strings.notBlank(comments)
        .ifPresent(c -> result.withElement(GeneratedAnnotation.COMMENTS_ELEMENT_NAME, b -> b.stringLiteral(c)));
    return result;
  }

  /**
   * @return A new {@link Deprecated} {@link IAnnotationGenerator}.
   */
  public static IAnnotationGenerator<?> createDeprecated() {
    return new AnnotationGenerator<>()
        .withElementName(Deprecated.class.getName());
  }

  /**
   * @param values
   *          The tokens to suppress. May not be {@code null}.
   * @return A new {@link SuppressWarnings} {@link IAnnotationGenerator} with the specified suppression values.
   */
  public static IAnnotationGenerator<?> createSupressWarnings(String... values) {
    return new AnnotationGenerator<>()
        .withElementName(SuppressWarnings.class.getName())
        .withElement("value", b -> b.stringLiteralArray(values, false, true));
  }

  @Override
  public TYPE withElement(String name, String valueSrc) {
    return withElement(name, ISourceGenerator.raw(valueSrc));
  }

  @Override
  public TYPE withElement(String name, ISourceGenerator<IExpressionBuilder<?>> value) {
    if (value != null) {
      m_values.put(Ensure.notBlank(name), value);
    }
    return currentInstance();
  }

  @Override
  public Optional<ISourceGenerator<IExpressionBuilder<?>>> element(String name) {
    return Optional.ofNullable(m_values.get(name));
  }

  @Override
  public Map<String, ISourceGenerator<IExpressionBuilder<?>>> elements() {
    return unmodifiableMap(m_values);
  }

  @Override
  public TYPE withoutElement(String name) {
    m_values.remove(name);
    return currentInstance();
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);

    builder.atSign().ref(elementName().orElseThrow(() -> newFail("Annotation name missing for generator {}", this)));

    if (m_values.isEmpty()) {
      return;
    }

    builder.parenthesisOpen();
    buildElements(ExpressionBuilder.create(builder));
    builder.parenthesisClose();
  }

  protected void buildElements(IExpressionBuilder<?> builder) {
    if (m_values.size() == 1 && m_values.containsKey("value")) {
      buildSingleValueAnnotation(builder);
      return;
    }
    buildMultiValueAnnotation(builder);
  }

  protected void buildMultiValueAnnotation(IExpressionBuilder<?> builder) {
    StringBuilder elementDelimiter = new StringBuilder(3);
    elementDelimiter.append(JavaTypes.C_COMMA);
    if (m_values.size() > 4) {
      elementDelimiter.append(builder.context().lineDelimiter());
    }
    else {
      elementDelimiter.append(JavaTypes.C_SPACE);
    }

    Stream<ISourceGenerator<ISourceBuilder<?>>> elementGenerators = m_values.entrySet().stream()
        .<ISourceGenerator<IJavaSourceBuilder<?>>> map(e -> b -> b.append(e.getKey()).equalSign().append(e.getValue().generalize(builder)))
        .map(jb -> jb.generalize(builder));
    builder.append(elementGenerators, null, elementDelimiter, null);
  }

  protected void buildSingleValueAnnotation(IExpressionBuilder<?> builder) {
    m_values.values().iterator().next().generate(builder);
  }
}
