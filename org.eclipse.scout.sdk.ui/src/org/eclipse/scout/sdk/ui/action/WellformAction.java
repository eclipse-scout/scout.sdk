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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link WellformAction}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.09.2010
 */
public class WellformAction extends AbstractOperationAction {

  public WellformAction() {
    super(Texts.get("Wellform"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolLoading), null, false, Category.UDPATE);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    box.setMessage(Texts.get("WellformConfirmationMessage"));
    if (box.open() == SWT.OK) {
      super.execute(shell, selection, event);
    }
    return null;
  }
}
