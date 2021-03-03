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
import org.eclipse.scout.sdk.core.s.environment.SdkFuture;
import org.eclipse.scout.sdk.core.s.service.ServiceNewOperation;
import org.eclipse.scout.sdk.core.s.testcase.TestGenerator;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Ensure;
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
  private boolean m_createService;

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
  private String m_createdServiceIfcFqn;

  private IFuture<IType> m_createdServiceImpl;
  private String m_createdServiceImplFqn;

  private IFuture<IType> m_createdServiceTest;
  private String m_createdServiceTestFqn;

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
    setCreateService(isCreatePageData() && getSharedSourceFolder() != null && getServerSourceFolder() != null);
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

    var isCreateService = isCreateService();
    if (isCreateService) {
      createService(sharedPackage, calcServiceBaseName(), env, progress.newChild(2));
    }

    if (isCreateAbstractPage()) {
      setCreatedAbstractPage(createAbstractPage(isPageWithTable(), env, progress.newChild(1)));
      setSuperType(getCreatedAbstractPageFqn());
    }

    setCreatedPage(createPage(isPageWithTable(), env, progress.newChild(1)));

    if (isCreateService) {
      setCreatedServiceTest(createServiceTest(env, progress.newChild(1)));
    }

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
      env.writeCompilationUnitAsync(abstractPageDataGenerator.get(), getPageDataSourceFolder(), progress.newChild(1));
    }
    // the DtoGeneratorFactory parses the @Data-Annotation and uses the given pageData-IType, therefore the pageData needs to be created already
    getCreatedPageData().result();
    var pageDataGenerator = DtoGeneratorFactory.createPageDataGenerator(getCreatedPage().result(), getPageDataSourceFolder().javaEnvironment());
    env.writeCompilationUnitAsync(pageDataGenerator.get(), getPageDataSourceFolder(), progress.newChild(1));
  }

  protected IFuture<IType> createAbstractPage(boolean isPageWithTable, IEnvironment env, IProgress progress) {
    var pageBuilder = createPageBuilder(isPageWithTable, true);

    setCreatedAbstractPageFqn(pageBuilder.fullyQualifiedName());
    return env.writeCompilationUnitAsync(pageBuilder, getClientSourceFolder(), progress);
  }

  protected String calcServiceMethodName() {
    var name = calcPageBaseName();
    return "get" + name + "TableData";
  }

  protected String calcPageBaseName() {
    var name = getPageName();
    String[] suffixes = {ISdkConstants.SUFFIX_PAGE_WITH_NODES, ISdkConstants.SUFFIX_PAGE_WITH_TABLE, ISdkConstants.SUFFIX_OUTLINE_PAGE};
    for (var suffix : suffixes) {
      var suffixLen = suffix.length();
      var strOffset = name.length() - suffixLen;
      if (name.regionMatches(true, strOffset, suffix, 0, suffixLen)) {
        name = name.substring(0, name.length() - suffixLen);
      }
    }
    return name;
  }

  protected String calcServiceBaseName() {
    var baseName = calcPageBaseName();
    return baseName + "Page";
  }

  protected IFuture<IType> createServiceTest(IEnvironment env, IProgress progress) {
    var testSourceFolder = getTestSourceFolder();
    if (testSourceFolder == null) {
      return null;
    }

    var serverPackage = JavaTypes.qualifier(getCreatedServiceImplFqn());
    var baseName = JavaTypes.simpleName(getCreatedServiceImplFqn());
    var elementName = baseName + ISdkConstants.SUFFIX_TEST;

    var existingServiceTest = testSourceFolder.javaEnvironment().findType(serverPackage + JavaTypes.C_DOT + elementName);
    if (existingServiceTest.isPresent()) {
      // service test class already exists
      return SdkFuture.completed(existingServiceTest.get());
    }

    var scoutApi = testSourceFolder.javaEnvironment().requireApi(IScoutApi.class);
    TestGenerator<?> testBuilder = new TestGenerator<>()
        .withElementName(elementName)
        .withPackageName(serverPackage)
        .withRunner(scoutApi.ServerTestRunner().fqn())
        .asClientTest(false);
    if (Strings.hasText(getServerSession())) {
      testBuilder.withSession(getServerSession());
    }

    setCreatedServiceTestFqn(testBuilder.fullyQualifiedName());
    return env.writeCompilationUnitAsync(testBuilder, testSourceFolder, progress);
  }

  protected void createService(String sharedPackage, String baseName, IEnvironment env, IProgress progress) {
    var serviceNewOperation = createServiceOperation();
    serviceNewOperation.setServiceName(baseName);
    serviceNewOperation.setSharedPackage(sharedPackage);
    serviceNewOperation.setSharedSourceFolder(getSharedSourceFolder());
    serviceNewOperation.setServerSourceFolder(getServerSourceFolder());
    serviceNewOperation.addMethod(createServiceMethod());
    serviceNewOperation.accept(env, progress);

    setCreatedServiceImpl(serviceNewOperation.getCreatedServiceImpl());
    setCreatedServiceImplFqn(serviceNewOperation.getCreatedServiceImplFqn());

    setCreatedServiceIfc(serviceNewOperation.getCreatedServiceInterface());
    setCreatedServiceIfcFqn(serviceNewOperation.getCreatedServiceInterfaceFqn());
  }

  protected IMethodGenerator<?, ?> createServiceMethod() {
    setDataFetchMethodName(calcServiceMethodName());
    return MethodGenerator.create()
        .asPublic()
        .withReturnType(getCreatedPageDataFqn())
        .withElementName(getDataFetchMethodName())
        .withParameter(MethodParameterGenerator.create()
            .withElementName("filter")
            .withDataTypeFrom(IScoutApi.class, api -> api.SearchFilter().fqn()))
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withBody(b -> {
          var varName = "pageData";
          b.ref(getCreatedPageDataFqn()).space().append(varName).equalSign().appendNew().ref(getCreatedPageDataFqn()).parenthesisOpen().parenthesisClose().semicolon().nl()
              .appendTodo("fill " + varName + '.')
              .returnClause().append(varName).semicolon();
        });
  }

  protected ServiceNewOperation createServiceOperation() {
    return new ServiceNewOperation();
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
    if (!isAbstractPage && getCreatedServiceIfc() != null) {
      generator.withPageServiceInterface(getCreatedServiceIfcFqn());
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

  public String getCreatedServiceIfcFqn() {
    return m_createdServiceIfcFqn;
  }

  protected void setCreatedServiceIfcFqn(String createdServiceIfcFqn) {
    m_createdServiceIfcFqn = createdServiceIfcFqn;
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

  public String getCreatedServiceTestFqn() {
    return m_createdServiceTestFqn;
  }

  protected void setCreatedServiceTestFqn(String createdServiceTestFqn) {
    m_createdServiceTestFqn = createdServiceTestFqn;
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

  public boolean isCreateService() {
    return m_createService;
  }

  protected void setCreateService(boolean createService) {
    m_createService = createService;
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
