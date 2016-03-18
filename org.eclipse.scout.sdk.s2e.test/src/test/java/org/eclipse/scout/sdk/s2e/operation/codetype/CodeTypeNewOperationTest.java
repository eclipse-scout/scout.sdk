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
package org.eclipse.scout.sdk.s2e.operation.codetype;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.testing.SdkPlatformTestRunner;
import org.eclipse.scout.sdk.s2e.testing.mock.PlatformMock;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h3>{@link CodeTypeNewOperationTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
@RunWith(SdkPlatformTestRunner.class)
public class CodeTypeNewOperationTest {

  @PlatformMock
  private IJavaEnvironmentProvider m_envProvider;
  @PlatformMock
  private IPackageFragmentRoot m_sharedSourceFolder;

  @Test
  public void testCodeTypeLongInteger() throws CoreException {
    testCodeTypeCreation(IJavaRuntimeTypes.Long, IJavaRuntimeTypes.Integer);
  }

  @Test
  public void testCodeTypeComplex() throws CoreException {
    String codeTypeIdFqn = IJavaRuntimeTypes.List + ISignatureConstants.C_GENERIC_START + IJavaRuntimeTypes.BigDecimal + ISignatureConstants.C_GENERIC_END;
    String codeIdFqn = IJavaRuntimeTypes.Map + ISignatureConstants.C_GENERIC_START + IJavaRuntimeTypes.BigDecimal + ", " + IJavaRuntimeTypes.CharSequence + ISignatureConstants.C_GENERIC_END;
    testCodeTypeCreation(codeTypeIdFqn, codeIdFqn);
  }

  protected void testCodeTypeCreation(String codeTypeIdFqn, String codeIdFqn) throws CoreException {
    String superTypeFqn = IScoutRuntimeTypes.AbstractCodeType + ISignatureConstants.C_GENERIC_START + codeTypeIdFqn + ", " + codeIdFqn + ISignatureConstants.C_GENERIC_END;

    CodeTypeNewOperation op = new CodeTypeNewOperation(m_envProvider);
    op.setCodeTypeIdSignature(Signature.createTypeSignature(codeTypeIdFqn));
    op.setCodeTypeName("My" + ISdkProperties.SUFFIX_CODE_TYPE);
    op.setPackage("org.eclipse.scout.sdk.s2e.shared.test");
    op.setSharedSourceFolder(m_sharedSourceFolder);
    op.setSuperTypeSignature(Signature.createTypeSignature(superTypeFqn));
    op.validate();
    op.run(new NullProgressMonitor(), ScoutSdkCore.createWorkingCopyManager());

    IType createdCodeType = Validate.notNull(op.getCreatedCodeType());
    CoreTestingUtils.assertNoCompileErrors(m_envProvider.get(null), createdCodeType.getFullyQualifiedName(), createdCodeType.getCompilationUnit().getSource());
  }
}
