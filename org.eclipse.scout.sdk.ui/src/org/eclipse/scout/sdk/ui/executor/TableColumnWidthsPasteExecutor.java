/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.executor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.TableFieldNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageWithTableNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.ColumnTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.TableNodePage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.parser.IntegerPropertySourceParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link TableColumnWidthsPasteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class TableColumnWidthsPasteExecutor extends AbstractExecutor {

  private static final String TABLE_MENU_COLUMN_COPY_CLIPBOARD_IDENTIFIER = "COLUMN_COPY_CLIPBOARD_IDENTIFIER";
  private static final String COLUMN_WIDTH_METHOD_NAME = "getConfiguredWidth";
  private IType m_tableType;
  private String m_stringFromClipboard;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_stringFromClipboard = getStringFromClipboard();
    if (isInteresting(m_stringFromClipboard)) {
      m_tableType = getTableType(selection);
      return TypeUtility.exists(m_tableType);
    }
    return false;
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    Map<String, Integer> clipBoardContent = parseContent(m_stringFromClipboard);
    if (clipBoardContent != null && !clipBoardContent.isEmpty()) {
      try {
        changeColumnWidths(m_tableType, clipBoardContent, shell);
      }
      catch (CoreException e) {
        ScoutSdkUi.logError("Unable to change column widths.", e);
      }
    }
    else {
      showInfoMessageBox(shell, Texts.get("Action_PasteColumnWidths_InvalidClipboard"));
    }
    return null;
  }

  private void changeColumnWidths(IType tableType, Map<String, Integer> map, Shell shell) throws CoreException {
    // for all columns within the table
    List<IOperation> updateOps = new LinkedList<>();
    for (IType innerType : ScoutTypeUtility.getColumns(tableType)) {
      String className = innerType.getFullyQualifiedName();
      Integer columnWidth = map.get(className);
      // there is a corresponding entry in the clipboard for the current column?
      if (columnWidth != null && columnWidth >= 0) {
        ConfigurationMethod configurationMethod = ScoutTypeUtility.getConfigurationMethod(innerType, COLUMN_WIDTH_METHOD_NAME, TypeUtility.getSupertypeHierarchy(innerType), ConfigurationMethod.PROPERTY_METHOD, "INTEGER");
        ConfigPropertyUpdateOperation<Integer> updateOp = new ConfigPropertyUpdateOperation<>(configurationMethod, new IntegerPropertySourceParser());
        updateOp.setValue(columnWidth);
        updateOps.add(updateOp);
      }
      else if (columnWidth != null) {
        showInfoMessageBox(shell, Texts.get("ColumnWidthPasteInvalidWidth", columnWidth.toString()));
      }
    }
    if (updateOps.size() > 0) {
      new OperationJob(updateOps).schedule();
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
  private Map<String, Integer> parseContent(String content) {
    Map<String, Integer> map = new HashMap<>();
    try {
      // clean content
      content = content.replaceAll("\r", "");

      // split lines
      String[] lines = content.split("\n");
      for (int i = 1; i < lines.length; i++) { // start with second row, because first row contains identifier
        String line = lines[i];
        if (StringUtility.hasText(line)) {
          String[] parts = line.split("\t");
          Integer columnWidth = Integer.valueOf(parts[1]);
          if (columnWidth < 0) {
            columnWidth = 0;
          }
          map.put(parts[0], columnWidth);
        }
      }

      return map;
    }
    catch (Exception e) {
      ScoutSdkUi.logError("Unable to parse column widths", e);
      return null;
    }
  }

  private IType getTableType(IStructuredSelection selection) {
    Object page = selection.getFirstElement();
    IType ret = null;
    if (page instanceof ColumnTablePage) {
      // it's a column table page, so get the declaring type directly
      ret = ((ColumnTablePage) page).getColumnDeclaringType();
    }
    else if (page instanceof PageWithTableNodePage) {
      // it's a abstract page with table
      Set<IType> tables = ScoutTypeUtility.getTables(((PageWithTableNodePage) page).getType());
      if (tables.size() > 0) {
        ret = CollectionUtility.firstElement(tables);
      }
    }
    else if (page instanceof TableFieldNodePage) {
      // table field node page
      Set<IType> tables = ScoutTypeUtility.getTables(((TableFieldNodePage) page).getType());
      if (tables.size() > 0) {
        ret = CollectionUtility.firstElement(tables);
      }
    }
    else if (page instanceof TableNodePage) {
      // table node page
      ret = ((TableNodePage) page).getType();
    }
    return ret;
  }

  private void showInfoMessageBox(Shell s, String message) {
    MessageBox msgBox = new MessageBox(s, SWT.OK | SWT.ICON_INFORMATION);
    msgBox.setMessage(message);
    msgBox.open();
  }

  private boolean isInteresting(String content) {
    if (!StringUtility.hasText(content)) {
      return false;
    }

    try {
      Object identifier = TypeUtility.getFieldConstant(TypeUtility.getType(IRuntimeClasses.CopyWidthsOfColumnsMenu).getField(TABLE_MENU_COLUMN_COPY_CLIPBOARD_IDENTIFIER));
      if (identifier instanceof String) {
        return content.startsWith(identifier.toString());
      }
      return false;
    }
    catch (Exception e) {
      ScoutSdkUi.logError("Unable to get the identification constant from the scout class", e);
      return false;
    }
  }

  @SuppressWarnings("resource")
  private String getStringFromClipboard() {
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Reader reader = DataFlavor.stringFlavor.getReaderForText(clipboard.getContents(null));
      return IOUtility.getContent(reader, true);
    }
    catch (Exception e) {
      ScoutSdkUi.logError("Unable to get content from clipboard", e);
      return null;
    }
  }
}
