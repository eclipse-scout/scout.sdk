/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.builder;

import org.eclipse.scout.sdk.core.util.PropertySupport;

/**
 * <h3>{@link IBuilderContext}</h3>
 *
 * @since 6.1.0
 */
public interface IBuilderContext {

  /**
   * Context property for the module that is used to resolve imports and in which the source of the generator will be
   * stored.
   *
   * @see IBuilderContext#properties()
   */
  String PROPERTY_MODULE = "module";

  /**
   * Context property for the absolute target {@link java.nio.file.Path} in which the source of the generator will be
   * stored.<br>
   * This might be the exact path to the target file or a parent folder of the file (if the exact target file is not yet
   * known).
   *
   * @see IBuilderContext#properties()
   */
  String PROPERTY_TARGET_PATH = "targetPath";

  /**
   * @return The line delimiter to use.
   */
  String lineDelimiter();

  /**
   * @return A {@link PropertySupport} that can be used to provider and share custom properties.
   */
  PropertySupport properties();
}
