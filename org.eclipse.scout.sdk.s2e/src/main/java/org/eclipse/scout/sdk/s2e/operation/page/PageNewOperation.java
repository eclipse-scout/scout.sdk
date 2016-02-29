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

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.sourcebuilder.page.PageSourceBuilder;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerationContext;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerators;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;
import org.jsoup.helper.Validate;

/**
 * <h3>{@link PageNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PageNewOperation implements IOperation {

  // in
  private String m_pageName;
  private IPackageFragmentRoot m_clientSourceFolder;
  private IPackageFragmentRoot m_sharedSourceFolder;
  private String m_package;
  private IType m_superType;

  // out
  private IType m_createdPage;
  private IType m_createdPageData;

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
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 2);

    // calc names
    String sharedPackage = ScoutTier.Client.convert(ScoutTier.Shared, getPackage());
    boolean isPageWithTable = S2eUtils.hierarchyContains(getSuperType().newSupertypeHierarchy(null), IScoutRuntimeTypes.IPageWithTable);

    if (isPageWithTable && S2eUtils.exists(getSharedSourceFolder())) {
      setCreatedPageData(createPageData(sharedPackage, progress.newChild(1), workingCopyManager));
    }

    setCreatedPage(createPage(isPageWithTable, progress.newChild(1), workingCopyManager));

    // schedule DTO update because the pageData has been created as empty java file
    ScoutSdkCore.getDerivedResourceManager().trigger(Collections.singleton(getCreatedPage().getResource()));
  }

  protected PageSourceBuilder createPageBuilder(boolean isPageWithTable) {
    PageSourceBuilder pageBuilder = new PageSourceBuilder(getPageName(), getPackage());

    if (ClassIdGenerators.isAutomaticallyCreateClassIdAnnotation()) {
      pageBuilder.setClassIdValue(ClassIdGenerators.generateNewId(new ClassIdGenerationContext(getPackage() + '.' + getPageName())));
    }

    if (S2eUtils.exists(getCreatedPageData())) {
      pageBuilder.setPageDataSignature(Signature.createTypeSignature(getCreatedPageData().getFullyQualifiedName()));
    }

    pageBuilder.setPageWithTable(isPageWithTable);
    pageBuilder.setSuperTypeSignature(Signature.createTypeSignature(getSuperType().getFullyQualifiedName()));
    pageBuilder.setup();
    return pageBuilder;
  }

  protected IType createPage(boolean isPageWithTable, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    PageSourceBuilder pageBuilder = createPageBuilder(isPageWithTable);
    return S2eUtils.writeType(getClientSourceFolder(), pageBuilder, monitor, workingCopyManager);
  }

  protected IType createPageData(String sharedPackage, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    String pageDataName = getPageName() + ISdkProperties.SUFFIX_DTO;

    ICompilationUnitSourceBuilder pageDataBuilder = new CompilationUnitSourceBuilder(pageDataName + SuffixConstants.SUFFIX_STRING_java, sharedPackage);
    ITypeSourceBuilder formDataTypeBuilder = new TypeSourceBuilder(pageDataName);
    formDataTypeBuilder.setFlags(Flags.AccPublic);
    formDataTypeBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractTablePageData));
    pageDataBuilder.addType(formDataTypeBuilder);

    return S2eUtils.writeType(getSharedSourceFolder(), pageDataBuilder, monitor, workingCopyManager);
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
}
