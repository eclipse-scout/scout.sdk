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
import org.eclipse.scout.sdk.core.s.environment.IProgress;
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

  // out
  private IType m_createdAbstractPage;
  private IType m_createdPage;
  private IType m_createdPageData;
  private IType m_createdAbstractPageData;
  private IType m_createdServiceIfc;
  private IType m_createdServiceImpl;
  private IType m_createdServiceTest;

  private String m_dataFetchMethodName;

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    Ensure.notBlank(getPageName(), "No page name provided");
    Ensure.notNull(getClientSourceFolder(), "No client source folder provided");
    Ensure.notBlank(getPackage(), "No package name provided");
    Ensure.notNull(getSuperType(), "No supertype provided");

    progress.init(10, toString());

    var sharedPackage = ScoutTier.Client.convert(ScoutTier.Shared, getPackage());
    var isPageWithTable = isPageWithTable();
    var isCreatePageData = isPageWithTable && getPageDataSourceFolder() != null;

    if (isCreatePageData) {
      if (isCreateAbstractPage()) {
        setCreatedAbstractPageData(createPageData(ISdkConstants.PREFIX_ABSTRACT + getPageName(), sharedPackage, env, progress.newChild(1)));
      }
      setCreatedPageData(createPageData(getPageName(), sharedPackage, env, progress.newChild(1)));
    }

    var isCreateService = isCreatePageData && getSharedSourceFolder() != null && getServerSourceFolder() != null;
    if (isCreateService) {
      createService(sharedPackage, calcServiceBaseName(), env, progress.newChild(2));
    }
    progress.setWorkRemaining(6);

    if (isCreateAbstractPage()) {
      setCreatedAbstractPage(createAbstractPage(isPageWithTable, env, progress.newChild(2)));
      setSuperType(getCreatedAbstractPage().reference());
    }
    progress.setWorkRemaining(4);

    setCreatedPage(createPage(isPageWithTable, env, progress.newChild(1)));

    if (isCreateService) {
      setCreatedServiceTest(createServiceTest(env, progress.newChild(1)));
    }

    // run DTO update because the pageData has been created as empty java file
    if (isCreatePageData) {
      updatePageDatas(env, progress.newChild(2));
    }
    progress.setWorkRemaining(0);
  }

  protected boolean isPageWithTable() {
    return getClientSourceFolder()
        .javaEnvironment()
        .findType(JavaTypes.erasure(getSuperType()))
        .map(PageNewOperation::isPageWithTable)
        .orElse(false);
  }

  protected static boolean isPageWithTable(IType type) {
    var scoutApi = type.javaEnvironment().requireApi(IScoutApi.class);
    return type.isInstanceOf(scoutApi.IPageWithTable());
  }

  protected void updatePageDatas(IEnvironment env, IProgress progress) {
    if (isCreateAbstractPage()) {
      var abstractPageDataGenerator = DtoGeneratorFactory.createPageDataGenerator(getCreatedAbstractPage(), getPageDataSourceFolder().javaEnvironment());
      env.writeCompilationUnit(abstractPageDataGenerator.get(), getPageDataSourceFolder(), progress.newChild(1));
    }
    var pageDataGenerator = DtoGeneratorFactory.createPageDataGenerator(getCreatedPage(), getPageDataSourceFolder().javaEnvironment());
    env.writeCompilationUnit(pageDataGenerator.get(), getPageDataSourceFolder(), progress.newChild(1));
  }

  protected IType createAbstractPage(boolean isPageWithTable, IEnvironment env, IProgress progress) {
    var pageBuilder = createPageBuilder(isPageWithTable, true);
    return env.writeCompilationUnit(pageBuilder, getClientSourceFolder(), progress);
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
    return calcPageBaseName();
  }

  protected IType createServiceTest(IEnvironment env, IProgress progress) {
    var testSourceFolder = getTestSourceFolder();
    if (testSourceFolder == null) {
      return null;
    }

    var serverPackage = JavaTypes.qualifier(getCreatedServiceImpl().name());
    var baseName = getCreatedServiceImpl().elementName();
    var elementName = baseName + ISdkConstants.SUFFIX_TEST;

    var existingServiceTest = testSourceFolder.javaEnvironment().findType(serverPackage + JavaTypes.C_DOT + elementName);
    if (existingServiceTest.isPresent()) {
      // service test class already exists
      return existingServiceTest.get();
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

    return env.writeCompilationUnit(testBuilder, testSourceFolder, progress);
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
    setCreatedServiceIfc(serviceNewOperation.getCreatedServiceInterface());
  }

  protected IMethodGenerator<?, ?> createServiceMethod() {
    setDataFetchMethodName(calcServiceMethodName());
    return MethodGenerator.create()
        .asPublic()
        .withReturnType(getCreatedPageData().name())
        .withElementName(getDataFetchMethodName())
        .withParameter(MethodParameterGenerator.create()
            .withElementName("filter")
            .withDataTypeFrom(IScoutApi.class, api -> api.SearchFilter().fqn()))
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withBody(b -> {
          var varName = "pageData";
          b.ref(getCreatedPageData()).space().append(varName).equalSign().appendNew().ref(getCreatedPageData()).parenthesisOpen().parenthesisClose().semicolon().nl()
              .appendTodo("fill " + varName + '.')
              .returnClause().append(varName).semicolon();
        });
  }

  protected static ServiceNewOperation createServiceOperation() {
    return new ServiceNewOperation();
  }

  protected PageGenerator<?> createPageBuilder(boolean isPageWithTable, boolean isAbstractPage) {
    var name = getPageName();
    if (isAbstractPage) {
      name = ISdkConstants.PREFIX_ABSTRACT + name;
    }
    var fqn = getPackage() + JavaTypes.C_DOT + name;
    PageGenerator<?> pageBuilder = new PageGenerator<>()
        .withElementName(name)
        .withPackageName(getPackage())
        .withFlags(isAbstractPage ? Flags.AccAbstract : Flags.AccPublic)
        .withNlsMethod(isCreateAbstractPage() == isAbstractPage)
        .withClassIdValue(ClassIds.nextIfEnabled(fqn))
        .withTableClassIdValue(ClassIds.nextIfEnabled(fqn))
        .withDataFetchMethodName(getDataFetchMethodName())
        .asPageWithTable(isPageWithTable)
        .withSuperClass(getSuperType());

    var dto = getCreatedPageData();
    if (isAbstractPage) {
      dto = getCreatedAbstractPageData();
    }
    if (dto != null) {
      pageBuilder.withPageData(dto.name());
    }
    if (!isAbstractPage && getCreatedServiceIfc() != null) {
      pageBuilder.withPageServiceInterface(getCreatedServiceIfc().name());
    }
    return pageBuilder;
  }

  protected IType createPage(boolean isPageWithTable, IEnvironment env, IProgress progress) {
    var pageBuilder = createPageBuilder(isPageWithTable, false);
    if (isCreateAbstractPage() && isPageWithTable) {
      var superTypeFqn = getCreatedAbstractPage().name()
          + JavaTypes.C_GENERIC_START
          + pageBuilder.fullyQualifiedName()
          + JavaTypes.C_DOLLAR
          + PageGenerator.INNER_TABLE_NAME
          + JavaTypes.C_GENERIC_END
          + JavaTypes.C_DOT
          + PageGenerator.INNER_TABLE_NAME;
      pageBuilder.withTableSuperType(superTypeFqn);
    }

    return env.writeCompilationUnit(pageBuilder, getClientSourceFolder(), progress);
  }

  protected IType createPageData(String pageName, String sharedPackage, IEnvironment env, IProgress progress) {
    var pageDataGenerator = PrimaryTypeGenerator.create()
        .withElementName(pageName + ISdkConstants.SUFFIX_DTO)
        .withPackageName(sharedPackage)
        .asPublic()
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractTablePageData().fqn());
    return env.writeCompilationUnit(pageDataGenerator, getPageDataSourceFolder(), progress);
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

  public IType getCreatedPageData() {
    return m_createdPageData;
  }

  protected void setCreatedPageData(IType createdPageData) {
    m_createdPageData = createdPageData;
  }

  public IClasspathEntry getServerSourceFolder() {
    return m_serverSourceFolder;
  }

  public void setServerSourceFolder(IClasspathEntry serverSourceFolder) {
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
