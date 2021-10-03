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
package org.eclipse.scout.sdk.core.testing.context;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.junit.platform.commons.support.ReflectionSupport.findMethod;
import static org.junit.platform.commons.support.ReflectionSupport.invokeMethod;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcj;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.ReflectionSupport;

/**
 * <h3>{@link JavaEnvironmentExtension}</h3>
 * <p>
 * A JUnit {@link ParameterResolver} that allows unit tests to specify a method parameter of type
 * {@link IJavaEnvironment}. An {@link IJavaEnvironment} is created as configured using
 * {@link ExtendWithJavaEnvironmentFactory}.
 * <p>
 * The scope of the {@link IJavaEnvironment} is defined by the placement of the {@link ExtendWithJavaEnvironmentFactory}
 * annotation. If it is placed on the method, this test case gets its own private instance. On the other hand if the
 * class is annotated one {@link IJavaEnvironment} is used for all test cases in this class.
 *
 * @since 7.1.0
 * @see ExtendWithJavaEnvironmentFactory
 */
public class JavaEnvironmentExtension extends AbstractContextExtension<IJavaEnvironment, ExtendWithJavaEnvironmentFactory> {

  protected JavaEnvironmentExtension() {
    super("javaEnv");
  }

  public static Optional<JavaEnvironmentWithEcj> createJavaEnvironmentUsingBuilder(JavaEnvironmentWithEcjBuilder<?> builder) {
    return Optional.ofNullable(builder)
        .map(b -> b.withParseMethodBodies(true)) // ensure method bodies are parsed in testing
        .map(b -> (JavaEnvironmentWithEcj) invokeMethod(
            findMethod(b.getClass(), "build")
                .orElseThrow(() -> newFail("Could not find method 'build' in class '{}'.", b.getClass())),
            b));
  }

  public static Optional<JavaEnvironmentWithEcj> createJavaEnvironmentUsingBuilder(Class<? extends Supplier<JavaEnvironmentWithEcjBuilder<?>>> builderClass) {
    return Optional.ofNullable(builderClass)
        .map(ReflectionSupport::newInstance)
        .map(Supplier::get)
        .flatMap(JavaEnvironmentExtension::createJavaEnvironmentUsingBuilder);
  }

  @Override
  protected void close(IJavaEnvironment element) {
    closeResource((AutoCloseable) element.unwrap());
  }

  @Override
  @SuppressWarnings("resource")
  protected IJavaEnvironment annotationToContext(ExtendWithJavaEnvironmentFactory annotation) {
    return createJavaEnvironmentUsingBuilder(annotation.value()).orElseThrow().wrap();
  }
}
