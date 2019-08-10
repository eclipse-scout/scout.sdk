/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.service;

import static org.eclipse.scout.sdk.core.model.api.Flags.isInterface;
import static org.eclipse.scout.sdk.core.model.api.Flags.isPublic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;

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
  private final List<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> m_methods;

  // out
  private IType m_createdServiceInterface;
  private IType m_createdServiceImpl;

  public ServiceNewOperation() {
    m_methods = new ArrayList<>();
  }

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    Ensure.notNull(getSharedSourceFolder(), "No shared source folder provided");
    Ensure.notNull(getServerSourceFolder(), "No server source folder provided");
    Ensure.notNull(getSharedPackage(), "No shared package provided");
    Ensure.notNull(getServiceName(), "No service base name provided");

    progress.init(toString(), 2);

    String serverPackage = ScoutTier.Shared.convert(ScoutTier.Server, getSharedPackage());
    String svcName = getServiceName() + ISdkProperties.SUFFIX_SERVICE;

    setCreatedServiceInterface(createServiceIfc(svcName, getSharedPackage(), env, progress.newChild(1)));
    setCreatedServiceImpl(createServiceImpl(svcName, serverPackage, env, progress.newChild(1)));
  }

  protected ICompilationUnitGenerator<?> createServiceImplBuilder(String svcName, String serverPackage) {
    Optional<IType> existingServiceImpl = getServerSourceFolder().javaEnvironment().findType(serverPackage + JavaTypes.C_DOT + svcName);
    ICompilationUnitGenerator<?> implBuilder;
    if (existingServiceImpl.isPresent()) {
      ICompilationUnit compilationUnit = existingServiceImpl.get().requireCompilationUnit();
      implBuilder = compilationUnit.toWorkingCopy();
      implBuilder.mainType().ifPresent(t -> t.withInterface(getServiceIfcBuilder().fullyQualifiedName()));
    }
    else {
      implBuilder = PrimaryTypeGenerator.create()
          .withElementName(svcName)
          .withPackageName(serverPackage)
          .withInterface(getServiceIfcBuilder().fullyQualifiedName());
    }

    for (IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> msb : getMethods()) {
      Optional<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> existingMehtod = implBuilder
          .mainType()
          .flatMap(mainType -> mainType.method(msb.identifier(true)));
      if (!existingMehtod.isPresent()) {
        boolean existsInInterface = isInterface(msb.flags()) || isPublic(msb.flags());
        if (existsInInterface) {
          msb.withAnnotation(AnnotationGenerator.createOverride());
        }
        implBuilder.mainType().ifPresent(t -> t
            .withMethod(msb
                .withoutFlags(Flags.AccInterface)
                .withFlags(Flags.AccPublic)
                .withComment(null)));
      }
    }
    return implBuilder;
  }

  protected IType createServiceImpl(String svcName, String serverPackage, IEnvironment env, IProgress progress) {
    ICompilationUnitGenerator<?> implBuilder = createServiceImplBuilder(svcName, serverPackage);
    return env.writeCompilationUnit(implBuilder, getServerSourceFolder(), progress);
  }

  protected ICompilationUnitGenerator<?> createServiceIfcBuilder(String svcName, String sharedPackage) {
    String ifcName = 'I' + svcName;
    Optional<IType> existingServiceIfc = getSharedSourceFolder().javaEnvironment().findType(sharedPackage + JavaTypes.C_DOT + ifcName);
    ICompilationUnitGenerator<?> ifcBuilder;
    ifcBuilder = existingServiceIfc
        .<ICompilationUnitGenerator<?>> map(iType -> iType.requireCompilationUnit().toWorkingCopy())
        .orElseGet(() -> new ServiceInterfaceGenerator<>()
            .withElementName(ifcName)
            .withPackageName(sharedPackage));

    for (IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> msb : getMethods()) {
      if (isPublic(msb.flags()) || isInterface(msb.flags())) {
        Optional<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> existingMethod = ifcBuilder.mainType()
            .flatMap(mainType -> mainType.method(msb.identifier()));
        if (!existingMethod.isPresent()) {
          ifcBuilder.mainType().ifPresent(t -> t
              .withMethod(msb
                  .withoutFlags(Flags.AccPublic)
                  .withFlags(Flags.AccInterface)));
        }
      }
    }
    return ifcBuilder;
  }

  protected IType createServiceIfc(String svcName, String sharedPackage, IEnvironment env, IProgress progress) {
    ICompilationUnitGenerator<?> ifcBuilder = createServiceIfcBuilder(svcName, sharedPackage);
    IType createdIfc = env.writeCompilationUnit(ifcBuilder, getSharedSourceFolder(), progress);
    setServiceIfcBuilder(ifcBuilder.mainType().orElse(null));
    return createdIfc;
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

  public void addMethod(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> msb) {
    m_methods.add(msb);
  }

  public List<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> getMethods() {
    return m_methods;
  }

  /**
   * @param serviceName
   *          Service name without any pre- and suffixes.
   */
  public void setServiceName(String serviceName) {
    m_serviceName = serviceName;
  }

  @Override
  public String toString() {
    return "Create new Service";
  }
}
