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
package org.eclipse.scout.sdk.s2e.operation.lookupcall;

import static org.mockito.Mockito.when;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.testing.SdkPlatformTestRunner;
import org.eclipse.scout.sdk.s2e.testing.mock.PlatformMock;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h3>{@link LookupCallNewOperationTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
@RunWith(SdkPlatformTestRunner.class)
public class LookupCallNewOperationTest {
  @PlatformMock
  private IJavaEnvironmentProvider m_envProvider;
  @PlatformMock
  private IPackageFragmentRoot m_sharedSourceFolder;
  @PlatformMock
  private IPackageFragmentRoot m_serverSourceFolder;
  @PlatformMock
  private IPackageFragmentRoot m_testSourceFolder;
  @PlatformMock
  private IType m_superType;
  @PlatformMock
  private IType m_keyType;
  @PlatformMock
  private IType m_lookupSvcSuperType;

  @Test
  public void testLookupCallFull() throws CoreException {
    testLookupCallCreation(false, IJavaRuntimeTypes.Double, IScoutRuntimeTypes.AbstractLookupService, m_serverSourceFolder, m_testSourceFolder);
  }

  @Test
  public void testLookupCallFullClient() throws CoreException {
    testLookupCallCreation(true, IJavaRuntimeTypes.Double, IScoutRuntimeTypes.AbstractLookupService, m_serverSourceFolder, m_testSourceFolder);
  }

  @Test
  public void testLookupCallNoTest() throws CoreException {
    testLookupCallCreation(false, IJavaRuntimeTypes.Double, IScoutRuntimeTypes.AbstractLookupService, m_serverSourceFolder, null);
  }

  @Test
  public void testLookupCallNoService1() throws CoreException {
    testLookupCallCreation(false, IJavaRuntimeTypes.Double, IScoutRuntimeTypes.AbstractLookupService, null, m_testSourceFolder);
  }

  @Test
  public void testLookupCallNoService2() throws CoreException {
    testLookupCallCreation(false, IJavaRuntimeTypes.Double, null, m_serverSourceFolder, m_testSourceFolder);
  }

  @Test
  public void testLookupCallOnly() throws CoreException {
    testLookupCallCreation(false, IJavaRuntimeTypes.Double, null, null, null);
  }

  protected void testLookupCallCreation(boolean isClient, String keyFqn, String lookupServiceSuperTypeFqn, IPackageFragmentRoot serverSourceFolder, IPackageFragmentRoot testSourceFolder) throws CoreException {
    when(m_keyType.getFullyQualifiedName()).thenReturn(Validate.notNull(keyFqn));
    when(m_superType.getFullyQualifiedName()).thenReturn(IScoutRuntimeTypes.LookupCall);

    if (testSourceFolder != null) {
      IJavaProject testProject = testSourceFolder.getJavaProject();
      // just return any type if a server session is searched in the test source folder. This is because the ScoutTier.valueOf() should return "server".
      if (isClient) {
        when(testProject.findType(IScoutRuntimeTypes.IClientSession)).thenReturn(m_keyType);
      }
      else {
        when(testProject.findType(IScoutRuntimeTypes.IServerSession)).thenReturn(m_keyType);
      }
    }

    IType lookupSvcSuperType = null;
    if (lookupServiceSuperTypeFqn != null) {
      when(m_lookupSvcSuperType.getFullyQualifiedName()).thenReturn(lookupServiceSuperTypeFqn);
      lookupSvcSuperType = m_lookupSvcSuperType;
    }

    LookupCallNewOperation op = new LookupCallNewOperation(m_envProvider);
    op.setKeyType(m_keyType);
    op.setLookupCallName("My" + ISdkProperties.SUFFIX_LOOKUP_CALL);
    op.setLookupServiceSuperType(lookupSvcSuperType);
    op.setPackage("org.eclipse.scout.sdk.s2e.shared.test");
    op.setServerSourceFolder(serverSourceFolder);
    op.setSharedSourceFolder(m_sharedSourceFolder);
    op.setSuperType(m_superType);
    op.setTestSourceFolder(testSourceFolder);
    op.validate();
    op.run(new NullProgressMonitor(), ScoutSdkCore.createWorkingCopyManager());

    IJavaEnvironment env = m_envProvider.get(null);
    IType createdLookupCall = Validate.notNull(op.getCreatedLookupCall());
    CoreTestingUtils.assertNoCompileErrors(env, createdLookupCall.getFullyQualifiedName(), createdLookupCall.getCompilationUnit().getSource());

    if (S2eUtils.exists(testSourceFolder)) {
      IType createdLookupCallTest = Validate.notNull(op.getCreatedLookupCallTest());
      CoreTestingUtils.assertNoCompileErrors(env, createdLookupCallTest.getFullyQualifiedName(), createdLookupCallTest.getCompilationUnit().getSource());
    }

    if (S2eUtils.exists(serverSourceFolder) && S2eUtils.exists(lookupSvcSuperType)) {
      IType createdLookupSvcIfc = Validate.notNull(op.getCreatedLookupServiceIfc());
      CoreTestingUtils.assertNoCompileErrors(env, createdLookupSvcIfc.getFullyQualifiedName(), createdLookupSvcIfc.getCompilationUnit().getSource());

      IType createdLookupSvcImpl = Validate.notNull(op.getCreatedLookupServiceImpl());
      CoreTestingUtils.assertNoCompileErrors(env, createdLookupSvcImpl.getFullyQualifiedName(), createdLookupSvcImpl.getCompilationUnit().getSource());
    }

  }
}
