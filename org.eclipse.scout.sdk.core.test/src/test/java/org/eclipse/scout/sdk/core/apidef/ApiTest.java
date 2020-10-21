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
package org.eclipse.scout.sdk.core.apidef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.scout.sdk.core.fixture.apidef.IJavaApi;
import org.eclipse.scout.sdk.core.fixture.apidef.Java8Api.TestClass;
import org.eclipse.scout.sdk.core.fixture.apidef.JavaApiProvider;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
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
    assertEquals(11, Api.version(IJavaApi.class, env.requireType(Long.class.getName())).get().segments()[0]);
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
