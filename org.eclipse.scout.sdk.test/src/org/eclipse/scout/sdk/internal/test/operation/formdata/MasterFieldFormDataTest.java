/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test.operation.formdata;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.workspace.dto.FormDataDtoUpdateOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link MasterFieldFormDataTest}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 04.11.2013
 */
public class MasterFieldFormDataTest extends AbstractSdkTestWithFormDataProject {
  @Test
  public void testCreateFormData() throws Exception {
    String formName = "MasterFieldTestForm";
    IType form = TypeUtility.getType("formdata.client.ui.forms." + formName);
    Assert.assertTrue(TypeUtility.exists(form));

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(form);

    executeBuildAssertNoCompileErrors(op);

    testApiOfMasterFieldTestFormData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfMasterFieldTestFormData() throws Exception {
    // type MasterFieldTestFormData
    IType masterFieldTestFormData = SdkAssert.assertTypeExists("formdata.shared.services.MasterFieldTestFormData");
    SdkAssert.assertHasFlags(masterFieldTestFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(masterFieldTestFormData, "QAbstractFormData;");

    // fields of MasterFieldTestFormData
    SdkAssert.assertEquals("field count of 'MasterFieldTestFormData'", 1, masterFieldTestFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(masterFieldTestFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'MasterFieldTestFormData'", 3, masterFieldTestFormData.getMethods().length);
    IMethod masterFieldTestFormData1 = SdkAssert.assertMethodExist(masterFieldTestFormData, "MasterFieldTestFormData", new String[]{});
    SdkAssert.assertTrue(masterFieldTestFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(masterFieldTestFormData1, "V");
    IMethod getMyMaster = SdkAssert.assertMethodExist(masterFieldTestFormData, "getMyMaster", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getMyMaster, "QMyMaster;");
    IMethod getMySlave = SdkAssert.assertMethodExist(masterFieldTestFormData, "getMySlave", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getMySlave, "QMySlave;");

    SdkAssert.assertEquals("inner types count of 'MasterFieldTestFormData'", 2, masterFieldTestFormData.getTypes().length);
    // type MyMaster
    IType myMaster = SdkAssert.assertTypeExists(masterFieldTestFormData, "MyMaster");
    SdkAssert.assertHasFlags(myMaster, 9);
    SdkAssert.assertHasSuperTypeSignature(myMaster, "QAbstractValueFieldData<QString;>;");

    // fields of MyMaster
    SdkAssert.assertEquals("field count of 'MyMaster'", 1, myMaster.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(myMaster, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'MyMaster'", 2, myMaster.getMethods().length);
    IMethod myMaster1 = SdkAssert.assertMethodExist(myMaster, "MyMaster", new String[]{});
    SdkAssert.assertTrue(myMaster1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(myMaster1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(myMaster, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'MyMaster'", 0, myMaster.getTypes().length);
    // type MySlave
    IType mySlave = SdkAssert.assertTypeExists(masterFieldTestFormData, "MySlave");
    SdkAssert.assertHasFlags(mySlave, 9);
    SdkAssert.assertHasSuperTypeSignature(mySlave, "QAbstractValueFieldData<QString;>;");

    // fields of MySlave
    SdkAssert.assertEquals("field count of 'MySlave'", 1, mySlave.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(mySlave, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'MySlave'", 2, mySlave.getMethods().length);
    IMethod mySlave1 = SdkAssert.assertMethodExist(mySlave, "MySlave", new String[]{});
    SdkAssert.assertTrue(mySlave1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(mySlave1, "V");
    IMethod initValidationRules1 = SdkAssert.assertMethodExist(mySlave, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules1, "V");
    SdkAssert.assertAnnotation(initValidationRules1, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules1, new String[]{"ruleMap.put(ValidationRule.MASTER_VALUE_FIELD, MyMaster.class);", "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'MySlave'", 0, mySlave.getTypes().length);
  }
}
