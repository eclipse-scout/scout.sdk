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
package org.eclipse.scout.sdk.core.builder.java;

import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.SourceBuilderWrapper;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link JavaSourceBuilder}</h3>
 *
 * @since 6.1.0
 */
public class JavaSourceBuilder extends SourceBuilderWrapper<JavaSourceBuilder> implements IJavaSourceBuilder<JavaSourceBuilder> {

  private final IJavaBuilderContext m_context;

  protected JavaSourceBuilder(ISourceBuilder<?> inner, IJavaEnvironment env) {
    super(inner);
    var context = inner.context();
    if (context instanceof IJavaBuilderContext) {
      m_context = (IJavaBuilderContext) context;
    }
    else {
      m_context = new JavaBuilderContext(context, env);
    }
  }

  /**
   * Creates a new {@link IJavaSourceBuilder} wrapping the given inner {@link ISourceBuilder}.
   * <p>
   * If the context of the inner {@link ISourceBuilder} is an {@link IJavaBuilderContext}, this context and its
   * {@link IJavaEnvironment} is re-used. In that case the specified {@link IJavaEnvironment} is ignored!<br>
   * Otherwise a new {@link IJavaBuilderContext} with the specified {@link IJavaEnvironment} is created.
   *
   * @param inner
   *          The inner {@link ISourceBuilder}. Must not be {@code null}.
   * @param env
   *          The {@link IJavaEnvironment} that should be used to resolve imports or {@code null}.
   * @return A new {@link IJavaSourceBuilder} instance.
   */
  public static IJavaSourceBuilder<?> create(ISourceBuilder<?> inner, IJavaEnvironment env) {
    return new JavaSourceBuilder(inner, env);
  }

  /**
   * Creates a new {@link IJavaSourceBuilder} wrapping the given inner {@link ISourceBuilder}.
   * <p>
   * If the context of the inner {@link ISourceBuilder} is an {@link IJavaBuilderContext}, this context and its
   * {@link IJavaEnvironment} is re-used.<br>
   * Otherwise a new {@link IJavaBuilderContext} without an {@link IJavaEnvironment} is created!
   *
   * @param inner
   *          The inner {@link ISourceBuilder}. Must not be {@code null}.
   * @return A new {@link IJavaSourceBuilder} instance.
   */
  public static IJavaSourceBuilder<?> create(ISourceBuilder<?> inner) {
    return create(inner, null);
  }

  @Override
  public IJavaBuilderContext context() {
    return m_context;
  }

  @Override
  public JavaSourceBuilder ref(IType t) {
    return ref(t.reference());
  }

  @Override
  public JavaSourceBuilder ref(CharSequence ref) {
    return append(context().validator().useReference(ref));
  }

  @Override
  public JavaSourceBuilder blockStart() {
    return append('{');
  }

  @Override
  public JavaSourceBuilder blockEnd() {
    return append('}');
  }

  @Override
  public JavaSourceBuilder at() {
    return append('@');
  }

  @Override
  public JavaSourceBuilder parenthesisOpen() {
    return append('(');
  }

  @Override
  public JavaSourceBuilder parenthesisClose() {
    return append(')');
  }

  @Override
  public JavaSourceBuilder equalSign() {
    return append(" = ");
  }

  @Override
  public JavaSourceBuilder dot() {
    return append(JavaTypes.C_DOT);
  }

  @Override
  public JavaSourceBuilder comma() {
    return append(JavaTypes.C_COMMA);
  }

  @Override
  public JavaSourceBuilder semicolon() {
    return append(';');
  }

  @Override
  public JavaSourceBuilder references(Stream<? extends CharSequence> references, CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    if (references == null) {
      return thisInstance();
    }

    var referenceBuilders = references
        .<ISourceGenerator<IJavaSourceBuilder<?>>> map(s -> builder -> builder.ref(s))
        .map(builder -> builder.generalize(JavaSourceBuilder::create));
    return append(referenceBuilders, prefix, delimiter, suffix);
  }

  @Override
  public <API extends IApiSpecification> JavaSourceBuilder refClassFrom(Class<API> apiClass, Function<API, ITypeNameSupplier> sourceProvider) {
    return refClassFunc(new ApiFunction<>(apiClass, sourceProvider));
  }

  @Override
  public JavaSourceBuilder refClassFunc(Function<IJavaBuilderContext, ITypeNameSupplier> func) {
    var cns = Ensure.notNull(func.apply(context()), "{} function '{}' did not return a valid value.", IJavaBuilderContext.class.getSimpleName(), func);
    var fqn = Ensure.notBlank(cns.fqn(), "{} function '{}' did not return a valid value.", IJavaBuilderContext.class.getSimpleName(), func);
    return ref(fqn);
  }

  @Override
  public <API extends IApiSpecification> JavaSourceBuilder refFrom(Class<API> apiClass, Function<API, ? extends CharSequence> refProvider) {
    return refFunc(new ApiFunction<>(apiClass, refProvider));
  }

  @Override
  public JavaSourceBuilder refFunc(Function<IJavaBuilderContext, ? extends CharSequence> func) {
    var ref = Ensure.notBlank(func.apply(context()), "{} function '{}' did not return a valid value.", IJavaBuilderContext.class.getSimpleName(), func);
    return ref(ref);
  }

  @Override
  public <API extends IApiSpecification> JavaSourceBuilder appendFrom(Class<API> apiClass, Function<API, ? extends CharSequence> sourceProvider) {
    return appendFunc(new ApiFunction<>(apiClass, sourceProvider));
  }

  @Override
  public JavaSourceBuilder appendFunc(Function<IJavaBuilderContext, ? extends CharSequence> sourceProvider) {
    var src = Ensure.notNull(sourceProvider.apply(context()), "{} function '{}' did not return a valid value.", IJavaBuilderContext.class.getSimpleName(), sourceProvider);
    return append(src);
  }

  @Override
  public JavaSourceBuilder referencesFrom(Stream<Function<IJavaBuilderContext, ? extends CharSequence>> references, CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    if (references == null) {
      return thisInstance();
    }
    return references(references
        .map(af -> af.apply(context()))
        .filter(Strings::hasText), prefix, delimiter, suffix);
  }
}
