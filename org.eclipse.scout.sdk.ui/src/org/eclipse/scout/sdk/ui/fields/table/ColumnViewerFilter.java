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
package org.eclipse.scout.sdk.ui.fields.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.fields.table.IColumnProvider;
import org.eclipse.scout.sdk.ui.internal.fields.table.IColumnProvider.LayoutUpdateListener;
import org.eclipse.scout.sdk.ui.internal.fields.table.TableColumnProvider;
import org.eclipse.scout.sdk.ui.internal.fields.table.TreeColumnProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

/**
 *
 */
public class ColumnViewerFilter extends Composite {

  private Button m_resetButton;
  private Text[] m_filterFields;
  private IColumnProvider m_columnProvider;
  private List<Object> m_disabledRows = new ArrayList<Object>();
  private LayoutUpdateListener m_layoutUpdateListener = new LayoutUpdateListener() {
    @Override
    public void updateLayout() {
      layout();
    }
  };

  /**
   * @param parent
   * @param style
   */
  public ColumnViewerFilter(Composite parent) {
    super(parent, SWT.DOUBLE_BUFFERED);
    setBackground(parent.getBackground());
    m_resetButton = new Button(this, SWT.PUSH | SWT.FLAT);
    m_resetButton.setText("Reset");
    m_resetButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (m_filterFields != null) {
          for (Text t : m_filterFields) {
            t.setText("");
          }
        }
      }
    });
    updateFilterFields(this);
    //layout
    setLayout(new P_FilterComponentLayout());
  }

  public void setViewer(ColumnViewer columnViewer) {
    if (m_columnProvider != null) {
      m_columnProvider.dispose();
    }
    if (columnViewer instanceof TableViewer) {
      m_columnProvider = new TableColumnProvider((TableViewer) columnViewer);
    }
    else if (columnViewer instanceof TreeViewer) {
      m_columnProvider = new TreeColumnProvider((TreeViewer) columnViewer);
    }
    else {
      throw new IllegalArgumentException("viewer must be instanceof TableViwer or TreeViewer");
    }

    m_columnProvider.addLayoutUpdateListener(m_layoutUpdateListener);

    updateFilterFields(this);
  }

  /**
   * @param columnViewerFilter
   */
  private void updateFilterFields(ColumnViewerFilter columnViewerFilter) {
    if (m_filterFields != null) {
      // dispose old text fields and backup the filter text to restore it afterwards in the new fields
      for (Text t : m_filterFields) {
        t.dispose();
      }
    }

    if (m_columnProvider != null) {
      m_filterFields = new Text[m_columnProvider.getColumnCount()];
      for (int i = 0; i < m_filterFields.length; i++) {
        Text filterField = new Text(this, SWT.BORDER);
        filterField.addModifyListener(new P_FilterModifyListener(i));
        m_filterFields[i] = filterField;
      }
    }
  }

  private void handleFilterModified(int index) {
    if (m_columnProvider != null) {
      // update filters
      ArrayList<ViewerFilter> filters = new ArrayList<ViewerFilter>();
      // backup old filters
      for (ViewerFilter filter : m_columnProvider.getViewer().getFilters()) {
        if (!(filter instanceof P_ViewerFilter)) {
          filters.add(filter);
        }
      }
      for (int i = 0; i < m_filterFields.length; i++) {
        String text = m_filterFields[i].getText();
        if (!StringUtility.isNullOrEmpty(text)) {
          filters.add(new P_ViewerFilter(text, i));
        }
      }
      try {
        m_columnProvider.getViewer().getControl().setRedraw(false);
        m_columnProvider.getViewer().setFilters(filters.toArray(new ViewerFilter[filters.size()]));
        m_columnProvider.expandAll();
      }
      finally {
        m_columnProvider.getViewer().getControl().setRedraw(true);
      }
    }
  }

  private class P_FilterModifyListener implements ModifyListener {
    private int m_columnIndex;

    public P_FilterModifyListener(int columnIndex) {
      m_columnIndex = columnIndex;
    }

    @Override
    public void modifyText(ModifyEvent e) {
      handleFilterModified(m_columnIndex);
    }
  } // end class P_FilterModifyListener

  private class P_ViewerFilter extends ViewerFilter {

    private final String m_pattern;
    private final int m_columnIndex;
    private HashMap<Object, Boolean> m_visitedNodes;

    public P_ViewerFilter(String pattern, int columnIndex) {
      m_columnIndex = columnIndex;
      m_pattern = pattern.toLowerCase() + "*";
      m_visitedNodes = new HashMap<Object, Boolean>();
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      return visitChildren(element);
    }

    private boolean visitChildren(Object node) {
      Boolean result = m_visitedNodes.remove(node);
      if (result == null) {
        String cellText = m_columnProvider.getCellText(node, m_columnIndex);
        result = CharOperation.match(m_pattern.toCharArray(), cellText.toCharArray(), false);
        m_visitedNodes.put(node, result);
      }
      if (result) {
        return true;
      }
      else {
        Object[] children = m_columnProvider.getChildren(node);
        for (Object child : children) {
          result = visitChildren(child);
          if (result) {
            return true;
          }
        }
      }
      return false;
    }
  } // end class P_ViewerFilter

  private class P_FilterComponentLayout extends Layout {
    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
      // height
      int height = m_resetButton.computeSize(SWT.DEFAULT, hHint).y + 2;
      for (Text t : m_filterFields) {
        height = Math.max(height, t.computeSize(SWT.DEFAULT, hHint).y + 2);
      }
      return new Point(wHint, height);
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
      Rectangle clientArea = composite.getClientArea();
      Point resetButtonSize = m_resetButton.computeSize(SWT.DEFAULT, clientArea.height - 2);
      int w = clientArea.width;
      Rectangle buttonBounds = new Rectangle(-1, clientArea.y + 1, Math.min(clientArea.width, resetButtonSize.x), clientArea.height - 2);
      buttonBounds.x = clientArea.x + clientArea.width - buttonBounds.width - 2;
      m_resetButton.setBounds(buttonBounds);
      w -= buttonBounds.width;
      if (m_columnProvider != null) {
        int x = m_columnProvider.getXOffset();
        int[] columnWidths = m_columnProvider.getColumnWidths();
        for (int i = 0; i < columnWidths.length; i++) {
          Rectangle bounds = new Rectangle(x + 1, clientArea.y + 1, columnWidths[i] - 2, clientArea.height - 2);
          if ((bounds.x + bounds.width) <= 0) {
            m_filterFields[i].setVisible(false);
          }
          else if (w < 0) {
            m_filterFields[i].setVisible(false);
          }
          else {
            m_filterFields[i].setVisible(true);
            bounds.width = Math.min(w, bounds.width);
            m_filterFields[i].setBounds(bounds);
          }
          w -= bounds.width;
          x += columnWidths[i];
        }
      }
      composite.update();
    }
  } // end P_FilterComponentLayout

}
