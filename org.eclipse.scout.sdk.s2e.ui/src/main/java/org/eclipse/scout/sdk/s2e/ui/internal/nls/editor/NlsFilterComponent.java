/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class NlsFilterComponent extends Composite {

  private final TableViewer m_tableViewer;
  private final NlsTableController m_controller;

  private final Button m_resetButton;
  private final Map<Language, Text> m_filterFields;

  protected NlsFilterComponent(Composite parent, TableViewer viewer, NlsTableController controller) {
    super(parent, SWT.DOUBLE_BUFFERED);
    m_controller = controller;
    setBackground(parent.getBackground());

    m_tableViewer = viewer;
    m_tableViewer.getTable().addListener(SWT.Paint, event -> layout());
    m_filterFields = new HashMap<>();

    m_resetButton = new Button(this, SWT.PUSH | SWT.FLAT);
    m_resetButton.setText("Clear");
    m_resetButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        for (Text t : m_filterFields.values()) {
          t.setText("");
        }
      }
    });
    columnsChanged();
    setLayout(new P_FilterComponentLayout());
  }

  public void columnsChanged() {
    Map<Language, String> oldContents = new HashMap<>(m_filterFields.size());
    if (!m_filterFields.isEmpty()) {
      // dispose old text fields and backup the filter text to restore it afterwards in the new fields
      for (Entry<Language, Text> entry : m_filterFields.entrySet()) {
        oldContents.put(entry.getKey(), entry.getValue().getText());
        entry.getValue().dispose();
      }
      m_filterFields.clear();
    }

    if (m_tableViewer != null && m_tableViewer.getTable().getColumnCount() >= NlsTableController.INDEX_COLUMN_KEYS) {
      TableColumn[] columns = m_tableViewer.getTable().getColumns();
      for (int i = NlsTableController.INDEX_COLUMN_KEYS; i < columns.length; i++) {
        Language l = m_controller.languageOfColumn(i);
        Text filterField = new Text(this, SWT.BORDER);

        // restore old filter text
        String oldText = oldContents.get(l);
        if (oldText != null) {
          filterField.setText(oldText);
        }

        filterField.addModifyListener(this::handleFilterModified);
        m_filterFields.put(l, filterField);
      }
    }
  }

  /**
   * Invoked when a filter field is changed
   *
   * @param event
   *          The change event
   */
  protected void handleFilterModified(ModifyEvent event) {
    // update filters
    List<ViewerFilter> filters = new ArrayList<>();
    // backup old filters
    for (ViewerFilter filter : m_tableViewer.getFilters()) {
      if (!(filter instanceof P_ViewerFilter)) {
        filters.add(filter);
      }
    }
    for (Entry<Language, Text> e : m_filterFields.entrySet()) {
      if (!e.getValue().getText().isEmpty()) {
        filters.add(new P_ViewerFilter(e.getKey(), e.getValue().getText()));
      }
    }
    try {
      m_tableViewer.getTable().setRedraw(false);
      m_tableViewer.setFilters(filters.toArray(new ViewerFilter[0]));
    }
    finally {
      m_tableViewer.getTable().setRedraw(true);
    }
  }

  private static final class P_ViewerFilter extends ViewerFilter {

    private final char[] m_pattern;
    private final Language m_lang;

    private P_ViewerFilter(Language lang, String pattern) {
      m_lang = lang;
      m_pattern = (pattern.toLowerCase(Locale.ENGLISH) + '*').toCharArray();
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      ITranslation nlsEntry = NlsTableController.entryOfRow(element);
      String text;
      if (m_lang == null) {
        text = nlsEntry.key();
      }
      else {
        text = nlsEntry.translation(m_lang).orElse(null);
      }
      if (text == null) {
        text = "";
      }
      else {
        text = Strings.replace(text, "&", "");
      }
      return CharOperation.match(m_pattern, text.toCharArray(), false);
    }
  }

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
        int colIndex = colOrder[i];
        TableColumn column = columns[colIndex];
        if (i == NlsTableController.INDEX_COLUMN_KEYS) {
          // layout button
          m_resetButton.setBounds(1, 1, x - 2, parentBounds.height - 2);
        }
        if (i >= NlsTableController.INDEX_COLUMN_KEYS) {
          Language lang = m_controller.languageOfColumn(colIndex);
          Text text = m_filterFields.get(lang);
          if (text != null) {
            text.setBounds(x + 1, 1, column.getWidth() - 2, parentBounds.height - 2);
          }
        }
        x += column.getWidth();
      }
      composite.update();
    }
  }
}
