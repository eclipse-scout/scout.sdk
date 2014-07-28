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
package org.eclipse.scout.sdk.ui.action;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;

/**
 * <h3>OperationAction</h3> The operation action is used when an action is needed to be added to a {@link MenuManager}.
 * The action will
 * schedule an {@link OperationJob} which runs with the workspace scheduling rule.
 *
 * @see OperationJob
 */
public class OperationAction extends Action {

  private final Collection<IOperation> m_operations;

  public OperationAction(String label, ImageDescriptor imageDescriptor, IOperation operation) {
    this(label, imageDescriptor, Arrays.asList(new IOperation[]{operation}));
  }

  public OperationAction(String label, ImageDescriptor imageDescriptor, Collection<IOperation> operations) {
    super(label, imageDescriptor);
    m_operations = operations;
  }

  @Override
  public void run() {
    OperationJob job = new OperationJob(m_operations);
    job.schedule();

  }

}
