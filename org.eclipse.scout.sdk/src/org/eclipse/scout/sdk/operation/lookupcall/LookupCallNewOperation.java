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
package org.eclipse.scout.sdk.operation.lookupcall;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.field.FieldCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.service.LookupServiceNewOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class LookupCallNewOperation implements IOperation {
  // in members
  private String m_lookupCallName;
  private String m_serviceSuperTypeSignature;
  private IScoutBundle m_bundle;
  private String m_lookupCallPackageName;
  private IScoutBundle m_interfaceRegistrationBundle;
  private String m_serviceInterfacePackageName;
  private IScoutBundle m_serviceInterfaceBundle;
  private IScoutBundle m_implementationRegistrationBundle;
  private String m_serviceImplementationPackage;
  private IScoutBundle m_serviceImplementationBundle;
  private IType m_lookupService;
  private boolean m_formatSource;

  //out members
  private IType m_outLookupCall;
  private IType m_outLookupService;
  private IType m_outLookupServiceInterface;

  @Override
  public String getOperationName() {
    return null;
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    String namePrefix = getLookupCallName();
    namePrefix = namePrefix.replaceAll(SdkProperties.SUFFIX_LOOKUP_CALL + "$", "");
    // service
    IType lookupServiceInterface = getLookupService();
    if (lookupServiceInterface == null) {
      if (!StringUtility.isNullOrEmpty(getServiceSuperTypeSignature())) {
        LookupServiceNewOperation serviceOp = new LookupServiceNewOperation();
        serviceOp.addProxyRegistrationBundle(getInterfaceRegistrationBundle());
        serviceOp.setImplementationBundle(getServiceImplementationBundle());
        serviceOp.setServiceName(namePrefix + SdkProperties.SUFFIX_LOOKUP_SERVICE);
        serviceOp.setServicePackageName(getServiceImplementationPackage());
        serviceOp.setInterfaceBundle(getServiceInterfaceBundle());
        serviceOp.setServiceInterfaceName("I" + namePrefix + SdkProperties.SUFFIX_LOOKUP_SERVICE);
        serviceOp.setServiceInterfaceSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.ILookupService));
        serviceOp.setServiceInterfacePackageName(getServiceInterfacePackageName());
        serviceOp.setServiceSuperTypeSignature(getServiceSuperTypeSignature());
        serviceOp.addServiceRegistrationBundle(getImplementationRegistrationBundle());
        serviceOp.validate();
        serviceOp.run(monitor, workingCopyManager);
        lookupServiceInterface = serviceOp.getCreatedServiceInterface();
        m_outLookupService = serviceOp.getCreatedServiceImplementation();
      }
    }
    m_outLookupServiceInterface = lookupServiceInterface;
    // lookup call
    ScoutTypeNewOperation lookupCallOp = new ScoutTypeNewOperation(getLookupCallName(), getLookupCallPackageName(), getBundle());
    lookupCallOp.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.LookupCall));
    lookupCallOp.validate();
    lookupCallOp.run(monitor, workingCopyManager);
    m_outLookupCall = lookupCallOp.getCreatedType();
    FieldCreateOperation serialVersionUidOp = new FieldCreateOperation(getOutLookupCall(), "serialVersionUID", false);
    serialVersionUidOp.setFlags(Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    serialVersionUidOp.setSignature(Signature.SIG_LONG);
    serialVersionUidOp.setSimpleInitValue("1L");
    serialVersionUidOp.validate();
    serialVersionUidOp.run(monitor, workingCopyManager);
    if (lookupServiceInterface != null) {
      final IType finalService = lookupServiceInterface;
      MethodOverrideOperation lookupServiceMethodOp = new MethodOverrideOperation(m_outLookupCall, "getConfiguredService", false) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          String serviceTypeRef = validator.getTypeName(SignatureCache.createTypeSignature(finalService.getFullyQualifiedName()));
          return "  return " + serviceTypeRef + ".class;";
        }
      };
      lookupServiceMethodOp.validate();
      lookupServiceMethodOp.run(monitor, workingCopyManager);
    }
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  public String getLookupCallName() {
    return m_lookupCallName;
  }

  public void setLookupCallName(String lookupCallName) {
    m_lookupCallName = lookupCallName;
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public IScoutBundle getInterfaceRegistrationBundle() {
    return m_interfaceRegistrationBundle;
  }

  public void setInterfaceRegistrationBundle(IScoutBundle interfaceRegistrationBundle) {
    m_interfaceRegistrationBundle = interfaceRegistrationBundle;
  }

  public IScoutBundle getServiceInterfaceBundle() {
    return m_serviceInterfaceBundle;
  }

  public void setServiceInterfaceBundle(IScoutBundle serviceInterfaceBundle) {
    m_serviceInterfaceBundle = serviceInterfaceBundle;
  }

  public void setServiceSuperTypeSignature(String serviceSuperTypeSignature) {
    m_serviceSuperTypeSignature = serviceSuperTypeSignature;
  }

  public String getServiceSuperTypeSignature() {
    return m_serviceSuperTypeSignature;
  }

  public IScoutBundle getImplementationRegistrationBundle() {
    return m_implementationRegistrationBundle;
  }

  public void setImplementationRegistrationBundle(IScoutBundle implementationRegistrationBundle) {
    m_implementationRegistrationBundle = implementationRegistrationBundle;
  }

  public IScoutBundle getServiceImplementationBundle() {
    return m_serviceImplementationBundle;
  }

  public void setServiceImplementationBundle(IScoutBundle serviceImplementationBundle) {
    m_serviceImplementationBundle = serviceImplementationBundle;
  }

  public void setLookupService(IType lookupService) {
    m_lookupService = lookupService;
  }

  public IType getLookupService() {
    return m_lookupService;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public IType getOutLookupCall() {
    return m_outLookupCall;
  }

  public IType getOutLookupService() {
    return m_outLookupService;
  }

  public IType getOutLookupServiceInterface() {
    return m_outLookupServiceInterface;
  }

  public String getServiceImplementationPackage() {
    return m_serviceImplementationPackage;
  }

  public void setServiceImplementationPackage(String serviceImplementationPackage) {
    m_serviceImplementationPackage = serviceImplementationPackage;
  }

  public String getServiceInterfacePackageName() {
    return m_serviceInterfacePackageName;
  }

  public void setServiceInterfacePackageName(String serviceInterfacePackageName) {
    m_serviceInterfacePackageName = serviceInterfacePackageName;
  }

  public String getLookupCallPackageName() {
    return m_lookupCallPackageName;
  }

  public void setLookupCallPackageName(String lookupCallPackageName) {
    m_lookupCallPackageName = lookupCallPackageName;
  }
}
