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
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.NlsTableModel;
import org.eclipse.scout.nls.sdk.internal.model.workspace.InheritedNlsEntry;
import org.eclipse.scout.nls.sdk.internal.ui.action.CopyPasteAction;
import org.eclipse.scout.nls.sdk.internal.ui.action.FindReferencesAction;
import org.eclipse.scout.nls.sdk.internal.ui.action.RemoveAction;
import org.eclipse.scout.nls.sdk.internal.ui.action.UpdateKeyUsageCountAction;
import org.eclipse.scout.nls.sdk.internal.ui.action.UpdateReferenceCountAction;
import org.eclipse.scout.nls.sdk.internal.ui.wizard.importExport.NlsExportAction;
import org.eclipse.scout.nls.sdk.internal.ui.wizard.importExport.NlsImportAction;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProjectListener;
import org.eclipse.scout.nls.sdk.model.workspace.project.NlsProjectEvent;
import org.eclipse.scout.nls.sdk.ui.INlsTableActionHanlder;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryModifyAction;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryNewAction;
import org.eclipse.scout.nls.sdk.ui.action.NlsRefreshAction;
import org.eclipse.scout.nls.sdk.ui.action.TranslationNewAction;
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

  private INlsProject m_nlsProjects;
  private NlsTable m_table;
  private Button m_hideInherited;
  private NlsTableModel m_tableModel;
  private P_ProjectListener m_projectListener;

  public NlsTablePage(Composite parent, INlsProject projects) {
    super(parent, SWT.NONE);
    m_nlsProjects = projects;

    m_tableModel = new NlsTableModel(projects);
    FormToolkit toolkit = new FormToolkit(getDisplay());
    Form form = toolkit.createForm(this);
    form.setText("Translations");
    createContent(toolkit, form.getBody());

    setLayout(new FillLayout());
    createFormMenu(form.getToolBarManager());
    form.updateToolBar();
    attachListeners();
    addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        dettachListeners();
      }
    });
  }

  private void attachListeners() {
    if (m_projectListener == null) {
      m_projectListener = new P_ProjectListener();
    }
    if (m_nlsProjects != null) {
      m_nlsProjects.addProjectListener(m_projectListener);
    }
  }

  private void dettachListeners() {
    if (m_projectListener != null && m_nlsProjects != null) {
      m_nlsProjects.removeProjectListener(m_projectListener);
    }
  }

  private void createFormMenu(IToolBarManager manager) {
    manager.add(new NlsRefreshAction(m_nlsProjects));
    manager.add(new UpdateReferenceCountAction(m_nlsProjects, m_table, m_tableModel));
    manager.add(new NlsEntryNewAction(m_table.getShell(), m_nlsProjects, false));
    manager.add(new TranslationNewAction(m_nlsProjects, m_table.getShell()));
    manager.add(new NlsImportAction(m_nlsProjects, m_table.getShell()));
    manager.add(new NlsExportAction(m_nlsProjects, m_table.getShell()));
  }

  private void createContent(FormToolkit toolkit, Composite parent) {
    Composite rootArea = toolkit.createComposite(parent);
    m_hideInherited = toolkit.createButton(rootArea, "Hide inherited rows", SWT.CHECK);
    m_hideInherited.setSelection(true);
    m_hideInherited.setEnabled(m_nlsProjects != null);
    m_hideInherited.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_table.setHideInherited(m_hideInherited.getSelection());
      }
    });

    m_table = new NlsTable(rootArea, m_tableModel);
    m_table.setHideInherited(m_hideInherited.getSelection());
    m_table.setInputValidator(new NlsTableInputValidator(m_nlsProjects));
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
    @Override
    public void handleRefreshTable() {
      NlsRefreshAction refreshAciton = new NlsRefreshAction(m_nlsProjects);
      refreshAciton.run();
      try {
        refreshAciton.join();
      }
      catch (InterruptedException e) {
        NlsCore.logWarning(e);
      }
      m_nlsProjects = refreshAciton.getNlsProject();
    }

    @Override
    public void handleRefreshReferenceCount(final String key) {
    }
  } // end class P_TableActionHandler

  private void handleProjectChangedEvent(NlsProjectEvent event) {
    try {
      m_table.setRedraw(false);
      boolean fullRefresh = handleProjectChangedEventInternalRec(event);
      m_table.refreshAll(fullRefresh);
    }
    finally {
      m_table.setRedraw(true);
    }
  }

  private boolean handleProjectChangedEventInternalRec(NlsProjectEvent event) {
    boolean full = false;
    if (event.isMultiEvent()) {
      for (NlsProjectEvent e : event.getChildEvents()) {
        if (handleProjectChangedEventInternalRec(e)) {
          full = true;
        }
      }
    }
    else {
      switch (event.getType()) {
        case NlsProjectEvent.TYPE_ENTRY_ADDED:
          m_table.getViewer().reveal(event.getEntry());
          m_table.getViewer().setSelection(new StructuredSelection(event.getEntry()));
          break;
        case NlsProjectEvent.TYPE_ENTRY_REMOVED:
          m_table.getViewer().remove(event.getEntry());
          break;
        case NlsProjectEvent.TYPE_ENTRY_MODIFYED:
        case NlsProjectEvent.TYPE_REFRESH:
        case NlsProjectEvent.TYPE_TRANSLATION_RESOURCE_ADDED:
        case NlsProjectEvent.TYPE_FULL_REFRESH:
          full = true;
          break;
      }
    }
    return full;
  }

  private class P_ProjectListener implements INlsProjectListener {
    @Override
    public void notifyProjectChanged(final NlsProjectEvent event) {
      if (m_table != null && !m_table.isDisposed()) {
        m_table.getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            handleProjectChangedEvent(event);
          }
        });
      }
    }
  } // end class P_ProjectListener

  private class P_MenuListener implements IMenuListener {
    @Override
    public void menuAboutToShow(IMenuManager manager) {
      IStructuredSelection selection = (IStructuredSelection) m_table.getViewer().getSelection();
      List<INlsEntry> entries = new ArrayList<>(selection.size());

      for (Iterator<?> it = selection.iterator(); it.hasNext();) {
        entries.add((NlsEntry) it.next());
      }
      MenuManager menuManager = (MenuManager) manager;
      if (entries.size() == 1) {
        Point cursorPos /* row, column */= m_table.getCursorLocation();
        if (cursorPos.y < 0) {
          cursorPos.y = -1;
        }
        TableItem row = null;
        if (cursorPos.x >= 0) {
          row = m_table.getViewer().getTable().getItem(cursorPos.x);
        }
        if (row != null) {
          INlsEntry e = entries.get(0);
          int colIndex = cursorPos.y;
          String txt = null;
          if (colIndex == 1) {
            txt = e.getKey();
          }
          else if (colIndex > 1) {
            Language languageOfColumn = m_tableModel.getLanguageOfColumn(colIndex);
            if (languageOfColumn != null) {
              txt = e.getTranslation(languageOfColumn);
            }
          }
          addSingleSelectMenues(menuManager, e, colIndex, txt, e instanceof InheritedNlsEntry);
        }
      }
      else if (entries.size() > 1) {
        addMultiSelectMenues(menuManager, entries);
      }
    }

    private void addSingleSelectMenues(MenuManager manager, INlsEntry entry, int cursorColumn, String cursorText, boolean isInheritedEntry) {
      if (!isInheritedEntry) {
        if (cursorColumn == NlsTable.INDEX_COLUMN_KEYS) {
          manager.add(new Action("Edit key") {
            @Override
            public void run() {
              m_table.showEditor();
            }
          });
          manager.add(new Separator());
        }
        manager.add(new NlsEntryModifyAction(getShell(), entry, m_nlsProjects));
        manager.add(new Separator());
      }
      manager.add(new FindReferencesAction(m_nlsProjects, entry.getKey()));
      manager.add(new UpdateReferenceCountAction(m_nlsProjects, m_table, m_tableModel));
      manager.add(new UpdateKeyUsageCountAction(m_nlsProjects, m_table, m_tableModel));
      if (cursorText != null && cursorText.length() > 0) {
        manager.add(new Separator());
        manager.add(new CopyPasteAction("Copy", cursorText, m_table.getDisplay()));
      }
      if (!isInheritedEntry) {
        manager.add(new Separator());
        manager.add(new RemoveAction("Remove " + entry.getKey(), m_nlsProjects, entry));
      }
    }

    private void addMultiSelectMenues(MenuManager manager, List<INlsEntry> entries) {
      manager.add(new UpdateReferenceCountAction(m_nlsProjects, m_table, m_tableModel));
      manager.add(new Separator());
      manager.add(new RemoveAction("Remove entries", m_nlsProjects, entries));
    }
  } // end class P_MenuListener
}
