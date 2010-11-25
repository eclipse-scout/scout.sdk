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
package org.eclipse.scout.sdk.ui.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class AxisWebServiceConsumerDialog extends AbstractStatusDialog {

  private StyledTextField m_wsdlUrlField;
  private StyledTextField m_usernameField;
  private StyledTextField m_passwordField;

  private String m_wsdlUrl;
  private String m_username;
  private String m_password;

  public AxisWebServiceConsumerDialog(Shell parentShell) {
    super(parentShell);
    setTitle("New Webservice Consumer");
    setMessage("Create a new Webservice consumer.");
    setTitleImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_NEW_WIZARD));
  }

  @Override
  protected Control createDialogArea(Composite originalParent) {
    Composite parent = new Composite(originalParent, SWT.NONE);

    m_wsdlUrlField = getFieldToolkit().createStyledTextField(parent, "WSDL URL");
    m_wsdlUrlField.setText(getWsdlUrl());
    m_wsdlUrlField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_wsdlUrl = m_wsdlUrlField.getText();
        pingStateChanging();
      }
    });
    m_usernameField = getFieldToolkit().createStyledTextField(parent, "Username");
    m_usernameField.setText(getWsdlUrl());
    m_usernameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        setUsername(m_usernameField.getText());
        pingStateChanging();
      }
    });
    m_passwordField = getFieldToolkit().createStyledTextField(parent, "Password");
    m_passwordField.setText(getWsdlUrl());
    m_passwordField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        setPassword(m_passwordField.getText());
        pingStateChanging();
      }
    });
    // layout
    parent.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    parent.setLayout(new GridLayout(1, true));
    m_wsdlUrlField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_usernameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_passwordField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return parent;
  }

  @Override
  protected void validate(MultiStatus multiStatus) {
    super.validate(multiStatus);
    if (StringUtility.isNullOrEmpty(getWsdlUrl())) {
      multiStatus.add(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "WSDL URL can not be empty."));
    }
    else {
      multiStatus.add(Status.OK_STATUS);
    }
  }

  public void setWsdlUrl(String wsdlUrl) {
    m_wsdlUrl = wsdlUrl;
  }

  public String getWsdlUrl() {
    return m_wsdlUrl;
  }

  public void setUsername(String username) {
    m_username = username;
  }

  public String getUsername() {
    return m_username;
  }

  public void setPassword(String password) {
    m_password = password;
  }

  public String getPassword() {
    return m_password;
  }

}
