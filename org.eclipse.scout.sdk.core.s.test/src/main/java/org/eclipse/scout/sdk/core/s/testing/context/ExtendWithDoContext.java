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

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers;
import org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers.IDoContextResolver;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * An testing extension for {@link DoContextResolvers}. Registers an {@link IDoContextResolver} which always returns the
 * classes configured.<br>
 * Requires {@link ExtendWithTestingEnvironment} or {@link ExtendWithJavaEnvironmentFactory} to present to configure the
 * classpath in which the configured classes are searched.
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@ExtendWith(DoContextExtension.class)
public @interface ExtendWithDoContext {

  /**
   * @return The namespace class to be returned in {@link DoContextResolvers#resolve(CharSequence, IJavaEnvironment)}
   */
  Class<?> namespace();

  /**
   * @return The type version class to be returned in {@link DoContextResolvers#resolve(CharSequence, IJavaEnvironment)}
   */
  Class<?> typeVersion();
}
