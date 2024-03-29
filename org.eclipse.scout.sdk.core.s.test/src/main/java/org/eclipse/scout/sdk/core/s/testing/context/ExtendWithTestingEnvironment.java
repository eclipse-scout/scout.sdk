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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild;
import org.eclipse.scout.sdk.core.s.util.maven.MavenRunner;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link ExtendWithTestingEnvironment}</h3>
 * <p>
 * Configuration for the JUnit {@link TestingEnvironmentExtension}. If a test case has a method parameter of type
 * {@link TestingEnvironment}, the passed instance uses the configuration as specified by this annotation.
 * <p>
 * This annotation can be added to methods or types. The {@link TestingEnvironment} is created for each annotation. This
 * means if a method is annotated this method gets its own {@link TestingEnvironment} instance that will be discarded
 * after the method finished. If the annotation is added to a type, one environment will be used for all tests in that
 * type.
 *
 * @since 7.1.0
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@ExtendWith(TestingEnvironmentExtension.class)
public @interface ExtendWithTestingEnvironment {

  /**
   * Configures {@link IJavaEnvironment} factory to use to create the primary {@link IJavaEnvironment} that will be
   * available in the {@link TestingEnvironment#primaryEnvironment()}. By default an {@link EmptyJavaEnvironmentFactory}
   * factory will be used.
   */
  ExtendWithJavaEnvironmentFactory primary() default @ExtendWithJavaEnvironmentFactory(EmptyJavaEnvironmentFactory.class);

  /**
   * Configures the {@link IJavaEnvironment} factory to use to create the {@link IJavaEnvironment} that will be used to
   * create DTOs in it. By default an {@link EmptyJavaEnvironmentFactory} factory will be used.
   */
  ExtendWithJavaEnvironmentFactory dto() default @ExtendWithJavaEnvironmentFactory(EmptyJavaEnvironmentFactory.class);

  /**
   * Specifies if created resources should be written to disk. This can be useful if the resource should be available to
   * a subsequent Maven call (see {@link MavenRunner#execute(MavenBuild, IEnvironment, IProgress)}). Default is
   * {@code false}.
   */
  boolean flushToDisk() default false;

  /**
   * Specifies whether to ensure that created resources do not have compile errors. The default is {@code true}. Turning
   * this property off can be useful when testing the creation of multiple cyclic classes. Here, you must then manually
   * ensure that there are no compile errors.
   */
  boolean assertNoCompileErrors() default true;
}
