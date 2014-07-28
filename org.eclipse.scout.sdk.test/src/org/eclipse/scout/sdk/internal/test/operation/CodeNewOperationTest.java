/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test.operation;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.CodeNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link CodeNewOperationTest}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class CodeNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewCode() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType countryCodeType = TypeUtility.getType("sample.shared.services.code.CountryCodeType");
    CodeNewOperation codeOp = new CodeNewOperation(countryCodeType);
    codeOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.shared.services.common.code.AbstractCode<" + Long.class.getName() + ">", true));
    codeOp.setTypeName("ItalyCode");
    executeBuildAssertNoCompileErrors(codeOp);
    IType createdCode = codeOp.getCreatedCode();
    SdkAssert.assertPublic(createdCode).assertStatic().assertNoMoreFlags();
    SdkAssert.assertExist(createdCode);
    IMethod getIdMethod = SdkAssert.assertMethodExist(createdCode, "getId");
    SdkAssert.assertPublic(getIdMethod).assertNoMoreFlags();
    IField field = SdkAssert.assertFieldExist(createdCode, "ID");
    SdkAssert.assertPublic(field).assertFinal().assertStatic().assertNoMoreFlags();

  }
}
