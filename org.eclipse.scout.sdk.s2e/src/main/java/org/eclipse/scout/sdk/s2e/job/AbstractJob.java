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
package org.eclipse.scout.sdk.s2e.job;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Extended job which adds the following features:
 * <ul>
 * <li>helper method to wait for a certain job family</li>
 * </ul>
 */
public abstract class AbstractJob extends Job {

  /**
   * Waits until all jobs of the given family are finished. This method will block the calling thread until all such
   * jobs have finished executing. If there are no jobs in the family that are currently waiting, running, or sleeping,
   * this method returns immediately.
   */
  public static void waitForJobFamily(final Object family) {
    boolean wasInterrupted = false;
    do {
      try {
        Job.getJobManager().join(family, null);
        wasInterrupted = false;
      }
      catch (OperationCanceledException e) {
        //nop
      }
      catch (InterruptedException e) {
        wasInterrupted = true;
      }
    }
    while (wasInterrupted);
  }

  public AbstractJob(String name) {
    super(name);
  }
}
