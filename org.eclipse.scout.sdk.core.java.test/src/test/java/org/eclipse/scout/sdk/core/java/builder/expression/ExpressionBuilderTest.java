/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.builder.expression;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.Api;
import org.eclipse.scout.sdk.core.java.builder.JavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.fixture.apidef.IJavaApi;
import org.eclipse.scout.sdk.core.java.fixture.apidef.JavaApiProvider;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(EmptyJavaEnvironmentFactory.class)
public class ExpressionBuilderTest {
  @Test
  public void testAppendNew(IJavaEnvironment env) {
    assertExpressionEquals("new ", env, IExpressionBuilder::appendNew);
    assertExpressionEquals("new ArrayList<String>(", env,
        b -> b.appendNew(ArrayList.class.getName() + JavaTypes.C_GENERIC_START + String.class.getName() + JavaTypes.C_GENERIC_END));

    // test using API
    Api.registerProvider(IJavaApi.class, new JavaApiProvider());
    try {
      assertExpressionEquals("new Name(", env, b -> b.appendNewFrom(IJavaApi.class, IJavaApi::TestClass));
    }
    finally {
      Api.unregisterProvider(IJavaApi.class);
    }
  }

  protected static void assertExpressionEquals(String expected, IJavaEnvironment env, Consumer<IExpressionBuilder<?>> test) {
    var memorySourceBuilder = MemorySourceBuilder.create();
    var builder = ExpressionBuilder.create(JavaSourceBuilder.create(memorySourceBuilder, env));
    test.accept(builder);
    Assertions.assertEquals(expected, memorySourceBuilder.source().toString());
  }
}
