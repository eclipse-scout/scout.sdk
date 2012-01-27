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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;

@Deprecated
public class LegacyOperationAction extends Action {
  private final Collection<IOperation> m_operations;

  public LegacyOperationAction(String label, ImageDescriptor imageDescriptor, IOperation operation) {
    this(label, imageDescriptor, Arrays.asList(new IOperation[]{operation}));
  }

  public LegacyOperationAction(String label, ImageDescriptor imageDescriptor, Collection<IOperation> operations) {
    super(label, imageDescriptor);
    m_operations = operations;
  }

  @Override
  public void run() {
    OperationJob job = new OperationJob(m_operations);
    job.schedule();
  }
}
