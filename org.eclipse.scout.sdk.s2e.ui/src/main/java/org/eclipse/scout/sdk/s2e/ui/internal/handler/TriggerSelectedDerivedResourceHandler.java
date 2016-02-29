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

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <h3>{@link TriggerSelectedDerivedResourceHandler}</h3>
 * <p>
 * Only trigger in the selected workspace projects
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class TriggerSelectedDerivedResourceHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    MessageBox messageBox = new MessageBox(HandlerUtil.getActiveShellChecked(event), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    messageBox.setMessage("This will update all derived resources in the selected scope.\nDepending on the size of your selection this can take several minutes.\nDo you really want to continue?");
    messageBox.setText("Do you really want to update the derived resources in the selected scope?");
    int answer = messageBox.open();
    if (answer == SWT.YES) {
      ISelection selection = HandlerUtil.getCurrentSelection(event);
      Set<IResource> resourcesFromSelection = S2eUiUtils.getResourcesOfSelection(selection);
      if (!resourcesFromSelection.isEmpty()) {
        ScoutSdkCore.getDerivedResourceManager().trigger(resourcesFromSelection);
      }
    }
    return null;
  }
}
