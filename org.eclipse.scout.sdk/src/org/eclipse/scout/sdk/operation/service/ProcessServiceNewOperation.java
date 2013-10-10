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
/**
 *
 */
package org.eclipse.scout.sdk.operation.service;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.PermissionNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link ProcessServiceNewOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 10.02.2010
 */
public class ProcessServiceNewOperation extends ServiceNewOperation {
  private static final String TEXT_AUTHORIZATION_FAILED = "AuthorizationFailed";

  private IType m_formData;
  private IJavaProject m_permissionCreateProject;
  private String m_permissionCreatePackageName;
  private String m_permissionCreateName;
  private IJavaProject m_permissionReadProject;
  private String m_permissionReadPackageName;
  private String m_permissionReadName;
  private IJavaProject m_permissionUpdateProject;
  private String m_permissionUpdatePackageName;
  private String m_permissionUpdateName;

  // created types
  private IType m_createdReadPermission;
  private IType m_createdUpdatePermission;
  private IType m_createdCreatePermission;

  public ProcessServiceNewOperation(String serviceName) {
    this("I" + serviceName, serviceName);
  }

  public ProcessServiceNewOperation(String interfaceName, String implementationName) {
    super(interfaceName, implementationName);
    getInterfaceSourceBuilder().addInterfaceSignature(SignatureCache.createTypeSignature(RuntimeClasses.IService2));
    getInterfaceSourceBuilder().addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createValidationStrategyProcess());
  }

  @Override
  public String getOperationName() {
    return "create process service '" + getImplementationName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {

  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // permissions
    if (getPermissionCreateProject() != null) {
      PermissionNewOperation permissionOp = new PermissionNewOperation(getPermissionCreateName(), getPermissionCreatePackageName(), getPermissionCreateProject());
      permissionOp.validate();
      permissionOp.run(monitor, workingCopyManager);
      m_createdCreatePermission = permissionOp.getCreatedType();
    }
    if (getPermissionReadProject() != null) {
      PermissionNewOperation permissionOp = new PermissionNewOperation(getPermissionReadName(), getPermissionReadPackageName(), getPermissionReadProject());
      permissionOp.validate();
      permissionOp.run(monitor, workingCopyManager);
      m_createdReadPermission = permissionOp.getCreatedType();
    }
    if (getPermissionUpdateProject() != null) {
      PermissionNewOperation permissionOp = new PermissionNewOperation(getPermissionUpdateName(), getPermissionUpdatePackageName(), getPermissionUpdateProject());
      permissionOp.validate();
      permissionOp.run(monitor, workingCopyManager);
      m_createdUpdatePermission = permissionOp.getCreatedType();
    }

    if (TypeUtility.exists(getFormData())) {
      String interfaceFqn = null;
      if (getInterfaceProject() != null && getInterfacePackageName() != null) {
        interfaceFqn = getInterfacePackageName() + "." + getInterfaceName();
      }
      String formDataSignature = SignatureCache.createTypeSignature(getFormData().getFullyQualifiedName());
      IJavaProject nlsLookupProject = getInterfaceProject();
      if (nlsLookupProject == null) {
        // fallback
        nlsLookupProject = getImplementationProject();
      }
      INlsProject nlsProject = ScoutTypeUtility.findNlsProject(nlsLookupProject);
      // create methods
      createCreateMethod(interfaceFqn, formDataSignature, nlsProject);
      createLoadMethod(interfaceFqn, formDataSignature, nlsProject);
      createPrepareCreateMethod(interfaceFqn, formDataSignature, nlsProject);
      createStoreMethod(interfaceFqn, formDataSignature, nlsProject);
    }
    super.run(monitor, workingCopyManager);
  }

  /**
   *
   */
  private void createCreateMethod(String interfaceFqn, String formDataSignature, final INlsProject nlsProject) {
    ServiceMethod createMethod = new ServiceMethod("create", interfaceFqn);
    createMethod.addParameter(new MethodParameter("formData", formDataSignature));
    createMethod.setReturnTypeSignature(formDataSignature);
    createMethod.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
    createMethod.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        IType createPermission = getCreatedCreatePermission();
        if (createPermission != null) {
          source.append("if(!").append(SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ACCESS, validator));
          source.append(".check(new ").append(SignatureUtility.getTypeReferenceFromFqn(createPermission.getFullyQualifiedName(), validator)).append("())){").append(lineDelimiter);
          source.append("throw new ").append(SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.VetoException, validator)).append("(");
          if (nlsProject != null) {
            source.append(SignatureUtility.getTypeReferenceFromFqn(nlsProject.getNlsAccessorType().getFullyQualifiedName(), validator));
            source.append(".get(\"").append(TEXT_AUTHORIZATION_FAILED).append("\")");
          }
          else {
            source.append("\"Authorization Failed\"");
          }
          source.append(");").append(lineDelimiter);
          source.append("}").append(lineDelimiter);
        }
        source.append(ScoutUtility.getCommentBlock("business logic here.")).append(lineDelimiter);
        source.append("return formData;");
      }
    });
    getInterfaceSourceBuilder().addMethodSourceBuilder(createMethod.getInterfaceSourceBuilder());
    getImplementationSourceBuilder().addMethodSourceBuilder(createMethod.getImplementationSourceBuilder());
  }

  /**
  *
  */
  private void createLoadMethod(String interfaceFqn, String formDataSignature, final INlsProject nlsProject) {
    ServiceMethod loadMethod = new ServiceMethod("load", interfaceFqn);
    loadMethod.addParameter(new MethodParameter("formData", formDataSignature));
    loadMethod.setReturnTypeSignature(formDataSignature);
    loadMethod.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
    loadMethod.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        IType readPermission = getCreatedReadPermission();
        if (readPermission != null) {
          source.append("if(!").append(SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ACCESS, validator));
          source.append(".check(new ").append(SignatureUtility.getTypeReferenceFromFqn(readPermission.getFullyQualifiedName(), validator)).append("())){").append(lineDelimiter);
          source.append("throw new ").append(SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.VetoException, validator)).append("(");
          if (nlsProject != null) {
            source.append(SignatureUtility.getTypeReferenceFromFqn(nlsProject.getNlsAccessorType().getFullyQualifiedName(), validator));
            source.append(".get(\"").append(TEXT_AUTHORIZATION_FAILED).append("\")");
          }
          else {
            source.append("\"Authorization Failed\"");
          }
          source.append(");").append(lineDelimiter);
          source.append("}").append(lineDelimiter);
        }
        source.append(ScoutUtility.getCommentBlock("business logic here.")).append(lineDelimiter);
        source.append("return formData;");
      }
    });
    getInterfaceSourceBuilder().addMethodSourceBuilder(loadMethod.getInterfaceSourceBuilder());
    getImplementationSourceBuilder().addMethodSourceBuilder(loadMethod.getImplementationSourceBuilder());
  }

  /**
  *
  */
  private void createPrepareCreateMethod(String interfaceFqn, String formDataSignature, final INlsProject nlsProject) {
    ServiceMethod serviceMethod = new ServiceMethod("prepareCreate", interfaceFqn);
    serviceMethod.addParameter(new MethodParameter("formData", formDataSignature));
    serviceMethod.setReturnTypeSignature(formDataSignature);
    serviceMethod.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
    serviceMethod.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        IType createPermission = getCreatedCreatePermission();
        if (createPermission != null) {
          source.append("if(!").append(SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ACCESS, validator));
          source.append(".check(new ").append(SignatureUtility.getTypeReferenceFromFqn(createPermission.getFullyQualifiedName(), validator)).append("())){").append(lineDelimiter);
          source.append("throw new ").append(SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.VetoException, validator)).append("(");
          if (nlsProject != null) {
            source.append(SignatureUtility.getTypeReferenceFromFqn(nlsProject.getNlsAccessorType().getFullyQualifiedName(), validator));
            source.append(".get(\"").append(TEXT_AUTHORIZATION_FAILED).append("\")");
          }
          else {
            source.append("\"Authorization Failed\"");
          }
          source.append(");").append(lineDelimiter);
          source.append("}").append(lineDelimiter);
        }
        source.append(ScoutUtility.getCommentBlock("business logic here.")).append(lineDelimiter);
        source.append("return formData;");
      }
    });
    getInterfaceSourceBuilder().addMethodSourceBuilder(serviceMethod.getInterfaceSourceBuilder());
    getImplementationSourceBuilder().addMethodSourceBuilder(serviceMethod.getImplementationSourceBuilder());
  }

  /**
  *
  */
  private void createStoreMethod(String interfaceFqn, String formDataSignature, final INlsProject nlsProject) {
    ServiceMethod serviceMethod = new ServiceMethod("store", interfaceFqn);
    serviceMethod.addParameter(new MethodParameter("formData", formDataSignature));
    serviceMethod.setReturnTypeSignature(formDataSignature);
    serviceMethod.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
    serviceMethod.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        IType updatePermission = getCreatedUpdatePermission();
        if (updatePermission != null) {
          source.append("if(!").append(SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ACCESS, validator));
          source.append(".check(new ").append(SignatureUtility.getTypeReferenceFromFqn(updatePermission.getFullyQualifiedName(), validator)).append("())){").append(lineDelimiter);
          source.append("throw new ").append(SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.VetoException, validator)).append("(");
          if (nlsProject != null) {
            source.append(SignatureUtility.getTypeReferenceFromFqn(nlsProject.getNlsAccessorType().getFullyQualifiedName(), validator));
            source.append(".get(\"").append(TEXT_AUTHORIZATION_FAILED).append("\")");
          }
          else {
            source.append("\"Authorization Failed\"");
          }
          source.append(");").append(lineDelimiter);
          source.append("}").append(lineDelimiter);
        }
        source.append(ScoutUtility.getCommentBlock("business logic here.")).append(lineDelimiter);
        source.append("return formData;");
      }
    });
    getInterfaceSourceBuilder().addMethodSourceBuilder(serviceMethod.getInterfaceSourceBuilder());
    getImplementationSourceBuilder().addMethodSourceBuilder(serviceMethod.getImplementationSourceBuilder());
  }

  /**
   * @return the createdReadPermission
   */
  public IType getCreatedReadPermission() {
    return m_createdReadPermission;
  }

  /**
   * @return the createdUpdatePermission
   */
  public IType getCreatedUpdatePermission() {
    return m_createdUpdatePermission;
  }

  /**
   * @return the createdCreatePermission
   */
  public IType getCreatedCreatePermission() {
    return m_createdCreatePermission;
  }

  public void setPermissionsEntityName(String entityName) {
    setPermissionCreateName("Create" + entityName + "Permission");
    setPermissionReadName("Read" + entityName + "Permission");
    setPermissionUpdateName("Update" + entityName + "Permission");
  }

  public void setPermissionsProject(IJavaProject project) {
    setPermissionCreateProject(project);
    setPermissionReadProject(project);
    setPermissionUpdateProject(project);
  }

  public void setPermissionsPackageName(String permissionsPackageName) {
    setPermissionCreatePackageName(permissionsPackageName);
    setPermissionReadPackageName(permissionsPackageName);
    setPermissionUpdatePackageName(permissionsPackageName);
  }

  public void setPermissionCreateProject(IJavaProject permissionCreateProject) {
    m_permissionCreateProject = permissionCreateProject;
  }

  public IJavaProject getPermissionCreateProject() {
    return m_permissionCreateProject;
  }

  public void setPermissionCreatePackageName(String permissionCreatePackageName) {
    m_permissionCreatePackageName = permissionCreatePackageName;
  }

  public String getPermissionCreatePackageName() {
    return m_permissionCreatePackageName;
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

  public void setPermissionReadProject(IJavaProject permissionReadProject) {
    m_permissionReadProject = permissionReadProject;
  }

  public IJavaProject getPermissionReadProject() {
    return m_permissionReadProject;
  }

  public void setPermissionReadPackageName(String permissionReadPackageName) {
    m_permissionReadPackageName = permissionReadPackageName;
  }

  public String getPermissionReadPackageName() {
    return m_permissionReadPackageName;
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

  public void setPermissionUpdateProject(IJavaProject permissionUpdateProject) {
    m_permissionUpdateProject = permissionUpdateProject;
  }

  public IJavaProject getPermissionUpdateProject() {
    return m_permissionUpdateProject;
  }

  public void setPermissionUpdatePackageName(String permissionUpdatePackageName) {
    m_permissionUpdatePackageName = permissionUpdatePackageName;
  }

  public String getPermissionUpdatePackageName() {
    return m_permissionUpdatePackageName;
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
   * @param formData
   *          the formData to set
   */
  public void setFormData(IType formData) {
    m_formData = formData;
  }

  /**
   * @return the formData
   */
  public IType getFormData() {
    return m_formData;
  }

}
