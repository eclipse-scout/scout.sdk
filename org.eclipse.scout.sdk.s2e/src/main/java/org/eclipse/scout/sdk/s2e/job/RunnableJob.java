/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.job;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;

/**
 * <h3>{@link RunnableJob}</h3> Eclipse Job to execute a java {@link Runnable}.
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class RunnableJob extends AbstractJob {

  private final Runnable m_runnable;

  public RunnableJob(String name, Runnable runnable) {
    super(name);
    m_runnable = Validate.notNull(runnable);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      if (monitor.isCanceled()) {
        return Status.CANCEL_STATUS;
      }

      m_runnable.run();
      return Status.OK_STATUS;
    }
    catch (Exception e) {
      return new ScoutStatus("Exception executing runnable '" + m_runnable.getClass().getName() + "'.", e);
    }
  }
}