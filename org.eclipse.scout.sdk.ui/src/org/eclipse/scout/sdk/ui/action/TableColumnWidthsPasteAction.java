package org.eclipse.scout.sdk.ui.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.Reader;
import java.util.HashMap;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
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

  private IType m_type;
  private HashMap<String, Integer> m_widthsMap;
  private String m_errorTxt;

  public TableColumnWidthsPasteAction() {
    super(Texts.get("Action_PasteColumnWidths"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TableColumn), "CTRL+V", false, Category.IMPORT);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) {
    changeColumnWidths(m_type, m_widthsMap);
    return null;
  }

  @Override
  public boolean isVisible() {
    return isEditable(m_type) && m_widthsMap != null;
  }

  public void init(IPage origin) {
    determineTableType(origin);
  }

  private boolean fastDetection(String content) {
    try {
      Object identifier = TypeUtility.getFieldConstant(TypeUtility.getType(RuntimeClasses.CopyWidthsOfColumnsMenu).getField(TABLE_MENU_COLUMN_COPY_CLIPBOARD_IDENTIFIER));
      if (identifier instanceof String) {
        return content.startsWith(identifier.toString());
      }
      return false;
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
  private HashMap<String, Integer> parseContent(String content) {
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
            columnWidth = 0;
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

  private void determineTableType(IPage page) {
    if (page instanceof ColumnTablePage) {
      // it's a column table page, so get the declaring type directly
      m_type = ((ColumnTablePage) page).getColumnDeclaringType();
    }
    else if (page instanceof PageWithTableNodePage) {
      // it's a abstract page with table
      IType[] tables = ScoutTypeUtility.getTables(((PageWithTableNodePage) page).getType());
      if (tables.length > 0) {
        m_type = tables[0];
      }
    }
    else {
      // it's something else (table node page or table field node page)

      if (page instanceof TableFieldNodePage) {
        // table field node page
        IType[] tables = ScoutTypeUtility.getTables(((TableFieldNodePage) page).getType());
        if (tables.length > 0) {
          m_type = tables[0];
        }
      }
      else if (page instanceof TableNodePage) {
        // table node page
        m_type = ((TableNodePage) page).getType();
      }
    }

    String content = getStringFromClipboard();
    if (content != null && fastDetection(content)) {
      m_widthsMap = parseContent(content);
      if (m_type != null && m_widthsMap != null) {
        // verify that correct table is selected (according to content of clipboard)
        final String tableTypeClassName = m_type.getFullyQualifiedName();
        for (String clazz : m_widthsMap.keySet()) {
          if (!clazz.startsWith(tableTypeClassName)) {
            m_type = null;
            m_widthsMap = null;
            break;
          }
        }
      }
    }
  }
}
