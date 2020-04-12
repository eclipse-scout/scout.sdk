/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.lookupcall;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.classid.ClassIds;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
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
  private IType m_createdLookupCall;
  private IType m_createdLookupServiceIfc;
  private IType m_createdLookupServiceImpl;
  private IType m_createdLookupCallTest;

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    Ensure.notBlank(getLookupCallName(), "No lookup call name provided");
    Ensure.notNull(getSharedSourceFolder(), "No shared source folder provided");
    Ensure.notNull(getKeyType(), "No key type provided");
    Ensure.notBlank(getPackage(), "No package name provided");
    Ensure.notNull(getSuperType(), "No supertype provided");

    progress.init(toString(), 4);

    String svcName = getLookupCallName();
    String suffix = "Call";
    if (svcName.endsWith(suffix)) {
      svcName = svcName.substring(0, svcName.length() - suffix.length());
    }
    svcName += ISdkProperties.SUFFIX_SERVICE;

    setCreatedLookupServiceIfc(createLookupServiceIfc(svcName, env, progress.newChild(1)));

    if (getServerSourceFolder() != null && getLookupServiceSuperType() != null) {
      String serverPackage = ScoutTier.Shared.convert(ScoutTier.Server, getPackage());
      setCreatedLookupServiceImpl(createLookupServiceImpl(svcName, serverPackage, env, progress.newChild(1)));
    }
    progress.setWorkRemaining(2);

    setCreatedLookupCall(createLookupCall(env, progress.newChild(1)));
    setCreatedLookupCallTest(createLookupCallTest(env, progress.newChild(1)));
  }

  protected IType createLookupCallTest(IEnvironment env, IProgress progress) {
    IClasspathEntry testSourceFolder = getTestSourceFolder();
    if (testSourceFolder == null) {
      return null;
    }

    ScoutTier targetTier = ScoutTier.valueOf(testSourceFolder.javaEnvironment())
        .orElseThrow(() -> newFail("Test-source-folder {} has no access to Scout classes", testSourceFolder));
    String testPackage = ScoutTier.Shared.convert(targetTier, getPackage());
    boolean isClient = ScoutTier.Client.isIncludedIn(targetTier);
    String runnerFqn;
    if (isClient) {
      runnerFqn = IScoutRuntimeTypes.ClientTestRunner;
    }
    else {
      runnerFqn = IScoutRuntimeTypes.ServerTestRunner;
    }

    // validate source folder
    if (!testSourceFolder.javaEnvironment().exists(runnerFqn)) {
      // source folder cannot be used: required runner is not accessible
      SdkLog.warning("Cannot generate a LookupCall test class because the class '{}' is not on the classpath. Consider adding the required dependency.", runnerFqn);
      return null;
    }

    String createLookupCallMethodName = "createLookupCall";
    TestGenerator<?> lookupCallTestBuilder = new TestGenerator<>()
        .withElementName(getLookupCallName() + ISdkProperties.SUFFIX_TEST)
        .withPackageName(testPackage)
        .asClientTest(isClient)
        .withRunner(runnerFqn)
        .withMethod(MethodGenerator.create()
            .asProtected()
            .withElementName(createLookupCallMethodName)
            .withReturnType(getCreatedLookupCall().name())
            .withBody(b -> b.returnClause().appendNew().ref(getCreatedLookupCall()).parenthesisOpen().parenthesisClose().semicolon()))
        .withMethod(createTestMethod(createLookupCallMethodName, "All"))
        .withMethod(createTestMethod(createLookupCallMethodName, "Key"))
        .withMethod(createTestMethod(createLookupCallMethodName, "Text"));
    if (!isClient && Strings.hasText(getServerSession())) {
      lookupCallTestBuilder.withSession(getServerSession());
    }

    return env.writeCompilationUnit(lookupCallTestBuilder, testSourceFolder, progress);
  }

  protected IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createTestMethod(String lookupCallCreateMethodName, String suffix) {
    return MethodGenerator.create()
        .withAnnotation(ScoutAnnotationGenerator.createTest())
        .asPublic()
        .withReturnType(JavaTypes._void)
        .withElementName("testLookupBy" + suffix)
        .withBody(b -> {
          String callVarName = "call";
          String dataType = new StringBuilder(List.class.getName()).append(JavaTypes.C_GENERIC_START)
              .append("? extends ").append(IScoutRuntimeTypes.ILookupRow).append(JavaTypes.C_GENERIC_START).append(getKeyType())
              .append(JavaTypes.C_GENERIC_END).append(JavaTypes.C_GENERIC_END).toString();

          b.ref(getCreatedLookupCall()).space().append(callVarName).equalSign().append(lookupCallCreateMethodName).parenthesisOpen().parenthesisClose().semicolon().nl()
              .appendTodo("fill call")
              .ref(dataType).space().append("data").equalSign().append(callVarName).dotSign().append("getDataBy").append(suffix).parenthesisOpen().parenthesisClose().semicolon().nl()
              .appendTodo("verify data");
        });
  }

  protected IType createLookupServiceIfc(String svcName, IEnvironment env, IProgress progress) {
    String ifcName = 'I' + svcName;
    StringBuilder superTypeBuilder = new StringBuilder(IScoutRuntimeTypes.ILookupService);
    superTypeBuilder.append(JavaTypes.C_GENERIC_START);
    superTypeBuilder.append(getKeyType());
    superTypeBuilder.append(JavaTypes.C_GENERIC_END);

    PrimaryTypeGenerator<?> ifc = PrimaryTypeGenerator.create()
        .withAnnotation(ScoutAnnotationGenerator.createTunnelToServer())
        .withElementName(ifcName)
        .withPackageName(getPackage())
        .asPublic()
        .asInterface()
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withInterface(superTypeBuilder.toString());

    return env.writeCompilationUnit(ifc, getSharedSourceFolder(), progress);
  }

  protected IType createLookupServiceImpl(String svcName, String serverPackage, IEnvironment env, IProgress progress) {
    StringBuilder superTypeBuilder = new StringBuilder(getLookupServiceSuperType());
    superTypeBuilder.append(JavaTypes.C_GENERIC_START);
    superTypeBuilder.append(getKeyType());
    superTypeBuilder.append(JavaTypes.C_GENERIC_END);

    PrimaryTypeGenerator<?> impl = PrimaryTypeGenerator.create()
        .withElementName(svcName)
        .withPackageName(serverPackage)
        .asPublic()
        .withSuperClass(superTypeBuilder.toString())
        .withInterface(getCreatedLookupServiceIfc().name())
        .withAllMethodsImplemented();

    return env.writeCompilationUnit(impl, getServerSourceFolder(), progress);
  }

  protected IType createLookupCall(IEnvironment env, IProgress progress) {

    StringBuilder superTypeBuilder = new StringBuilder(getSuperType());
    superTypeBuilder.append(JavaTypes.C_GENERIC_START);
    superTypeBuilder.append(getKeyType());
    superTypeBuilder.append(JavaTypes.C_GENERIC_END);

    LookupCallGenerator<?> lcsb = new LookupCallGenerator<>()
        .withElementName(getLookupCallName())
        .withPackageName(getPackage())
        .withSuperClass(superTypeBuilder.toString())
        .withClassIdValue(ClassIds.nextIfEnabled(getPackage() + JavaTypes.C_DOT + getLookupCallName()))
        .withAllMethodsImplemented();

    if (getCreatedLookupServiceIfc() != null) {
      lcsb.withLookupServiceInterface(getCreatedLookupServiceIfc().name());
    }

    return env.writeCompilationUnit(lcsb, getSharedSourceFolder(), progress);
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

  public IType getCreatedLookupCallTest() {
    return m_createdLookupCallTest;
  }

  protected void setCreatedLookupCallTest(IType createdLookupCallTest) {
    m_createdLookupCallTest = createdLookupCallTest;
  }

  public String getServerSession() {
    return m_serverSession;
  }

  public void setServerSession(String serverSession) {
    m_serverSession = serverSession;
  }

  @Override
  public String toString() {
    return "Create new LookupCall";
  }
}
