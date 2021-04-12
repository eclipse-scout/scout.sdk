/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.testing.context;

import static org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension.createJavaEnvironmentUsingBuilder;

import org.eclipse.scout.sdk.core.testing.context.AbstractContextExtension;
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
    return new TestingEnvironment(createJavaEnvironmentUsingBuilder(annotation.primary().value()).get().wrap(),
        annotation.flushToDisk(),
        createJavaEnvironmentUsingBuilder(annotation.dto().value()).get().wrap());
  }

  @Override
  protected void close(TestingEnvironment element) {
    closeResource(element);
  }
}
