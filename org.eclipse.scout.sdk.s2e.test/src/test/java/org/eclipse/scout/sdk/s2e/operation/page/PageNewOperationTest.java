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

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
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
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, m_testSourceFolder, true);
  }

  @Test
  public void testPageWithTableNoTest() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, null, true);
  }

  @Test
  public void testPageWithTableNoService() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, null, null, true);
  }

  @Test
  public void testPageWithTableNoService2() throws CoreException {
    testPageCreation(m_dtoSourceFolder, null, m_sharedSourceFolder, null, true);
  }

  @Test
  public void testPageWithTableOnly() throws CoreException {
    testPageCreation(null, null, null, null, true);
  }

  @Test
  public void testPageWithNodesFull() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, m_testSourceFolder, false);
  }

  @Test
  public void testPageWithNodesNoTest() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, m_sharedSourceFolder, null, false);
  }

  @Test
  public void testPageWithNodesNoService() throws CoreException {
    testPageCreation(m_dtoSourceFolder, m_serverSourceFolder, null, null, false);
  }

  @Test
  public void testPageWithNodesNoService2() throws CoreException {
    testPageCreation(m_dtoSourceFolder, null, m_sharedSourceFolder, null, false);
  }

  @Test
  public void testPageWithNodesOnly() throws CoreException {
    testPageCreation(null, null, null, null, false);
  }

  protected void testPageCreation(IPackageFragmentRoot dtoSourceFolder, IPackageFragmentRoot serverSourceFolder, IPackageFragmentRoot sharedSourceFolder, IPackageFragmentRoot testSourceFolder, boolean isPageWithTable) throws CoreException {
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

    PageNewOperation pno = new PageNewOperation(m_envProvider);
    pno.setClientSourceFolder(m_clientSourceFolder);
    pno.setPackage("org.eclipse.scout.sdk.s2e.client.test");
    pno.setPageName("My" + ISdkProperties.SUFFIX_PAGE);
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

    if (isPageWithTable && S2eUtils.exists(dtoSourceFolder)) {
      IType createdPageData = Validate.notNull(pno.getCreatedPageData());
      CoreTestingUtils.assertNoCompileErrors(env, createdPageData.getFullyQualifiedName(), createdPageData.getCompilationUnit().getSource());

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
