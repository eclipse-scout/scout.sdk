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

import java.beans.PropertyChangeListener;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.FieldToolkit;
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

public class RenameDialog extends TitleAreaDialog {
  public static final String PROP_NEW_NAME = "newName";
  private final FieldToolkit m_fieldToolkit;
  private final String m_title;
  private final String m_oldName;
  private final String m_readOnlySuffix;
  private final String m_readOnlyPrefix;
  private BasicPropertySupport m_propertySupport;

  // ui fields
  private StyledTextField m_typeNameField;

  public RenameDialog(Shell parentShell, String title, String oldName, String readOnlySuffix) {
    this(parentShell, title, oldName, readOnlySuffix, null);
  }

  public RenameDialog(Shell parentShell, String title, String oldName, String readOnlySuffix, String readOnlyPrefix) {
    super(parentShell);
    m_propertySupport = new BasicPropertySupport(this);
    m_title = title;
    m_oldName = oldName;
    setNewName(m_oldName);
    m_readOnlySuffix = readOnlySuffix;
    m_readOnlyPrefix = readOnlyPrefix;
    setShellStyle(getShellStyle() | SWT.RESIZE);
    setHelpAvailable(false);
    m_fieldToolkit = new FieldToolkit();
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    if (m_title != null) {
      newShell.setText(m_title);
    }
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite rootPane = new Composite(parent, SWT.NONE);
    m_typeNameField = m_fieldToolkit.createStyledTextField(rootPane, Texts.get("Dialog_rename_oldNameLabel"));
    m_typeNameField.setReadOnlySuffix(getReadOnlySuffix());
    m_typeNameField.setReadOnlyPrefix(getReadOnlyPrefix());
    m_typeNameField.setText(getNewName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_propertySupport.setPropertyString(PROP_NEW_NAME, m_typeNameField.getText());
      }
    });
    rootPane.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    rootPane.setLayout(new GridLayout(1, true));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return rootPane;
  }

  public Button getOkButton() {
    return getButton(OK);
  }

  public String getOldName() {
    return m_oldName;
  }

  public String getReadOnlySuffix() {
    return m_readOnlySuffix;
  }

  /**
   * @return the readOnlyPrefix
   */
  public String getReadOnlyPrefix() {
    return m_readOnlyPrefix;
  }

  public void setNewName(String newName) {
    if (getContents() != null && !getContents().isDisposed()) {
      m_typeNameField.setText(newName);
    }
    m_propertySupport.setPropertyString(PROP_NEW_NAME, newName);
  }

  public String getNewName() {
    return m_propertySupport.getPropertyString(PROP_NEW_NAME);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }
}
