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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.internal.ui.TextField;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * <h4>AbstractNlsEntryDialog</h4>
 */
public abstract class AbstractNlsEntryDialog extends TitleAreaDialog {

  private String m_title;
  private TextField<String> m_keyField;
  // private TextField<String> m_defaultTranslationField;
  private NlsEntry m_nlsEntry;

  public NlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public INlsProject getNlsProject() {
    return m_nlsProject;
  }

  private INlsProject m_nlsProject;
  private HashMap<Language, TextField<String>> m_translationFields;

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
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    postCreate();
    return contents;
  }

  @Override
  protected final Control createDialogArea(Composite parent) {
    Composite rootArea = new Composite(parent, SWT.NONE);
    Composite inputComp = new Composite(rootArea, SWT.NONE);
    m_keyField = new TextField<String>(inputComp, TextField.VALIDATE_ON_MODIFY, "Key Name");
    String key = m_nlsEntry.getKey();
    if (key == null) {
      key = "";
    }
    m_keyField.setValue(key);
    m_keyField.addInputChangedListener(new IInputChangedListener<String>() {
      public void inputChanged(String input) {
        m_nlsEntry.setKey(input);
      }
    });

    Group translationGroup = new Group(rootArea, SWT.NONE);
    for (Language l : m_nlsProject.getAllLanguages()) {
      TranslationField field = new TranslationField(l);
      TextField<String> control = field.create(translationGroup);
      control.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
      field.setText(m_nlsEntry.getTranslation(l));
      m_translationFields.put(l, control);
    }

    // layout
    rootArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    rootArea.setLayout(new GridLayout(1, true));
    GridData data = new GridData(300, SWT.DEFAULT);
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    inputComp.setLayoutData(data);
    inputComp.setLayout(new GridLayout(1, true));
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    m_keyField.setLayoutData(data);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
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

  protected class TranslationField {
    private TextField<String> m_input;
    private Language m_language;

    public TranslationField(Language language) {
      m_language = language;
    }

    public TextField<String> create(Composite parent) {
      m_input = new TextField<String>(parent, TextField.VALIDATE_ON_MODIFY, m_language.getDispalyName());
      m_input.addInputChangedListener(new IInputChangedListener<String>() {
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
