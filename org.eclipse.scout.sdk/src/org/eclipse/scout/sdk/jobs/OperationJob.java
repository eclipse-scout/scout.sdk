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
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>OperationJob</h3> Use this class to ensure an operation is executed with the scheduling rule of workspace root.
 * This scheduling rule ensures not having simultaneously modifications on Java Types.
 */
public class OperationJob extends AbstractWorkspaceBlockingJob {

  private List<IOperation> m_operations;
  private Exception m_outherException;

  public OperationJob(IOperation... operations) {
    this((operations == null) ? Arrays.asList(new IOperation[0]) : Arrays.asList(operations));
  }

  public OperationJob(Collection<IOperation> operations) {
    super("");
    m_operations = new ArrayList<IOperation>(operations.size());
    if (operations != null) {
      for (IOperation op : operations) {
        m_operations.add(op);
      }
    }
    updateJobName();
  }

  @Override
  public boolean shouldSchedule() {
    m_outherException = new Exception();
    return super.shouldSchedule();
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
        try {
          op.validate();
        }
        catch (IllegalArgumentException e) {
          ScoutSdk.logError("validation of operation '" + op.getOperationName() + "' failed.", e);
          throw e;
        }
      }
    }
  }

  @Override
  protected void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IOperation[] allOps;
    synchronized (m_operations) {
      allOps = getAllOperations();
    }
    for (IOperation op : allOps) {
      try {
        op.run(monitor, workingCopyManager);
      }
      catch (CoreException e) {
        ScoutSdk.logError("Error occured while running Operation job.", e);
        ScoutSdk.logError("CALLED BY:", m_outherException);
        throw e;
      }
      catch (IllegalArgumentException e) {
        ScoutSdk.logError("Error occured while running Operation job.", e);
        ScoutSdk.logError("CALLED BY:", m_outherException);
        throw e;
      }
    }
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

}
