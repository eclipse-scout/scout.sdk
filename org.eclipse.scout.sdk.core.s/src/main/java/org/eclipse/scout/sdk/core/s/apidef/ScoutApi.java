/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.scout.sdk.core.apidef.Api;
import org.eclipse.scout.sdk.core.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.apidef.IApiProvider;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.util.maven.MavenArtifactVersions;

public final class ScoutApi {

  public static final String SCOUT_RT_PLATFORM_NAME = "org.eclipse.scout.rt.platform";

  private ScoutApi() {
  }

  /**
   * @see Api#latest(Class)
   */
  public static IScoutApi latest() {
    return Api.latest(IScoutApi.class);
  }

  /**
   * @see Api#latestMajorVersion(Class)
   */
  public static int latestMajorVersion() {
    return Api.latestMajorVersion(IScoutApi.class);
  }

  /**
   * @see Api#create(Class, IJavaElement)
   */
  public static Optional<IScoutApi> create(IJavaElement context) {
    return Api.create(IScoutApi.class, context);
  }

  /**
   * @see Api#create(Class, IJavaEnvironment)
   */
  public static Optional<IScoutApi> create(IJavaEnvironment context) {
    return Api.create(IScoutApi.class, context);
  }

  /**
   * @see Api#version(Class, IJavaEnvironment)
   */
  public static Optional<ApiVersion> version(IJavaEnvironment context) {
    return Api.version(IScoutApi.class, context);
  }

  /**
   * Gets the Scout version used in the module at the given {@link Path}.
   * 
   * @param moduleDir
   *          The root {@link Path} of the module. Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} to find the module with the given path. Must not be {@code null}.
   * @return The Scout version or an empty {@link Optional} if the module could not be found or it is no Scout module.
   */
  public static Optional<ApiVersion> version(Path moduleDir, IEnvironment env) {
    return env.findJavaEnvironment(moduleDir).flatMap(je -> Api.version(IScoutApi.class, je));
  }

  /**
   * @see Api#allKnown(Class)
   */
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
      return Arrays.asList(Scout10Api.class, Scout11Api.class, Scout22010Api.class, Scout22Api.class);
    }

    @Override
    public Optional<ApiVersion> version(IJavaEnvironment context) {
      return MavenArtifactVersions.usedIn(SCOUT_RT_PLATFORM_NAME, context);
    }
  }
}
