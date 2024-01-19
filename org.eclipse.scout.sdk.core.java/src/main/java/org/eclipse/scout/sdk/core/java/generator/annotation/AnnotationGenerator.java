/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.annotation;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.scout.sdk.core.generator.ISourceGenerator.raw;
import static org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer.transformAnnotationElement;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.builder.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.AbstractJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.java.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link AnnotationGenerator}</h3>
 *
 * @since 6.1.0
 */
public class AnnotationGenerator<TYPE extends IAnnotationGenerator<TYPE>> extends AbstractJavaElementGenerator<TYPE> implements IAnnotationGenerator<TYPE> {

  private final Map<JavaBuilderContextFunction<String> /* element name */, ISourceGenerator<IExpressionBuilder<?>>> m_elements;

  protected AnnotationGenerator() {
    m_elements = new LinkedHashMap<>();
  }

  protected AnnotationGenerator(IAnnotation annotation, IWorkingCopyTransformer transformer) {
    super(annotation);
    withElementName(annotation.type().name());
    m_elements = annotation.elements().values().stream()
        .filter(ae -> !ae.isDefault())
        .map(ae -> transformAndAssociateWithName(ae, transformer))
        .flatMap(Optional::stream)
        .collect(toMap(Entry::getKey, Entry::getValue, Ensure::failOnDuplicates, LinkedHashMap::new));
  }

  protected static Optional<Entry<JavaBuilderContextFunction<String>, ISourceGenerator<IExpressionBuilder<?>>>> transformAndAssociateWithName(IAnnotationElement ae, IWorkingCopyTransformer transformer) {
    return transformAnnotationElement(ae, transformer)
        .map(g -> new SimpleImmutableEntry<>(JavaBuilderContextFunction.create(ae.elementName()), g));
  }

  /**
   * @return A new empty {@link IAnnotationGenerator}.
   */
  public static IAnnotationGenerator<?> create() {
    return new AnnotationGenerator<>();
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
   * @return A new {@link Override} {@link IAnnotationGenerator}.
   */
  public static IAnnotationGenerator<?> createOverride() {
    return new AnnotationGenerator<>()
        .withElementName(Override.class.getName());
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
  public static IAnnotationGenerator<?> createSuppressWarnings(CharSequence... values) {
    return new AnnotationGenerator<>()
        .withElementName(SuppressWarnings.class.getName())
        .withElement("value", b -> b.stringLiteralArray(values, false, true));
  }

  @Override
  public <A extends IApiSpecification> TYPE withAnnotationNameFrom(Class<A> apiDefinition, Function<A, ITypeNameSupplier> nameSupplier) {
    if (nameSupplier == null) {
      return withElementNameFunc(null);
    }
    return withElementNameFunc(c -> {
      var classNameSupplier = new ApiFunction<>(apiDefinition, nameSupplier).apply(c);
      return classNameSupplier == null ? null : classNameSupplier.fqn();
    });
  }

  @Override
  public TYPE withElement(String name, CharSequence valueSrc) {
    return withElement(name, raw(valueSrc));
  }

  @Override
  public TYPE withElement(String name, ISourceGenerator<IExpressionBuilder<?>> value) {
    if (value != null && Strings.hasText(name)) {
      m_elements.put(JavaBuilderContextFunction.create(name), value);
    }
    return thisInstance();
  }

  @Override
  public <A extends IApiSpecification> TYPE withElementFrom(Class<A> apiDefinition, Function<A, String> elementNameSupplier, CharSequence valueSrc) {
    return withElementFrom(apiDefinition, elementNameSupplier, raw(valueSrc));
  }

  @Override
  public <A extends IApiSpecification> TYPE withElementFrom(Class<A> apiDefinition, Function<A, String> elementNameSupplier, ISourceGenerator<IExpressionBuilder<?>> value) {
    if (value != null && elementNameSupplier != null) {
      m_elements.put(new ApiFunction<>(apiDefinition, elementNameSupplier), value);
    }
    return thisInstance();
  }

  @Override
  public TYPE withElementFunc(Function<IJavaBuilderContext, String> elementNameSupplier, CharSequence valueSrc) {
    return withElementFunc(elementNameSupplier, raw(valueSrc));
  }

  @Override
  public TYPE withElementFunc(Function<IJavaBuilderContext, String> elementNameSupplier, ISourceGenerator<IExpressionBuilder<?>> value) {
    if (value != null && elementNameSupplier != null) {
      m_elements.put(JavaBuilderContextFunction.create(elementNameSupplier), value);
    }
    return thisInstance();
  }

  @Override
  public Optional<ISourceGenerator<IExpressionBuilder<?>>> element(String name) {
    return m_elements.entrySet().stream()
        .filter(e -> e.getKey().apply().filter(isEqual(name)).isPresent())
        .reduce((a, b) -> b) // find last
        .map(Entry::getValue);
  }

  @Override
  public Optional<ISourceGenerator<IExpressionBuilder<?>>> element(Predicate<JavaBuilderContextFunction<String>> selector) {
    Ensure.notNull(selector, "Element selector must not be null.");
    return m_elements.entrySet().stream()
        .filter(entry -> selector.test(entry.getKey()))
        .reduce((a, b) -> b) // find last
        .map(Entry::getValue);
  }

  @Override
  public Map<String, ISourceGenerator<IExpressionBuilder<?>>> elements() {
    return elementsFunc().entrySet().stream()
        .map(e -> e.getKey().apply().map(elementName -> new SimpleImmutableEntry<>(elementName, e.getValue())))
        .flatMap(Optional::stream)
        .collect(toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));
  }

  @Override
  public Map<JavaBuilderContextFunction<String>, ISourceGenerator<IExpressionBuilder<?>>> elementsFunc() {
    return unmodifiableMap(m_elements);
  }

  @Override
  public TYPE withoutElement(String elementName) {
    return withoutElement(f -> f.apply()
        .filter(isEqual(elementName))
        .isPresent());
  }

  @Override
  public TYPE withoutElement(Predicate<JavaBuilderContextFunction<String>> toRemove) {
    if (toRemove == null) {
      m_elements.clear();
    }
    else {
      m_elements.keySet().removeIf(toRemove);
    }
    return thisInstance();
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);

    var annotationName = elementName(builder.context()).orElseThrow(() -> newFail("Annotation name missing for generator {}", this));
    builder.at().ref(annotationName);

    if (m_elements.isEmpty()) {
      return;
    }

    builder.parenthesisOpen();
    buildElements(ExpressionBuilder.create(builder));
    builder.parenthesisClose();
  }

  protected void buildElements(IExpressionBuilder<?> builder) {
    // evaluate the elementName function and collect to a map
    var context = builder.context();
    var elementMap = m_elements.entrySet().stream()
        .map(entry -> new SimpleImmutableEntry<>(entry.getKey().apply(context), entry.getValue()))
        .filter(entry -> !Strings.isEmpty(entry.getKey()))
        .collect(toMap(Entry::getKey, Entry::getValue, (a, b) -> b /* last element with a certain name wins */, LinkedHashMap::new));
    var isSingleValueAnnotation = buildSingleValueAnnotation(elementMap, builder);
    if (!isSingleValueAnnotation) {
      buildNamedValueAnnotation(elementMap, builder);
    }
  }

  protected static void buildNamedValueAnnotation(Map<String, ISourceGenerator<IExpressionBuilder<?>>> elementMap, IExpressionBuilder<?> builder) {
    var elementDelimiter = new StringBuilder(3);
    elementDelimiter.append(JavaTypes.C_COMMA);
    if (elementMap.size() > 4) {
      elementDelimiter.append(builder.context().lineDelimiter());
    }
    else {
      elementDelimiter.append(JavaTypes.C_SPACE);
    }

    var elementGenerators = elementMap
        .entrySet().stream()
        .map(e -> toElementGenerator(builder, e.getKey(), e.getValue()))
        .map(g -> g.generalize(builder));
    builder.append(elementGenerators, null, elementDelimiter, null);
  }

  protected static ISourceGenerator<IJavaSourceBuilder<?>> toElementGenerator(IExpressionBuilder<?> builder, String elementName, ISourceGenerator<IExpressionBuilder<?>> valueGenerator) {
    return b -> b.append(elementName).equalSign().append(valueGenerator.generalize(builder));
  }

  protected static boolean buildSingleValueAnnotation(Map<String, ISourceGenerator<IExpressionBuilder<?>>> elementMap, IExpressionBuilder<?> builder) {
    if (elementMap.size() != 1) {
      return false;
    }
    var element = elementMap.entrySet().iterator().next();
    var elementName = element.getKey();
    var valueGenerator = element.getValue();
    if (!"value".equals(elementName)) {
      return false;
    }
    valueGenerator.generate(builder);
    return true;
  }
}
