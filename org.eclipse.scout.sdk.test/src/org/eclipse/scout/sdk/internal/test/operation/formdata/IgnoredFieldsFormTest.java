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
package org.eclipse.scout.sdk.internal.test.operation.formdata;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.workspace.dto.FormDataDtoUpdateOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

public class IgnoredFieldsFormTest extends AbstractSdkTestWithFormDataProject {

  @Test
  public void testCreateFormData() throws Exception {
    String formName = "IgnoredFieldsForm";
    IType form = TypeUtility.getType("formdata.client.ui.forms." + formName);
    Assert.assertTrue(TypeUtility.exists(form));

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(form);
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, op);

    testApiOfIgnoredFieldsFormData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfIgnoredFieldsFormData() throws Exception {
    // type IgnoredFieldsFormData
    IType ignoredFieldsFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.IgnoredFieldsFormData");
    SdkAssert.assertHasFlags(ignoredFieldsFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(ignoredFieldsFormData, "QAbstractFormData;");

    // fields of IgnoredFieldsFormData
    SdkAssert.assertEquals("field count of 'IgnoredFieldsFormData'", 1, ignoredFieldsFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(ignoredFieldsFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'IgnoredFieldsFormData'", 2, ignoredFieldsFormData.getMethods().length);
    IMethod ignoredFieldsFormData1 = SdkAssert.assertMethodExist(ignoredFieldsFormData, "IgnoredFieldsFormData", new String[]{});
    SdkAssert.assertTrue(ignoredFieldsFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(ignoredFieldsFormData1, "V");
    IMethod getNotIgnored = SdkAssert.assertMethodExist(ignoredFieldsFormData, "getNotIgnored", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getNotIgnored, "QNotIgnored;");

    SdkAssert.assertEquals("inner types count of 'IgnoredFieldsFormData'", 1, ignoredFieldsFormData.getTypes().length);
    // type NotIgnored
    IType notIgnored = SdkAssert.assertTypeExists(ignoredFieldsFormData, "NotIgnored");
    SdkAssert.assertHasFlags(notIgnored, 9);
    SdkAssert.assertHasSuperTypeSignature(notIgnored, "QAbstractValueFieldData<QString;>;");

    // fields of NotIgnored
    SdkAssert.assertEquals("field count of 'NotIgnored'", 1, notIgnored.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(notIgnored, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'NotIgnored'", 2, notIgnored.getMethods().length);
    IMethod notIgnored1 = SdkAssert.assertMethodExist(notIgnored, "NotIgnored", new String[]{});
    SdkAssert.assertTrue(notIgnored1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(notIgnored1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(notIgnored, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'NotIgnored'", 0, notIgnored.getTypes().length);
  }
}
