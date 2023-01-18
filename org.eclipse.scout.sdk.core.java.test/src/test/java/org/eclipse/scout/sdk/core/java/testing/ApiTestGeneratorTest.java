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

import org.eclipse.scout.sdk.core.java.fixture.BaseClass;
import org.eclipse.scout.sdk.core.java.fixture.ChildClass;
import org.eclipse.scout.sdk.core.java.fixture.InterfaceWithDefaultMethods;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ApiTestGeneratorTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class ApiTestGeneratorTest {

  @Test
  public void testApiGenerator(IJavaEnvironment env) {
    var gen = new ApiTestGenerator(env.requireType(BaseClass.class.getName()));
    gen.buildSource();

    var gen2 = new ApiTestGenerator(env.requireType(ChildClass.class.getName()));
    gen2.buildSource();

    new CoreJavaEnvironmentBinaryOnlyFactory().accept(binEnv -> {
      var interfaceTestType = binEnv.requireType(InterfaceWithDefaultMethods.class.getName());
      var gen3 = new ApiTestGenerator(interfaceTestType);
      gen3.buildSource();
    });
  }
}
