/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.environment;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link RunnableJob}</h3> Eclipse Job to execute a java {@link Runnable}.
 *
 * @since 5.2.0
 */
public class RunnableJob extends AbstractJob {

  private final Runnable m_runnable;

  public RunnableJob(String name, Runnable runnable) {
    super(name);
    m_runnable = Ensure.notNull(runnable);
  }

  @Override
  protected void execute(IProgressMonitor monitor) {
    if (monitor.isCanceled()) {
      return;
    }
    m_runnable.run();
  }
}
