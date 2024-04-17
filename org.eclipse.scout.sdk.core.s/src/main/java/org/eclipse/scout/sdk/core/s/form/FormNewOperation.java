/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.form;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.java.builder.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.dto.DtoGeneratorFactory;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutVariousApi;
import org.eclipse.scout.sdk.core.s.java.builder.body.IScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.s.java.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.java.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.s.permission.PermissionGenerator;
import org.eclipse.scout.sdk.core.s.service.ServiceNewOperation;
import org.eclipse.scout.sdk.core.s.testcase.TestGenerator;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link FormNewOperation}</h3>
 *
 * @since 7.0.0
 */
public class FormNewOperation implements BiConsumer<IEnvironment, IProgress> {

  // in
  private String m_formName;
  private FinalValue<String> m_baseName = new FinalValue<>();
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
  private boolean m_createOrAppendService;
  private boolean m_createPermissions;
  private List<String> m_attributes; // optional
  private ServiceNewOperation m_serviceNewOperation; // optional

  // out
  private IFuture<IType> m_createdForm;
  private String m_createdFormFqn;

  private IFuture<IType> m_createdFormData;
  private String m_createdFormDataFqn;

  private IFuture<IType> m_createdServiceInterface;
  private String m_serviceInterfaceFqn;

  private IFuture<IType> m_createdServiceImpl;
  private String m_serviceImplFqn;

  private IFuture<IType> m_createdReadPermission;
  private String m_createdReadPermissionFqn;

  private IFuture<IType> m_createdUpdatePermission;
  private String m_createdUpdatePermissionFqn;

  private IFuture<IType> m_createdCreatePermission;
  private String m_createdCreatePermissionFqn;

  private IFuture<IType> m_createdFormTest;
  private String m_createdFormTestFqn;

  private IFuture<IType> m_createdServiceTest;
  private String m_serviceTestFqn;

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

    // DTO
    if (isCreateFormData()) {
      setCreatedFormData(createFormData(sharedPackage, env, progress.newChild(1)));
    }

    // permissions
    if (isCreatePermissions()) {
      createPermissions(sharedPackage, getBaseName() + ISdkConstants.SUFFIX_PERMISSION, env, progress);
    }

    // Service
    if (isCreateOrAppendService()) {
      createOrAppendService(sharedPackage, env, progress);
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

  protected String getBaseName() {
    return m_baseName.computeIfAbsentAndGet(this::calcFormBaseName);
  }

  protected String calcFormBaseName() {
    return Strings.removeSuffix(getFormName(), ISdkConstants.SUFFIX_FORM);
  }

  public String getServiceBaseName() {
    return getBaseName();
  }

  protected void updateFormData(IEnvironment env, IProgress progress) {
    // the DtoGeneratorFactory parses the @FormData-Annotation and uses the given formData-IType, therefore the formData needs to be created already
    getCreatedFormData().result();
    var formDataGenerator = DtoGeneratorFactory.createFormDataGenerator(getCreatedForm().result(), getFormDataSourceFolder().javaEnvironment());
    env.writeCompilationUnitAsync(formDataGenerator.orElseThrow(), getFormDataSourceFolder(), progress);
  }

  protected TestGenerator<?> createFormTestBuilder(IScoutVariousApi scoutApi) {
    var testBuilder = new TestGenerator<>()
        .withElementName(JavaTypes.simpleName(getCreatedFormFqn()) + ISdkConstants.SUFFIX_TEST)
        .withPackageName(getClientPackage())
        .withRunner(scoutApi.ClientTestRunner().fqn())
        .asClientTest(true);
    if (isCreateOrAppendService() && isCreateFormData()) {
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
            .withDataType(getServiceInterfaceFqn())
            .withAnnotation(ScoutAnnotationGenerator.createBeanMock()))
        .withMethod(MethodGenerator.create()
            .withElementName("setup")
            .asPublic()
            .withReturnType(JavaTypes._void)
            .withAnnotation(ScoutAnnotationGenerator.createBefore())
            .withBody(b -> {
              var varName = "answer";
              var formDataName = getCreatedFormDataFqn();
              b.ref(formDataName).space().append(varName).equalSign().appendNew(formDataName).parenthesisClose().semicolon().nl();
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

  protected void createOrAppendService(String sharedPackage, IEnvironment env, IProgress progress) {
    if (isCreateService()) {
      createService(sharedPackage, getServiceBaseName(), env, progress.newChild(3));
    }
    else if (isAppendService()) {
      var op = getServiceNewOperation();
      addServiceMethods(op);

      var svcName = op.getServiceName() + ISdkConstants.SUFFIX_SERVICE;
      var serverPackage = ScoutTier.Shared.convert(ScoutTier.Server, op.getSharedPackage());
      setServiceInterfaceFqn(sharedPackage + JavaTypes.C_DOT + "I" + svcName);
      setServiceImplFqn(serverPackage + JavaTypes.C_DOT + svcName);

      if (op.isCreateTest()) {
        setServiceTestFqn(getServiceImplFqn() + ISdkConstants.SUFFIX_TEST);
      }
    }
  }

  protected void createService(String sharedPackage, String baseName, IEnvironment env, IProgress progress) {
    var serviceNewOperation = new ServiceNewOperation();
    prepareServiceOperation(serviceNewOperation, sharedPackage, baseName);

    // add service methods
    addServiceMethods(serviceNewOperation);

    serviceNewOperation.accept(env, progress);

    setCreatedServiceImpl(serviceNewOperation.getCreatedServiceImpl());
    setServiceImplFqn(serviceNewOperation.getCreatedServiceImplFqn());

    setCreatedServiceInterface(serviceNewOperation.getCreatedServiceInterface());
    setServiceInterfaceFqn(serviceNewOperation.getCreatedServiceInterfaceFqn());

    setCreatedServiceTest(serviceNewOperation.getCreatedServiceTest());
    setServiceTestFqn(serviceNewOperation.getCreatedServiceTestFqn());
  }

  protected void prepareServiceOperation(ServiceNewOperation serviceNewOperation, String sharedPackage, String baseName) {
    serviceNewOperation.setServiceName(baseName);
    serviceNewOperation.setSharedPackage(sharedPackage);
    serviceNewOperation.setSharedSourceFolder(getSharedSourceFolder());
    serviceNewOperation.setServerSourceFolder(getServerSourceFolder());
    if (getServerTestSourceFolder() == null) {
      return;
    }
    serviceNewOperation.setTestSourceFolder(getServerTestSourceFolder());
    serviceNewOperation.setServerSession(getServerSession());
    serviceNewOperation.setCreateTest(true);
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
        .withFlags(Flags.AccInterface) // also add the method in the service interface
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withElementName(name)
        .withBody(b -> {
          // permission check
          if (isCreatePermissions()) {
            var methodName = b.surroundingMethod().elementName(b.context()).orElse(null);
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

    var returnType = builder.surroundingMethodReturnType().orElseThrow();
    builder
        .returnClause()
        .append(getParamNameHavingDataType(builder.surroundingMethod(), returnType, builder.context())
            .orElseGet(() -> JavaTypes.defaultValueOf(returnType)))
        .semicolon();
  }

  protected static Optional<String> getParamNameHavingDataType(IMethodGenerator<?, ?> msb, String returnType, IJavaBuilderContext context) {
    return msb.parameters()
        .filter(p -> returnType.equals(p.reference(context)))
        .findAny()
        .flatMap(param -> param.elementName(context));
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
    var psb = new PermissionGenerator()
        .withElementName(permissionName)
        .withPackageName(sharedPackage);
    if (fqnConsumer != null) {
      fqnConsumer.accept(psb.fullyQualifiedName());
    }
    return env.writeCompilationUnitAsync(psb, getSharedSourceFolder(), progress);
  }

  protected IFuture<IType> createCreatePermission(String permissionBaseName, String sharedPackage, IEnvironment env, IProgress progress) {
    return createPermission(ISdkConstants.PREFIX_CREATE_PERMISSION + permissionBaseName, sharedPackage, this::setCreatedCreatePermissionFqn, env, progress);
  }

  protected IFuture<IType> createReadPermission(String permissionBaseName, String sharedPackage, IEnvironment env, IProgress progress) {
    return createPermission(ISdkConstants.PREFIX_READ_PERMISSION + permissionBaseName, sharedPackage, this::setCreatedReadPermissionFqn, env, progress);
  }

  protected IFuture<IType> createUpdatePermission(String permissionBaseName, String sharedPackage, IEnvironment env, IProgress progress) {
    return createPermission(ISdkConstants.PREFIX_UPDATE_PERMISSION + permissionBaseName, sharedPackage, this::setCreatedUpdatePermissionFqn, env, progress);
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
    if (isCreateOrAppendService()) {
      formBuilder.withServiceInterface(getServiceInterfaceFqn());
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
    m_baseName = new FinalValue<>();
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

  public boolean isCreateOrAppendService() {
    return m_createOrAppendService;
  }

  public void setCreateOrAppendService(boolean createOrAppendService) {
    m_createOrAppendService = createOrAppendService;
  }

  public boolean isCreateService() {
    return isCreateOrAppendService() && getServiceNewOperation() == null;
  }

  public boolean isAppendService() {
    return isCreateOrAppendService() && getServiceNewOperation() != null;
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

  public ServiceNewOperation getServiceNewOperation() {
    return m_serviceNewOperation;
  }

  public void setServiceNewOperation(ServiceNewOperation serviceNewOperation) {
    m_serviceNewOperation = serviceNewOperation;
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

  public String getServiceInterfaceFqn() {
    return m_serviceInterfaceFqn;
  }

  protected void setServiceInterfaceFqn(String serviceInterfaceFqn) {
    m_serviceInterfaceFqn = serviceInterfaceFqn;
  }

  public IFuture<IType> getCreatedServiceImpl() {
    return m_createdServiceImpl;
  }

  protected void setCreatedServiceImpl(IFuture<IType> createdServiceImpl) {
    m_createdServiceImpl = createdServiceImpl;
  }

  public String getServiceImplFqn() {
    return m_serviceImplFqn;
  }

  protected void setServiceImplFqn(String serviceImplFqn) {
    m_serviceImplFqn = serviceImplFqn;
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

  public String getServiceTestFqn() {
    return m_serviceTestFqn;
  }

  protected void setServiceTestFqn(String serviceTestFqn) {
    m_serviceTestFqn = serviceTestFqn;
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
