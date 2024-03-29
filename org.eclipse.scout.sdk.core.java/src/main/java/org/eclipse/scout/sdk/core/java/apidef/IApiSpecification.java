/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.apidef;

import java.util.Optional;

/**
 * Base interface for a Java API specification.
 * <p>
 * Typically an API specification consists of a sub interface to {@link IApiSpecification} defining the API
 * elements.<br>
 * Specific versions of such an API specification are sub interfaces to that specification holding an
 * {@link MaxApiLevel} annotation indicating the latest (newest) API version for which it may be used. On these level
 * the elements are implemented using default methods.
 * </p>
 * <p>
 * <b>Example</b> (all classes below are interfaces):
 * 
 * <pre>
 *          IApiSpecification
 *                   |
 *              IMyLibraryApi
 *               |         |
 *   &#64;MaxApiLevel(2)      @MaxApiLevel(4) 
 *     MyLibrary1Api       MyLibrary2Api
 * </pre>
 * </p>
 */
public interface IApiSpecification {

  /**
   * Gets the maximum API level version this specification supports.<br>
   * Typically, this is provided by the {@link MaxApiLevel} annotation on a specific API interface class.<br>
   * If an API is annotated with "{@code @MaxApiLevel(14)}", this API can be used for all runtime versions <= 14.x.x
   * 
   * @return The maximum API level version. Never returns {@code null}.
   */
  ApiVersion maxLevel();

  /**
   * Tries to find the given API within this {@link IApiSpecification}. This is only successful if this API is
   * compatible with the given one.
   * 
   * @param apiDefinition
   *          The API to find
   * @param <A>
   *          The new API type
   * @return The API or an empty {@link Optional} if this API level is not compatible with the given one.
   */
  <A extends IApiSpecification> Optional<A> api(Class<A> apiDefinition);

  /**
   * Gets the given API within this one. This is only successful if this API is compatible with the given one.
   * 
   * @param apiDefinition
   *          The API to find. Must not be {@code null}.
   * @param <A>
   *          The new API type
   * @return The requested API. Never returns {@code null}.
   * @throws IllegalArgumentException
   *           if this API is not compatible with the given one or the given class is {@code null}.
   */
  <A extends IApiSpecification> A requireApi(Class<A> apiDefinition);
}
