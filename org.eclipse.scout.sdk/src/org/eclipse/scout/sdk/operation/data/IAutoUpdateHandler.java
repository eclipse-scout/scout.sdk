/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.data;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * Strategy used for auto-generating resources that are derived from a Scout model type.
 * 
 * @since 3.10.0-M1
 */
public interface IAutoUpdateHandler {

  /**
   * Creates an operation that generates or updates types that are depending on the given model type.
   * 
   * @param modelType
   * @param modelTypeHierarchy
   * @return Returns an update operation if the given model type is supported by this handler. Otherwise
   *         <code>null</code>.
   * @throws CoreException
   */
  IAutoUpdateOperation createUpdateOperation(IType modelType, ITypeHierarchy modelTypeHierarchy) throws CoreException;
}
