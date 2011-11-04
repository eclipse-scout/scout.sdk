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

import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.internal.ui.TextField;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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

  private final String m_title;
  private final NlsEntry m_nlsEntry;
  private final INlsProject m_nlsProject;

  private final HashMap<Language, TextField<String>> m_translationFields;
  private TextField<String> m_keyField;

  public AbstractNlsEntryDialog(Shell parentShell, String title, NlsEntry row, INlsProject project) {
    super(parentShell);
    m_nlsProject = project;
    m_title = title;
    m_nlsEntry = row;
    m_translationFields = new HashMap<Language, TextField<String>>();
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
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    setTitle(m_title);
    postCreate();

    getButton(IDialogConstants.OK_ID).setText("&Ok");
    return contents;
  }

  @Override
  protected final Control createDialogArea(Composite parent) {
    Composite rootArea = new Composite(parent, SWT.NONE);
    Composite inputComp = new Composite(rootArea, SWT.NONE);
    m_keyField = new TextField<String>(inputComp, TextField.VALIDATE_ON_MODIFY | TextField.LABEL_NO_ALIGN, "Key Name");
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
    for (Language l : m_nlsProject.getAllLanguages()) {
      TranslationField field = new TranslationField(l);
      TextField<String> control = field.create(translationGroup);
      control.setValue("");

      TabItem tabItem = new TabItem(translationGroup, SWT.NULL);
      tabItem.setText(l.getDispalyName());
      tabItem.setControl(control);

      control.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
      field.setText(m_nlsEntry.getTranslation(l));
      m_translationFields.put(l, control);
    }

    // layout
    rootArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    rootArea.setLayout(new GridLayout(1, true));

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
    return rootArea;
  }

  public final TextField<String> getKeyField() {
    return m_keyField;
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

  protected class TranslationField {
    private TextField<String> m_input;
    private Language m_language;

    public TranslationField(Language language) {
      m_language = language;
    }

    public TextField<String> create(Composite parent) {
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
      return m_input;
    }

    public void setText(String text) {
      if (text == null) {
        text = "";
      }
      m_input.setValue(text);
    }
  }
}
