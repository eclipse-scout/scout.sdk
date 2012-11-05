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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.PermissionNewOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link ProcessServiceNewOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 10.02.2010
 */
public class ProcessServiceNewOperation implements IOperation {

  private IScoutBundle[] m_clientServiceRegistryBundles;
  private IScoutBundle m_serviceInterfaceBundle;
  private String m_serviceInterfaceName;
  private IScoutBundle m_permissionCreateBundle;
  private String m_permissionCreateName;
  private IScoutBundle m_permissionReadBundle;
  private String m_permissionReadName;
  private IScoutBundle m_permissionUpdateBundle;
  private IScoutBundle m_serviceImplementationBundle;
  private String m_serviceImplementationName;
  private String m_permissionUpdateName;
  private IScoutBundle[] m_serverServiceRegistryBundles;
  private IType m_formData;

  // created types
  private IType m_createdServiceInterface;
  private IType m_createdServiceImplementation;
  private IType m_createdReadPermission;
  private IType m_createdUpdatePermission;
  private IType m_createdCreatePermission;

  @Override
  public String getOperationName() {
    return "create process service '" + getServiceImplementationName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {

  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // permissions
    if (getPermissionCreateBundle() != null) {
      PermissionNewOperation permissionOp = new PermissionNewOperation();
      permissionOp.setSharedBundle(getPermissionCreateBundle());
      permissionOp.setTypeName(getPermissionCreateName());
      permissionOp.run(monitor, workingCopyManager);
      m_createdCreatePermission = permissionOp.getCreatedPermission();
    }
    if (getPermissionReadBundle() != null) {
      PermissionNewOperation permissionOp = new PermissionNewOperation();
      permissionOp.setSharedBundle(getPermissionReadBundle());
      permissionOp.setTypeName(getPermissionReadName());
      permissionOp.run(monitor, workingCopyManager);
      m_createdReadPermission = permissionOp.getCreatedPermission();
    }
    if (getPermissionUpdateBundle() != null) {
      PermissionNewOperation permissionOp = new PermissionNewOperation();
      permissionOp.setSharedBundle(getPermissionUpdateBundle());
      permissionOp.setTypeName(getPermissionUpdateName());
      permissionOp.run(monitor, workingCopyManager);
      m_createdUpdatePermission = permissionOp.getCreatedPermission();
    }

    if (getServiceImplementationBundle() != null) {
      // service interface
      ServiceNewOperation serviceOp = new ServiceNewOperation();
      for (IScoutBundle cb : getClientServiceRegistryBundles()) {
        serviceOp.addProxyRegistrationBundle(cb);
      }
      for (IScoutBundle sb : getServerServiceRegistryBundles()) {
        serviceOp.addServiceRegistrationBundle(sb);
      }
      if (getServiceImplementationBundle() != null) {
        serviceOp.setImplementationBundle(getServiceImplementationBundle());
        serviceOp.setServiceName(getServiceImplementationName());
        serviceOp.setServicePackageName(getServiceImplementationBundle().getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_PROCESS));
      }
      if (getServiceInterfaceBundle() != null) {
        serviceOp.setInterfaceBundle(getServiceInterfaceBundle());
        serviceOp.setServiceInterfaceName(getServiceInterfaceName());
        serviceOp.setServiceInterfacePackageName(getServiceInterfaceBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_PROCESS));
        serviceOp.setServiceInterfaceSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.IService, true));
      }
      serviceOp.setServiceSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractService, true));
      serviceOp.run(monitor, workingCopyManager);
      m_createdServiceInterface = serviceOp.getCreatedServiceInterface();
      m_createdServiceImplementation = serviceOp.getCreatedServiceImplementation();

      // input validation annotation for process services
      AnnotationCreateOperation valStratOp = new AnnotationCreateOperation(m_createdServiceInterface, Signature.createTypeSignature(RuntimeClasses.InputValidation, true)) {
        @Override
        public TextEdit createEdit(IImportValidator validator, String NL) throws CoreException {
          validator.addImport(RuntimeClasses.InputValidation);
          validator.addImport(RuntimeClasses.IValidationStrategy);
          return super.createEdit(validator, NL);
        }
      };
      valStratOp.addParameter("IValidationStrategy.PROCESS.class");
      valStratOp.validate();
      valStratOp.run(monitor, workingCopyManager);

      // fill service
      if (getFormData() != null) {
        ProcessServiceCreateMethodOperation processServiceFillOp = new ProcessServiceCreateMethodOperation();
        processServiceFillOp.setFormData(getFormData());
        processServiceFillOp.setCreateCreateMethod(true);
        processServiceFillOp.setCreateLoadMethod(true);
        processServiceFillOp.setCreatePrepareCreateMethod(true);
        processServiceFillOp.setCreateStoreMethod(true);
        processServiceFillOp.setCreatePermission(getCreatedCreatePermission());
        processServiceFillOp.setReadPermission(getCreatedReadPermission());
        processServiceFillOp.setUpdatePermission(getCreatedUpdatePermission());
        processServiceFillOp.setServiceImplementations(new IType[]{getCreatedServiceImplementation()});
        processServiceFillOp.setServiceInterface(getCreatedServiceInterface());
        processServiceFillOp.run(monitor, workingCopyManager);
      }
    }
  }

  /**
   * @return the createdServiceInterface
   */
  public IType getCreatedServiceInterface() {
    return m_createdServiceInterface;
  }

  /**
   * @return the createdServiceImplementation
   */
  public IType getCreatedServiceImplementation() {
    return m_createdServiceImplementation;
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

  /**
   * @return the clientServiceRegistryBundles
   */
  public IScoutBundle[] getClientServiceRegistryBundles() {
    return m_clientServiceRegistryBundles;
  }

  /**
   * @param clientServiceRegistryBundles
   *          the clientServiceRegistryBundles to set
   */
  public void setClientServiceRegistryBundles(IScoutBundle[] clientServiceRegistryBundles) {
    m_clientServiceRegistryBundles = clientServiceRegistryBundles;
  }

  /**
   * @return the serviceInterfaceBundle
   */
  public IScoutBundle getServiceInterfaceBundle() {
    return m_serviceInterfaceBundle;
  }

  /**
   * @param serviceInterfaceBundle
   *          the serviceInterfaceBundle to set
   */
  public void setServiceInterfaceBundle(IScoutBundle serviceInterfaceBundle) {
    m_serviceInterfaceBundle = serviceInterfaceBundle;
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
   * @return the permissionCreateBundle
   */
  public IScoutBundle getPermissionCreateBundle() {
    return m_permissionCreateBundle;
  }

  /**
   * @param permissionCreateBundle
   *          the permissionCreateBundle to set
   */
  public void setPermissionCreateBundle(IScoutBundle permissionCreateBundle) {
    m_permissionCreateBundle = permissionCreateBundle;
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
   * @return the permissionReadBundle
   */
  public IScoutBundle getPermissionReadBundle() {
    return m_permissionReadBundle;
  }

  /**
   * @param permissionReadBundle
   *          the permissionReadBundle to set
   */
  public void setPermissionReadBundle(IScoutBundle permissionReadBundle) {
    m_permissionReadBundle = permissionReadBundle;
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
   * @return the permissionUpdateBundle
   */
  public IScoutBundle getPermissionUpdateBundle() {
    return m_permissionUpdateBundle;
  }

  /**
   * @param permissionUpdateBundle
   *          the permissionUpdateBundle to set
   */
  public void setPermissionUpdateBundle(IScoutBundle permissionUpdateBundle) {
    m_permissionUpdateBundle = permissionUpdateBundle;
  }

  /**
   * @return the serviceImplementationBundle
   */
  public IScoutBundle getServiceImplementationBundle() {
    return m_serviceImplementationBundle;
  }

  /**
   * @param serviceImplementationBundle
   *          the serviceImplementationBundle to set
   */
  public void setServiceImplementationBundle(IScoutBundle serviceImplementationBundle) {
    m_serviceImplementationBundle = serviceImplementationBundle;
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
   * @return the serverServiceRegistryBundles
   */
  public IScoutBundle[] getServerServiceRegistryBundles() {
    return m_serverServiceRegistryBundles;
  }

  /**
   * @param serverServiceRegistryBundles
   *          the serverServiceRegistryBundles to set
   */
  public void setServerServiceRegistryBundles(IScoutBundle[] serverServiceRegistryBundles) {
    m_serverServiceRegistryBundles = serverServiceRegistryBundles;
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
