/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.service;

import static org.eclipse.scout.sdk.core.model.api.Flags.isInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.environment.SdkFuture;
import org.eclipse.scout.sdk.core.s.testcase.TestGenerator;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link ServiceNewOperation}</h3>
 *
 * @since 7.0.0
 */
public class ServiceNewOperation implements BiConsumer<IEnvironment, IProgress> {

  private ITypeGenerator<?> m_serviceIfcBuilder;

  // in
  private IClasspathEntry m_sharedSourceFolder;
  private IClasspathEntry m_serverSourceFolder;
  private String m_sharedPackage;
  private String m_serviceName;
  private final List<IMethodGenerator<?, ?>> m_methods;
  private IClasspathEntry m_testSourceFolder;
  private String m_serverSession; // optional
  private boolean m_createTest;

  // out
  private IFuture<IType> m_createdServiceInterface;
  private String m_createdServiceInterfaceFqn;

  private IFuture<IType> m_createdServiceImpl;
  private String m_createdServiceImplFqn;

  private IFuture<IType> m_createdServiceTest;
  private String m_createdServiceTestFqn;

  public ServiceNewOperation() {
    m_methods = new ArrayList<>();
  }

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    validateOperation();
    prepareProgress(progress);
    executeOperation(env, progress);
  }

  protected void validateOperation() {
    Ensure.notNull(getSharedSourceFolder(), "No shared source folder provided");
    Ensure.notNull(getServerSourceFolder(), "No server source folder provided");
    Ensure.notNull(getSharedPackage(), "No shared package provided");
    Ensure.notNull(getServiceName(), "No service base name provided");
    if (isCreateTest()) {
      Ensure.notNull(getTestSourceFolder(), "No test source folder provided");
    }
  }

  protected void prepareProgress(IProgress progress) {
    progress.init(getTotalWork(), toString());
  }

  protected void executeOperation(IEnvironment env, IProgress progress) {
    var serverPackage = ScoutTier.Shared.convert(ScoutTier.Server, getSharedPackage());
    var svcName = getServiceName() + ISdkConstants.SUFFIX_SERVICE;

    setCreatedServiceInterface(createServiceIfc(svcName, getSharedPackage(), env, progress.newChild(1)));
    setCreatedServiceImpl(createServiceImpl(svcName, serverPackage, env, progress.newChild(1)));

    if (isCreateTest()) {
      setCreatedServiceTest(createServiceTest(env, progress.newChild(1)));
    }
  }

  protected int getTotalWork() {
    var result = 2; // ifc, impl
    if (isCreateTest()) {
      result += 1;// test
    }
    return result;
  }

  protected ICompilationUnitGenerator<?> createServiceImplGenerator(String svcName, String serverPackage) {
    var javaEnvironment = getServerSourceFolder().javaEnvironment();
    var existingServiceImpl = javaEnvironment.findType(serverPackage + JavaTypes.C_DOT + svcName);
    ICompilationUnitGenerator<?> implBuilder;
    if (existingServiceImpl.isPresent()) {
      implBuilder = existingServiceImpl.orElseThrow().requireCompilationUnit().toWorkingCopy();
    }
    else {
      implBuilder = PrimaryTypeGenerator.create()
          .withElementName(svcName)
          .withPackageName(serverPackage);
    }
    implBuilder.mainType().ifPresent(t -> t
        .withInterface(getServiceIfcBuilder().fullyQualifiedName())
        .withPreProcessor(this::addMissingImplMethods));
    return implBuilder;
  }

  protected void addMissingImplMethods(ITypeGenerator<?> svcImpl, IJavaBuilderContext context) {
    for (var msb : getMethods()) {
      var methodIdToSearch = msb.identifier(context);
      var existingMethod = svcImpl.method(methodIdToSearch, context, false);
      if (existingMethod.isEmpty()) {
        var existsInInterface = isInterface(msb.flags());
        if (existsInInterface) {
          msb.asPublic().withAnnotation(AnnotationGenerator.createOverride());
        }
        svcImpl.withMethod(msb
            .withFlags(msb.flags())
            .withoutFlags(Flags.AccInterface)
            .withComment(null));
      }
    }
  }

  protected IFuture<IType> createServiceImpl(String svcName, String serverPackage, IEnvironment env, IProgress progress) {
    var implBuilder = createServiceImplGenerator(svcName, serverPackage);
    implBuilder.mainType().ifPresent(gen -> setCreatedServiceImplFqn(gen.fullyQualifiedName()));
    return env.writeCompilationUnitAsync(implBuilder, getServerSourceFolder(), progress);
  }

  protected ICompilationUnitGenerator<?> createServiceIfcBuilder(String svcName, String sharedPackage) {
    var ifcName = 'I' + svcName;
    var javaEnvironment = getSharedSourceFolder().javaEnvironment();
    var existingServiceIfc = javaEnvironment.findType(sharedPackage + JavaTypes.C_DOT + ifcName);
    var ifcBuilder = existingServiceIfc
        .<ICompilationUnitGenerator<?>> map(iType -> iType.requireCompilationUnit().toWorkingCopy())
        .orElseGet(() -> new ServiceInterfaceGenerator<>()
            .withElementName(ifcName)
            .withPackageName(sharedPackage));
    ifcBuilder.mainType().ifPresent(t -> t.withPreProcessor(this::addMissingIfcMethods));
    return ifcBuilder;
  }

  protected void addMissingIfcMethods(ITypeGenerator<?> svcIfc, IJavaBuilderContext context) {
    for (var msb : getMethods()) {
      if (isInterface(msb.flags())) {
        var methodIdToSearch = msb.identifier(context);
        var existingMethod = svcIfc.method(methodIdToSearch, context, false);
        if (existingMethod.isEmpty()) {
          svcIfc.withMethod(msb.withoutFlags(Flags.AccPublic));
        }
      }
    }
  }

  protected IFuture<IType> createServiceIfc(String svcName, String sharedPackage, IEnvironment env, IProgress progress) {
    var ifcBuilder = createServiceIfcBuilder(svcName, sharedPackage);
    ifcBuilder.mainType().ifPresent(gen -> setCreatedServiceInterfaceFqn(gen.fullyQualifiedName()));
    var createdIfc = env.writeCompilationUnitAsync(ifcBuilder, getSharedSourceFolder(), progress);
    setServiceIfcBuilder(ifcBuilder.mainType().orElse(null));
    return createdIfc;
  }

  protected IFuture<IType> createServiceTest(IEnvironment env, IProgress progress) {
    var testSourceFolder = getTestSourceFolder();
    var serverPackage = JavaTypes.qualifier(getCreatedServiceImplFqn());
    var baseName = JavaTypes.simpleName(getCreatedServiceImplFqn());
    var elementName = baseName + ISdkConstants.SUFFIX_TEST;

    var existingServiceTest = testSourceFolder.javaEnvironment().findType(serverPackage + JavaTypes.C_DOT + elementName);
    if (existingServiceTest.isPresent()) {
      // service test class already exists
      return SdkFuture.completed(existingServiceTest.orElseThrow());
    }

    var scoutApi = testSourceFolder.javaEnvironment().requireApi(IScoutApi.class);
    var testBuilder = new TestGenerator<>()
        .withElementName(elementName)
        .withPackageName(serverPackage)
        .withRunner(scoutApi.ServerTestRunner().fqn())
        .asClientTest(false);
    if (Strings.hasText(getServerSession())) {
      testBuilder.withSession(getServerSession());
    }

    setCreatedServiceTestFqn(testBuilder.fullyQualifiedName());
    return env.writeCompilationUnitAsync(testBuilder, testSourceFolder, progress);
  }

  public IClasspathEntry getSharedSourceFolder() {
    return m_sharedSourceFolder;
  }

  public void setSharedSourceFolder(IClasspathEntry sharedSourceFolder) {
    m_sharedSourceFolder = sharedSourceFolder;
  }

  public IClasspathEntry getServerSourceFolder() {
    return m_serverSourceFolder;
  }

  public void setServerSourceFolder(IClasspathEntry serverSourceFolder) {
    m_serverSourceFolder = serverSourceFolder;
  }

  protected ITypeGenerator<?> getServiceIfcBuilder() {
    return m_serviceIfcBuilder;
  }

  protected void setServiceIfcBuilder(ITypeGenerator<?> serviceIfcBuilder) {
    m_serviceIfcBuilder = serviceIfcBuilder;
  }

  public IFuture<IType> getCreatedServiceInterface() {
    return m_createdServiceInterface;
  }

  protected void setCreatedServiceInterface(IFuture<IType> createdServiceInterface) {
    m_createdServiceInterface = createdServiceInterface;
  }

  public String getCreatedServiceInterfaceFqn() {
    return m_createdServiceInterfaceFqn;
  }

  protected void setCreatedServiceInterfaceFqn(String createdServiceInterfaceFqn) {
    m_createdServiceInterfaceFqn = createdServiceInterfaceFqn;
  }

  public IFuture<IType> getCreatedServiceImpl() {
    return m_createdServiceImpl;
  }

  protected void setCreatedServiceTest(IFuture<IType> createdServiceTest) {
    m_createdServiceTest = createdServiceTest;
  }

  public IFuture<IType> getCreatedServiceTest() {
    return m_createdServiceTest;
  }

  protected void setCreatedServiceTestFqn(String createdServiceTestFqn) {
    m_createdServiceTestFqn = createdServiceTestFqn;
  }

  public String getCreatedServiceTestFqn() {
    return m_createdServiceTestFqn;
  }

  protected void setCreatedServiceImpl(IFuture<IType> createdServiceImpl) {
    m_createdServiceImpl = createdServiceImpl;
  }

  public String getCreatedServiceImplFqn() {
    return m_createdServiceImplFqn;
  }

  protected void setCreatedServiceImplFqn(String createdServiceImplFqn) {
    m_createdServiceImplFqn = createdServiceImplFqn;
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

  public void addMethod(IMethodGenerator<?, ?> msb) {
    m_methods.add(msb);
  }

  public List<IMethodGenerator<?, ?>> getMethods() {
    return m_methods;
  }

  /**
   * @param serviceName
   *          Service name without any pre- and suffixes.
   */
  public void setServiceName(String serviceName) {
    m_serviceName = serviceName;
  }

  public void setTestSourceFolder(IClasspathEntry testSourceFolder) {
    m_testSourceFolder = testSourceFolder;
  }

  public IClasspathEntry getTestSourceFolder() {
    return m_testSourceFolder;
  }

  public void setServerSession(String serverSession) {
    m_serverSession = serverSession;
  }

  public String getServerSession() {
    return m_serverSession;
  }

  public boolean isCreateTest() {
    return m_createTest;
  }

  public void setCreateTest(boolean createTest) {
    m_createTest = createTest;
  }

  @Override
  public String toString() {
    return "Create new Service";
  }
}
