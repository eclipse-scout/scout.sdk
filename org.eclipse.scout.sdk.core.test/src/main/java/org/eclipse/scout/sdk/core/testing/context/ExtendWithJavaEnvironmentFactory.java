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
package org.eclipse.scout.sdk.core.testing.context;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.RunningJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder;

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
