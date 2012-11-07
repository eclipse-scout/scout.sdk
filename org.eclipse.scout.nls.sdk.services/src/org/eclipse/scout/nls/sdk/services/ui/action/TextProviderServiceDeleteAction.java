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
package org.eclipse.scout.nls.sdk.services.ui.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scout.nls.sdk.services.operation.DeleteServiceNlsProjectOperation;
import org.eclipse.scout.nls.sdk.services.ui.page.TextServiceNodePage;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class TextProviderServiceDeleteAction extends AbstractScoutHandler {

  public TextProviderServiceDeleteAction() {
    super(Texts.get("DeleteWithPopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TextRemove), "Delete", true, Category.DELETE);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    if (selection.length == 1) {
      box.setMessage(Texts.get("DeleteTextProviderService"));
    }
    else {
      box.setMessage(Texts.get("DeleteTextProviderServicePlural"));
    }
    if (box.open() == SWT.OK) {
      for (IPage page : selection) {
        TextServiceNodePage p = (TextServiceNodePage) page;
        DeleteServiceNlsProjectOperation o = new DeleteServiceNlsProjectOperation(p.getType(), p.getScoutResource());
        o.validate();
        OperationJob job = new OperationJob(o);
        job.schedule();
      }
    }
    return null;
  }
}
