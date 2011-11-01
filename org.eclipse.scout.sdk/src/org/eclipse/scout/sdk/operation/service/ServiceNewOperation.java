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

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

/**
 * <h3>{@link ServiceNewOperation}</h3> To create a new service a service consists out of:
 * <ul>
 * <li><b>a service implementation</b> located in client or server bundles</li>
 * <li><b>service interface</b> used mainly for remote services can be an a shared, server or client bundle</li>
 * <li><b>proxy registrations</b> must be 0...n client bundles</li>
 * <li><b>service registrations</b> can be in client or server bundles.</li>
 * </ul>
 * In case the service implementation bundle is not set no service implementation will be created nor any service
 * registrations added.<br>
 * In case the service interface bundle is null no service proxy registrations will be created.
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 03.02.2010
 */
public class ServiceNewOperation implements IOperation {
  final IType iClientSession = ScoutSdk.getType(RuntimeClasses.IClientSession);
  final IType iServerSession = ScoutSdk.getType(RuntimeClasses.IServerSession);

  private String m_serviceName;
  private String m_servicePackageName;
  private String m_serviceInterfaceName;
  private String m_serviceInterfacePackageName;
  private String m_serviceInterfaceSuperTypeSignature;
  private String m_serviceSuperTypeSignature;
  private IScoutBundle m_interfaceBundle;
  private IScoutBundle m_implementationBundle;
  private final List<IScoutBundle> m_proxyRegistrationBundles = new ArrayList<IScoutBundle>();
  private final List<IScoutBundle> m_serviceRegistrationBundles = new ArrayList<IScoutBundle>();
  private IType m_createdServiceInterface;
  private IType m_createdServiceImplementation;

  @Override
  public String getOperationName() {
    return "create new service...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getInterfaceBundle() != null) {
      if (StringUtility.isNullOrEmpty(getServiceInterfaceName())) {
        throw new IllegalArgumentException("service interface name not set.");
      }
      if (StringUtility.isNullOrEmpty(getServiceInterfacePackageName())) {
        throw new IllegalArgumentException("service interface package name not set.");
      }
    }
    if (getImplementationBundle() != null) {
      if (StringUtility.isNullOrEmpty(getServiceName())) {
        throw new IllegalArgumentException("service name not set.");
      }
      if (StringUtility.isNullOrEmpty(getServicePackageName())) {
        throw new IllegalArgumentException("service package name not set.");
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (getInterfaceBundle() != null) {
      // create
      ScoutTypeNewOperation interfaceOp = new ScoutTypeNewOperation(getServiceInterfaceName(), getServiceInterfacePackageName(), getInterfaceBundle());
      interfaceOp.addInterfaceSignature(getServiceInterfaceSuperTypeSignature());
      interfaceOp.addTypeModifier(Flags.AccInterface);
      interfaceOp.run(monitor, workingCopyManager);
      IType createdInterface = interfaceOp.getCreatedType();
      workingCopyManager.register(createdInterface.getCompilationUnit(), monitor);
      // add to exported packages
      ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{createdInterface.getPackageFragment()}, true);
      manifestOp.run(monitor, workingCopyManager);
      m_createdServiceInterface = createdInterface;
      // register
      for (IScoutBundle cb : getProdyRegistrationBundles()) {
        ScoutUtility.registerServiceClass(cb.getProject(), IScoutBundle.CLIENT_EXTENSION_POINT_SERVICE_PROXIES, IScoutBundle.CLIENT_EXTENSION_ELEMENT_SERVICE_PROXY, getCreatedServiceInterface().getFullyQualifiedName(), null, RuntimeClasses.ClientProxyServiceFactory, monitor);
      }
    }
    if (getImplementationBundle() != null) {
      String intSig = Signature.createTypeSignature(RuntimeClasses.IService2, true);
      if (getCreatedServiceInterface() != null) {
        intSig = Signature.createTypeSignature(getCreatedServiceInterface().getFullyQualifiedName(), true);
      }
      ScoutTypeNewOperation newOp = new ScoutTypeNewOperation(getServiceName(), getServicePackageName(), getImplementationBundle());
      newOp.setSuperTypeSignature(getServiceSuperTypeSignature());
      newOp.addInterfaceSignature(intSig);
      newOp.run(monitor, workingCopyManager);
      workingCopyManager.register(newOp.getCreatedType().getCompilationUnit(), monitor);
      // add to exported packages
      ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{newOp.getCreatedType().getPackageFragment()}, true);
      manifestOp.run(monitor, workingCopyManager);
      m_createdServiceImplementation = newOp.getCreatedType();
      // register
      for (IScoutBundle sb : getServiceRegistrationBundles()) {
        IType sessionType = null;
        String serviceFactory = null;
        ITypeFilter sessionFilter = TypeFilters.getMultiTypeFilter(
            TypeFilters.getTypesOnClasspath(sb.getJavaProject()),
            TypeFilters.getInWorkspaceFilter(),
            TypeFilters.getClassFilter()
            );
        if (sb.getType() == IScoutBundle.BUNDLE_CLIENT) {
          serviceFactory = RuntimeClasses.ClientServiceFactory;
          // find client session
          ICachedTypeHierarchy clientSessionHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iClientSession);
          IType[] clientSessions = clientSessionHierarchy.getAllSubtypes(iClientSession, sessionFilter, null);
          if (clientSessions.length > 0) {
            sessionType = clientSessions[0];
          }
        }
        else if (sb.getType() == IScoutBundle.BUNDLE_SERVER) {
          serviceFactory = RuntimeClasses.ServerServiceFactory;
          // find server session
          ICachedTypeHierarchy serverSessionHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iServerSession);
          IType[] serverSessions = serverSessionHierarchy.getAllSubtypes(iServerSession, sessionFilter, null);
          if (serverSessions.length > 0) {
            sessionType = serverSessions[0];
          }
        }
        else if (sb.getType() == IScoutBundle.BUNDLE_SHARED) {
          sessionType = null;
          serviceFactory = RuntimeClasses.DefaultServiceFactory;
        }

        ScoutUtility.registerServiceClass(sb.getProject(), IScoutBundle.EXTENSION_POINT_SERVICES,
            IScoutBundle.EXTENSION_ELEMENT_SERVICE, getCreatedServiceImplementation().getFullyQualifiedName(),
            sessionType == null ? null : sessionType.getFullyQualifiedName(), serviceFactory, monitor);
      }
    }
  }

  /**
   * @return the createdServiceInterface
   */
  public IType getCreatedServiceInterface() {
    return m_createdServiceInterface;
  }

  public void setCreatedServiceInterface(IType t) {
    m_createdServiceInterface = t;
  }

  /**
   * @return the createdServiceImplementation
   */
  public IType getCreatedServiceImplementation() {
    return m_createdServiceImplementation;
  }

  /**
   * @return the serviceName
   */
  public String getServiceName() {
    return m_serviceName;
  }

  /**
   * @param serviceName
   *          the serviceName to set
   */
  public void setServiceName(String serviceName) {
    m_serviceName = serviceName;
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
   * @return the servicePackageName
   */
  public String getServicePackageName() {
    return m_servicePackageName;
  }

  /**
   * @param servicePackageName
   *          the servicePackageName to set
   */
  public void setServicePackageName(String servicePackageName) {
    m_servicePackageName = servicePackageName;
  }

  /**
   * @return the serviceInterfacePackageName
   */
  public String getServiceInterfacePackageName() {
    return m_serviceInterfacePackageName;
  }

  /**
   * @param serviceInterfacePackageName
   *          the serviceInterfacePackageName to set
   */
  public void setServiceInterfacePackageName(String serviceInterfacePackageName) {
    m_serviceInterfacePackageName = serviceInterfacePackageName;
  }

  /**
   * @return the serviceInterfaceSuperTypeSignature
   */
  public String getServiceInterfaceSuperTypeSignature() {
    return m_serviceInterfaceSuperTypeSignature;
  }

  /**
   * @param serviceInterfaceSuperTypeSignature
   *          the serviceInterfaceSuperTypeSignature to set
   */
  public void setServiceInterfaceSuperTypeSignature(String serviceInterfaceSuperTypeSignature) {
    m_serviceInterfaceSuperTypeSignature = serviceInterfaceSuperTypeSignature;
  }

  /**
   * @return the serviceSuperTypeSignature
   */
  public String getServiceSuperTypeSignature() {
    return m_serviceSuperTypeSignature;
  }

  /**
   * @param serviceSuperTypeSignature
   *          the serviceSuperTypeSignature to set
   */
  public void setServiceSuperTypeSignature(String serviceSuperTypeSignature) {
    m_serviceSuperTypeSignature = serviceSuperTypeSignature;
  }

  /**
   * @return the interfaceBundle
   */
  public IScoutBundle getInterfaceBundle() {
    return m_interfaceBundle;
  }

  /**
   * @param interfaceBundle
   *          the interfaceBundle to set
   */
  public void setInterfaceBundle(IScoutBundle interfaceBundle) {
    m_interfaceBundle = interfaceBundle;
  }

  /**
   * @return the implementationBundle
   */
  public IScoutBundle getImplementationBundle() {
    return m_implementationBundle;
  }

  /**
   * @param implementationBundle
   *          the implementationBundle to set
   */
  public void setImplementationBundle(IScoutBundle implementationBundle) {
    m_implementationBundle = implementationBundle;
  }

  public boolean addProxyRegistrationBundle(IScoutBundle clientBundle) {
    return m_proxyRegistrationBundles.add(clientBundle);
  }

  public boolean removeProxyRegistrationBundle(IScoutBundle clientBundle) {
    return m_proxyRegistrationBundles.remove(clientBundle);
  }

  public IScoutBundle[] getProdyRegistrationBundles() {
    return m_proxyRegistrationBundles.toArray(new IScoutBundle[m_proxyRegistrationBundles.size()]);
  }

  public boolean addServiceRegistrationBundle(IScoutBundle serverBundle) {
    return m_serviceRegistrationBundles.add(serverBundle);
  }

  public boolean removeServiceRegistrationBundle(IScoutBundle o) {
    return m_serviceRegistrationBundles.remove(o);
  }

  public IScoutBundle[] getServiceRegistrationBundles() {
    return m_serviceRegistrationBundles.toArray(new IScoutBundle[m_serviceRegistrationBundles.size()]);
  }

}
