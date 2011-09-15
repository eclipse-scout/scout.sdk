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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.field.FieldCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.service.LookupServiceNewOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class LookupCallNewOperation implements IOperation {
  // in members
  private String m_lookupCallName;
  private String m_serviceSuperTypeSignature;
  private IScoutBundle m_bundle;
  private IScoutBundle m_interfaceRegistrationBundle;
  private IScoutBundle m_serviceInterfaceBundle;
  private IScoutBundle m_implementationRegistrationBundle;
  private IScoutBundle m_serviceImplementationBundle;
  private IType m_lookupService;
  private boolean m_formatSource;
  //out members
  private IType m_outLookupCall;

  @Override
  public String getOperationName() {
    return null;
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    String namePrefix = getLookupCallName();
    namePrefix = namePrefix.replaceAll(ScoutIdeProperties.SUFFIX_LOOKUP_CALL + "$", "");
    // service
    IType lookupServiceInterface = getLookupService();
    if (lookupServiceInterface == null) {
      if (!StringUtility.isNullOrEmpty(getServiceSuperTypeSignature())) {
        LookupServiceNewOperation serviceOp = new LookupServiceNewOperation();
        serviceOp.addProxyRegistrationBundle(getInterfaceRegistrationBundle());
        serviceOp.setImplementationBundle(getServiceImplementationBundle());
        serviceOp.setServiceName(namePrefix + ScoutIdeProperties.SUFFIX_LOOKUP_SERVICE);
        serviceOp.setInterfaceBundle(getServiceInterfaceBundle());
        serviceOp.setServiceInterfaceName("I" + namePrefix + ScoutIdeProperties.SUFFIX_LOOKUP_SERVICE);
        serviceOp.setServiceInterfaceSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.ILookupService, true));
        serviceOp.setServiceSuperTypeSignature(getServiceSuperTypeSignature());
        serviceOp.addServiceRegistrationBundle(getImplementationRegistrationBundle());
        serviceOp.validate();
        serviceOp.run(monitor, workingCopyManager);
        lookupServiceInterface = serviceOp.getCreatedServiceInterface();
      }
    }
    // lookup call
    ScoutTypeNewOperation lookupCallOp = new ScoutTypeNewOperation(getLookupCallName(), getBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_LOOKUP), getBundle());
    lookupCallOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.LookupCall, true));
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
          String serviceTypeRef = validator.getSimpleTypeRef(Signature.createTypeSignature(finalService.getFullyQualifiedName(), true));
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

}
