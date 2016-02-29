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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;

/**
 * <h3>OperationJob</h3> Use this class to ensure an operation is executed with the scheduling rule of workspace root.
 * This scheduling rule ensures not having simultaneously modifications on Java Types.
 */
public class ResourceBlockingOperationJob extends AbstractResourceBlockingJob {

  private final Iterable<? extends IOperation> m_operations;

  public ResourceBlockingOperationJob(IOperation operation) {
    this(operation, (IResource[]) null);
  }

  public ResourceBlockingOperationJob(IOperation operation, IResource... resources) {
    this(Collections.singletonList(operation), resources);
  }

  public ResourceBlockingOperationJob(Iterable<? extends IOperation> operations) {
    this(operations, (IResource[]) null);
  }

  public ResourceBlockingOperationJob(Iterable<? extends IOperation> operations, IResource... resources) {
    super(getJobName(operations), resources);

    List<IOperation> ops = new ArrayList<>();
    if (operations != null) {
      Iterator<? extends IOperation> it = operations.iterator();
      while (it.hasNext()) {
        IOperation op = it.next();
        if (op != null) {
          ops.add(op);
        }
      }
    }
    m_operations = new ArrayList<>(ops);
  }

  private static String getJobName(Iterable<? extends IOperation> operations) {
    if (operations == null) {
      return "";
    }

    StringBuilder nameBuilder = new StringBuilder();
    for (IOperation op : operations) {
      if (op == null) {
        continue;
      }

      String itOpName = op.getOperationName();
      if (StringUtils.isBlank(itOpName)) {
        SdkLog.warning("operation '{}' does not have a name.", op.getClass().getName());
        itOpName = "Missing operation name.";
      }
      if (nameBuilder.length() > 0) {
        nameBuilder.append(", ");
      }
      nameBuilder.append(itOpName);
    }
    return nameBuilder.toString();
  }

  @Override
  protected void validate() {
    synchronized (m_operations) {
      for (IOperation op : m_operations) {
        op.validate();
      }
    }
  }

  @Override
  protected final void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IOperation op : m_operations) {
      op.run(monitor, workingCopyManager);
    }
  }

}
