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
package org.eclipse.scout.nls.sdk.internal.ui.dialog.language;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.internal.ui.smartfield.ISmartFieldListener;
import org.eclipse.scout.nls.sdk.internal.ui.smartfield.SmartField;
import org.eclipse.scout.nls.sdk.internal.ui.wizard.TranslationLocationSmartFieldModel;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/** <h4>LanguageNewDialog</h4> */
public class TranslationFileNewDialog extends TitleAreaDialog {
  private SmartField m_languageChooser;
  private Composite m_rootArea;
  private SmartField m_folderSelection;
  private final TranslationFileNewModel m_model;

  // model
  private SmartField m_languageCountryChooser;

  /**
   * @param parentShell
   */
  public TranslationFileNewDialog(Shell parentShell, TranslationFileNewModel model) {
    super(parentShell);
    m_model = model;
    m_model.addPropertyChangeListener(new P_ModelPropertyListener());
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    revalidate();
    return contents;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    m_rootArea = new Composite(parent, SWT.NONE);

    m_languageChooser = new SmartField(m_rootArea, SWT.NONE);
    m_languageChooser.setLabel("Language");
    m_languageChooser.setSmartFieldModel(new LanguageSmartFieldModel());
    m_languageChooser.addSmartFieldListener(new ISmartFieldListener() {
      public void itemSelected(Object item) {
        String languageIso = null;
        if (item != null) {
          languageIso = ((Locale) item).getLanguage();
        }
        m_model.setLanguageIso(languageIso);
      }
    });

    m_languageCountryChooser = new SmartField(m_rootArea, SWT.NONE);
    m_languageCountryChooser.setLabel("Country");
    m_languageCountryChooser.setSmartFieldModel(new CountrySmartFieldModel());
    m_languageCountryChooser.addSmartFieldListener(new ISmartFieldListener() {
      public void itemSelected(Object item) {
        String countryIso = null;
        if (item != null) {
          countryIso = ((Locale) item).getCountry();
        }
        m_model.setCountryIso(countryIso);
      }
    });

    attachGridData(m_languageCountryChooser);
    m_folderSelection = new SmartField(m_rootArea, SWT.NONE);
    m_folderSelection.setLabel("Location");
    m_folderSelection.setSmartFieldModel(
        new TranslationLocationSmartFieldModel(m_model.getProject(), m_model.getPath()));
    m_folderSelection.addSmartFieldListener(new ISmartFieldListener() {
      public void itemSelected(Object item) {
        m_model.setFolder((INlsFolder) item);
        revalidate();
      }
    });
    attachGridData(m_folderSelection);

    // layout
    m_rootArea.setLayout(new GridLayout(1, true));
    GridData data = new GridData(300, SWT.DEFAULT);
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    m_languageChooser.setLayoutData(data);
    return m_rootArea;
  }

  private void revalidate() {
    boolean valid = false;
    String langIso = m_model.getLanguageIso();
    if (langIso == null) {
      // must be set
      setMessage("Specify a language to add to the project.", IMessageProvider.WARNING);
      return;
    }
    String countryIso = m_model.getCountryIso();
    if (countryIso == null) countryIso = "";
    String variant = m_model.getCountryIso();
    if (variant == null) variant = "";
    Language lang = new Language(new Locale(langIso, countryIso, variant));

    if (m_model.getNlsProject().containsLanguage(lang)) {
      setMessage("Language already exists.", IMessageProvider.WARNING);
    }
    else if (m_model.getFolder() == null) {
      setMessage("Folder to place the translation file not found.", IMessageProvider.WARNING);
    }
    else {
      setMessage(null);
      valid = true;
    }
    getButton(OK).setEnabled(valid);
  }

  private void attachGridData(Control c) {
    GridData d = new GridData();
    d.grabExcessHorizontalSpace = true;
    d.horizontalAlignment = SWT.FILL;
    c.setLayoutData(d);
  }

  protected void handlePropertyChanged(String propertyName, Object oldVal, Object newVal) {
    revalidate();
  }

  private class P_ModelPropertyListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      handlePropertyChanged(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }

}
