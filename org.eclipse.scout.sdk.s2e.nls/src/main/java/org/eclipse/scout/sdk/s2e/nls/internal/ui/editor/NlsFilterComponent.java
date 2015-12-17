/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * <h4>NlsFilterComponent</h4>
 */
public class NlsFilterComponent extends Composite {

  private TableViewer m_tableViewer;
  private final Button m_resetButton;
  private final HashMap<Language, Text> m_filterFields;

  public NlsFilterComponent(Composite parent) {
    super(parent, SWT.DOUBLE_BUFFERED);
    m_filterFields = new HashMap<>();
    setBackground(parent.getBackground());
    m_resetButton = new Button(this, SWT.PUSH | SWT.FLAT);
    m_resetButton.setText("Reset");
    m_resetButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        for (Text t : m_filterFields.values()) {
          t.setText("");
        }
      }
    });
    updateFilterFields();
    setLayout(new P_FilterComponentLayout());
  }

  public void setTableViewer(TableViewer tableViewer) {
    m_tableViewer = tableViewer;
    m_tableViewer.getTable().addListener(SWT.Paint, new Listener() {
      @Override
      public void handleEvent(Event event) {
        layout();
      }
    });
    updateFilterFields();
  }

  public void columnsChanged() {
    updateFilterFields();
  }

  protected void updateFilterFields() {
    Map<Language, String> oldContents = new HashMap<>(m_filterFields.size());
    if (!m_filterFields.isEmpty()) {
      // dispose old text fields and backup the filter text to restore it afterwards in the new fields
      for (Entry<Language, Text> entry : m_filterFields.entrySet()) {
        oldContents.put(entry.getKey(), entry.getValue().getText());
        entry.getValue().dispose();
      }
      m_filterFields.clear();
    }

    if (m_tableViewer != null && m_tableViewer.getTable().getColumnCount() > NlsTable.AMOUNT_UTILITY_COLS) {
      TableColumn[] columns = m_tableViewer.getTable().getColumns();
      for (int i = NlsTable.INDEX_COLUMN_KEYS; i < columns.length; i++) {
        Language l = (Language) columns[i].getData(NlsTable.LANGUAGE_COLUMN_ID);
        Text filterField = new Text(this, SWT.BORDER);
        filterField.addModifyListener(new P_FilterModifyListener());

        // restore old filter text
        String oldText = oldContents.get(l);
        if (oldText != null) {
          filterField.setText(oldText);
        }

        m_filterFields.put(l, filterField);
      }
    }
  }

  protected void handleFilterModified() {
    // update filters
    List<ViewerFilter> filters = new ArrayList<>();
    // backup old filters
    for (ViewerFilter filter : m_tableViewer.getFilters()) {
      if (!(filter instanceof P_ViewerFilter)) {
        filters.add(filter);
      }
    }
    for (Entry<Language, Text> e : m_filterFields.entrySet()) {
      if (e.getValue().getText().length() > 0) {
        filters.add(new P_ViewerFilter(e.getKey(), e.getValue().getText()));
      }
    }
    try {
      m_tableViewer.getTable().setRedraw(false);
      m_tableViewer.setFilters(filters.toArray(new ViewerFilter[filters.size()]));
    }
    finally {
      m_tableViewer.getTable().setRedraw(true);
    }
  }

  private final class P_FilterModifyListener implements ModifyListener {
    @Override
    public void modifyText(ModifyEvent e) {
      handleFilterModified();
    }
  }

  private final class P_ViewerFilter extends ViewerFilter {

    private final String m_pattern;
    private final Language m_lang;

    private P_ViewerFilter(Language lang, String pattern) {
      m_lang = lang;
      m_pattern = pattern.toLowerCase() + "*";

    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      String text;
      INlsEntry nlsEntry = (INlsEntry) element;
      if (m_lang == Language.LANGUAGE_KEY) {
        text = nlsEntry.getKey();
      }
      else {
        text = nlsEntry.getTranslation(m_lang);
      }
      if (text == null) {
        text = "";
      }
      text = text.replace("&", "");
      return CharOperation.match(m_pattern.toCharArray(), text.toCharArray(), false);

    }
  } // end class P_ViewerFilter

  private final class P_FilterComponentLayout extends Layout {
    @Override
    protected Point computeSize(Composite composite, int hint, int hint2, boolean flushCache) {
      // height
      int height = m_resetButton.computeSize(SWT.DEFAULT, hint2).y + 2;
      for (Text t : m_filterFields.values()) {
        height = Math.max(height, t.computeSize(SWT.DEFAULT, hint2).y + 2);
      }
      return new Point(hint, height);
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
      Rectangle parentBounds = composite.getClientArea();
      TableColumn[] columns = m_tableViewer.getTable().getColumns();
      int[] colOrder = m_tableViewer.getTable().getColumnOrder();
      int x = -m_tableViewer.getTable().getHorizontalBar().getSelection();
      for (int i = 0; i < columns.length; i++) {
        TableColumn column = columns[colOrder[i]];
        if (i == NlsTable.AMOUNT_UTILITY_COLS) {
          // layout button
          m_resetButton.setBounds(1, 1, x - 2, parentBounds.height - 2);
        }
        if (i >= NlsTable.AMOUNT_UTILITY_COLS) {
          Language lang = (Language) column.getData(NlsTable.LANGUAGE_COLUMN_ID);
          Text text = m_filterFields.get(lang);
          if (text != null) {
            text.setBounds(x + 1, 1, column.getWidth() - 2, parentBounds.height - 2);
          }
        }
        x += column.getWidth();
      }
      composite.update();
    }
  } // end P_FilterComponentLayout

}
