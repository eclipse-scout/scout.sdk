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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.form.FormHandlerNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link FormHandlerNewWizardPage2}</h3> ...
 */
public class FormHandlerNewWizardPage2 extends AbstractWorkspaceWizardPage {

  private String m_typeName;
  private StyledTextField m_typeNameField;

  // process members
  private final IType m_declaringType;
  private IType m_createdFormHandler;

  public FormHandlerNewWizardPage2(IType declaringType) {
    super(FormHandlerNewWizardPage2.class.getName());
    setTitle(Texts.get("NewFormHandler"));
    setDescription(Texts.get("CreateANewCalendarFormHandler"));
    m_declaringType = declaringType;
  }

  @Override
  protected void createContent(Composite parent) {

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_FORM_HANDLER);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
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
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    FormHandlerNewOperation operation = new FormHandlerNewOperation(getTypeName(), m_declaringType, true);
    // write back members
    FormHandlerNewWizardPage1 previousPage = (FormHandlerNewWizardPage1) getWizard().getPreviousPage(this);
    if (previousPage.getSelectedSuperType() != null) {
      operation.setSuperTypeSignature(SignatureCache.createTypeSignature(previousPage.getSelectedSuperType().getFullyQualifiedName()));
    }

    IStructuredType structuredType = ScoutTypeUtility.createStructuredForm(m_declaringType);
    operation.setSibling(structuredType.getSiblingTypeFormHandler(getTypeName()));
    operation.run(monitor, workingCopyManager);
    m_createdFormHandler = operation.getCreatedType();
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
    IStatus javaFieldNameStatus = ScoutUtility.getJavaNameStatus(getTypeName(), SdkProperties.SUFFIX_FORM_HANDLER);
    if (javaFieldNameStatus.isOK()) {
      if (TypeUtility.exists(getDeclaringType().getType(getTypeName()))) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
      }
    }
    return javaFieldNameStatus;
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
