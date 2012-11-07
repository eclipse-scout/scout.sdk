package org.eclipse.scout.sdk.ui.internal.view.outline.clipboard;

import org.eclipse.scout.sdk.ui.action.TableColumnWidthsPasteAction;
import org.eclipse.scout.sdk.ui.extensions.IPasteTargetDelegator;
import org.eclipse.scout.sdk.ui.view.outline.OutlinePasteTargetEvent;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

public class TableColumnWidthsPasteTargetDelegator implements IPasteTargetDelegator {

  /**
   * Name of method containing the column width in Scout
   */

  @Override
  public boolean performPaste(OutlinePasteTargetEvent event) {
    TableColumnWidthsPasteAction action = new TableColumnWidthsPasteAction();
    action.execute(event.getPage().getOutlineView().getSite().getShell(), new IPage[]{event.getPage()}, null);
    /*String content = getStringFromTransferable(event.getTransferData());
    if (content != null) {
      if (!fastDetection(content)) {
        return false;
      }

      HashMap<String, Integer> map = parseContent(content, event.getPage());

      // parsing was successful?
      if (map != null) {
        // determine columns table
        IType tableType = determineTableType(event.getPage(), map.keySet());
        // correct abstract page selected?
        if (tableType != null) {
          changeColumnWidths(tableType, map);
        }
      }
    }*/

    return false;
  }
}
