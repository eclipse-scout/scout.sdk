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
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContextFunction;
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
@SuppressWarnings("squid:S2160") // Subclasses that add fields should override "equals"
public class ApiFunction<API extends IApiSpecification, RET> extends JavaBuilderContextFunction<RET> {

  private final Class<API> m_apiClass;
  private final Function<API, RET> m_apiFunction;

  public ApiFunction(RET constantValue) {
    this(null, api -> constantValue);
  }

  public ApiFunction(Class<API> apiClass, Function<API, RET> function) {
    super(c -> applyApi(apiClass, function, c), apiClass != null);
    m_apiClass = apiClass; // may be null
    m_apiFunction = Ensure.notNull(function);
  }

  protected static <API extends IApiSpecification, RET> RET applyApi(Class<API> apiDefinition, Function<API, RET> task, IJavaBuilderContext context) {
    if (apiDefinition == null) {
      return task.apply(null);
    }
    return applyApi(apiDefinition, task, context.environment().orElse(null));
  }

  protected static <API extends IApiSpecification, RET> RET applyApi(Class<API> apiDefinition, Function<API, RET> task, IJavaEnvironment context) {
    if (apiDefinition == null) {
      return task.apply(null);
    }
    return Optional.ofNullable(context)
        .flatMap(c -> c.api(apiDefinition))
        .map(task)
        .orElse(null);
  }

  public Optional<RET> apply(IJavaEnvironment context) {
    return Optional.ofNullable(applyApi(m_apiClass, m_apiFunction, context));
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
