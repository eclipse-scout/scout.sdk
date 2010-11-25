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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.axis.util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class FileListViewer extends Composite {
  private List<IFile> m_files;
  private List<IFile> m_result;
  private CheckboxTableViewer m_viewer;

  public FileListViewer(Composite parent, int style) {
    super(parent, style);
    m_files = new ArrayList<IFile>(0);
    m_result = new ArrayList<IFile>(0);
    createPartControl(this);
  }

  public void setFiles(List<IFile> files) {
    m_files = files;
    sortTable(2, true);
    sortTable(1, true);
    m_viewer.setAllChecked(true);
  }

  public List<IFile> getFiles() {
    return m_files;
  }

  public void saveResult() {
    ArrayList<IFile> list = new ArrayList<IFile>();
    for (TableItem item : m_viewer.getTable().getItems()) {
      if (item.getChecked()) {
        list.add((IFile) item.getData());
      }
    }
    m_result = list;
  }

  public List<IFile> getCheckedFiles() {
    return m_result;
  }

  /**
   * This is a callback that will allow us to create the viewer and initialize
   * it.
   */
  protected void createPartControl(Composite parent) {
    GridLayout layout = new GridLayout(1, true);
    parent.setLayout(layout);
    // create checkbox "check all"
    Composite buttons = new Composite(parent, SWT.NONE);
    buttons.setLayout(new RowLayout(SWT.HORIZONTAL));
    Button checkAllButton = new Button(buttons, SWT.NONE);
    checkAllButton.setLayoutData(new RowData());
    checkAllButton.setText("Check all");
    checkAllButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_viewer.setAllChecked(true);
      }
    });
    Button uncheckAllButton = new Button(buttons, SWT.NONE);
    uncheckAllButton.setLayoutData(new RowData());
    uncheckAllButton.setText("Uncheck all");
    uncheckAllButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_viewer.setAllChecked(false);
      }
    });
    // create table
    Table table = new Table(parent, SWT.CHECK | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    m_viewer = new CheckboxTableViewer(table);
    while (table.getColumnCount() > 0) {
      table.getColumn(0).dispose();
    }
    new TableColumn(table, SWT.LEFT);
    new TableColumn(table, SWT.LEFT);
    new TableColumn(table, SWT.LEFT);
    new TableColumn(table, SWT.LEFT);
    //
    table.getColumn(0).setData("index", 0);
    table.getColumn(1).setData("index", 1);
    table.getColumn(2).setData("index", 2);
    table.getColumn(3).setData("index", 3);
    //
    TableSortAdapter sortAdapter = new TableSortAdapter();
    table.getColumn(0).addSelectionListener(sortAdapter);
    table.getColumn(1).addSelectionListener(sortAdapter);
    table.getColumn(2).addSelectionListener(sortAdapter);
    table.getColumn(3).addSelectionListener(sortAdapter);
    //
    table.getColumn(0).setWidth(32);
    //
    table.getColumn(1).setText("Path");
    table.getColumn(1).setWidth(320);
    //
    table.getColumn(2).setText("Resource");
    table.getColumn(2).setWidth(250);
    //
    table.getColumn(3).setText("Problems");
    table.getColumn(3).setWidth(80);
    //
    table.setHeaderVisible(true);
    m_viewer.setContentProvider(new ViewContentProvider());
    m_viewer.setLabelProvider(new ViewLabelProvider());
    m_viewer.setInput("default");
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public boolean setFocus() {
    return m_viewer.getControl().setFocus();
  }

  public void setInput(Object input) {
    m_viewer.setInput(input);
  }

  public void sortTable(int columnIndex, boolean ascending) {
    Table table = m_viewer.getTable();
    TableColumn col = table.getColumn(columnIndex);
    ViewLabelProvider labelProvider = (ViewLabelProvider) m_viewer.getLabelProvider();
    Collator coll = Collator.getInstance();
    TreeMap<CompositeObject, IFile> sortMap = new TreeMap<CompositeObject, IFile>();
    int index = 0;
    for (IFile f : m_files) {
      String text = null;
      if (columnIndex > 0) {
        text = labelProvider.getColumnText(f, columnIndex);
      }
      else {
        if (index < table.getItemCount()) {
          text = table.getItem(index).getChecked() ? "X" : "";
        }
      }
      sortMap.put(new CompositeObject(coll.getCollationKey(text), (ascending ? index : m_files.size() - 1 - index)), f);
      //
      index++;
    }
    if (ascending) {
      m_files = new ArrayList<IFile>(sortMap.values());
    }
    else {
      m_files = new ArrayList<IFile>(sortMap.size());
      for (ListIterator<IFile> it = new ArrayList<IFile>(sortMap.values()).listIterator(sortMap.size()); it.hasPrevious();) {
        m_files.add(it.previous());
      }
    }
    //
    table.setSortColumn(col);
    table.setSortDirection(ascending ? SWT.UP : SWT.DOWN);
    m_viewer.refresh();
  }

  /**
   * The content provider class is responsible for providing objects to the
   * view. It can wrap existing objects in adapters or simply return objects
   * as-is. These objects may be sensitive to the current input of the view,
   * or ignore it and always show the same content (like Task List, for
   * example).
   */
  private class ViewContentProvider implements IStructuredContentProvider {
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    }

    public void dispose() {
    }

    public Object[] getElements(Object parent) {
      return m_files.toArray(new IFile[m_files.size()]);
    }
  }

  private class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

    public String getColumnText(Object obj, int index) {
      IFile f = (IFile) obj;
      switch (index) {
        case 0:
          return null;
        case 1:
          return f.getFullPath().removeLastSegments(1).toString();
        case 2:
          return f.getName();
        case 3:
          return f.exists() ? "File exists" : "";
        default:
          return null;
      }
    }

    public Image getColumnImage(Object obj, int index) {
      IFile f = (IFile) obj;
      switch (index) {
        case 0:
          return null;
        case 1:
          return null;
        case 2:
          return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
        case 3:
          return f.exists() ? PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK) : null;
        default:
          return null;
      }
    }
  }

  private class TableSortAdapter extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent e) {
      TableColumn col = (TableColumn) e.widget;
      int direction;
      if (col.getParent().getSortColumn() == col) {
        direction = (col.getParent().getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
      }
      else {
        direction = SWT.UP;
      }
      sortTable((Integer) col.getData("index"), direction == SWT.UP);
    }
  }

}
