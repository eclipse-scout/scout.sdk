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
package org.eclipse.scout.sdk.internal.test.operation.form;

import java.io.File;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.form.FormNewOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldDeleteOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link FormNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2013
 */
public class FormNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewForm() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    String typeName = "TestForm01";
    String packageName = "sample.client.form.output";
    FormNewOperation formOp = new FormNewOperation(typeName, packageName, getClientJavaProject());
    formOp.setFormatSource(true);
    formOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IForm, getClientJavaProject()));
    executeBuildAssertNoCompileErrors(formOp);
    SdkAssert.assertExist(formOp.getCreatedType());
    SdkAssert.assertExist(formOp.getCreatedMainBox());
    PluginModelHelper h = new PluginModelHelper(getClientProject());
    SdkAssert.assertTrue(h.Manifest.existsExportPackage(packageName));

    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(formOp.getCreatedType(), true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewFormWithCancelButton() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    String typeName = "TestForm02";
    String packageName = "sample.client.form.output";
    FormNewOperation formOp = new FormNewOperation(typeName, packageName, getClientJavaProject());
    formOp.setFormatSource(true);
    formOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IForm, getClientJavaProject()));
    formOp.setCreateButtonCancel(true);
    executeBuildAssertNoCompileErrors(formOp);
    SdkAssert.assertExist(formOp.getCreatedType());
    SdkAssert.assertExist(formOp.getCreatedMainBox());

    // check cancel button
    IType cancelButton = SdkAssert.assertTypeExists(formOp.getCreatedMainBox(), SdkProperties.TYPE_NAME_CANCEL_BUTTON);
    SdkAssert.assertPublic(cancelButton).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(cancelButton, Double.valueOf(10));
    IMethod cancelGetter = SdkAssert.assertMethodExist(formOp.getCreatedType(), "get" + SdkProperties.TYPE_NAME_CANCEL_BUTTON);
    SdkAssert.assertPublic(cancelGetter).assertNoMoreFlags();

    SdkAssert.assertEquals(formOp.getCreatedMainBox().getTypes().length, 1);
    // check manifest
    PluginModelHelper h = new PluginModelHelper(getClientProject());
    SdkAssert.assertTrue(h.Manifest.existsExportPackage(packageName));

    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(formOp.getCreatedType(), true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewFormWithButtons() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    String typeName = "TestForm03";
    String packageName = "sample.client.form.output";
    FormNewOperation formOp = new FormNewOperation(typeName, packageName, getClientJavaProject());
    formOp.setFormatSource(true);
    formOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IForm, getClientJavaProject()));
    formOp.setCreateButtonCancel(true);
    formOp.setCreateButtonOk(true);
    executeBuildAssertNoCompileErrors(formOp);
    SdkAssert.assertExist(formOp.getCreatedType());
    SdkAssert.assertExist(formOp.getCreatedMainBox());
    // check ok button
    IType okButton = SdkAssert.assertTypeExists(formOp.getCreatedMainBox(), SdkProperties.TYPE_NAME_OK_BUTTON);
    SdkAssert.assertPublic(okButton).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(10));
    IMethod okGetter = SdkAssert.assertMethodExist(formOp.getCreatedType(), "get" + SdkProperties.TYPE_NAME_OK_BUTTON);
    SdkAssert.assertPublic(okGetter).assertNoMoreFlags();

    // check cancel button
    IType cancelButton = SdkAssert.assertTypeExists(formOp.getCreatedMainBox(), SdkProperties.TYPE_NAME_CANCEL_BUTTON);
    SdkAssert.assertPublic(cancelButton).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(cancelButton, Double.valueOf(20));
    IMethod cancelGetter = SdkAssert.assertMethodExist(formOp.getCreatedType(), "get" + SdkProperties.TYPE_NAME_CANCEL_BUTTON);
    SdkAssert.assertPublic(cancelGetter).assertNoMoreFlags();

    SdkAssert.assertEquals(formOp.getCreatedMainBox().getTypes().length, 2);

    // check manifest
    PluginModelHelper h = new PluginModelHelper(getClientProject());
    SdkAssert.assertTrue(h.Manifest.existsExportPackage(packageName));

    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(formOp.getCreatedType(), true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewFormWithFormId() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    String typeName = "TestForm01";
    String packageName = "sample.client.form.output";
    FormNewOperation formOp = new FormNewOperation(typeName, packageName, getClientJavaProject());
    formOp.setFormatSource(true);
    formOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IForm, getClientJavaProject()));
    formOp.setFormIdName("testId");
    formOp.setFormIdSignature(Signature.createTypeSignature(File.class.getName(), true));
    executeBuildAssertNoCompileErrors(formOp);
    SdkAssert.assertExist(formOp.getCreatedType());
    SdkAssert.assertExist(formOp.getCreatedMainBox());
    SdkAssert.assertMethodExist(formOp.getCreatedType(), "getTestId");
    SdkAssert.assertMethodExist(formOp.getCreatedType(), "setTestId");

    PluginModelHelper h = new PluginModelHelper(getClientProject());
    SdkAssert.assertTrue(h.Manifest.existsExportPackage(packageName));

    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(formOp.getCreatedType(), true);
    executeBuildAssertNoCompileErrors(delOp);
  }

}
