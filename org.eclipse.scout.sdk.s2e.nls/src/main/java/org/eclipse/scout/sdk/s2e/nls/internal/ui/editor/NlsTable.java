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
package org.eclipse.scout.sdk.s2e.nls.internal.ui.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.INlsIcons;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
import org.eclipse.scout.sdk.s2e.nls.model.NlsEntry;
import org.eclipse.scout.sdk.s2e.nls.project.AbstractNlsProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * The represenation of a NlsProject.
 *
 * @see AbstractNlsProject
 * @see NlsTableModel
 */
public class NlsTable extends Composite {

  public static final int AMOUNT_UTILITY_COLS = 1;
  public static final int INDEX_COLUMN_KEYS = 1;
  public static final int REFRESH_ORIGINAL_COLUMN = 1 << 0;
  public static final int REFRESH_ALL = REFRESH_ORIGINAL_COLUMN;
  public static final String LANGUAGE_COLUMN_ID = "columnIdentifierLanguage";
  private Table m_table;
  private NlsTableCursor m_cursorManager;
  private TableColumn m_sortColumn;
  private MenuManager m_menuManger;
  private TableViewer m_viewer;
  private NlsTableModel m_tableModel;
  private InheritedFilter m_inheritedFilter = new InheritedFilter();

  private INlsTableActionHanlder m_actionHanlder;
  private NlsFilterComponent m_filterComp;

  public NlsTable(Composite parent, NlsTableModel model) {
    super(parent, SWT.NONE);
    setBackground(parent.getBackground());
    createContent(this);
    setModel(model);
  }

  public void setHideInherited(boolean hideInherited) {
    m_table.setRedraw(false);
    try {
      if (hideInherited) {
        m_viewer.addFilter(m_inheritedFilter);
      }
      else {
        m_viewer.removeFilter(m_inheritedFilter);
      }
    }
    finally {
      m_table.setRedraw(true);
    }
  }

  public Table getTable() {
    return m_table;
  }

  private void createContent(Composite parent) {
    m_table = new Table(parent, SWT.FULL_SELECTION | SWT.MULTI);
    m_table.setHeaderVisible(true);
    m_table.setLinesVisible(true);

    m_cursorManager = new NlsTableCursor(m_table, this);
    m_cursorManager.addCursorMangerListener(new INlsTableCursorManangerListener() {
      @Override
      public void textChangend(INlsEntry row, int i, String string) {
        handleTextUpdate(row, i, string);
      }
    });

    m_viewer = new TableViewer(m_table);

    m_filterComp = new NlsFilterComponent(parent);
    m_filterComp.setTableViewer(m_viewer);
    m_viewer.setUseHashlookup(true);
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        StructuredSelection selection = (StructuredSelection) event.getSelection();
        if (selection.size() > 1) {
          m_cursorManager.getCursor().setVisible(false);
        }
        else {
          m_cursorManager.getCursor().setVisible(true);
        }
      }
    });
    m_cursorManager.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.keyCode == SWT.F5) {
          m_actionHanlder.handleRefreshTable();
        }
        if (e.keyCode == SWT.F6) {
          if (e.stateMask == SWT.SHIFT) {
            m_actionHanlder.handleRefreshReferenceCount(null);
          }
          else {
            // update only selection ref
            m_actionHanlder.handleRefreshReferenceCount(m_cursorManager.getSelection().getSelectedRow().getKey());
          }
        }
      }
    });

    // menu manager
    m_menuManger = new MenuManager();
    m_menuManger.setRemoveAllWhenShown(true);
    TableCursor tableCursor = m_cursorManager.getCursor();
    Menu contextMenu = m_menuManger.createContextMenu(tableCursor);
    tableCursor.setMenu(contextMenu);

    Menu tableMenu = m_menuManger.createContextMenu(m_table);
    m_table.setMenu(tableMenu);

    // layout
    parent.setLayout(new FormLayout());
    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    m_filterComp.setLayoutData(data);
    data = new FormData();
    data.top = new FormAttachment(m_filterComp, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(100, 0);
    m_table.setLayoutData(data);
  }

  public Point getCursorLocation() {
    TableCursor tableCursor = m_cursorManager.getCursor();
    TableItem row = tableCursor.getRow();
    if (row == null) {
      return new Point(-1, -1);
    }
    return new Point(m_table.indexOf(tableCursor.getRow()), tableCursor.getColumn());
  }

  /**
   * @param row
   * @param i
   */
  public void setEditor(NlsEntry row, int column) {
    if (m_table.getColumnCount() < column) {
      return;
    }
    int rowIndex = 0;
    for (TableItem item : m_table.getItems()) {
      if (item.getData().equals(row)) {
        m_cursorManager.setEditableText(rowIndex, column);
        return;
      }
      rowIndex++;
    }
  }

  public void setInputValidator(NlsTableInputValidator validator) {
    m_cursorManager.setInputValidator(validator);
  }

  public void addMenuListener(IMenuListener listener) {
    m_menuManger.addMenuListener(listener);
  }

  private Language getLanguageOfTableColumn(int columnIndex) {
    return m_tableModel.getProjects().getAllLanguages().get(columnIndex - (INDEX_COLUMN_KEYS + 1));
  }

  public NlsTableModel getModel() {
    return m_tableModel;
  }

  public void setModel(NlsTableModel model) {
    try {
      m_viewer.getTable().setRedraw(false);
      m_tableModel = model;

      createColumns(m_table);
      m_viewer.setLabelProvider(m_tableModel);
      m_viewer.setContentProvider(m_tableModel);
      m_viewer.setInput(m_tableModel);
      m_viewer.setComparator(m_tableModel);
      updateSortIcon();
    }
    finally {
      m_viewer.getTable().setRedraw(true);
    }
  }

  public void setActionHanlder(INlsTableActionHanlder handler) {
    m_actionHanlder = handler;
  }

  private void createColumns(Table table) {
    // cleare old columns
    TableColumn[] cols = m_table.getColumns();
    for (TableColumn col : cols) {
      col.dispose();
    }
    // utility columns
    TableColumn colRefs = new TableColumn(table, SWT.LEFT);
    colRefs.setResizable(false);
    colRefs.setMoveable(false);
    colRefs.setWidth(45);
    colRefs.setText("*");
    colRefs.setToolTipText("* if the references are not sync!");
    colRefs.addSelectionListener(new P_SortSelectionAdapter(0));

    int i = NlsTable.INDEX_COLUMN_KEYS;
    // nls java column

    TableColumn jColumn = new TableColumn(table, SWT.LEFT);
    jColumn.setText(Language.LANGUAGE_KEY.getDispalyName());
    jColumn.setData(LANGUAGE_COLUMN_ID, Language.LANGUAGE_KEY);
    jColumn.setMoveable(false);
    jColumn.setWidth(200);
    jColumn.addSelectionListener(new P_SortSelectionAdapter(i++));

    if (m_tableModel.getProjects() != null) {
      for (Language language : m_tableModel.getProjects().getAllLanguages()) {
        createTableColumnInternal(language);
      }
      m_filterComp.columnsChanged();
    }
  }

  public void createTableColumnInternal(Language language) {
    TableColumn c = new TableColumn(m_table, SWT.LEFT);
    c.setData(LANGUAGE_COLUMN_ID, language);
    if (!language.isLocal()) {
      c.setToolTipText("this language is only in the parent defined!\nCreate a new language for " + language.getLocale().toString() + " to make it locally available.");
      c.setImage(NlsCore.getImage(INlsIcons.WARNING));
    }
    c.setText(language.getDispalyName());
    c.setMoveable(true);
    c.addSelectionListener(new P_SortSelectionAdapter(m_table.indexOf(c)));
    c.setWidth(200);
  }

  public void createTableColumn(Language language) {
    createTableColumnInternal(language);
    m_viewer.setInput(m_tableModel);
  }

  private void updateSortIcon() {
    int index = m_tableModel.getSortIndex();

    if (index < 0) {
      index = NlsTable.INDEX_COLUMN_KEYS;
      m_tableModel.setSortIndex(index);
    }
    TableColumn col = m_table.getColumn(index);
    if (col == null) {
      return;
    }
    else if (col.equals(m_sortColumn)) {
      int sortDir = SWT.UP;
      switch (m_table.getSortDirection()) {
        case SWT.UP:
          sortDir = SWT.DOWN;
          break;
      }
      m_table.setSortDirection(sortDir);
    }
    else {
      m_sortColumn = col;
      m_table.setSortColumn(m_sortColumn);
      m_table.setSortDirection(SWT.UP);
    }
  }

  private boolean handleTextUpdate(INlsEntry row, int column, String newText) {
    if (column == INDEX_COLUMN_KEYS) {
      // update key
      m_tableModel.getProjects().updateKey(row, newText, new NullProgressMonitor());
      ((NlsEntry) row).setKey(newText);
      m_viewer.refresh(false);
      return true;
    }
    // update translation
    final NlsEntry copy = new NlsEntry(row);
    copy.addTranslation(getLanguageOfTableColumn(column), newText);
    Job job = new Job("update text") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        m_tableModel.getProjects().updateRow(copy, monitor);
        return Status.OK_STATUS;
      }
    };
    job.setSystem(false);
    job.schedule();
    try {
      job.join();
    }
    catch (InterruptedException e) {
      SdkLog.warning(e);
    }
    m_viewer.refresh(false);
    return job.getResult().isOK();
  }

  public void asyncRefresh(final INlsEntry row) {
    m_table.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        refresh(row);
      }
    });
  }

  public void refresh(INlsEntry row) {
    if (row == null) {
      refreshAll(false);
    }
    else {
      m_viewer.refresh(row);
    }
  }

  public void refreshAllAsync(final boolean recreateColumns) {
    if (!isDisposed()) {
      getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          refreshAll(recreateColumns);
        }
      });
    }
  }

  public void refreshAll(boolean recreateColumns) {
    try {
      m_table.setRedraw(false);
      if (recreateColumns) {
        createColumns(m_table);
      }
      m_viewer.refresh();
      TableItem[] selection = m_table.getSelection();
      if (selection != null && selection.length > 0) {
        TableItem row = (selection.length == 0) ? m_table.getItem(m_table.getTopIndex()) : selection[0];
        m_table.showItem(row);
        m_cursorManager.ensureFocus(row);
      }
    }
    finally {
      m_table.setRedraw(true);
    }
  }

  public void showEditor() {
    TableCursor cursor = m_cursorManager.getCursor();
    Assert.isTrue(cursor.getColumn() == INDEX_COLUMN_KEYS);
    m_cursorManager.createEditableText();
  }

  public TableViewer getViewer() {
    return m_viewer;
  }

  private class P_SortSelectionAdapter extends SelectionAdapter {
    private int m_index;

    private P_SortSelectionAdapter(int index) {
      m_index = index;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      if (m_tableModel.getSortIndex() == m_index) {
        m_tableModel.setAscSorting(!m_tableModel.isAscSorting());
      }
      else {
        m_tableModel.setAscSorting(false);
        m_tableModel.setSortIndex(m_index);
      }
      updateSortIcon();
      refreshAll(false);
    }
  } // end P_SortSelectionAdapter
}
