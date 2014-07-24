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
package org.eclipse.scout.sdk.ui.internal.view.icons;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.ui.fields.proposal.icon.IconContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.icon.IconLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * <h3>IconsEditor</h3> linked with .icon files.
 */
public class IconsEditor extends EditorPart {

  public static final String ID = IconsEditor.class.getName();
  private FormToolkit m_toolkit;
  private Form m_form;
  private Table m_table;
  private TableViewer m_viewer;
  private IIconProvider m_iconProvider;
  private final IconRowFilter m_inheritedFilter;

  public IconsEditor() {
    m_inheritedFilter = new IconRowFilter();
  }

  @Override
  public void createPartControl(Composite parent) {
    m_toolkit = new FormToolkit(parent.getDisplay());
    m_form = m_toolkit.createForm(parent);
    m_form.setText(Texts.get("Icons"));
    Composite body = m_form.getBody();
    body.setLayout(new FillLayout());
    createFormBody(body);
  }

  private void createFormBody(Composite parent) {
    m_table = m_toolkit.createTable(parent, SWT.FULL_SELECTION);
    m_table.setLinesVisible(true);
    m_table.setHeaderVisible(true);

    TableColumn[] columns = new TableColumn[3];
    columns[0] = new TableColumn(m_table, SWT.LEFT);
    columns[0].setWidth(20);
    columns[1] = new TableColumn(m_table, SWT.LEFT);
    columns[1].setText(Texts.get("KEY"));
    columns[1].setWidth(300);
    columns[2] = new TableColumn(m_table, SWT.LEFT);
    columns[2].setText(Texts.get("Name"));
    columns[2].setWidth(300);
    P_SortSelectionAdapter sortListener = new P_SortSelectionAdapter();
    for (TableColumn c : columns) {
      c.addSelectionListener(sortListener);
    }

    m_viewer = new TableViewer(m_table);

    P_IconLabelProvider labelProvider = new P_IconLabelProvider(m_table.getDisplay());
    IconContentProvider contentProvider = new IconContentProvider(m_iconProvider, labelProvider);
    m_viewer.setLabelProvider(labelProvider);
    m_viewer.setContentProvider(contentProvider);
    m_viewer.setSorter(new P_Sorter());
    m_viewer.setInput(contentProvider.getProposals(null, null));

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_table.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
  }

  public void setScoutSharedBundle(IScoutBundle sharedBundle) {
    if (sharedBundle != null) {
      m_iconProvider = sharedBundle.getIconProvider();
      if (m_viewer != null && !m_viewer.getControl().isDisposed()) {
        m_viewer.setContentProvider(new IconContentProvider(m_iconProvider, (ILabelProvider) m_viewer.getLabelProvider()));
        m_viewer.refresh();
      }
    }
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

  @Override
  public void init(IEditorSite site, IEditorInput input) throws PartInitException {
    if (input instanceof FileEditorInput) {
      IFile file = ((FileEditorInput) input).getFile();
      IScoutBundle scoutProject = ScoutTypeUtility.getScoutBundle(file.getProject());
      if (scoutProject != null && IScoutBundle.TYPE_SHARED.equals(scoutProject.getType())) {
        setScoutSharedBundle(scoutProject);
      }
    }
    setSite(site);
    setInput(input);
  }

  @Override
  public void setFocus() {
    m_form.setFocus();
  }

  @Override
  public void doSave(IProgressMonitor monitor) {

  }

  @Override
  public void doSaveAs() {
  }

  @Override
  public boolean isDirty() {
    return false;
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  private class P_SortSelectionAdapter extends SelectionAdapter {

    @Override
    public void widgetSelected(SelectionEvent e) {
      TableColumn col = (TableColumn) e.getSource();
      if (m_table.getSortColumn() == col) {
        if (m_table.getSortDirection() == SWT.UP) {
          m_table.setSortDirection(SWT.DOWN);
        }
        else {
          m_table.setSortDirection(SWT.UP);
        }
      }
      else {
        m_table.setSortColumn(col);
        m_table.setSortDirection(SWT.UP);
      }
      m_viewer.refresh();
    }
  }

  private class P_IconLabelProvider extends IconLabelProvider implements ITableLabelProvider, ITableColorProvider {
    public P_IconLabelProvider(Display display) {
      super(display);
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      Image img = null;
      if (columnIndex == 0) {
        img = super.getImage(element);
      }
      return img;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      switch (columnIndex) {
        case 1:
          return ((ScoutIconDesc) element).getId();
        case 2:
          return ((ScoutIconDesc) element).getIconName();
        default:
          return "";
      }
    }

    @Override
    public Color getBackground(Object element, int columnIndex) {
      return null;
    }

    @Override
    public Color getForeground(Object element, int columnIndex) {
      Color c = null;
      if (element instanceof ScoutIconDesc) {
        if (((ScoutIconDesc) element).isInherited()) {
          c = ScoutSdkUi.getColor(ScoutSdkUi.COLOR_INACTIVE_FOREGROUND);
        }
      }
      return c;
    }
  } // end class P_IconLabelProvider

  private class P_Sorter extends ViewerSorter {
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
      Table table = (Table) viewer.getControl();
      boolean sortAsc = table.getSortDirection() == SWT.UP;
      int c = super.compare(viewer, e1, e2);
      if (!sortAsc) {
        c = -c;
      }
      return c;
    }
  }
}
