/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.api;

import java.io.File;

/**
 * <h3>{@link IFileLocator}</h3>
 * <p>
 * Used in {@link IJavaEnvironment#getFileLocator()}
 *
 * @author Ivan Motsch
 * @since 5.2.0
 */
@FunctionalInterface
public interface IFileLocator {
  /**
   * @param path
   *          is the relative path, for example "src/main/resources/scout.xml"
   * @return a file from the workspace module / project
   *         <p>
   *         The returned file object is never null but may not exist.
   */
  File getFile(String path);
}
