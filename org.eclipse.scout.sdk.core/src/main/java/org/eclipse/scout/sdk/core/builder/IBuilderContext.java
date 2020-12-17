/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
   * Context property for the java project module that is used to resolve imports and in which the source of the
   * generator will be stored.
   *
   * @see IBuilderContext#properties()
   */
  String PROPERTY_JAVA_MODULE = "javaModule";

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
