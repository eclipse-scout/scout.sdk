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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.annotation.FormDataAnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldNewOperation;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.operation.method.ConstructorCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

public class SearchFormNewOperation implements IOperation {

  private String m_typeName;
  private String m_superTypeSignature;
  private INlsEntry m_nlsEntry;
  private IScoutBundle m_searchFormLocationBundle;
  private IScoutBundle m_searchFormDataLocationBundle;
  private IType m_tablePage;
  private boolean m_createSearchHandler;
  // created types
  private IType m_createdFormType;
  private IType m_createdFormDataType;
  private IType m_createdSearchHandler;

  @Override
  public void validate() throws IllegalArgumentException {
    if (getSearchFormLocationBundle() == null) {
      throw new IllegalArgumentException("client bundle can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name is null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // create empty form data
    String formDataSignature = null;
    if (getSearchFormDataLocationBundle() != null) {
      ScoutTypeNewOperation formDataOp = new ScoutTypeNewOperation(getTypeName() + "Data", getSearchFormDataLocationBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_PROCESS), getSearchFormDataLocationBundle());
      formDataOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractFormData, true));
      formDataOp.run(monitor, workingCopyManager);
      formDataSignature = Signature.createTypeSignature(formDataOp.getCreatedType().getFullyQualifiedName(), true);
      // exported form type
      ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{formDataOp.getCreatedType().getPackageFragment()}, true);
      manifestOp.run(monitor, workingCopyManager);
    }
    // form
    ScoutTypeNewOperation newOp = new ScoutTypeNewOperation(getTypeName(), getSearchFormLocationBundle().getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_SEARCHFORMS), getSearchFormLocationBundle());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    if (!StringUtility.isNullOrEmpty(formDataSignature)) {
      FormDataAnnotationCreateOperation annotOp = new FormDataAnnotationCreateOperation(null);
      annotOp.setSdkCommand(SdkCommand.CREATE);
      annotOp.setFormDataSignature(formDataSignature);
      newOp.addAnnotation(annotOp);
    }
    newOp.run(monitor, workingCopyManager);
    m_createdFormType = newOp.getCreatedType();
    workingCopyManager.register(m_createdFormType.getCompilationUnit(), monitor);
    if (getNlsEntry() != null) {
      NlsTextMethodUpdateOperation nlsOp = new NlsTextMethodUpdateOperation(m_createdFormType, NlsTextMethodUpdateOperation.GET_CONFIGURED_TITLE);
      nlsOp.setNlsEntry(getNlsEntry());
      nlsOp.validate();
      nlsOp.run(monitor, workingCopyManager);
    }

    // add to exported packages
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY,
        new IPackageFragment[]{m_createdFormType.getPackageFragment()}, true);
    manifestOp.run(monitor, workingCopyManager);

    // create constructor
    ConstructorCreateOperation constructorOp = new ConstructorCreateOperation(m_createdFormType);
    constructorOp.setMethodFlags(Flags.AccPublic);
    constructorOp.addExceptionSignature(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true));
    constructorOp.setSimpleBody("  super();");
    constructorOp.validate();
    constructorOp.run(monitor, workingCopyManager);
    // form data
    FormDataUpdateOperation formDataOp = null;
    if (getSearchFormDataLocationBundle() != null) {
      formDataOp = new FormDataUpdateOperation(getCreatedFormType());
      formDataOp.run(monitor, workingCopyManager);
      m_createdFormDataType = formDataOp.getFormDataType();
    }
    if (getTablePage() != null) {
      SearchFormFromTablePageFillOperation fillOp = new SearchFormFromTablePageFillOperation();
      fillOp.setSearchFormType(m_createdFormType);
      fillOp.setFormDataType(m_createdFormDataType);
      fillOp.setTablePageType(getTablePage());
      fillOp.run(monitor, workingCopyManager);
      IMethod confSearchFormMethod = TypeUtility.getMethod(getTablePage(), "getConfiguredSearchForm");
      if (TypeUtility.exists(confSearchFormMethod)) {
        confSearchFormMethod.delete(true, monitor);
      }
      IMethod getConfiguredSearchFormMethod = TypeUtility.getMethod(getTablePage(), "getConfiguredSearchForm");
      if (TypeUtility.exists(getConfiguredSearchFormMethod)) {
        ScoutMethodDeleteOperation delOp = new ScoutMethodDeleteOperation(getConfiguredSearchFormMethod);
        delOp.validate();
        delOp.run(monitor, workingCopyManager);
      }

      MethodOverrideOperation overrideOp = new MethodOverrideOperation(getTablePage(), "getConfiguredSearchForm") {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          String simpleRef = validator.getTypeName(Signature.createTypeSignature(getCreatedFormType().getFullyQualifiedName(), true));
          return "return " + simpleRef + ".class;";
        }
      };
      overrideOp.validate();
      overrideOp.run(monitor, workingCopyManager);
    }
    else {
      // main box
      FormFieldNewOperation mainBoxOp = new FormFieldNewOperation(getCreatedFormType());
      mainBoxOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractGroupBox, true));
      mainBoxOp.setTypeName(SdkProperties.TYPE_NAME_MAIN_BOX);
      mainBoxOp.validate();
      mainBoxOp.run(monitor, workingCopyManager);
      if (isCreateSearchHandler()) {
        FormHandlerNewOperation searchHandlerOp = new FormHandlerNewOperation(getCreatedFormType());
        searchHandlerOp.setTypeName(SdkProperties.TYPE_NAME_SEARCH_HANDLER);
        searchHandlerOp.setStartMethodSibling(ScoutTypeUtility.createStructuredForm(getCreatedFormType()).getSiblingMethodStartHandler(searchHandlerOp.getStartMethodName()));
        searchHandlerOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractFormHandler, true));
        searchHandlerOp.run(monitor, workingCopyManager);
        m_createdSearchHandler = searchHandlerOp.getCreatedHandler();
      }
    }

    if (formDataOp != null) {
      formDataOp.run(monitor, workingCopyManager);
    }
    // format source
    JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedFormType(), true);
    formatOp.validate();
    formatOp.run(monitor, workingCopyManager);
  }

  @Override
  public String getOperationName() {
    return "New Search Form...";
  }

  public IType getCreatedFormType() {
    return m_createdFormType;
  }

  public IType getCreatedFormDataType() {
    return m_createdFormDataType;
  }

  public IType getCreatedSearchHandler() {
    return m_createdSearchHandler;
  }

  public IScoutBundle getSearchFormLocationBundle() {
    return m_searchFormLocationBundle;
  }

  public void setSearchFormLocationBundle(IScoutBundle searchFormLocationBundle) {
    m_searchFormLocationBundle = searchFormLocationBundle;
  }

  public IScoutBundle getSearchFormDataLocationBundle() {
    return m_searchFormDataLocationBundle;
  }

  public void setSearchFormDataLocationBundle(IScoutBundle searchFormDataLocationBundle) {
    m_searchFormDataLocationBundle = searchFormDataLocationBundle;
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

  public boolean isCreateSearchHandler() {
    return m_createSearchHandler;
  }

  public void setCreateSearchHandler(boolean createSearchHandler) {
    m_createSearchHandler = createSearchHandler;
  }

  public void setTablePage(IType tablePage) {
    m_tablePage = tablePage;
  }

  public IType getTablePage() {
    return m_tablePage;
  }

}
