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