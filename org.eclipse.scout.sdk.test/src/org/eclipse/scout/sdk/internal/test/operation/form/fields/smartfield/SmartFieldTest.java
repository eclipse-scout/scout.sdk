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
package org.eclipse.scout.sdk.internal.test.operation.form.fields.smartfield;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.operation.form.field.SmartFieldNewOperation;
import org.eclipse.scout.sdk.operation.jdt.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h3>{@link SmartFieldTest}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 13.02.2013
 */
public class SmartFieldTest extends AbstractScoutSdkTest {
  public static final String AbstractSmartFieldTypeName = "org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField";

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/operation/form/fields", "formfield.shared", "formfield.client");
  }

  @Test
  public void testCreateSmartField() throws Exception {
    Assert.assertTrue(TypeUtility.exists(createSmartField("EmptySmartField")));
  }

  @Test
  public void testExecParseValue01() throws Exception {
    IType field = createSmartField("ExecParseValueField");
    MethodOverrideOperation mop = new MethodOverrideOperation("execParseValue", field);
    executeBuildAssertNoCompileErrors(mop);
    SdkAssert.assertExist(mop.getCreatedMethod());

    Assert.assertEquals(Signature.createTypeSignature(Long.class.getName(), true), SignatureUtility.getResolvedSignature(mop.getCreatedMethod().getReturnType(), field));
    Assert.assertTrue(Flags.isProtected(mop.getCreatedMethod().getFlags()));

  }

  @Test
  public void testExecFormatValue01() throws Exception {
    IType field = createSmartField("ExecFormatValueField01");
    MethodOverrideOperation mop = new MethodOverrideOperation("execFormatValue", field);
    executeBuildAssertNoCompileErrors(mop);
    SdkAssert.assertExist(mop.getCreatedMethod());

    Assert.assertTrue(Flags.isProtected(mop.getCreatedMethod().getFlags()));
    Assert.assertEquals(1, mop.getCreatedMethod().getParameterTypes().length);
    Assert.assertEquals(Signature.createTypeSignature(Long.class.getName(), true), SignatureUtility.getResolvedSignature(mop.getCreatedMethod().getParameterTypes()[0], field));
  }

  @Test
  public void testExecFormatValue02() throws Exception {
    IType field = createSmartField("ExecFormatValueField02", Signature.createTypeSignature(AbstractSmartFieldTypeName + "<" + Map.class.getName() + "<" + List.class.getName() + "<" + String.class.getName() + ">," + File.class.getName() + ">>", true));
    MethodOverrideOperation mop = new MethodOverrideOperation("execFormatValue", field);
    executeBuildAssertNoCompileErrors(mop);
    SdkAssert.assertExist(mop.getCreatedMethod());
    Assert.assertTrue(Flags.isProtected(mop.getCreatedMethod().getFlags()));
    Assert.assertEquals(1, mop.getCreatedMethod().getParameterTypes().length);
    Assert.assertEquals(Signature.createTypeSignature(Map.class.getName() + "<" + List.class.getName() + "<" + String.class.getName() + ">," + File.class.getName() + ">", true), SignatureUtility.getResolvedSignature(mop.getCreatedMethod().getParameterTypes()[0], field));
  }

  private IType createSmartField(String name) throws Exception {
    return createSmartField(name, Signature.createTypeSignature(AbstractSmartFieldTypeName + "<" + Long.class.getName() + ">", true));
  }

  private IType createSmartField(String name, String superTypeSignature) throws Exception {
    IType mainBox = TypeUtility.getType("formfield.client.ui.forms.DesktopForm.MainBox");
    SmartFieldNewOperation fieldOp = new SmartFieldNewOperation(name, mainBox);
    fieldOp.setSuperTypeSignature(superTypeSignature);
    executeBuildAssertNoCompileErrors(fieldOp);
    return fieldOp.getCreatedField();
  }

}
