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
package org.eclipse.scout.sdk.core.s.lookupcall;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.classid.ClassIds;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.testcase.TestGenerator;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link LookupCallNewOperation}</h3>
 *
 * @since 7.0.0
 */
public class LookupCallNewOperation implements BiConsumer<IEnvironment, IProgress> {

  // in
  private String m_lookupCallName;
  private IClasspathEntry m_sharedSourceFolder;
  private IClasspathEntry m_serverSourceFolder;
  private IClasspathEntry m_testSourceFolder;
  private String m_package;
  private String m_superType;
  private String m_keyType;
  private String m_lookupServiceSuperType;
  private String m_serverSession;

  // out
  private IFuture<IType> m_createdLookupCall;
  private String m_createdLookupCallFqn;

  private IFuture<IType> m_createdLookupServiceIfc;
  private String m_createdLookupServiceIfcFqn;

  private IFuture<IType> m_createdLookupServiceImpl;
  private String m_createdLookupServiceImplFqn;

  private IFuture<IType> m_createdLookupCallTest;

  private String m_createdLookupCallTestFqn;

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    validateOperation();
    prepareProgress(progress);
    executeOperation(env, progress);
  }

  protected void validateOperation() {
    Ensure.notBlank(getLookupCallName(), "No lookup call name provided");
    Ensure.notNull(getSharedSourceFolder(), "No shared source folder provided");
    Ensure.notNull(getKeyType(), "No key type provided");
    Ensure.notBlank(getPackage(), "No package name provided");
    Ensure.notNull(getSuperType(), "No supertype provided");
  }

  protected void prepareProgress(IProgress progress) {
    progress.init(getTotalWork(), toString());
  }

  protected void executeOperation(IEnvironment env, IProgress progress) {
    var svcName = Strings.removeSuffix(getLookupCallName(), "Call") + ISdkConstants.SUFFIX_SERVICE;
    setCreatedLookupServiceIfc(createLookupServiceIfc(svcName, env, progress.newChild(1)));

    if (getServerSourceFolder() != null && getLookupServiceSuperType() != null) {
      var serverPackage = ScoutTier.Shared.convert(ScoutTier.Server, getPackage());
      setCreatedLookupServiceImpl(createLookupServiceImpl(svcName, serverPackage, env, progress.newChild(1)));
    }
    progress.setWorkRemaining(2);

    setCreatedLookupCall(createLookupCall(env, progress.newChild(1)));
    setCreatedLookupCallTest(createLookupCallTest(env, progress.newChild(1)));
  }

  protected int getTotalWork() {
    return 4; // lookupcall, service ifc & impl, test
  }

  protected IFuture<IType> createLookupCallTest(IEnvironment env, IProgress progress) {
    var testSourceFolder = getTestSourceFolder();
    if (testSourceFolder == null) {
      return null;
    }
    var testEnv = testSourceFolder.javaEnvironment();

    var scoutApi = testEnv.requireApi(IScoutApi.class);
    var targetTier = ScoutTier.valueOf(testEnv)
        .orElseThrow(() -> newFail("Test-source-folder {} has no access to Scout classes", testSourceFolder));
    var testPackage = ScoutTier.Shared.convert(targetTier, getPackage());
    var isClient = ScoutTier.Client.isIncludedIn(targetTier);
    String runnerFqn;
    if (isClient) {
      runnerFqn = scoutApi.ClientTestRunner().fqn();
    }
    else {
      runnerFqn = scoutApi.ServerTestRunner().fqn();
    }

    // validate source folder
    if (!testEnv.exists(runnerFqn)) {
      // source folder cannot be used: required runner is not accessible
      SdkLog.warning("Cannot generate a LookupCall test class because the class '{}' is not on the classpath. Consider adding the required dependency.", runnerFqn);
      return null;
    }

    var createLookupCallMethodName = "createLookupCall";
    var lookupCallTestBuilder = new TestGenerator<>()
        .withElementName(getLookupCallName() + ISdkConstants.SUFFIX_TEST)
        .withPackageName(testPackage)
        .asClientTest(isClient)
        .withRunner(runnerFqn)
        .withMethod(MethodGenerator.create()
            .asProtected()
            .withElementName(createLookupCallMethodName)
            .withReturnType(getCreatedLookupCallFqn())
            .withBody(b -> b.returnClause().appendNew(getCreatedLookupCallFqn()).parenthesisClose().semicolon()))
        .withMethod(createTestMethod(createLookupCallMethodName, scoutApi.LookupCall().getDataByAllMethodName()))
        .withMethod(createTestMethod(createLookupCallMethodName, scoutApi.LookupCall().getDataByKeyMethodName()))
        .withMethod(createTestMethod(createLookupCallMethodName, scoutApi.LookupCall().getDataByTextMethodName()));
    if (!isClient && Strings.hasText(getServerSession())) {
      lookupCallTestBuilder.withSession(getServerSession());
    }

    setCreatedLookupCallTestFqn(lookupCallTestBuilder.fullyQualifiedName());
    return env.writeCompilationUnitAsync(lookupCallTestBuilder, testSourceFolder, progress);
  }

  protected IMethodGenerator<?, ?> createTestMethod(String createLookupCallMethodName, String getDataByMethodName) {
    return MethodGenerator.create()
        .withAnnotation(ScoutAnnotationGenerator.createTest())
        .asPublic()
        .withReturnType(JavaTypes._void)
        .withElementName("test" + Strings.capitalize(getDataByMethodName))
        .withBody(b -> {
          var callVarName = "call";
          var scoutApi = b.context().requireApi(IScoutApi.class);
          var dataType = new StringBuilder(List.class.getName()).append(JavaTypes.C_GENERIC_START)
              .append("? extends ").append(scoutApi.ILookupRow().fqn()).append(JavaTypes.C_GENERIC_START).append(getKeyType())
              .append(JavaTypes.C_GENERIC_END).append(JavaTypes.C_GENERIC_END).toString();

          b.ref(getCreatedLookupCallFqn()).space().append(callVarName).equalSign().append(createLookupCallMethodName).parenthesisOpen().parenthesisClose().semicolon().nl()
              .appendTodo("fill call")
              .ref(dataType).space().append("data").equalSign().append(callVarName).dot().append(getDataByMethodName).parenthesisOpen().parenthesisClose().semicolon().nl()
              .appendTodo("verify data");
        });
  }

  protected IFuture<IType> createLookupServiceIfc(String svcName, IEnvironment env, IProgress progress) {
    var ifcName = 'I' + svcName;
    var scoutApi = getSharedSourceFolder().javaEnvironment().requireApi(IScoutApi.class);
    var superTypeBuilder = new StringBuilder(scoutApi.ILookupService().fqn());
    superTypeBuilder.append(JavaTypes.C_GENERIC_START);
    superTypeBuilder.append(getKeyType());
    superTypeBuilder.append(JavaTypes.C_GENERIC_END);

    var ifc = PrimaryTypeGenerator.create()
        .withAnnotation(ScoutAnnotationGenerator.createTunnelToServer())
        .withElementName(ifcName)
        .withPackageName(getPackage())
        .asPublic()
        .asInterface()
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withInterface(superTypeBuilder.toString());

    setCreatedLookupServiceIfcFqn(ifc.fullyQualifiedName());
    return env.writeCompilationUnitAsync(ifc, getSharedSourceFolder(), progress);
  }

  protected IFuture<IType> createLookupServiceImpl(String svcName, String serverPackage, IEnvironment env, IProgress progress) {
    var impl = createLookupServiceImplGenerator()
        .withElementName(svcName)
        .withPackageName(serverPackage);

    setCreatedLookupServiceImplFqn(impl.fullyQualifiedName());
    getCreatedLookupServiceIfc().result();
    return env.writeCompilationUnitAsync(impl, getServerSourceFolder(), progress);
  }

  protected PrimaryTypeGenerator<?> createLookupServiceImplGenerator() {
    var superTypeBuilder = new StringBuilder(getLookupServiceSuperType());
    superTypeBuilder.append(JavaTypes.C_GENERIC_START);
    superTypeBuilder.append(getKeyType());
    superTypeBuilder.append(JavaTypes.C_GENERIC_END);
    return PrimaryTypeGenerator.create()
        .asPublic()
        .withSuperClass(superTypeBuilder.toString())
        .withInterface(getCreatedLookupServiceIfcFqn())
        .withAllMethodsImplemented();
  }

  protected IFuture<IType> createLookupCall(IEnvironment env, IProgress progress) {
    var lcsb = createLookupCallGenerator()
        .withElementName(getLookupCallName())
        .withPackageName(getPackage())
        .withSuperType(getSuperType())
        .withKeyType(getKeyType())
        .withClassIdValue(ClassIds.nextIfEnabled(getPackage() + JavaTypes.C_DOT + getLookupCallName()));

    if (getCreatedLookupServiceIfc() != null) {
      lcsb.withLookupServiceInterface(getCreatedLookupServiceIfcFqn());
    }

    setCreatedLookupCallFqn(lcsb.fullyQualifiedName());
    return env.writeCompilationUnitAsync(lcsb, getSharedSourceFolder(), progress);
  }

  protected LookupCallGenerator<?> createLookupCallGenerator() {
    return new LookupCallGenerator<>()
        .withAllMethodsImplemented();
  }

  public IFuture<IType> getCreatedLookupCall() {
    return m_createdLookupCall;
  }

  protected void setCreatedLookupCall(IFuture<IType> createdLookupCall) {
    m_createdLookupCall = createdLookupCall;
  }

  public String getCreatedLookupCallFqn() {
    return m_createdLookupCallFqn;
  }

  protected void setCreatedLookupCallFqn(String createdLookupCallFqn) {
    m_createdLookupCallFqn = createdLookupCallFqn;
  }

  public String getLookupCallName() {
    return m_lookupCallName;
  }

  public void setLookupCallName(String lookupCallName) {
    m_lookupCallName = lookupCallName;
  }

  public IClasspathEntry getSharedSourceFolder() {
    return m_sharedSourceFolder;
  }

  public void setSharedSourceFolder(IClasspathEntry sharedSourceFolder) {
    m_sharedSourceFolder = sharedSourceFolder;
  }

  public String getPackage() {
    return m_package;
  }

  public void setPackage(String package1) {
    m_package = package1;
  }

  public String getSuperType() {
    return m_superType;
  }

  public void setSuperType(String superType) {
    m_superType = superType;
  }

  public String getKeyType() {
    return m_keyType;
  }

  public void setKeyType(String keyType) {
    m_keyType = keyType;
  }

  public IClasspathEntry getServerSourceFolder() {
    return m_serverSourceFolder;
  }

  public void setServerSourceFolder(IClasspathEntry serverSourceFolder) {
    m_serverSourceFolder = serverSourceFolder;
  }

  public IFuture<IType> getCreatedLookupServiceIfc() {
    return m_createdLookupServiceIfc;
  }

  protected void setCreatedLookupServiceIfc(IFuture<IType> createdLookupServiceIfc) {
    m_createdLookupServiceIfc = createdLookupServiceIfc;
  }

  public String getCreatedLookupServiceIfcFqn() {
    return m_createdLookupServiceIfcFqn;
  }

  protected void setCreatedLookupServiceIfcFqn(String createdLookupServiceIfcFqn) {
    m_createdLookupServiceIfcFqn = createdLookupServiceIfcFqn;
  }

  public IFuture<IType> getCreatedLookupServiceImpl() {
    return m_createdLookupServiceImpl;
  }

  protected void setCreatedLookupServiceImpl(IFuture<IType> createdLookupServiceImpl) {
    m_createdLookupServiceImpl = createdLookupServiceImpl;
  }

  public String getCreatedLookupServiceImplFqn() {
    return m_createdLookupServiceImplFqn;
  }

  protected void setCreatedLookupServiceImplFqn(String createdLookupServiceImplFqn) {
    m_createdLookupServiceImplFqn = createdLookupServiceImplFqn;
  }

  public String getLookupServiceSuperType() {
    return m_lookupServiceSuperType;
  }

  public void setLookupServiceSuperType(String lookupServiceSuperType) {
    m_lookupServiceSuperType = lookupServiceSuperType;
  }

  public IClasspathEntry getTestSourceFolder() {
    return m_testSourceFolder;
  }

  public void setTestSourceFolder(IClasspathEntry testSourceFolder) {
    m_testSourceFolder = testSourceFolder;
  }

  public IFuture<IType> getCreatedLookupCallTest() {
    return m_createdLookupCallTest;
  }

  protected void setCreatedLookupCallTest(IFuture<IType> createdLookupCallTest) {
    m_createdLookupCallTest = createdLookupCallTest;
  }

  public String getCreatedLookupCallTestFqn() {
    return m_createdLookupCallTestFqn;
  }

  protected void setCreatedLookupCallTestFqn(String createdLookupCallTestFqn) {
    m_createdLookupCallTestFqn = createdLookupCallTestFqn;
  }

  public String getServerSession() {
    return m_serverSession;
  }

  public void setServerSession(String serverSession) {
    m_serverSession = serverSession;
  }

  @Override
  public String toString() {
    return "Create new Lookup Call";
  }
}
