/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.ui.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.fields.ISmartFieldListener;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.fields.ISmartFieldModel;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.fields.SmartField;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.fields.TextField;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
import org.eclipse.scout.sdk.s2e.nls.model.NlsEntry;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;

/**
 * <h4>AbstractNlsEntryDialog</h4>
 */
public abstract class AbstractNlsEntryDialog extends TitleAreaDialog {

  protected static final String DIALOG_SETTINGS_WIDTH = "dialogSettingsWidth";
  protected static final String DIALOG_SETTINGS_HEIGHT = "dialogSettingsHeight";
  protected static final String DIALOG_SETTINGS_X = "dialogSettingsX";
  protected static final String DIALOG_SETTINGS_Y = "dialogSettingsY";

  private NlsEntry m_nlsEntry;
  private INlsProject m_nlsProject;
  private boolean m_showProjectList;
  private boolean m_keyToClipboard;

  private final String m_title;
  private final Map<Language, TextField<String>> m_translationFields;
  private final INlsProject m_rootProject;

  private TextField<String> m_keyField;
  private SmartField m_projectProposalField;
  private Composite m_fixDialogArea;
  private Button m_copyKeyToClipboard;
  private Display m_display;

  protected AbstractNlsEntryDialog(Shell parentShell, String title, NlsEntry row, INlsProject project, boolean showProjectList) {
    super(parentShell);
    m_display = parentShell.getDisplay();
    m_nlsProject = project;
    m_title = title;
    m_nlsEntry = row;
    m_rootProject = project;
    m_translationFields = new HashMap<>();
    m_showProjectList = showProjectList;
    setHelpAvailable(true);
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
    return NlsCore.getDefault().getDialogSettingsSection(AbstractNlsEntryDialog.class.getName() + ".dialogBounds");
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    setTitle(m_title);
    postCreate();

    getButton(IDialogConstants.OK_ID).setText("&Ok");
    getDefaultTranslationField().setFocus();

    // as defined in IScoutHelpContextIds.SCOUT_ENTRY_WIZARD_PAGE
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.scout.sdk.s2e.ui.scout_wizard_nls-entry_page_context");
    return contents;
  }

  @Override
  protected final Control createDialogArea(final Composite parent) {
    if (m_fixDialogArea == null) {
      m_fixDialogArea = new Composite(parent, SWT.NONE);
      GridLayoutFactory
          .swtDefaults()
          .applyTo(m_fixDialogArea);
      GridDataFactory
          .defaultsFor(m_fixDialogArea)
          .align(SWT.FILL, SWT.FILL)
          .applyTo(m_fixDialogArea);
    }
    final Composite rootArea = new Composite(m_fixDialogArea, SWT.NONE);
    Composite inputComp = new Composite(rootArea, SWT.NONE);

    P_ProjectSmartfieldModel model = new P_ProjectSmartfieldModel();
    if (model.getProposals("").size() < 2) {
      m_showProjectList = false;
    }
    m_projectProposalField = new SmartField(inputComp, SWT.NONE, 10);
    m_projectProposalField.setLabel("Create in");
    m_projectProposalField.setSmartFieldModel(new P_ProjectSmartfieldModel());
    m_projectProposalField.setValue(getNlsProject());
    m_projectProposalField.addSmartFieldListener(new ISmartFieldListener() {
      @Override
      public void itemSelected(Object item) {
        if (item instanceof INlsProject && m_nlsProject != item) {
          m_nlsProject = (INlsProject) item;
          m_nlsEntry = new NlsEntry(m_nlsEntry, m_nlsProject);

          rootArea.dispose();
          createDialogArea(parent);
          postCreate();
          m_fixDialogArea.layout(true, true);
          parent.redraw();

          m_keyField.validate();
        }
      }
    });
    m_projectProposalField.setVisible(isShowProjectList());

    m_keyField = new TextField<>(inputComp, TextField.VALIDATE_ON_MODIFY, "Key Name", 10);
    String key = m_nlsEntry.getKey();
    if (key == null) {
      key = "";
    }
    m_keyField.setValue(key);
    m_keyField.addInputChangedListener(new IInputChangedListener<String>() {
      @Override
      public void inputChanged(String input) {
        m_nlsEntry.setKey(input);
      }
    });

    final TabFolder translationGroup = new TabFolder(rootArea, SWT.NULL);
    translationGroup.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        // on click on a language tab: set focus to text-area
        translationGroup.getItem(translationGroup.getSelectionIndex()).getControl().setFocus();
      }
    });
    m_translationFields.clear();
    for (Language l : m_nlsProject.getAllLanguages()) {
      TranslationField field = new TranslationField(l);
      TextField<String> control = field.create(translationGroup);
      control.setValue("");

      TabItem tabItem = new TabItem(translationGroup, SWT.NULL);
      tabItem.setText(l.getDispalyName());
      tabItem.setControl(control);

      GridDataFactory
          .defaultsFor(control)
          .align(SWT.FILL, SWT.FILL)
          .minSize(0, 80)
          .applyTo(control);

      field.setText(m_nlsEntry.getTranslation(l));
      m_translationFields.put(l, control);
    }

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

  public final TextField<String> getKeyField() {
    return m_keyField;
  }

  public final SmartField getProjectProposalField() {
    return m_projectProposalField;
  }

  public NlsEntry show() {
    if (open() == OK) {
      if (m_keyToClipboard) {
        copyKeyToClipboard();
      }
      return m_nlsEntry;
    }
    return null;
  }

  /**
   *
   */
  private void copyKeyToClipboard() {
    if (m_nlsEntry != null) {
      String key = getNlsEntry().getKey();
      if (key != null && key.length() > 0) {
        Clipboard clipboard = new Clipboard(m_display);
        String rtfData = "{\\rtf1\\b\\i " + key + "}"; // formatted as bold and italic
        TextTransfer textTransfer = TextTransfer.getInstance();
        RTFTransfer rtfTransfer = RTFTransfer.getInstance();
        Transfer[] transfers = new Transfer[]{textTransfer, rtfTransfer};
        Object[] data = new Object[]{key, rtfData};
        clipboard.setContents(data, transfers);
        clipboard.dispose();
      }
    }
  }

  protected void postCreate() {
  }

  protected abstract void revalidate();

  protected TextField<String> getTranslationField(Language language) {
    return m_translationFields.get(language);
  }

  protected TextField<String> getDefaultTranslationField() {
    return getTranslationField(Language.LANGUAGE_DEFAULT);
  }

  public void setMessage(IStatus status) {
    int sev = IMessageProvider.NONE;
    switch (status.getSeverity()) {
      case IStatus.ERROR: {
        sev = IMessageProvider.ERROR;
        break;
      }
      case IStatus.WARNING: {
        sev = IMessageProvider.WARNING;
        break;
      }
      case IStatus.INFO: {
        sev = IMessageProvider.INFORMATION;
        break;
      }
    }

    setMessage(status.getMessage(), sev);
  }

  public NlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public INlsProject getNlsProject() {
    return m_nlsProject;
  }

  private boolean isShowProjectList() {
    return m_showProjectList;
  }

  protected class TranslationField {
    private TextField<String> m_input;
    private Language m_language;

    protected TranslationField(Language language) {
      m_language = language;
    }

    protected TextField<String> create(Composite parent) {
      m_input = new TextField<>(parent, TextField.VALIDATE_ON_MODIFY | TextField.MULTI_LINE_TEXT_FIELD, "");
      m_input.setLabelVisible(false);
      m_input.addInputChangedListener(new IInputChangedListener<String>() {
        @Override
        public void inputChanged(String input) {
          if (input == null || input.length() < 1) {
            m_nlsEntry.removeTranslation(m_language);
          }
          else {
            m_nlsEntry.addTranslation(m_language, input);
          }
        }
      });
      m_input.addInputChangedListener(new IInputChangedListener<String>() {
        private String m_oldInput;

        @Override
        public void inputChanged(String input) {
          if (m_keyField.isEnabled() && input != null && input.length() > 0) {
            String oldKey = getNlsProject().generateNewKey(m_oldInput);
            String curVal = m_keyField.getValue();
            if (curVal != null && curVal.length() < 1) {
              curVal = null;
            }
            if (curVal == null || Objects.equals(curVal, oldKey)) {
              m_keyField.setValue(getNlsProject().generateNewKey(input));
            }
          }
          m_oldInput = input;
        }
      });
      return m_input;
    }

    public void setText(String text) {
      if (text == null) {
        text = "";
      }
      m_input.setValue(text);
    }
  }

  private class P_ProjectSmartfieldModel implements ISmartFieldModel {
    @Override
    public List<Object> getProposals(String pattern) {
      List<Object> collector = new ArrayList<>();
      INlsProject p = m_rootProject;
      do {
        List<Language> languages = p.getAllLanguages();
        boolean writeableLangAvailable = languages.size() > 0 && !p.getTranslationResource(languages.get(0)).isReadOnly();
        boolean patternMatches = pattern == null || pattern.length() < 1 || p.getName().toLowerCase().startsWith(pattern.toLowerCase());
        if (writeableLangAvailable && patternMatches) {
          collector.add(p);
        }
      }
      while ((p = p.getParent()) != null);

      return collector;
    }

    @Override
    public String getText(Object item) {
      if (item instanceof INlsProject) {
        return ((INlsProject) item).getName();
      }
      return null;
    }

    @Override
    public Image getImage(Object item) {
      return null;
    }
  }
}
