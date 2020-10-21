/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.derived;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <h3>{@link TriggerSelectedDerivedResourceHandler}</h3>
 * <p>
 * Only trigger in the selected workspace projects
 *
 * @since 5.1.0
 */
public class TriggerSelectedDerivedResourceHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    var messageBox = new MessageBox(HandlerUtil.getActiveShellChecked(event), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    messageBox.setMessage("This will update all derived resources in the selected scope.\nDepending on the size of your selection this can take several minutes.\nDo you really want to continue?");
    messageBox.setText("Do you really want to update the derived resources in the selected scope?");
    var answer = messageBox.open();
    if (answer == SWT.YES) {
      var selection = HandlerUtil.getCurrentSelection(event);
      var resourcesFromSelection = S2eUiUtils.getResourcesOfSelection(selection);
      if (!resourcesFromSelection.isEmpty()) {
        var mgr = S2ESdkActivator.getDefault().getDerivedResourceManager();
        if (mgr != null) {
          mgr.trigger(resourcesFromSelection);
        }
      }
    }
    return null;
  }
}
