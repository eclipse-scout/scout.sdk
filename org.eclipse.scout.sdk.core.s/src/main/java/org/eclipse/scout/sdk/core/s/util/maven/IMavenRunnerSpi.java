/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.util.maven;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;

/**
 * <h3>{@link IMavenRunnerSpi}</h3> Represents a class that is capable to execute {@link MavenBuild}s.
 *
 * @since 5.2.0
 */
@FunctionalInterface
public interface IMavenRunnerSpi {
  /**
   * Executes the given {@link MavenBuild}.
   *
   * @param build
   *          The {@link MavenBuild} to execute.
   * @param env
   *          The {@link IEnvironment} in which the Maven build should be executed. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} indicator. Must not be {@code null}.
   * @throws RuntimeException
   *           if there is an error during the maven build.
   */
  void execute(MavenBuild build, IEnvironment env, IProgress progress);
}
