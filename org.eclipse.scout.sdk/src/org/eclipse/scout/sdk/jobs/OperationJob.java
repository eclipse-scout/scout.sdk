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
package org.eclipse.scout.sdk.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>OperationJob</h3> Use this class to ensure an operation is executed with the scheduling rule of workspace root.
 * This scheduling rule ensures not having simultaneously modifications on Java Types.
 */
public class OperationJob extends AbstractWorkspaceBlockingJob {

  private List<IOperation> m_operations;

  public OperationJob() {
    this(Arrays.asList(new IOperation[0]));
  }

  public OperationJob(IOperation... operations) {
    this(operations == null ? null : Arrays.asList(operations));
  }

  public OperationJob(Collection<IOperation> operations) {
    super("");
    if (operations == null) {
      m_operations = new ArrayList<IOperation>();
    }
    else {
      m_operations = new ArrayList<IOperation>(operations);
    }
    updateJobName();
  }

  private void updateJobName() {
    synchronized (m_operations) {
      StringBuilder nameBuilder = new StringBuilder();
      if (!m_operations.isEmpty()) {
        Iterator<IOperation> operationIt = m_operations.iterator();
        IOperation currentOperation = operationIt.next();
        String itOpName = currentOperation.getOperationName();
        if (itOpName == null) {
          ScoutSdk.logWarning("operation '" + currentOperation.getClass().getName() + "' does not have a name");
          itOpName = "Missing operation name.";
        }
        nameBuilder.append(itOpName);
        while (operationIt.hasNext()) {
          currentOperation = operationIt.next();
          itOpName = currentOperation.getOperationName();
          if (itOpName == null) {
            ScoutSdk.logWarning("operation '" + currentOperation.getClass().getName() + "' does not have a name");
            itOpName = "Missing operation name.";
          }
          nameBuilder.append(", ").append(itOpName);
        }
      }
      setName(nameBuilder.toString());
    }
  }

  @Override
  protected void validate() throws IllegalArgumentException {
    synchronized (m_operations) {
      for (IOperation op : m_operations) {
        op.validate();
      }
    }
  }

  @Override
  protected final void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IOperation[] allOps;
    synchronized (m_operations) {
      allOps = getAllOperations();
    }
    for (IOperation op : allOps) {
      op.run(monitor, workingCopyManager);
    }
  }

  public Job scheduleAndJoin() throws InterruptedException {
    schedule();
    join();
    return this;
  }

  public void setOperations(Collection<IOperation> operations) {
    switch (getState()) {
      case RUNNING:
      case SLEEPING:
        throw new IllegalStateException("Job is already running!");
    }
    synchronized (m_operations) {
      m_operations.clear();
      m_operations.addAll(operations);
    }
    updateJobName();
  }

  public void addOperation(IOperation operation) throws IllegalStateException {
    switch (getState()) {
      case RUNNING:
      case SLEEPING:
        throw new IllegalStateException("Job is already running!");
    }
    synchronized (m_operations) {
      m_operations.add(operation);
    }
    updateJobName();
  }

  public IOperation[] getAllOperations() {
    synchronized (m_operations) {
      return m_operations.toArray(new IOperation[m_operations.size()]);
    }
  }

  public int getOperationCount() {
    return m_operations.size();
  }
}
