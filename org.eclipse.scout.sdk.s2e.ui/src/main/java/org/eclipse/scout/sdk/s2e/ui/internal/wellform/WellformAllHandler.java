/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.wellform;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scout.sdk.s2e.operation.wellform.WellformAllOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <h3>{@link WellformAllHandler}</h3>
 *
 * @since 5.1.0
 */
public class WellformAllHandler extends AbstractHandler {

  @Override
  @SuppressWarnings("HardcodedLineSeparator")
  public Object execute(ExecutionEvent event) throws ExecutionException {
    var messageBox = new MessageBox(HandlerUtil.getActiveShellChecked(event), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    messageBox.setMessage("This will wellform all Scout classes.\nDepending on the size of your workspace this can take several minutes.\nDo you really want to continue?");
    messageBox.setText("Do you really want to wellform all Scout classes?");
    var answer = messageBox.open();
    if (answer == SWT.YES) {
      runInEclipseEnvironment(new WellformAllOperation());
    }
    return null;
  }
}
