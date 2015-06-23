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
package org.eclipse.scout.sdk.s2e.nls.internal.simpleProject.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleProject.INlsFolder;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleProject.model.CountrySmartFieldModel;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleProject.model.LanguageSmartFieldModel;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleProject.model.TranslationFileNewModel;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleProject.model.TranslationLocationSmartFieldModel;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.fields.ISmartFieldListener;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.fields.SmartField;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class TranslationFileNewDialog extends TitleAreaDialog {

  private SmartField m_languageChooser;
  private SmartField m_folderSelection;
  private SmartField m_languageCountryChooser;

  private final TranslationFileNewModel m_model;
  private final String m_title;

  public TranslationFileNewDialog(Shell parentShell, TranslationFileNewModel model) {
    super(parentShell);
    m_title = "Add a Language";
    m_model = model;
    m_model.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        revalidate();
      }
    });
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_title);
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    setTitle(m_title);
    revalidate();
    return contents;
  }

  @Override
  protected Control createDialogArea(Composite p) {
    Composite rootArea = new Composite(p, SWT.NONE);

    m_languageChooser = new SmartField(rootArea, SWT.NONE, 20);
    m_languageChooser.setLabel("Language");
    m_languageChooser.setSmartFieldModel(new LanguageSmartFieldModel());
    m_languageChooser.addSmartFieldListener(new ISmartFieldListener() {
      @Override
      public void itemSelected(Object item) {
        String languageIso = null;
        if (item != null) {
          languageIso = ((Locale) item).getLanguage();
        }
        m_model.setLanguageIso(languageIso);
      }
    });

    m_languageCountryChooser = new SmartField(rootArea, SWT.NONE, 20);
    m_languageCountryChooser.setLabel("Country");
    m_languageCountryChooser.setSmartFieldModel(new CountrySmartFieldModel());
    m_languageCountryChooser.addSmartFieldListener(new ISmartFieldListener() {
      @Override
      public void itemSelected(Object item) {
        String countryIso = null;
        if (item != null) {
          countryIso = ((Locale) item).getCountry();
        }
        m_model.setCountryIso(countryIso);
      }
    });

    m_folderSelection = new SmartField(rootArea, SWT.NONE, 20);
    m_folderSelection.setLabel("Folder");
    TranslationLocationSmartFieldModel m = new TranslationLocationSmartFieldModel(m_model.getProject(), m_model.getPath());
    m_folderSelection.setSmartFieldModel(m);
    m_folderSelection.addSmartFieldListener(new ISmartFieldListener() {
      @Override
      public void itemSelected(Object item) {
        m_model.setFolder((INlsFolder) item);
      }
    });
    List<Object> folders = m.getProposals(null);
    if (folders.size() == 1) {
      m_folderSelection.setValue(folders.get(0));
    }

    attachGridData(m_folderSelection);
    attachGridData(m_languageCountryChooser);
    attachGridData(m_languageChooser);

    // layout
    rootArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    rootArea.setLayout(new GridLayout(1, true));
    return rootArea;
  }

  private void revalidate() {
    Button okButton = getButton(IDialogConstants.OK_ID);
    if (okButton != null) {
      okButton.setEnabled(false);
    }

    // check that language is specified
    String langIso = m_model.getLanguageIso();
    if (langIso == null) {
      setMessage("Specify a language to add to the project.", IMessageProvider.WARNING);
      return;
    }

    // check if language already exists
    String countryIso = m_model.getCountryIso();
    if (countryIso == null) {
      countryIso = "";
    }
    Language lang = new Language(new Locale(langIso, countryIso));
    if (m_model.getNlsProject().containsLanguage(lang)) {
      setMessage("This Language already exists.", IMessageProvider.WARNING);
      return;
    }

    // check if folder is valid
    if (m_model.getFolder() == null) {
      setMessage("Specify a folder where to create the translation file.", IMessageProvider.WARNING);
      return;
    }
    else if (!m_model.getFolder().getFolder().exists()) {
      setMessage("The folder could not be found.", IMessageProvider.WARNING);
      return;
    }

    setMessage("Adds a new Language to the project.");
    if (okButton != null) {
      okButton.setEnabled(true);
    }
  }

  private void attachGridData(Control c) {
    GridData d = new GridData();
    d.grabExcessHorizontalSpace = true;
    d.horizontalAlignment = SWT.FILL;
    c.setLayoutData(d);
  }
}
