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
package org.eclipse.scout.sdk.operation.service;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;

/**
 * <h3>RemoteServiceNewOperation</h3> this operation creates a service interface in the serviceInterfaceBundle and an
 * implementation in the serviceImplementationBundle.
 * The service implementation is registered in the serviceRegistrationBundle and its interface in the
 * serviceProxyRegistrationBundle.
 * 
 * @deprecated use {@link ServiceNewOperation} instead.
 */
@Deprecated
public class RemoteServiceNewOperation implements IOperation {

  final IType iServerSession = ScoutSdk.getType(RuntimeClasses.IServerSession);
  final IType iClientSession = ScoutSdk.getType(RuntimeClasses.IClientSession);

  private IScoutBundle m_interfaceBundle;
  private IScoutBundle m_implementationBundle;
  private IScoutBundle m_serverRegistrationBundle;
  private IScoutBundle m_clientRegistrationBundle;
  private String m_interfacePackageName;
  private String m_implementationPackageName;
  private String m_serviceName;
  private String m_serviceSuperTypeSignature;
  private String m_serviceInterfaceSuperTypeSignature;
  private boolean m_createInterface = true;
  private boolean m_createImplementation = true;
  private IType m_createdServiceInterface;
  private IType m_createdServiceImplementation;

  @Override
  public String getOperationName() {
    return "create remote service '" + m_serviceName + "'.";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getServiceName())) {
      throw new IllegalArgumentException("no service name specified!");
    }
    if (isCreateInterface()) {
      if (getInterfaceBundle() == null) {
        throw new IllegalArgumentException("host bundle of service interface missing (serviceInterfaceBundle).");
      }
      if (StringUtility.isNullOrEmpty(getInterfacePackageName())) {
        throw new IllegalArgumentException("package of service interface missing (serviceInterfacePackageName).");
      }
    }
    if (isCreateImplementation()) {
      if (getImplementationBundle() == null) {
        throw new IllegalArgumentException("host bundle of service implementation missing (serviceImplementationBundle).");
      }
      if (StringUtility.isNullOrEmpty(getImplementationPackageName())) {
        throw new IllegalArgumentException("package of service implementation missing (serviceImplementationPackageName).");
      }
    }

  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (isCreateInterface()) {
      createInterface(monitor, workingCopyManager);
    }

    if (isCreateImplementation()) {
      createImplementation(monitor, workingCopyManager);
    }

    // find best match server session
    IScoutBundle[] serverBundles = getServerRegistrationBundle().getRequiredBundles(ScoutBundleFilters.getServerFilter(), true);
    IType serverSession = null;
    for (IScoutBundle b : serverBundles) {
      IType[] serverSessions = ScoutSdk.getPrimaryTypeHierarchy(iServerSession).getAllSubtypes(iServerSession, TypeFilters.getClassesInProject(b.getJavaProject()), null);
      if (serverSessions.length == 1) {
        serverSession = serverSessions[0];
        break;
      }
      else if (serverSessions.length > 1) {
        ScoutSdk.logWarning("bundle '" + b.getBundleName() + "' has more than one <? implements IServerSession>!");
        serverSession = serverSessions[0];
      }
    }
    if (serverSession != null) {
      ScoutUtility.registerServiceClass(getServerRegistrationBundle().getProject(), IScoutBundle.EXTENSION_POINT_SERVICES, IScoutBundle.EXTENSION_ELEMENT_SERVICE, getCreatedServiceImplementation().getFullyQualifiedName(), serverSession.getFullyQualifiedName(), RuntimeClasses.ServerServiceFactory, monitor);
      if (getCreatedServiceInterface() != null) {
        ScoutUtility.registerServiceClass(getClientRegistrationBundle().getProject(), IScoutBundle.CLIENT_EXTENSION_POINT_SERVICE_PROXIES, IScoutBundle.CLIENT_EXTENSION_ELEMENT_SERVICE_PROXY, getCreatedServiceInterface().getFullyQualifiedName(), null, RuntimeClasses.ClientProxyServiceFactory, monitor);
      }
    }
  }

  protected IType createInterface(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    ScoutTypeNewOperation interfaceOp = new ScoutTypeNewOperation("I" + getServiceName(), getInterfacePackageName(), getInterfaceBundle());
    interfaceOp.addInterfaceSignature(getServiceInterfaceSuperTypeSignature());
    interfaceOp.addTypeModifier(Flags.AccInterface);
    interfaceOp.run(monitor, workingCopyManager);
    IType createdInterface = interfaceOp.getCreatedType();
    workingCopyManager.register(createdInterface.getCompilationUnit(), monitor);
    // add to exported packages
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{createdInterface.getPackageFragment()}, true);
    manifestOp.run(monitor, workingCopyManager);
    m_createdServiceInterface = createdInterface;
    return m_createdServiceInterface;
  }

  protected IType createImplementation(IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    String intSig = Signature.createTypeSignature(RuntimeClasses.IService, true);
    if (getCreatedServiceInterface() != null) {
      intSig = Signature.createTypeSignature(getCreatedServiceInterface().getFullyQualifiedName(), true);
    }
    ScoutTypeNewOperation newOp = new ScoutTypeNewOperation(getServiceName(), getImplementationPackageName(), getImplementationBundle());
    newOp.setSuperTypeSignature(getServiceSuperTypeSignature());
    newOp.addInterfaceSignature(intSig);
    newOp.run(monitor, manager);
    manager.register(newOp.getCreatedType().getCompilationUnit(), monitor);
    // add to exported packages
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{newOp.getCreatedType().getPackageFragment()}, true);
    manifestOp.run(monitor, manager);
    m_createdServiceImplementation = newOp.getCreatedType();
    return m_createdServiceImplementation;

  }

  public IScoutBundle getInterfaceBundle() {
    return m_interfaceBundle;
  }

  public void setInterfaceBundle(IScoutBundle serviceInterfaceBundle) {
    m_interfaceBundle = serviceInterfaceBundle;
  }

  public IScoutBundle getImplementationBundle() {
    return m_implementationBundle;
  }

  public void setImplementationBundle(IScoutBundle serviceImplementationBundle) {
    m_implementationBundle = serviceImplementationBundle;
  }

  public IScoutBundle getServerRegistrationBundle() {
    return m_serverRegistrationBundle;
  }

  public void setServerRegistrationBundle(IScoutBundle serviceRegistrationBundle) {
    m_serverRegistrationBundle = serviceRegistrationBundle;
  }

  public IScoutBundle getClientRegistrationBundle() {
    return m_clientRegistrationBundle;
  }

  public void setClientRegistrationBundle(IScoutBundle serviceProxyRegistrationBundle) {
    m_clientRegistrationBundle = serviceProxyRegistrationBundle;
  }

  public String getInterfacePackageName() {
    return m_interfacePackageName;
  }

  public void setInterfacePackageName(String serviceInterfacePackageName) {
    m_interfacePackageName = serviceInterfacePackageName;
  }

  public String getImplementationPackageName() {
    return m_implementationPackageName;
  }

  public void setImplementationPackageName(String serviceImplementationPackageName) {
    m_implementationPackageName = serviceImplementationPackageName;
  }

  public String getServiceName() {
    return m_serviceName;
  }

  public void setServiceName(String serviceName) {
    m_serviceName = serviceName;
  }

  public String getServiceSuperTypeSignature() {
    return m_serviceSuperTypeSignature;
  }

  public void setServiceSuperTypeSignature(String serviceSuperTypeSignature) {
    m_serviceSuperTypeSignature = serviceSuperTypeSignature;
  }

  public String getServiceInterfaceSuperTypeSignature() {
    return m_serviceInterfaceSuperTypeSignature;
  }

  public void setServiceInterfaceSuperTypeSignature(String serviceInterfaceSuperTypeSignature) {
    m_serviceInterfaceSuperTypeSignature = serviceInterfaceSuperTypeSignature;
  }

  public boolean isCreateInterface() {
    return m_createInterface;
  }

  public void setCreateInterface(boolean createInterface) {
    m_createInterface = createInterface;
  }

  public void setCreateImplementation(boolean createImplementation) {
    m_createImplementation = createImplementation;
  }

  public boolean isCreateImplementation() {
    return m_createImplementation;
  }

  public IType getCreatedServiceInterface() {
    return m_createdServiceInterface;
  }

  public IType getCreatedServiceImplementation() {
    return m_createdServiceImplementation;
  }

}
