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
package org.eclipse.scout.sdk.core.s.apidef;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.util.maven.MavenModuleVersion;
import org.eclipse.scout.sdk.core.util.apidef.Api;
import org.eclipse.scout.sdk.core.util.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.util.apidef.IApiProvider;
import org.eclipse.scout.sdk.core.util.apidef.IApiSpecification;

public final class ScoutApi {

  public static final String SCOUT_RT_PLATFORM_NAME = "org.eclipse.scout.rt.platform";

  private ScoutApi() {
  }

  public static IScoutApi latest() {
    return Api.latest(IScoutApi.class);
  }

  public static int latestMajorVersion() {
    return Api.latestMajorVersion(IScoutApi.class);
  }

  public static Optional<IScoutApi> create(IJavaElement context) {
    return Api.create(IScoutApi.class, context);
  }

  public static Optional<IScoutApi> create(IJavaEnvironment context) {
    return Api.create(IScoutApi.class, context);
  }

  public static Optional<ApiVersion> version(IJavaEnvironment context) {
    return Api.version(IScoutApi.class, context);
  }

  public static Optional<ApiVersion> version(Path moduleDir, IEnvironment env) {
    return env.findJavaEnvironment(moduleDir).flatMap(je -> Api.version(IScoutApi.class, je));
  }

  public static Stream<IScoutApi> allKnown() {
    return Api.allKnown(IScoutApi.class);
  }

  static boolean register() {
    Api.registerProvider(IScoutApi.class, new ScoutApiProvider());
    return true;
  }

  public static class ScoutApiProvider implements IApiProvider {

    @Override
    public Collection<Class<? extends IApiSpecification>> knownApis() {
      return Arrays.asList(Scout10Api.class, Scout11Api.class);
    }

    @Override
    public Optional<ApiVersion> version(IJavaEnvironment context) {
      return MavenModuleVersion.get(SCOUT_RT_PLATFORM_NAME, context);
    }
  }
}
