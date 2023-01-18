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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContext;
import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.fixture.apidef.AlwaysMissingApiProvider;
import org.eclipse.scout.sdk.core.java.fixture.apidef.IAlwaysMissingApi;
import org.eclipse.scout.sdk.core.java.fixture.apidef.IJavaApi;
import org.eclipse.scout.sdk.core.java.fixture.apidef.Java11Api;
import org.eclipse.scout.sdk.core.java.fixture.apidef.JavaApiProvider;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(EmptyJavaEnvironmentFactory.class)
public class ApiFunctionTest {
  @Test
  public void testFunctionCalledWithoutEnvironment() {
    var val = "result".toCharArray();
    var f = new ApiFunction<IJavaApi, char[]>(val);
    assertSame(val, f.apply().orElseThrow());
    assertSame(val, f.apply((IJavaEnvironment) null).orElseThrow());
    assertFalse(f.apiClass().isPresent());
    assertNotNull(f.apiFunction());
  }

  @Test
  public void testWithContext(IJavaEnvironment env) {
    Api.registerProvider(IJavaApi.class, new JavaApiProvider());
    try {
      assertEquals(Java11Api.VALUE, new ApiFunction<>(IJavaApi.class, IJavaApi::method).apply(env).orElseThrow());
    }
    finally {
      Api.unregisterProvider(IJavaApi.class);
    }
  }

  @Test
  public void testFunctionWithoutApi(IJavaEnvironment env) {
    assertEquals("val", new ApiFunction<>(null, c -> "val").apply(env).orElseThrow());
    assertEquals("val", new ApiFunction<>(null, c -> "val").apply().orElseThrow());
    assertEquals("val", new ApiFunction<>(null, c -> "val").apply(new JavaBuilderContext(env)));

    assertEquals("val", new ApiFunction<>("val").apply(env).orElseThrow());
    assertEquals("val", new ApiFunction<>("val").apply().orElseThrow());
    assertEquals("val", new ApiFunction<>("val").apply(new JavaBuilderContext(env)));
  }

  @Test
  public void testMissingApiWithContext(IJavaEnvironment env) {
    Api.registerProvider(IAlwaysMissingApi.class, new AlwaysMissingApiProvider());
    try {
      assertTrue(new ApiFunction<>(IAlwaysMissingApi.class, IAlwaysMissingApi::alwaysMissing).apply(env).isEmpty());
    }
    finally {
      Api.unregisterProvider(IAlwaysMissingApi.class);
    }
  }

  @Test
  public void testReturnsNullWithoutContext() {
    assertFalse(new ApiFunction<>(IJavaApi.class, IJavaApi::method).apply().isPresent());
  }
}
