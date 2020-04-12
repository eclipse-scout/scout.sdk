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
package org.eclipse.scout.sdk.core.testing;

import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.IJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder;

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
