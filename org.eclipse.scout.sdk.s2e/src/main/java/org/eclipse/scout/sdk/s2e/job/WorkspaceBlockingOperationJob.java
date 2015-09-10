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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.workspace.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.workspace.IWorkspaceBlockingOperation;

/**
 * <h3>OperationJob</h3> Use this class to ensure an operation is executed with the scheduling rule of workspace root.
 * This scheduling rule ensures not having simultaneously modifications on Java Types.
 */
public class WorkspaceBlockingOperationJob extends AbstractWorkspaceBlockingJob {

  private final List<? extends IWorkspaceBlockingOperation> m_operations;

  public WorkspaceBlockingOperationJob(IWorkspaceBlockingOperation... operations) {
    this(Arrays.asList(operations));
  }

  public WorkspaceBlockingOperationJob(Collection<? extends IWorkspaceBlockingOperation> operations) {
    this(new ArrayList<>(operations));
  }

  private WorkspaceBlockingOperationJob(List<? extends IWorkspaceBlockingOperation> operations) {
    super("");
    m_operations = operations;
    updateJobName();
  }

  private void updateJobName() {
    synchronized (m_operations) {
      StringBuilder nameBuilder = new StringBuilder();
      if (!m_operations.isEmpty()) {
        Iterator<? extends IWorkspaceBlockingOperation> operationIt = m_operations.iterator();
        IWorkspaceBlockingOperation currentOperation = operationIt.next();
        String itOpName = currentOperation.getOperationName();
        if (itOpName == null) {
          S2ESdkActivator.logWarning("operation '" + currentOperation.getClass().getName() + "' does not have a name");
          itOpName = "Missing operation name.";
        }
        nameBuilder.append(itOpName);
        while (operationIt.hasNext()) {
          currentOperation = operationIt.next();
          itOpName = currentOperation.getOperationName();
          if (itOpName == null) {
            S2ESdkActivator.logWarning("operation '" + currentOperation.getClass().getName() + "' does not have a name");
            itOpName = "Missing operation name.";
          }
          nameBuilder.append(", ").append(itOpName);
        }
      }
      setName(nameBuilder.toString());
    }
  }

  @Override
  protected void validate() {
    synchronized (m_operations) {
      for (IWorkspaceBlockingOperation op : m_operations) {
        op.validate();
      }
    }
  }

  @Override
  protected final void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IWorkspaceBlockingOperation op : m_operations) {
      op.run(monitor, workingCopyManager);
    }
  }

}
