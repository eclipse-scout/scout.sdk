/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.Api;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.fixture.AnnotationQueryTestFixture;
import org.eclipse.scout.sdk.core.java.fixture.AnnotationQueryTestFixture.TestChildClass;
import org.eclipse.scout.sdk.core.java.fixture.apidef.AlwaysMissingApiProvider;
import org.eclipse.scout.sdk.core.java.fixture.apidef.IAlwaysMissingApi;
import org.eclipse.scout.sdk.core.java.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link AnnotationQueryTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class AnnotationQueryTest {

  @Test
  public void testAllSuperTypes(IJavaEnvironment env) {
    var testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName() + JavaTypes.C_DOLLAR + TestChildClass.class.getSimpleName());
    assertEquals(4, testChildClass.methods().first().orElseThrow().annotations().withSuperTypes(true).stream().count());
  }

  @Test
  public void testSuperClasses(IJavaEnvironment env) {
    var testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName() + JavaTypes.C_DOLLAR + TestChildClass.class.getSimpleName());
    assertEquals(3, testChildClass.methods().first().orElseThrow().annotations().withSuperClasses(true).stream().count());
  }

  @Test
  public void testSuperInterfaces(IJavaEnvironment env) {
    var testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName() + JavaTypes.C_DOLLAR + TestChildClass.class.getSimpleName());
    assertEquals(3, testChildClass.methods().first().orElseThrow().annotations().withSuperInterfaces(true).stream().count());
  }

  @Test
  public void testOnField(IJavaEnvironment env) {
    var testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName());
    assertEquals(1, testChildClass.fields().first().orElseThrow().annotations().stream().count());
  }

  @Test
  public void testAnnotationQueryWithNonExistingApi(IJavaEnvironment env) {
    Api.registerProvider(IAlwaysMissingApi.class, new AlwaysMissingApiProvider());
    try {
      var testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName());
      assertTrue(testChildClass.annotations().withNameFrom(IAlwaysMissingApi.class, IAlwaysMissingApi::alwaysMissing).first().isEmpty());
    }
    finally {
      Api.unregisterProvider(IAlwaysMissingApi.class);
    }
  }

  @Test
  public void testAnnotationQueryWithNonExistingApiUsingWrapper(IJavaEnvironment env) {
    Api.registerProvider(IAlwaysMissingApi.class, new AlwaysMissingApiProvider());
    try {
      var testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName());
      assertNotNull(AlwaysMissingAnnotation.TYPE_NAME);
      assertTrue(testChildClass.annotations().withManagedWrapper(AlwaysMissingAnnotation.class).first().isEmpty());
    }
    finally {
      Api.unregisterProvider(IAlwaysMissingApi.class);
    }
  }

  private static final class AlwaysMissingAnnotation extends AbstractManagedAnnotation {
    public static final ApiFunction<?, ITypeNameSupplier> TYPE_NAME = new ApiFunction<>(IAlwaysMissingApi.class, IAlwaysMissingApi::alwaysMissing);
  }
}
