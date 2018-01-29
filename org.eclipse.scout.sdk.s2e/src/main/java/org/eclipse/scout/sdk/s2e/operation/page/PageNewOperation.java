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
package org.eclipse.scout.sdk.s2e.operation.page;

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
import org.eclipse.scout.sdk.core.s.sourcebuilder.page.PageSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.testcase.TestSourceBuilder;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.MethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerationContext;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerators;
import org.eclipse.scout.sdk.s2e.internal.dto.DtoDerivedResourceHandler;
import org.eclipse.scout.sdk.s2e.operation.CompilationUnitWriteOperation;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;

/**
 * <h3>{@link PageNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PageNewOperation implements IOperation {

  private final IJavaEnvironmentProvider m_javaEnvironmentProvider;

  // in
  private String m_pageName;
  private IPackageFragmentRoot m_clientSourceFolder;
  private IPackageFragmentRoot m_pageDataSourceFolder;
  private IPackageFragmentRoot m_sharedSourceFolder;
  private IPackageFragmentRoot m_serverSourceFolder;
  private IPackageFragmentRoot m_testSourceFolder;
  private String m_package;
  private IType m_superType;
  private boolean m_createAbstractPage;

  // out
  private IType m_createdAbstractPage;
  private IType m_createdPage;
  private IType m_createdPageData;
  private IType m_createdAbstractPageData;
  private IType m_createdServiceIfc;
  private IType m_createdServiceImpl;
  private IType m_createdServiceTest;

  private String m_dataFetchMethodName;

  public PageNewOperation() {
    this(new CachingJavaEnvironmentProvider());
  }

  protected PageNewOperation(IJavaEnvironmentProvider provider) {
    m_javaEnvironmentProvider = Validate.notNull(provider);
  }

  @Override
  public String getOperationName() {
    return "Create Page '" + getPageName() + "'.";
  }

  @Override
  public void validate() {
    Validate.isTrue(StringUtils.isNotBlank(getPageName()), "No page name provided");
    Validate.isTrue(S2eUtils.exists(getClientSourceFolder()), "No client source folder provided");
    Validate.isTrue(StringUtils.isNotBlank(getPackage()), "No package name provided");
    Validate.isTrue(S2eUtils.exists(getSuperType()), "No supertype provided");
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 10);

    String sharedPackage = ScoutTier.Client.convert(ScoutTier.Shared, getPackage());

    boolean isPageWithTable = S2eUtils.hierarchyContains(getSuperType().newSupertypeHierarchy(progress.newChild(1)), IScoutRuntimeTypes.IPageWithTable);
    boolean isCreatePageData = isPageWithTable && S2eUtils.exists(getPageDataSourceFolder());

    if (isCreatePageData) {
      setCreatedPageData(createPageData(getPageName(), sharedPackage, progress.newChild(1), workingCopyManager));
    }

    boolean isCreateService = isCreatePageData && S2eUtils.exists(getSharedSourceFolder()) && S2eUtils.exists(getServerSourceFolder());
    if (isCreateService) {
      createService(sharedPackage, calcServiceBaseName(), progress.newChild(2), workingCopyManager);
    }
    progress.setWorkRemaining(6);

    if (isCreateAbstractPage()) {
      if (isCreatePageData) {
        setCreatedAbstractPageData(createPageData(ISdkProperties.PREFIX_ABSTRACT + getPageName(), sharedPackage, progress.newChild(1), workingCopyManager));
      }
      setCreatedAbstractPage(createAbstractPage(isPageWithTable, progress.newChild(1), workingCopyManager));
      setSuperType(getCreatedAbstractPage());
    }
    progress.setWorkRemaining(4);

    setCreatedPage(createPage(isPageWithTable, progress.newChild(1), workingCopyManager));

    if (isCreateService) {
      setCreatedServiceTest(createServiceTest(progress.newChild(1), workingCopyManager));
    }
    progress.setWorkRemaining(2);

    // run DTO update because the pageData has been created as empty java file
    if (isCreatePageData) {
      updatePageDatas(progress.newChild(2), workingCopyManager);
    }
    progress.setWorkRemaining(0);
  }

  protected void updatePageDatas(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 10);

    IJavaEnvironment clientEnv = getEnvProvider().get(getCreatedPage().getJavaProject());
    clientEnv.reload();

    if (isCreateAbstractPage()) {
      CompilationUnitWriteOperation abstractPageDataUpdateOp = DtoDerivedResourceHandler.newDtoOp(getCreatedAbstractPage(), S2eUtils.jdtTypeToScoutType(getCreatedAbstractPage(), clientEnv), getEnvProvider());
      if (abstractPageDataUpdateOp != null) {
        abstractPageDataUpdateOp.run(progress.newChild(1), workingCopyManager);

        IJavaEnvironment sharedEnv = getEnvProvider().get(getCreatedAbstractPageData().getJavaProject());

        // make the new content of the AbstractPageDat available for the shared env. Because the workingcopy has not yet been written, the file on the disk still contains the old content -> update with new
        sharedEnv.registerCompilationUnitOverride(getCreatedAbstractPageData().getPackageFragment().getElementName(),
            getCreatedAbstractPageData().getCompilationUnit().getElementName(),
            new StringBuilder(abstractPageDataUpdateOp.getContent()));
        sharedEnv.reload(); // reload the shared env because we just changed the abstractPageData
      }
    }

    CompilationUnitWriteOperation pageDataUpdateOp = DtoDerivedResourceHandler.newDtoOp(getCreatedPage(), S2eUtils.jdtTypeToScoutType(getCreatedPage(), clientEnv), getEnvProvider());
    if (pageDataUpdateOp != null) {
      pageDataUpdateOp.run(progress.newChild(1), workingCopyManager);
    }
  }

  protected IType createAbstractPage(boolean isPageWithTable, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    PageSourceBuilder pageBuilder = createPageBuilder(isPageWithTable, true);
    return S2eUtils.writeType(getClientSourceFolder(), pageBuilder, pageBuilder.getJavaEnvironment(), monitor, workingCopyManager);
  }

  protected String calcServiceMethodName() {
    String name = calcPageBaseName();
    return "get" + name + "TableData";
  }

  protected String calcPageBaseName() {
    String name = getPageName();
    String[] suffixes = new String[]{ISdkProperties.SUFFIX_PAGE_WITH_NODES, ISdkProperties.SUFFIX_PAGE_WITH_TABLE, "Page"};
    for (String suffix : suffixes) {
      if (StringUtils.endsWithIgnoreCase(name, suffix)) {
        name = name.substring(0, name.length() - suffix.length());
      }
    }
    return name;
  }

  protected String calcServiceBaseName() {
    return calcPageBaseName();
  }

  protected IType createServiceTest(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (!S2eUtils.exists(getTestSourceFolder())) {
      return null;
    }

    String serverPackage = Signature.getQualifier(getCreatedServiceImpl().getFullyQualifiedName());
    String baseName = getCreatedServiceImpl().getElementName();
    String elementName = baseName + ISdkProperties.SUFFIX_TEST;

    IType existingServiceTest = getTestSourceFolder().getJavaProject().findType(serverPackage, elementName);
    if (S2eUtils.exists(existingServiceTest)) {
      // service test class already exists
      return existingServiceTest;
    }

    IJavaEnvironment env = getEnvProvider().get(getTestSourceFolder().getJavaProject());
    TestSourceBuilder testBuilder = new TestSourceBuilder(elementName, serverPackage, env);
    testBuilder.setRunnerSignature(Signature.createTypeSignature(IScoutRuntimeTypes.ServerTestRunner));
    testBuilder.setClientTest(false);
    IType session = S2eUtils.getSession(getServerSourceFolder().getJavaProject(), ScoutTier.Server, monitor);
    if (S2eUtils.exists(session)) {
      testBuilder.setSessionSignature(Signature.createTypeSignature(session.getFullyQualifiedName()));
    }
    testBuilder.setup();

    return S2eUtils.writeType(getTestSourceFolder(), testBuilder, env, monitor, workingCopyManager);
  }

  protected void createService(String sharedPackage, String baseName, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ServiceNewOperation serviceNewOperation = createServiceOperation();
    serviceNewOperation.setServiceName(baseName);
    serviceNewOperation.setSharedPackage(sharedPackage);
    serviceNewOperation.setSharedSourceFolder(getSharedSourceFolder());
    serviceNewOperation.setServerSourceFolder(getServerSourceFolder());
    serviceNewOperation.addMethod(createServiceMethod());

    serviceNewOperation.validate();
    serviceNewOperation.run(monitor, workingCopyManager);

    setCreatedServiceImpl(serviceNewOperation.getCreatedServiceImpl());
    setCreatedServiceIfc(serviceNewOperation.getCreatedServiceInterface());
  }

  protected IMethodSourceBuilder createServiceMethod() {
    setDataFetchMethodName(calcServiceMethodName());
    final IMethodSourceBuilder methodBuilder = new MethodSourceBuilder(getDataFetchMethodName());
    methodBuilder.setFlags(Flags.AccPublic);
    methodBuilder.setComment(CommentSourceBuilderFactory.createDefaultMethodComment(methodBuilder));

    final String pageDataSig = Signature.createTypeSignature(getCreatedPageData().getFullyQualifiedName());
    methodBuilder.setReturnTypeSignature(pageDataSig);
    methodBuilder.addParameter(new MethodParameterSourceBuilder("filter", Signature.createTypeSignature(IScoutRuntimeTypes.SearchFilter)));
    methodBuilder.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        String varName = "pageData";
        source.append(validator.useSignature(pageDataSig)).append(' ').append(varName).append(" = new ").append(validator.useSignature(pageDataSig)).append("();").append(lineDelimiter);
        source.append(CoreUtils.getCommentBlock("fill " + varName + '.')).append(lineDelimiter);
        source.append("return ").append(varName).append(';');
      }
    });
    return methodBuilder;
  }

  protected ServiceNewOperation createServiceOperation() {
    return new ServiceNewOperation(getEnvProvider());
  }

  protected PageSourceBuilder createPageBuilder(boolean isPageWithTable, boolean isAbstractPage) {
    String name = getPageName();
    if (isAbstractPage) {
      name = ISdkProperties.PREFIX_ABSTRACT + name;
    }
    PageSourceBuilder pageBuilder = new PageSourceBuilder(name, getPackage(), getEnvProvider().get(getClientSourceFolder().getJavaProject()));
    pageBuilder.setAbstractPage(isAbstractPage);
    pageBuilder.setCreateNlsMethod(isCreateAbstractPage() == isAbstractPage);
    if (ClassIdGenerators.isAutomaticallyCreateClassIdAnnotation()) {
      ClassIdGenerationContext context = new ClassIdGenerationContext(getPackage() + '.' + getPageName());
      pageBuilder.setClassIdValue(ClassIdGenerators.generateNewId(context));
      pageBuilder.setTableClassIdValue(ClassIdGenerators.generateNewId(context));
    }
    IType dto = getCreatedPageData();
    if (isAbstractPage) {
      dto = getCreatedAbstractPageData();
    }
    if (S2eUtils.exists(dto)) {
      pageBuilder.setPageDataSignature(Signature.createTypeSignature(dto.getFullyQualifiedName()));
    }
    if (!isAbstractPage && S2eUtils.exists(getCreatedServiceIfc())) {
      pageBuilder.setPageServiceIfcSignature(Signature.createTypeSignature(getCreatedServiceIfc().getFullyQualifiedName()));
    }
    pageBuilder.setDataFetchMethodName(getDataFetchMethodName());
    pageBuilder.setPageWithTable(isPageWithTable);
    pageBuilder.setSuperTypeSignature(Signature.createTypeSignature(getSuperType().getFullyQualifiedName()));
    pageBuilder.setup();
    return pageBuilder;
  }

  protected IType createPage(boolean isPageWithTable, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    PageSourceBuilder pageBuilder = createPageBuilder(isPageWithTable, false);
    if (isCreateAbstractPage() && isPageWithTable) {
      ITypeSourceBuilder mainType = pageBuilder.getMainType();

      ITypeSourceBuilder tableBuilder = mainType.getTypes().get(0);
      String superTypeFqn = getCreatedAbstractPage().getFullyQualifiedName()
          + ISignatureConstants.C_GENERIC_START
          + tableBuilder.getFullyQualifiedName()
          + ISignatureConstants.C_GENERIC_END
          + '.'
          + PageSourceBuilder.INNER_TABLE_NAME;
      tableBuilder.setSuperTypeSignature(Signature.createTypeSignature(superTypeFqn));
    }
    return S2eUtils.writeType(getClientSourceFolder(), pageBuilder, pageBuilder.getJavaEnvironment(), monitor, workingCopyManager);
  }

  protected IType createPageData(String pageName, String sharedPackage, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    String pageDataName = pageName + ISdkProperties.SUFFIX_DTO;

    ICompilationUnitSourceBuilder pageDataBuilder = new CompilationUnitSourceBuilder(pageDataName + SuffixConstants.SUFFIX_STRING_java, sharedPackage);
    ITypeSourceBuilder formDataTypeBuilder = new TypeSourceBuilder(pageDataName);
    formDataTypeBuilder.setFlags(Flags.AccPublic);
    formDataTypeBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractTablePageData));
    pageDataBuilder.addType(formDataTypeBuilder);

    return S2eUtils.writeType(getPageDataSourceFolder(), pageDataBuilder, getEnvProvider().get(getPageDataSourceFolder().getJavaProject()), monitor, workingCopyManager);
  }

  public IType getCreatedPage() {
    return m_createdPage;
  }

  protected void setCreatedPage(IType page) {
    m_createdPage = page;
  }

  public String getPageName() {
    return m_pageName;
  }

  public void setPageName(String pageName) {
    m_pageName = pageName;
  }

  public IPackageFragmentRoot getClientSourceFolder() {
    return m_clientSourceFolder;
  }

  public void setClientSourceFolder(IPackageFragmentRoot clientSourceFolder) {
    m_clientSourceFolder = clientSourceFolder;
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

  public IPackageFragmentRoot getSharedSourceFolder() {
    return m_sharedSourceFolder;
  }

  public void setSharedSourceFolder(IPackageFragmentRoot sharedSourceFolder) {
    m_sharedSourceFolder = sharedSourceFolder;
  }

  public IType getCreatedPageData() {
    return m_createdPageData;
  }

  protected void setCreatedPageData(IType createdPageData) {
    m_createdPageData = createdPageData;
  }

  protected IJavaEnvironmentProvider getEnvProvider() {
    return m_javaEnvironmentProvider;
  }

  public IPackageFragmentRoot getServerSourceFolder() {
    return m_serverSourceFolder;
  }

  public void setServerSourceFolder(IPackageFragmentRoot serverSourceFolder) {
    m_serverSourceFolder = serverSourceFolder;
  }

  public IType getCreatedServiceIfc() {
    return m_createdServiceIfc;
  }

  protected void setCreatedServiceIfc(IType createdServiceIfc) {
    m_createdServiceIfc = createdServiceIfc;
  }

  public IType getCreatedServiceImpl() {
    return m_createdServiceImpl;
  }

  protected void setCreatedServiceImpl(IType createdServiceImpl) {
    m_createdServiceImpl = createdServiceImpl;
  }

  public IPackageFragmentRoot getPageDataSourceFolder() {
    return m_pageDataSourceFolder;
  }

  public void setPageDataSourceFolder(IPackageFragmentRoot pageDataSourceFolder) {
    m_pageDataSourceFolder = pageDataSourceFolder;
  }

  public IPackageFragmentRoot getTestSourceFolder() {
    return m_testSourceFolder;
  }

  public void setTestSourceFolder(IPackageFragmentRoot testSourceFolder) {
    m_testSourceFolder = testSourceFolder;
  }

  public IType getCreatedServiceTest() {
    return m_createdServiceTest;
  }

  protected void setCreatedServiceTest(IType createdServiceTest) {
    m_createdServiceTest = createdServiceTest;
  }

  protected String getDataFetchMethodName() {
    return m_dataFetchMethodName;
  }

  protected void setDataFetchMethodName(String dataFetchMethodName) {
    m_dataFetchMethodName = dataFetchMethodName;
  }

  public boolean isCreateAbstractPage() {
    return m_createAbstractPage;
  }

  public void setCreateAbstractPage(boolean createAbstractPage) {
    m_createAbstractPage = createAbstractPage;
  }

  public IType getCreatedAbstractPage() {
    return m_createdAbstractPage;
  }

  protected void setCreatedAbstractPage(IType createdAbstractPage) {
    m_createdAbstractPage = createdAbstractPage;
  }

  public IType getCreatedAbstractPageData() {
    return m_createdAbstractPageData;
  }

  protected void setCreatedAbstractPageData(IType createdAbstractPageData) {
    m_createdAbstractPageData = createdAbstractPageData;
  }
}
