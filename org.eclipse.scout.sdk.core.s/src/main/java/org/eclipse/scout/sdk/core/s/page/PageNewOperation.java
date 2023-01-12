/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.page;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.classid.ClassIds;
import org.eclipse.scout.sdk.core.s.dto.DtoGeneratorFactory;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.service.ServiceNewOperation;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link PageNewOperation}</h3>
 *
 * @since 7.0.0
 */
public class PageNewOperation implements BiConsumer<IEnvironment, IProgress> {
  // in
  private String m_pageName;
  private FinalValue<String> m_baseName = new FinalValue<>();
  private IClasspathEntry m_clientSourceFolder;
  private IClasspathEntry m_pageDataSourceFolder;
  private IClasspathEntry m_sharedSourceFolder;
  private IClasspathEntry m_serverSourceFolder;
  private IClasspathEntry m_testSourceFolder;
  private String m_package;
  private String m_superType;
  private String m_serverSession;
  private boolean m_createAbstractPage;
  private boolean m_pageWithTable;
  private boolean m_createPageData;
  private boolean m_createOrAppendService;
  private ServiceNewOperation m_serviceNewOperation; // optional

  // out
  private IFuture<IType> m_createdAbstractPage;
  private String m_createdAbstractPageFqn;

  private IFuture<IType> m_createdPage;
  private String m_createdPageFqn;

  private IFuture<IType> m_createdPageData;
  private String m_createdPageDataFqn;

  private IFuture<IType> m_createdAbstractPageData;
  private String m_createdAbstractPageDataFqn;

  private IFuture<IType> m_createdServiceIfc;
  private String m_serviceIfcFqn;

  private IFuture<IType> m_createdServiceImpl;
  private String m_serviceImplFqn;

  private IFuture<IType> m_createdServiceTest;
  private String m_serviceTestFqn;

  private String m_dataFetchMethodName;

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    validateOperation();
    prepareOperation();
    prepareProgress(progress);
    executeOperation(env, progress);
  }

  protected void validateOperation() {
    Ensure.notBlank(getPageName(), "No page name provided");
    Ensure.notNull(getClientSourceFolder(), "No client source folder provided");
    Ensure.notBlank(getPackage(), "No package name provided");
    Ensure.notNull(getSuperType(), "No supertype provided");
  }

  protected void prepareOperation() {
    setPageWithTable(calculatePageWithTable());
    setCreatePageData(isPageWithTable() && getPageDataSourceFolder() != null);
    setCreateOrAppendService(isCreatePageData() && getSharedSourceFolder() != null && getServerSourceFolder() != null);
  }

  protected void prepareProgress(IProgress progress) {
    progress.init(getTotalWork(), toString());
  }

  protected void executeOperation(IEnvironment env, IProgress progress) {
    var sharedPackage = ScoutTier.Client.convert(ScoutTier.Shared, getPackage());

    if (isCreatePageData()) {
      if (isCreateAbstractPage()) {
        setCreatedAbstractPageData(createPageData(ISdkConstants.PREFIX_ABSTRACT + getPageName(), sharedPackage, this::setCreatedAbstractPageDataFqn, env, progress.newChild(1)));
      }
      setCreatedPageData(createPageData(getPageName(), sharedPackage, this::setCreatedPageDataFqn, env, progress.newChild(1)));
    }

    if (isCreateOrAppendService()) {
      createOrAppendService(sharedPackage, env, progress);
    }

    if (isCreateAbstractPage()) {
      setCreatedAbstractPage(createAbstractPage(isPageWithTable(), env, progress.newChild(1)));
      setSuperType(getCreatedAbstractPageFqn());
    }

    setCreatedPage(createPage(isPageWithTable(), env, progress.newChild(1)));

    // run DTO update because the pageData has been created as empty java file
    if (isCreatePageData()) {
      updatePageDatas(env, progress.newChild(2));
    }
  }

  protected int getTotalWork() {
    var result = 1; // page
    if (isCreateAbstractPage()) {
      result += 1;// abstract page
    }
    if (isCreatePageData()) {
      if (isCreateAbstractPage()) {
        result += 1; // abstract pageData
      }
      result += 1; // pageData
      result += 2; // update pageDatas
    }
    if (isCreateService()) {
      result += 3;// ifc, impl, test
    }
    return result;
  }

  protected boolean calculatePageWithTable() {
    return getClientSourceFolder()
        .javaEnvironment()
        .findType(JavaTypes.erasure(getSuperType()))
        .map(PageNewOperation::calculatePageWithTable)
        .orElse(false);
  }

  protected static boolean calculatePageWithTable(IType type) {
    var scoutApi = type.javaEnvironment().requireApi(IScoutApi.class);
    return type.isInstanceOf(scoutApi.IPageWithTable());
  }

  protected void updatePageDatas(IEnvironment env, IProgress progress) {
    if (isCreateAbstractPage()) {
      // the DtoGeneratorFactory parses the @Data-Annotation and uses the given pageData-IType, therefore the pageData needs to be created already
      getCreatedAbstractPageData().result();
      var abstractPageDataGenerator = DtoGeneratorFactory.createPageDataGenerator(getCreatedAbstractPage().result(), getPageDataSourceFolder().javaEnvironment());
      env.writeCompilationUnitAsync(abstractPageDataGenerator.orElseThrow(), getPageDataSourceFolder(), progress.newChild(1));
    }
    // the DtoGeneratorFactory parses the @Data-Annotation and uses the given pageData-IType, therefore the pageData needs to be created already
    getCreatedPageData().result();
    var pageDataGenerator = DtoGeneratorFactory.createPageDataGenerator(getCreatedPage().result(), getPageDataSourceFolder().javaEnvironment());
    env.writeCompilationUnitAsync(pageDataGenerator.orElseThrow(), getPageDataSourceFolder(), progress.newChild(1));
  }

  protected IFuture<IType> createAbstractPage(boolean isPageWithTable, IEnvironment env, IProgress progress) {
    var pageBuilder = createPageBuilder(isPageWithTable, true);

    setCreatedAbstractPageFqn(pageBuilder.fullyQualifiedName());
    return env.writeCompilationUnitAsync(pageBuilder, getClientSourceFolder(), progress);
  }

  protected String getBaseName() {
    return m_baseName.computeIfAbsentAndGet(this::calcPageBaseName);
  }

  protected String calcPageBaseName() {
    var name = getPageName();
    var suffixes = new String[]{ISdkConstants.SUFFIX_PAGE_WITH_NODES, ISdkConstants.SUFFIX_PAGE_WITH_TABLE, ISdkConstants.SUFFIX_OUTLINE_PAGE};
    for (var suffix : suffixes) {
      name = Strings.removeSuffix(name, suffix, false);
    }
    return name;
  }

  public String getServiceBaseName() {
    return getBaseName();
  }

  protected String getServiceMethodName() {
    var name = getBaseName();
    return "get" + name + "TableData";
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
      setServiceIfcFqn(sharedPackage + JavaTypes.C_DOT + "I" + svcName);
      setServiceImplFqn(serverPackage + JavaTypes.C_DOT + svcName);

      if (op.isCreateTest()) {
        setServiceTestFqn(getServiceImplFqn() + ISdkConstants.SUFFIX_TEST);
      }
    }
  }

  protected void createService(String sharedPackage, String baseName, IEnvironment env, IProgress progress) {
    var serviceNewOperation = createServiceOperation();
    prepareServiceOperation(serviceNewOperation, sharedPackage, baseName);
    addServiceMethods(serviceNewOperation);
    serviceNewOperation.accept(env, progress);

    setCreatedServiceImpl(serviceNewOperation.getCreatedServiceImpl());
    setServiceImplFqn(serviceNewOperation.getCreatedServiceImplFqn());

    setCreatedServiceIfc(serviceNewOperation.getCreatedServiceInterface());
    setServiceIfcFqn(serviceNewOperation.getCreatedServiceInterfaceFqn());

    setCreatedServiceTest(serviceNewOperation.getCreatedServiceTest());
    setServiceTestFqn(serviceNewOperation.getCreatedServiceTestFqn());
  }

  protected void addServiceMethods(ServiceNewOperation serviceNewOperation) {
    serviceNewOperation.addMethod(createServiceMethod());
  }

  protected IMethodGenerator<?, ?> createServiceMethod() {
    setDataFetchMethodName(getServiceMethodName());
    return MethodGenerator.create()
        .asPublic()
        .withFlags(Flags.AccInterface) // also add the method in the service interface
        .withReturnType(getCreatedPageDataFqn())
        .withElementName(getDataFetchMethodName())
        .withParameter(MethodParameterGenerator.create()
            .withElementName("filter")
            .withDataTypeFrom(IScoutApi.class, api -> api.SearchFilter().fqn()))
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withBody(b -> {
          var varName = "pageData";
          b.ref(getCreatedPageDataFqn()).space().append(varName).equalSign().appendNew(getCreatedPageDataFqn()).parenthesisClose().semicolon().nl()
              .appendTodo("fill " + varName + '.')
              .returnClause().append(varName).semicolon();
        });
  }

  protected ServiceNewOperation createServiceOperation() {
    return new ServiceNewOperation();
  }

  protected void prepareServiceOperation(ServiceNewOperation serviceNewOperation, String sharedPackage, String baseName) {
    serviceNewOperation.setServiceName(baseName);
    serviceNewOperation.setSharedPackage(sharedPackage);
    serviceNewOperation.setSharedSourceFolder(getSharedSourceFolder());
    serviceNewOperation.setServerSourceFolder(getServerSourceFolder());
    if (getTestSourceFolder() == null) {
      return;
    }
    serviceNewOperation.setTestSourceFolder(getTestSourceFolder());
    serviceNewOperation.setServerSession(getServerSession());
    serviceNewOperation.setCreateTest(true);
  }

  protected PageGenerator<?> createPageBuilder(boolean isPageWithTable, boolean isAbstractPage) {
    var generator = createPageGenerator();
    preparePageGenerator(generator, isPageWithTable, isAbstractPage);
    return generator;
  }

  protected PageGenerator<?> createPageGenerator() {
    return new PageGenerator<>();
  }

  protected void preparePageGenerator(PageGenerator<?> generator, boolean isPageWithTable, boolean isAbstractPage) {
    var name = getPageName();
    if (isAbstractPage) {
      name = ISdkConstants.PREFIX_ABSTRACT + name;
    }
    var fqn = getPackage() + JavaTypes.C_DOT + name;

    generator
        .withElementName(name)
        .withPackageName(getPackage())
        .withFlags(isAbstractPage ? Flags.AccAbstract : Flags.AccPublic)
        .withNlsMethod(isCreateAbstractPage() == isAbstractPage)
        .withLeafMethod(isPageWithTable && isCreateAbstractPage() == isAbstractPage)
        .withClassIdValue(ClassIds.nextIfEnabled(fqn))
        .withTableClassIdValue(ClassIds.nextIfEnabled(fqn))
        .withDataFetchMethodName(getDataFetchMethodName())
        .asPageWithTable(isPageWithTable)
        .withSuperClass(getSuperType());

    var dtoFqn = getCreatedPageDataFqn();
    if (isAbstractPage) {
      dtoFqn = getCreatedAbstractPageDataFqn();
    }
    generator.withPageData(dtoFqn);
    if (!isAbstractPage && Strings.hasText(getServiceIfcFqn())) {
      generator.withPageServiceInterface(getServiceIfcFqn());
    }
  }

  protected IFuture<IType> createPage(boolean isPageWithTable, IEnvironment env, IProgress progress) {
    var pageBuilder = createPageBuilder(isPageWithTable, false);
    if (isCreateAbstractPage() && isPageWithTable) {
      var superTypeFqn = getCreatedAbstractPageFqn()
          + JavaTypes.C_GENERIC_START
          + pageBuilder.fullyQualifiedName()
          + JavaTypes.C_DOLLAR
          + PageGenerator.INNER_TABLE_NAME
          + JavaTypes.C_GENERIC_END
          + JavaTypes.C_DOT
          + PageGenerator.INNER_TABLE_NAME;
      pageBuilder.withTableSuperType(superTypeFqn);
    }

    setCreatedPageFqn(pageBuilder.fullyQualifiedName());
    return env.writeCompilationUnitAsync(pageBuilder, getClientSourceFolder(), progress);
  }

  protected IFuture<IType> createPageData(String pageName, String sharedPackage, Consumer<String> fqnConsumer, IEnvironment env, IProgress progress) {
    var pageDataGenerator = PrimaryTypeGenerator.create()
        .withElementName(pageName + ISdkConstants.SUFFIX_DTO)
        .withPackageName(sharedPackage)
        .asPublic()
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractTablePageData().fqn());

    if (fqnConsumer != null) {
      fqnConsumer.accept(pageDataGenerator.fullyQualifiedName());
    }
    return env.writeCompilationUnitAsync(pageDataGenerator, getPageDataSourceFolder(), progress);
  }

  public IFuture<IType> getCreatedPage() {
    return m_createdPage;
  }

  protected void setCreatedPage(IFuture<IType> page) {
    m_createdPage = page;
  }

  public String getCreatedPageFqn() {
    return m_createdPageFqn;
  }

  protected void setCreatedPageFqn(String createdPageFqn) {
    m_createdPageFqn = createdPageFqn;
  }

  public String getPageName() {
    return m_pageName;
  }

  public void setPageName(String pageName) {
    m_baseName = new FinalValue<>();
    m_pageName = pageName;
  }

  public IClasspathEntry getClientSourceFolder() {
    return m_clientSourceFolder;
  }

  public void setClientSourceFolder(IClasspathEntry clientSourceFolder) {
    m_clientSourceFolder = clientSourceFolder;
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

  public IClasspathEntry getSharedSourceFolder() {
    return m_sharedSourceFolder;
  }

  public void setSharedSourceFolder(IClasspathEntry sharedSourceFolder) {
    m_sharedSourceFolder = sharedSourceFolder;
  }

  public IFuture<IType> getCreatedPageData() {
    return m_createdPageData;
  }

  protected void setCreatedPageData(IFuture<IType> createdPageData) {
    m_createdPageData = createdPageData;
  }

  public String getCreatedPageDataFqn() {
    return m_createdPageDataFqn;
  }

  protected void setCreatedPageDataFqn(String createdPageDataFqn) {
    m_createdPageDataFqn = createdPageDataFqn;
  }

  public IClasspathEntry getServerSourceFolder() {
    return m_serverSourceFolder;
  }

  public void setServerSourceFolder(IClasspathEntry serverSourceFolder) {
    m_serverSourceFolder = serverSourceFolder;
  }

  public IFuture<IType> getCreatedServiceIfc() {
    return m_createdServiceIfc;
  }

  protected void setCreatedServiceIfc(IFuture<IType> createdServiceIfc) {
    m_createdServiceIfc = createdServiceIfc;
  }

  public String getServiceIfcFqn() {
    return m_serviceIfcFqn;
  }

  protected void setServiceIfcFqn(String serviceIfcFqn) {
    m_serviceIfcFqn = serviceIfcFqn;
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

  public IClasspathEntry getPageDataSourceFolder() {
    return m_pageDataSourceFolder;
  }

  public void setPageDataSourceFolder(IClasspathEntry pageDataSourceFolder) {
    m_pageDataSourceFolder = pageDataSourceFolder;
  }

  public IClasspathEntry getTestSourceFolder() {
    return m_testSourceFolder;
  }

  public void setTestSourceFolder(IClasspathEntry testSourceFolder) {
    m_testSourceFolder = testSourceFolder;
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

  public boolean isPageWithTable() {
    return m_pageWithTable;
  }

  protected void setPageWithTable(boolean pageWithTable) {
    m_pageWithTable = pageWithTable;
  }

  public boolean isCreatePageData() {
    return m_createPageData;
  }

  protected void setCreatePageData(boolean createPageData) {
    m_createPageData = createPageData;
  }

  public boolean isCreateOrAppendService() {
    return m_createOrAppendService;
  }

  protected void setCreateOrAppendService(boolean createOrAppendService) {
    m_createOrAppendService = createOrAppendService;
  }

  public boolean isCreateService() {
    return isCreateOrAppendService() && getServiceNewOperation() == null;
  }

  public boolean isAppendService() {
    return isCreateOrAppendService() && getServiceNewOperation() != null;
  }

  public ServiceNewOperation getServiceNewOperation() {
    return m_serviceNewOperation;
  }

  public void setServiceNewOperation(ServiceNewOperation serviceNewOperation) {
    m_serviceNewOperation = serviceNewOperation;
  }

  public IFuture<IType> getCreatedAbstractPage() {
    return m_createdAbstractPage;
  }

  protected void setCreatedAbstractPage(IFuture<IType> createdAbstractPage) {
    m_createdAbstractPage = createdAbstractPage;
  }

  public String getCreatedAbstractPageFqn() {
    return m_createdAbstractPageFqn;
  }

  protected void setCreatedAbstractPageFqn(String createdAbstractPageFqn) {
    m_createdAbstractPageFqn = createdAbstractPageFqn;
  }

  public IFuture<IType> getCreatedAbstractPageData() {
    return m_createdAbstractPageData;
  }

  protected void setCreatedAbstractPageData(IFuture<IType> createdAbstractPageData) {
    m_createdAbstractPageData = createdAbstractPageData;
  }

  public String getCreatedAbstractPageDataFqn() {
    return m_createdAbstractPageDataFqn;
  }

  protected void setCreatedAbstractPageDataFqn(String createdAbstractPageDataFqn) {
    m_createdAbstractPageDataFqn = createdAbstractPageDataFqn;
  }

  public String getServerSession() {
    return m_serverSession;
  }

  public void setServerSession(String serverSession) {
    m_serverSession = serverSession;
  }

  @Override
  public String toString() {
    return "Create new Page";
  }
}
