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

import static org.eclipse.scout.sdk.core.util.apidef.ApiVersion.requireApiLevelOf;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.util.apidef.IApiProvider;
import org.eclipse.scout.sdk.core.util.apidef.IApiSpecification;

public class JavaApiProvider implements IApiProvider {
  @Override
  public Collection<Class<? extends IApiSpecification>> knownApis() {
    return Arrays.asList(JavaApi8.class, JavaApi11.class, JavaApi13.class);
  }

  @Override
  public Optional<ApiVersion> version(IJavaEnvironment context) {
    // for testing: always return version 11
    return Optional.of(requireApiLevelOf(JavaApi11.class));
  }
}
