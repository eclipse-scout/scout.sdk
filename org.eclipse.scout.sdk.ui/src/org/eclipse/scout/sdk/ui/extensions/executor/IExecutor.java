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
package org.eclipse.scout.sdk.ui.extensions.executor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link IExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 08.10.2014
 */
public interface IExecutor {
  /**
   * Specifies if {@link #run(Shell, IStructuredSelection, ExecutionEvent)} of this {@link IExecutor} can be called.
   *
   * @param selection
   *          The {@link IStructuredSelection} which should be evaluated
   * @return <code>true</code> if this {@link IExecutor} can be run. <code>false</code> otherwise.
   */
  boolean canRun(IStructuredSelection selection);

  /**
   * Starts this {@link IExecutor}.
   * 
   * @param shell
   *          The {@link Shell} to use for the UI.
   * @param selection
   *          The {@link IStructuredSelection} that was active when the {@link IExecutor} was invoked.
   * @param event
   *          An {@link ExecutionEvent} containing all the information about the current state of the application.
   * @return the result of the execution. Reserved for future use, must be null.
   */
  Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event);
}
