/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.apidef;

import static java.util.Collections.emptyMap;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertApiValid;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.java.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class)
public class ScoutApiTest {

  @Test
  public void testCreate(IJavaEnvironment env) {
    assertApiContainsData(ScoutApi.create(env.requireType(Long.class.getName())).orElseThrow());
    assertApiContainsData(ScoutApi.create(env).orElseThrow());
  }

  @Test
  public void testLatest() {
    assertNotNull(ScoutApi.latest().maxLevel().asString());
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
    assertApiValid(IScoutApi.class, env, ScoutApiTest::onlyInvalid);
  }

  @Test
  public void testSupportedJavaVersions() {
    assertArrayEquals(new int[]{8, 11}, ScoutApi.create("10.0").supportedJavaVersions());
    assertArrayEquals(new int[]{8, 11}, ScoutApi.create("11.0-SNAPSHOT").supportedJavaVersions());
    assertArrayEquals(new int[]{8, 11}, ScoutApi.create("11.0.15").supportedJavaVersions());
    assertArrayEquals(new int[]{11}, ScoutApi.create("22.0.1").supportedJavaVersions());
    assertArrayEquals(new int[]{11}, ScoutApi.create("22.0.9").supportedJavaVersions());
    assertArrayEquals(new int[]{11}, ScoutApi.create("22.0.10").supportedJavaVersions());
    assertArrayEquals(new int[]{11, 17}, ScoutApi.create("22.0.11").supportedJavaVersions());
    assertArrayEquals(new int[]{11, 17}, ScoutApi.create("22.0").supportedJavaVersions());
    assertArrayEquals(new int[]{11, 17}, ScoutApi.create("23.2").supportedJavaVersions());
    assertArrayEquals(new int[]{17}, ScoutApi.create("24.1-SNAPSHOT").supportedJavaVersions());
    assertArrayEquals(new int[]{17}, ScoutApi.create("24.2-SNAPSHOT").supportedJavaVersions());
    assertArrayEquals(new int[]{17}, ScoutApi.create("24.1").supportedJavaVersions());
    assertArrayEquals(new int[]{17}, ScoutApi.create("24.1.0").supportedJavaVersions());
    assertArrayEquals(new int[]{17}, ScoutApi.create("24.1.165").supportedJavaVersions());
    assertArrayEquals(new int[]{17}, ScoutApi.create("24.2.13").supportedJavaVersions());
    assertArrayEquals(new int[]{21}, ScoutApi.create("25.1").supportedJavaVersions());
    assertArrayEquals(new int[]{21}, ScoutApi.create("25.1-SNAPSHOT").supportedJavaVersions());
    assertArrayEquals(new int[]{21}, ScoutApi.create("25.1.0").supportedJavaVersions());
    assertArrayEquals(new int[]{21}, ScoutApi.create("25.1.1234").supportedJavaVersions());
  }

  private static Map<String, String> onlyInvalid(Map<String, String> candidates, IType type, IScoutApi api) {
    if (type.isInstanceOf(api.IMenuType())) {
      // Menu Type enums are accepted to be different (without suffix) because the declaring class is an enum type
      return emptyMap();
    }
    return candidates;
  }

  private static void assertApiContainsData(IScoutApi api) {
    assertTrue(api.maxLevel().segments().length > 0);
    assertEquals(Scout10Api.DATA_ANNOTATION.fqn(), api.Data().fqn());
  }
}
