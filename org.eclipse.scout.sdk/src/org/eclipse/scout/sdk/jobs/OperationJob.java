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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * <h3>OperationJob</h3> Use this class to ensure an operation is executed with the scheduling rule of workspace root.
 * This scheduling rule ensures not having simultaneously modifications on Java Types.
 */
public class OperationJob extends AbstractWorkspaceBlockingJob {

  private List<IOperation> m_operations;

  public OperationJob(IOperation operation) {
    this(Arrays.asList(new IOperation[]{operation}));
  }

  public OperationJob(Collection<IOperation> operations) {
    super("");
    m_operations = new ArrayList<IOperation>();
    if (operations != null) {
      for (IOperation op : operations) {
        m_operations.add(op);
      }
    }
    String operationName = "";
    if (m_operations.size() == 1) {
      operationName = m_operations.get(0).getOperationName();
    }
    else if (m_operations.size() >= 2) {
      StringBuffer buf = new StringBuffer();
      for (IOperation o : m_operations) {
        buf.append(o.getOperationName());
        buf.append(", ");
      }
      buf.replace(0, 2, "");
      operationName = buf.toString();
    }
    if (operationName == null) {
      for (IOperation o : m_operations) {
        ScoutSdk.logWarning("operation '" + o.getClass().getName() + "' does not have a name");
      }
      operationName = "";
    }
    setName(operationName);
  }

  @Override
  protected void validate() throws IllegalArgumentException {
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

  @Override
  protected void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    for (IOperation op : m_operations) {
      try {
        op.run(monitor, workingCopyManager);
      }
      catch (CoreException e) {
        ScoutSdk.logError("Error occured while running Operation job.", e);
        throw e;
      }
      catch (IllegalArgumentException e) {
        ScoutSdk.logError("Error occured while running Operation job.", e);
        throw e;
      }
    }
  }

}
