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
package org.eclipse.scout.nls.sdk.util.concurrent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * <h4>AbstractJob</h4> Is used to TODO
 */
public abstract class AbstractJob extends Job {

  protected Object[] args;

  /**
   * @param name
   */
  public AbstractJob(String name) {
    this(name, null);
  }

  public AbstractJob(String name, Object[] args) {
    super(name);
    this.args = args;
  }

  public IStatus runSync(IProgressMonitor monitor) {
    IStatus status = run(monitor);
    return status;
  }

}
