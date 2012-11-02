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
package org.eclipse.scout.nls.sdk.internal.ui.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.TextField;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.nls.sdk.internal.ui.smartfield.ISmartFieldListener;
import org.eclipse.scout.nls.sdk.internal.ui.smartfield.ISmartFieldModel;
import org.eclipse.scout.nls.sdk.internal.ui.smartfield.SmartField;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

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

  private final String m_title;
  private final HashMap<Language, TextField<String>> m_translationFields;
  private final INlsProject m_rootProject;

  private TextField<String> m_keyField;
  private SmartField m_projectProposalField;
  private Composite m_fixDialogArea;

  protected AbstractNlsEntryDialog(Shell parentShell, String title, NlsEntry row, INlsProject project, boolean showProjectList) {
    super(parentShell);
    m_nlsProject = project;
    m_title = title;
    m_nlsEntry = row;
    m_rootProject = project;
    m_translationFields = new HashMap<Language, TextField<String>>();
    m_showProjectList = showProjectList;
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
    return contents;
  }

  @Override
  protected final Control createDialogArea(final Composite parent) {
    if (m_fixDialogArea == null) {
      m_fixDialogArea = new Composite(parent, SWT.NONE);
      m_fixDialogArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
      m_fixDialogArea.setLayout(new GridLayout(1, true));
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
        if (item instanceof INlsProject) {
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

    m_keyField = new TextField<String>(inputComp, TextField.VALIDATE_ON_MODIFY, "Key Name", 10);
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

      GridData txtGd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
      txtGd.minimumHeight = 80;
      control.setLayoutData(txtGd);
      field.setText(m_nlsEntry.getTranslation(l));
      m_translationFields.put(l, control);
    }

    // layout
    rootArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    rootArea.setLayout(new GridLayout(1, true));

    GridData plgd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    plgd.exclude = !isShowProjectList();
    m_projectProposalField.setLayoutData(plgd);

    GridData data = new GridData(300, SWT.DEFAULT);
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    inputComp.setLayoutData(data);
    inputComp.setLayout(new GridLayout(1, true));

    data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    m_keyField.setLayoutData(data);

    data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    data.widthHint = 600;
    data.heightHint = 100;
    translationGroup.setLayoutData(data);
    translationGroup.setLayout(new GridLayout(1, true));
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
      return m_nlsEntry;
    }
    return null;
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
      m_input = new TextField<String>(parent, TextField.VALIDATE_ON_MODIFY | TextField.MULTI_LINE_TEXT_FIELD, "");
      m_input.setLabelVisible(false);
      m_input.addInputChangedListener(new IInputChangedListener<String>() {
        @Override
        public void inputChanged(String input) {
          if (StringUtility.isNullOrEmpty(input)) {
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
          if (m_keyField.isEnabled() && StringUtility.hasText(input)) {
            String oldKey = getNlsProject().generateNewKey(m_oldInput);
            String curVal = m_keyField.getValue();
            if (!StringUtility.hasText(curVal)) {
              curVal = null;
            }
            if (!StringUtility.hasText(curVal) || CompareUtility.equals(curVal, oldKey)) {
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
      ArrayList<Object> collector = new ArrayList<Object>();
      INlsProject p = m_rootProject;
      do {
        Language[] languages = p.getAllLanguages();
        if (languages.length > 0 && !p.getTranslationResource(languages[0]).isReadOnly()) {
          if (!StringUtility.hasText(pattern) || p.getName().toLowerCase().startsWith(pattern.toLowerCase())) {
            collector.add(p);
          }
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
      else {
        return null;
      }
    }

    @Override
    public Image getImage(Object item) {
      return null;
    }
  }
}
