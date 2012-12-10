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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.BeanPropertyNewOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.PermissionNewOperation;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.operation.service.ProcessServiceCreateMethodOperation;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class FormStackNewOperation implements IOperation {

  private String m_formName;
  private INlsEntry m_nlsEntry;
  private String m_formSuperTypeSignature;
  private String m_formIdName;
  private IScoutBundle m_formBundle;
  private boolean m_createButtonOk = false;
  private boolean m_createButtonCancel = false;
  private boolean m_createIdProperty = false;
  private boolean m_createNewHandler = false;
  private boolean m_createModifyHandler = false;
  private IScoutBundle[] m_clientServiceRegistryBundles;
  private IScoutBundle m_formDataBundle;
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
  private boolean m_formatSource;

  // operation members
  private IType m_outProcessService;
  private IType m_outProcessServiceInterface;
  private IType m_outFormData;
  private IType m_outForm;
  private IType m_outMainBox;
  private IType m_outReadPermission;
  private IType m_outCreatePermission;
  private IType m_outUpdatePermission;
  private IType m_outNewHandler;
  private IType m_outModifyHandler;

  public FormStackNewOperation() {
    this(false);
  }

  public FormStackNewOperation(boolean formatSource) {
    m_formatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return "Create Form and Service '" + getFormName() + "'.";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getFormName())) {
      throw new IllegalArgumentException("Form name can not be null.");
    }
    if (isCreateIdProperty() && StringUtility.isNullOrEmpty(getFormIdName())) {
      throw new IllegalArgumentException("FormId can not be null.");
    }

  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // create empty form data
    String formDataSignature = null;
    if (getFormDataBundle() != null) {
      ScoutTypeNewOperation formDataOp = new ScoutTypeNewOperation(getFormName() + "Data", getFormDataBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES), getFormDataBundle());
      formDataOp.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.AbstractFormData));
      formDataOp.run(monitor, workingCopyManager);
      m_outFormData = formDataOp.getCreatedType();
      formDataSignature = SignatureCache.createTypeSignature(m_outFormData.getFullyQualifiedName());
    }
    // form
    FormNewOperation formOp = new FormNewOperation();
    formOp.setClientBundle(getFormBundle());
    formOp.setFormatSource(m_formatSource);
    // write back members
    formOp.setNlsEntry(getNlsEntry());
    formOp.setTypeName(getFormName());
    if (getFormSuperTypeSignature() != null) {
      formOp.setSuperTypeSignature(getFormSuperTypeSignature());
    }
    formOp.setFormDataSignature(formDataSignature);

    formOp.setCreateButtonOk(isCreateButtonOk());
    formOp.setCreateButtonCancel(isCreateButtonCancel());
    formOp.validate();
    formOp.run(monitor, workingCopyManager);
    m_outForm = formOp.getCreatedFormType();
    m_outMainBox = formOp.getCreatedMainBox();
    m_outForm.getCompilationUnit().reconcile(ICompilationUnit.NO_AST, false, null, monitor);
    if (isCreateIdProperty()) {
      BeanPropertyNewOperation beanPropOp = new BeanPropertyNewOperation(getOutForm(), getFormIdName(), SignatureCache.createTypeSignature(Long.class.getName()), Flags.AccPublic);
      beanPropOp.setCreateFormDataAnnotation(true);
      beanPropOp.setSiblingMethods(formOp.getCreatedMainBoxGetter());
      beanPropOp.setSiblingField(null);
      beanPropOp.run(monitor, workingCopyManager);
    }
//    // form data
//    if (getFormDataBundle() != null) {
//      FormDataUpdateOperation formDataOp = new FormDataUpdateOperation(getOutForm(), getFormDataBundle());
//      formDataOp.validate();
//      formDataOp.run(monitor, workingCopyManager);
//      m_outFormData = formDataOp.getFormDataType();
//
//    }
    // permissions
    if (getPermissionCreateBundle() != null) {
      PermissionNewOperation permissionOp = new PermissionNewOperation(m_formatSource);
      permissionOp.setSharedBundle(getPermissionCreateBundle());
      permissionOp.setTypeName(getPermissionCreateName());
      permissionOp.run(monitor, workingCopyManager);
      m_outCreatePermission = permissionOp.getCreatedPermission();

    }
    if (getPermissionReadBundle() != null) {
      PermissionNewOperation permissionOp = new PermissionNewOperation(m_formatSource);
      permissionOp.setSharedBundle(getPermissionReadBundle());
      permissionOp.setTypeName(getPermissionReadName());
      permissionOp.run(monitor, workingCopyManager);
      m_outReadPermission = permissionOp.getCreatedPermission();

    }
    if (getPermissionUpdateBundle() != null) {
      PermissionNewOperation permissionOp = new PermissionNewOperation(m_formatSource);
      permissionOp.setSharedBundle(getPermissionUpdateBundle());
      permissionOp.setTypeName(getPermissionUpdateName());
      permissionOp.run(monitor, workingCopyManager);
      m_outUpdatePermission = permissionOp.getCreatedPermission();
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
      serviceOp.setImplementationBundle(getServiceImplementationBundle());
      serviceOp.setInterfaceBundle(getServiceInterfaceBundle());
      serviceOp.setServiceInterfaceName(getServiceInterfaceName());
      serviceOp.setServiceInterfacePackageName(getServiceInterfaceBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES));
      serviceOp.setServiceInterfaceSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.IService2));
      serviceOp.setServiceName(getServiceImplementationName());
      serviceOp.setServicePackageName(getServiceImplementationBundle().getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES));
      serviceOp.setServiceSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IService2, getServiceImplementationBundle().getJavaProject()));
      serviceOp.run(monitor, workingCopyManager);
      m_outProcessService = serviceOp.getCreatedServiceImplementation();
      m_outProcessServiceInterface = serviceOp.getCreatedServiceInterface();
      // fill service
      if (getOutFormData() != null) {
        ProcessServiceCreateMethodOperation processServiceFillOp = new ProcessServiceCreateMethodOperation();
        processServiceFillOp.setFormData(getOutFormData());
        processServiceFillOp.setCreateCreateMethod(true);
        processServiceFillOp.setCreateLoadMethod(true);
        processServiceFillOp.setCreatePrepareCreateMethod(true);
        processServiceFillOp.setCreateStoreMethod(true);
        processServiceFillOp.setCreatePermission(getOutCreatePermission());
        processServiceFillOp.setReadPermission(getOutReadPermission());
        processServiceFillOp.setUpdatePermission(getOutUpdatePermission());
        processServiceFillOp.setServiceImplementations(new IType[]{getOutProcessService()});
        processServiceFillOp.setServiceInterface(getOutProcessServiceInterface());
        processServiceFillOp.run(monitor, workingCopyManager);
      }
      if (getOutProcessService() != null && m_formatSource) {
        // format
        JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getOutProcessService(), true);
        formatOp.validate();
        formatOp.run(monitor, workingCopyManager);
      }
      if (getOutProcessServiceInterface() != null && m_formatSource) {
        // format
        JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getOutProcessServiceInterface(), true);
        formatOp.validate();
        formatOp.run(monitor, workingCopyManager);
      }
    }
    // fill form
    if (isCreateModifyHandler()) {
      FormHandlerNewOperation modifyHandlerOp = new FormHandlerNewOperation(getOutForm());
      modifyHandlerOp.setStartMethodSibling(formOp.getCreatedMainBoxGetter());
      modifyHandlerOp.setTypeName(SdkProperties.TYPE_NAME_MODIFY_HANDLER);
      modifyHandlerOp.run(monitor, workingCopyManager);
      m_outNewHandler = modifyHandlerOp.getCreatedHandler();
      if (getOutProcessServiceInterface() != null) {
        ModifyHandlerCreateMethodsOperation fillOp = new ModifyHandlerCreateMethodsOperation();
        fillOp.setCreateExecLoad(true);
        fillOp.setCreateExecStore(true);
        fillOp.setUpdatePermission(getOutUpdatePermission());
        fillOp.setFormData(getOutFormData());
        fillOp.setFormHandler(getOutNewHandler());
        fillOp.setServiceInterface(getOutProcessServiceInterface());
        fillOp.validate();
        fillOp.run(monitor, workingCopyManager);
      }
    }
    // fill form
    if (isCreateNewHandler()) {
      FormHandlerNewOperation newHandlerOp = new FormHandlerNewOperation(getOutForm());
      newHandlerOp.setStartMethodSibling(formOp.getCreatedMainBoxGetter());
      newHandlerOp.setTypeName(SdkProperties.TYPE_NAME_NEW_HANDLER);
      newHandlerOp.run(monitor, workingCopyManager);
      m_outNewHandler = newHandlerOp.getCreatedHandler();
      NewHandlerCreateMethodsOperation fillOp = new NewHandlerCreateMethodsOperation();
      fillOp.setCreateExecLoad(true);
      fillOp.setCreateExecStore(true);
      fillOp.setFormData(getOutFormData());
      fillOp.setFormHandler(getOutNewHandler());
      fillOp.setServiceInterface(getOutProcessServiceInterface());
      fillOp.run(monitor, workingCopyManager);
    }
    if (TypeUtility.exists(m_outFormData)) {
      FormDataUpdateOperation formDataUpdateOp = new FormDataUpdateOperation(m_outForm);
      formDataUpdateOp.run(monitor, workingCopyManager);
    }

    if (m_formatSource) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getOutForm(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }

  }

  public String getFormName() {
    return m_formName;
  }

  public void setFormName(String formName) {
    m_formName = formName;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public String getFormSuperTypeSignature() {
    return m_formSuperTypeSignature;
  }

  public void setFormSuperTypeSignature(String formSuperTypeSignature) {
    m_formSuperTypeSignature = formSuperTypeSignature;
  }

  /**
   * @return the formBundle
   */
  public IScoutBundle getFormBundle() {
    return m_formBundle;
  }

  /**
   * @param formBundle
   *          the formBundle to set
   */
  public void setFormBundle(IScoutBundle formBundle) {
    m_formBundle = formBundle;
  }

  /**
   * @return the clientServiceRegistryBundle
   */
  public IScoutBundle[] getClientServiceRegistryBundles() {
    return m_clientServiceRegistryBundles;
  }

  /**
   * @param clientServiceRegistryBundles
   *          the clientServiceRegistryBundle to set
   */
  public void setClientServiceRegistryBundles(IScoutBundle[] clientServiceRegistryBundles) {
    m_clientServiceRegistryBundles = clientServiceRegistryBundles;
  }

  /**
   * @return the formDataBundle
   */
  public IScoutBundle getFormDataBundle() {
    return m_formDataBundle;
  }

  /**
   * @param formDataBundle
   *          the formDataBundle to set
   */
  public void setFormDataBundle(IScoutBundle formDataBundle) {
    m_formDataBundle = formDataBundle;
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
   * @return the serverServiceRegistryBundles
   */
  public IScoutBundle[] getServerServiceRegistryBundles() {
    return m_serverServiceRegistryBundles;
  }

  /**
   * @param serverServiceRegistryBundles
   *          the serverServiceRegistryBundle to set
   */
  public void setServerServiceRegistryBundles(IScoutBundle[] serverServiceRegistryBundles) {
    m_serverServiceRegistryBundles = serverServiceRegistryBundles;
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

  public void setCreateIdProperty(boolean createIdProperty) {
    m_createIdProperty = createIdProperty;
  }

  public boolean isCreateIdProperty() {
    return m_createIdProperty;
  }

  public void setCreateButtonOk(boolean createButtonOk) {
    m_createButtonOk = createButtonOk;
  }

  public boolean isCreateButtonCancel() {
    return m_createButtonCancel;
  }

  public void setCreateButtonCancel(boolean createButtonCancel) {
    m_createButtonCancel = createButtonCancel;
  }

  public boolean isCreateButtonOk() {
    return m_createButtonOk;
  }

  public void setFormIdName(String formIdName) {
    m_formIdName = formIdName;
  }

  public String getFormIdName() {
    return m_formIdName;
  }

  /**
   * @return the outProcessService
   */
  public IType getOutProcessService() {
    return m_outProcessService;
  }

  /**
   * @return the outProcessServiceInterface
   */
  public IType getOutProcessServiceInterface() {
    return m_outProcessServiceInterface;
  }

  /**
   * @return the outFormData
   */
  public IType getOutFormData() {
    return m_outFormData;
  }

  /**
   * @return the outForm
   */
  public IType getOutForm() {
    return m_outForm;
  }

  /**
   * gets the mainbox that was created.
   * 
   * @return
   */
  public IType getOutMainBox() {
    return m_outMainBox;
  }

  /**
   * @return the outReadPermission
   */
  public IType getOutReadPermission() {
    return m_outReadPermission;
  }

  /**
   * @return the outCreatePermission
   */
  public IType getOutCreatePermission() {
    return m_outCreatePermission;
  }

  /**
   * @return the outUpdatePermission
   */
  public IType getOutUpdatePermission() {
    return m_outUpdatePermission;
  }

  /**
   * @return the outNewHandler
   */
  public IType getOutNewHandler() {
    return m_outNewHandler;
  }

  /**
   * @return the outModifyHandler
   */
  public IType getOutModifyHandler() {
    return m_outModifyHandler;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

}
