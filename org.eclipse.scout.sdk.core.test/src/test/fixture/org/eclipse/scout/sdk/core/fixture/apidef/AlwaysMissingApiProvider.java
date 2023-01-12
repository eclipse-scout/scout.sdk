/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.fixture.apidef;

import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.sdk.core.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.apidef.IApiProvider;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

public class AlwaysMissingApiProvider implements IApiProvider {

  @Override
  public Collection<Class<? extends IApiSpecification>> knownApis() {
    return singletonList(IAlwaysMissingApi.class);
  }

  @Override
  public Optional<ApiVersion> version(IJavaEnvironment context) {
    return Optional.empty(); // is always missing
  }
}