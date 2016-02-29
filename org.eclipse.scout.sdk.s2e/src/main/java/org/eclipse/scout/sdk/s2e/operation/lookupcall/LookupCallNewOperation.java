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
package org.eclipse.scout.sdk.s2e.operation.lookupcall;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.s.sourcebuilder.lookupcall.LookupCallSourceBuilder;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;

/**
 * <h3>{@link LookupCallNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class LookupCallNewOperation implements IOperation {

  // in
  private String m_lookupCallName;
  private IPackageFragmentRoot m_sharedSourceFolder;
  private IPackageFragmentRoot m_serverSourceFolder;
  private String m_package;
  private IType m_superType;
  private IType m_keyType;
  private IType m_lookupServiceSuperType;

  // out
  private IType m_createdLookupCall;
  private IType m_createdLookupServiceIfc;
  private IType m_createdLookupServiceImpl;

  @Override
  public String getOperationName() {
    return "Create LookupCall '" + getLookupCallName() + "'.";
  }

  @Override
  public void validate() {
    Validate.isTrue(StringUtils.isNotBlank(getLookupCallName()), "No lookup call name provided");
    Validate.isTrue(S2eUtils.exists(getSharedSourceFolder()), "No shared source folder provided");
    Validate.isTrue(S2eUtils.exists(getKeyType()), "No key type provided");
    Validate.isTrue(StringUtils.isNotBlank(getPackage()), "No package name provided");
    Validate.isTrue(S2eUtils.exists(getSuperType()), "No supertype provided");
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 3);

    String svcName = getLookupCallName();
    String suffix = "Call";
    if (svcName.endsWith(suffix)) {
      svcName = svcName.substring(0, svcName.length() - suffix.length());
    }
    svcName += ISdkProperties.SUFFIX_SERVICE;

    setCreatedLookupServiceIfc(createLookupServiceIfc(svcName, progress.newChild(1), workingCopyManager));

    if (S2eUtils.exists(getServerSourceFolder()) && S2eUtils.exists(getLookupServiceSuperType())) {
      String serverPackage = ScoutTier.Shared.convert(ScoutTier.Server, getPackage());
      setCreatedLookupServiceImpl(createLookupServiceImpl(svcName, serverPackage, progress.newChild(1), workingCopyManager));
    }
    progress.setWorkRemaining(1);

    setCreatedLookupCall(createLookupCall(progress.newChild(1), workingCopyManager));
  }

  protected IType createLookupServiceIfc(String svcName, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    String ifcName = 'I' + svcName;
    CompilationUnitSourceBuilder ifcBuilder = new CompilationUnitSourceBuilder(ifcName + SuffixConstants.SUFFIX_STRING_java, getPackage());
    ifcBuilder.setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(ifcBuilder));

    ITypeSourceBuilder lookupSvcIfcBuilder = new TypeSourceBuilder(ifcName);
    lookupSvcIfcBuilder.setFlags(Flags.AccPublic | Flags.AccInterface);
    lookupSvcIfcBuilder.setComment(CommentSourceBuilderFactory.createDefaultTypeComment(lookupSvcIfcBuilder));

    StringBuilder superTypeBuilder = new StringBuilder(IScoutRuntimeTypes.ILookupService);
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_START);
    superTypeBuilder.append(getKeyType().getFullyQualifiedName());
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_END);
    lookupSvcIfcBuilder.addInterfaceSignature(Signature.createTypeSignature(superTypeBuilder.toString()));
    ifcBuilder.addType(lookupSvcIfcBuilder);

    // @TunnelToServer
    lookupSvcIfcBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createTunnelToServer());

    return S2eUtils.writeType(getSharedSourceFolder(), ifcBuilder, monitor, workingCopyManager);
  }

  protected IType createLookupServiceImpl(String svcName, String serverPackage, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    String icuFileName = svcName + SuffixConstants.SUFFIX_STRING_java;
    CompilationUnitSourceBuilder implBuilder = new CompilationUnitSourceBuilder(icuFileName, serverPackage);

    ITypeSourceBuilder lookupSvcImplBuilder = new TypeSourceBuilder(svcName);
    lookupSvcImplBuilder.setFlags(Flags.AccPublic);

    StringBuilder superTypeBuilder = new StringBuilder(getLookupServiceSuperType().getFullyQualifiedName());
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_START);
    superTypeBuilder.append(getKeyType().getFullyQualifiedName());
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_END);
    lookupSvcImplBuilder.setSuperTypeSignature(Signature.createTypeSignature(superTypeBuilder.toString()));
    lookupSvcImplBuilder.addInterfaceSignature(Signature.createTypeSignature(getCreatedLookupServiceIfc().getFullyQualifiedName()));
    implBuilder.addType(lookupSvcImplBuilder);

    // add unimplemented methods
    IJavaEnvironment env = ScoutSdkCore.createJavaEnvironment(getServerSourceFolder().getJavaProject());
    List<IMethodSourceBuilder> unimplementedMethods = MethodSourceBuilderFactory.createUnimplementedMethods(lookupSvcImplBuilder.getSuperTypeSignature(), null, env);
    for (IMethodSourceBuilder methodSourceBuilder : unimplementedMethods) {
      lookupSvcImplBuilder.addMethod(methodSourceBuilder);
    }

    return S2eUtils.writeType(getServerSourceFolder(), implBuilder, monitor, workingCopyManager);
  }

  protected IType createLookupCall(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    LookupCallSourceBuilder lcsb = new LookupCallSourceBuilder(getLookupCallName(), getPackage());
    lcsb.setSuperTypeSignature(Signature.createTypeSignature(getSuperType().getFullyQualifiedName()));
    if (S2eUtils.exists(getCreatedLookupServiceIfc())) {
      lcsb.setLookupServiceIfcSignature(Signature.createTypeSignature(getCreatedLookupServiceIfc().getFullyQualifiedName()));
    }
    lcsb.setKeyTypeSignature(Signature.createTypeSignature(getKeyType().getFullyQualifiedName()));
    lcsb.setup();

    // add unimplemented methods
    IJavaEnvironment env = ScoutSdkCore.createJavaEnvironment(getSharedSourceFolder().getJavaProject());
    List<IMethodSourceBuilder> unimplementedMethods = MethodSourceBuilderFactory.createUnimplementedMethods(lcsb.getMainType().getSuperTypeSignature(), null, env);
    for (IMethodSourceBuilder methodSourceBuilder : unimplementedMethods) {
      lcsb.getMainType().addSortedMethod(SortedMemberKeyFactory.createMethodAnyKey(methodSourceBuilder), methodSourceBuilder);
    }

    return S2eUtils.writeType(getSharedSourceFolder(), lcsb, monitor, workingCopyManager);
  }

  public IType getCreatedLookupCall() {
    return m_createdLookupCall;
  }

  protected void setCreatedLookupCall(IType createdLookupCall) {
    m_createdLookupCall = createdLookupCall;
  }

  public String getLookupCallName() {
    return m_lookupCallName;
  }

  public void setLookupCallName(String lookupCallName) {
    m_lookupCallName = lookupCallName;
  }

  public IPackageFragmentRoot getSharedSourceFolder() {
    return m_sharedSourceFolder;
  }

  public void setSharedSourceFolder(IPackageFragmentRoot sharedSourceFolder) {
    m_sharedSourceFolder = sharedSourceFolder;
  }

  public String getPackage() {
    return m_package;
  }

  public void setPackage(String package1) {
    m_package = package1;
  }

  public IType getSuperType() {
    return m_superType;
  }

  public void setSuperType(IType superType) {
    m_superType = superType;
  }

  public IType getKeyType() {
    return m_keyType;
  }

  public void setKeyType(IType keyType) {
    m_keyType = keyType;
  }

  public IPackageFragmentRoot getServerSourceFolder() {
    return m_serverSourceFolder;
  }

  public void setServerSourceFolder(IPackageFragmentRoot serverSourceFolder) {
    m_serverSourceFolder = serverSourceFolder;
  }

  public IType getCreatedLookupServiceIfc() {
    return m_createdLookupServiceIfc;
  }

  protected void setCreatedLookupServiceIfc(IType createdLookupServiceIfc) {
    m_createdLookupServiceIfc = createdLookupServiceIfc;
  }

  public IType getCreatedLookupServiceImpl() {
    return m_createdLookupServiceImpl;
  }

  protected void setCreatedLookupServiceImpl(IType createdLookupServiceImpl) {
    m_createdLookupServiceImpl = createdLookupServiceImpl;
  }

  public IType getLookupServiceSuperType() {
    return m_lookupServiceSuperType;
  }

  public void setLookupServiceSuperType(IType lookupServiceSuperType) {
    m_lookupServiceSuperType = lookupServiceSuperType;
  }

}
