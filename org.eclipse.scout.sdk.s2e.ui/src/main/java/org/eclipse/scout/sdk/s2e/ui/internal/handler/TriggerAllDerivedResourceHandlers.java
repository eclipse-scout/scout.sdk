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

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <h3>{@link TriggerAllDerivedResourceHandlers}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class TriggerAllDerivedResourceHandlers extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    MessageBox messageBox = new MessageBox(HandlerUtil.getActiveShellChecked(event), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    messageBox.setMessage("This will update all derived resources.\nDepending on the size of your workspace this can take several minutes.\nDo you really want to continue?");
    messageBox.setText("Do you really want to update all derived resources?");
    int answer = messageBox.open();
    if (answer == SWT.YES) {
      Set<IResource> workspaceroot = Collections.<IResource> singleton(ResourcesPlugin.getWorkspace().getRoot());
      ScoutSdkCore.getDerivedResourceManager().trigger(workspaceroot);
    }
    return null;
  }
}
