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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.builder.JavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.builder.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.builder.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link AbstractJavaElementGenerator}</h3>
 *
 * @since 6.1.0
 */
public abstract class AbstractJavaElementGenerator<TYPE extends IJavaElementGenerator<TYPE>> implements IJavaElementGenerator<TYPE> {

  private JavaBuilderContextFunction<String> m_elementName;
  private ISourceGenerator<IJavaElementCommentBuilder<?>> m_comment;
  private final List<BiConsumer<TYPE, IJavaBuilderContext>> m_preProcessors;

  protected AbstractJavaElementGenerator() {
    m_preProcessors = new ArrayList<>();
  }

  protected AbstractJavaElementGenerator(IJavaElement element) {
    this();
    withElementName(Ensure.notNull(element).elementName());
  }

  @SuppressWarnings("unchecked")
  protected TYPE thisInstance() {
    return (TYPE) this;
  }

  @Override
  public Optional<String> elementName() {
    return elementNameFunc().flatMap(JavaBuilderContextFunction::apply);
  }

  @Override
  public Optional<String> elementName(IJavaBuilderContext context) {
    return elementNameFunc().map(f -> f.apply(context));
  }

  @Override
  public Optional<JavaBuilderContextFunction<String>> elementNameFunc() {
    return Optional.ofNullable(m_elementName);
  }

  @Override
  public TYPE withElementName(String newName) {
    if (Strings.isEmpty(newName)) {
      m_elementName = null;
    }
    else {
      m_elementName = JavaBuilderContextFunction.create(newName);
    }
    return thisInstance();
  }

  @Override
  public <A extends IApiSpecification> TYPE withElementNameFrom(Class<A> apiDefinition, Function<A, String> nameSupplier) {
    m_elementName = new ApiFunction<>(apiDefinition, nameSupplier);
    return thisInstance();
  }

  @Override
  public TYPE withElementNameFunc(Function<IJavaBuilderContext, String> nameSupplier) {
    m_elementName = JavaBuilderContextFunction.orNull(nameSupplier);
    return thisInstance();
  }

  @SuppressWarnings("unchecked")
  protected void build(IJavaSourceBuilder<?> builder) {
    var context = builder.context();
    preProcessors().forEach(c -> c.accept((TYPE) this, context));
    createComment(builder);
  }

  @Override
  public Stream<BiConsumer<TYPE, IJavaBuilderContext>> preProcessors() {
    return m_preProcessors.stream();
  }

  @Override
  public TYPE withPreProcessor(BiConsumer<TYPE, IJavaBuilderContext> processor) {
    if (processor != null) {
      m_preProcessors.add(processor);
    }
    return thisInstance();
  }

  protected void createComment(ISourceBuilder<?> builder) {
    comment()
        .map(b -> b.generalize(this::createCommentBuilder))
        .ifPresent(builder::append);
  }

  protected IJavaElementCommentBuilder<?> createCommentBuilder(ISourceBuilder<?> builder) {
    return JavaElementCommentBuilder.create(builder);
  }

  public static IJavaSourceBuilder<?> ensureJavaSourceBuilder(ISourceBuilder<?> inner) {
    if (inner instanceof IJavaSourceBuilder<?>) {
      return (IJavaSourceBuilder<?>) inner;
    }
    return JavaSourceBuilder.create(inner);
  }

  /**
   * If the given name is a reserved java keyword a suffix is added to ensure it is a valid name to use e.g. for
   * variables or parameters.
   *
   * @param parameterName
   *          The original name.
   * @return The new value which probably has a suffix appended.
   */
  public static String ensureValidJavaName(String parameterName) {
    if (JavaTypes.isReservedJavaKeyword(parameterName)) {
      return parameterName + '_';
    }
    return parameterName;
  }

  @Override
  public final void generate(ISourceBuilder<?> builder) {
    build(ensureJavaSourceBuilder(builder));
  }

  @Override
  public TYPE withComment(ISourceGenerator<IJavaElementCommentBuilder<?>> commentBuilder) {
    m_comment = commentBuilder;
    return thisInstance();
  }

  @Override
  public Optional<ISourceGenerator<IJavaElementCommentBuilder<?>>> comment() {
    return Optional.ofNullable(m_comment);
  }
}
