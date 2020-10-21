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
package org.eclipse.scout.sdk.core.s.apidef;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertApiValid;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class)
public class ScoutApiTest {

  @Test
  public void testCreate(IJavaEnvironment env) {
    assertApiContainsData(ScoutApi.create(env.requireType(Long.class.getName())).get());
    assertApiContainsData(ScoutApi.create(env).get());
  }

  @Test
  public void testLatest() {
    assertNotNull(ScoutApi.latest().level().asString());
  }

  @Test
  public void testCreationUsingContext(IJavaEnvironment env) {
    var generator = PrimaryTypeGenerator.create()
        .withPackageName("org.test")
        .withElementName("MyClass")
        .withAnnotation(ScoutAnnotationGenerator.createAuthentication());
    assertNoCompileErrors(env, generator);
  }

  @Test
  public void testScoutApiMatches(IJavaEnvironment env) {
    assertApiValid(IScoutApi.class, env, ScoutApiTest::assertOthersValid);
  }

  private static boolean assertOthersValid(IType type, IScoutApi api) {
    // Menu Type enums are accepted to be different (without suffix) because the declaring class is an enum type
    return type.isInstanceOf(api.IMenuType());
  }

  private static void assertApiContainsData(IScoutApi api) {
    assertTrue(api.level().segments().length > 0);
    assertEquals(Scout10Api.DATA_ANNOTATION.fqn(), api.Data().fqn());
  }
}
