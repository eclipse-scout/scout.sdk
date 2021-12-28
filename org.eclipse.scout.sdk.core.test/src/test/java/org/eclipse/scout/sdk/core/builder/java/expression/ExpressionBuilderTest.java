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
package org.eclipse.scout.sdk.core.builder.java.expression;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.apidef.Api;
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaSourceBuilder;
import org.eclipse.scout.sdk.core.fixture.apidef.IJavaApi;
import org.eclipse.scout.sdk.core.fixture.apidef.JavaApiProvider;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
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
