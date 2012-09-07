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
package org.eclipse.scout.sdk.ws.jaxws.swt.dialog;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.tooltip.JavadocTooltip;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.TypePresenter.ISearchJavaSearchScopeFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

public class TypeBrowseDialog extends TitleAreaDialog {

  protected static final String IStructuredSelection = null;
  private IType m_type;
  private StyledTextField m_typeField;
  private Button m_typeBrowseButton;
  private JavadocTooltip m_tooltipType;

  private String m_dialogTitle;
  private String m_dialogMessage;
  private ISearchJavaSearchScopeFactory m_javaSearchScopeFactory;
  private int m_typeStyle;

  public TypeBrowseDialog(Shell shell, String dialogTitle, String dialogMessage) {
    super(shell);
    m_dialogTitle = dialogTitle;
    m_dialogMessage = dialogMessage;
    m_typeStyle = IJavaElementSearchConstants.CONSIDER_CLASSES | IJavaElementSearchConstants.CONSIDER_INTERFACES;
    setDialogHelpAvailable(false);
    setShellStyle(getShellStyle() | SWT.RESIZE);
  }

  @Override
  protected final void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(m_dialogTitle);
  }

  @Override
  protected Control createContents(Composite parent) {
    Control control = super.createContents(parent);
    setTitle(m_dialogTitle);
    setMessage(m_dialogMessage, IMessageProvider.INFORMATION);
    getOkButton().setEnabled(false);
    return control;
  }

  public Button getOkButton() {
    return getButton(OK);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    m_typeField = new StyledTextField(composite, Texts.get("Type"));
    if (TypeUtility.exists(getType())) {
      m_typeField.setText(getType().getElementName());
    }
    m_typeField.setEditable(false);
    m_typeField.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        openTypeDialog();
      }

    });
    m_tooltipType = new JavadocTooltip(m_typeField.getTextComponent());
    updateJavaDoc(m_tooltipType, getType());

    m_typeBrowseButton = new Button(composite, SWT.PUSH | SWT.FLAT);
    m_typeBrowseButton.setText(Texts.get("Browse"));
    m_typeBrowseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        openTypeDialog();
      }
    });

    // layout
    parent.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

    composite.setLayout(new FormLayout());

    FormData formData = new FormData();
    formData.top = new FormAttachment(20, 0);
    formData.left = new FormAttachment(0, 50);
    formData.right = new FormAttachment(100, -75);
    m_typeField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_typeField, -2, SWT.TOP);
    formData.left = new FormAttachment(100, -70);
    formData.right = new FormAttachment(100, 0);
    m_typeBrowseButton.setLayoutData(formData);

    return composite;
  }

  protected boolean getConfiguredIsDescriptionColumnVisible() {
    return false;
  }

  protected String getConfiguredNameColumnText() {
    return Texts.get("Name");
  }

  protected String getConfiguredDescriptionColumnText() {
    return Texts.get("Description");
  }

  public IType getType() {
    return m_type;
  }

  public void setType(IType type) {
    m_type = type;
  }

  public int getTypeStyle() {
    return m_typeStyle;
  }

  public void setTypeStyle(int typeStyle) {
    m_typeStyle = typeStyle;
  }

  public ISearchJavaSearchScopeFactory getJavaSearchScopeFactory() {
    return m_javaSearchScopeFactory;
  }

  public void setJavaSearchScopeFactory(ISearchJavaSearchScopeFactory javaSearchScopeFactory) {
    m_javaSearchScopeFactory = javaSearchScopeFactory;
  }

  private void updateJavaDoc(JavadocTooltip javadocTooltip, IType type) {
    try {
      javadocTooltip.setMember(null);
      if (TypeUtility.exists(type)) {
        javadocTooltip.setMember(type);
      }
    }
    catch (Exception e) {
      JaxWsSdk.logWarning("Could not render tooltip", e);
    }
  }

  private void openTypeDialog() {
    try {
      IJavaSearchScope searchScope;
      if (m_javaSearchScopeFactory == null) {
        searchScope = SearchEngine.createWorkspaceScope();
      }
      else {
        searchScope = m_javaSearchScopeFactory.create();
      }
      SelectionDialog dialog = JavaUI.createTypeDialog(ScoutSdkUi.getShell(), null, searchScope, m_typeStyle, false, "*.*");
      dialog.setTitle(Texts.get("Type"));
      dialog.setMessage(Texts.get("ChooseXY", Texts.get("Type")));
      dialog.setBlockOnOpen(true);
      if (dialog.open() == Window.OK) {
        if (dialog.getResult() != null) {
          IType type = (IType) dialog.getResult()[0];
          if (TypeUtility.exists(type)) {
            m_typeField.setText(type.getFullyQualifiedName());
            updateJavaDoc(m_tooltipType, type);
            getOkButton().setEnabled(true);
          }
          else {
            updateJavaDoc(m_tooltipType, null);
            m_typeField.setText("");
            getOkButton().setEnabled(false);
          }
          setType(type);
        }
      }
    }
    catch (JavaModelException exception) {
      JaxWsSdk.logError(exception);
    }
  }
}
