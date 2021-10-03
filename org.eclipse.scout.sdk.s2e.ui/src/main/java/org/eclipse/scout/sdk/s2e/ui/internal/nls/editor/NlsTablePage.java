/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
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
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>{@link NlsTablePage}</h3>
 *
 * @since 7.0.0
 */
public class NlsTablePage extends Composite {

  private final TranslationManager m_manager;

  private Button m_hideReadOnly;
  private NlsTable m_view;
  private NlsTableController m_controller;

  public NlsTablePage(Composite parent, TranslationManager manager) {
    super(parent, SWT.NONE);
    m_manager = Ensure.notNull(manager);

    var toolkit = new FormToolkit(getDisplay());
    //noinspection ThisEscapedInObjectConstruction
    var form = toolkit.createForm(this);
    form.setText("Translations");
    createContent(toolkit, form.getBody());

    setLayout(new FillLayout());
    createFormMenu(form.getToolBarManager());
    form.updateToolBar();
  }

  protected void createContent(FormToolkit toolkit, Composite parent) {
    m_controller = new NlsTableController(translationManager());
    var rootArea = toolkit.createComposite(parent);

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
    manager.add(new TranslationRefreshAction(translationManager(), m_view));
    manager.add(new UpdateReferenceCountAction(m_controller, m_view.getDisplay()));
    manager.add(new TranslationNewAction(m_view.getShell(), translationManager(), m_controller));
    manager.add(new LanguageNewAction(translationManager(), m_view.getShell()));
    manager.add(new TranslationImportAction(translationManager(), m_view.getShell()));
    manager.add(new TranslationExportAction(translationManager(), m_view.getShell()));
  }

  public TranslationManager translationManager() {
    return m_manager;
  }

  private final class P_MenuListener implements IMenuListener {

    @Override
    public void menuAboutToShow(IMenuManager manager) {
      var entries = m_controller.getSelectedEntries();
      if (entries.size() == 1) {
        addSingleSelectMenus(manager, m_view.getCursorSelection().orElseThrow());
      }
      else if (entries.size() > 1) {
        addMultiSelectMenus(manager, entries);
      }
    }

    private void addSingleSelectMenus(IContributionManager manager, NlsTableCell cursorSelection) {
      if (cursorSelection.translation().hasEditableStores()) {
        if (cursorSelection.column() == NlsTableController.INDEX_COLUMN_KEYS && cursorSelection.translation().hasOnlyEditableStores()) {
          manager.add(new Action("Edit key") {
            @Override
            public void run() {
              m_view.showEditor();
            }
          });
          manager.add(new Separator());
        }
        manager.add(new TranslationModifyAction(getShell(), cursorSelection.translation(), translationManager()));
        manager.add(new Separator());
      }
      manager.add(new FindReferencesAction(translationManager(), cursorSelection.translation().key()));

      var text = cursorSelection.text();
      if (!Strings.isEmpty(text)) {
        manager.add(new Separator());
        manager.add(new CopyPasteAction("Copy", text, m_view.getDisplay()));
      }

      if (cursorSelection.translation().hasOnlyEditableStores()) {
        manager.add(new Separator());
        manager.add(new TranslationRemoveAction(translationManager(), cursorSelection.translation()));
      }
    }

    private void addMultiSelectMenus(IContributionManager manager, List<IStackedTranslation> entries) {
      if (entries.stream().allMatch(IStackedTranslation::hasOnlyEditableStores)) {
        manager.add(new TranslationRemoveAction("Remove entries", translationManager(), entries));
      }
    }
  }
}
