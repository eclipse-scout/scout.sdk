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
package org.eclipse.scout.sdk.ui.action.delete;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.outline.OutlineDeleteOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link OutlineDeleteAction}</h3>
 *
 * @author Matthias Villiger
 * @since 3.8.0 09.03.2012
 */
public class OutlineDeleteAction extends AbstractScoutHandler {

  private IType m_outlineType;

  public OutlineDeleteAction() {
    super(Texts.get("DeleteWithPopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolRemove), "Delete", true, Category.DELETE);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    box.setMessage(Texts.get("OutlineDeleteConfirmation"));
    if (box.open() == SWT.OK) {
      OutlineDeleteOperation op = new OutlineDeleteOperation(m_outlineType);
      OperationJob job = new OperationJob(op);
      job.schedule();
    }
    return null;
  }

  @Override
  public boolean isVisible() {
    return isEditable(m_outlineType);
  }

  public void setOutlineType(IType outlineType) {
    m_outlineType = outlineType;
  }
}
