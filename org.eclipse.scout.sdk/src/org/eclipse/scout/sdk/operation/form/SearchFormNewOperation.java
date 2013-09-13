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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.jdt.method.MethodNewOperation;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class SearchFormNewOperation implements IOperation {

  private String m_typeName;
  private String m_superTypeSignature;
  private INlsEntry m_nlsEntry;
  private IScoutBundle m_searchFormLocationBundle;
  private IScoutBundle m_searchFormDataLocationBundle;
  private IType m_tablePage;
  private boolean m_createSearchHandler;
  private String m_searchFormPackageName;
  private String m_searchFormDataPackageName;

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
    if (StringUtility.isNullOrEmpty(getSearchFormPackageName())) {
      throw new IllegalArgumentException("search form package is null or empty.");
    }
    if (StringUtility.isNullOrEmpty(getSearchFormDataPackageName())) {
      throw new IllegalArgumentException("search form data package is null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // create empty form data
//    String formDataSignature = null;
    IType formDataType = null;
    if (getSearchFormDataLocationBundle() != null) {
      // form data
      PrimaryTypeNewOperation formDataOp = new PrimaryTypeNewOperation(getTypeName() + "Data", getSearchFormDataPackageName(), ScoutUtility.getJavaProject(getSearchFormDataLocationBundle()));
      formDataOp.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.AbstractFormData));
      formDataOp.setPackageExportPolicy(ExportPolicy.AddPackage);
      formDataOp.run(monitor, workingCopyManager);
      formDataType = formDataOp.getCreatedType();
//      formDataSignature = SignatureCache.createTypeSignature(formDataOp.getCreatedType().getFullyQualifiedName());
    }
    // form
    PrimaryTypeNewOperation newOp = new PrimaryTypeNewOperation(getTypeName(), getSearchFormPackageName(), ScoutUtility.getJavaProject(getSearchFormLocationBundle()));
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    if (formDataType != null) {
      newOp.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createFormDataAnnotation(SignatureCache.createTypeSignature(formDataType.getFullyQualifiedName()), SdkCommand.CREATE, null));
    }
    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(newOp.getElementName());
    constructorBuilder.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
    constructorBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("super();"));
    newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
    // getConfiguredLabel method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TITLE);
      nlsMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsMethodBuilder), nlsMethodBuilder);
    }
    if (getTablePage() != null) {
      // fill search form from table page
      fillFromTablePage(newOp.getSourceBuilder(), newOp.getPackageName() + "." + newOp.getElementName(), getTablePage(), formDataType, newOp.getJavaProject(), monitor, workingCopyManager);
    }
    else {
      // main box
      ITypeSourceBuilder mainBoxBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_MAIN_BOX);
      mainBoxBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(10.0));
      mainBoxBuilder.setFlags(Flags.AccPublic);
      mainBoxBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IGroupBox, newOp.getJavaProject()));
      newOp.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(mainBoxBuilder, 10.0), mainBoxBuilder);

      if (isCreateSearchHandler()) {
        ITypeSourceBuilder newHandlerBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_SEARCH_HANDLER);
        newHandlerBuilder.setFlags(Flags.AccPublic);
        newHandlerBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IFormHandler, newOp.getJavaProject()));
        newOp.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormHandlerKey(newHandlerBuilder), newHandlerBuilder);

        // start method
        final String handlerFqn = newOp.getPackageName() + "." + newOp.getElementName() + "." + newHandlerBuilder.getElementName();
        IMethodSourceBuilder startHandlerMethodBuilder = new MethodSourceBuilder(SdkProperties.TYPE_NAME_SEARCH_HANDLER_PREFIX);
        startHandlerMethodBuilder.setFlags(Flags.AccPublic);
        startHandlerMethodBuilder.setReturnTypeSignature(Signature.SIG_VOID);
        startHandlerMethodBuilder.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
        startHandlerMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
          @Override
          public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
            source.append("startInternal(new ").append(validator.getTypeName(Signature.createTypeSignature(handlerFqn, true))).append("());");
          }
        });
        newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodStartFormKey(startHandlerMethodBuilder), startHandlerMethodBuilder);

      }
    }
    newOp.setPackageExportPolicy(ExportPolicy.AddPackage);
    newOp.setFormatSource(true);
    newOp.validate();
    newOp.run(monitor, workingCopyManager);
    m_createdFormType = newOp.getCreatedType();
    workingCopyManager.register(m_createdFormType.getCompilationUnit(), monitor);
    // TODO evaluate update form data!!!
//    FormDataUpdateOperation formDataOp = null;
//    if (getSearchFormDataLocationBundle() != null) {
//      formDataOp = new FormDataUpdateOperation(getCreatedFormType());
//      formDataOp.run(monitor, workingCopyManager);
//      m_createdFormDataType = formDataOp.getFormDataType();
//    }
  }

  /**
   * @param sourceBuilder
   * @param tablePage
   * @param monitor
   * @param workingCopyManager
   * @throws CoreException
   */
  private void fillFromTablePage(ITypeSourceBuilder searchFormBuilder, String searchFormFqn, IType tablePage, IType formDataType, IJavaProject searchFormProject, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SearchFormFromTablePageHelper.fillSearchForm(searchFormBuilder, searchFormFqn, formDataType, getTablePage(), searchFormProject, monitor);

    IMethod getConfiguredSearchFormMethod = TypeUtility.getMethod(getTablePage(), "getConfiguredSearchForm");
    if (TypeUtility.exists(getConfiguredSearchFormMethod)) {
      JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
      delOp.addMember(getConfiguredSearchFormMethod);
      delOp.validate();
      delOp.run(monitor, workingCopyManager);
    }

    MethodNewOperation getConfiguredSearchFormOp = new MethodNewOperation(MethodSourceBuilderFactory.createOverrideMethodSourceBuilder("", getTablePage()), getTablePage());
    getConfiguredSearchFormOp.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        String simpleRef = validator.getTypeName(SignatureCache.createTypeSignature(getCreatedFormType().getFullyQualifiedName()));
        source.append("return ").append(simpleRef).append(".class;");
      }
    });
    getConfiguredSearchFormOp.validate();
    getConfiguredSearchFormOp.run(monitor, workingCopyManager);
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

  public String getSearchFormPackageName() {
    return m_searchFormPackageName;
  }

  public void setSearchFormPackageName(String searchFormPackageName) {
    m_searchFormPackageName = searchFormPackageName;
  }

  public String getSearchFormDataPackageName() {
    return m_searchFormDataPackageName;
  }

  public void setSearchFormDataPackageName(String searchFormDataPackageName) {
    m_searchFormDataPackageName = searchFormDataPackageName;
  }
}
