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
package org.eclipse.scout.sdk.core.model.api.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.fixture.AnnotationQueryTestFixture;
import org.eclipse.scout.sdk.core.fixture.AnnotationQueryTestFixture.TestChildClass;
import org.eclipse.scout.sdk.core.fixture.apidef.AlwaysMissingApiProvider;
import org.eclipse.scout.sdk.core.fixture.apidef.IAlwaysMissingApi;
import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.apidef.Api;
import org.eclipse.scout.sdk.core.util.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.util.apidef.IClassNameSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link AnnotationQueryTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class AnnotationQueryTest {

  @Test
  public void testAllSuperTypes(IJavaEnvironment env) {
    IType testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName() + JavaTypes.C_DOLLAR + TestChildClass.class.getSimpleName());
    assertEquals(4, testChildClass.methods().first().get().annotations().withSuperTypes(true).stream().count());
  }

  @Test
  public void testSuperClasses(IJavaEnvironment env) {
    IType testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName() + JavaTypes.C_DOLLAR + TestChildClass.class.getSimpleName());
    assertEquals(3, testChildClass.methods().first().get().annotations().withSuperClasses(true).stream().count());
  }

  @Test
  public void testSuperInterfaces(IJavaEnvironment env) {
    IType testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName() + JavaTypes.C_DOLLAR + TestChildClass.class.getSimpleName());
    assertEquals(3, testChildClass.methods().first().get().annotations().withSuperInterfaces(true).stream().count());
  }

  @Test
  public void testOnField(IJavaEnvironment env) {
    IType testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName());
    assertEquals(1, testChildClass.fields().first().get().annotations().stream().count());
  }

  @Test
  public void testAnnotationQueryWithNonExistingApi(IJavaEnvironment env) {
    Api.registerProvider(IAlwaysMissingApi.class, new AlwaysMissingApiProvider());
    try {
      IType testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName());
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
      IType testChildClass = env.requireType(AnnotationQueryTestFixture.class.getName());
      assertNotNull(AlwaysMissingAnnotation.TYPE_NAME);
      assertTrue(testChildClass.annotations().withManagedWrapper(AlwaysMissingAnnotation.class).first().isEmpty());
    }
    finally {
      Api.unregisterProvider(IAlwaysMissingApi.class);
    }
  }

  private static final class AlwaysMissingAnnotation extends AbstractManagedAnnotation {
    public static final ApiFunction<?, IClassNameSupplier> TYPE_NAME = new ApiFunction<>(IAlwaysMissingApi.class, IAlwaysMissingApi::alwaysMissing);
  }
}
