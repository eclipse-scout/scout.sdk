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
package org.eclipse.scout.sdk.core.fixture.apidef;

import static org.eclipse.scout.sdk.core.apidef.ApiVersion.requireMaxApiLevelOf;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.sdk.core.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.apidef.IApiProvider;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

public class JavaApiProvider implements IApiProvider {
  @Override
  public Collection<Class<? extends IApiSpecification>> knownApis() {
    return Arrays.asList(Java8Api.class, Java11Api.class, Java13Api.class);
  }

  @Override
  public Optional<ApiVersion> version(IJavaEnvironment context) {
    // for testing: always return version 11
    return Optional.of(requireMaxApiLevelOf(Java11Api.class));
  }
}
