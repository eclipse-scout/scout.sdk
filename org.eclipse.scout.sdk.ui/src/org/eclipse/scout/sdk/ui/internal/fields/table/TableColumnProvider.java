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
package org.eclipse.scout.sdk.ui.internal.fields.table;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;

/**
 *
 */
public class TableColumnProvider extends AbstractColumnProvider {

  private TableViewer m_tableViewer;

  public TableColumnProvider(TableViewer tableViewer) {
    m_tableViewer = tableViewer;

  }

  @Override
  public void dispose() {

  }

  @Override
  public TableViewer getViewer() {
    return m_tableViewer;
  }

  @Override
  public void expandAll() {

  }

  @Override
  public int getColumnCount() {
    return getViewer().getTable().getColumnCount();
  }

  @Override
  public Object[] getChildren(Object node) {
    return ((ITreeContentProvider) getViewer().getContentProvider()).getChildren(node);
  }

  @Override
  public String getCellText(Object element, int columnIndex) {
    IBaseLabelProvider provider = getViewer().getLabelProvider();
    if (provider instanceof ITableLabelProvider) {
      return ((ITableLabelProvider) provider).getColumnText(element, columnIndex);
    }
    return "";
  }

  @Override
  public int[] getColumnWidths() {
    TableColumn[] columns = getViewer().getTable().getColumns();
    int[] widths = new int[columns.length];
    for (int i = 0; i < columns.length; i++) {
      widths[i] = columns[i].getWidth();
    }
    return widths;
  }

  @Override
  public int getXOffset() {
    return -getViewer().getTable().getClientArea().x;
  }

}
