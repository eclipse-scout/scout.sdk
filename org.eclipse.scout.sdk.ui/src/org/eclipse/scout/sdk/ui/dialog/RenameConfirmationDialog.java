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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class RenameConfirmationDialog extends TitleAreaDialog {
  private final String m_title;
  private final String m_message;

  private StyledTextField m_typeNameField;
  private Set<String> m_notAllowedNames;
  private String m_typeName;

  public RenameConfirmationDialog(Shell parentShell, String title, String message) {
    super(parentShell);
    m_title = title;
    m_message = message;
    setNotAllowedNames(new HashSet<String>());
    setHelpAvailable(false);
    setShellStyle(getShellStyle() | SWT.RESIZE);
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    if (m_title != null) {
      newShell.setText(m_title);
    }
  }

  public Button getOkButton() {
    return getButton(OK);

  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite rootPane = new Composite(parent, SWT.NONE);
    m_typeNameField = new StyledTextField(rootPane, Texts.get("TypeName"));
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        revalidate();
      }
    });
    // layout
    rootPane.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    rootPane.setLayout(new GridLayout(1, true));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    setMessage(m_message);
    return rootPane;
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    revalidate();
    return contents;
  }

  protected void revalidate() {
    if (getNotAllowedNames().contains(m_typeNameField.getText())) {
      getOkButton().setEnabled(false);
      setMessage(Texts.get("NameAlreadyInUse"), IMessageProvider.ERROR);
    }
    else {
      getOkButton().setEnabled(true);
      setMessage(m_message);
    }
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
    if (getContents() != null && (!getContents().isDisposed())) {
      m_typeNameField.setText(typeName);
    }
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setNotAllowedNames(Set<String> notAllowedNames) {
    m_notAllowedNames = notAllowedNames;
    if (getContents() != null && (!getContents().isDisposed())) {
      revalidate();
    }
  }

  public Set<String> getNotAllowedNames() {
    return m_notAllowedNames;
  }
}
