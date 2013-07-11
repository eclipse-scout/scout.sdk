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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.IOperation;

/**
 * Operation used for auto-update resources that are derived from a Scout model type (e.g. form data, page data).
 * 
 * @since 3.10.0-M1
 */
public interface IAutoUpdateOperation extends IOperation {

  /**
   * @return Returns the Scout model type this auto-update operation is working on.
   */
  IType getModelType();
}
