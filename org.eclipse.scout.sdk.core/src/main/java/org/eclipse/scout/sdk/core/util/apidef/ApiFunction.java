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
package org.eclipse.scout.sdk.core.util.apidef;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.Ensure;

public class ApiFunction<API extends IApiSpecification, RET> implements Function<API, RET> {

  private final Class<API> m_apiClass;
  private final Function<API, RET> m_apiFunction;

  public ApiFunction(RET constantValue) {
    this(api -> constantValue);
  }

  public ApiFunction(Function<API, RET> function) {
    this(null, function);
  }

  public ApiFunction(Class<API> apiClass, Function<API, RET> function) {
    m_apiClass = apiClass; // may be null
    m_apiFunction = Ensure.notNull(function);
  }

  public Optional<RET> apply() {
    return apply((IJavaEnvironment) null);
  }

  @Override
  public RET apply(IApiSpecification context) {
    API api = Optional.ofNullable(context)
        .flatMap(input -> input.optApi(apiClass().orElse(null)))
        .orElse(null);
    return apiFunction().apply(api);
  }

  public Optional<RET> apply(IJavaSourceBuilder<?> builder) {
    IJavaBuilderContext context = Optional.ofNullable(builder).map(IJavaSourceBuilder::context).orElse(null);
    return apply(context);
  }

  public Optional<RET> apply(IJavaBuilderContext context) {
    IJavaEnvironment env = Optional.ofNullable(context)
        .flatMap(IJavaBuilderContext::environment)
        .orElse(null);
    return apply(env);
  }

  public Optional<RET> apply(IJavaEnvironment context) {
    return applyWithApi(apiClass().orElse(null), apiFunction(), context);
  }

  public static <A extends IApiSpecification, R> Optional<R> applyWithApi(Class<A> apiDefinition, Function<A, R> task, IJavaEnvironment context) {
    Ensure.notNull(task);
    if (apiDefinition == null) {
      return Optional.ofNullable(task.apply(null));
    }
    return Ensure.notNull(context, "Cannot apply {} without context because an API ({}) is present. If an API is specified the context is required to compute the API version.", ApiFunction.class.getSimpleName(), apiDefinition)
        .api(apiDefinition) // API may be absent. Then the task is not executed. This is required e.g. when filtering on an annotation (react e.g. on existence) but it is not present on the classpath).
        .map(task);
  }

  public Optional<Class<API>> apiClass() {
    return Optional.ofNullable(m_apiClass);
  }

  public Function<API, RET> apiFunction() {
    return m_apiFunction;
  }
}
