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
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.s.sourcebuilder.lookupcall.LookupCallSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.testcase.TestSourceBuilder;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerationContext;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerators;
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

  private final IJavaEnvironmentProvider m_javaEnvironmentProvider;

  // in
  private String m_lookupCallName;
  private IPackageFragmentRoot m_sharedSourceFolder;
  private IPackageFragmentRoot m_serverSourceFolder;
  private IPackageFragmentRoot m_testSourceFolder;
  private String m_package;
  private IType m_superType;
  private IType m_keyType;
  private IType m_lookupServiceSuperType;

  // out
  private IType m_createdLookupCall;
  private IType m_createdLookupServiceIfc;
  private IType m_createdLookupServiceImpl;
  private IType m_createdLookupCallTest;

  public LookupCallNewOperation() {
    this(new CachingJavaEnvironmentProvider());
  }

  protected LookupCallNewOperation(IJavaEnvironmentProvider provider) {
    m_javaEnvironmentProvider = provider;
  }

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
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 4);

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

    setCreatedLookupCallTest(createLookupCallTest(progress.newChild(1), workingCopyManager));
  }

  protected IType createLookupCallTest(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IPackageFragmentRoot testSourceFolder = getTestSourceFolder();
    if (!S2eUtils.exists(testSourceFolder)) {
      return null;
    }

    IJavaEnvironment env = getEnvProvider().get(testSourceFolder.getJavaProject());
    ScoutTier targetTier = ScoutTier.valueOf(testSourceFolder);
    String testPackage = ScoutTier.Shared.convert(targetTier, getPackage());
    boolean isClient = ScoutTier.Client.equals(targetTier);
    String runnerFqn = null;
    if (isClient) {
      runnerFqn = IScoutRuntimeTypes.ClientTestRunner;
    }
    else {
      runnerFqn = IScoutRuntimeTypes.ServerTestRunner;
    }

    // validate source folder
    if (env.findType(runnerFqn) == null) {
      // source folder cannot be used: required runner is not accessible
      SdkLog.warning("Cannot generate a LookupCall test class because the class '{}' is not on the classpath of project '{}'. Consider adding the required dependency.", runnerFqn, testSourceFolder.getJavaProject().getElementName());
      return null;
    }

    TestSourceBuilder lookupCallTestBuilder = new TestSourceBuilder(getLookupCallName() + ISdkProperties.SUFFIX_TEST, testPackage, env);
    lookupCallTestBuilder.setClientTest(isClient);
    lookupCallTestBuilder.setRunnerSignature(Signature.createTypeSignature(runnerFqn));
    if (!isClient) {
      IType session = S2eUtils.getSession(testSourceFolder.getJavaProject(), ScoutTier.Server, monitor);
      if (S2eUtils.exists(session)) {
        lookupCallTestBuilder.setSessionSignature(Signature.createTypeSignature(session.getFullyQualifiedName()));
      }
    }
    lookupCallTestBuilder.setup();

    ITypeSourceBuilder mainType = lookupCallTestBuilder.getMainType();

    // createLookupCall
    IMethodSourceBuilder createLookupCall = new MethodSourceBuilder("createLookupCall");
    createLookupCall.setFlags(Flags.AccProtected);
    final String lookupCallSig = Signature.createTypeSignature(getCreatedLookupCall().getFullyQualifiedName());
    createLookupCall.setReturnTypeSignature(lookupCallSig);
    createLookupCall.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("return new ").append(validator.useSignature(lookupCallSig)).append("();");
      }
    });
    mainType.addMethod(createLookupCall);
    mainType.addMethod(createTestMethod(createLookupCall.getElementName(), "All"));
    mainType.addMethod(createTestMethod(createLookupCall.getElementName(), "Key"));
    mainType.addMethod(createTestMethod(createLookupCall.getElementName(), "Text"));

    return S2eUtils.writeType(testSourceFolder, lookupCallTestBuilder, env, monitor, workingCopyManager);
  }

  protected IMethodSourceBuilder createTestMethod(final String lookupCallCreateMethodName, final String suffix) {
    IMethodSourceBuilder testMethod = new MethodSourceBuilder("testLookupBy" + suffix);
    testMethod.setFlags(Flags.AccPublic);
    testMethod.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    testMethod.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        String callVarName = "call";
        source.append(validator.useName(getCreatedLookupCall().getFullyQualifiedName())).append(' ').append(callVarName).append(" = ")
            .append(lookupCallCreateMethodName).append("();").append(lineDelimiter);
        source.append(CoreUtils.getCommentBlock("fill call")).append(lineDelimiter);

        String dataType = new StringBuilder(IJavaRuntimeTypes.List).append(ISignatureConstants.C_GENERIC_START)
            .append("? extends ").append(IScoutRuntimeTypes.ILookupRow).append(ISignatureConstants.C_GENERIC_START).append(validator.useName(getKeyType().getFullyQualifiedName()))
            .append(ISignatureConstants.C_GENERIC_END).append(ISignatureConstants.C_GENERIC_END).toString();

        source.append(validator.useName(dataType)).append(" data = ").append(callVarName).append(".getDataBy").append(suffix).append("();").append(lineDelimiter);
        source.append(CoreUtils.getCommentBlock("verify data"));
      }
    });
    testMethod.addAnnotation(ScoutAnnotationSourceBuilderFactory.createTest());
    return testMethod;
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

    return S2eUtils.writeType(getSharedSourceFolder(), ifcBuilder, getEnvProvider().get(getSharedSourceFolder().getJavaProject()), monitor, workingCopyManager);
  }

  protected IType createLookupServiceImpl(String svcName, String serverPackage, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    CompilationUnitSourceBuilder implBuilder = new CompilationUnitSourceBuilder(svcName + SuffixConstants.SUFFIX_STRING_java, serverPackage);
    implBuilder.setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(implBuilder));

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
    IJavaEnvironment env = getEnvProvider().get(getServerSourceFolder().getJavaProject());
    List<IMethodSourceBuilder> unimplementedMethods = MethodSourceBuilderFactory.createUnimplementedMethods(lookupSvcImplBuilder.getSuperTypeSignature(), null, env);
    for (IMethodSourceBuilder methodSourceBuilder : unimplementedMethods) {
      lookupSvcImplBuilder.addMethod(methodSourceBuilder);
    }

    return S2eUtils.writeType(getServerSourceFolder(), implBuilder, getEnvProvider().get(getServerSourceFolder().getJavaProject()), monitor, workingCopyManager);
  }

  protected IType createLookupCall(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    IJavaEnvironment env = getEnvProvider().get(getSharedSourceFolder().getJavaProject());

    LookupCallSourceBuilder lcsb = new LookupCallSourceBuilder(getLookupCallName(), getPackage(), env);
    lcsb.setSuperTypeSignature(Signature.createTypeSignature(getSuperType().getFullyQualifiedName()));
    if (S2eUtils.exists(getCreatedLookupServiceIfc())) {
      lcsb.setLookupServiceIfcSignature(Signature.createTypeSignature(getCreatedLookupServiceIfc().getFullyQualifiedName()));
    }
    lcsb.setKeyTypeSignature(Signature.createTypeSignature(getKeyType().getFullyQualifiedName()));
    if (ClassIdGenerators.isAutomaticallyCreateClassIdAnnotation()) {
      lcsb.setClassIdValue(ClassIdGenerators.generateNewId(new ClassIdGenerationContext(getPackage() + '.' + getLookupCallName())));
    }
    lcsb.setup();

    // add unimplemented methods
    List<IMethodSourceBuilder> unimplementedMethods = MethodSourceBuilderFactory.createUnimplementedMethods(lcsb.getMainType().getSuperTypeSignature(), null, env);
    for (IMethodSourceBuilder methodSourceBuilder : unimplementedMethods) {
      lcsb.getMainType().addSortedMethod(SortedMemberKeyFactory.createMethodAnyKey(methodSourceBuilder), methodSourceBuilder);
    }

    return S2eUtils.writeType(getSharedSourceFolder(), lcsb, env, monitor, workingCopyManager);
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

  protected IJavaEnvironmentProvider getEnvProvider() {
    return m_javaEnvironmentProvider;
  }

  public IPackageFragmentRoot getTestSourceFolder() {
    return m_testSourceFolder;
  }

  public void setTestSourceFolder(IPackageFragmentRoot testSourceFolder) {
    m_testSourceFolder = testSourceFolder;
  }

  public IType getCreatedLookupCallTest() {
    return m_createdLookupCallTest;
  }

  protected void setCreatedLookupCallTest(IType createdLookupCallTest) {
    m_createdLookupCallTest = createdLookupCallTest;
  }
}
