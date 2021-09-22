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
package org.eclipse.scout.sdk.core.apidef;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * Represents a {@link Function} that takes an {@link IApiSpecification} as input. It contains some helper methods to
 * execute the function in a specific context.
 * 
 * @param <API>
 *          The API type.
 * @param <RET>
 *          The return type of the function.
 */
public class ApiFunction<API extends IApiSpecification, RET> implements Function<API, RET> {

  private final Class<API> m_apiClass;
  private final Function<API, RET> m_apiFunction;

  public ApiFunction(RET constantValue) {
    this(null, api -> constantValue);
  }

  public ApiFunction(Class<API> apiClass, Function<API, RET> function) {
    m_apiClass = apiClass; // may be null
    m_apiFunction = Ensure.notNull(function);
  }

  /**
   * Executes the {@link ApiFunction} without context information. This method call is only successful if this
   * {@link ApiFunction} does not require an API.
   * 
   * @return The function result.
   */
  public Optional<RET> apply() {
    return apply((IJavaEnvironment) null);
  }

  /**
   * Executes the {@link ApiFunction} with the given API. The given API must be compatible with the {@link #apiClass()}
   * of this function or this {@link ApiFunction} does not require an API.
   * 
   * @param context
   *          The API wich should be converted to the {@link #apiClass()} or {@code null}.
   * @return The function result.
   */
  @Override
  public RET apply(IApiSpecification context) {
    var api = Optional.ofNullable(context)
        .flatMap(input -> input.api(apiClass().orElse(null)))
        .orElse(null);
    return apiFunction().apply(api);
  }

  /**
   * Executes this {@link ApiFunction} with given {@link IJavaSourceBuilder} as context.
   * <p>
   * It retrieves an API instance of type {@link #apiClass()} from the given {@link IJavaSourceBuilder#context()} (if
   * necessary) and calls the {@link #apiFunction()} with that API instance.
   * </p>
   *
   * @param builder
   *          The {@link IJavaSourceBuilder} to get the API from or {@code null} if this {@link ApiFunction} does not
   *          require an API.
   * @return The result of the {@link #apiFunction()} or an empty {@link Optional} in the following cases:
   *         <ol>
   *         <li>The result of the {@link #apiFunction()} was {@code null}</li>
   *         <li>An API of type {@link #apiClass()} could not be found in the {@link IJavaEnvironment} of
   *         {@link IJavaSourceBuilder#context()}. In that case the {@link #apiFunction()} is not executed!</li>
   *         </ol>
   * @throws IllegalArgumentException
   *           if the {@link #apiClass()} is not {@code null} but the {@link IJavaEnvironment} of the given
   *           {@link IJavaSourceBuilder#context()} is {@code null}
   * @see #applyWithApi(Class, Function, IJavaEnvironment)
   */
  public Optional<RET> apply(IJavaSourceBuilder<?> builder) {
    var context = Optional.ofNullable(builder).map(IJavaSourceBuilder::context).orElse(null);
    return apply(context);
  }

  /**
   * Executes this {@link ApiFunction} with given {@link IJavaBuilderContext} as context.
   * <p>
   * It retrieves an API instance of type {@link #apiClass()} from the {@link IJavaBuilderContext#environment()} (if
   * necessary) and calls the {@link #apiFunction()} with that API instance.
   * </p>
   *
   * @param context
   *          The {@link IJavaBuilderContext} to get the API from or {@code null} if this {@link ApiFunction} does not
   *          require an API.
   * @return The result of the {@link #apiFunction()} or an empty {@link Optional} in the following cases:
   *         <ol>
   *         <li>The result of the {@link #apiFunction()} was {@code null}</li>
   *         <li>An API of type {@link #apiClass()} could not be found in the {@link IJavaBuilderContext#environment()}.
   *         In that case the {@link #apiFunction()} is not executed!</li>
   *         </ol>
   * @throws IllegalArgumentException
   *           if the {@link #apiClass()} is not {@code null} but the given {@link IJavaBuilderContext#environment()} is
   *           empty.
   * @see #applyWithApi(Class, Function, IJavaEnvironment)
   */
  public Optional<RET> apply(IJavaBuilderContext context) {
    var env = Optional.ofNullable(context)
        .flatMap(IJavaBuilderContext::environment)
        .orElse(null);
    return apply(env);
  }

  /**
   * Executes this {@link ApiFunction} with given {@link IJavaEnvironment} as context.
   * <p>
   * It retrieves an API instance of type {@link #apiClass()} from the given {@link IJavaEnvironment} (if necessary) and
   * calls the {@link #apiFunction()} with that API instance.
   * </p>
   * 
   * @param context
   *          The {@link IJavaEnvironment} to get the API from or {@code null} if this {@link ApiFunction} does not
   *          require an API.
   * @return The result of the {@link #apiFunction()} or an empty {@link Optional} in the following cases:
   *         <ol>
   *         <li>The result of the {@link #apiFunction()} was {@code null}</li>
   *         <li>An API of type {@link #apiClass()} could not be found in the {@link IJavaEnvironment}. In that case the
   *         {@link #apiFunction()} is not executed!</li>
   *         </ol>
   * @throws IllegalArgumentException
   *           if the {@link #apiClass()} is not {@code null} but the given {@link IJavaEnvironment} is {@code null}
   * @see #applyWithApi(Class, Function, IJavaEnvironment)
   */
  public Optional<RET> apply(IJavaEnvironment context) {
    return applyWithApi(apiClass().orElse(null), apiFunction(), context);
  }

  /**
   * Executes the given task and passes an API of the given type to the function. The API is retrieved from the given
   * {@link IJavaEnvironment} (if necessary).
   * <p>
   * <b>Note</b>: If the task requires an API (API type is not {@code null}) but the API cannot be found in the
   * {@link IJavaEnvironment}, the task is not executed!
   * </p>
   * 
   * @param apiDefinition
   *          The API type required by the task. May be {@code null} in case the task can be executed with {@code null}
   *          as input.
   * @param task
   *          The {@link Function} call. Must not be {@code null}.
   * @param context
   *          The {@link IJavaEnvironment} context to obtain the API of the given type (if available). See
   *          {@link IJavaEnvironment#api(Class)}. Must not be {@code null} in case the apiDefinition is not
   *          {@code null}.
   * @param <A>
   *          The API type.
   * @param <R>
   *          The task return type.
   * @return The result of the function or an empty {@link Optional} in the following cases:
   *         <ol>
   *         <li>The result of the task was {@code null}</li>
   *         <li>The apiDefinition is given but could not be found in the {@link IJavaEnvironment}. In that case the
   *         task is not executed!</li>
   *         </ol>
   * @throws IllegalArgumentException
   *           if one of the following cases is true:
   *           <ol>
   *           <li>The task given is {@code null}</li>
   *           <li>The apiDefinition is not {@code null} but the {@link IJavaEnvironment} is {@code null}</li>
   *           </ol>
   */
  public static <A extends IApiSpecification, R> Optional<R> applyWithApi(Class<A> apiDefinition, Function<A, R> task, IJavaEnvironment context) {
    Ensure.notNull(task);
    if (apiDefinition == null) {
      return Optional.ofNullable(task.apply(null));
    }
    return Ensure.notNull(context, "Cannot apply {} without a context because API ({}) is passed. If an API is given, the context is mandatory.", ApiFunction.class.getSimpleName(), apiDefinition)
        .api(apiDefinition) // API may be absent. Then the task is not executed. This is required e.g. when filtering on an annotation (react e.g. on existence) but it is not present on the classpath).
        .map(task);
  }

  /**
   * @return The type of API this function requires.
   */
  public Optional<Class<API>> apiClass() {
    return Optional.ofNullable(m_apiClass);
  }

  /**
   * @return The {@link Function} to execute. As input to the function an instance of type {@link #apiClass()} is
   *         passed.
   */
  public Function<API, RET> apiFunction() {
    return m_apiFunction;
  }
}
