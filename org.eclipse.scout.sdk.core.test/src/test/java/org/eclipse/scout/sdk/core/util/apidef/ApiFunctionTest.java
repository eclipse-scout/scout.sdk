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
package org.eclipse.scout.sdk.core.util.apidef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.fixture.apidef.AlwaysMissingApiProvider;
import org.eclipse.scout.sdk.core.fixture.apidef.IAlwaysMissingApi;
import org.eclipse.scout.sdk.core.fixture.apidef.IJavaApi;
import org.eclipse.scout.sdk.core.fixture.apidef.JavaApi11;
import org.eclipse.scout.sdk.core.fixture.apidef.JavaApiProvider;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(EmptyJavaEnvironmentFactory.class)
public class ApiFunctionTest {
  @Test
  public void testFunctionCalledWithoutEnvironment() {
    char[] val = "result".toCharArray();
    ApiFunction<IJavaApi, char[]> f = new ApiFunction<>(val);
    assertSame(val, f.apply().get());
    assertSame(val, f.apply((IApiSpecification) null));
    assertSame(val, f.apply((IJavaSourceBuilder<?>) null).get());
    assertSame(val, f.apply((IJavaEnvironment) null).get());
    assertFalse(f.apiClass().isPresent());
    assertNotNull(f.apiFunction());
  }

  @Test
  public void testWithContext(IJavaEnvironment env) {
    Api.registerProvider(IJavaApi.class, new JavaApiProvider());
    try {
      assertEquals(JavaApi11.VALUE, new ApiFunction<>(IJavaApi.class, IJavaApi::method).apply(env).get());
    }
    finally {
      Api.unregisterProvider(IJavaApi.class);
    }
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
  public void testFailsOnApiWithoutContext() {
    assertThrows(IllegalArgumentException.class, () -> new ApiFunction<>(IJavaApi.class, IJavaApi::method).apply());
  }
}
