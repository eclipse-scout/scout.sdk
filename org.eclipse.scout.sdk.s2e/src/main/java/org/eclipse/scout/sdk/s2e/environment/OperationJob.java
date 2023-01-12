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

import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.runWithWorkingCopyManager;

import java.util.function.BiConsumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link OperationJob}</h3>
 *
 * @since 7.0.0
 */
public class OperationJob extends AbstractJob {

  private final BiConsumer<? super EclipseEnvironment, ? super EclipseProgress> m_operation;

  protected OperationJob(BiConsumer<? super EclipseEnvironment, ? super EclipseProgress> operation, String jobName) {
    super(Ensure.notNull(jobName));
    m_operation = Ensure.notNull(operation);
  }

  public static String getJobName(Object op) {
    return CoreUtils.toStringIfOverwritten(op).orElse("");
  }

  @Override
  protected void execute(IProgressMonitor monitor) {
    var workForCommit = 1;
    var workForOperation = 10000;
    var progress = EclipseEnvironment
        .toScoutProgress(monitor)
        .init(workForOperation + workForCommit, getName());

    runWithWorkingCopyManager(() -> executeWithWorkingCopyManager(progress.newChild(workForOperation)), () -> progress.newChild(workForCommit).monitor());
  }

  protected void executeWithWorkingCopyManager(EclipseProgress eclipseProgress) {
    try (var env = new EclipseEnvironment()) {
      operation().accept(env, eclipseProgress);
    }
  }

  public BiConsumer<? super EclipseEnvironment, ? super EclipseProgress> operation() {
    return m_operation;
  }
}
