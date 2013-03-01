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
