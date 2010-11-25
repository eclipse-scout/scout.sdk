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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * <h3>IconsEditor</h3> linked with .icon files.
 */
public class IconsEditor extends EditorPart {

  public static final String ID = IconsEditor.class.getName();
  private FormToolkit m_toolkit;
  private ScrolledForm m_form;
  private Table m_table;
  private TableViewer m_viewer;
  private IconTableContentProvider m_contentProvider;
  private IconRowFilter m_inheritedFilter = new IconRowFilter();

  @Override
  public void createPartControl(Composite parent) {
    m_toolkit = new FormToolkit(parent.getDisplay());
    m_form = m_toolkit.createScrolledForm(parent);
    m_form.setText("Icons");
    Composite body = m_form.getBody();
    body.setLayout(new FillLayout());
    createFormBody(body);
    m_form.reflow(true);
  }

  private void createFormBody(Composite parent) {
    // final Button hideInheritedButton=m_toolkit.createButton(parent, "hide inherited rows", SWT.CHECK);
    // hideInheritedButton.setSelection(true);
    // hideInheritedButton.addSelectionListener(new SelectionAdapter(){
    // @Override
    // public void widgetSelected(SelectionEvent e){
    // setHideInherited(hideInheritedButton.getSelection());
    // }
    // });
    m_table = m_toolkit.createTable(parent, SWT.FULL_SELECTION);
    m_table.setLinesVisible(true);
    m_table.setHeaderVisible(true);

    TableColumn[] columns = new TableColumn[3];
    columns[0] = new TableColumn(m_table, SWT.LEFT);
    columns[0].setWidth(20);
    columns[1] = new TableColumn(m_table, SWT.LEFT);
    columns[1].setText("KEY");
    columns[1].setWidth(300);
    columns[2] = new TableColumn(m_table, SWT.LEFT);
    columns[2].setText("Name");
    columns[2].setWidth(300);
    P_SortSelectionAdapter sortListener = new P_SortSelectionAdapter();
    for (TableColumn c : columns) {
      c.addSelectionListener(sortListener);
    }

    m_viewer = new TableViewer(m_table);

    m_viewer.setLabelProvider(m_contentProvider);
    m_viewer.setContentProvider(m_contentProvider);
    m_viewer.setComparator(m_contentProvider);
    m_viewer.setInput(m_contentProvider);
    // setHideInherited(hideInheritedButton.getSelection());

    // layout
    parent.setLayout(new GridLayout(1, true));
    // hideInheritedButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_table.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
  }

  public void setScoutSharedBundle(IScoutBundle sharedBundle) {
    // XXX load async
    if (sharedBundle != null) {
      m_contentProvider.setIcons(ScoutProposalUtility.getScoutIconProposals(ScoutSdkUi.getDisplay(), sharedBundle));
      m_inheritedFilter.setScoutBundle(sharedBundle);
      if (m_viewer != null && !m_viewer.getControl().isDisposed()) {
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
    m_contentProvider = new IconTableContentProvider();
    if (input instanceof FileEditorInput) {
      IFile file = ((FileEditorInput) input).getFile();
      IScoutBundle scoutProject = ScoutSdk.getScoutWorkspace().getScoutBundle(file.getProject());
      if (scoutProject.getType() == IScoutElement.BUNDLE_SHARED) {
        setScoutSharedBundle((IScoutBundle) scoutProject);
      }
      // TODO parse file
      // setScoutSharedBundle(BsiCaseCore.getDefault().getScoutWorkspace().getProjectGroup(file.getProject()));
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
    // TODO Auto-generated method stub

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

}
