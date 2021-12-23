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
package org.eclipse.scout.sdk.core.model.api.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.apidef.Api;
import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.fixture.AnnotationQueryTestFixture;
import org.eclipse.scout.sdk.core.fixture.AnnotationQueryTestFixture.TestChildClass;
import org.eclipse.scout.sdk.core.fixture.apidef.AlwaysMissingApiProvider;
import org.eclipse.scout.sdk.core.fixture.apidef.IAlwaysMissingApi;
import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
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
