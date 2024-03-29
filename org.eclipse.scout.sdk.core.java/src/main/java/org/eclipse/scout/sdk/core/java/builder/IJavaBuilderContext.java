/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.builder;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.IBuilderContext;
import org.eclipse.scout.sdk.core.java.apidef.Api;
import org.eclipse.scout.sdk.core.java.apidef.IApiProvider;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.imports.IImportCollector;
import org.eclipse.scout.sdk.core.java.imports.IImportValidator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;

/**
 * <h3>{@link IJavaBuilderContext}</h3>
 *
 * @since 6.1.0
 */
public interface IJavaBuilderContext extends IBuilderContext {

  /**
   * @return The {@link IJavaEnvironment} of this {@link IJavaBuilderContext}.
   */
  Optional<IJavaEnvironment> environment();

  /**
   * @return The {@link IImportValidator} used to resolve imports. The {@link IImportCollector#getJavaEnvironment()} of
   *         the returned {@link IImportValidator} is the same as {@link #environment()}.
   */
  IImportValidator validator();

  /**
   * Tries to find the given API in the {@link #environment()} of this context.
   * 
   * @param apiDefinition
   *          The API definition class to find. If {@code null}, the resulting {@link Optional} will be empty.
   * @param <A>
   *          The API type
   * @return An {@link Optional} holding the API. The optional is empty if this context has no {@link #environment()},
   *         the given class is {@code null} or the API could not be found in the {@link IJavaEnvironment}.
   * @throws IllegalArgumentException
   *           if one of the following conditions is true:
   *           <ol>
   *           <li>the API version found in the {@link #environment()} is not supported (version in the
   *           {@link #environment()} is too old).</li>
   *           <li>the given API class is not registered (see {@link Api#registerProvider(Class, IApiProvider)}</li>
   *           </ol>
   * @see #requireApi(Class)
   */
  <A extends IApiSpecification> Optional<A> api(Class<A> apiDefinition);

  /**
   * Gets the given API from the {@link #environment()} of this context.
   * 
   * @param apiDefinition
   *          The API definition class to find. Must not be {@code null}.
   * @param <A>
   *          The API type
   * @return The API instance
   * @throws IllegalArgumentException
   *           if one of the following conditions is true:
   *           <ol>
   *           <li>the given API class is {@code null}</li>
   *           <li>this context has no {@link #environment()}</li>
   *           <li>the given API cannot be found in the {@link #environment()}</li>
   *           <li>the API version found in the {@link #environment()} is not supported (version in the
   *           {@link #environment()} is too old).</li>
   *           <li>the given API class is not registered (see {@link Api#registerProvider(Class, IApiProvider)}</li>
   *           </ol>
   * @see #api(Class)
   */
  <A extends IApiSpecification> A requireApi(Class<A> apiDefinition);
}
