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
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldNewOperation;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.operation.method.ConstructorCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

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

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {

    ScoutTypeNewOperation newOp = new ScoutTypeNewOperation(getTypeName(), getSearchFormLocationBundle().getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_SEARCHFORMS), getSearchFormLocationBundle());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    if (getSearchFormDataLocationBundle() != null) {
      AnnotationCreateOperation annotOp = new AnnotationCreateOperation(null, Signature.createTypeSignature(RuntimeClasses.FormData, true));
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
    constructorOp.setExceptionSignatures(new String[]{Signature.createTypeSignature(RuntimeClasses.ProcessingException, true)});
    constructorOp.setSimpleBody("  super();");
    constructorOp.validate();
    constructorOp.run(monitor, workingCopyManager);
    // form data
    FormDataUpdateOperation formDataOp = null;
    if (getSearchFormDataLocationBundle() != null) {
      formDataOp = new FormDataUpdateOperation(getCreatedFormType());
      // formDataOp.setFormDataPackageName(getSearchFormDataLocationBundle().getPackageNameOutlineService());
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
          String simpleRef = validator.getSimpleTypeRef(Signature.createTypeSignature(getCreatedFormType().getFullyQualifiedName(), true));
          return "return " + simpleRef + ".class;";
        }
      };
//      OperationJob job = new OperationJob(overrideOp);
//      job.schedule(50);
      overrideOp.validate();
      overrideOp.run(monitor, workingCopyManager);
//      StringBuilder methodSource = new StringBuilder();
//      IImportValidator tablePageImportValidator = new CompilationUnitImportValidator(getTablePage().getCompilationUnit());
//      methodSource.append("@Override\n");
//      methodSource.append("public Class<? extends " + tablePageImportValidator.getSimpleTypeRef(Signature.createTypeSignature(RuntimeClasses.ISearchForm, true)) + "> getConfiguredSearchForm(){\n");
//      methodSource.append(ScoutIdeProperties.TAB + "return " + tablePageImportValidator.getSimpleTypeRef(Signature.createTypeSignature(getCreatedFormType().getFullyQualifiedName(), true)) + ".class;\n}");
//      getTablePage().createMethod(methodSource.toString(), null, true, monitor);
//      for (String imp : tablePageImportValidator.getImportsToCreate()) {
//        getTablePage().getCompilationUnit().createImport(imp, null, monitor);
//      }
//      workingCopyManager.register(getTablePage().getCompilationUnit(), true, monitor);
    }
    else {
      // main box
      FormFieldNewOperation mainBoxOp = new FormFieldNewOperation(getCreatedFormType());
      mainBoxOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractGroupBox, true));
      mainBoxOp.setTypeName(ScoutIdeProperties.TYPE_NAME_MAIN_BOX);
      mainBoxOp.validate();
      mainBoxOp.run(monitor, workingCopyManager);
      if (isCreateSearchHandler()) {
        FormHandlerNewOperation searchHandlerOp = new FormHandlerNewOperation(getCreatedFormType());
        searchHandlerOp.setTypeName(ScoutIdeProperties.TYPE_NAME_SEARCH_HANDLER);
        searchHandlerOp.setStartMethodSibling(SdkTypeUtility.createStructuredForm(getCreatedFormType()).getSiblingMethodStartHandler(searchHandlerOp.getStartMethodName()));
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
