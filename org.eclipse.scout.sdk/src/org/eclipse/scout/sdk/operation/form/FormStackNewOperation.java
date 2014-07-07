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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.operation.service.ProcessServiceNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceRegistrationDescription;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class FormStackNewOperation extends FormNewOperation {

  private boolean m_createNewHandler = false;
  private boolean m_createModifyHandler = false;
  private IJavaProject m_formDataProject;
  private String m_formDataPackage;

  private IJavaProject m_permissionCreateProject;
  private String m_permissionCreateName;
  private String m_permissionCreatePackage;

  private IJavaProject m_permissionReadProejct;
  private String m_permissionReadName;
  private String m_permissionReadPackage;

  private IJavaProject m_permissionUpdateProject;
  private String m_permissionUpdateName;
  private String m_permissionUpdatePackage;

  private IJavaProject m_serviceInterfaceProject;
  private String m_serviceInterfaceName;
  private String m_serviceInterfacePackage;
  private IJavaProject m_serviceImplementationProject;
  private String m_serviceImplementationSuperTypeSignature;
  private String m_serviceImplementationName;
  private String m_serviceImplementationPackage;
  private final List<IJavaProject> m_serviceProxyRegistrationProjects;
  private final List<ServiceRegistrationDescription> m_serviceRegistrationDescriptions;

  // operation members
  private IType m_createdService;
  private IType m_createdServiceInterface;
  private IType m_createdFormData;
  private IType m_createdCreatePermission;
  private IType m_createdReadPermission;
  private IType m_createdUpdatePermission;
  private IType m_createdNewHandler;
  private IType m_createdModifyHandler;

  public FormStackNewOperation(String formName, String formPackageName, IJavaProject formProject) throws JavaModelException {
    super(formName, formPackageName, formProject);
    m_serviceRegistrationDescriptions = new ArrayList<ServiceRegistrationDescription>();
    m_serviceProxyRegistrationProjects = new ArrayList<IJavaProject>();
  }

  @Override
  public String getOperationName() {
    return "Create Form and Service '" + getElementName() + "'.";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // create empty form data
    if (getFormDataProject() != null) {
      String formDataTypeName = getElementName() + "Data";
      PrimaryTypeNewOperation formDataTypeNewOp = new PrimaryTypeNewOperation(formDataTypeName, getFormDataPackage(), getFormDataProject().getJavaProject());
      formDataTypeNewOp.addMethodSourceBuilder(MethodSourceBuilderFactory.createConstructorSourceBuilder(formDataTypeName));
      formDataTypeNewOp.setFlags(Flags.AccPublic);
      formDataTypeNewOp.setPackageExportPolicy(ExportPolicy.AddPackage);
      formDataTypeNewOp.setSuperTypeSignature(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractFormData));
      formDataTypeNewOp.validate();
      formDataTypeNewOp.run(monitor, workingCopyManager);
      m_createdFormData = formDataTypeNewOp.getCreatedType();
      setFormDataSignature(SignatureCache.createTypeSignature(getCreatedFormData().getFullyQualifiedName()));
    }
    // service
    ProcessServiceNewOperation serviceOp = new ProcessServiceNewOperation(getServiceImplementationName());
    serviceOp.setFormatSource(isFormatSource());
    serviceOp.setFormData(m_createdFormData);
    serviceOp.setImplementationPackageName(getServiceImplementationPackage());
    serviceOp.setImplementationProject(getServiceImplementationProject());
    serviceOp.setImplementationSuperTypeSignature(getServiceImplementationSuperTypeSignature());
    serviceOp.setInterfacePackageName(getServiceInterfacePackage());
    serviceOp.setInterfaceProject(getServiceInterfaceProject());
    serviceOp.setPermissionCreateName(getPermissionCreateName());
    serviceOp.setPermissionCreatePackageName(getPermissionCreatePackage());
    serviceOp.setPermissionCreateProject(getPermissionCreateProject());
    serviceOp.setPermissionReadName(getPermissionReadName());
    serviceOp.setPermissionReadPackageName(getPermissionReadPackage());
    serviceOp.setPermissionReadProject(getPermissionReadProject());
    serviceOp.setPermissionUpdateName(getPermissionUpdateName());
    serviceOp.setPermissionUpdatePackageName(getPermissionUpdatePackage());
    serviceOp.setPermissionUpdateProject(getPermissionUpdateProject());
    serviceOp.setProxyRegistrationProjects(getServiceProxyRegistrationProjects());
    serviceOp.setServiceRegistrations(getServiceRegistrations());
    serviceOp.validate();
    serviceOp.run(monitor, workingCopyManager);
    m_createdService = serviceOp.getCreatedServiceImplementation();
    m_createdServiceInterface = serviceOp.getCreatedServiceInterface();
    m_createdCreatePermission = serviceOp.getCreatedCreatePermission();
    m_createdReadPermission = serviceOp.getCreatedReadPermission();
    m_createdUpdatePermission = serviceOp.getCreatedUpdatePermission();

    if (isCreateNewHandler()) {
      createNewHandler(getSourceBuilder());
    }
    if (isCreateModifyHandler()) {
      createModifyHandler(getSourceBuilder());
    }
    super.run(monitor, workingCopyManager);
  }

  /**
   * @param sourceBuilder
   * @throws CoreException
   */
  protected void createNewHandler(ITypeSourceBuilder formSourceBuilder) throws CoreException {
    ITypeSourceBuilder newHandlerBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_NEW_HANDLER);
    newHandlerBuilder.setFlags(Flags.AccPublic);
    newHandlerBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IFormHandler, getJavaProject()));
    formSourceBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormHandlerKey(newHandlerBuilder), newHandlerBuilder);

    if (m_createdServiceInterface != null && m_createdFormData != null) {
      // exec load method
      IMethodSourceBuilder execLoadSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newHandlerBuilder, "execLoad");
      execLoadSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          String serviceInterfaceName = validator.getTypeName(SignatureCache.createTypeSignature(m_createdServiceInterface.getFullyQualifiedName()));
          String formDataTypeName = validator.getTypeName(SignatureCache.createTypeSignature(m_createdFormData.getFullyQualifiedName()));
          source.append(serviceInterfaceName).append(" service = ");
          source.append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.SERVICES))).append(".getService(");
          source.append(serviceInterfaceName).append(".class);").append(lineDelimiter);
          source.append(formDataTypeName).append(" formData = new ").append(formDataTypeName).append("();").append(lineDelimiter);
          source.append("exportFormData(formData);").append(lineDelimiter);
          if (TypeUtility.exists(TypeUtility.getMethod(m_createdServiceInterface, "prepareCreate"))) {
            source.append("formData = service.prepareCreate(formData);").append(lineDelimiter);
          }
          else {
            source.append(ScoutUtility.getCommentBlock("service call here")).append(lineDelimiter);
          }
          source.append("importFormData(formData);").append(lineDelimiter);
        }
      });
      newHandlerBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execLoadSourceBuilder), execLoadSourceBuilder);
      // exec store method
      IMethodSourceBuilder execStoreSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newHandlerBuilder, "execStore");
      execStoreSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          String serviceInterfaceName = validator.getTypeName(SignatureCache.createTypeSignature(m_createdServiceInterface.getFullyQualifiedName()));
          String formDataTypeName = validator.getTypeName(SignatureCache.createTypeSignature(m_createdFormData.getFullyQualifiedName()));
          source.append(serviceInterfaceName).append(" service = ").append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.SERVICES))).append(".getService(").append(serviceInterfaceName).append(".class);").append(lineDelimiter);
          source.append(formDataTypeName).append(" formData = new ").append(formDataTypeName).append("();").append(lineDelimiter);
          source.append("exportFormData(formData);").append(lineDelimiter);
          if (TypeUtility.exists(TypeUtility.getMethod(m_createdServiceInterface, "create"))) {
            source.append("formData = service.create(formData);").append(lineDelimiter);
          }
          else {
            source.append(ScoutUtility.getCommentBlock("service call here")).append("").append(lineDelimiter);
          }
        }
      });
      newHandlerBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execStoreSourceBuilder), execStoreSourceBuilder);
    }

    // start method
    final String handlerFqn = getPackageName() + "." + getElementName() + "." + newHandlerBuilder.getElementName();
    IMethodSourceBuilder startHandlerMethodBuilder = new MethodSourceBuilder("start" + SdkProperties.TYPE_NAME_NEW_HANDLER_PREFIX);
    startHandlerMethodBuilder.setFlags(Flags.AccPublic);
    startHandlerMethodBuilder.setReturnTypeSignature(Signature.SIG_VOID);
    startHandlerMethodBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    startHandlerMethodBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    startHandlerMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("startInternal(new ").append(validator.getTypeName(SignatureCache.createTypeSignature(handlerFqn))).append("());");
      }
    });
    formSourceBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodStartFormKey(startHandlerMethodBuilder), startHandlerMethodBuilder);
  }

  /**
   * @param typeSourceBuilder
   * @throws CoreException
   */
  protected void createModifyHandler(ITypeSourceBuilder formSourceBuilder) throws CoreException {
    ITypeSourceBuilder modifyHandlerBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_MODIFY_HANDLER);
    modifyHandlerBuilder.setFlags(Flags.AccPublic);
    modifyHandlerBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IFormHandler, getJavaProject()));
    formSourceBuilder.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormHandlerKey(modifyHandlerBuilder), modifyHandlerBuilder);

    if (m_createdServiceInterface != null && m_createdFormData != null) {
      // exec load method
      IMethodSourceBuilder execLoadSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(modifyHandlerBuilder, "execLoad");
      execLoadSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          String serviceInterfaceName = validator.getTypeName(SignatureCache.createTypeSignature(m_createdServiceInterface.getFullyQualifiedName()));
          String formDataTypeName = validator.getTypeName(SignatureCache.createTypeSignature(m_createdFormData.getFullyQualifiedName()));
          source.append(serviceInterfaceName).append(" service = ");
          source.append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.SERVICES))).append(".getService(");
          source.append(serviceInterfaceName).append(".class);").append(lineDelimiter);
          source.append(formDataTypeName).append(" formData = new ").append(formDataTypeName).append("();").append(lineDelimiter);
          source.append("exportFormData(formData);").append(lineDelimiter);
          if (TypeUtility.exists(TypeUtility.getMethod(m_createdServiceInterface, "load"))) {
            source.append("formData = service.load(formData);").append(lineDelimiter);
          }
          else {
            source.append(ScoutUtility.getCommentBlock("service call here")).append(lineDelimiter);
          }
          source.append("importFormData(formData);").append(lineDelimiter);
          if (getCreatedUpdatePermission() != null) {
            source.append("setEnabledPermission(new " + validator.getTypeName(SignatureCache.createTypeSignature(getCreatedUpdatePermission().getFullyQualifiedName()))).append("());").append(lineDelimiter);
          }
        }
      });
      modifyHandlerBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execLoadSourceBuilder), execLoadSourceBuilder);
      // exec store method
      IMethodSourceBuilder execStoreSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(modifyHandlerBuilder, "execStore");
      execStoreSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          String serviceInterfaceName = validator.getTypeName(SignatureCache.createTypeSignature(m_createdServiceInterface.getFullyQualifiedName()));
          String formDataTypeName = validator.getTypeName(SignatureCache.createTypeSignature(m_createdFormData.getFullyQualifiedName()));
          source.append(serviceInterfaceName).append(" service = ").append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.SERVICES))).append(".getService(").append(serviceInterfaceName).append(".class);").append(lineDelimiter);
          source.append(formDataTypeName).append(" formData = new ").append(formDataTypeName).append("();").append(lineDelimiter);
          source.append("exportFormData(formData);").append(lineDelimiter);
          if (TypeUtility.exists(TypeUtility.getMethod(m_createdServiceInterface, "store"))) {
            source.append("formData = service.store(formData);").append(lineDelimiter);
          }
          else {
            source.append(ScoutUtility.getCommentBlock("service call here")).append("").append(lineDelimiter);
          }
        }
      });
      modifyHandlerBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execStoreSourceBuilder), execStoreSourceBuilder);
    }

    // start method
    final String handlerFqn = getPackageName() + "." + getElementName() + "." + modifyHandlerBuilder.getElementName();
    IMethodSourceBuilder startHandlerMethodBuilder = new MethodSourceBuilder("start" + SdkProperties.TYPE_NAME_MODIFY_HANDLER_PREFIX);
    startHandlerMethodBuilder.setFlags(Flags.AccPublic);
    startHandlerMethodBuilder.setReturnTypeSignature(Signature.SIG_VOID);
    startHandlerMethodBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    startHandlerMethodBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    startHandlerMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("startInternal(new ").append(validator.getTypeName(SignatureCache.createTypeSignature(handlerFqn))).append("());");
      }
    });
    formSourceBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodStartFormKey(startHandlerMethodBuilder), startHandlerMethodBuilder);
  }

  public void addServiceProxyRegistrationProject(IJavaProject project) {
    m_serviceProxyRegistrationProjects.add(project);
  }

  /**
   * @param clientServiceRegistryProjects
   *          the clientServiceRegistryProjects to set
   */
  public void setServiceProxyRegistrationProjects(List<IJavaProject> clientServiceRegistryProjects) {
    m_serviceProxyRegistrationProjects.clear();
    m_serviceProxyRegistrationProjects.addAll(clientServiceRegistryProjects);
  }

  /**
   * @return the serviceProxyRegistrationProjects
   */
  public List<IJavaProject> getServiceProxyRegistrationProjects() {
    return Collections.unmodifiableList(m_serviceProxyRegistrationProjects);
  }

  /**
   * @return the formDataProject
   */
  public IJavaProject getFormDataProject() {
    return m_formDataProject;
  }

  /**
   * @param formDataProject
   *          the formDataProject to set
   */
  public void setFormDataProject(IJavaProject formDataProject) {
    m_formDataProject = formDataProject;
  }

  /**
   * @return the serviceInterfaceProject
   */
  public IJavaProject getServiceInterfaceProject() {
    return m_serviceInterfaceProject;
  }

  /**
   * @param serviceInterfaceProject
   *          the serviceInterfaceProject to set
   */
  public void setServiceInterfaceProject(IJavaProject serviceInterfaceProject) {
    m_serviceInterfaceProject = serviceInterfaceProject;
  }

  /**
   * @return the serviceInterfaceName
   */
  public String getServiceInterfaceName() {
    return m_serviceInterfaceName;
  }

  /**
   * @param serviceInterfaceName
   *          the serviceInterfaceName to set
   */
  public void setServiceInterfaceName(String serviceInterfaceName) {
    m_serviceInterfaceName = serviceInterfaceName;
  }

  /**
   * @return the serviceImplementationName
   */
  public String getServiceImplementationName() {
    return m_serviceImplementationName;
  }

  /**
   * @param serviceImplementationName
   *          the serviceImplementationName to set
   */
  public void setServiceImplementationName(String serviceImplementationName) {
    m_serviceImplementationName = serviceImplementationName;
  }

  /**
   * @return the permissionCreateProject
   */
  public IJavaProject getPermissionCreateProject() {
    return m_permissionCreateProject;
  }

  /**
   * @param permissionCreateProject
   *          the permissionCreateProject to set
   */
  public void setPermissionCreateProject(IJavaProject permissionCreateProject) {
    m_permissionCreateProject = permissionCreateProject;
  }

  /**
   * @return the permissionReadProject
   */
  public IJavaProject getPermissionReadProject() {
    return m_permissionReadProejct;
  }

  /**
   * @param permissionReadProject
   *          the permissionReadProject to set
   */
  public void setPermissionReadProject(IJavaProject permissionReadProject) {
    m_permissionReadProejct = permissionReadProject;
  }

  /**
   * @return the permissionUpdateProject
   */
  public IJavaProject getPermissionUpdateProject() {
    return m_permissionUpdateProject;
  }

  /**
   * @param permissionUpdateProject
   *          the permissionUpdateProject to set
   */
  public void setPermissionUpdateProject(IJavaProject permissionUpdateProject) {
    m_permissionUpdateProject = permissionUpdateProject;
  }

  /**
   * @return the permissionCreateName
   */
  public String getPermissionCreateName() {
    return m_permissionCreateName;
  }

  /**
   * @param permissionCreateName
   *          the permissionCreateName to set
   */
  public void setPermissionCreateName(String permissionCreateName) {
    m_permissionCreateName = permissionCreateName;
  }

  /**
   * @return the permissionReadName
   */
  public String getPermissionReadName() {
    return m_permissionReadName;
  }

  /**
   * @param permissionReadName
   *          the permissionReadName to set
   */
  public void setPermissionReadName(String permissionReadName) {
    m_permissionReadName = permissionReadName;
  }

  /**
   * @return the permissionUpdateName
   */
  public String getPermissionUpdateName() {
    return m_permissionUpdateName;
  }

  /**
   * @param permissionUpdateName
   *          the permissionUpdateName to set
   */
  public void setPermissionUpdateName(String permissionUpdateName) {
    m_permissionUpdateName = permissionUpdateName;
  }

  /**
   * @return the serviceImplementationProject
   */
  public IJavaProject getServiceImplementationProject() {
    return m_serviceImplementationProject;
  }

  /**
   * @param serviceImplementationProject
   *          the serviceImplementationProject to set
   */
  public void setServiceImplementationProject(IJavaProject serviceImplementationProject) {
    m_serviceImplementationProject = serviceImplementationProject;
  }

  public boolean isCreateNewHandler() {
    return m_createNewHandler;
  }

  public void setCreateNewHandler(boolean createNewHandler) {
    m_createNewHandler = createNewHandler;
  }

  public boolean isCreateModifyHandler() {
    return m_createModifyHandler;
  }

  public void setCreateModifyHandler(boolean createModifyHandler) {
    m_createModifyHandler = createModifyHandler;
  }

  /**
   * @return the outProcessService
   */
  public IType getCreatedService() {
    return m_createdService;
  }

  /**
   * @return the outProcessServiceInterface
   */
  public IType getCreatedServiceInterface() {
    return m_createdServiceInterface;
  }

  /**
   * @return the outFormData
   */
  public IType getCreatedFormData() {
    return m_createdFormData;
  }

  /**
   * @return the outReadPermission
   */
  public IType getCreatedReadPermission() {
    return m_createdReadPermission;
  }

  /**
   * @return the outCreatePermission
   */
  public IType getCreatedCreatePermission() {
    return m_createdCreatePermission;
  }

  /**
   * @return the outUpdatePermission
   */
  public IType getCreatedUpdatePermission() {
    return m_createdUpdatePermission;
  }

  /**
   * @return the outNewHandler
   */
  public IType getOutNewHandler() {
    return m_createdNewHandler;
  }

  /**
   * @return the outModifyHandler
   */
  public IType getOutModifyHandler() {
    return m_createdModifyHandler;
  }

  public String getFormDataPackage() {
    return m_formDataPackage;
  }

  public void setFormDataPackage(String formDataPackage) {
    m_formDataPackage = formDataPackage;
  }

  public String getServiceInterfacePackage() {
    return m_serviceInterfacePackage;
  }

  public void setServiceInterfacePackage(String serviceInterfacePackage) {
    m_serviceInterfacePackage = serviceInterfacePackage;
  }

  public String getServiceImplementationPackage() {
    return m_serviceImplementationPackage;
  }

  public void setServiceImplementationPackage(String serviceImplementationPackage) {
    m_serviceImplementationPackage = serviceImplementationPackage;
  }

  public String getPermissionCreatePackage() {
    return m_permissionCreatePackage;
  }

  public void setPermissionCreatePackage(String permissionCreatePackage) {
    m_permissionCreatePackage = permissionCreatePackage;
  }

  public String getPermissionReadPackage() {
    return m_permissionReadPackage;
  }

  public void setPermissionReadPackage(String permissionReadPackage) {
    m_permissionReadPackage = permissionReadPackage;
  }

  public String getPermissionUpdatePackage() {
    return m_permissionUpdatePackage;
  }

  public void setPermissionUpdatePackage(String permissionUpdatePackage) {
    m_permissionUpdatePackage = permissionUpdatePackage;
  }

  public String getServiceImplementationSuperTypeSignature() {
    return m_serviceImplementationSuperTypeSignature;
  }

  public void setServiceImplementationSuperTypeSignature(String serviceSuperTypeSignature) {
    m_serviceImplementationSuperTypeSignature = serviceSuperTypeSignature;
  }

  public boolean addServiceRegistration(ServiceRegistrationDescription desc) {
    return m_serviceRegistrationDescriptions.add(desc);
  }

  public boolean removeServiceRegistration(ServiceRegistrationDescription desc) {
    return m_serviceRegistrationDescriptions.remove(desc);
  }

  public void setServiceRegistrations(List<ServiceRegistrationDescription> desc) {
    m_serviceRegistrationDescriptions.clear();
    m_serviceRegistrationDescriptions.addAll(desc);
  }

  public List<ServiceRegistrationDescription> getServiceRegistrations() {
    return Collections.unmodifiableList(m_serviceRegistrationDescriptions);
  }
}
