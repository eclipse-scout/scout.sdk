/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

  public static final String KEY = "javaEnv";

  public JavaEnvironmentExtension() {
    super(KEY);
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
  protected IJavaEnvironment annotationToContext(ExtendWithJavaEnvironmentFactory annotation) {
    //noinspection resource
    return createJavaEnvironmentUsingBuilder(annotation.value()).orElseThrow().wrap();
  }
}
