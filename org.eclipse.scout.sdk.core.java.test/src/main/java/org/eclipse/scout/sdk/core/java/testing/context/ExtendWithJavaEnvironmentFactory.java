/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.testing.context;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.RunningJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentWithEcjBuilder;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link ExtendWithJavaEnvironmentFactory}</h3>
 * <p>
 * Configuration for the JUnit {@link JavaEnvironmentExtension}. If a test case has a method parameter of type
 * {@link IJavaEnvironment}, the passed instance uses the configuration as specified by this annotation.
 * <p>
 * This annotation can be added to methods or types. The {@link IJavaEnvironment} is created for each annotation. This
 * means if a method is annotated this method gets its own {@link IJavaEnvironment} instance that will be discarded
 * after the method finished. If the annotation is added to a type, one environment will be used for all tests in that
 * type.
 *
 * @since 7.1.0
 * @see JavaEnvironmentExtension
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@ExtendWith(JavaEnvironmentExtension.class)
public @interface ExtendWithJavaEnvironmentFactory {
  /**
   * Specifies the {@link IJavaEnvironment} factory to use.
   *
   * @return The class that returns an {@link JavaEnvironmentWithEcjBuilder} ready to use.
   * @see RunningJavaEnvironmentFactory
   * @see EmptyJavaEnvironmentFactory
   */
  Class<? extends Supplier<JavaEnvironmentWithEcjBuilder<?>>> value();
}
