/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.form;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.builder.java.body.IScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.s.classid.ClassIds;
import org.eclipse.scout.sdk.core.s.dto.DtoGeneratorFactory;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.s.permission.PermissionGenerator;
import org.eclipse.scout.sdk.core.s.service.ServiceNewOperation;
import org.eclipse.scout.sdk.core.s.testcase.TestGenerator;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link FormNewOperation}</h3>
 *
 * @since 7.0.0
 */
public class FormNewOperation implements BiConsumer<IEnvironment, IProgress> {

  // in
  private String m_formName;
  private IClasspathEntry m_clientSourceFolder;
  private IClasspathEntry m_sharedSourceFolder;
  private IClasspathEntry m_serverSourceFolder;
  private IClasspathEntry m_formDataSourceFolder;
  private IClasspathEntry m_clientTestSourceFolder;
  private IClasspathEntry m_serverTestSourceFolder;
  private String m_clientPackage;
  private String m_superType;
  private String m_serverSession;
  private boolean m_createFormData;
  private boolean m_createService;
  private boolean m_createPermissions;

  // out
  private IType m_createdForm;
  private IType m_createdFormData;
  private IType m_createdServiceInterface;
  private IType m_createdServiceImpl;
  private IType m_createdReadPermission;
  private IType m_createdUpdatePermission;
  private IType m_createdCreatePermission;
  private IType m_createdFormTest;
  private IType m_createdServiceTest;

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    Ensure.notBlank(getFormName(), "No form name provided");
    Ensure.notNull(getClientSourceFolder(), "No client source folder provided");
    if (isCreateService()) {
      Ensure.notNull(getServerSourceFolder(), "No server source folder provided");
    }
    if (isCreateService() || isCreatePermissions()) {
      Ensure.notNull(getSharedSourceFolder(), "No shared source folder provided");
    }
    if (isCreateFormData()) {
      Ensure.notNull(getFormDataSourceFolder(), "No form data source folder provided");
    }
    Ensure.notBlank(getClientPackage(), "No package provided");
    Ensure.notNull(getSuperType(), "Super type does not exist");

    progress.init(toString(), getTotalWork());

    // calc names
    String sharedPackage = ScoutTier.Client.convert(ScoutTier.Shared, getClientPackage());
    String baseName = getFormName();
    if (baseName.endsWith(ISdkProperties.SUFFIX_FORM)) {
      baseName = baseName.substring(0, baseName.length() - ISdkProperties.SUFFIX_FORM.length());
    }

    // DTO
    if (isCreateFormData()) {
      setCreatedFormData(createFormData(sharedPackage, env, progress.newChild(1)));
    }

    // permissions
    if (isCreatePermissions()) {
      String permissionBaseName = baseName + ISdkProperties.SUFFIX_PERMISSION;
      setCreatedReadPermission(createReadPermission(permissionBaseName, sharedPackage, env, progress.newChild(1)));
      setCreatedUpdatePermission(createUpdatePermission(permissionBaseName, sharedPackage, env, progress.newChild(1)));
      setCreatedCreatePermission(createCreatePermission(permissionBaseName, sharedPackage, env, progress.newChild(1)));
    }

    // Service
    if (isCreateService()) {
      createService(sharedPackage, baseName, env, progress.newChild(2));

      // service test
      setCreatedServiceTest(createServiceTest(env, progress.newChild(1)));
    }

    // form
    setCreatedForm(createForm(env, progress.newChild(1)));

    // form test
    setCreatedFormTest(createFormTest(env, progress.newChild(1)));

    // schedule DTO update because the formData has been created as empty java file
    if (isCreateFormData()) {
      updateFormData(env, progress.newChild(1));
    }
  }

  protected int getTotalWork() {
    int result = 2; // form & form-tests
    if (isCreateFormData()) {
      result += 2;// formdata creation & formData update
    }
    if (isCreateService()) {
      result += 3; // ifc, impl, test
    }
    if (isCreatePermissions()) {
      result += 3; // create, read, update
    }
    return result;
  }

  protected void updateFormData(IEnvironment env, IProgress progress) {
    Optional<ICompilationUnitGenerator<?>> formDataGenerator = DtoGeneratorFactory.createFormDataGenerator(getCreatedForm(), getFormDataSourceFolder().javaEnvironment());
    env.writeCompilationUnit(formDataGenerator.get(), getFormDataSourceFolder(), progress);
  }

  protected TestGenerator<?> createFormTestBuilder() {
    TestGenerator<?> testBuilder = new TestGenerator<>()
        .withElementName(getCreatedForm().elementName() + ISdkProperties.SUFFIX_TEST)
        .withPackageName(getClientPackage())
        .withRunner(IScoutRuntimeTypes.ClientTestRunner)
        .asClientTest(true);
    if (isCreateService() && isCreateFormData()) {
      // prepare mock
      addMock(testBuilder);
    }
    return testBuilder;
  }

  protected void addMock(ITypeGenerator<?> testBuilder) {
    String mockVarName = "m_mockSvc";
    testBuilder
        .withField(FieldGenerator.create()
            .withElementName(mockVarName)
            .asPrivate()
            .withDataType(getCreatedServiceInterface().name())
            .withAnnotation(ScoutAnnotationGenerator.createBeanMock()))
        .withMethod(MethodGenerator.create()
            .withElementName("setup")
            .asPublic()
            .withReturnType(JavaTypes._void)
            .withAnnotation(ScoutAnnotationGenerator.createBefore())
            .withBody(b -> {
              String varName = "answer";
              String formDataName = getCreatedFormData().name();
              b.ref(formDataName).space().append(varName).equalSign().appendNew().ref(formDataName).parenthesisOpen().parenthesisClose().semicolon().nl();
              appendMockSource(b, varName, mockVarName, FormGenerator.SERVICE_PREPARECREATE_METHOD_NAME);
              appendMockSource(b, varName, mockVarName, FormGenerator.SERVICE_CREATE_METHOD_NAME);
              appendMockSource(b, varName, mockVarName, FormGenerator.SERVICE_LOAD_METHOD_NAME);
              appendMockSource(b, varName, mockVarName, FormGenerator.SERVICE_STORE_METHOD_NAME);
            }));
  }

  protected static void appendMockSource(IMethodBodyBuilder<?> builder, String formDataVarName, String mockVarName, String methodToMock) {
    builder.ref(IScoutRuntimeTypes.Mockito).dotSign().append("when").parenthesisOpen().append(mockVarName).dotSign().append(methodToMock).parenthesisOpen()
        .ref(IScoutRuntimeTypes.ArgumentMatchers).dotSign().append("any").parenthesisOpen().parenthesisClose().parenthesisClose().parenthesisClose()
        .dotSign().append("thenReturn").parenthesisOpen().append(formDataVarName).parenthesisClose().semicolon().nl();
  }

  protected IType createFormTest(IEnvironment env, IProgress progress) {
    IClasspathEntry testSourceFolder = getClientTestSourceFolder();
    if (testSourceFolder == null) {
      return null;
    }

    TestGenerator<?> formTestBuilder = createFormTestBuilder();
    return env.writeCompilationUnit(formTestBuilder, testSourceFolder, progress);
  }

  protected IType createServiceTest(IEnvironment env, IProgress progress) {
    IClasspathEntry testSourceFolder = getServerTestSourceFolder();
    if (testSourceFolder == null) {
      return null;
    }
    String serverPackage = ScoutTier.Client.convert(ScoutTier.Server, getClientPackage());
    String baseName = getCreatedServiceImpl().elementName();
    String elementName = baseName + ISdkProperties.SUFFIX_TEST;

    Optional<IType> existingServiceTest = testSourceFolder.javaEnvironment().findType(serverPackage + JavaTypes.C_DOT + elementName);
    if (existingServiceTest.isPresent()) {
      // service test class already exists
      return existingServiceTest.get();
    }

    TestGenerator<?> testBuilder = new TestGenerator<>()
        .withElementName(elementName)
        .withPackageName(serverPackage)
        .withRunner(IScoutRuntimeTypes.ServerTestRunner)
        .asClientTest(false);
    if (getServerSession() != null) {
      testBuilder.withSession(getServerSession());
    }
    return env.writeCompilationUnit(testBuilder, testSourceFolder, progress);
  }

  protected void createService(String sharedPackage, String baseName, IEnvironment env, IProgress progress) {
    ServiceNewOperation serviceNewOperation = new ServiceNewOperation();
    serviceNewOperation.setServiceName(baseName);
    serviceNewOperation.setSharedPackage(sharedPackage);
    serviceNewOperation.setSharedSourceFolder(getSharedSourceFolder());
    serviceNewOperation.setServerSourceFolder(getServerSourceFolder());

    // add service methods
    serviceNewOperation.addMethod(createServiceMethod(FormGenerator.SERVICE_PREPARECREATE_METHOD_NAME));
    serviceNewOperation.addMethod(createServiceMethod(FormGenerator.SERVICE_CREATE_METHOD_NAME));
    serviceNewOperation.addMethod(createServiceMethod(FormGenerator.SERVICE_LOAD_METHOD_NAME));
    serviceNewOperation.addMethod(createServiceMethod(FormGenerator.SERVICE_STORE_METHOD_NAME));

    serviceNewOperation.accept(env, progress);

    setCreatedServiceImpl(serviceNewOperation.getCreatedServiceImpl());
    setCreatedServiceInterface(serviceNewOperation.getCreatedServiceInterface());
  }

  protected IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createServiceMethod(String name) {
    IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> methodBuilder = ScoutMethodGenerator.create()
        .asPublic()
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withElementName(name)
        .withBody(b -> {
          // permission check
          if (isCreatePermissions()) {
            String methodName = b.surroundingMethod().elementName().orElse(null);
            String permissionFqn;
            if (FormGenerator.SERVICE_LOAD_METHOD_NAME.equals(methodName)) {
              permissionFqn = getCreatedReadPermission().name();
            }
            else if (FormGenerator.SERVICE_STORE_METHOD_NAME.equals(methodName)) {
              permissionFqn = getCreatedUpdatePermission().name();
            }
            else {
              permissionFqn = getCreatedCreatePermission().name();
            }
            createPermissionCheckSource(b, permissionFqn);
          }

          createServiceMethodBody(b);
        });
    if (isCreateFormData()) {
      methodBuilder.withReturnType(getCreatedFormData().name());
      methodBuilder.withParameter(MethodParameterGenerator.create()
          .withElementName("formData")
          .withDataType(getCreatedFormData().name()));
    }
    else {
      methodBuilder.withReturnType(JavaTypes._void);
    }
    return methodBuilder;
  }

  protected void createPermissionCheckSource(IScoutMethodBodyBuilder<?> b, String permissionFqn) {
    b.appendPermissionCheck(permissionFqn);
  }

  protected void createServiceMethodBody(IScoutMethodBodyBuilder<?> builder) {
    builder.appendTodo("add business logic here.");
    if (!builder.needsReturnClause()) {
      return;
    }

    String returnType = builder.surroundingMethod().returnType().get();
    builder
        .returnClause()
        .append(getParamNameOfType(builder.surroundingMethod(), returnType)
            .orElseGet(() -> JavaTypes.defaultValueOf(returnType)))
        .semicolon();
  }

  protected static Optional<String> getParamNameOfType(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> msb, String returnType) {
    return msb.parameters()
        .filter(p -> returnType.equals(p.dataType()
            .orElseThrow(() -> newFail("Type parameter {} in method {} is missing a data type.", p.elementName().orElse(null), msb.elementName().orElse(null)))))
        .findAny()
        .flatMap(IMethodParameterGenerator::elementName);
  }

  protected IType createFormData(String sharedPackage, IEnvironment env, IProgress progress) {
    PrimaryTypeGenerator<?> formDataGenerator = PrimaryTypeGenerator.create()
        .withElementName(getFormName() + ISdkProperties.SUFFIX_DTO)
        .withPackageName(sharedPackage)
        .withSuperClass(IScoutRuntimeTypes.AbstractFormData);
    return env.writeCompilationUnit(formDataGenerator, getFormDataSourceFolder(), progress);
  }

  protected IType createPermission(String permissionName, String sharedPackage, IEnvironment env, IProgress progress) {
    PermissionGenerator<?> psb = new PermissionGenerator<>()
        .withElementName(permissionName)
        .withPackageName(sharedPackage);
    return env.writeCompilationUnit(psb, getSharedSourceFolder(), progress);
  }

  protected IType createCreatePermission(String permissionBaseName, String sharedPackage, IEnvironment env, IProgress progress) {
    return createPermission("Create" + permissionBaseName, sharedPackage, env, progress);
  }

  protected IType createReadPermission(String permissionBaseName, String sharedPackage, IEnvironment env, IProgress progress) {
    return createPermission("Read" + permissionBaseName, sharedPackage, env, progress);
  }

  protected IType createUpdatePermission(String permissionBaseName, String sharedPackage, IEnvironment env, IProgress progress) {
    return createPermission("Update" + permissionBaseName, sharedPackage, env, progress);
  }

  protected IType createForm(IEnvironment env, IProgress progress) {
    FormGenerator<?> formBuilder = createFormBuilder();
    return env.writeCompilationUnit(formBuilder, getClientSourceFolder(), progress);
  }

  protected FormGenerator<?> createFormBuilderInstance() {
    return new FormGenerator<>();
  }

  protected FormGenerator<?> createFormBuilder() {
    FormGenerator<?> formBuilder = createFormBuilderInstance()
        .withSuperClass(getSuperType())
        .withElementName(getFormName())
        .withPackageName(getClientPackage());
    if (isCreateFormData()) {
      formBuilder.withFormData(getCreatedFormData().name());
    }
    if (isCreateService()) {
      formBuilder.withServiceInterface(getCreatedServiceInterface().name());
    }
    if (isCreatePermissions()) {
      formBuilder.withPermissionUpdate(getCreatedUpdatePermission().name());
      formBuilder.withPermissionCreate(getCreatedCreatePermission().name());
    }

    // @ClassId
    if (ClassIds.isAutomaticallyCreateClassIdAnnotation()) {
      String[] classIds = new String[FormGenerator.NUM_CLASS_IDS];
      String fqn = getClientPackage() + JavaTypes.C_DOT + getFormName();
      for (int i = 0; i < classIds.length; i++) {
        classIds[i] = ClassIds.next(fqn);
      }
      formBuilder.withClassIdValues(classIds);
    }
    return formBuilder;
  }

  public String getFormName() {
    return m_formName;
  }

  public void setFormName(String formName) {
    m_formName = formName;
  }

  public IClasspathEntry getClientSourceFolder() {
    return m_clientSourceFolder;
  }

  public void setClientSourceFolder(IClasspathEntry clientSourceFolder) {
    m_clientSourceFolder = clientSourceFolder;
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

  public String getClientPackage() {
    return m_clientPackage;
  }

  public void setClientPackage(String clientPackage) {
    m_clientPackage = clientPackage;
  }

  public String getSuperType() {
    return m_superType;
  }

  public void setSuperType(String superType) {
    m_superType = superType;
  }

  public boolean isCreateFormData() {
    return m_createFormData;
  }

  public void setCreateFormData(boolean createFormData) {
    m_createFormData = createFormData;
  }

  public boolean isCreateService() {
    return m_createService;
  }

  public void setCreateService(boolean createService) {
    m_createService = createService;
  }

  public boolean isCreatePermissions() {
    return m_createPermissions;
  }

  public void setCreatePermissions(boolean createPermissions) {
    m_createPermissions = createPermissions;
  }

  public IType getCreatedForm() {
    return m_createdForm;
  }

  protected void setCreatedForm(IType createdForm) {
    m_createdForm = createdForm;
  }

  public IType getCreatedFormData() {
    return m_createdFormData;
  }

  protected void setCreatedFormData(IType createdFormData) {
    m_createdFormData = createdFormData;
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

  public IType getCreatedReadPermission() {
    return m_createdReadPermission;
  }

  protected void setCreatedReadPermission(IType createdReadPermission) {
    m_createdReadPermission = createdReadPermission;
  }

  public IType getCreatedUpdatePermission() {
    return m_createdUpdatePermission;
  }

  protected void setCreatedUpdatePermission(IType createdUpdatePermission) {
    m_createdUpdatePermission = createdUpdatePermission;
  }

  public IType getCreatedCreatePermission() {
    return m_createdCreatePermission;
  }

  protected void setCreatedCreatePermission(IType createdCreatePermission) {
    m_createdCreatePermission = createdCreatePermission;
  }

  public IClasspathEntry getFormDataSourceFolder() {
    return m_formDataSourceFolder;
  }

  public void setFormDataSourceFolder(IClasspathEntry formDataSourceFolder) {
    m_formDataSourceFolder = formDataSourceFolder;
  }

  public IClasspathEntry getClientTestSourceFolder() {
    return m_clientTestSourceFolder;
  }

  public void setClientTestSourceFolder(IClasspathEntry clientTestSourceFolder) {
    m_clientTestSourceFolder = clientTestSourceFolder;
  }

  public IClasspathEntry getServerTestSourceFolder() {
    return m_serverTestSourceFolder;
  }

  public void setServerTestSourceFolder(IClasspathEntry serverTestSourceFolder) {
    m_serverTestSourceFolder = serverTestSourceFolder;
  }

  public IType getCreatedFormTest() {
    return m_createdFormTest;
  }

  protected void setCreatedFormTest(IType createdFormTest) {
    m_createdFormTest = createdFormTest;
  }

  public IType getCreatedServiceTest() {
    return m_createdServiceTest;
  }

  protected void setCreatedServiceTest(IType createdServiceTest) {
    m_createdServiceTest = createdServiceTest;
  }

  public String getServerSession() {
    return m_serverSession;
  }

  /**
   * @param serverSession
   *          The server session to use in the service test
   */
  public void setServerSession(String serverSession) {
    m_serverSession = serverSession;
  }

  @Override
  public String toString() {
    return "Create new Form";
  }
}