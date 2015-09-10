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
package org.eclipse.scout.sdk.s2e.trigger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * <h3>{@link ITypeChangedOperation}</h3><br>
 *
 * @author imo
 * @since 5.1
 */
public interface ITypeChangedOperation {

  String getOperationName();

  /**
   * throw a {@link IllegalArgumentException} if the operation should not be executed and has invalid parameters
   */
  void validate();

  /**
   * Usually the run method is called of a job implementation.
   *
   * @param monitor
   *          to provide progress information and observe the cancel state.
   * @throws CoreException
   */
  void run(IProgressMonitor monitor) throws CoreException;

}
