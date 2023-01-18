/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testing.context;

import static org.eclipse.scout.sdk.core.java.testing.context.JavaEnvironmentExtension.createJavaEnvironmentUsingBuilder;

import org.eclipse.scout.sdk.core.java.testing.context.AbstractContextExtension;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * <h3>{@link TestingEnvironmentExtension}</h3>
 * <p>
 * A JUnit {@link ParameterResolver} that allows unit tests to specify a method parameter of type
 * {@link TestingEnvironment}. A {@link TestingEnvironment} is created as configured using
 * {@link ExtendWithTestingEnvironment}.
 * <p>
 * The scope of the {@link TestingEnvironment} is defined by the placement of the {@link ExtendWithTestingEnvironment}
 * annotation. If it is placed on the method, this test case gets its own private instance. On the other hand if the
 * class is annotated, one {@link TestingEnvironment} is used for all test cases in this class.
 *
 * @since 7.1.0
 */
public class TestingEnvironmentExtension extends AbstractContextExtension<TestingEnvironment, ExtendWithTestingEnvironment> {

  protected TestingEnvironmentExtension() {
    super("env");
  }

  @Override
  protected TestingEnvironment annotationToContext(ExtendWithTestingEnvironment annotation) {
    //noinspection resource
    return new TestingEnvironment(createJavaEnvironmentUsingBuilder(annotation.primary().value()).orElseThrow().wrap(),
        annotation.flushToDisk(),
        annotation.assertNoCompileErrors(),
        createJavaEnvironmentUsingBuilder(annotation.dto().value()).orElseThrow().wrap());
  }

  @Override
  protected void close(TestingEnvironment element) {
    closeResource(element);
  }
}
