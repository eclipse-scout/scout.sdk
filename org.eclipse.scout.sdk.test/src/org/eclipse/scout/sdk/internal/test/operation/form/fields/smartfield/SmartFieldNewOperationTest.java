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

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.form.field.FormFieldDeleteOperation;
import org.eclipse.scout.sdk.operation.form.field.SmartFieldNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link SmartFieldNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2013
 */
public class SmartFieldNewOperationTest extends AbstractSdkTestWithSampleProject {
  @Test
  public void testApi() throws Exception {
    IType abstractSmartField = SdkAssert.assertTypeExists("org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField");
    SdkAssert.assertMethodExistInSuperTypeHierarchy(abstractSmartField, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
    SdkAssert.assertMethodExistInSuperTypeHierarchy(abstractSmartField, "getConfiguredCodeType");
    SdkAssert.assertMethodExistInSuperTypeHierarchy(abstractSmartField, "getConfiguredLookupCall");
  }

  @Test
  public void testNewField() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    SmartFieldNewOperation fieldNewOp = new SmartFieldNewOperation("TestField01", mainBox);
    fieldNewOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ISmartField, mainBox.getJavaProject()));
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(10));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(20));
    SdkAssert.assertHasSuperType(field, RuntimeClasses.ISmartField);
    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewFieldWithNlsText() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    SmartFieldNewOperation fieldNewOp = new SmartFieldNewOperation("TestField02", mainBox);
    fieldNewOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ISmartField, mainBox.getJavaProject()));
    //nls
    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(getSharedJavaProject());
    INlsEntry entry = nlsProject.getEntry("Text02");
    fieldNewOp.setNlsEntry(entry);

    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(10));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(20));
    SdkAssert.assertHasSuperType(field, RuntimeClasses.ISmartField);
    SdkAssert.assertMethodExist(field, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);

    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewFieldWithCodeType() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    SmartFieldNewOperation fieldNewOp = new SmartFieldNewOperation("TestField01", mainBox);
    fieldNewOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField<java.lang.Long>", true));
    fieldNewOp.setCodeType(TypeUtility.getType("sample.shared.services.code.CountryCodeType"));
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(10));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(20));
    SdkAssert.assertHasSuperType(field, RuntimeClasses.ISmartField);
    SdkAssert.assertMethodExist(field, "getConfiguredCodeType");
    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewFieldWithLookupCall() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    SmartFieldNewOperation fieldNewOp = new SmartFieldNewOperation("TestField01", mainBox);
    fieldNewOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField<java.lang.Long>", true));
    fieldNewOp.setLookupCall(TypeUtility.getType("sample.shared.services.lookup.CountryLookupCall"));
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(10));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(20));
    SdkAssert.assertHasSuperType(field, RuntimeClasses.ISmartField);
    SdkAssert.assertMethodExist(field, "getConfiguredLookupCall");
    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }
}
