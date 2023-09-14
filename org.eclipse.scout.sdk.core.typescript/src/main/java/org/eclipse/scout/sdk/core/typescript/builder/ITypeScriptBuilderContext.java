/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.builder;

import org.eclipse.scout.sdk.core.builder.IBuilderContext;
import org.eclipse.scout.sdk.core.typescript.builder.imports.IES6ImportValidator;

public interface ITypeScriptBuilderContext extends IBuilderContext {
  /**
   * @return The {@link IES6ImportValidator} of this {@link IBuilderContext}. Is never {@code null}.
   */
  IES6ImportValidator importValidator();
}
