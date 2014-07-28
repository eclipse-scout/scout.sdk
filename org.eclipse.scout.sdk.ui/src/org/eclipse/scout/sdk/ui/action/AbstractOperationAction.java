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

import java.util.LinkedList;
import java.util.List;

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

  private final List<IOperation> m_ops;

  public AbstractOperationAction(String label, ImageDescriptor image, String keyStroke, boolean multiSelectSupported, Category cat) {
    super(label, image, keyStroke, multiSelectSupported, cat);
    m_ops = new LinkedList<IOperation>();
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    if (getOperationCount() > 0) {
      OperationJob job = new OperationJob(m_ops);
      job.schedule();
    }
    return null;
  }

  public void setOperation(IOperation operation) {
    m_ops.clear();
    m_ops.add(operation);
  }

  public void addOperation(IOperation operation) {
    m_ops.add(operation);
  }

  public int getOperationCount() {
    return m_ops.size();
  }
}
