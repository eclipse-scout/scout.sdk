/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
