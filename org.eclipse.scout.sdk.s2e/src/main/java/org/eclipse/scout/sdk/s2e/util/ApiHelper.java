/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.util;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.callInEclipseEnvironmentSync;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.core.java.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;

public final class ApiHelper {

  private ApiHelper() {
  }

  public static IScoutApi requireScoutApiFor(IJavaElement context) {
    return requireScoutApiFor(context, null);
  }

  public static IScoutApi requireScoutApiFor(IJavaElement context, EclipseEnvironment environment) {
    var project = context.getJavaProject();
    return applyWithEclipseEnvironment(e -> requireApiFor(project, IScoutApi.class, e), environment);
  }

  public static Optional<ApiVersion> scoutVersionOf(IJavaElement context, EclipseEnvironment environment) {
    var project = context.getJavaProject();
    return applyWithEclipseEnvironment(e -> ScoutApi.version(e.toScoutJavaEnvironment(project)), environment);
  }

  static <T> T applyWithEclipseEnvironment(Function<EclipseEnvironment, T> task, EclipseEnvironment existingEnvironment) {
    if (existingEnvironment == null) {
      return callInEclipseEnvironmentSync((e, p) -> task.apply(e), new NullProgressMonitor());
    }
    return task.apply(existingEnvironment);
  }

  public static <T extends IApiSpecification> T requireApiFor(IJavaProject project, Class<T> api, EclipseEnvironment environment) {
    return applyWithEclipseEnvironment(e -> e.toScoutJavaEnvironment(project).requireApi(api), environment);
  }

  public static Optional<IScoutApi> scoutApiFor(IJavaProject project) {
    return scoutApiFor(project, null);
  }

  public static Optional<IScoutApi> scoutApiFor(IJavaProject project, EclipseEnvironment environment) {
    return apiFor(project, IScoutApi.class, environment);
  }

  public static <T extends IApiSpecification> Optional<T> apiFor(IJavaProject project, Class<T> api) {
    return apiFor(project, api, null);
  }

  public static <T extends IApiSpecification> Optional<T> apiFor(IJavaProject project, Class<T> api, EclipseEnvironment environment) {
    return applyWithEclipseEnvironment(e -> e.toScoutJavaEnvironment(project).api(api), environment);
  }
}
