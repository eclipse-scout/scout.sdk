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
package org.eclipse.scout.sdk.core.s.util.maven;

import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link MavenRunner}</h3>
 *
 * @since 5.2.0
 */
public final class MavenRunner {

  private static IMavenRunnerSpi mavenRunner;

  private MavenRunner() {
    // no instances
  }

  /**
   * Sets a new {@link IMavenRunnerSpi} strategy.
   *
   * @param newRunner
   *          The new strategy or {@code null}.
   */
  public static synchronized void set(IMavenRunnerSpi newRunner) {
    mavenRunner = newRunner;
  }

  /**
   * @return The current {@link IMavenRunnerSpi} strategy.
   */
  public static synchronized IMavenRunnerSpi get() {
    return mavenRunner;
  }

  /**
   * Sets a new {@link IMavenRunnerSpi} using the given {@link Supplier} if there is not yet a runner present.
   *
   * @param producer
   *          The {@link Supplier} to use if there is no runner yet.
   * @return the produced runner that will be active after this call.
   */
  public static synchronized IMavenRunnerSpi setIfAbsent(Supplier<IMavenRunnerSpi> producer) {
    if (get() == null) {
      set(producer.get());
    }
    return get();
  }

  /**
   * Executes the given {@link MavenBuild} using the current {@link IMavenRunnerSpi} strategy.
   *
   * @param build
   *          The {@link MavenBuild} to execute. Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} in which the Maven build should be executed. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} indicator. Must not be {@code null}.
   * @throws IllegalArgumentException
   *           if an argument is {@code null} or no {@link IMavenRunnerSpi} has been set.
   */
  public static void execute(MavenBuild build, IEnvironment env, IProgress progress) {
    Ensure.notNull(get(), "no maven runner set").execute(Ensure.notNull(build), Ensure.notNull(env), Ensure.notNull(progress));
  }
}
