/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page;

import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.fields.TextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class WsdlSelectionWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_CREATE_NEW_WSDL = "createNewWsdl";
  public static final String PROP_WSDL_FOLDER = "wsdlFolder";

  private BasicPropertySupport m_propertySupport;

  private IScoutBundle m_bundle;

  private Button m_newWsdlRadioButton;
  private Button m_existingWsdlRadioButton;

  private Text m_descriptionField;
  private TextField m_wsdlFolderField;
  private Button m_browseButton;
  private IFolder m_rootWsdlFolder;

  public WsdlSelectionWizardPage(IScoutBundle bundle) {
    super(WsdlSelectionWizardPage.class.getName());
    m_bundle = bundle;
    m_propertySupport = new BasicPropertySupport(this);
    setDescription(Texts.get("ChooseWsdlFile"));
  }

  @Override
  protected void createContent(Composite parent) {
    m_newWsdlRadioButton = new Button(parent, SWT.RADIO);
    m_newWsdlRadioButton.setText(Texts.get("CreateWsdlFromScratch"));
    m_newWsdlRadioButton.setSelection(isNewWsdl());
    m_newWsdlRadioButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          setStateChanging(true);
          setNewWsdl(m_newWsdlRadioButton.getSelection());
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_existingWsdlRadioButton = new Button(parent, SWT.RADIO);
    m_existingWsdlRadioButton.setText(Texts.get("UseExistingWsdlFile"));
    m_existingWsdlRadioButton.setSelection(!isNewWsdl());

    // WSDL folder
    m_descriptionField = new Text(parent, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
    m_descriptionField.setEnabled(false);
    m_descriptionField.setForeground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    m_descriptionField.setText(Texts.get("ChooseFolderForWsdlFileAndArtefacts"));

    m_wsdlFolderField = new TextField(parent);
    m_wsdlFolderField.setLabelText(Texts.get("WsdlFolder"));
    m_wsdlFolderField.getTextComponent().setBackground(JaxWsSdkUtility.getColorLightGray());
    m_wsdlFolderField.setEditable(false);

    m_browseButton = new Button(parent, SWT.PUSH | SWT.FLAT);
    m_browseButton.setText(Texts.get("Browse"));
    m_browseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IFolder folder = JaxWsSdkUtility.openProjectFolderDialog(
            m_bundle,
            new WsdlFolderViewerFilter(m_bundle, getRootWsdlFolder()),
            Texts.get("WsdlFolder"),
            Texts.get("ChooseFolderForWsdlFileAndArtefacts"),
            m_rootWsdlFolder, // root folder
            getWsdlFolder());
        if (folder != null) {
          setWsdlFolder(folder);
        }
      }

    });
    IFolder folder = getWsdlFolder();
    if (folder != null) {
      m_wsdlFolderField.setText(folder.getProjectRelativePath().toPortableString());
    }

    // layout
    parent.setLayout(new FormLayout());

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_newWsdlRadioButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_newWsdlRadioButton, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_existingWsdlRadioButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_newWsdlRadioButton, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_existingWsdlRadioButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_existingWsdlRadioButton, 20, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_descriptionField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_descriptionField, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 50);
    formData.right = new FormAttachment(100, -75);
    m_wsdlFolderField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_wsdlFolderField, 0, SWT.TOP);
    formData.left = new FormAttachment(100, -70);
    formData.right = new FormAttachment(100, 0);
    m_browseButton.setLayoutData(formData);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (m_bundle == null) { // not fully initialized yet
      return;
    }
    if (getWsdlFolder() == null) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", m_wsdlFolderField.getText())));
    }
  }

  public void setNewWsdl(boolean newWsdl) {
    try {
      setStateChanging(true);
      setNewWsdlInternal(newWsdl);
      if (isControlCreated()) {
        m_newWsdlRadioButton.setSelection(newWsdl);
        m_existingWsdlRadioButton.setSelection(!newWsdl);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setNewWsdlInternal(boolean newWsdl) {
    m_propertySupport.setProperty(PROP_CREATE_NEW_WSDL, newWsdl);
  }

  public boolean isNewWsdl() {
    return m_propertySupport.getPropertyBool(PROP_CREATE_NEW_WSDL);
  }

  public void setWsdlFolder(IFolder wsdlFolder) {
    try {
      setStateChanging(true);
      setWsdlFolderInternal(wsdlFolder);
      if (isControlCreated()) {
        if (wsdlFolder != null) {
          m_wsdlFolderField.setText(wsdlFolder.getProjectRelativePath().toPortableString());
        }
        else {
          m_wsdlFolderField.setText("");
        }
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setWsdlFolderInternal(IFolder wsdlFolder) {
    m_propertySupport.setProperty(PROP_WSDL_FOLDER, wsdlFolder);
  }

  public IFolder getWsdlFolder() {
    return (IFolder) m_propertySupport.getProperty(PROP_WSDL_FOLDER);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
    super.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
    super.removePropertyChangeListener(listener);
  }

  public IFolder getRootWsdlFolder() {
    return m_rootWsdlFolder;
  }

  public void setRootWsdlFolder(IFolder rootWsdlFolder) {
    m_rootWsdlFolder = rootWsdlFolder;
  }
}
