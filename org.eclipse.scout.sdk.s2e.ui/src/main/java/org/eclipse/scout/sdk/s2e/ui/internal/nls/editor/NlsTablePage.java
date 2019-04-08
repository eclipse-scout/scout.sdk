/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.action.CopyPasteAction;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.action.FindReferencesAction;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.action.LanguageNewAction;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.action.TranslationModifyAction;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.action.TranslationNewAction;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.action.TranslationRefreshAction;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.action.TranslationRemoveAction;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.action.UpdateReferenceCountAction;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.importexport.TranslationExportAction;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.importexport.TranslationImportAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>{@link NlsTablePage}</h3>
 *
 * @since 7.0.0
 */
public class NlsTablePage extends Composite {

  private final TranslationStoreStack m_stack;

  private Button m_hideReadOnly;
  private NlsTable m_view;
  private NlsTableController m_controller;

  public NlsTablePage(Composite parent, TranslationStoreStack stack) {
    super(parent, SWT.NONE);
    m_stack = Ensure.notNull(stack);

    FormToolkit toolkit = new FormToolkit(getDisplay());
    Form form = toolkit.createForm(this);
    form.setText("Translations");
    createContent(toolkit, form.getBody());

    setLayout(new FillLayout());
    createFormMenu(form.getToolBarManager());
    form.updateToolBar();
  }

  protected void createContent(FormToolkit toolkit, Composite parent) {
    m_controller = new NlsTableController(stack());
    Composite rootArea = toolkit.createComposite(parent);

    m_hideReadOnly = toolkit.createButton(rootArea, "Hide read-only rows", SWT.CHECK);
    m_hideReadOnly.setSelection(true);
    m_hideReadOnly.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_controller.setHideReadOnly(m_hideReadOnly.getSelection());
      }
    });

    m_view = new NlsTable(rootArea, m_controller);
    m_view.addMenuListener(new P_MenuListener());
    m_controller.bind(m_view);
    m_controller.setHideReadOnly(m_hideReadOnly.getSelection());

    // layout
    parent.setLayout(new FillLayout());
    GridLayoutFactory
        .swtDefaults()
        .applyTo(rootArea);
    GridDataFactory
        .defaultsFor(m_hideReadOnly)
        .align(SWT.FILL, SWT.CENTER)
        .applyTo(m_hideReadOnly);
    GridDataFactory
        .fillDefaults()
        .align(SWT.FILL, SWT.FILL)
        .grab(true, true)
        .applyTo(m_view);
  }

  protected void createFormMenu(IContributionManager manager) {
    manager.add(new TranslationRefreshAction(stack(), m_view));
    manager.add(new UpdateReferenceCountAction(m_controller, m_view.getDisplay()));
    manager.add(new TranslationNewAction(m_view.getShell(), stack(), m_controller));
    manager.add(new LanguageNewAction(stack(), m_view.getShell()));
    manager.add(new TranslationImportAction(stack(), m_view.getShell()));
    manager.add(new TranslationExportAction(stack(), m_view.getShell()));
  }

  public TranslationStoreStack stack() {
    return m_stack;
  }

  private final class P_MenuListener implements IMenuListener {

    @Override
    public void menuAboutToShow(IMenuManager manager) {
      List<ITranslationEntry> entries = m_controller.getSelectedEntries();
      if (entries.size() == 1) {
        addSingleSelectMenus(manager, m_view.getCursorSelection().get());
      }
      else if (entries.size() > 1) {
        addMultiSelectMenus(manager, entries);
      }
    }

    private void addSingleSelectMenus(IContributionManager manager, NlsTableCell cursorSelection) {
      if (cursorSelection.store().isEditable()) {
        if (cursorSelection.column() == NlsTableController.INDEX_COLUMN_KEYS) {
          manager.add(new Action("Edit key") {
            @Override
            public void run() {
              m_view.showEditor();
            }
          });
          manager.add(new Separator());
        }
        manager.add(new TranslationModifyAction(getShell(), cursorSelection.entry(), stack()));
        manager.add(new Separator());
      }
      manager.add(new FindReferencesAction(stack(), cursorSelection.entry().key()));

      String text = cursorSelection.text();
      if (!Strings.isEmpty(text)) {
        manager.add(new Separator());
        manager.add(new CopyPasteAction("Copy", text, m_view.getDisplay()));
      }

      if (cursorSelection.store().isEditable()) {
        manager.add(new Separator());
        manager.add(new TranslationRemoveAction(stack(), cursorSelection.entry()));
      }
    }

    private void addMultiSelectMenus(IContributionManager manager, List<ITranslationEntry> entries) {
      manager.add(new TranslationRemoveAction("Remove entries", stack(), entries));
    }
  }
}