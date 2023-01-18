/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.testing;

import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.IJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentWithEcjBuilder;

public final class FixtureHelper {
  public static final String FIXTURE_PATH = "src/test/fixture";

  private FixtureHelper() {
  }

  public static final class CoreJavaEnvironmentWithSourceFactory implements IJavaEnvironmentFactory {
    @Override
    public JavaEnvironmentWithEcjBuilder<?> get() {
      return new JavaEnvironmentWithEcjBuilder<>()
          .withoutScoutSdk()
          .withParseMethodBodies(true)
          .withSourceFolder(FIXTURE_PATH);
    }
  }

  public static final class CoreJavaEnvironmentBinaryOnlyFactory implements IJavaEnvironmentFactory {

    @Override
    public JavaEnvironmentWithEcjBuilder<?> get() {
      return new JavaEnvironmentWithEcjBuilder<>()
          .withoutScoutSdk()
          .withSourcesIncluded(false)
          .withParseMethodBodies(true)
          .withClassesFolder("target/classes")
          .withClassesFolder("target/test-classes");
    }
  }
}
