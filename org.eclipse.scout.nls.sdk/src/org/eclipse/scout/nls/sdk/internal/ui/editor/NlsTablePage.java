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
package org.eclipse.scout.nls.sdk.internal.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.NlsTableModel;
import org.eclipse.scout.nls.sdk.internal.model.workspace.InheritedNlsEntry;
import org.eclipse.scout.nls.sdk.internal.model.workspace.project.NlsProject;
import org.eclipse.scout.nls.sdk.internal.ui.action.CopyPasteAction;
import org.eclipse.scout.nls.sdk.internal.ui.action.FindReferencesAction;
import org.eclipse.scout.nls.sdk.internal.ui.action.RemoveAction;
import org.eclipse.scout.nls.sdk.internal.ui.action.UpdateReferenceCountAction;
import org.eclipse.scout.nls.sdk.internal.ui.wizard.importExport.NlsExportAction;
import org.eclipse.scout.nls.sdk.internal.ui.wizard.importExport.NlsImportAction;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProjectListener;
import org.eclipse.scout.nls.sdk.model.workspace.project.NlsProjectEvent;
import org.eclipse.scout.nls.sdk.ui.INlsTableActionHanlder;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryNewAction;
import org.eclipse.scout.nls.sdk.ui.action.NlsRefreshAction;
import org.eclipse.scout.nls.sdk.ui.action.TranslationFileNewAction;
import org.eclipse.scout.nls.sdk.util.concurrent.UiRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class NlsTablePage extends Composite {

  private NlsProject m_nlsProject;
  private NlsTable m_table;
  private Button m_hideInherited;
  private NlsTableModel m_tableModel;
  private P_ProjectListener m_projectListener;
  private final IFile m_nlsFile;

  public NlsTablePage(Composite parent, NlsProject project, IFile nlsFile) {
    super(parent, SWT.NONE);
    m_nlsProject = project;
    m_nlsFile = nlsFile;

    m_tableModel = new NlsTableModel(project);
    FormToolkit toolkit = new FormToolkit(getDisplay());
    Form form = toolkit.createForm(this);
    form.setText("Translations");
    createContent(toolkit, form.getBody());

    setLayout(new FillLayout());
    // createContent(this);
    createFormMenu(form.getToolBarManager());
    form.updateToolBar();
    attachListeners();
    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        dettachListeners();
      }
    });
  }

  private void attachListeners() {
    if (m_projectListener == null) {
      m_projectListener = new P_ProjectListener();
    }
    m_nlsProject.addProjectListener(m_projectListener);
  }

  private void dettachListeners() {
    if (m_projectListener != null) {
      m_nlsProject.removeProjectListener(m_projectListener);
    }
  }

  private void createFormMenu(IToolBarManager manager) {
    manager.add(new UpdateReferenceCountAction(m_nlsProject, m_table, m_tableModel));
    manager.add(new NlsEntryNewAction(true, m_nlsProject));
    // manager.add(new NewEntryAction(m_nlsProject, m_table));
    manager.add(new TranslationFileNewAction(m_table.getShell(), true, m_nlsProject));
    manager.add(new NlsImportAction(m_nlsProject, m_table.getShell()));
    manager.add(new NlsExportAction(m_nlsProject, m_table.getShell()));
    // manager.add(new NewLanguageFileAction(m_nlsProject,m_table.getViewer()));
    // manager.add( new UpdateReferencesAction("Update ALL references [Shift+F6]",m_nlsProject,null));
  }

  private void createContent(FormToolkit toolkit, Composite parent) {
    Composite rootArea = toolkit.createComposite(parent);
    m_hideInherited = toolkit.createButton(rootArea, "hide inherited rows", SWT.CHECK);
    m_hideInherited.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_table.setHideInherited(m_hideInherited.getSelection());
      }
    });
    m_table = new NlsTable(rootArea, m_tableModel);
    m_table.setInputValidator(new NlsTableInputValidator(m_nlsProject));
    m_table.setActionHanlder(new P_TableActionHandler());
    m_table.addMenuListener(new P_MenuListener());
    // layout
    parent.setLayout(new FillLayout());
    rootArea.setLayout(new GridLayout(1, true));
    m_hideInherited.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_table.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
  }

  /**
   * This class will be used of the {@link NlsTable} to invoke any operation.
   */
  private class P_TableActionHandler implements INlsTableActionHanlder {
    public void handleRefreshTable(boolean realoadFiles) {
      NlsRefreshAction refreshAciton = new NlsRefreshAction(m_nlsProject, realoadFiles);
      refreshAciton.run();
      try {
        refreshAciton.join();
      }
      catch (InterruptedException e) {
        NlsCore.logWarning(e);
      }
      m_nlsProject = refreshAciton.getNlsProject();
      // Job reloadJob = new Job("reload files"){
      // protected IStatus run(IProgressMonitor monitor) {
      // m_nlsProject.reload(monitor);
      // return Status.OK_STATUS;
      // }
      // };
      // reloadJob.setUser(false);
      // reloadJob.schedule();
    }

    public void handleRefreshReferenceCount(final String key) {
      // Job job = new NlsFindReferencesOperation(key, m_nlsProject);
      // job.setUser(false);
      // job.schedule();
    }

    public void handleReload() {
    }

  } // end class P_TableActionHandler

  protected void handleProjectChangedEvnet(NlsProjectEvent event) {
    try {
      m_table.setRedraw(false);
      handleProjectChangedEvnetInternal(event);
    }
    finally {
      m_table.setRedraw(true);
    }
  }

  protected void handleProjectChangedEvnetInternal(NlsProjectEvent event) {
    if (event.isMultiEvent()) {
      for (NlsProjectEvent e : event.getChildEvents()) {
        handleProjectChangedEvnetInternal(e);
      }
    }
    else {
      switch (event.getType()) {
        case NlsProjectEvent.TYPE_ENTRY_ADDED:
          m_table.getViewer().add(event.getEntry());
          m_table.getViewer().reveal(event.getEntry());
          m_table.getViewer().setSelection(new StructuredSelection(event.getEntry()));
          break;
        case NlsProjectEvent.TYPE_ENTRY_MODIFYED:
          m_table.refresh(event.getEntry());
          break;
        case NlsProjectEvent.TYPE_ENTRY_REMOVEED:
          m_table.getViewer().remove(event.getEntry());
          break;
        case NlsProjectEvent.TYPE_REFRESH:
          m_table.refreshAll(false);
          break;
        case NlsProjectEvent.TYPE_FULL_REFRESH:
          m_table.refreshAll(true);
          break;
        default:
          break;
      }
    }

  }

  private class P_ProjectListener implements INlsProjectListener {
    public void notifyProjectChanged(NlsProjectEvent event) {
      if (m_table != null) {
        m_table.getDisplay().asyncExec(new UiRunnable(new Object[]{event}) {
          public void run() {
            handleProjectChangedEvnet((NlsProjectEvent) p_args[0]);
          }
        });
      }
    }

  } // end class P_ProjectListener

  private class P_MenuListener implements IMenuListener {
    public void menuAboutToShow(IMenuManager manager) {
      IStructuredSelection selection = (IStructuredSelection) m_table.getViewer().getSelection();
      ArrayList<INlsEntry> entries = new ArrayList<INlsEntry>();
      for (Iterator it = selection.iterator(); it.hasNext();) {
        entries.add((INlsEntry) it.next());
      }
      MenuManager menuManager = (MenuManager) manager;
      if (entries.size() == 1) {
        Point cursorPos /* row, column */= m_table.getCursorLocation();
        if (cursorPos.y < 0) {
          cursorPos.y = -1;
        }
        TableItem row = null;
        if(cursorPos.x > 0){
          row = m_table.getViewer().getTable().getItem(cursorPos.x);
        }
        addSingleSelectMenues(menuManager, entries.get(0), cursorPos.y, row.getText(cursorPos.y));
      }
      else if (entries.size() > 1) {
        addMultiSelectMenues(menuManager, entries.toArray(new INlsEntry[entries.size()]));
      }
      // anyway
      manager.add(new Separator());
      manager.add(new UpdateReferenceCountAction(m_nlsProject, m_table, m_tableModel));
      // TODO check if the creation of a new file is legal (workspace vs. platform)
      manager.add(new TranslationFileNewAction(m_table.getShell(), true, m_nlsProject));
    }

    private void addSingleSelectMenues(MenuManager manager, INlsEntry entry, int cursorColumn, String cursorText) {
      if (!(entry instanceof InheritedNlsEntry)) {
        if (cursorColumn == NlsTable.INDEX_COLUMN_KEYS) {
          manager.add(new Action("Edit key") {
            @Override
            public void run() {
              m_table.showEditor();
            }
          });
        }
        manager.add(new RemoveAction("Remove "+entry.getKey(), m_nlsProject, entry));
        manager.add(new Separator());
      }
      manager.add(new FindReferencesAction(m_nlsProject, entry.getKey()));
      manager.add(new CopyPasteAction("Copy",cursorText , m_table.getDisplay()));
    }

    private void addMultiSelectMenues(MenuManager manager, INlsEntry[] entries) {
      manager.add(new RemoveAction("Remove entries", m_nlsProject, entries));
    }


  } // end class P_MenuListener

}
