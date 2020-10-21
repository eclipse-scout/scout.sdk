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
package org.eclipse.scout.sdk.core.apidef;

import java.util.Optional;

/**
 * Base interface for a Java API specification.
 * <p>
 * Typically an API specification consists of a sub interface to {@link IApiSpecification} defining the API
 * elements.<br>
 * Specific versions of such an API specification are sub interfaces to that specification holding an {@link ApiLevel}
 * annotation indicating the API version of it. On these level the elements are implemented using default methods.
 * </p>
 * <p>
 * <b>Example</b> (all classes below are interfaces):
 * 
 * <pre>
 *          IApiSpecification
 *                   |
 *              IMyLibraryApi
 *               |         |
 *     &#64;ApiLevel(1)        @ApiLevel(2) 
 *     MyLibrary1Api       MyLibrary2Api
 * </pre>
 * </p>
 */
public interface IApiSpecification {

  /**
   * Gets the API level version.<br>
   * Typically this is provided by the {@link ApiLevel} annotation on a specific API interface class.
   * 
   * @return The API level version. Never returns {@code null}.
   */
  ApiVersion level();

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
