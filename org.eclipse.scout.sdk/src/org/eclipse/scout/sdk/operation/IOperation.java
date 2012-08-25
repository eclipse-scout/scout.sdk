/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link IOperation}</h3><br>
 * An operation is usually used to create, modify or delete some java elements. To execute
 * one or a set of operations the best way is to use the {@link OperationJob}.<br>
 * <h4>NOTE</h4> When an operation is not
 * used with and {@link OperationJob} the user is responsible to call validate first and execute only on successed
 * validation.
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 23.02.2012
 * @see OperationJob
 */
public interface IOperation {

  String getOperationName();

  /**
   * throw a {@link IllegalArgumentException} if the operation should not be executed and has invalid parameters.
   * 
   * @throws IllegalArgumentException
   */
  void validate() throws IllegalArgumentException;

  /**
   * Usually the run method is called of a job implementation.
   * 
   * @param monitor
   *          to provide progress information and observe the cancel state.
   * @param workingCopyManager
   *          the working copy manager to add every changed {@link IJavaElement}.
   *          The working copy manager ensures a working copy for any added compilation unit and stores on success or
   *          discards when the operation failed.
   * @throws CoreException
   * @throws IllegalArgumentException
   */
  void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException;

}
