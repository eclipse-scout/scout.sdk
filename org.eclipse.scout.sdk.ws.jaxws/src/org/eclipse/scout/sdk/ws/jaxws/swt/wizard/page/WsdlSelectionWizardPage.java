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

import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class WsdlSelectionWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_CREATE_NEW_WSDL = "createNewWsdl";

  private BasicPropertySupport m_propertySupport;

  private Button m_newWsdlRadioButton;
  private Button m_existingWsdlRadioButton;

  public WsdlSelectionWizardPage() {
    super(WsdlSelectionWizardPage.class.getName());
    m_propertySupport = new BasicPropertySupport(this);
    setDescription(Texts.get("ConfigureWebServiceStub"));
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
}
