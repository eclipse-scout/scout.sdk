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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;

/**
 * <h3>OperationJob</h3> Use this class to ensure an operation is executed with the scheduling rule of workspace root.
 * This scheduling rule ensures not having simultaneously modifications on Java Types.
 */
public class ResourceBlockingOperationJob extends AbstractResourceBlockingJob {

  private final Collection<? extends IOperation> m_operations;

  public ResourceBlockingOperationJob(IOperation operation) {
    this(operation, (IResource[]) null);
  }

  public ResourceBlockingOperationJob(IOperation operation, IResource... resources) {
    this(operation == null ? Collections.emptyList() : Collections.singletonList(operation), resources);
  }

  public ResourceBlockingOperationJob(Collection<? extends IOperation> operations) {
    this(operations, (IResource[]) null);
  }

  public ResourceBlockingOperationJob(Collection<? extends IOperation> operations, IResource... resources) {
    this(cleanList(operations), resources);
  }

  protected ResourceBlockingOperationJob(List<? extends IOperation> cleanOperations, IResource[] resources) {
    super(getJobName(cleanOperations), resources);
    m_operations = cleanOperations;
  }

  protected static <T> List<T> cleanList(Collection<T> c) {
    if (c == null || c.isEmpty()) {
      return Collections.emptyList();
    }
    List<T> result = new ArrayList<>(c.size());
    for (T element : c) {
      if (element != null) {
        result.add(element);
      }
    }
    return result;
  }

  /**
   * @param operations
   *          May not contain {@code null} entries and may not be {@code null}.
   * @return The name for this job based on the given operations
   */
  protected static String getJobName(Collection<? extends IOperation> operations) {
    if (operations.isEmpty()) {
      return "";
    }
    StringBuilder nameBuilder = new StringBuilder();
    Iterator<? extends IOperation> iterator = operations.iterator();
    appendName(iterator.next(), nameBuilder);
    while (iterator.hasNext()) {
      nameBuilder.append(", ");
      appendName(iterator.next(), nameBuilder);
    }
    return nameBuilder.toString();
  }

  protected static void appendName(IOperation op, StringBuilder nameBuilder) {
    String itOpName = op.getOperationName();
    if (StringUtils.isBlank(itOpName)) {
      SdkLog.warning("operation '{}' does not have a name.", op.getClass().getName());
      itOpName = "Missing operation name.";
    }
    nameBuilder.append(itOpName);
  }

  @Override
  protected void validate() {
    for (IOperation op : m_operations) {
      op.validate();
    }
  }

  @Override
  protected final void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    final SubMonitor progress = SubMonitor.convert(monitor, getName(), m_operations.size() * 100);
    if (progress.isCanceled()) {
      return;
    }
    for (IOperation op : m_operations) {
      op.run(progress.newChild(100), workingCopyManager);
    }
  }
}
