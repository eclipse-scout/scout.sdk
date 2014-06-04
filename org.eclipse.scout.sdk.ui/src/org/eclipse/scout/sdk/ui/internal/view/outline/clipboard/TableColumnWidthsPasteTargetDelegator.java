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

import org.eclipse.scout.sdk.ui.action.TableColumnWidthsPasteAction;
import org.eclipse.scout.sdk.ui.extensions.IPasteTargetDelegator;
import org.eclipse.scout.sdk.ui.view.outline.OutlinePasteTargetEvent;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

public class TableColumnWidthsPasteTargetDelegator implements IPasteTargetDelegator {
  @Override
  public boolean performPaste(OutlinePasteTargetEvent event) {
    TableColumnWidthsPasteAction action = new TableColumnWidthsPasteAction();
    action.init(event.getPage());
    action.execute(event.getPage().getOutlineView().getSite().getShell(), new IPage[]{event.getPage()}, null);
    return false;
  }
}
