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
package org.eclipse.scout.sdk.ui.wizard.form.handler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.form.FormHandlerNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link FormHandlerNewWizardPage2}</h3> ...
 */
public class FormHandlerNewWizardPage2 extends AbstractWorkspaceWizardPage {

  final IType iFormHandler = ScoutSdk.getType(RuntimeClasses.IFormHandler);

  private String m_typeName;

  private StyledTextField m_typeNameField;

  // process members
  private final IType m_declaringType;
  private IType m_createdFormHandler;

  public FormHandlerNewWizardPage2(IType declaringType) {
    super("New Form Handler");
    setTitle("New Form Handler");
    setDefaultMessage("Create a new calendar form handler.");
    m_declaringType = declaringType;

  }

  @Override
  protected void createContent(Composite parent) {

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, "Type Name");
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_FORM_HANDLER);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    FormHandlerNewOperation operation = new FormHandlerNewOperation(getDeclaringType());
    operation.setFormatSource(true);
    // write back members
    operation.setTypeName(getTypeName());
    FormHandlerNewWizardPage1 previousPage = (FormHandlerNewWizardPage1) getWizard().getPreviousPage(this);
    if (previousPage.getSelectedSuperType() != null) {
      operation.setSuperTypeSignature(Signature.createTypeSignature(previousPage.getSelectedSuperType().getFullyQualifiedName(), true));
    }

    IStructuredType structuredType = SdkTypeUtility.createStructuredForm(m_declaringType);
    operation.setSibling(structuredType.getSiblingTypeFormHandler(getTypeName()));
    operation.setStartMethodSibling(structuredType.getSiblingMethodStartHandler(operation.getStartMethodName()));
    operation.run(monitor, workingCopyManager);
    m_createdFormHandler = operation.getCreatedHandler();
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_FORM_HANDLER)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (TypeUtility.exists(getDeclaringType().getType(getTypeName()))) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    if (getTypeName().matches(Regex.REGEX_WELLFORMD_JAVAFIELD)) {
      return Status.OK_STATUS;
    }
    else if (getTypeName().matches(Regex.REGEX_JAVAFIELD)) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_invalidFieldX", getTypeName()));
    }
  }

  /**
   * @return the createdFormHandler
   */
  public IType getCreatedFormHandler() {
    return m_createdFormHandler;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    try {
      setStateChanging(true);
      m_typeName = typeName;
      if (isControlCreated()) {
        m_typeNameField.setText(typeName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  /**
   * @return the declaringType
   */
  public IType getDeclaringType() {
    return m_declaringType;
  }
}
