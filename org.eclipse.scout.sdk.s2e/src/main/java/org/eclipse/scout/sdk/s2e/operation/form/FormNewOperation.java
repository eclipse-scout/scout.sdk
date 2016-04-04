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
package org.eclipse.scout.sdk.s2e.operation.form;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.s.sourcebuilder.form.FormSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.permission.PermissionSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.testcase.TestSourceBuilder;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.IMethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.MethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerationContext;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerators;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;

/**
 * <h3>{@link FormNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class FormNewOperation implements IOperation {

  private final IJavaEnvironmentProvider m_javaEnvironmentProvider;
  private static final String TEXT_AUTHORIZATION_FAILED = "AuthorizationFailed";

  // in
  private String m_formName;
  private IPackageFragmentRoot m_clientSourceFolder;
  private IPackageFragmentRoot m_sharedSourceFolder;
  private IPackageFragmentRoot m_serverSourceFolder;
  private IPackageFragmentRoot m_formDataSourceFolder;
  private IPackageFragmentRoot m_clientTestSourceFolder;
  private IPackageFragmentRoot m_serverTestSourceFolder;
  private String m_clientPackage;
  private IType m_superType;
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

  public FormNewOperation() {
    this(new CachingJavaEnvironmentProvider());
  }

  protected FormNewOperation(IJavaEnvironmentProvider provider) {
    m_javaEnvironmentProvider = provider;
  }

  @Override
  public String getOperationName() {
    return "Create Form '" + getFormName() + "'.";
  }

  @Override
  public void validate() {
    Validate.isTrue(StringUtils.isNotBlank(getFormName()), "No form name provided");
    Validate.isTrue(S2eUtils.exists(getClientSourceFolder()), "No client source folder provided");
    if (isCreateService()) {
      Validate.isTrue(S2eUtils.exists(getServerSourceFolder()), "No server source folder provided");
    }
    if (isCreateService() || isCreateFormData() || isCreatePermissions()) {
      Validate.isTrue(S2eUtils.exists(getSharedSourceFolder()), "No shared source folder provided");
    }
    Validate.isTrue(StringUtils.isNotBlank(getClientPackage()), "No package provided");
    Validate.isTrue(S2eUtils.exists(getSuperType()), "Super type does not exist");
  }

  protected int getTotalWork() {
    int result = 2; // form & form-tests
    if (isCreateFormData()) {
      result++;
    }
    if (isCreateService()) {
      result += 3; // ifc, impl, test
    }
    if (isCreatePermissions()) {
      result += 3; // create, read, update
    }
    return result;
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), getTotalWork());

    // calc names
    String sharedPackage = ScoutTier.Client.convert(ScoutTier.Shared, getClientPackage());
    String baseName = getFormName();
    if (baseName.endsWith(ISdkProperties.SUFFIX_FORM)) {
      baseName = baseName.substring(0, baseName.length() - ISdkProperties.SUFFIX_FORM.length());
    }

    // DTO
    if (isCreateFormData()) {
      setCreatedFormData(createFormData(sharedPackage, progress.newChild(1), workingCopyManager));
    }

    // permissions
    if (isCreatePermissions()) {
      String permissionBaseName = baseName + ISdkProperties.SUFFIX_PERMISSION;
      setCreatedReadPermission(createReadPermission(permissionBaseName, sharedPackage, progress.newChild(1), workingCopyManager));
      setCreatedUpdatePermission(createUpdatePermission(permissionBaseName, sharedPackage, progress.newChild(1), workingCopyManager));
      setCreatedCreatePermission(createCreatePermission(permissionBaseName, sharedPackage, progress.newChild(1), workingCopyManager));
    }

    // Service
    if (isCreateService()) {
      createService(sharedPackage, baseName, progress.newChild(2), workingCopyManager);

      // service test
      setCreatedServiceTest(createServiceTest(progress.newChild(1), workingCopyManager));
    }

    // form
    setCreatedForm(createForm(progress.newChild(1), workingCopyManager));

    // form test
    setCreatedFormTest(createFormTest(progress.newChild(1), workingCopyManager));

    // schedule DTO update because the formData has been created as empty java file
    if (isCreateFormData()) {
      ScoutSdkCore.getDerivedResourceManager().trigger(Collections.singleton(getCreatedForm().getResource()));
    }
  }

  protected TestSourceBuilder createFormTestBuilder(IJavaEnvironment env) {
    TestSourceBuilder testBuilder = new TestSourceBuilder(getCreatedForm().getElementName() + ISdkProperties.SUFFIX_TEST, getClientPackage(), env);
    testBuilder.setRunnerSignature(Signature.createTypeSignature(IScoutRuntimeTypes.ClientTestRunner));
    testBuilder.setClientTest(true);
    testBuilder.setup();

    if (isCreateService() && isCreateFormData()) {
      // prepare mock
      addMock(testBuilder.getMainType());
    }
    return testBuilder;
  }

  protected void addMock(ITypeSourceBuilder testBuilder) {
    final String mockVarName = "m_mockSvc";
    IFieldSourceBuilder mockSvc = new FieldSourceBuilder(mockVarName);
    mockSvc.setFlags(Flags.AccPrivate);
    mockSvc.setSignature(Signature.createTypeSignature(getCreatedServiceInterface().getFullyQualifiedName()));
    mockSvc.addAnnotation(ScoutAnnotationSourceBuilderFactory.createBeanMock());
    testBuilder.addField(mockSvc);

    IMethodSourceBuilder setup = new MethodSourceBuilder("setup");
    setup.setFlags(Flags.AccPublic);
    setup.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    setup.addAnnotation(ScoutAnnotationSourceBuilderFactory.createBefore());
    setup.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        String varName = "answer";
        String formDataRef = validator.useName(getCreatedFormData().getFullyQualifiedName());
        source.append(formDataRef).append(' ').append(varName).append(" = new ").append(formDataRef).append("();").append(lineDelimiter);
        appendMockSource(varName, FormSourceBuilder.SERVICE_PREPARECREATE_METHOD_NAME, source, validator);
        appendMockSource(varName, FormSourceBuilder.SERVICE_CREATE_METHOD_NAME, source, validator);
        appendMockSource(varName, FormSourceBuilder.SERVICE_LOAD_METHOD_NAME, source, validator);
        appendMockSource(varName, FormSourceBuilder.SERVICE_STORE_METHOD_NAME, source, validator);
      }

      protected void appendMockSource(String varName, String methodToMock, StringBuilder source, IImportValidator validator) {
        source.append(validator.useName(IScoutRuntimeTypes.Mockito)).append(".when(").append(mockVarName).append('.').append(methodToMock).append('(')
            .append(validator.useName(IScoutRuntimeTypes.Matchers)).append(".any(")
            .append(validator.useName(getCreatedFormData().getFullyQualifiedName())).append(SuffixConstants.SUFFIX_class).append("))).thenReturn(").append(varName).append(");");
      }
    });

    testBuilder.addMethod(setup);
  }

  protected IType createFormTest(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    IPackageFragmentRoot testSourceFolder = getClientTestSourceFolder();
    if (!S2eUtils.exists(testSourceFolder)) {
      return null;
    }

    IJavaEnvironment env = getEnvProvider().get(testSourceFolder.getJavaProject());
    TestSourceBuilder formTestBuilder = createFormTestBuilder(env);

    return S2eUtils.writeType(testSourceFolder, formTestBuilder, env, monitor, workingCopyManager);
  }

  protected IType createServiceTest(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IPackageFragmentRoot testSourceFolder = getServerTestSourceFolder();
    if (!S2eUtils.exists(testSourceFolder)) {
      return null;
    }
    String serverPackage = ScoutTier.Client.convert(ScoutTier.Server, getClientPackage());
    String baseName = getCreatedServiceImpl().getElementName();
    String elementName = baseName + ISdkProperties.SUFFIX_TEST;

    IType existingServiceTest = testSourceFolder.getJavaProject().findType(serverPackage, elementName);
    if (S2eUtils.exists(existingServiceTest)) {
      // service test class already exists
      return existingServiceTest;
    }

    IJavaEnvironment env = getEnvProvider().get(testSourceFolder.getJavaProject());
    TestSourceBuilder testBuilder = new TestSourceBuilder(elementName, serverPackage, env);
    testBuilder.setRunnerSignature(Signature.createTypeSignature(IScoutRuntimeTypes.ServerTestRunner));
    testBuilder.setClientTest(false);
    IType session = S2eUtils.getSession(getServerSourceFolder().getJavaProject(), ScoutTier.Server, monitor);
    if (S2eUtils.exists(session)) {
      testBuilder.setSessionSignature(Signature.createTypeSignature(session.getFullyQualifiedName()));
    }
    testBuilder.setup();

    return S2eUtils.writeType(testSourceFolder, testBuilder, env, monitor, workingCopyManager);
  }

  protected void createService(String sharedPackage, String baseName, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ServiceNewOperation serviceNewOperation = new ServiceNewOperation(getEnvProvider());
    serviceNewOperation.setServiceName(baseName);
    serviceNewOperation.setSharedPackage(sharedPackage);
    serviceNewOperation.setSharedSourceFolder(getSharedSourceFolder());
    serviceNewOperation.setServerSourceFolder(getServerSourceFolder());

    // add service methods
    serviceNewOperation.addMethod(createServiceMethod(FormSourceBuilder.SERVICE_PREPARECREATE_METHOD_NAME));
    serviceNewOperation.addMethod(createServiceMethod(FormSourceBuilder.SERVICE_CREATE_METHOD_NAME));
    serviceNewOperation.addMethod(createServiceMethod(FormSourceBuilder.SERVICE_LOAD_METHOD_NAME));
    serviceNewOperation.addMethod(createServiceMethod(FormSourceBuilder.SERVICE_STORE_METHOD_NAME));

    serviceNewOperation.validate();
    serviceNewOperation.run(monitor, workingCopyManager);

    setCreatedServiceImpl(serviceNewOperation.getCreatedServiceImpl());
    setCreatedServiceInterface(serviceNewOperation.getCreatedServiceInterface());
  }

  protected IMethodSourceBuilder createServiceMethod(String name) {
    final IMethodSourceBuilder methodBuilder = new MethodSourceBuilder(name);
    methodBuilder.setFlags(Flags.AccPublic);
    methodBuilder.setComment(CommentSourceBuilderFactory.createDefaultMethodComment(methodBuilder));
    if (isCreateFormData()) {
      String formDataSig = Signature.createTypeSignature(getCreatedFormData().getFullyQualifiedName());
      methodBuilder.setReturnTypeSignature(formDataSig);
      methodBuilder.addParameter(new MethodParameterSourceBuilder("input", formDataSig));
    }
    else {
      methodBuilder.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    }
    methodBuilder.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        if (isCreatePermissions()) {
          // permission check
          String permissionSig = null;

          if (FormSourceBuilder.SERVICE_LOAD_METHOD_NAME.equals(methodBuilder.getElementName())) {
            permissionSig = Signature.createTypeSignature(getCreatedReadPermission().getFullyQualifiedName());
          }
          else if (FormSourceBuilder.SERVICE_STORE_METHOD_NAME.equals(methodBuilder.getElementName())) {
            permissionSig = Signature.createTypeSignature(getCreatedUpdatePermission().getFullyQualifiedName());
          }
          else {
            permissionSig = Signature.createTypeSignature(getCreatedCreatePermission().getFullyQualifiedName());
          }

          createPermissionCheckSource(source, lineDelimiter, validator, permissionSig);
        }

        createServiceMethodBody(source, lineDelimiter, validator, methodBuilder);
      }
    });
    return methodBuilder;
  }

  protected void createPermissionCheckSource(StringBuilder source, String lineDelimiter, IImportValidator validator, String permissionSig) {
    source.append("if(!").append(validator.useName(IScoutRuntimeTypes.ACCESS));
    source.append(".check(new ").append(validator.useSignature(permissionSig)).append("())) {").append(lineDelimiter);

    source.append("  throw new ").append(validator.useName(IScoutRuntimeTypes.VetoException)).append('(');
    source.append(validator.useName(IScoutRuntimeTypes.TEXTS));
    source.append(".get(").append(CoreUtils.toStringLiteral(TEXT_AUTHORIZATION_FAILED)).append(')');
    source.append(");").append(lineDelimiter);

    source.append('}').append(lineDelimiter);
  }

  /**
   * @param source
   * @param lineDelimiter
   * @param validator
   * @param parentMethod
   */
  protected void createServiceMethodBody(StringBuilder source, String lineDelimiter, IImportValidator validator, IMethodSourceBuilder parentMethod) {
    // add todo
    source.append(CoreUtils.getCommentBlock("add business logic here.")).append(lineDelimiter);

    // return clause
    String paramToReturn = getParamNameOfReturnType(parentMethod);
    String returnSig = parentMethod.getReturnTypeSignature();

    if (paramToReturn == null) {
      String returnValue = CoreUtils.getDefaultValueOf(returnSig);
      if (returnValue != null) {
        source.append("return ").append(returnValue).append(';');
      }
    }
    else {
      source.append("return ").append(paramToReturn).append(';');
    }
  }

  protected String getParamNameOfReturnType(IMethodSourceBuilder msb) {
    if (msb.getReturnTypeSignature() == null || ISignatureConstants.SIG_VOID.equals(msb.getReturnTypeSignature())) {
      return null;
    }
    for (IMethodParameterSourceBuilder mpsb : msb.getParameters()) {
      if (msb.getReturnTypeSignature().equals(mpsb.getDataTypeSignature())) {
        return mpsb.getElementName();
      }
    }
    return null;
  }

  protected IType createFormData(String sharedPackage, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    String formDataName = getFormName() + ISdkProperties.SUFFIX_DTO;

    ICompilationUnitSourceBuilder formDataBuilder = new CompilationUnitSourceBuilder(formDataName + SuffixConstants.SUFFIX_STRING_java, sharedPackage);
    ITypeSourceBuilder formDataTypeBuilder = new TypeSourceBuilder(formDataName);
    formDataTypeBuilder.setFlags(Flags.AccPublic);
    formDataTypeBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractFormData));
    formDataBuilder.addType(formDataTypeBuilder);

    return S2eUtils.writeType(getFormDataSourceFolder(), formDataBuilder, getEnvProvider().get(getFormDataSourceFolder().getJavaProject()), monitor, workingCopyManager);
  }

  protected IType createPermission(String permissionName, String sharedPackage, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    IJavaEnvironment env = getEnvProvider().get(getSharedSourceFolder().getJavaProject());
    PermissionSourceBuilder psb = new PermissionSourceBuilder(permissionName, sharedPackage, env);
    psb.setup();
    return S2eUtils.writeType(getSharedSourceFolder(), psb, env, monitor, workingCopyManager);
  }

  protected IType createCreatePermission(String permissionBaseName, String sharedPackage, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    return createPermission("Create" + permissionBaseName, sharedPackage, monitor, workingCopyManager);
  }

  protected IType createReadPermission(String permissionBaseName, String sharedPackage, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    return createPermission("Read" + permissionBaseName, sharedPackage, monitor, workingCopyManager);
  }

  protected IType createUpdatePermission(String permissionBaseName, String sharedPackage, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    return createPermission("Update" + permissionBaseName, sharedPackage, monitor, workingCopyManager);
  }

  protected IType createForm(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    FormSourceBuilder formBuilder = createFormBuilder();
    return S2eUtils.writeType(getClientSourceFolder(), formBuilder, getEnvProvider().get(getClientSourceFolder().getJavaProject()), monitor, workingCopyManager);
  }

  protected FormSourceBuilder createFormBuilderInstance() {
    return new FormSourceBuilder(getFormName(), getClientPackage(), getEnvProvider().get(getClientSourceFolder().getJavaProject()));
  }

  protected FormSourceBuilder createFormBuilder() {
    FormSourceBuilder formBuilder = createFormBuilderInstance();
    formBuilder.setSuperTypeSignature(Signature.createTypeSignature(getSuperType().getFullyQualifiedName()));
    if (isCreateFormData()) {
      formBuilder.setFormDataSignature(Signature.createTypeSignature(getCreatedFormData().getFullyQualifiedName()));
    }
    if (isCreateService()) {
      formBuilder.setServiceIfcSignature(Signature.createTypeSignature(getCreatedServiceInterface().getFullyQualifiedName()));
    }
    if (isCreatePermissions()) {
      formBuilder.setUpdatePermissionSignature(Signature.createTypeSignature(getCreatedUpdatePermission().getFullyQualifiedName()));
      formBuilder.setCreatePermissionSignature(Signature.createTypeSignature(getCreatedCreatePermission().getFullyQualifiedName()));
    }

    // @ClassId
    if (ClassIdGenerators.isAutomaticallyCreateClassIdAnnotation()) {
      String[] classIds = new String[FormSourceBuilder.NUM_CLASS_IDS];
      ClassIdGenerationContext context = new ClassIdGenerationContext(getClientPackage() + '.' + getFormName());
      for (int i = 0; i < classIds.length; i++) {
        classIds[i] = ClassIdGenerators.generateNewId(context);
      }
      formBuilder.setClassIdValues(classIds);
    }
    formBuilder.setup();
    return formBuilder;
  }

  public String getFormName() {
    return m_formName;
  }

  public void setFormName(String formName) {
    m_formName = formName;
  }

  public IPackageFragmentRoot getClientSourceFolder() {
    return m_clientSourceFolder;
  }

  public void setClientSourceFolder(IPackageFragmentRoot clientSourceFolder) {
    m_clientSourceFolder = clientSourceFolder;
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

  public String getClientPackage() {
    return m_clientPackage;
  }

  public void setClientPackage(String clientPackage) {
    m_clientPackage = clientPackage;
  }

  public IType getSuperType() {
    return m_superType;
  }

  public void setSuperType(IType superType) {
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

  public IPackageFragmentRoot getFormDataSourceFolder() {
    return m_formDataSourceFolder;
  }

  public void setFormDataSourceFolder(IPackageFragmentRoot formDataSourceFolder) {
    m_formDataSourceFolder = formDataSourceFolder;
  }

  protected IJavaEnvironmentProvider getEnvProvider() {
    return m_javaEnvironmentProvider;
  }

  public IPackageFragmentRoot getClientTestSourceFolder() {
    return m_clientTestSourceFolder;
  }

  public void setClientTestSourceFolder(IPackageFragmentRoot clientTestSourceFolder) {
    m_clientTestSourceFolder = clientTestSourceFolder;
  }

  public IPackageFragmentRoot getServerTestSourceFolder() {
    return m_serverTestSourceFolder;
  }

  public void setServerTestSourceFolder(IPackageFragmentRoot serverTestSourceFolder) {
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
}
