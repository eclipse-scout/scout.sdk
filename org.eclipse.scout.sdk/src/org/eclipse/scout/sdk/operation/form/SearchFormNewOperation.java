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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.holders.StringHolder;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.jdt.method.MethodNewOperation;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
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

public class SearchFormNewOperation extends FormNewOperation {

  private IScoutBundle m_searchFormDataLocationBundle;
  private String m_searchFormDataPackageName;
  private boolean m_createSearchHandler;
  private IType m_tablePage;

  // created types
  private IType m_createdFormType;
  private IType m_createdFormDataType;
  private IType m_createdSearchHandler;

  /**
   * @param typeName
   * @param packageName
   * @param clientProject
   * @throws JavaModelException
   */
  public SearchFormNewOperation(String typeName, String packageName, IJavaProject clientProject) throws JavaModelException {
    super(typeName, packageName, clientProject);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {

    // create empty form data
    if (getSearchFormDataLocationBundle() != null) {
      String formDataTypeName = getElementName() + "Data";
      PrimaryTypeNewOperation formDataTypeNewOp = new PrimaryTypeNewOperation(formDataTypeName, getSearchFormDataPackageName(), ScoutUtility.getJavaProject(getSearchFormDataLocationBundle()));
      formDataTypeNewOp.addMethodSourceBuilder(MethodSourceBuilderFactory.createConstructorSourceBuilder(formDataTypeName));
      formDataTypeNewOp.setFlags(Flags.AccPublic);
      formDataTypeNewOp.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.AbstractFormData));
      formDataTypeNewOp.validate();
      formDataTypeNewOp.run(monitor, workingCopyManager);
      m_createdFormDataType = formDataTypeNewOp.getCreatedType();
      setFormDataSignature(Signature.createTypeSignature(m_createdFormDataType.getFullyQualifiedName(), true));

    }

    final StringHolder handlerFqnHolder = new StringHolder();
    if (isCreateSearchHandler()) {
      ITypeSourceBuilder newHandlerBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_SEARCH_HANDLER);
      newHandlerBuilder.setFlags(Flags.AccPublic);
      newHandlerBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IFormHandler, getJavaProject()));
      addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormHandlerKey(newHandlerBuilder), newHandlerBuilder);
      handlerFqnHolder.setValue(getPackageName() + "." + getElementName() + "." + newHandlerBuilder.getElementName());

    }
    // start method
    IMethodSourceBuilder startHandlerMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(getSourceBuilder(), "startSearch");
    if (handlerFqnHolder.getValue() != null) {
      startHandlerMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("startInternal(new ").append(validator.getTypeName(Signature.createTypeSignature(handlerFqnHolder.getValue(), true))).append("());");
        }
      });
    }

    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodStartFormKey(startHandlerMethodBuilder), startHandlerMethodBuilder);

    super.run(monitor, workingCopyManager);

    // TODO evaluate update form data!!!
//    FormDataUpdateOperation formDataOp = null;
//    if (getSearchFormDataLocationBundle() != null) {
//      formDataOp = new FormDataUpdateOperation(getCreatedFormType());
//      formDataOp.run(monitor, workingCopyManager);
//      m_createdFormDataType = formDataOp.getFormDataType();
//    }
  }

  @Override
  protected void createMainBox(ITypeSourceBuilder formBuilder, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (getTablePage() != null) {
      // fill search form from table page
      fillFromTablePage(getSourceBuilder(), getPackageName() + "." + getElementName(), getTablePage(), m_createdFormDataType, getJavaProject(), monitor, workingCopyManager);
    }
    else {
      // main box
      ITypeSourceBuilder mainBoxBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_MAIN_BOX);
      mainBoxBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(10.0));
      mainBoxBuilder.setFlags(Flags.AccPublic);
      mainBoxBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IGroupBox, getJavaProject()));
      formBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(mainBoxBuilder, 10.0), mainBoxBuilder);

    }
  }

  /**
   * @param sourceBuilder
   * @param tablePage
   * @param monitor
   * @param workingCopyManager
   * @throws CoreException
   */
  private void fillFromTablePage(ITypeSourceBuilder searchFormBuilder, final String searchFormFqn, IType tablePage, IType formDataType, IJavaProject searchFormProject, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SearchFormFromTablePageHelper.fillSearchForm(searchFormBuilder, searchFormFqn, formDataType, getTablePage(), searchFormProject, monitor);

    IMethod getConfiguredSearchFormMethod = TypeUtility.getMethod(getTablePage(), "getConfiguredSearchForm");
    if (TypeUtility.exists(getConfiguredSearchFormMethod)) {
      JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
      delOp.addMember(getConfiguredSearchFormMethod);
      delOp.validate();
      delOp.run(monitor, workingCopyManager);
    }

    MethodNewOperation getConfiguredSearchFormOp = new MethodNewOperation(MethodSourceBuilderFactory.createOverrideMethodSourceBuilder("getConfiguredSearchForm", getTablePage()), getTablePage());
    getConfiguredSearchFormOp.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        String simpleRef = validator.getTypeName(SignatureCache.createTypeSignature(searchFormFqn));
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

  public IScoutBundle getSearchFormDataLocationBundle() {
    return m_searchFormDataLocationBundle;
  }

  public void setSearchFormDataLocationBundle(IScoutBundle searchFormDataLocationBundle) {
    m_searchFormDataLocationBundle = searchFormDataLocationBundle;
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

  public String getSearchFormDataPackageName() {
    return m_searchFormDataPackageName;
  }

  public void setSearchFormDataPackageName(String searchFormDataPackageName) {
    m_searchFormDataPackageName = searchFormDataPackageName;
  }
}
