/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
