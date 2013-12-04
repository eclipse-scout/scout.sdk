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
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataDtoUpdateOperation;
import org.junit.Assert;
import org.junit.Test;

public class FormWithGroupboxesTest extends AbstractSdkTestWithFormDataProject {

  @Test
  public void testCreateFormData() throws Exception {
    String formName = "FormWithGroupBoxesForm";
    IType form = TypeUtility.getType("formdata.client.ui.forms." + formName);
    Assert.assertTrue(TypeUtility.exists(form));

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(form);
    executeBuildAssertNoCompileErrors(op);

    testApiOfFormWithGroupBoxesFormData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfFormWithGroupBoxesFormData() throws Exception {
    // type FormWithGroupBoxesFormData
    IType formWithGroupBoxesFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.FormWithGroupBoxesFormData");
    SdkAssert.assertHasFlags(formWithGroupBoxesFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(formWithGroupBoxesFormData, "QAbstractFormData;");

    // fields of FormWithGroupBoxesFormData
    SdkAssert.assertEquals("field count of 'FormWithGroupBoxesFormData'", 1, formWithGroupBoxesFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(formWithGroupBoxesFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'FormWithGroupBoxesFormData'", 3, formWithGroupBoxesFormData.getMethods().length);
    IMethod formWithGroupBoxesFormData1 = SdkAssert.assertMethodExist(formWithGroupBoxesFormData, "FormWithGroupBoxesFormData", new String[]{});
    SdkAssert.assertTrue(formWithGroupBoxesFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(formWithGroupBoxesFormData1, "V");
    IMethod getFlatString = SdkAssert.assertMethodExist(formWithGroupBoxesFormData, "getFlatString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFlatString, "QFlatString;");
    IMethod getInnerInteger = SdkAssert.assertMethodExist(formWithGroupBoxesFormData, "getInnerInteger", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getInnerInteger, "QInnerInteger;");

    SdkAssert.assertEquals("inner types count of 'FormWithGroupBoxesFormData'", 2, formWithGroupBoxesFormData.getTypes().length);
    // type FlatString
    IType flatString = SdkAssert.assertTypeExists(formWithGroupBoxesFormData, "FlatString");
    SdkAssert.assertHasFlags(flatString, 9);
    SdkAssert.assertHasSuperTypeSignature(flatString, "QAbstractValueFieldData<QString;>;");

    // fields of FlatString
    SdkAssert.assertEquals("field count of 'FlatString'", 1, flatString.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(flatString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'FlatString'", 2, flatString.getMethods().length);
    IMethod flatString1 = SdkAssert.assertMethodExist(flatString, "FlatString", new String[]{});
    SdkAssert.assertTrue(flatString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(flatString1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(flatString, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'FlatString'", 0, flatString.getTypes().length);
    // type InnerInteger
    IType innerInteger = SdkAssert.assertTypeExists(formWithGroupBoxesFormData, "InnerInteger");
    SdkAssert.assertHasFlags(innerInteger, 9);
    SdkAssert.assertHasSuperTypeSignature(innerInteger, "QAbstractValueFieldData<QInteger;>;");

    // fields of InnerInteger
    SdkAssert.assertEquals("field count of 'InnerInteger'", 1, innerInteger.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(innerInteger, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'InnerInteger'", 2, innerInteger.getMethods().length);
    IMethod innerInteger1 = SdkAssert.assertMethodExist(innerInteger, "InnerInteger", new String[]{});
    SdkAssert.assertTrue(innerInteger1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(innerInteger1, "V");
    IMethod initValidationRules1 = SdkAssert.assertMethodExist(innerInteger, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules1, "V");
    SdkAssert.assertAnnotation(initValidationRules1, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules1, new String[]{"ruleMap.put(ValidationRule.MAX_VALUE, Integer.MAX_VALUE);", "ruleMap.put(ValidationRule.MIN_VALUE, Integer.MIN_VALUE);"}, true);

    SdkAssert.assertEquals("inner types count of 'InnerInteger'", 0, innerInteger.getTypes().length);
  }

}
