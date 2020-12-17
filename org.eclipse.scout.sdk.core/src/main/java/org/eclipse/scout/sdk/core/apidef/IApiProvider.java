/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.apidef;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

/**
 * An {@link IApiProvider} lists all supported API specification versions and detects the version found in a
 * {@link IJavaEnvironment}<br>
 * An {@link IApiProvider} must be registered using {@link Api#registerProvider(Class, IApiProvider)}.
 */
public interface IApiProvider {

  /**
   * @return All API specification versions supported. Must not be {@code null}.
   */
  Collection<Class<? extends IApiSpecification>> knownApis();

  /**
   * Gets the {@link ApiVersion} of the API in the given {@link IJavaEnvironment}.
   * 
   * @param context
   *          The {@link IJavaEnvironment} in which the version of the API should be computed. Must not be {@code null}.
   * @return An {@link Optional} holding the API version within the {@link IJavaEnvironment} or an empty
   *         {@link Optional} if the API could not be found.
   */
  Optional<ApiVersion> version(IJavaEnvironment context);
}
