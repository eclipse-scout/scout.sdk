/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.apidef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.fixture.apidef.IJavaApi;
import org.eclipse.scout.sdk.core.java.fixture.apidef.Java8Api.TestClass;
import org.eclipse.scout.sdk.core.java.fixture.apidef.JavaApiProvider;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(EmptyJavaEnvironmentFactory.class)
public class ApiTest {
  @BeforeAll
  public static void setup() {
    Api.registerProvider(IJavaApi.class, new JavaApiProvider());
  }

  @Test
  public void testLatest() {
    assertEquals(13, Api.latestMajorVersion(IJavaApi.class));
  }

  @Test
  public void testVersion(IJavaEnvironment env) {
    assertEquals(12, Api.version(IJavaApi.class, env.requireType(Long.class.getName())).orElseThrow().major());
  }

  @Test
  public void testAllKnown() {
    assertEquals(3, Api.allKnown(IJavaApi.class).count());
  }

  @Test
  public void testDump() {
    var dump = Api.dump(Api.create(IJavaApi.class, new ApiVersion(11)));
    assertNotNull(dump.get(TestClass.TEST_CLASS_FQN));
  }

  @AfterAll
  public static void cleanup() {
    Api.unregisterProvider(IJavaApi.class);
  }
}
