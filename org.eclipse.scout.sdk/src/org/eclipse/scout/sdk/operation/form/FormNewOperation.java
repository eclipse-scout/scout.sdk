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
package org.eclipse.scout.sdk.operation.form;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.annotation.FormDataAnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.form.field.ButtonFieldNewOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldNewOperation;
import org.eclipse.scout.sdk.operation.method.ConstructorCreateOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link FormNewOperation}</h3> To create a new form. A form is either a view, editor or a dialog.
 * 
 * @author Andreas Hoegger
 * @since 1.0.0 2008
 */
public class FormNewOperation implements IOperation {

  private String m_typeName;
  private String m_package;
  private String m_superTypeSignature;
  private INlsEntry m_nlsEntry;
  private String m_formDataSignature;
  private boolean m_createButtonOk;
  private boolean m_createButtonCancel;
  private boolean m_formatSource;
  private IScoutBundle m_clientBundle;

  private IType m_createdFormType;
  private IType m_createdMainBox;
  private IMethod m_createdNlsLabelMethod;
  private IMethod m_createdMainBoxGetter;

  public FormNewOperation() {
    setFormatSource(true);
  }

  @Override
  public String getOperationName() {
    return "New Form...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("form name is null or empty.");
    }
    if (getClientBundle() == null) {
      throw new IllegalArgumentException("client boundle missing.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ScoutTypeNewOperation newOp = new ScoutTypeNewOperation(getTypeName(), getPackage(), getClientBundle());
    if (getSuperTypeSignature() != null) {
      newOp.setSuperTypeSignature(getSuperTypeSignature());
    }
    else {
      newOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IForm, getClientBundle().getJavaProject()));
    }
    if (!StringUtility.isNullOrEmpty(getFormDataSignature())) {
      FormDataAnnotationCreateOperation annotOp = new FormDataAnnotationCreateOperation(null);
      annotOp.setSdkCommand(SdkCommand.CREATE);
      annotOp.setFormDataSignature(getFormDataSignature());
      newOp.addAnnotation(annotOp);
    }
    newOp.run(monitor, workingCopyManager);
    m_createdFormType = newOp.getCreatedType();
    workingCopyManager.register(m_createdFormType.getCompilationUnit(), monitor);

    // create constructor
    ConstructorCreateOperation constructorOp = new ConstructorCreateOperation(getCreatedFormType(), false);
    constructorOp.setMethodFlags(Flags.AccPublic);
    constructorOp.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
    constructorOp.setSimpleBody("  super();");
    constructorOp.validate();
    constructorOp.run(monitor, workingCopyManager);

    if (getNlsEntry() != null) {
      NlsTextMethodUpdateOperation labelOp = new NlsTextMethodUpdateOperation(getCreatedFormType(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TITLE);
      labelOp.setNlsEntry(getNlsEntry());
      labelOp.validate();
      labelOp.run(monitor, workingCopyManager);
      m_createdNlsLabelMethod = labelOp.getUpdatedMethod();
    }

    // add to exported packages
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{m_createdFormType.getPackageFragment()}, true);
    manifestOp.run(monitor, workingCopyManager);

    // main box
    FormFieldNewOperation mainBoxOp = new FormFieldNewOperation(getCreatedFormType());
    mainBoxOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IGroupBox, getCreatedFormType().getJavaProject()));
    mainBoxOp.setTypeName(SdkProperties.TYPE_NAME_MAIN_BOX);
    mainBoxOp.validate();
    mainBoxOp.run(monitor, workingCopyManager);
    m_createdMainBox = mainBoxOp.getCreatedFormField();
    // buttons
    if (isCreateButtonOk()) {
      ButtonFieldNewOperation okOp = new ButtonFieldNewOperation(m_createdMainBox, false);
      okOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.AbstractOkButton, getCreatedFormType().getJavaProject()));
      okOp.setTypeName("OkButton");
      okOp.validate();
      okOp.run(monitor, workingCopyManager);
    }

    if (isCreateButtonCancel()) {
      ButtonFieldNewOperation cancelButtonOp = new ButtonFieldNewOperation(m_createdMainBox, false);
      cancelButtonOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.AbstractCancelButton, getCreatedFormType().getJavaProject()));
      cancelButtonOp.setTypeName("CancelButton");
      cancelButtonOp.validate();
      cancelButtonOp.run(monitor, workingCopyManager);
    }
    m_createdMainBoxGetter = mainBoxOp.getCreatedFieldGetterMethod();
    if (isFormatSource()) {
      // format source
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedFormType(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  public IType getCreatedFormType() {
    return m_createdFormType;
  }

  public IType getCreatedMainBox() {
    return m_createdMainBox;
  }

  public IMethod getCreatedNlsLabelMethod() {
    return m_createdNlsLabelMethod;
  }

  public IMethod getCreatedMainBoxGetter() {
    return m_createdMainBoxGetter;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  /**
   * @param formDataSignature
   *          the formDataSignature to set
   */
  public void setFormDataSignature(String formDataSignature) {
    m_formDataSignature = formDataSignature;
  }

  /**
   * @return the formDataSignature
   */
  public String getFormDataSignature() {
    return m_formDataSignature;
  }

  public void setCreateButtonOk(boolean createButtonOk) {
    m_createButtonOk = createButtonOk;
  }

  public boolean isCreateButtonOk() {
    return m_createButtonOk;
  }

  public void setCreateButtonCancel(boolean createButtonCancel) {
    m_createButtonCancel = createButtonCancel;
  }

  public boolean isCreateButtonCancel() {
    return m_createButtonCancel;
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public void setClientBundle(IScoutBundle clientBundle) {
    m_clientBundle = clientBundle;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public String getPackage() {
    return m_package;
  }

  public void setPackage(String _package) {
    m_package = _package;
  }
}
