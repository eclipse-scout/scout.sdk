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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>OperationAction</h3> The operation action is used when an action is needed to be added to a {@link MenuManager}.
 * The action will
 * schedule an {@link OperationJob} which runs with the workspace scheduling rule.
 * 
 * @see OperationJob
 */
public abstract class AbstractOperationAction extends AbstractScoutHandler {

  private Collection<IOperation> m_operations;

  public AbstractOperationAction(String label, ImageDescriptor image, String keyStroke, boolean multiSelectSupported, Category cat) {
    super(label, image, keyStroke, multiSelectSupported, cat);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    if (m_operations != null && m_operations.size() > 0) {
      OperationJob job = new OperationJob(m_operations);
      job.schedule();
    }
    return null;
  }

  public Collection<IOperation> getOperations() {
    return m_operations;
  }

  public void setOperation(IOperation operation) {
    setOperations(Arrays.asList(new IOperation[]{operation}));
  }

  public void setOperations(Collection<IOperation> operations) {
    m_operations = operations;
  }
}
