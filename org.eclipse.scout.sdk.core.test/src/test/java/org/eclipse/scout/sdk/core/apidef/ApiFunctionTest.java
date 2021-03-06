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
package org.eclipse.scout.sdk.core.apidef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.fixture.apidef.AlwaysMissingApiProvider;
import org.eclipse.scout.sdk.core.fixture.apidef.IAlwaysMissingApi;
import org.eclipse.scout.sdk.core.fixture.apidef.IJavaApi;
import org.eclipse.scout.sdk.core.fixture.apidef.Java11Api;
import org.eclipse.scout.sdk.core.fixture.apidef.JavaApiProvider;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
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
