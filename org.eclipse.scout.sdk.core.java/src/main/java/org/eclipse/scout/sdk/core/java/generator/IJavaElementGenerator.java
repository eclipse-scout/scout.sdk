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

import static java.util.function.Function.identity;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.builder.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;

/**
 * <h3>{@link IJavaElementGenerator}</h3>
 * <p>
 * An {@link ISourceGenerator} that creates source for Java elements.
 *
 * @since 6.1.0
 */
public interface IJavaElementGenerator<TYPE extends IJavaElementGenerator<TYPE>> extends ISourceGenerator<ISourceBuilder<?>> {

  /**
   * @return The name of the element if it is context independent. Otherwise, an empty {@link Optional} is returned.
   */
  Optional<String> elementName();

  /**
   * @param context
   *          To compute context dependent names.
   * @return The name of the element or an empty {@link Optional} if the element has no name yet.
   */
  Optional<String> elementName(IJavaBuilderContext context);

  /**
   * @return The {@link JavaBuilderContextFunction} that of the element name.
   */
  Optional<JavaBuilderContextFunction<String>> elementNameFunc();

  /**
   * Sets the name of this {@link IJavaElementGenerator}.
   *
   * @param newName
   *          The new name or {@code null}.
   * @return This generator
   * @see #withElementNameFrom(Class, Function)
   * @see #withElementNameFunc(Function)
   */
  TYPE withElementName(String newName);

  /**
   * Sets the element name to the result of the given nameSupplier.
   * <p>
   * This method may be handy if the name changes between different versions of an API. The generator then decides which
   * API to use based on the version found in the {@link IJavaEnvironment} of the {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code generator.withElementNameFrom(IJavaApi.class, api -> api.Long().valueOfMethodName())}.
   *
   * @param apiDefinition
   *          The api type that defines the element name. An instance of this API is passed to the nameSupplier. May be
   *          {@code null} in case the given nameSupplier can handle a {@code null} input.
   * @param nameSupplier
   *          A {@link Function} to be called to obtain the element name of this {@link IJavaElementGenerator}. Must not
   *          be {@code null}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withElementName(String)
   * @see #withElementNameFunc(Function)
   */
  <A extends IApiSpecification> TYPE withElementNameFrom(Class<A> apiDefinition, Function<A, String> nameSupplier);

  /**
   * Sets the element name to the result of the given nameSupplier.
   * <p>
   * This method may be handy if the name is context dependent.
   * </p>
   * 
   * @param nameSupplier
   *          A {@link Function} to be called to obtain the name of this {@link IJavaElementGenerator} or {@code null}.
   * @return This generator.
   * @see #withElementName(String)
   * @see #withElementNameFrom(Class, Function)
   */
  TYPE withElementNameFunc(Function<IJavaBuilderContext, String> nameSupplier);

  /**
   * Sets the {@link ISourceGenerator} providing the javadoc comment for this {@link IJavaElementGenerator}.
   *
   * @param commentGenerator
   *          The generator for the comment or {@code null} if no comment should be generated.
   * @return This generator.
   */
  TYPE withComment(ISourceGenerator<IJavaElementCommentBuilder<?>> commentGenerator);

  /**
   * @return The generator of the javadoc comment of this {@link IJavaElementGenerator}.
   */
  Optional<ISourceGenerator<IJavaElementCommentBuilder<?>>> comment();

  /**
   * Appends the given pre-processor to this generator. Pre-processors are executed before each generator execution and
   * allow to apply any modifications at generation time. This might be useful if the setup of the generator is
   * {@link IJavaBuilderContext context} dependent.<br>
   * <br>
   * <p>
   * <b>Note:</b> pre-processors are executed each time the generator is executed. This is necessary to apply context
   * dependent modifications. On the other hand it brings the risk that changes applied in former executions are still
   * in the generator. Pre-processors must somehow deal with this situation. One solution could be to implement it in an
   * idempotent way. Another might be to apply some cleanup/rollback before creating new modifications.
   * </p>
   * 
   * @param processor
   *          The pre-processor to add. This method does nothing if it is {@code null}.
   * @return This generator.
   */
  TYPE withPreProcessor(BiConsumer<TYPE, IJavaBuilderContext> processor);

  /**
   * @return A {@link Stream} returning all pre-processors registered.
   */
  Stream<BiConsumer<TYPE, IJavaBuilderContext>> preProcessors();

  /**
   * Executes this {@link IJavaElementGenerator} and creates its source in memory.
   * <p>
   * When using this method no {@link IJavaEnvironment} will be used to resolve imports.
   *
   * @return A {@link StringBuilder} holding the content of the created source.
   * @see #toJavaSource(IJavaEnvironment)
   */
  default StringBuilder toJavaSource() {
    return toJavaSource((IJavaEnvironment) null);
  }

  /**
   * Executes this {@link IJavaElementGenerator} and creates its source in memory.
   *
   * @param context
   *          The {@link IJavaEnvironment} in which context the source is created. It will be used to resolve imports.
   *          May be {@code null}.
   * @return A {@link StringBuilder} holding the content of the created source.
   * @see #toJavaSource(IJavaBuilderContext)
   */
  default StringBuilder toJavaSource(IJavaEnvironment context) {
    return toJavaSource(new JavaBuilderContext(context));
  }

  /**
   * Executes this {@link IJavaElementGenerator} and creates its source in memory.
   *
   * @param context
   *          The {@link IJavaBuilderContext} in which the source is created. Must not be {@code null}.
   * @return A {@link StringBuilder} holding the content of the created source.
   * @see #toJavaSource(IJavaEnvironment)
   */
  default StringBuilder toJavaSource(IJavaBuilderContext context) {
    return toSource(identity(), context);
  }
}
