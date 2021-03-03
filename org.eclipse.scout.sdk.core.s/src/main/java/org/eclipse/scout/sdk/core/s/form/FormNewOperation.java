/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.form;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.apidef.IScoutVariousApi;
import org.eclipse.scout.sdk.core.s.builder.java.body.IScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.s.dto.DtoGeneratorFactory;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.environment.SdkFuture;
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
  private List<String> m_attributes; // optional

  // out
  private IFuture<IType> m_createdForm;
  private String m_createdFormFqn;

  private IFuture<IType> m_createdFormData;
  private String m_createdFormDataFqn;

  private IFuture<IType> m_createdServiceInterface;
  private String m_createdServiceInterfaceFqn;

  private IFuture<IType> m_createdServiceImpl;
  private String m_createdServiceImplFqn;

  private IFuture<IType> m_createdReadPermission;
  private String m_createdReadPermissionFqn;

  private IFuture<IType> m_createdUpdatePermission;
  private String m_createdUpdatePermissionFqn;

  private IFuture<IType> m_createdCreatePermission;
  private String m_createdCreatePermissionFqn;

  private IFuture<IType> m_createdFormTest;
  private String m_createdFormTestFqn;

  private IFuture<IType> m_createdServiceTest;
  private String m_createdServiceTestFqn;

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    validateOperation();
    prepareProgress(progress);
    executeOperation(env, progress);
  }

  protected void validateOperation() {
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
  }

  protected void prepareProgress(IProgress progress) {
    progress.init(getTotalWork(), toString());
  }

  protected void executeOperation(IEnvironment env, IProgress progress) {
    // calc names
    var sharedPackage = ScoutTier.Client.convert(ScoutTier.Shared, getClientPackage());
    var baseName = getFormName();
    if (baseName.endsWith(ISdkConstants.SUFFIX_FORM)) {
      baseName = baseName.substring(0, baseName.length() - ISdkConstants.SUFFIX_FORM.length());
    }

    // DTO
    if (isCreateFormData()) {
      setCreatedFormData(createFormData(sharedPackage, env, progress.newChild(1)));
    }

    // permissions
    if (isCreatePermissions()) {
      createPermissions(sharedPackage, baseName + ISdkConstants.SUFFIX_PERMISSION, env, progress);
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
    var result = 2; // form & form-tests
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
    // the DtoGeneratorFactory parses the @FormData-Annotation and uses the given formData-IType, therefore the formData needs to be created already
    getCreatedFormData().result();
    var formDataGenerator = DtoGeneratorFactory.createFormDataGenerator(getCreatedForm().result(), getFormDataSourceFolder().javaEnvironment());
    env.writeCompilationUnitAsync(formDataGenerator.get(), getFormDataSourceFolder(), progress);
  }

  protected TestGenerator<?> createFormTestBuilder(IScoutVariousApi scoutApi) {
    TestGenerator<?> testBuilder = new TestGenerator<>()
        .withElementName(JavaTypes.simpleName(getCreatedFormFqn()) + ISdkConstants.SUFFIX_TEST)
        .withPackageName(getClientPackage())
        .withRunner(scoutApi.ClientTestRunner().fqn())
        .asClientTest(true);
    if (isCreateService() && isCreateFormData()) {
      // prepare mock
      addMock(testBuilder);
    }
    return testBuilder;
  }

  protected void addMock(ITypeGenerator<?> testBuilder) {
    var mockVarName = "m_mockSvc";
    testBuilder
        .withField(FieldGenerator.create()
            .withElementName(mockVarName)
            .asPrivate()
            .withDataType(getCreatedServiceInterfaceFqn())
            .withAnnotation(ScoutAnnotationGenerator.createBeanMock()))
        .withMethod(MethodGenerator.create()
            .withElementName("setup")
            .asPublic()
            .withReturnType(JavaTypes._void)
            .withAnnotation(ScoutAnnotationGenerator.createBefore())
            .withBody(b -> {
              var varName = "answer";
              var formDataName = getCreatedFormDataFqn();
              b.ref(formDataName).space().append(varName).equalSign().appendNew().ref(formDataName).parenthesisOpen().parenthesisClose().semicolon().nl();
              appendMockSource(b, varName, mockVarName, FormGenerator.SERVICE_PREPARE_CREATE_METHOD_NAME);
              appendMockSource(b, varName, mockVarName, FormGenerator.SERVICE_CREATE_METHOD_NAME);
              appendMockSource(b, varName, mockVarName, FormGenerator.SERVICE_LOAD_METHOD_NAME);
              appendMockSource(b, varName, mockVarName, FormGenerator.SERVICE_STORE_METHOD_NAME);
            }));
  }

  protected static void appendMockSource(IMethodBodyBuilder<?> builder, String formDataVarName, String mockVarName, String methodToMock) {
    builder.refClassFrom(IScoutApi.class, IScoutApi::Mockito).dot().appendFrom(IScoutApi.class, api -> api.Mockito().whenMethodName()).parenthesisOpen().append(mockVarName).dot().append(methodToMock).parenthesisOpen()
        .refClassFrom(IScoutApi.class, IScoutApi::ArgumentMatchers).dot().appendFrom(IScoutApi.class, api -> api.ArgumentMatchers().anyMethodName()).parenthesisOpen().parenthesisClose().parenthesisClose().parenthesisClose()
        .dot().append("thenReturn").parenthesisOpen().append(formDataVarName).parenthesisClose().semicolon().nl();
  }

  protected IFuture<IType> createFormTest(IEnvironment env, IProgress progress) {
    var testSourceFolder = getClientTestSourceFolder();
    if (testSourceFolder == null) {
      return null;
    }
    var scoutApi = testSourceFolder.javaEnvironment().requireApi(IScoutApi.class);
    var formTestBuilder = createFormTestBuilder(scoutApi);

    setCreatedFormTestFqn(formTestBuilder.fullyQualifiedName());
    return env.writeCompilationUnitAsync(formTestBuilder, testSourceFolder, progress);
  }

  protected IFuture<IType> createServiceTest(IEnvironment env, IProgress progress) {
    var testSourceFolder = getServerTestSourceFolder();
    if (testSourceFolder == null) {
      return null;
    }
    var scoutApi = testSourceFolder.javaEnvironment().requireApi(IScoutApi.class);
    var serverPackage = ScoutTier.Client.convert(ScoutTier.Server, getClientPackage());
    var baseName = JavaTypes.simpleName(getCreatedServiceImplFqn());
    var elementName = baseName + ISdkConstants.SUFFIX_TEST;

    var existingServiceTest = testSourceFolder.javaEnvironment().findType(serverPackage + JavaTypes.C_DOT + elementName);
    if (existingServiceTest.isPresent()) {
      // service test class already exists
      return SdkFuture.completed(existingServiceTest.get());
    }

    TestGenerator<?> testBuilder = new TestGenerator<>()
        .withElementName(elementName)
        .withPackageName(serverPackage)
        .withRunner(scoutApi.ServerTestRunner().fqn())
        .asClientTest(false);
    if (getServerSession() != null) {
      testBuilder.withSession(getServerSession());
    }

    setCreatedServiceTestFqn(testBuilder.fullyQualifiedName());
    return env.writeCompilationUnitAsync(testBuilder, testSourceFolder, progress);
  }

  protected void createService(String sharedPackage, String baseName, IEnvironment env, IProgress progress) {
    var serviceNewOperation = new ServiceNewOperation();
    serviceNewOperation.setServiceName(baseName);
    serviceNewOperation.setSharedPackage(sharedPackage);
    serviceNewOperation.setSharedSourceFolder(getSharedSourceFolder());
    serviceNewOperation.setServerSourceFolder(getServerSourceFolder());

    // add service methods
    addServiceMethods(serviceNewOperation);

    serviceNewOperation.accept(env, progress);

    setCreatedServiceImpl(serviceNewOperation.getCreatedServiceImpl());
    setCreatedServiceImplFqn(serviceNewOperation.getCreatedServiceImplFqn());

    setCreatedServiceInterface(serviceNewOperation.getCreatedServiceInterface());
    setCreatedServiceInterfaceFqn(serviceNewOperation.getCreatedServiceInterfaceFqn());
  }

  protected void addServiceMethods(ServiceNewOperation serviceNewOperation) {
    serviceNewOperation.addMethod(createServiceMethod(FormGenerator.SERVICE_PREPARE_CREATE_METHOD_NAME));
    serviceNewOperation.addMethod(createServiceMethod(FormGenerator.SERVICE_CREATE_METHOD_NAME));
    serviceNewOperation.addMethod(createServiceMethod(FormGenerator.SERVICE_LOAD_METHOD_NAME));
    serviceNewOperation.addMethod(createServiceMethod(FormGenerator.SERVICE_STORE_METHOD_NAME));
  }

  protected IMethodGenerator<?, ?> createServiceMethod(String name) {
    IMethodGenerator<?, ?> methodBuilder = ScoutMethodGenerator.create()
        .asPublic()
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withElementName(name)
        .withBody(b -> {
          // permission check
          if (isCreatePermissions()) {
            var methodName = b.surroundingMethod().elementName().orElse(null);
            String permissionFqn;
            if (FormGenerator.SERVICE_LOAD_METHOD_NAME.equals(methodName)) {
              permissionFqn = getCreatedReadPermissionFqn();
            }
            else if (FormGenerator.SERVICE_STORE_METHOD_NAME.equals(methodName)) {
              permissionFqn = getCreatedUpdatePermissionFqn();
            }
            else {
              permissionFqn = getCreatedCreatePermissionFqn();
            }
            createPermissionCheckSource(b, permissionFqn);
          }

          createServiceMethodBody(b);
        });
    if (isCreateFormData()) {
      methodBuilder.withReturnType(getCreatedFormDataFqn());
      methodBuilder.withParameter(MethodParameterGenerator.create()
          .withElementName("formData")
          .withDataType(getCreatedFormDataFqn()));
    }
    else {
      methodBuilder.withReturnType(JavaTypes._void);
    }
    return methodBuilder;
  }

  protected void createPermissionCheckSource(IScoutMethodBodyBuilder<?> b, CharSequence permissionFqn) {
    b.appendPermissionCheck(permissionFqn);
  }

  protected void createServiceMethodBody(IScoutMethodBodyBuilder<?> builder) {
    builder.appendTodo("add business logic here.");
    if (!builder.needsReturnClause()) {
      return;
    }

    var returnType = builder.surroundingMethodReturnType().get();
    builder
        .returnClause()
        .append(getParamNameHavingDataType(builder.surroundingMethod(), returnType, builder.context())
            .orElseGet(() -> JavaTypes.defaultValueOf(returnType)))
        .semicolon();
  }

  protected static Optional<String> getParamNameHavingDataType(IMethodGenerator<?, ?> msb, String returnType, IJavaBuilderContext context) {
    var environment = context.environment().orElse(null);
    return msb.parameters()
        .filter(p -> returnType.equals(p.reference(environment)))
        .findAny()
        .flatMap(IMethodParameterGenerator::elementName);
  }

  protected IFuture<IType> createFormData(String sharedPackage, IEnvironment env, IProgress progress) {
    var formDataGenerator = PrimaryTypeGenerator.create()
        .withElementName(getFormName() + ISdkConstants.SUFFIX_DTO)
        .withPackageName(sharedPackage)
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractFormData().fqn());

    setCreatedFormDataFqn(formDataGenerator.fullyQualifiedName());
    return env.writeCompilationUnitAsync(formDataGenerator, getFormDataSourceFolder(), progress);
  }

  protected void createPermissions(String sharedPackage, String permissionBaseName, IEnvironment env, IProgress progress) {
    setCreatedReadPermission(createReadPermission(permissionBaseName, sharedPackage, env, progress.newChild(1)));
    setCreatedUpdatePermission(createUpdatePermission(permissionBaseName, sharedPackage, env, progress.newChild(1)));
    setCreatedCreatePermission(createCreatePermission(permissionBaseName, sharedPackage, env, progress.newChild(1)));
  }

  protected IFuture<IType> createPermission(String permissionName, String sharedPackage, Consumer<String> fqnConsumer, IEnvironment env, IProgress progress) {
    PermissionGenerator<?> psb = new PermissionGenerator<>()
        .withElementName(permissionName)
        .withPackageName(sharedPackage);
    if (fqnConsumer != null) {
      fqnConsumer.accept(psb.fullyQualifiedName());
    }
    return env.writeCompilationUnitAsync(psb, getSharedSourceFolder(), progress);
  }

  protected IFuture<IType> createCreatePermission(String permissionBaseName, String sharedPackage, IEnvironment env, IProgress progress) {
    return createPermission("Create" + permissionBaseName, sharedPackage, this::setCreatedCreatePermissionFqn, env, progress);
  }

  protected IFuture<IType> createReadPermission(String permissionBaseName, String sharedPackage, IEnvironment env, IProgress progress) {
    return createPermission("Read" + permissionBaseName, sharedPackage, this::setCreatedReadPermissionFqn, env, progress);
  }

  protected IFuture<IType> createUpdatePermission(String permissionBaseName, String sharedPackage, IEnvironment env, IProgress progress) {
    return createPermission("Update" + permissionBaseName, sharedPackage, this::setCreatedUpdatePermissionFqn, env, progress);
  }

  protected IFuture<IType> createForm(IEnvironment env, IProgress progress) {
    var formBuilder = createFormBuilder();

    setCreatedFormFqn(formBuilder.fullyQualifiedName());
    return env.writeCompilationUnitAsync(formBuilder, getClientSourceFolder(), progress);
  }

  protected FormGenerator<?> createFormBuilderInstance() {
    return new FormGenerator<>();
  }

  protected FormGenerator<?> createFormBuilder() {
    var formBuilder = createFormBuilderInstance()
        .withSuperClass(getSuperType())
        .withElementName(getFormName())
        .withPackageName(getClientPackage())
        .withFormFields(getAttributes());
    if (isCreateFormData()) {
      formBuilder.withFormData(getCreatedFormDataFqn());
    }
    if (isCreateService()) {
      formBuilder.withServiceInterface(getCreatedServiceInterfaceFqn());
    }
    if (isCreatePermissions()) {
      formBuilder.withPermissionUpdate(getCreatedUpdatePermissionFqn());
      formBuilder.withPermissionCreate(getCreatedCreatePermissionFqn());
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

  public List<String> getAttributes() {
    return m_attributes;
  }

  public void setAttributes(List<String> attributes) {
    m_attributes = attributes;
  }

  public IFuture<IType> getCreatedForm() {
    return m_createdForm;
  }

  protected void setCreatedForm(IFuture<IType> createdForm) {
    m_createdForm = createdForm;
  }

  public String getCreatedFormFqn() {
    return m_createdFormFqn;
  }

  protected void setCreatedFormFqn(String createdFormFqn) {
    m_createdFormFqn = createdFormFqn;
  }

  public IFuture<IType> getCreatedFormData() {
    return m_createdFormData;
  }

  protected void setCreatedFormData(IFuture<IType> createdFormData) {
    m_createdFormData = createdFormData;
  }

  public String getCreatedFormDataFqn() {
    return m_createdFormDataFqn;
  }

  protected void setCreatedFormDataFqn(String createdFormDataFqn) {
    m_createdFormDataFqn = createdFormDataFqn;
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

  protected void setCreatedServiceImpl(IFuture<IType> createdServiceImpl) {
    m_createdServiceImpl = createdServiceImpl;
  }

  public String getCreatedServiceImplFqn() {
    return m_createdServiceImplFqn;
  }

  protected void setCreatedServiceImplFqn(String createdServiceImplFqn) {
    m_createdServiceImplFqn = createdServiceImplFqn;
  }

  public IFuture<IType> getCreatedReadPermission() {
    return m_createdReadPermission;
  }

  protected void setCreatedReadPermission(IFuture<IType> createdReadPermission) {
    m_createdReadPermission = createdReadPermission;
  }

  public String getCreatedReadPermissionFqn() {
    return m_createdReadPermissionFqn;
  }

  protected void setCreatedReadPermissionFqn(String createdReadPermissionFqn) {
    m_createdReadPermissionFqn = createdReadPermissionFqn;
  }

  public IFuture<IType> getCreatedUpdatePermission() {
    return m_createdUpdatePermission;
  }

  protected void setCreatedUpdatePermission(IFuture<IType> createdUpdatePermission) {
    m_createdUpdatePermission = createdUpdatePermission;
  }

  public String getCreatedUpdatePermissionFqn() {
    return m_createdUpdatePermissionFqn;
  }

  protected void setCreatedUpdatePermissionFqn(String createdUpdatePermissionFqn) {
    m_createdUpdatePermissionFqn = createdUpdatePermissionFqn;
  }

  public IFuture<IType> getCreatedCreatePermission() {
    return m_createdCreatePermission;
  }

  protected void setCreatedCreatePermission(IFuture<IType> createdCreatePermission) {
    m_createdCreatePermission = createdCreatePermission;
  }

  public String getCreatedCreatePermissionFqn() {
    return m_createdCreatePermissionFqn;
  }

  protected void setCreatedCreatePermissionFqn(String createdCreatePermissionFqn) {
    m_createdCreatePermissionFqn = createdCreatePermissionFqn;
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

  public IFuture<IType> getCreatedFormTest() {
    return m_createdFormTest;
  }

  protected void setCreatedFormTest(IFuture<IType> createdFormTest) {
    m_createdFormTest = createdFormTest;
  }

  public String getCreatedFormTestFqn() {
    return m_createdFormTestFqn;
  }

  protected void setCreatedFormTestFqn(String createdFormTestFqn) {
    m_createdFormTestFqn = createdFormTestFqn;
  }

  public IFuture<IType> getCreatedServiceTest() {
    return m_createdServiceTest;
  }

  protected void setCreatedServiceTest(IFuture<IType> createdServiceTest) {
    m_createdServiceTest = createdServiceTest;
  }

  public String getCreatedServiceTestFqn() {
    return m_createdServiceTestFqn;
  }

  protected void setCreatedServiceTestFqn(String createdServiceTestFqn) {
    m_createdServiceTestFqn = createdServiceTestFqn;
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
