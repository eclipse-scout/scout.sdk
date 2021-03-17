/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.generator.annotation;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.scout.sdk.core.generator.ISourceGenerator.raw;
import static org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.transformAnnotationElement;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.AbstractJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.model.annotation.GeneratedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link AnnotationGenerator}</h3>
 *
 * @since 6.1.0
 */
public class AnnotationGenerator<TYPE extends IAnnotationGenerator<TYPE>> extends AbstractJavaElementGenerator<TYPE> implements IAnnotationGenerator<TYPE> {

  private final Map<ApiFunction<?, String> /* element name */, ISourceGenerator<IExpressionBuilder<?>>> m_values;
  private ApiFunction<?, IClassNameSupplier> m_name;

  protected AnnotationGenerator(IAnnotation annotation, IWorkingCopyTransformer transformer) {
    super(annotation);
    withElementName(annotation.type().name());
    m_values = annotation.elements().values().stream()
        .filter(ae -> !ae.isDefault())
        .map(ae -> transformAndAssociateWithName(ae, transformer))
        .flatMap(Optional::stream)
        .collect(toMap(Entry::getKey, Entry::getValue, Ensure::failOnDuplicates, LinkedHashMap::new));
  }

  protected static Optional<Entry<ApiFunction<?, String>, ISourceGenerator<IExpressionBuilder<?>>>> transformAndAssociateWithName(IAnnotationElement ae, IWorkingCopyTransformer transformer) {
    return transformAnnotationElement(ae, transformer)
        .map(g -> new SimpleImmutableEntry<>(new ApiFunction<>(ae.elementName()), g));
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
  public static IAnnotationGenerator<?> createGenerated(CharSequence typeThatGeneratedTheCode) {
    return createGenerated(typeThatGeneratedTheCode, "This class is auto generated. No manual modifications recommended.");
  }

  /**
   * @param typeThatGeneratedTheCode
   *          The name of the class the generated (derived) element is based on. Must not be blank.
   * @param comments
   *          The comment value of the {@code Generated} annotation. May be {@code null}.
   * @return A new {@code Generated} {@link IAnnotationGenerator} with the specified value and comment.
   */
  public static IAnnotationGenerator<?> createGenerated(CharSequence typeThatGeneratedTheCode, CharSequence comments) {
    IAnnotationGenerator<?> result = new AnnotationGenerator<>()
        .withElementName(GeneratedAnnotation.FQN)
        .withElement(GeneratedAnnotation.VALUE_ELEMENT_NAME, b -> b.stringLiteral(Ensure.notBlank(typeThatGeneratedTheCode)));
    Strings.notBlank(comments).ifPresent(c -> result.withElement(GeneratedAnnotation.COMMENTS_ELEMENT_NAME, b -> b.stringLiteral(c)));
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
  public static IAnnotationGenerator<?> createSuppressWarnings(CharSequence... values) {
    return new AnnotationGenerator<>()
        .withElementName(SuppressWarnings.class.getName())
        .withElement("value", b -> b.stringLiteralArray(values, false, true));
  }

  @Override
  public TYPE withElementName(String newName) {
    var annotationName = IClassNameSupplier.raw(newName);
    return withElementNameFrom(null, api -> annotationName);
  }

  @Override
  public <A extends IApiSpecification> TYPE withElementNameFrom(Class<A> apiDefinition, Function<A, IClassNameSupplier> nameSupplier) {
    if (nameSupplier == null) {
      m_name = null;
      super.withElementName(null);
    }
    else {
      m_name = new ApiFunction<>(apiDefinition, nameSupplier);
      if (apiDefinition == null) {
        super.withElementName(nameSupplier.apply(null).fqn());
      }
      else {
        super.withElementName(null);
      }
    }
    return thisInstance();
  }

  @Override
  public TYPE withElement(String name, CharSequence valueSrc) {
    return withElement(name, raw(valueSrc));
  }

  @Override
  public TYPE withElement(String name, ISourceGenerator<IExpressionBuilder<?>> value) {
    return withElementFrom(null, api -> name, value);
  }

  @Override
  public <A extends IApiSpecification> TYPE withElementFrom(Class<A> apiDefinition, Function<A, String> elementNameSupplier, CharSequence valueSrc) {
    return withElementFrom(apiDefinition, elementNameSupplier, raw(valueSrc));
  }

  @Override
  public <A extends IApiSpecification> TYPE withElementFrom(Class<A> apiDefinition, Function<A, String> elementNameSupplier, ISourceGenerator<IExpressionBuilder<?>> value) {
    if (value == null) {
      return thisInstance();
    }
    m_values.put(new ApiFunction<>(apiDefinition, elementNameSupplier), value);
    return thisInstance();
  }

  @Override
  public Optional<ISourceGenerator<IExpressionBuilder<?>>> element(Predicate<ApiFunction<?, String>> selector) {
    Ensure.notNull(selector, "Element selector must not be null.");
    return m_values
        .entrySet().stream()
        .filter(entry -> selector.test(entry.getKey()))
        .reduce((a, b) -> b) // find last
        .map(Entry::getValue);
  }

  @Override
  public Map<ApiFunction<?, String>, ISourceGenerator<IExpressionBuilder<?>>> elements() {
    return unmodifiableMap(m_values);
  }

  @Override
  public TYPE withoutElement(Predicate<ApiFunction<?, String>> toRemove) {
    if (toRemove == null) {
      m_values.clear();
    }
    else {
      m_values.keySet().removeIf(toRemove);
    }
    return thisInstance();
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);

    var annotationName = elementName(builder.context())
        .orElseThrow(() -> newFail("Annotation name missing for generator {}", this));
    builder.at().ref(annotationName);

    if (m_values.isEmpty()) {
      return;
    }

    builder.parenthesisOpen();
    buildElements(ExpressionBuilder.create(builder));
    builder.parenthesisClose();
  }

  @Override
  public Optional<String> elementName(IJavaBuilderContext context) {
    var env = Optional.ofNullable(context)
        .flatMap(IJavaBuilderContext::environment)
        .orElse(null);
    return elementName(env);
  }

  @Override
  public Optional<String> elementName(IJavaEnvironment context) {
    return Optional.ofNullable(m_name)
        .flatMap(api -> api.apply(context))
        .map(IClassNameSupplier::fqn)
        .filter(Strings::hasText);
  }

  protected void buildElements(IExpressionBuilder<?> builder) {
    // evaluate the elementName function and collect to a map
    var context = builder.context();
    Map<String, ISourceGenerator<IExpressionBuilder<?>>> elementMap = m_values.entrySet()
        .stream()
        .map(entry -> evaluateElementName(entry, context))
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

  protected static Entry<String, ISourceGenerator<IExpressionBuilder<?>>> evaluateElementName(Entry<ApiFunction<?, String>, ISourceGenerator<IExpressionBuilder<?>>> entry, IJavaBuilderContext context) {
    var elementName = entry.getKey().apply(context).orElseThrow(() -> newFail(""));
    return new SimpleImmutableEntry<>(elementName, entry.getValue());
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
