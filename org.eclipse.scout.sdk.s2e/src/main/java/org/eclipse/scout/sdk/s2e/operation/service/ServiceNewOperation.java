/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.operation.service;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.sourcebuilder.service.ServiceImplSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.service.ServiceInterfaceSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;

/**
 * <h3>{@link ServiceNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ServiceNewOperation implements IOperation {

  private final IJavaEnvironmentProvider m_javaEnvironmentProvider;

  private ITypeSourceBuilder m_serviceIfcBuilder;

  // in
  private IPackageFragmentRoot m_sharedSourceFolder;
  private IPackageFragmentRoot m_serverSourceFolder;
  private String m_sharedPackage;
  private String m_serviceName;
  private final List<IMethodSourceBuilder> m_methods;

  // out
  private IType m_createdServiceInterface;
  private IType m_createdServiceImpl;

  public ServiceNewOperation() {
    this(new CachingJavaEnvironmentProvider());
  }

  public ServiceNewOperation(IJavaEnvironmentProvider provider) {
    m_javaEnvironmentProvider = Validate.notNull(provider);
    m_methods = new LinkedList<>();
  }

  @Override
  public String getOperationName() {
    return "Create Service '" + getServiceName() + "'.";
  }

  @Override
  public void validate() {
    Validate.isTrue(S2eUtils.exists(getSharedSourceFolder()), "No shared source folder provided");
    Validate.isTrue(S2eUtils.exists(getServerSourceFolder()), "No server source folder provided");
    Validate.notNull(getSharedPackage(), "No shared package provided");
    Validate.notNull(getServiceName(), "No service base name provided");
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 2);

    String serverPackage = ScoutTier.Shared.convert(ScoutTier.Server, getSharedPackage());
    String svcName = getServiceName() + ISdkProperties.SUFFIX_SERVICE;

    setCreatedServiceInterface(createServiceIfc(svcName, getSharedPackage(), progress.newChild(1), workingCopyManager));
    setCreatedServiceImpl(createServiceImpl(svcName, serverPackage, progress.newChild(1), workingCopyManager));
  }

  protected ServiceImplSourceBuilder createServiceImplBuilder(String svcName, String serverPackage) throws JavaModelException {
    IType existingServiceImpl = getServerSourceFolder().getJavaProject().findType(serverPackage, svcName);
    ServiceImplSourceBuilder implBuilder = null;
    ICompilationUnit compilationUnit = null;
    if (S2eUtils.exists(existingServiceImpl) && !existingServiceImpl.isBinary()) {
      compilationUnit = S2eUtils.jdtTypeToScoutType(existingServiceImpl, getEnvProvider().get(existingServiceImpl.getJavaProject())).compilationUnit();
      implBuilder = new ServiceImplSourceBuilder(compilationUnit, getServiceIfcBuilder());
    }
    else {
      implBuilder = new ServiceImplSourceBuilder(svcName, serverPackage, getServiceIfcBuilder());
    }

    implBuilder.setup();
    for (IMethodSourceBuilder msb : getMethods()) {
      if (compilationUnit == null || !compilationUnit.mainType().methods().withMethodIdentifier(msb.getMethodIdentifier()).existsAny()) {
        boolean existsInInterface = Flags.isInterface(msb.getFlags()) || Flags.isPublic(msb.getFlags());
        msb.setFlags((msb.getFlags() & ~Flags.AccInterface) | Flags.AccPublic);
        msb.setComment(null);
        if (existsInInterface) {
          msb.addAnnotation(AnnotationSourceBuilderFactory.createOverride());
        }
        implBuilder.getMainType().addMethod(msb);
      }
    }

    return implBuilder;
  }

  protected IType createServiceImpl(String svcName, String serverPackage, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws JavaModelException {
    ServiceImplSourceBuilder implBuilder = createServiceImplBuilder(svcName, serverPackage);
    return S2eUtils.writeType(getServerSourceFolder(), implBuilder, getEnvProvider().get(getServerSourceFolder().getJavaProject()), monitor, workingCopyManager);
  }

  protected ServiceInterfaceSourceBuilder createServiceIfcBuilder(String svcName, String sharedPackage) throws JavaModelException {
    String ifcName = 'I' + svcName;
    IType existingServiceIfc = getSharedSourceFolder().getJavaProject().findType(sharedPackage, ifcName);
    ServiceInterfaceSourceBuilder ifcBuilder = null;
    ICompilationUnit compilationUnit = null;
    if (S2eUtils.exists(existingServiceIfc) && !existingServiceIfc.isBinary()) {
      compilationUnit = S2eUtils.jdtTypeToScoutType(existingServiceIfc, getEnvProvider().get(existingServiceIfc.getJavaProject())).compilationUnit();
      ifcBuilder = new ServiceInterfaceSourceBuilder(compilationUnit);
    }
    else {
      ifcBuilder = new ServiceInterfaceSourceBuilder(ifcName, sharedPackage);
    }

    ifcBuilder.setup();
    for (IMethodSourceBuilder msb : getMethods()) {
      if ((Flags.isPublic(msb.getFlags()) || Flags.isInterface(msb.getFlags()))
          && (compilationUnit == null || !compilationUnit.mainType().methods().withMethodIdentifier(msb.getMethodIdentifier()).existsAny())) {
        msb.setFlags((msb.getFlags() | Flags.AccInterface) & ~Flags.AccPublic); // remove public flag, add interface flag
        ifcBuilder.getMainType().addMethod(msb);
      }
    }

    return ifcBuilder;
  }

  protected IType createServiceIfc(String svcName, String sharedPackage, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws JavaModelException {
    ServiceInterfaceSourceBuilder ifcBuilder = createServiceIfcBuilder(svcName, sharedPackage);
    IType createdIfc = S2eUtils.writeType(getSharedSourceFolder(), ifcBuilder, getEnvProvider().get(getSharedSourceFolder().getJavaProject()), monitor, workingCopyManager);
    setServiceIfcBuilder(ifcBuilder.getMainType());
    return createdIfc;
  }

  public IPackageFragmentRoot getSharedSourceFolder() {
    return m_sharedSourceFolder;
  }

  public void setSharedSourceFolder(IPackageFragmentRoot sharedSourceFolder) {
    m_sharedSourceFolder = sharedSourceFolder;
  }

  public IPackageFragmentRoot getServerSourceFolder() {
    return m_serverSourceFolder;
  }

  public void setServerSourceFolder(IPackageFragmentRoot serverSourceFolder) {
    m_serverSourceFolder = serverSourceFolder;
  }

  protected IJavaEnvironmentProvider getEnvProvider() {
    return m_javaEnvironmentProvider;
  }

  protected ITypeSourceBuilder getServiceIfcBuilder() {
    return m_serviceIfcBuilder;
  }

  protected void setServiceIfcBuilder(ITypeSourceBuilder serviceIfcBuilder) {
    m_serviceIfcBuilder = serviceIfcBuilder;
  }

  public IType getCreatedServiceInterface() {
    return m_createdServiceInterface;
  }

  protected void setCreatedServiceInterface(IType createdServiceInterface) {
    m_createdServiceInterface = createdServiceInterface;
  }

  public IType getCreatedServiceImpl() {
    return m_createdServiceImpl;
  }

  protected void setCreatedServiceImpl(IType createdServiceImpl) {
    m_createdServiceImpl = createdServiceImpl;
  }

  public String getSharedPackage() {
    return m_sharedPackage;
  }

  public void setSharedPackage(String sharedPackage) {
    m_sharedPackage = sharedPackage;
  }

  public String getServiceName() {
    return m_serviceName;
  }

  public void addMethod(IMethodSourceBuilder msb) {
    m_methods.add(msb);
  }

  public List<IMethodSourceBuilder> getMethods() {
    return m_methods;
  }

  /**
   * @param serviceName
   *          Service name without any pre- and suffixes.
   */
  public void setServiceName(String serviceName) {
    m_serviceName = serviceName;
  }
}
