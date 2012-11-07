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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.TreeColumn;

/**
 *
 */
public class TreeColumnProvider extends AbstractColumnProvider {

  private final TreeViewer m_viewer;
  private Listener m_listener = new P_ControlListener();

  public TreeColumnProvider(TreeViewer viewer) {
    m_viewer = viewer;
    attachListeners();
  }

  private void attachListeners() {
    m_viewer.getTree().getHorizontalBar().addListener(SWT.Selection, m_listener);
    for (TreeColumn col : m_viewer.getTree().getColumns()) {
      col.addListener(SWT.Resize, m_listener);
    }
  }

  @Override
  public void dispose() {
    m_viewer.getTree().getHorizontalBar().removeListener(SWT.Selection, m_listener);
  }

  @Override
  public TreeViewer getViewer() {
    return m_viewer;
  }

  @Override
  public void expandAll() {
    getViewer().expandAll();
  }

  @Override
  public int getColumnCount() {
    return getViewer().getTree().getColumnCount();
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
    TreeColumn[] columns = getViewer().getTree().getColumns();
    int[] widths = new int[columns.length];
    for (int i = 0; i < columns.length; i++) {
      widths[i] = columns[i].getWidth();
    }
    return widths;
  }

  @Override
  public int getXOffset() {
    int offset = -getViewer().getTree().getHorizontalBar().getSelection();
    return offset;
  }

  private class P_ControlListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Selection:
          if (event.widget instanceof ScrollBar) {
            scheduleUpdateLayout();
          }
          break;
        case SWT.Resize:
          scheduleUpdateLayout();
          break;
      }
    }
  }
}
