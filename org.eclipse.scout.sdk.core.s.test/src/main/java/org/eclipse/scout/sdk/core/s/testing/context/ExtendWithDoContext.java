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
package org.eclipse.scout.sdk.core.s.testing.context;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.platform.namespace.INamespace;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers;
import org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers.IDoContextResolver;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
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
  Class<? extends INamespace> namespace() default INamespace.class;

  /**
   * @return The type version class to be returned in {@link DoContextResolvers#resolve(CharSequence, IJavaEnvironment)}
   */
  Class<? extends ITypeVersion> typeVersion() default ITypeVersion.class;
}
