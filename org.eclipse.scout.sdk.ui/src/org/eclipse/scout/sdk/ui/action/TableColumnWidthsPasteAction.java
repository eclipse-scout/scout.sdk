package org.eclipse.scout.sdk.ui.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.Reader;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.TableFieldNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageWithTableNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.ColumnTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.TableNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class TableColumnWidthsPasteAction extends AbstractScoutHandler {

  private static final String TABLE_MENU_COLUMN_COPY_CLIPBOARD_IDENTIFIER = "COLUMN_COPY_CLIPBOARD_IDENTIFIER";
  private static final String COLUMN_WIDTH_METHOD_NAME = "getConfiguredWidth";

  public TableColumnWidthsPasteAction() {
    super(Texts.get("Action_PasteColumnWidths"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TableColumn), "CTRL+V", false, Category.IMPORT);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) {
    String content = getStringFromClipboard();
    if (content != null && selection != null && selection.length == 1) {
      if (!fastDetection(content)) {
        showInfoMessageBox(shell, Texts.get("Action_PasteColumnWidths_InvalidClipboard"));
        return null;
      }

      HashMap<String, Integer> map = parseContent(content, shell);

      if (map != null) {
        // determine columns table
        IType tableType = determineTableType(selection[0], shell, map.keySet());
        // correct abstract page selected?
        if (tableType != null) {
          changeColumnWidths(tableType, map);
        }
      }
    }
    return null;
  }

  private boolean fastDetection(String content) {
    try {
      String identifier = TypeUtility.getFieldValueAsString(TypeUtility.getType(RuntimeClasses.CopyWidthsOfColumnsMenu).getField(TABLE_MENU_COLUMN_COPY_CLIPBOARD_IDENTIFIER));
      return content.startsWith(identifier);
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("Unable to get the identification constant from the scout class", e);
      return true; // try to parse it anyway
    }
  }

  private String getStringFromClipboard() {
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Reader reader = DataFlavor.stringFlavor.getReaderForText(clipboard.getContents(null));
      return IOUtility.getContent(reader);
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Format of content:
   * for each column: [class name]\t[width]\n
   * The first line contains the identifier, which will be ignored here
   * 
   * @param content
   *          Clipboard content
   * @return List of column names and their width
   */
  private HashMap<String, Integer> parseContent(String content, Shell s) {
    HashMap<String, Integer> map = new HashMap<String, Integer>();
    try {
      // clean content
      content = content.replaceAll("\r", "");

      // split lines
      String[] lines = content.split("\n");
      for (int i = 1; i < lines.length; i++) { // start with second row, because first row contains identifier
        String line = lines[i];
        if (StringUtility.hasText(line)) {
          String[] parts = line.split("\t");
          Integer columnWidth = NumberUtility.nvl(new Integer(parts[1]), 0);
          if (columnWidth < 0) {
            showInfoMessageBox(s, Texts.get("ColumnWidthPasteInvalidWidth", columnWidth.toString()));
            return null;
          }
          map.put(parts[0], columnWidth);
        }
      }

      return map;
    }
    catch (Exception e) {
      // no error log, because content could not be parsed
      // just no processing afterwards
      return null;
    }
  }

  private void showInfoMessageBox(Shell s, String message) {
    MessageBox msgBox = new MessageBox(s, SWT.OK | SWT.ICON_INFORMATION);
    msgBox.setMessage(message);
    msgBox.open();
  }

  private void changeColumnWidths(IType tableType, HashMap<String, Integer> map) {
    // for all columns within the table
    for (IType innerType : ScoutTypeUtility.getColumns(tableType)) {
      String className = innerType.getFullyQualifiedName();
      Integer columnWidth = map.get(className);
      // there is a corresponding entry in the clipboard for the current column?
      if (columnWidth != null) {
        IOperation op = new ConfigPropertyMethodUpdateOperation(innerType, COLUMN_WIDTH_METHOD_NAME, "  return " + columnWidth.toString() + ";", true);
        new OperationJob(op).schedule();
      }
    }
  }

  private IType determineTableType(IPage page, Shell s, Set<String> classNames) {
    IType tableType = null;

    if (page instanceof ColumnTablePage) {
      // it's a column table page, so get the declaring type directly
      tableType = ((ColumnTablePage) page).getColumnDeclaringType();
    }
    else if (page instanceof PageWithTableNodePage) {
      // it's a abstract page with table
      IType[] tables = ScoutTypeUtility.getTables(((PageWithTableNodePage) page).getType());
      if (tables.length > 0) {
        tableType = tables[0];
      }
      else {
        ScoutSdkUi.logInfo(Texts.get("ColumnWidthPasteNoTableInPage"));
        showInfoMessageBox(s, Texts.get("ColumnWidthPasteNoTableInPage"));
      }
    }
    else {
      // it's something else (table node page or table field node page)

      if (page instanceof TableFieldNodePage) {
        // table field node page
        IType[] tables = ScoutTypeUtility.getTables(((TableFieldNodePage) page).getType());
        if (tables.length > 0) {
          tableType = tables[0];
        }
        else {
          ScoutSdkUi.logInfo(Texts.get("ColumnWidthPasteNoTableInField"));
          showInfoMessageBox(s, Texts.get("ColumnWidthPasteNoTableInField"));
        }
      }
      else if (page instanceof TableNodePage) {
        // table node page
        tableType = ((TableNodePage) page).getType();
      }
      else {
        // the page does not belong to one of the supported pages
        ScoutSdkUi.logInfo(Texts.get("ColumnWidthPasteUnsupportedSelection"));
        showInfoMessageBox(s, Texts.get("ColumnWidthPasteUnsupportedSelection"));
      }
    }

    if (tableType != null) {
      // verify that correct table is selected (according to content of clipboard)
      final String tableTypeClassName = tableType.getFullyQualifiedName();
      for (String clazz : classNames) {
        if (!clazz.startsWith(tableTypeClassName)) {
          ScoutSdkUi.logInfo(Texts.get("ColumnWidthPasteDifferentTable", tableTypeClassName, clazz));
          showInfoMessageBox(s, Texts.get("ColumnWidthPasteDifferentTable", tableTypeClassName, clazz));
          tableType = null;
          break;
        }
      }
    }

    return tableType;
  }
}
