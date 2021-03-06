/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.testing;

import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.InterfaceWithDefaultMethods;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
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
