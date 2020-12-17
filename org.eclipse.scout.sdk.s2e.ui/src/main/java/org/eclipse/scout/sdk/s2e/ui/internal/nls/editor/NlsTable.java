/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import static java.util.function.Predicate.isEqual;
import static org.eclipse.scout.sdk.core.util.Ensure.notNull;

import java.util.Optional;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * <h3>{@link NlsTable}</h3>
 *
 * @since 7.0.0
 */
public class NlsTable extends Composite {

  private final NlsTableController m_controller;
  private final Table m_table;
  private final TableViewer m_viewer;
  private final NlsFilterComponent m_filterComp;
  private final NlsTableCursor m_cursorManager;
  private final MenuManager m_menuManger;

  private TableColumn m_sortColumn;

  @SuppressWarnings("ThisEscapedInObjectConstruction")
  public NlsTable(Composite parent, NlsTableController controller) {
    super(parent, SWT.NONE);
    setBackground(parent.getBackground());
    m_controller = notNull(controller);

    // create table with columns
    m_table = new Table(this, SWT.FULL_SELECTION | SWT.MULTI);
    m_table.setHeaderVisible(true);
    m_table.setLinesVisible(true);

    // create cursor
    m_cursorManager = new NlsTableCursor(m_table, m_controller);
    m_cursorManager.addCursorListener(this::handleTextUpdate);

    // create table viewer
    m_viewer = new TableViewer(m_table);
    m_viewer.setUseHashlookup(false);
    m_viewer.addSelectionChangedListener(event -> m_cursorManager.setVisible(((IStructuredSelection) event.getSelection()).size() < 2));

    // create header filter
    m_filterComp = new NlsFilterComponent(this, tableViewer(), m_controller);

    // create context menu
    m_menuManger = new MenuManager();
    m_menuManger.setRemoveAllWhenShown(true);
    m_cursorManager.getCursor().setMenu(m_menuManger.createContextMenu(m_cursorManager.getCursor()));
    m_table.setMenu(m_menuManger.createContextMenu(m_table));

    // layout
    setLayout(new FormLayout());
    var filterData = new FormData();
    filterData.top = new FormAttachment(0, 0);
    filterData.left = new FormAttachment(0, 0);
    filterData.right = new FormAttachment(100, 0);
    m_filterComp.setLayoutData(filterData);

    var tableData = new FormData();
    tableData.top = new FormAttachment(m_filterComp, 0);
    tableData.left = new FormAttachment(0, 0);
    tableData.right = new FormAttachment(100, 0);
    tableData.bottom = new FormAttachment(100, 0);
    m_table.setLayoutData(tableData);
  }

  protected TableViewer tableViewer() {
    return m_viewer;
  }

  protected NlsTableCursor tableCursor() {
    return m_cursorManager;
  }

  private void handleTextUpdate(NlsTableCell cell, String newText) {
    if (cell.column() == NlsTableController.INDEX_COLUMN_KEYS) {
      // update key
      m_controller.stack().changeKey(cell.entry().key(), newText);
    }
    else {
      // update translation
      var stack = m_controller.stack();
      stack.setChanging(true);
      try {
        var updatedTranslation = new Translation(cell.entry());
        var language = cell.language().get();
        if (cell.store().languages().noneMatch(isEqual(language))) {
          // language does not yet exist for that store: create it
          stack.addNewLanguage(language, cell.entry().store());
        }
        updatedTranslation.putText(language, newText);
        stack.updateTranslation(updatedTranslation);
      }
      finally {
        stack.setChanging(false);
      }
    }
  }

  protected void createColumns() {
    // clear old columns
    var cols = m_table.getColumns();
    for (var col : cols) {
      col.dispose();
    }

    // ref count column
    var colRefs = new TableColumn(m_table, SWT.LEFT);
    colRefs.setResizable(false);
    colRefs.setMoveable(false);
    colRefs.setWidth(45);
    colRefs.addSelectionListener(new P_SortSelectionAdapter(0));

    // key column
    var keyColumn = new TableColumn(m_table, SWT.LEFT);
    keyColumn.setText("Key");
    keyColumn.setMoveable(false);
    keyColumn.setWidth(200);
    keyColumn.addSelectionListener(new P_SortSelectionAdapter(NlsTableController.INDEX_COLUMN_KEYS));

    // language columns
    m_controller.allLanguages().forEach(this::createColumnForLanguage);

    // refresh sort marker and filter
    m_filterComp.columnsChanged();
    updateSortIcon();
  }

  private void createColumnForLanguage(Language language) {
    var c = new TableColumn(m_table, SWT.LEFT);
    c.setText(language.displayName());
    c.setMoveable(false);
    c.addSelectionListener(new P_SortSelectionAdapter(m_table.indexOf(c)));
    c.setWidth(200);
  }

  private void updateSortIcon() {
    var col = m_table.getColumn(m_controller.getSortIndex());
    if (col == null) {
      return;
    }

    if (col.equals(m_sortColumn)) {
      var sortDir = SWT.UP;
      if (m_table.getSortDirection() == SWT.UP) {
        sortDir = SWT.DOWN;
      }
      m_table.setSortDirection(sortDir);
    }
    else {
      m_sortColumn = col;
      m_table.setSortColumn(m_sortColumn);
      m_table.setSortDirection(SWT.UP);
    }
  }

  public void addMenuListener(IMenuListener listener) {
    m_menuManger.addMenuListener(listener);
  }

  public void removeMenuListener(IMenuListener listener) {
    m_menuManger.removeMenuListener(listener);
  }

  public Optional<NlsTableCell> getCursorSelection() {
    return m_cursorManager.getSelection();
  }

  public void showEditor() {
    m_cursorManager.createEditableText();
  }

  private final class P_SortSelectionAdapter extends SelectionAdapter {
    private final int m_index;

    private P_SortSelectionAdapter(int index) {
      m_index = index;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      if (m_controller.getSortIndex() == m_index) {
        m_controller.setAscSorting(!m_controller.isAscSorting());
      }
      else {
        m_controller.setAscSorting(false);
        m_controller.setSortIndex(m_index);
      }
      updateSortIcon();
      m_controller.preservingSelectionDo(m_viewer::refresh);
    }
  }
}
