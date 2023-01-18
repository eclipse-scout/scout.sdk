/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.fixture.apidef;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.sdk.core.java.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.java.apidef.IApiProvider;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;

public class JavaApiProvider implements IApiProvider {
  @Override
  public Collection<Class<? extends IApiSpecification>> knownApis() {
    return Arrays.asList(Java8Api.class, Java11Api.class, Java13Api.class);
  }

  @Override
  public Optional<ApiVersion> version(IJavaEnvironment context) {
    // for testing: always return version 11
    return Optional.of(ApiVersion.requireMaxApiLevelOf(Java11Api.class));
  }
}
