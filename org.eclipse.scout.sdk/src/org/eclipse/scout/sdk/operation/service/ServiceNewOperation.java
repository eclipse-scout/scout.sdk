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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

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

  private IJavaProject m_interfaceProject;
  private String m_interfacePackageName;
  private TypeSourceBuilder m_interfaceBuilder;
  private IJavaProject m_implementationProject;
  private String m_implementationPackageName;
  private TypeSourceBuilder m_implementationBuilder;
  private List<IJavaProject> m_proxyRegistrationProjects;
  private List<IJavaProject> m_serviceRegistrationProjects;

  private IType m_createdServiceInterface;
  private IType m_createdServiceImplementation;
  private boolean m_formatSource;

  public ServiceNewOperation(String serviceInterfaceName, String serviceName) {
    m_formatSource = true;
    m_implementationBuilder = new TypeSourceBuilder(serviceName);
    m_implementationBuilder.setFlags(Flags.AccPublic);
    m_interfaceBuilder = new TypeSourceBuilder(serviceInterfaceName);
    m_interfaceBuilder.setFlags(Flags.AccPublic | Flags.AccInterface);

    m_proxyRegistrationProjects = new ArrayList<IJavaProject>();
    m_serviceRegistrationProjects = new ArrayList<IJavaProject>();
  }

  @Override
  public String getOperationName() {
    return "create new service...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getInterfaceProject() != null || getInterfacePackageName() != null) {
      if (getInterfaceProject() == null || getInterfacePackageName() == null) {
        throw new IllegalArgumentException("interface project and packagename must be both set or null. To avoid the interface creation set both members to null.");
      }
      getInterfaceSourceBuilder().validate();
    }
    if (getImplementationProject() != null || getImplementationPackageName() != null) {
      if (getImplementationProject() == null || getImplementationPackageName() == null) {
        throw new IllegalArgumentException("implementation project and packagename must be both set or null. To avoid the interface creation set both members to null.");
      }
      getImplementationSourceBuilder().validate();
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (getInterfaceProject() != null) {
      // create interface
      List<String> interfaceSignatures = getInterfaceSourceBuilder().getInterfaceSignatures();
      String service2Signature = SignatureCache.createTypeSignature(RuntimeClasses.IService2);
      if (interfaceSignatures.isEmpty()) {
        interfaceSignatures.add(service2Signature);
      }
      PrimaryTypeNewOperation interfaceOp = new PrimaryTypeNewOperation(getInterfaceSourceBuilder(), getInterfacePackageName(), getInterfaceProject());
      interfaceOp.setPackageExportPolicy(ExportPolicy.AddPackage);

      interfaceOp.setFormatSource(isFormatSource());
      interfaceOp.validate();
      interfaceOp.run(monitor, workingCopyManager);
      m_createdServiceInterface = interfaceOp.getCreatedType();
      workingCopyManager.register(m_createdServiceInterface.getCompilationUnit(), monitor);
      // register
      for (IJavaProject cb : getProxyRegistrationProjects()) {
        ScoutUtility.registerServiceClass(cb.getProject(), IRuntimeClasses.EXTENSION_POINT_CLIENT_SERVICE_PROXIES, IRuntimeClasses.EXTENSION_ELEMENT_CLIENT_SERVICE_PROXY, getCreatedServiceInterface().getFullyQualifiedName(), null, RuntimeClasses.ClientProxyServiceFactory, monitor);
      }
    }
    if (getImplementationProject() != null) {
      if (getCreatedServiceInterface() != null) {
        getImplementationSourceBuilder().addInterfaceSignature(Signature.createTypeSignature(getCreatedServiceInterface().getFullyQualifiedName(), true));
      }
      if (StringUtility.isNullOrEmpty(getImplementationSourceBuilder().getSuperTypeSignature())) {
        getImplementationSourceBuilder().setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IService2, getImplementationProject()));
      }
      PrimaryTypeNewOperation implementationOp = new PrimaryTypeNewOperation(getImplementationSourceBuilder(), getImplementationPackageName(), getImplementationProject());
      implementationOp.setFormatSource(isFormatSource());
      implementationOp.validate();
      implementationOp.run(monitor, workingCopyManager);
      m_createdServiceImplementation = implementationOp.getCreatedType();
      workingCopyManager.register(m_createdServiceImplementation.getCompilationUnit(), monitor);

      // register services
      for (IJavaProject sb : getServiceRegistrationProjects()) {
        IType sessionType = null;
        String serviceFactory = null;
        ITypeFilter sessionFilter = TypeFilters.getMultiTypeFilter(
            TypeFilters.getTypesOnClasspath(sb.getJavaProject()),
            TypeFilters.getClassFilter()
            );
        String projectType = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(sb).getType();
        if (projectType.equals(IScoutBundle.TYPE_CLIENT)) {
          serviceFactory = RuntimeClasses.ClientServiceFactory;
          // find client session
          IType iClientSession = TypeUtility.getType(RuntimeClasses.IClientSession);
          ICachedTypeHierarchy clientSessionHierarchy = TypeUtility.getPrimaryTypeHierarchy(iClientSession);
          IType[] clientSessions = clientSessionHierarchy.getAllSubtypes(iClientSession, sessionFilter, null);
          if (clientSessions.length > 0) {
            sessionType = clientSessions[0];
          }
        }
        else if (projectType.equals(IScoutBundle.TYPE_SERVER)) {
          serviceFactory = RuntimeClasses.ServerServiceFactory;
          // find server session
          IType iServerSession = TypeUtility.getType(RuntimeClasses.IServerSession);
          ICachedTypeHierarchy serverSessionHierarchy = TypeUtility.getPrimaryTypeHierarchy(iServerSession);
          IType[] serverSessions = serverSessionHierarchy.getAllSubtypes(iServerSession, sessionFilter, null);
          if (serverSessions.length > 0) {
            sessionType = serverSessions[0];
          }
        }
        else if (projectType.equals(IScoutBundle.TYPE_SHARED)) {
          sessionType = null;
          serviceFactory = RuntimeClasses.DefaultServiceFactory;
        }

        ScoutUtility.registerServiceClass(sb.getProject(), IRuntimeClasses.EXTENSION_POINT_SERVICES,
            IRuntimeClasses.EXTENSION_ELEMENT_SERVICE, getCreatedServiceImplementation().getFullyQualifiedName(),
            sessionType == null ? null : sessionType.getFullyQualifiedName(), serviceFactory, monitor);
      }
    }
  }

  public void setInterfaceProject(IJavaProject interfaceProject) {
    m_interfaceProject = interfaceProject;
  }

  public IJavaProject getInterfaceProject() {
    return m_interfaceProject;
  }

  public void setInterfacePackageName(String interfacePackageName) {
    m_interfacePackageName = interfacePackageName;
  }

  public String getInterfacePackageName() {
    return m_interfacePackageName;
  }

  public TypeSourceBuilder getInterfaceSourceBuilder() {
    return m_interfaceBuilder;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractJavaElementSourceBuilder#getElementName()
   */
  public String getInterfaceName() {
    return m_interfaceBuilder.getElementName();
  }

  /**
   * @param interfaceSignature
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addInterfaceSignature(java.lang.String)
   */
  public void addInterfaceInterfaceSignature(String interfaceSignature) {
    m_interfaceBuilder.addInterfaceSignature(interfaceSignature);
  }

  /**
   * @param interfaceSignature
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#removeInterfaceSignature(java.lang.String)
   */
  public boolean removeInterfaceInterfaceSignature(String interfaceSignature) {
    return m_interfaceBuilder.removeInterfaceSignature(interfaceSignature);
  }

  /**
   * @param interfaceSignatures
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#setInterfaceSignatures(java.lang.String[])
   */
  public void setInterfaceInterfaceSignatures(String[] interfaceSignatures) {
    m_interfaceBuilder.setInterfaceSignatures(interfaceSignatures);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getInterfaceSignatures()
   */
  public List<String> getInterfaceInterfaceSignatures() {
    return m_interfaceBuilder.getInterfaceSignatures();
  }

  /**
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addFieldSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder)
   */
  public void addInterfaceFieldSourceBuilder(IFieldSourceBuilder builder) {
    m_interfaceBuilder.addFieldSourceBuilder(builder);
  }

  /**
   * @param builder
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#removeFieldSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder)
   */
  public boolean removeInterfaceFieldSourceBuilder(IFieldSourceBuilder builder) {
    return m_interfaceBuilder.removeFieldSourceBuilder(builder);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getFieldSourceBuilders()
   */
  public List<IFieldSourceBuilder> getInterfaceFieldSourceBuilders() {
    return m_interfaceBuilder.getFieldSourceBuilders();
  }

  /**
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addMethodSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder)
   */
  public void addInterfaceMethodSourceBuilder(IMethodSourceBuilder builder) {
    m_interfaceBuilder.addMethodSourceBuilder(builder);
  }

  /**
   * @param sortKey
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addSortedMethodSourceBuilder(org.eclipse.scout.commons.CompositeObject,
   *      org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder)
   */
  public void addInterfaceSortedMethodSourceBuilder(CompositeObject sortKey, IMethodSourceBuilder builder) {
    m_interfaceBuilder.addSortedMethodSourceBuilder(sortKey, builder);
  }

  /**
   * @param builder
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#removeMethodSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder)
   */
  public boolean removeInterfaceMethodSourceBuilder(IMethodSourceBuilder builder) {
    return m_interfaceBuilder.removeMethodSourceBuilder(builder);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getMethodSourceBuilders()
   */
  public List<IMethodSourceBuilder> getInterfaceMethodSourceBuilders() {
    return m_interfaceBuilder.getMethodSourceBuilders();
  }

  public void setImplementationProject(IJavaProject implementationProject) {
    m_implementationProject = implementationProject;
  }

  public IJavaProject getImplementationProject() {
    return m_implementationProject;
  }

  public void setImplementationPackageName(String implementationPackageName) {
    m_implementationPackageName = implementationPackageName;
  }

  public String getImplementationPackageName() {
    return m_implementationPackageName;
  }

  public TypeSourceBuilder getImplementationSourceBuilder() {
    return m_implementationBuilder;
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractJavaElementSourceBuilder#getElementName()
   */
  public String getImplementationName() {
    return m_implementationBuilder.getElementName();
  }

  /**
   * @param superTypeSignature
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#setSuperTypeSignature(java.lang.String)
   */
  public void setImplementationSuperTypeSignature(String superTypeSignature) {
    m_implementationBuilder.setSuperTypeSignature(superTypeSignature);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getSuperTypeSignature()
   */
  public String getImplementationSuperTypeSignature() {
    return m_implementationBuilder.getSuperTypeSignature();
  }

  /**
   * @param interfaceSignature
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addInterfaceSignature(java.lang.String)
   */
  public void addImplementationInterfaceSignature(String interfaceSignature) {
    m_implementationBuilder.addInterfaceSignature(interfaceSignature);
  }

  /**
   * @param interfaceSignature
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#removeInterfaceSignature(java.lang.String)
   */
  public boolean removeImplementationInterfaceSignature(String interfaceSignature) {
    return m_implementationBuilder.removeInterfaceSignature(interfaceSignature);
  }

  /**
   * @param interfaceSignatures
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#setInterfaceSignatures(java.lang.String[])
   */
  public void setImplementationInterfaceSignatures(String[] interfaceSignatures) {
    m_implementationBuilder.setInterfaceSignatures(interfaceSignatures);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getInterfaceSignatures()
   */
  public List<String> getImplementationInterfaceSignatures() {
    return m_implementationBuilder.getInterfaceSignatures();
  }

  /**
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addFieldSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder)
   */
  public void addImplementationFieldSourceBuilder(IFieldSourceBuilder builder) {
    m_implementationBuilder.addFieldSourceBuilder(builder);
  }

  /**
   * @param sortKey
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addSortedFieldSourceBuilder(org.eclipse.scout.commons.CompositeObject,
   *      org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder)
   */
  public void addImplementationSortedFieldSourceBuilder(CompositeObject sortKey, IFieldSourceBuilder builder) {
    m_implementationBuilder.addSortedFieldSourceBuilder(sortKey, builder);
  }

  /**
   * @param builder
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#removeFieldSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder)
   */
  public boolean removeImplementationFieldSourceBuilder(IFieldSourceBuilder builder) {
    return m_implementationBuilder.removeFieldSourceBuilder(builder);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getFieldSourceBuilders()
   */
  public List<IFieldSourceBuilder> getImplementationFieldSourceBuilders() {
    return m_implementationBuilder.getFieldSourceBuilders();
  }

  /**
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addMethodSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder)
   */
  public void addImplementationMethodSourceBuilder(IMethodSourceBuilder builder) {
    m_implementationBuilder.addMethodSourceBuilder(builder);
  }

  /**
   * @param sortKey
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addSortedMethodSourceBuilder(org.eclipse.scout.commons.CompositeObject,
   *      org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder)
   */
  public void addImplementationSortedMethodSourceBuilder(CompositeObject sortKey, IMethodSourceBuilder builder) {
    m_implementationBuilder.addSortedMethodSourceBuilder(sortKey, builder);
  }

  /**
   * @param builder
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#removeMethodSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder)
   */
  public boolean removeImplementationMethodSourceBuilder(IMethodSourceBuilder builder) {
    return m_implementationBuilder.removeMethodSourceBuilder(builder);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getMethodSourceBuilders()
   */
  public List<IMethodSourceBuilder> getImplementationMethodSourceBuilders() {
    return m_implementationBuilder.getMethodSourceBuilders();
  }

  public void addServiceMethodBuilder(ServiceMethod method) {
    getInterfaceSourceBuilder().addMethodSourceBuilder(method.getInterfaceSourceBuilder());
    getImplementationSourceBuilder().addMethodSourceBuilder(method.getImplementationSourceBuilder());
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

  public boolean addProxyRegistrationProject(IJavaProject clientProject) {
    return m_proxyRegistrationProjects.add(clientProject);
  }

  public boolean removeProxyRegistrationProject(IJavaProject project) {
    return m_proxyRegistrationProjects.remove(project);
  }

  public void setProxyRegistrationProjects(List<IJavaProject> projects) {
    m_proxyRegistrationProjects.clear();
    m_proxyRegistrationProjects.addAll(projects);
  }

  public List<IJavaProject> getProxyRegistrationProjects() {
    return Collections.unmodifiableList(m_proxyRegistrationProjects);
  }

  public boolean addServiceRegistrationProject(IJavaProject project) {
    return m_serviceRegistrationProjects.add(project);
  }

  public boolean removeServiceRegistrationProject(IJavaProject project) {
    return m_serviceRegistrationProjects.remove(project);
  }

  public void setServiceRegistrationProjects(List<IJavaProject> projects) {
    m_serviceRegistrationProjects.clear();
    m_serviceRegistrationProjects.addAll(projects);
  }

  public List<IJavaProject> getServiceRegistrationProjects() {
    return Collections.unmodifiableList(m_serviceRegistrationProjects);
  }

}
