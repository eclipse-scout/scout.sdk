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
package org.eclipse.scout.sdk.core.builder.java;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.IBuilderContext;
import org.eclipse.scout.sdk.core.imports.IImportCollector;
import org.eclipse.scout.sdk.core.imports.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.apidef.IApiSpecification;

/**
 * <h3>{@link IJavaBuilderContext}</h3>
 *
 * @since 6.1.0
 */
public interface IJavaBuilderContext extends IBuilderContext {

  /**
   * @return The {@link IJavaEnvironment} of this {@link IJavaBuilderContext}.
   */
  Optional<IJavaEnvironment> environment();

  /**
   * @return The {@link IImportValidator} used to resolve imports. The {@link IImportCollector#getJavaEnvironment()} of
   *         the returned {@link IImportValidator} is the same as {@link #environment()}.
   */
  IImportValidator validator();

  <A extends IApiSpecification> Optional<A> api(Class<A> apiDefinition);

  <A extends IApiSpecification> A requireApi(Class<A> apiDefinition);
}
