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
package org.eclipse.scout.sdk.s2e.operation.permission;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.testing.SdkPlatformTestRunner;
import org.eclipse.scout.sdk.s2e.testing.mock.PlatformMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * <h3>{@link PermissionNewOperationTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
@RunWith(SdkPlatformTestRunner.class)
public class PermissionNewOperationTest {

  @PlatformMock
  private IType m_superType;
  @PlatformMock
  private IPackageFragmentRoot m_sourceFolder;
  @PlatformMock
  private IJavaEnvironmentProvider m_envProvider;

  @Test
  public void testPermissionCreation() throws CoreException {
    Mockito.when(m_superType.getFullyQualifiedName()).thenReturn(IJavaRuntimeTypes.BasicPermission);

    PermissionNewOperation op = new PermissionNewOperation(m_envProvider);
    op.setPackage("org.eclipse.scout.sdk.s2e.shared.test");
    op.setPermissionName("My" + ISdkProperties.SUFFIX_PERMISSION);
    op.setSharedSourceFolder(m_sourceFolder);
    op.setSuperType(m_superType);
    op.validate();
    op.run(new NullProgressMonitor(), ScoutSdkCore.createWorkingCopyManager());

    IType createdPermission = op.getCreatedPermission();
    CoreTestingUtils.assertNoCompileErrors(m_envProvider.get(null), op.getPackage(), op.getPermissionName(), createdPermission.getCompilationUnit().getSource());
  }
}
