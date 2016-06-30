/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.util.maven;

import org.apache.commons.lang3.Validate;

/**
 * <h3>{@link MavenRunner}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class MavenRunner {

  private static IMavenRunnerSpi mavenRunner = new MavenCliRunner();

  /**
   * Sets a new {@link IMavenRunnerSpi} strategy.
   * 
   * @param newRunner
   *          The new strategy. Cannot be <code>null</code>.
   */
  public static synchronized void setMavenRunner(IMavenRunnerSpi newRunner) {
    mavenRunner = Validate.notNull(newRunner);
  }

  /**
   * @return The current {@link IMavenRunnerSpi} strategy.
   */
  public static synchronized IMavenRunnerSpi getMavenRunner() {
    return mavenRunner;
  }

  /**
   * Executes the given {@link MavenBuild} using the current {@link IMavenRunnerSpi} strategy.
   * 
   * @param build
   *          The {@link MavenBuild} to execute.
   */
  public static void execute(MavenBuild build) {
    getMavenRunner().execute(build);
  }

  private MavenRunner() {
    // no instances
  }
}
