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
package org.eclipse.scout.sdk.dto.test;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.dto.test.util.CoreScoutTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link MasterFieldFormDataTest}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 04.11.2013
 */
public class MasterFieldFormDataTest {
  @Test
  public void testCreateFormData() {
    String formName = "MasterFieldTestForm";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms." + formName);
    testApiOfMasterFieldTestFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfMasterFieldTestFormData(IType masterFieldTestFormData) {
    // type MasterFieldTestFormData
    SdkAssert.assertHasFlags(masterFieldTestFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(masterFieldTestFormData, "QAbstractFormData;");

    // fields of MasterFieldTestFormData
    Assert.assertEquals("field count of 'MasterFieldTestFormData'", 1, masterFieldTestFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(masterFieldTestFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'MasterFieldTestFormData'", 3, masterFieldTestFormData.methods().list().size());
    IMethod masterFieldTestFormData1 = SdkAssert.assertMethodExist(masterFieldTestFormData, "MasterFieldTestFormData", new String[]{});
    Assert.assertTrue(masterFieldTestFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(masterFieldTestFormData1, null);
    IMethod getMyMaster = SdkAssert.assertMethodExist(masterFieldTestFormData, "getMyMaster", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getMyMaster, "QMyMaster;");
    IMethod getMySlave = SdkAssert.assertMethodExist(masterFieldTestFormData, "getMySlave", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getMySlave, "QMySlave;");

    Assert.assertEquals("inner types count of 'MasterFieldTestFormData'", 2, masterFieldTestFormData.innerTypes().list().size());
    // type MyMaster
    IType myMaster = SdkAssert.assertTypeExists(masterFieldTestFormData, "MyMaster");
    SdkAssert.assertHasFlags(myMaster, 9);
    SdkAssert.assertHasSuperTypeSignature(myMaster, "QAbstractValueFieldData<QString;>;");

    // fields of MyMaster
    Assert.assertEquals("field count of 'MyMaster'", 1, myMaster.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(myMaster, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'MyMaster'", 1, myMaster.methods().list().size());
    IMethod myMaster1 = SdkAssert.assertMethodExist(myMaster, "MyMaster", new String[]{});
    Assert.assertTrue(myMaster1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(myMaster1, null);

    Assert.assertEquals("inner types count of 'MyMaster'", 0, myMaster.innerTypes().list().size());
    // type MySlave
    IType mySlave = SdkAssert.assertTypeExists(masterFieldTestFormData, "MySlave");
    SdkAssert.assertHasFlags(mySlave, 9);
    SdkAssert.assertHasSuperTypeSignature(mySlave, "QAbstractValueFieldData<QString;>;");

    // fields of MySlave
    Assert.assertEquals("field count of 'MySlave'", 1, mySlave.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(mySlave, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'MySlave'", 1, mySlave.methods().list().size());
    IMethod mySlave1 = SdkAssert.assertMethodExist(mySlave, "MySlave", new String[]{});
    Assert.assertTrue(mySlave1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(mySlave1, null);

    Assert.assertEquals("inner types count of 'MySlave'", 0, mySlave.innerTypes().list().size());
  }
}
