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

import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.testing.SdkPlatformTestRunner;
import org.eclipse.scout.sdk.s2e.testing.mock.PlatformMock;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h3>{@link PageNewOperationTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
@RunWith(SdkPlatformTestRunner.class)
public class PageNewOperationTest {

  @PlatformMock
  private IType m_superType;
  @PlatformMock
  private IType m_iPageWithTable;
  @PlatformMock
  private IPackageFragmentRoot m_clientSourceFolder;
  @PlatformMock
  private IPackageFragmentRoot m_dtoSourceFolder;
  @PlatformMock
  private IPackageFragmentRoot m_serverSourceFolder;
  @PlatformMock
  private IPackageFragmentRoot m_sharedSourceFolder;
  @PlatformMock
  private IPackageFragmentRoot m_testSourceFolder;
  @PlatformMock
  private IJavaEnvironmentProvider m_envProvider;

  @Before
  public void init() {
    when(m_iPageWithTable.getFullyQualifiedName()).thenReturn(IScoutRuntimeTypes.IPageWithTable);
  }

  @Test
  public void testPageWithTableFull() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, m_testSourceFolder, true, false);
  }

  @Test
  public void testPageWithTableNoTest() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, null, true, false);
  }

  @Test
  public void testPageWithTableNoService() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, null, null, true, false);
  }

  @Test
  public void testPageWithTableNoService2() throws CoreException {
    testPageCreation(m_dtoSourceFolder, null, m_sharedSourceFolder, null, true, false);
  }

  @Test
  public void testPageWithTableOnly() throws CoreException {
    testPageCreation(null, null, null, null, true, false);
  }

  @Test
  public void testPageWithNodesFull() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, m_testSourceFolder, false, false);
  }

  @Test
  public void testPageWithNodesNoTest() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, null, false, false);
  }

  @Test
  public void testPageWithNodesNoService() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, null, null, false, false);
  }

  @Test
  public void testPageWithNodesNoService2() throws CoreException {
    testPageCreation(m_dtoSourceFolder, null, m_sharedSourceFolder, null, false, false);
  }

  @Test
  public void testPageWithNodesOnly() throws CoreException {
    testPageCreation(null, null, null, null, false, false);
  }

  @Test
  public void testPageWithTableFullWithAbstract() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, m_testSourceFolder, true, true);
  }

  @Test
  public void testPageWithTableNoTestWithAbstract() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, null, true, true);
  }

  @Test
  public void testPageWithTableNoServiceWithAbstract() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, null, null, true, true);
  }

  @Test
  public void testPageWithTableNoService2WithAbstract() throws CoreException {
    testPageCreation(m_dtoSourceFolder, null, m_sharedSourceFolder, null, true, true);
  }

  @Test
  public void testPageWithTableOnlyWithAbstract() throws CoreException {
    testPageCreation(null, null, null, null, true, true);
  }

  @Test
  public void testPageWithNodesFullWithAbstract() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, m_testSourceFolder, false, true);
  }

  @Test
  public void testPageWithNodesNoTestWithAbstract() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, null, false, true);
  }

  @Test
  public void testPageWithNodesNoServiceWithAbstract() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, null, null, false, true);
  }

  @Test
  public void testPageWithNodesNoService2WithAbstract() throws CoreException {
    testPageCreation(m_dtoSourceFolder, null, m_sharedSourceFolder, null, false, true);
  }

  @Test
  public void testPageWithNodesOnlyWithAbstract() throws CoreException {
    testPageCreation(null, null, null, null, false, true);
  }

  protected void testPageCreation(IPackageFragmentRoot dtoSourceFolder, IPackageFragmentRoot serverSourceFolder, IPackageFragmentRoot sharedSourceFolder, IPackageFragmentRoot testSourceFolder,
      boolean isPageWithTable, final boolean isCreateAbstractPage) throws CoreException {
    if (isPageWithTable) {
      // define super class to use
      when(m_superType.getFullyQualifiedName()).thenReturn(IScoutRuntimeTypes.AbstractPageWithTable);

      // setup hierarchy for pageWithTable
      ITypeHierarchy hierarchy = m_superType.newSupertypeHierarchy(null);
      when(hierarchy.getAllTypes()).thenReturn(new IType[]{m_iPageWithTable});
    }
    else {
      // define super class to use
      when(m_superType.getFullyQualifiedName()).thenReturn(IScoutRuntimeTypes.AbstractPageWithNodes);
    }

    PageNewOperation pno = new PageNewOperation(m_envProvider) {
      @Override
      protected void updatePageDatas(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
        if (isCreateAbstractPage) {
          ScoutSdkCore.getDerivedResourceManager().trigger(Collections.singleton(getCreatedAbstractPage().getResource()));
        }
        ScoutSdkCore.getDerivedResourceManager().trigger(Collections.singleton(getCreatedPage().getResource()));
      }
    };
    pno.setClientSourceFolder(m_clientSourceFolder);
    pno.setPackage("org.eclipse.scout.sdk.s2e.client.test");
    String suffix = null;
    if (isPageWithTable) {
      suffix = ISdkProperties.SUFFIX_PAGE_WITH_TABLE;
    }
    else {
      suffix = ISdkProperties.SUFFIX_PAGE_WITH_NODES;
    }
    pno.setCreateAbstractPage(isCreateAbstractPage);
    pno.setPageName("My" + suffix);
    pno.setSuperType(m_superType);
    pno.setPageDataSourceFolder(dtoSourceFolder);
    pno.setServerSourceFolder(serverSourceFolder);
    pno.setSharedSourceFolder(sharedSourceFolder);
    pno.setTestSourceFolder(testSourceFolder);
    pno.validate();
    pno.run(new NullProgressMonitor(), ScoutSdkCore.createWorkingCopyManager());

    IJavaEnvironment env = m_envProvider.get(null);

    IType createdPage = Validate.notNull(pno.getCreatedPage());
    CoreTestingUtils.assertNoCompileErrors(env, createdPage.getFullyQualifiedName(), createdPage.getCompilationUnit().getSource());

    if (isCreateAbstractPage) {
      IType createdAbstractPage = Validate.notNull(pno.getCreatedAbstractPage());
      CoreTestingUtils.assertNoCompileErrors(env, createdAbstractPage.getFullyQualifiedName(), createdAbstractPage.getCompilationUnit().getSource());
    }

    if (isPageWithTable && S2eUtils.exists(dtoSourceFolder)) {
      IType createdPageData = Validate.notNull(pno.getCreatedPageData());
      CoreTestingUtils.assertNoCompileErrors(env, createdPageData.getFullyQualifiedName(), createdPageData.getCompilationUnit().getSource());

      if (isCreateAbstractPage) {
        IType createdAbstractPageData = Validate.notNull(pno.getCreatedAbstractPageData());
        CoreTestingUtils.assertNoCompileErrors(env, createdAbstractPageData.getFullyQualifiedName(), createdAbstractPageData.getCompilationUnit().getSource());
      }

      if (S2eUtils.exists(serverSourceFolder) && S2eUtils.exists(sharedSourceFolder)) {
        IType createdServiceIfc = pno.getCreatedServiceIfc();
        CoreTestingUtils.assertNoCompileErrors(env, createdServiceIfc.getFullyQualifiedName(), createdServiceIfc.getCompilationUnit().getSource());
        IType createdServiceImpl = pno.getCreatedServiceImpl();
        CoreTestingUtils.assertNoCompileErrors(env, createdServiceImpl.getFullyQualifiedName(), createdServiceImpl.getCompilationUnit().getSource());

        if (S2eUtils.exists(testSourceFolder)) {
          IType createdServiceTest = pno.getCreatedServiceTest();
          CoreTestingUtils.assertNoCompileErrors(env, createdServiceTest.getFullyQualifiedName(), createdServiceTest.getCompilationUnit().getSource());
        }
      }
    }
  }
}
