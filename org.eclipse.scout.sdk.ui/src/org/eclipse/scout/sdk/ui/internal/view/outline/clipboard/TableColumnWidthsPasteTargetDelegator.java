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
package org.eclipse.scout.sdk.ui.internal.view.outline.clipboard;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.sdk.ui.action.TableColumnWidthsPasteAction;
import org.eclipse.scout.sdk.ui.extensions.IPasteTargetDelegator;
import org.eclipse.scout.sdk.ui.extensions.executor.ExecutorExtensionPoint;
import org.eclipse.scout.sdk.ui.view.outline.OutlinePasteTargetEvent;

public class TableColumnWidthsPasteTargetDelegator implements IPasteTargetDelegator {
  @Override
  public boolean performPaste(OutlinePasteTargetEvent event) {
    IStructuredSelection selection = new StructuredSelection(event.getPage());
    ExecutorExtensionPoint.getExecutorFor(TableColumnWidthsPasteAction.class.getName()).run(event.getPage().getOutlineView().getSite().getShell(), selection, new ExecutionEvent());
    return false;
  }
}
