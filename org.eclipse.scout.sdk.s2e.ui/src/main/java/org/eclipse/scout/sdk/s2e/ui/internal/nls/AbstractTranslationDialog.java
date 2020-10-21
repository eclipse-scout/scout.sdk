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
package org.eclipse.scout.sdk.s2e.ui.internal.nls;

import static java.util.stream.Collectors.toCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.action.CopyPasteAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractTranslationDialog extends TitleAreaDialog {

  protected static final String DIALOG_SETTINGS_WIDTH = "dialogSettingsWidth";
  protected static final String DIALOG_SETTINGS_HEIGHT = "dialogSettingsHeight";
  protected static final String DIALOG_SETTINGS_X = "dialogSettingsX";
  protected static final String DIALOG_SETTINGS_Y = "dialogSettingsY";

  private final Translation m_nlsEntry;
  private final TranslationStoreStack m_nlsProject;
  private final boolean m_showProjectList;
  private final String m_title;
  private final Map<Language, TextField> m_translationFields;
  private final Display m_display;

  private boolean m_keyToClipboard;
  private ITranslationStore m_store;

  // fields
  private TextField m_keyField;
  private ProposalTextField m_projectProposalField;
  private Composite m_fixDialogArea;
  private Button m_copyKeyToClipboard;

  protected AbstractTranslationDialog(Shell parentShell, String title, ITranslation row, TranslationStoreStack project, boolean showProjectList) {
    super(parentShell);
    m_display = parentShell.getDisplay();
    m_nlsProject = Ensure.notNull(project);
    m_title = Ensure.notNull(title);
    m_nlsEntry = new Translation(row);
    m_store = project.primaryEditableStore().orElse(null);
    m_translationFields = new HashMap<>();
    m_showProjectList = showProjectList && project.allEditableStores().count() > 1;
  }

  @Override
  protected final void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_title);
  }

  @Override
  protected int getShellStyle() {
    return super.getShellStyle() | SWT.RESIZE;
  }

  @Override
  protected IDialogSettings getDialogBoundsSettings() {
    return S2ESdkUiActivator.getDefault().getDialogSettingsSection(AbstractTranslationDialog.class.getName() + ".dialogBounds");
  }

  @Override
  protected Control createContents(Composite parent) {
    var contents = super.createContents(parent);
    setTitle(m_title);

    postCreate();

    getButton(IDialogConstants.OK_ID).setText("&Ok");
    var defaultTranslationField = getDefaultTranslationField();
    if (defaultTranslationField != null) {
      defaultTranslationField.setFocus();
    }
    contents.setEnabled(getNlsProject().isEditable());

    setTitleImage(S2ESdkUiActivator.getImage(ISdkIcons.ScoutProjectNewWizBanner));
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_ENTRY_WIZARD_PAGE);
    setHelpAvailable(true);
    return contents;
  }

  protected void reCreateDialogArea() {
    for (var c : m_fixDialogArea.getChildren()) {
      c.dispose();
    }
    createDialogArea(null);
    postCreate();
    m_fixDialogArea.layout(true, true);
    m_fixDialogArea.getParent().redraw();
  }

  @Override
  protected final Control createDialogArea(Composite parent) {
    if (m_fixDialogArea == null) {
      m_fixDialogArea = new Composite(parent, SWT.NONE);
      GridLayoutFactory
          .swtDefaults()
          .applyTo(m_fixDialogArea);
      GridDataFactory
          .defaultsFor(m_fixDialogArea)
          .align(SWT.FILL, SWT.FILL)
          .grab(true, true)
          .applyTo(m_fixDialogArea);
    }
    var rootArea = new Composite(m_fixDialogArea, SWT.NONE);
    var inputComp = new Composite(rootArea, SWT.NONE);

    // store selection proposal field
    m_projectProposalField = FieldToolkit.createTranslationStoreProposalField(inputComp, "Create in", getNlsProject(), 60);
    m_projectProposalField.acceptProposal(getSelectedStore().orElse(null));
    m_projectProposalField.addProposalListener(item -> {
      if (getSelectedStore().orElse(null) == item) {
        return;
      }
      setSelectedStore((ITranslationStore) item);
      reCreateDialogArea();
    });
    m_projectProposalField.setVisible(isShowProjectList());

    // key field
    m_keyField = new TextField(inputComp, TextField.TYPE_LABEL, 60);
    m_keyField.setLabelText("Key Name");
    m_keyField.setText(m_nlsEntry.key());
    m_keyField.addModifyListener(input -> m_nlsEntry.setKey(m_keyField.getText()));

    // Tabs
    var translationGroup = new TabFolder(rootArea, SWT.NULL);
    translationGroup.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        // on click on a language tab: set focus to text-area
        translationGroup.getItem(translationGroup.getSelectionIndex()).getControl().setFocus();
      }
    });
    Set<Language> languages = getSelectedStore()
        .map(ITranslationStore::languages)
        .orElseGet(Stream::empty)
        .collect(toCollection(TreeSet::new));
    m_translationFields.clear();
    for (var l : languages) {
      var txtFieldContainer = new Composite(translationGroup, SWT.NONE);
      var tabItem = new TabItem(translationGroup, SWT.NULL);
      tabItem.setText(l.displayName());
      tabItem.setControl(txtFieldContainer);

      GridDataFactory
          .defaultsFor(txtFieldContainer)
          .align(SWT.FILL, SWT.FILL)
          .grab(true, true)
          .minSize(0, 80)
          .applyTo(txtFieldContainer);
      GridLayoutFactory
          .swtDefaults()
          .margins(4, 4)
          .applyTo(txtFieldContainer);

      // translation text field for a language
      var field = createTranslationField(txtFieldContainer, l);
      field.setText(m_nlsEntry.text(l).orElse(""));
      field.setSelection(field.getText().length());
      GridDataFactory
          .defaultsFor(field)
          .align(SWT.FILL, SWT.FILL)
          .grab(true, true)
          .applyTo(field);
      m_translationFields.put(l, field);
    }

    // copyToClipboard
    m_copyKeyToClipboard = new Button(rootArea, SWT.CHECK);
    m_copyKeyToClipboard.setText("Copy key to clipboard");
    m_copyKeyToClipboard.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_keyToClipboard = m_copyKeyToClipboard.getSelection();
      }
    });

    // layout
    GridDataFactory
        .defaultsFor(rootArea)
        .align(SWT.FILL, SWT.FILL)
        .grab(true, true)
        .applyTo(rootArea);
    GridLayoutFactory
        .swtDefaults()
        .applyTo(rootArea);
    GridDataFactory
        .defaultsFor(m_projectProposalField)
        .align(SWT.FILL, SWT.CENTER)
        .exclude(!isShowProjectList())
        .applyTo(m_projectProposalField);
    GridLayoutFactory
        .swtDefaults()
        .applyTo(inputComp);
    GridDataFactory
        .defaultsFor(inputComp)
        .hint(300, SWT.DEFAULT)
        .grab(true, false)
        .align(SWT.FILL, SWT.CENTER)
        .applyTo(inputComp);
    GridDataFactory
        .defaultsFor(m_keyField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_keyField);
    GridDataFactory
        .defaultsFor(translationGroup)
        .align(SWT.FILL, SWT.FILL)
        .hint(600, 100)
        .grab(true, true)
        .applyTo(translationGroup);
    GridLayoutFactory
        .swtDefaults()
        .applyTo(translationGroup);
    GridDataFactory
        .defaultsFor(m_copyKeyToClipboard)
        .align(SWT.FILL, SWT.CENTER)
        .applyTo(m_copyKeyToClipboard);
    return m_fixDialogArea;
  }

  protected TextField createTranslationField(Composite parent, Language l) {
    var translationField = new TextField(parent, TextField.TYPE_MULTI_LINE);
    translationField.addModifyListener(event -> {
      var newText = translationField.getText();
      if (Strings.isEmpty(newText)) {
        m_nlsEntry.putText(l, null);
      }
      else {
        m_nlsEntry.putText(l, newText);
      }
    });

    translationField.addModifyListener(new ModifyListener() {
      private String m_oldInput;

      @Override
      public void modifyText(ModifyEvent event) {
        var newText = translationField.getText();
        if (getKeyField().isEnabled() && !Strings.isEmpty(newText)) {
          var oldKey = getNlsProject().generateNewKey(m_oldInput);
          var curVal = getKeyField().getText();
          if (Strings.isBlank(curVal)) {
            curVal = null;
          }
          if (curVal == null || Objects.equals(curVal, oldKey)) {
            m_keyField.setText(getNlsProject().generateNewKey(newText));
          }
        }
        m_oldInput = newText;
      }
    });
    return translationField;
  }

  protected final TextField getKeyField() {
    return m_keyField;
  }

  protected final ProposalTextField getProjectProposalField() {
    return m_projectProposalField;
  }

  public Optional<ITranslation> show() {
    if (open() == OK) {
      if (m_keyToClipboard) {
        copyKeyToClipboard();
      }
      return Optional.ofNullable(m_nlsEntry);
    }
    return Optional.empty();
  }

  private void copyKeyToClipboard() {
    var entry = getNlsEntry();
    if (entry == null) {
      return;
    }
    var key = entry.key();
    if (Strings.isBlank(key)) {
      return;
    }
    new CopyPasteAction("empty", key, m_display).run();
  }

  protected void postCreate() {
  }

  protected abstract void revalidate();

  protected TextField getTranslationField(Language language) {
    return m_translationFields.get(language);
  }

  protected TextField getDefaultTranslationField() {
    return getTranslationField(Language.LANGUAGE_DEFAULT);
  }

  public void setMessage(IStatus status) {
    int sev;
    switch (status.getSeverity()) {
      case IStatus.ERROR:
        sev = IMessageProvider.ERROR;
        break;
      case IStatus.WARNING:
        sev = IMessageProvider.WARNING;
        break;
      case IStatus.INFO:
        sev = IMessageProvider.INFORMATION;
        break;
      default:
        sev = IMessageProvider.NONE;
        break;
    }

    setMessage(status.getMessage(), sev);
  }

  public ITranslation getNlsEntry() {
    return m_nlsEntry;
  }

  public Optional<ITranslationStore> getSelectedStore() {
    return Optional.ofNullable(m_store);
  }

  public void setSelectedStore(ITranslationStore store) {
    m_store = store;
  }

  public TranslationStoreStack getNlsProject() {
    return m_nlsProject;
  }

  private boolean isShowProjectList() {
    return m_showProjectList;
  }
}
