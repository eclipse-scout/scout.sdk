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

import static org.mockito.Mockito.when;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
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
 * <h3>{@link FormNewOperationTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
@RunWith(SdkPlatformTestRunner.class)
public class FormNewOperationTest {
  @PlatformMock
  private IType m_superType;
  @PlatformMock
  private IJavaEnvironmentProvider m_envProvider;
  @PlatformMock
  private IPackageFragmentRoot m_clientSourceFolder;
  @PlatformMock
  private IPackageFragmentRoot m_serverSourceFolder;
  @PlatformMock
  private IPackageFragmentRoot m_sharedSourceFolder;
  @PlatformMock
  private IPackageFragmentRoot m_clientTestSourceFolder;
  @PlatformMock
  private IPackageFragmentRoot m_serverTestSourceFolder;

  @Test
  public void testFormFull() throws CoreException {
    testFormCreation(true, true, true, m_clientTestSourceFolder, m_serverTestSourceFolder);
  }

  @Test
  public void testFormNoDto() throws CoreException {
    testFormCreation(false, true, true, m_clientTestSourceFolder, m_serverTestSourceFolder);
  }

  @Test
  public void testFormNoPermissions() throws CoreException {
    testFormCreation(true, false, true, m_clientTestSourceFolder, m_serverTestSourceFolder);
  }

  @Test
  public void testFormNoService() throws CoreException {
    testFormCreation(true, true, false, m_clientTestSourceFolder, m_serverTestSourceFolder);
  }

  @Test
  public void testFormOnly() throws CoreException {
    testFormCreation(false, false, false, null, null);
  }

  @Test
  public void testFormWithTests() throws CoreException {
    testFormCreation(false, false, false, m_clientTestSourceFolder, m_serverTestSourceFolder);
  }

  @Test
  public void testFormWithServiceAndTest() throws CoreException {
    testFormCreation(false, false, true, m_clientTestSourceFolder, m_serverTestSourceFolder);
  }

  @Test
  public void testFormWithServiceAndNoTests() throws CoreException {
    testFormCreation(false, false, true, null, null);
  }

  @Test
  public void testFormWithDtoOnly() throws CoreException {
    testFormCreation(true, false, false, m_clientTestSourceFolder, m_serverTestSourceFolder);
  }

  @Test
  public void testFormWithPermissionsOnly() throws CoreException {
    testFormCreation(false, true, false, m_clientTestSourceFolder, m_serverTestSourceFolder);
  }

  @Test
  public void testFormWithTest() throws CoreException {
    testFormCreation(false, false, false, m_clientTestSourceFolder, null);
  }

  @Test
  public void testFormWithoutServiceButWithServiceTest() throws CoreException {
    testFormCreation(true, true, false, null, m_serverTestSourceFolder);
  }

  protected void testFormCreation(boolean isCreateFormData, boolean isCreatePermissions, boolean isCreateService, IPackageFragmentRoot clientTestSourceFolder, IPackageFragmentRoot serverTestSourceFolder) throws CoreException {
    when(m_superType.getFullyQualifiedName()).thenReturn(IScoutRuntimeTypes.AbstractForm);

    FormNewOperation fno = new FormNewOperation(m_envProvider);
    fno.setClientPackage("org.eclipse.scout.sdk.s2e.client.test");
    fno.setClientSourceFolder(m_clientSourceFolder);
    fno.setClientTestSourceFolder(clientTestSourceFolder);
    fno.setCreateFormData(isCreateFormData);
    fno.setCreatePermissions(isCreatePermissions);
    fno.setCreateService(isCreateService);
    fno.setFormDataSourceFolder(m_sharedSourceFolder);
    fno.setFormName("My" + ISdkProperties.SUFFIX_FORM);
    fno.setServerSourceFolder(m_serverSourceFolder);
    fno.setServerTestSourceFolder(serverTestSourceFolder);
    fno.setSharedSourceFolder(m_sharedSourceFolder);
    fno.setSuperType(m_superType);
    fno.validate();
    fno.run(new NullProgressMonitor(), ScoutSdkCore.createWorkingCopyManager());

    IJavaEnvironment env = m_envProvider.get(null);

    IType createdForm = Validate.notNull(fno.getCreatedForm());
    CoreTestingUtils.assertNoCompileErrors(env, createdForm.getFullyQualifiedName(), createdForm.getCompilationUnit().getSource());

    if (isCreateFormData) {
      IType createdFormData = Validate.notNull(fno.getCreatedFormData());
      CoreTestingUtils.assertNoCompileErrors(env, createdFormData.getFullyQualifiedName(), createdFormData.getCompilationUnit().getSource());
    }

    if (isCreatePermissions) {
      IType createdUpdatePermission = Validate.notNull(fno.getCreatedUpdatePermission());
      CoreTestingUtils.assertNoCompileErrors(env, createdUpdatePermission.getFullyQualifiedName(), createdUpdatePermission.getCompilationUnit().getSource());

      IType createdCreatePermission = Validate.notNull(fno.getCreatedCreatePermission());
      CoreTestingUtils.assertNoCompileErrors(env, createdCreatePermission.getFullyQualifiedName(), createdCreatePermission.getCompilationUnit().getSource());

      IType createdReadPermission = Validate.notNull(fno.getCreatedReadPermission());
      CoreTestingUtils.assertNoCompileErrors(env, createdReadPermission.getFullyQualifiedName(), createdReadPermission.getCompilationUnit().getSource());
    }
    if (isCreateService) {
      IType createdServiceImpl = Validate.notNull(fno.getCreatedServiceImpl());
      CoreTestingUtils.assertNoCompileErrors(env, createdServiceImpl.getFullyQualifiedName(), createdServiceImpl.getCompilationUnit().getSource());

      IType createdServiceIfc = Validate.notNull(fno.getCreatedServiceInterface());
      CoreTestingUtils.assertNoCompileErrors(env, createdServiceIfc.getFullyQualifiedName(), createdServiceIfc.getCompilationUnit().getSource());
    }

    if (S2eUtils.exists(clientTestSourceFolder)) {
      IType createdFormTest = Validate.notNull(fno.getCreatedFormTest());
      CoreTestingUtils.assertNoCompileErrors(env, createdFormTest.getFullyQualifiedName(), createdFormTest.getCompilationUnit().getSource());
    }

    if (isCreateService && S2eUtils.exists(serverTestSourceFolder)) {
      IType createdServiceTest = Validate.notNull(fno.getCreatedServiceTest());
      CoreTestingUtils.assertNoCompileErrors(env, createdServiceTest.getFullyQualifiedName(), createdServiceTest.getCompilationUnit().getSource());
    }
  }
}
