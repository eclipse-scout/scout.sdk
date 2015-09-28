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
package org.eclipse.scout.sdk.s2e.ui.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.workspace.wellform.WellformAllOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <h3>{@link WellformAllHandler}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class WellformAllHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    MessageBox messageBox = new MessageBox(HandlerUtil.getActiveShellChecked(event), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    messageBox.setMessage("This will wellform all Scout classes.\nDepending on the size of your workspace this can take several minutes.\nDo you really want to continue?");
    messageBox.setText("Do you really want to wellform all Scout classes?");
    int answer = messageBox.open();
    if (answer == SWT.YES) {
      new ResourceBlockingOperationJob(new WellformAllOperation()).schedule();
    }
    return null;
  }
}
