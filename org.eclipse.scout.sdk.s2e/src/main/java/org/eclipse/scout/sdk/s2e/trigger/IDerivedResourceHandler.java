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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.scout.sdk.s2e.job.AbstractResourceBlockingJob;
import org.eclipse.scout.sdk.s2e.operation.IOperation;

/**
 * <h3>{@link IDerivedResourceHandler}</h3><br>
 * Represents a handler to create the content of derived resources in memory.<br>
 * <b>Please note:</b><br>
 * Unlike {@link IOperation}s, {@link IDerivedResourceHandler}s do not run within an appropriate {@link ISchedulingRule}
 * and may therefore not directly write any resources! Please schedule an {@link AbstractResourceBlockingJob} to persist
 * the created contents afterwards.
 *
 * @author Ivan Motsch
 * @since 5.1
 */
public interface IDerivedResourceHandler {

  /**
   * @return The name of the handler to be shown to the user.
   */
  String getName();

  /**
   * throw a {@link IllegalArgumentException} if the handler should not be executed and has invalid parameters
   */
  void validate();

  /**
   * Execute the derived resource handler.
   *
   * @param monitor
   *          to provide progress information and observe the cancel state.
   * @throws CoreException
   */
  void run(IProgressMonitor monitor) throws CoreException;

}
