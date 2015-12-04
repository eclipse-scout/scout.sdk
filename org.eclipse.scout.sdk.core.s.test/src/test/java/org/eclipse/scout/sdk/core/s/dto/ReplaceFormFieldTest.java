/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

public class ReplaceFormFieldTest {

  private static final String BaseFormFqn = "formdata.client.ui.forms.replace.BaseForm";
  private static final String ExtendedFormFqn = "formdata.client.ui.forms.replace.ExtendedForm";
  private static final String ExtendedExtendedFormFqn = "formdata.client.ui.forms.replace.ExtendedExtendedForm";

  @Test
  public void runTests() {
    checkBaseFormData();
    checkExtendedFormData();
    checkExtendedExtendedFormData();
  }

  private static void checkBaseFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(BaseFormFqn);
    testApiOfBaseFormData(dto);
  }

  private static void checkExtendedFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(ExtendedFormFqn);
    testApiOfExtendedFormData(dto);
  }

  private static void checkExtendedExtendedFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(ExtendedExtendedFormFqn);
    testApiOfExtendedExtendedFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfBaseFormData(IType baseFormData) {
    // type BaseFormData
    SdkAssert.assertHasFlags(baseFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(baseFormData, "QAbstractFormData;");

    // fields of BaseFormData
    Assert.assertEquals("field count of 'BaseFormData'", 1, baseFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(baseFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'BaseFormData'", 7, baseFormData.methods().list().size());
    IMethod baseFormData1 = SdkAssert.assertMethodExist(baseFormData, "BaseFormData", new String[]{});
    Assert.assertTrue(baseFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(baseFormData1, null);
    IMethod getLookup = SdkAssert.assertMethodExist(baseFormData, "getLookup", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getLookup, "QLookup;");
    IMethod getName = SdkAssert.assertMethodExist(baseFormData, "getName", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getName, "QName;");
    IMethod getSdkCommandCreate = SdkAssert.assertMethodExist(baseFormData, "getSdkCommandCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreate, "QSdkCommandCreate;");
    IMethod getSdkCommandNone = SdkAssert.assertMethodExist(baseFormData, "getSdkCommandNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNone, "QSdkCommandNone;");
    IMethod getSdkCommandUse = SdkAssert.assertMethodExist(baseFormData, "getSdkCommandUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUse, "QSdkCommandUse;");
    IMethod getSmart = SdkAssert.assertMethodExist(baseFormData, "getSmart", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSmart, "QSmart;");

    Assert.assertEquals("inner types count of 'BaseFormData'", 6, baseFormData.innerTypes().list().size());
    // type Lookup
    IType lookup = SdkAssert.assertTypeExists(baseFormData, "Lookup");
    SdkAssert.assertHasFlags(lookup, 9);
    SdkAssert.assertHasSuperTypeSignature(lookup, "QAbstractValueFieldData<QLong;>;");

    // fields of Lookup
    Assert.assertEquals("field count of 'Lookup'", 1, lookup.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(lookup, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'Lookup'", 1, lookup.methods().list().size());
    IMethod lookup1 = SdkAssert.assertMethodExist(lookup, "Lookup", new String[]{});
    Assert.assertTrue(lookup1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(lookup1, null);

    Assert.assertEquals("inner types count of 'Lookup'", 0, lookup.innerTypes().list().size());
    // type Name
    IType name = SdkAssert.assertTypeExists(baseFormData, "Name");
    SdkAssert.assertHasFlags(name, 9);
    SdkAssert.assertHasSuperTypeSignature(name, "QAbstractValueFieldData<QString;>;");

    // fields of Name
    Assert.assertEquals("field count of 'Name'", 1, name.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(name, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'Name'", 1, name.methods().list().size());
    IMethod name1 = SdkAssert.assertMethodExist(name, "Name", new String[]{});
    Assert.assertTrue(name1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(name1, null);

    Assert.assertEquals("inner types count of 'Name'", 0, name.innerTypes().list().size());
    // type SdkCommandCreate
    IType sdkCommandCreate = SdkAssert.assertTypeExists(baseFormData, "SdkCommandCreate");
    SdkAssert.assertHasFlags(sdkCommandCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreate, "QAbstractValueFieldData<QString;>;");

    // fields of SdkCommandCreate
    Assert.assertEquals("field count of 'SdkCommandCreate'", 1, sdkCommandCreate.fields().list().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(sdkCommandCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'SdkCommandCreate'", 1, sdkCommandCreate.methods().list().size());
    IMethod sdkCommandCreate1 = SdkAssert.assertMethodExist(sdkCommandCreate, "SdkCommandCreate", new String[]{});
    Assert.assertTrue(sdkCommandCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreate'", 0, sdkCommandCreate.innerTypes().list().size());
    // type SdkCommandNone
    IType sdkCommandNone = SdkAssert.assertTypeExists(baseFormData, "SdkCommandNone");
    SdkAssert.assertHasFlags(sdkCommandNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNone, "QAbstractValueFieldData<QString;>;");

    // fields of SdkCommandNone
    Assert.assertEquals("field count of 'SdkCommandNone'", 1, sdkCommandNone.fields().list().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(sdkCommandNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    Assert.assertEquals("method count of 'SdkCommandNone'", 1, sdkCommandNone.methods().list().size());
    IMethod sdkCommandNone1 = SdkAssert.assertMethodExist(sdkCommandNone, "SdkCommandNone", new String[]{});
    Assert.assertTrue(sdkCommandNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNone'", 0, sdkCommandNone.innerTypes().list().size());
    // type SdkCommandUse
    IType sdkCommandUse = SdkAssert.assertTypeExists(baseFormData, "SdkCommandUse");
    SdkAssert.assertHasFlags(sdkCommandUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUse, "QUsingFormFieldData;");

    // fields of SdkCommandUse
    Assert.assertEquals("field count of 'SdkCommandUse'", 1, sdkCommandUse.fields().list().size());
    IField serialVersionUID5 = SdkAssert.assertFieldExist(sdkCommandUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    Assert.assertEquals("method count of 'SdkCommandUse'", 1, sdkCommandUse.methods().list().size());
    IMethod sdkCommandUse1 = SdkAssert.assertMethodExist(sdkCommandUse, "SdkCommandUse", new String[]{});
    Assert.assertTrue(sdkCommandUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUse'", 0, sdkCommandUse.innerTypes().list().size());
    // type Smart
    IType smart = SdkAssert.assertTypeExists(baseFormData, "Smart");
    SdkAssert.assertHasFlags(smart, 9);
    SdkAssert.assertHasSuperTypeSignature(smart, "QAbstractValueFieldData<QLong;>;");

    // fields of Smart
    Assert.assertEquals("field count of 'Smart'", 1, smart.fields().list().size());
    IField serialVersionUID6 = SdkAssert.assertFieldExist(smart, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    Assert.assertEquals("method count of 'Smart'", 1, smart.methods().list().size());
    IMethod smart1 = SdkAssert.assertMethodExist(smart, "Smart", new String[]{});
    Assert.assertTrue(smart1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(smart1, null);

    Assert.assertEquals("inner types count of 'Smart'", 0, smart.innerTypes().list().size());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfExtendedFormData(IType extendedFormData) {
    // type ExtendedFormData
    SdkAssert.assertHasFlags(extendedFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(extendedFormData, "QBaseFormData;");

    // fields of ExtendedFormData
    Assert.assertEquals("field count of 'ExtendedFormData'", 1, extendedFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(extendedFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'ExtendedFormData'", 20, extendedFormData.methods().list().size());
    IMethod extendedFormData1 = SdkAssert.assertMethodExist(extendedFormData, "ExtendedFormData", new String[]{});
    Assert.assertTrue(extendedFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedFormData1, null);
    IMethod getFirstName = SdkAssert.assertMethodExist(extendedFormData, "getFirstName", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirstName, "QFirstName;");
    IMethod getIgnoringGroupBoxExCreate = SdkAssert.assertMethodExist(extendedFormData, "getIgnoringGroupBoxExCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIgnoringGroupBoxExCreate, "QIgnoringGroupBoxExCreate;");
    IMethod getIgnoringGroupBoxExUse = SdkAssert.assertMethodExist(extendedFormData, "getIgnoringGroupBoxExUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIgnoringGroupBoxExUse, "QIgnoringGroupBoxExUse;");
    IMethod getNameEx = SdkAssert.assertMethodExist(extendedFormData, "getNameEx", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getNameEx, "QNameEx;");
    IMethod getSdkCommandCreateCreate = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandCreateCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateCreate, "QSdkCommandCreateCreate;");
    IMethod getSdkCommandCreateIgnore = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandCreateIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateIgnore, "QSdkCommandCreateIgnore;");
    IMethod getSdkCommandCreateNone = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateNone, "QSdkCommandCreateNone;");
    IMethod getSdkCommandCreateUse = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandCreateUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateUse, "QSdkCommandCreateUse;");
    IMethod getSdkCommandIgnoreCreate = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandIgnoreCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreCreate, "QSdkCommandIgnoreCreate;");
    IMethod getSdkCommandIgnoreUse = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandIgnoreUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreUse, "QSdkCommandIgnoreUse;");
    IMethod getSdkCommandNoneCreate = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneCreate, "QSdkCommandNoneCreate;");
    IMethod getSdkCommandNoneIgnore = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandNoneIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneIgnore, "QSdkCommandNoneIgnore;");
    IMethod getSdkCommandNoneNone = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandNoneNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneNone, "QSdkCommandNoneNone;");
    IMethod getSdkCommandNoneUse = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandNoneUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneUse, "QSdkCommandNoneUse;");
    IMethod getSdkCommandUseCreate = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandUseCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseCreate, "QSdkCommandUseCreate;");
    IMethod getSdkCommandUseIgnore = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandUseIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseIgnore, "QSdkCommandUseIgnore;");
    IMethod getSdkCommandUseNone = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandUseNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseNone, "QSdkCommandUseNone;");
    IMethod getSdkCommandUseUse = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandUseUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseUse, "QSdkCommandUseUse;");
    IMethod getSmartEx = SdkAssert.assertMethodExist(extendedFormData, "getSmartEx", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSmartEx, "QSmartEx;");

    Assert.assertEquals("inner types count of 'ExtendedFormData'", 19, extendedFormData.innerTypes().list().size());
    // type FirstName
    IType firstName = SdkAssert.assertTypeExists(extendedFormData, "FirstName");
    SdkAssert.assertHasFlags(firstName, 9);
    SdkAssert.assertHasSuperTypeSignature(firstName, "QAbstractValueFieldData<QString;>;");

    // fields of FirstName
    Assert.assertEquals("field count of 'FirstName'", 1, firstName.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(firstName, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'FirstName'", 1, firstName.methods().list().size());
    IMethod firstName1 = SdkAssert.assertMethodExist(firstName, "FirstName", new String[]{});
    Assert.assertTrue(firstName1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(firstName1, null);

    Assert.assertEquals("inner types count of 'FirstName'", 0, firstName.innerTypes().list().size());
    // type IgnoringGroupBoxExCreate
    IType ignoringGroupBoxExCreate = SdkAssert.assertTypeExists(extendedFormData, "IgnoringGroupBoxExCreate");
    SdkAssert.assertHasFlags(ignoringGroupBoxExCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(ignoringGroupBoxExCreate, "QAbstractValueFieldData<QString;>;");
    SdkAssert.assertAnnotation(ignoringGroupBoxExCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of IgnoringGroupBoxExCreate
    Assert.assertEquals("field count of 'IgnoringGroupBoxExCreate'", 1, ignoringGroupBoxExCreate.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(ignoringGroupBoxExCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'IgnoringGroupBoxExCreate'", 1, ignoringGroupBoxExCreate.methods().list().size());
    IMethod ignoringGroupBoxExCreate1 = SdkAssert.assertMethodExist(ignoringGroupBoxExCreate, "IgnoringGroupBoxExCreate", new String[]{});
    Assert.assertTrue(ignoringGroupBoxExCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(ignoringGroupBoxExCreate1, null);

    Assert.assertEquals("inner types count of 'IgnoringGroupBoxExCreate'", 0, ignoringGroupBoxExCreate.innerTypes().list().size());
    // type IgnoringGroupBoxExUse
    IType ignoringGroupBoxExUse = SdkAssert.assertTypeExists(extendedFormData, "IgnoringGroupBoxExUse");
    SdkAssert.assertHasFlags(ignoringGroupBoxExUse, 9);
    SdkAssert.assertHasSuperTypeSignature(ignoringGroupBoxExUse, "QUsingFormFieldData;");
    SdkAssert.assertAnnotation(ignoringGroupBoxExUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of IgnoringGroupBoxExUse
    Assert.assertEquals("field count of 'IgnoringGroupBoxExUse'", 1, ignoringGroupBoxExUse.fields().list().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(ignoringGroupBoxExUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'IgnoringGroupBoxExUse'", 1, ignoringGroupBoxExUse.methods().list().size());
    IMethod ignoringGroupBoxExUse1 = SdkAssert.assertMethodExist(ignoringGroupBoxExUse, "IgnoringGroupBoxExUse", new String[]{});
    Assert.assertTrue(ignoringGroupBoxExUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(ignoringGroupBoxExUse1, null);

    Assert.assertEquals("inner types count of 'IgnoringGroupBoxExUse'", 0, ignoringGroupBoxExUse.innerTypes().list().size());
    // type NameEx
    IType nameEx = SdkAssert.assertTypeExists(extendedFormData, "NameEx");
    SdkAssert.assertHasFlags(nameEx, 9);
    SdkAssert.assertHasSuperTypeSignature(nameEx, "QName;");
    SdkAssert.assertAnnotation(nameEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of NameEx
    Assert.assertEquals("field count of 'NameEx'", 1, nameEx.fields().list().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(nameEx, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    Assert.assertEquals("method count of 'NameEx'", 1, nameEx.methods().list().size());
    IMethod nameEx1 = SdkAssert.assertMethodExist(nameEx, "NameEx", new String[]{});
    Assert.assertTrue(nameEx1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(nameEx1, null);

    Assert.assertEquals("inner types count of 'NameEx'", 0, nameEx.innerTypes().list().size());
    // type SdkCommandCreateCreate
    IType sdkCommandCreateCreate = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandCreateCreate");
    SdkAssert.assertHasFlags(sdkCommandCreateCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateCreate, "QSdkCommandCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateCreate
    Assert.assertEquals("field count of 'SdkCommandCreateCreate'", 1, sdkCommandCreateCreate.fields().list().size());
    IField serialVersionUID5 = SdkAssert.assertFieldExist(sdkCommandCreateCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateCreate'", 1, sdkCommandCreateCreate.methods().list().size());
    IMethod sdkCommandCreateCreate1 = SdkAssert.assertMethodExist(sdkCommandCreateCreate, "SdkCommandCreateCreate", new String[]{});
    Assert.assertTrue(sdkCommandCreateCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateCreate'", 0, sdkCommandCreateCreate.innerTypes().list().size());
    // type SdkCommandCreateIgnore
    IType sdkCommandCreateIgnore = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandCreateIgnore");
    SdkAssert.assertHasFlags(sdkCommandCreateIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateIgnore, "QSdkCommandCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateIgnore
    Assert.assertEquals("field count of 'SdkCommandCreateIgnore'", 1, sdkCommandCreateIgnore.fields().list().size());
    IField serialVersionUID6 = SdkAssert.assertFieldExist(sdkCommandCreateIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateIgnore'", 1, sdkCommandCreateIgnore.methods().list().size());
    IMethod sdkCommandCreateIgnore1 = SdkAssert.assertMethodExist(sdkCommandCreateIgnore, "SdkCommandCreateIgnore", new String[]{});
    Assert.assertTrue(sdkCommandCreateIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateIgnore'", 0, sdkCommandCreateIgnore.innerTypes().list().size());
    // type SdkCommandCreateNone
    IType sdkCommandCreateNone = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandCreateNone");
    SdkAssert.assertHasFlags(sdkCommandCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateNone, "QSdkCommandCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateNone
    Assert.assertEquals("field count of 'SdkCommandCreateNone'", 1, sdkCommandCreateNone.fields().list().size());
    IField serialVersionUID7 = SdkAssert.assertFieldExist(sdkCommandCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateNone'", 1, sdkCommandCreateNone.methods().list().size());
    IMethod sdkCommandCreateNone1 = SdkAssert.assertMethodExist(sdkCommandCreateNone, "SdkCommandCreateNone", new String[]{});
    Assert.assertTrue(sdkCommandCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateNone'", 0, sdkCommandCreateNone.innerTypes().list().size());
    // type SdkCommandCreateUse
    IType sdkCommandCreateUse = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandCreateUse");
    SdkAssert.assertHasFlags(sdkCommandCreateUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateUse, "QSdkCommandCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateUse
    Assert.assertEquals("field count of 'SdkCommandCreateUse'", 1, sdkCommandCreateUse.fields().list().size());
    IField serialVersionUID8 = SdkAssert.assertFieldExist(sdkCommandCreateUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateUse'", 1, sdkCommandCreateUse.methods().list().size());
    IMethod sdkCommandCreateUse1 = SdkAssert.assertMethodExist(sdkCommandCreateUse, "SdkCommandCreateUse", new String[]{});
    Assert.assertTrue(sdkCommandCreateUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateUse'", 0, sdkCommandCreateUse.innerTypes().list().size());
    // type SdkCommandIgnoreCreate
    IType sdkCommandIgnoreCreate = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandIgnoreCreate");
    SdkAssert.assertHasFlags(sdkCommandIgnoreCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreCreate, "QAbstractValueFieldData<QString;>;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreCreate
    Assert.assertEquals("field count of 'SdkCommandIgnoreCreate'", 1, sdkCommandIgnoreCreate.fields().list().size());
    IField serialVersionUID9 = SdkAssert.assertFieldExist(sdkCommandIgnoreCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID9, 26);
    SdkAssert.assertFieldSignature(serialVersionUID9, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreCreate'", 1, sdkCommandIgnoreCreate.methods().list().size());
    IMethod sdkCommandIgnoreCreate1 = SdkAssert.assertMethodExist(sdkCommandIgnoreCreate, "SdkCommandIgnoreCreate", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreCreate'", 0, sdkCommandIgnoreCreate.innerTypes().list().size());
    // type SdkCommandIgnoreUse
    IType sdkCommandIgnoreUse = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandIgnoreUse");
    SdkAssert.assertHasFlags(sdkCommandIgnoreUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreUse, "QUsingFormFieldData;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreUse
    Assert.assertEquals("field count of 'SdkCommandIgnoreUse'", 1, sdkCommandIgnoreUse.fields().list().size());
    IField serialVersionUID10 = SdkAssert.assertFieldExist(sdkCommandIgnoreUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID10, 26);
    SdkAssert.assertFieldSignature(serialVersionUID10, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreUse'", 1, sdkCommandIgnoreUse.methods().list().size());
    IMethod sdkCommandIgnoreUse1 = SdkAssert.assertMethodExist(sdkCommandIgnoreUse, "SdkCommandIgnoreUse", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreUse'", 0, sdkCommandIgnoreUse.innerTypes().list().size());
    // type SdkCommandNoneCreate
    IType sdkCommandNoneCreate = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandNoneCreate");
    SdkAssert.assertHasFlags(sdkCommandNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneCreate, "QSdkCommandNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneCreate
    Assert.assertEquals("field count of 'SdkCommandNoneCreate'", 1, sdkCommandNoneCreate.fields().list().size());
    IField serialVersionUID11 = SdkAssert.assertFieldExist(sdkCommandNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID11, 26);
    SdkAssert.assertFieldSignature(serialVersionUID11, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneCreate'", 1, sdkCommandNoneCreate.methods().list().size());
    IMethod sdkCommandNoneCreate1 = SdkAssert.assertMethodExist(sdkCommandNoneCreate, "SdkCommandNoneCreate", new String[]{});
    Assert.assertTrue(sdkCommandNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneCreate'", 0, sdkCommandNoneCreate.innerTypes().list().size());
    // type SdkCommandNoneIgnore
    IType sdkCommandNoneIgnore = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandNoneIgnore");
    SdkAssert.assertHasFlags(sdkCommandNoneIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneIgnore, "QSdkCommandNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneIgnore
    Assert.assertEquals("field count of 'SdkCommandNoneIgnore'", 1, sdkCommandNoneIgnore.fields().list().size());
    IField serialVersionUID12 = SdkAssert.assertFieldExist(sdkCommandNoneIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID12, 26);
    SdkAssert.assertFieldSignature(serialVersionUID12, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneIgnore'", 1, sdkCommandNoneIgnore.methods().list().size());
    IMethod sdkCommandNoneIgnore1 = SdkAssert.assertMethodExist(sdkCommandNoneIgnore, "SdkCommandNoneIgnore", new String[]{});
    Assert.assertTrue(sdkCommandNoneIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneIgnore'", 0, sdkCommandNoneIgnore.innerTypes().list().size());
    // type SdkCommandNoneNone
    IType sdkCommandNoneNone = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandNoneNone");
    SdkAssert.assertHasFlags(sdkCommandNoneNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneNone, "QSdkCommandNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneNone
    Assert.assertEquals("field count of 'SdkCommandNoneNone'", 1, sdkCommandNoneNone.fields().list().size());
    IField serialVersionUID13 = SdkAssert.assertFieldExist(sdkCommandNoneNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID13, 26);
    SdkAssert.assertFieldSignature(serialVersionUID13, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneNone'", 1, sdkCommandNoneNone.methods().list().size());
    IMethod sdkCommandNoneNone1 = SdkAssert.assertMethodExist(sdkCommandNoneNone, "SdkCommandNoneNone", new String[]{});
    Assert.assertTrue(sdkCommandNoneNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneNone'", 0, sdkCommandNoneNone.innerTypes().list().size());
    // type SdkCommandNoneUse
    IType sdkCommandNoneUse = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandNoneUse");
    SdkAssert.assertHasFlags(sdkCommandNoneUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneUse, "QSdkCommandNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneUse
    Assert.assertEquals("field count of 'SdkCommandNoneUse'", 1, sdkCommandNoneUse.fields().list().size());
    IField serialVersionUID14 = SdkAssert.assertFieldExist(sdkCommandNoneUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID14, 26);
    SdkAssert.assertFieldSignature(serialVersionUID14, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneUse'", 1, sdkCommandNoneUse.methods().list().size());
    IMethod sdkCommandNoneUse1 = SdkAssert.assertMethodExist(sdkCommandNoneUse, "SdkCommandNoneUse", new String[]{});
    Assert.assertTrue(sdkCommandNoneUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneUse'", 0, sdkCommandNoneUse.innerTypes().list().size());
    // type SdkCommandUseCreate
    IType sdkCommandUseCreate = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandUseCreate");
    SdkAssert.assertHasFlags(sdkCommandUseCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseCreate, "QSdkCommandUse;");
    SdkAssert.assertAnnotation(sdkCommandUseCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseCreate
    Assert.assertEquals("field count of 'SdkCommandUseCreate'", 1, sdkCommandUseCreate.fields().list().size());
    IField serialVersionUID15 = SdkAssert.assertFieldExist(sdkCommandUseCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID15, 26);
    SdkAssert.assertFieldSignature(serialVersionUID15, "J");

    Assert.assertEquals("method count of 'SdkCommandUseCreate'", 1, sdkCommandUseCreate.methods().list().size());
    IMethod sdkCommandUseCreate1 = SdkAssert.assertMethodExist(sdkCommandUseCreate, "SdkCommandUseCreate", new String[]{});
    Assert.assertTrue(sdkCommandUseCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseCreate'", 0, sdkCommandUseCreate.innerTypes().list().size());
    // type SdkCommandUseIgnore
    IType sdkCommandUseIgnore = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandUseIgnore");
    SdkAssert.assertHasFlags(sdkCommandUseIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseIgnore, "QSdkCommandUse;");
    SdkAssert.assertAnnotation(sdkCommandUseIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseIgnore
    Assert.assertEquals("field count of 'SdkCommandUseIgnore'", 1, sdkCommandUseIgnore.fields().list().size());
    IField serialVersionUID16 = SdkAssert.assertFieldExist(sdkCommandUseIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID16, 26);
    SdkAssert.assertFieldSignature(serialVersionUID16, "J");

    Assert.assertEquals("method count of 'SdkCommandUseIgnore'", 1, sdkCommandUseIgnore.methods().list().size());
    IMethod sdkCommandUseIgnore1 = SdkAssert.assertMethodExist(sdkCommandUseIgnore, "SdkCommandUseIgnore", new String[]{});
    Assert.assertTrue(sdkCommandUseIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseIgnore'", 0, sdkCommandUseIgnore.innerTypes().list().size());
    // type SdkCommandUseNone
    IType sdkCommandUseNone = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandUseNone");
    SdkAssert.assertHasFlags(sdkCommandUseNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseNone, "QSdkCommandUse;");
    SdkAssert.assertAnnotation(sdkCommandUseNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseNone
    Assert.assertEquals("field count of 'SdkCommandUseNone'", 1, sdkCommandUseNone.fields().list().size());
    IField serialVersionUID17 = SdkAssert.assertFieldExist(sdkCommandUseNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID17, 26);
    SdkAssert.assertFieldSignature(serialVersionUID17, "J");

    Assert.assertEquals("method count of 'SdkCommandUseNone'", 1, sdkCommandUseNone.methods().list().size());
    IMethod sdkCommandUseNone1 = SdkAssert.assertMethodExist(sdkCommandUseNone, "SdkCommandUseNone", new String[]{});
    Assert.assertTrue(sdkCommandUseNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseNone'", 0, sdkCommandUseNone.innerTypes().list().size());
    // type SdkCommandUseUse
    IType sdkCommandUseUse = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandUseUse");
    SdkAssert.assertHasFlags(sdkCommandUseUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseUse, "QSdkCommandUse;");
    SdkAssert.assertAnnotation(sdkCommandUseUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseUse
    Assert.assertEquals("field count of 'SdkCommandUseUse'", 1, sdkCommandUseUse.fields().list().size());
    IField serialVersionUID18 = SdkAssert.assertFieldExist(sdkCommandUseUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID18, 26);
    SdkAssert.assertFieldSignature(serialVersionUID18, "J");

    Assert.assertEquals("method count of 'SdkCommandUseUse'", 1, sdkCommandUseUse.methods().list().size());
    IMethod sdkCommandUseUse1 = SdkAssert.assertMethodExist(sdkCommandUseUse, "SdkCommandUseUse", new String[]{});
    Assert.assertTrue(sdkCommandUseUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseUse'", 0, sdkCommandUseUse.innerTypes().list().size());
    // type SmartEx
    IType smartEx = SdkAssert.assertTypeExists(extendedFormData, "SmartEx");
    SdkAssert.assertHasFlags(smartEx, 9);
    SdkAssert.assertHasSuperTypeSignature(smartEx, "QSmart;");
    SdkAssert.assertAnnotation(smartEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of SmartEx
    Assert.assertEquals("field count of 'SmartEx'", 1, smartEx.fields().list().size());
    IField serialVersionUID19 = SdkAssert.assertFieldExist(smartEx, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID19, 26);
    SdkAssert.assertFieldSignature(serialVersionUID19, "J");

    Assert.assertEquals("method count of 'SmartEx'", 1, smartEx.methods().list().size());
    IMethod smartEx1 = SdkAssert.assertMethodExist(smartEx, "SmartEx", new String[]{});
    Assert.assertTrue(smartEx1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(smartEx1, null);

    Assert.assertEquals("inner types count of 'SmartEx'", 0, smartEx.innerTypes().list().size());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfExtendedExtendedFormData(IType extendedExtendedFormData) {
    // type ExtendedExtendedFormData
    SdkAssert.assertHasFlags(extendedExtendedFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(extendedExtendedFormData, "QExtendedFormData;");
    SdkAssert.assertAnnotation(extendedExtendedFormData, "javax.annotation.Generated");

    // fields of ExtendedExtendedFormData
    Assert.assertEquals("field count of 'ExtendedExtendedFormData'", 1, extendedExtendedFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(extendedExtendedFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'ExtendedExtendedFormData'", 64, extendedExtendedFormData.methods().list().size());
    IMethod extendedExtendedFormData1 = SdkAssert.assertMethodExist(extendedExtendedFormData, "ExtendedExtendedFormData", new String[]{});
    Assert.assertTrue(extendedExtendedFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedExtendedFormData1, null);
    IMethod getIgnoringGroupBoxExCreateNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getIgnoringGroupBoxExCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIgnoringGroupBoxExCreateNone, "QIgnoringGroupBoxExCreateNone;");
    IMethod getIgnoringGroupBoxExNoneCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getIgnoringGroupBoxExNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIgnoringGroupBoxExNoneCreate, "QIgnoringGroupBoxExNoneCreate;");
    IMethod getNameExEx = SdkAssert.assertMethodExist(extendedExtendedFormData, "getNameExEx", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getNameExEx, "QNameExEx;");
    IMethod getSdkCommandCreateCreateCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateCreateCreate, "QSdkCommandCreateCreateCreate;");
    IMethod getSdkCommandCreateCreateIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateCreateIgnore, "QSdkCommandCreateCreateIgnore;");
    IMethod getSdkCommandCreateCreateNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateCreateNone, "QSdkCommandCreateCreateNone;");
    IMethod getSdkCommandCreateCreateUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateCreateUse, "QSdkCommandCreateCreateUse;");
    IMethod getSdkCommandCreateIgnoreCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateIgnoreCreate, "QSdkCommandCreateIgnoreCreate;");
    IMethod getSdkCommandCreateIgnoreIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateIgnoreIgnore, "QSdkCommandCreateIgnoreIgnore;");
    IMethod getSdkCommandCreateIgnoreNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateIgnoreNone, "QSdkCommandCreateIgnoreNone;");
    IMethod getSdkCommandCreateIgnoreUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateIgnoreUse, "QSdkCommandCreateIgnoreUse;");
    IMethod getSdkCommandCreateNoneCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateNoneCreate, "QSdkCommandCreateNoneCreate;");
    IMethod getSdkCommandCreateNoneIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateNoneIgnore, "QSdkCommandCreateNoneIgnore;");
    IMethod getSdkCommandCreateNoneNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateNoneNone, "QSdkCommandCreateNoneNone;");
    IMethod getSdkCommandCreateNoneUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateNoneUse, "QSdkCommandCreateNoneUse;");
    IMethod getSdkCommandCreateUseCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateUseCreate, "QSdkCommandCreateUseCreate;");
    IMethod getSdkCommandCreateUseIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateUseIgnore, "QSdkCommandCreateUseIgnore;");
    IMethod getSdkCommandCreateUseNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateUseNone, "QSdkCommandCreateUseNone;");
    IMethod getSdkCommandCreateUseUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateUseUse, "QSdkCommandCreateUseUse;");
    IMethod getSdkCommandIgnoreCreateCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreCreateCreate, "QSdkCommandIgnoreCreateCreate;");
    IMethod getSdkCommandIgnoreCreateIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreCreateIgnore, "QSdkCommandIgnoreCreateIgnore;");
    IMethod getSdkCommandIgnoreCreateNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreCreateNone, "QSdkCommandIgnoreCreateNone;");
    IMethod getSdkCommandIgnoreCreateUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreCreateUse, "QSdkCommandIgnoreCreateUse;");
    IMethod getSdkCommandIgnoreIgnoreCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreIgnoreCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreIgnoreCreate, "QSdkCommandIgnoreIgnoreCreate;");
    IMethod getSdkCommandIgnoreIgnoreUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreIgnoreUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreIgnoreUse, "QSdkCommandIgnoreIgnoreUse;");
    IMethod getSdkCommandIgnoreNoneCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreNoneCreate, "QSdkCommandIgnoreNoneCreate;");
    IMethod getSdkCommandIgnoreNoneUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreNoneUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreNoneUse, "QSdkCommandIgnoreNoneUse;");
    IMethod getSdkCommandIgnoreUseCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreUseCreate, "QSdkCommandIgnoreUseCreate;");
    IMethod getSdkCommandIgnoreUseIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreUseIgnore, "QSdkCommandIgnoreUseIgnore;");
    IMethod getSdkCommandIgnoreUseNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreUseNone, "QSdkCommandIgnoreUseNone;");
    IMethod getSdkCommandIgnoreUseUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreUseUse, "QSdkCommandIgnoreUseUse;");
    IMethod getSdkCommandNoneCreateCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneCreateCreate, "QSdkCommandNoneCreateCreate;");
    IMethod getSdkCommandNoneCreateIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneCreateIgnore, "QSdkCommandNoneCreateIgnore;");
    IMethod getSdkCommandNoneCreateNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneCreateNone, "QSdkCommandNoneCreateNone;");
    IMethod getSdkCommandNoneCreateUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneCreateUse, "QSdkCommandNoneCreateUse;");
    IMethod getSdkCommandNoneIgnoreCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneIgnoreCreate, "QSdkCommandNoneIgnoreCreate;");
    IMethod getSdkCommandNoneIgnoreIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneIgnoreIgnore, "QSdkCommandNoneIgnoreIgnore;");
    IMethod getSdkCommandNoneIgnoreNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneIgnoreNone, "QSdkCommandNoneIgnoreNone;");
    IMethod getSdkCommandNoneIgnoreUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneIgnoreUse, "QSdkCommandNoneIgnoreUse;");
    IMethod getSdkCommandNoneNoneCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneNoneCreate, "QSdkCommandNoneNoneCreate;");
    IMethod getSdkCommandNoneNoneIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneNoneIgnore, "QSdkCommandNoneNoneIgnore;");
    IMethod getSdkCommandNoneNoneNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneNoneNone, "QSdkCommandNoneNoneNone;");
    IMethod getSdkCommandNoneNoneUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneNoneUse, "QSdkCommandNoneNoneUse;");
    IMethod getSdkCommandNoneUseCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneUseCreate, "QSdkCommandNoneUseCreate;");
    IMethod getSdkCommandNoneUseIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneUseIgnore, "QSdkCommandNoneUseIgnore;");
    IMethod getSdkCommandNoneUseNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneUseNone, "QSdkCommandNoneUseNone;");
    IMethod getSdkCommandNoneUseUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneUseUse, "QSdkCommandNoneUseUse;");
    IMethod getSdkCommandUseCreateCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseCreateCreate, "QSdkCommandUseCreateCreate;");
    IMethod getSdkCommandUseCreateIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseCreateIgnore, "QSdkCommandUseCreateIgnore;");
    IMethod getSdkCommandUseCreateNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseCreateNone, "QSdkCommandUseCreateNone;");
    IMethod getSdkCommandUseCreateUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseCreateUse, "QSdkCommandUseCreateUse;");
    IMethod getSdkCommandUseIgnoreCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseIgnoreCreate, "QSdkCommandUseIgnoreCreate;");
    IMethod getSdkCommandUseIgnoreIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseIgnoreIgnore, "QSdkCommandUseIgnoreIgnore;");
    IMethod getSdkCommandUseIgnoreNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseIgnoreNone, "QSdkCommandUseIgnoreNone;");
    IMethod getSdkCommandUseIgnoreUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseIgnoreUse, "QSdkCommandUseIgnoreUse;");
    IMethod getSdkCommandUseNoneCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseNoneCreate, "QSdkCommandUseNoneCreate;");
    IMethod getSdkCommandUseNoneIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseNoneIgnore, "QSdkCommandUseNoneIgnore;");
    IMethod getSdkCommandUseNoneNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseNoneNone, "QSdkCommandUseNoneNone;");
    IMethod getSdkCommandUseNoneUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseNoneUse, "QSdkCommandUseNoneUse;");
    IMethod getSdkCommandUseUseCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseUseCreate, "QSdkCommandUseUseCreate;");
    IMethod getSdkCommandUseUseIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseUseIgnore, "QSdkCommandUseUseIgnore;");
    IMethod getSdkCommandUseUseNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseUseNone, "QSdkCommandUseUseNone;");
    IMethod getSdkCommandUseUseUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseUseUse, "QSdkCommandUseUseUse;");

    Assert.assertEquals("inner types count of 'ExtendedExtendedFormData'", 63, extendedExtendedFormData.innerTypes().list().size());
    // type IgnoringGroupBoxExCreateNone
    IType ignoringGroupBoxExCreateNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "IgnoringGroupBoxExCreateNone");
    SdkAssert.assertHasFlags(ignoringGroupBoxExCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(ignoringGroupBoxExCreateNone, "QIgnoringGroupBoxExCreate;");
    SdkAssert.assertAnnotation(ignoringGroupBoxExCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of IgnoringGroupBoxExCreateNone
    Assert.assertEquals("field count of 'IgnoringGroupBoxExCreateNone'", 1, ignoringGroupBoxExCreateNone.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(ignoringGroupBoxExCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'IgnoringGroupBoxExCreateNone'", 1, ignoringGroupBoxExCreateNone.methods().list().size());
    IMethod ignoringGroupBoxExCreateNone1 = SdkAssert.assertMethodExist(ignoringGroupBoxExCreateNone, "IgnoringGroupBoxExCreateNone", new String[]{});
    Assert.assertTrue(ignoringGroupBoxExCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(ignoringGroupBoxExCreateNone1, null);

    Assert.assertEquals("inner types count of 'IgnoringGroupBoxExCreateNone'", 0, ignoringGroupBoxExCreateNone.innerTypes().list().size());
    // type IgnoringGroupBoxExNoneCreate
    IType ignoringGroupBoxExNoneCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "IgnoringGroupBoxExNoneCreate");
    SdkAssert.assertHasFlags(ignoringGroupBoxExNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(ignoringGroupBoxExNoneCreate, "QAbstractValueFieldData<QString;>;");
    SdkAssert.assertAnnotation(ignoringGroupBoxExNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of IgnoringGroupBoxExNoneCreate
    Assert.assertEquals("field count of 'IgnoringGroupBoxExNoneCreate'", 1, ignoringGroupBoxExNoneCreate.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(ignoringGroupBoxExNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'IgnoringGroupBoxExNoneCreate'", 1, ignoringGroupBoxExNoneCreate.methods().list().size());
    IMethod ignoringGroupBoxExNoneCreate1 = SdkAssert.assertMethodExist(ignoringGroupBoxExNoneCreate, "IgnoringGroupBoxExNoneCreate", new String[]{});
    Assert.assertTrue(ignoringGroupBoxExNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(ignoringGroupBoxExNoneCreate1, null);

    Assert.assertEquals("inner types count of 'IgnoringGroupBoxExNoneCreate'", 0, ignoringGroupBoxExNoneCreate.innerTypes().list().size());
    // type NameExEx
    IType nameExEx = SdkAssert.assertTypeExists(extendedExtendedFormData, "NameExEx");
    SdkAssert.assertHasFlags(nameExEx, 9);
    SdkAssert.assertHasSuperTypeSignature(nameExEx, "QNameEx;");
    SdkAssert.assertAnnotation(nameExEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of NameExEx
    Assert.assertEquals("field count of 'NameExEx'", 1, nameExEx.fields().list().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(nameExEx, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'NameExEx'", 4, nameExEx.methods().list().size());
    IMethod nameExEx1 = SdkAssert.assertMethodExist(nameExEx, "NameExEx", new String[]{});
    Assert.assertTrue(nameExEx1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(nameExEx1, null);
    IMethod getStringProperty = SdkAssert.assertMethodExist(nameExEx, "getStringProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getStringProperty, "QString;");
    IMethod setStringProperty = SdkAssert.assertMethodExist(nameExEx, "setStringProperty", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setStringProperty, "V");
    IMethod getStringPropertyProperty = SdkAssert.assertMethodExist(nameExEx, "getStringPropertyProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getStringPropertyProperty, "QStringPropertyProperty;");

    Assert.assertEquals("inner types count of 'NameExEx'", 1, nameExEx.innerTypes().list().size());
    // type StringPropertyProperty
    IType stringPropertyProperty = SdkAssert.assertTypeExists(nameExEx, "StringPropertyProperty");
    SdkAssert.assertHasFlags(stringPropertyProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(stringPropertyProperty, "QAbstractPropertyData<QString;>;");

    // fields of StringPropertyProperty
    Assert.assertEquals("field count of 'StringPropertyProperty'", 1, stringPropertyProperty.fields().list().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(stringPropertyProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    Assert.assertEquals("method count of 'StringPropertyProperty'", 1, stringPropertyProperty.methods().list().size());
    IMethod stringPropertyProperty1 = SdkAssert.assertMethodExist(stringPropertyProperty, "StringPropertyProperty", new String[]{});
    Assert.assertTrue(stringPropertyProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(stringPropertyProperty1, null);

    Assert.assertEquals("inner types count of 'StringPropertyProperty'", 0, stringPropertyProperty.innerTypes().list().size());
    // type SdkCommandCreateCreateCreate
    IType sdkCommandCreateCreateCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateCreate");
    SdkAssert.assertHasFlags(sdkCommandCreateCreateCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateCreateCreate, "QSdkCommandCreateCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateCreateCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateCreateCreate
    Assert.assertEquals("field count of 'SdkCommandCreateCreateCreate'", 1, sdkCommandCreateCreateCreate.fields().list().size());
    IField serialVersionUID5 = SdkAssert.assertFieldExist(sdkCommandCreateCreateCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateCreateCreate'", 1, sdkCommandCreateCreateCreate.methods().list().size());
    IMethod sdkCommandCreateCreateCreate1 = SdkAssert.assertMethodExist(sdkCommandCreateCreateCreate, "SdkCommandCreateCreateCreate", new String[]{});
    Assert.assertTrue(sdkCommandCreateCreateCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateCreateCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateCreateCreate'", 0, sdkCommandCreateCreateCreate.innerTypes().list().size());
    // type SdkCommandCreateCreateIgnore
    IType sdkCommandCreateCreateIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateIgnore");
    SdkAssert.assertHasFlags(sdkCommandCreateCreateIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateCreateIgnore, "QSdkCommandCreateCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateCreateIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateCreateIgnore
    Assert.assertEquals("field count of 'SdkCommandCreateCreateIgnore'", 1, sdkCommandCreateCreateIgnore.fields().list().size());
    IField serialVersionUID6 = SdkAssert.assertFieldExist(sdkCommandCreateCreateIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateCreateIgnore'", 1, sdkCommandCreateCreateIgnore.methods().list().size());
    IMethod sdkCommandCreateCreateIgnore1 = SdkAssert.assertMethodExist(sdkCommandCreateCreateIgnore, "SdkCommandCreateCreateIgnore", new String[]{});
    Assert.assertTrue(sdkCommandCreateCreateIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateCreateIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateCreateIgnore'", 0, sdkCommandCreateCreateIgnore.innerTypes().list().size());
    // type SdkCommandCreateCreateNone
    IType sdkCommandCreateCreateNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateNone");
    SdkAssert.assertHasFlags(sdkCommandCreateCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateCreateNone, "QSdkCommandCreateCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateCreateNone
    Assert.assertEquals("field count of 'SdkCommandCreateCreateNone'", 1, sdkCommandCreateCreateNone.fields().list().size());
    IField serialVersionUID7 = SdkAssert.assertFieldExist(sdkCommandCreateCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateCreateNone'", 1, sdkCommandCreateCreateNone.methods().list().size());
    IMethod sdkCommandCreateCreateNone1 = SdkAssert.assertMethodExist(sdkCommandCreateCreateNone, "SdkCommandCreateCreateNone", new String[]{});
    Assert.assertTrue(sdkCommandCreateCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateCreateNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateCreateNone'", 0, sdkCommandCreateCreateNone.innerTypes().list().size());
    // type SdkCommandCreateCreateUse
    IType sdkCommandCreateCreateUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateUse");
    SdkAssert.assertHasFlags(sdkCommandCreateCreateUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateCreateUse, "QSdkCommandCreateCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateCreateUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateCreateUse
    Assert.assertEquals("field count of 'SdkCommandCreateCreateUse'", 1, sdkCommandCreateCreateUse.fields().list().size());
    IField serialVersionUID8 = SdkAssert.assertFieldExist(sdkCommandCreateCreateUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateCreateUse'", 1, sdkCommandCreateCreateUse.methods().list().size());
    IMethod sdkCommandCreateCreateUse1 = SdkAssert.assertMethodExist(sdkCommandCreateCreateUse, "SdkCommandCreateCreateUse", new String[]{});
    Assert.assertTrue(sdkCommandCreateCreateUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateCreateUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateCreateUse'", 0, sdkCommandCreateCreateUse.innerTypes().list().size());
    // type SdkCommandCreateIgnoreCreate
    IType sdkCommandCreateIgnoreCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreCreate");
    SdkAssert.assertHasFlags(sdkCommandCreateIgnoreCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateIgnoreCreate, "QSdkCommandCreateIgnore;");
    SdkAssert.assertAnnotation(sdkCommandCreateIgnoreCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateIgnoreCreate
    Assert.assertEquals("field count of 'SdkCommandCreateIgnoreCreate'", 1, sdkCommandCreateIgnoreCreate.fields().list().size());
    IField serialVersionUID9 = SdkAssert.assertFieldExist(sdkCommandCreateIgnoreCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID9, 26);
    SdkAssert.assertFieldSignature(serialVersionUID9, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateIgnoreCreate'", 1, sdkCommandCreateIgnoreCreate.methods().list().size());
    IMethod sdkCommandCreateIgnoreCreate1 = SdkAssert.assertMethodExist(sdkCommandCreateIgnoreCreate, "SdkCommandCreateIgnoreCreate", new String[]{});
    Assert.assertTrue(sdkCommandCreateIgnoreCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateIgnoreCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateIgnoreCreate'", 0, sdkCommandCreateIgnoreCreate.innerTypes().list().size());
    // type SdkCommandCreateIgnoreIgnore
    IType sdkCommandCreateIgnoreIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreIgnore");
    SdkAssert.assertHasFlags(sdkCommandCreateIgnoreIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateIgnoreIgnore, "QSdkCommandCreateIgnore;");
    SdkAssert.assertAnnotation(sdkCommandCreateIgnoreIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateIgnoreIgnore
    Assert.assertEquals("field count of 'SdkCommandCreateIgnoreIgnore'", 1, sdkCommandCreateIgnoreIgnore.fields().list().size());
    IField serialVersionUID10 = SdkAssert.assertFieldExist(sdkCommandCreateIgnoreIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID10, 26);
    SdkAssert.assertFieldSignature(serialVersionUID10, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateIgnoreIgnore'", 1, sdkCommandCreateIgnoreIgnore.methods().list().size());
    IMethod sdkCommandCreateIgnoreIgnore1 = SdkAssert.assertMethodExist(sdkCommandCreateIgnoreIgnore, "SdkCommandCreateIgnoreIgnore", new String[]{});
    Assert.assertTrue(sdkCommandCreateIgnoreIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateIgnoreIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateIgnoreIgnore'", 0, sdkCommandCreateIgnoreIgnore.innerTypes().list().size());
    // type SdkCommandCreateIgnoreNone
    IType sdkCommandCreateIgnoreNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreNone");
    SdkAssert.assertHasFlags(sdkCommandCreateIgnoreNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateIgnoreNone, "QSdkCommandCreateIgnore;");
    SdkAssert.assertAnnotation(sdkCommandCreateIgnoreNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateIgnoreNone
    Assert.assertEquals("field count of 'SdkCommandCreateIgnoreNone'", 1, sdkCommandCreateIgnoreNone.fields().list().size());
    IField serialVersionUID11 = SdkAssert.assertFieldExist(sdkCommandCreateIgnoreNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID11, 26);
    SdkAssert.assertFieldSignature(serialVersionUID11, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateIgnoreNone'", 1, sdkCommandCreateIgnoreNone.methods().list().size());
    IMethod sdkCommandCreateIgnoreNone1 = SdkAssert.assertMethodExist(sdkCommandCreateIgnoreNone, "SdkCommandCreateIgnoreNone", new String[]{});
    Assert.assertTrue(sdkCommandCreateIgnoreNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateIgnoreNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateIgnoreNone'", 0, sdkCommandCreateIgnoreNone.innerTypes().list().size());
    // type SdkCommandCreateIgnoreUse
    IType sdkCommandCreateIgnoreUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreUse");
    SdkAssert.assertHasFlags(sdkCommandCreateIgnoreUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateIgnoreUse, "QSdkCommandCreateIgnore;");
    SdkAssert.assertAnnotation(sdkCommandCreateIgnoreUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateIgnoreUse
    Assert.assertEquals("field count of 'SdkCommandCreateIgnoreUse'", 1, sdkCommandCreateIgnoreUse.fields().list().size());
    IField serialVersionUID12 = SdkAssert.assertFieldExist(sdkCommandCreateIgnoreUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID12, 26);
    SdkAssert.assertFieldSignature(serialVersionUID12, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateIgnoreUse'", 1, sdkCommandCreateIgnoreUse.methods().list().size());
    IMethod sdkCommandCreateIgnoreUse1 = SdkAssert.assertMethodExist(sdkCommandCreateIgnoreUse, "SdkCommandCreateIgnoreUse", new String[]{});
    Assert.assertTrue(sdkCommandCreateIgnoreUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateIgnoreUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateIgnoreUse'", 0, sdkCommandCreateIgnoreUse.innerTypes().list().size());
    // type SdkCommandCreateNoneCreate
    IType sdkCommandCreateNoneCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneCreate");
    SdkAssert.assertHasFlags(sdkCommandCreateNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateNoneCreate, "QSdkCommandCreateNone;");
    SdkAssert.assertAnnotation(sdkCommandCreateNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateNoneCreate
    Assert.assertEquals("field count of 'SdkCommandCreateNoneCreate'", 1, sdkCommandCreateNoneCreate.fields().list().size());
    IField serialVersionUID13 = SdkAssert.assertFieldExist(sdkCommandCreateNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID13, 26);
    SdkAssert.assertFieldSignature(serialVersionUID13, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateNoneCreate'", 1, sdkCommandCreateNoneCreate.methods().list().size());
    IMethod sdkCommandCreateNoneCreate1 = SdkAssert.assertMethodExist(sdkCommandCreateNoneCreate, "SdkCommandCreateNoneCreate", new String[]{});
    Assert.assertTrue(sdkCommandCreateNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateNoneCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateNoneCreate'", 0, sdkCommandCreateNoneCreate.innerTypes().list().size());
    // type SdkCommandCreateNoneIgnore
    IType sdkCommandCreateNoneIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneIgnore");
    SdkAssert.assertHasFlags(sdkCommandCreateNoneIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateNoneIgnore, "QSdkCommandCreateNone;");
    SdkAssert.assertAnnotation(sdkCommandCreateNoneIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateNoneIgnore
    Assert.assertEquals("field count of 'SdkCommandCreateNoneIgnore'", 1, sdkCommandCreateNoneIgnore.fields().list().size());
    IField serialVersionUID14 = SdkAssert.assertFieldExist(sdkCommandCreateNoneIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID14, 26);
    SdkAssert.assertFieldSignature(serialVersionUID14, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateNoneIgnore'", 1, sdkCommandCreateNoneIgnore.methods().list().size());
    IMethod sdkCommandCreateNoneIgnore1 = SdkAssert.assertMethodExist(sdkCommandCreateNoneIgnore, "SdkCommandCreateNoneIgnore", new String[]{});
    Assert.assertTrue(sdkCommandCreateNoneIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateNoneIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateNoneIgnore'", 0, sdkCommandCreateNoneIgnore.innerTypes().list().size());
    // type SdkCommandCreateNoneNone
    IType sdkCommandCreateNoneNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneNone");
    SdkAssert.assertHasFlags(sdkCommandCreateNoneNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateNoneNone, "QSdkCommandCreateNone;");
    SdkAssert.assertAnnotation(sdkCommandCreateNoneNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateNoneNone
    Assert.assertEquals("field count of 'SdkCommandCreateNoneNone'", 1, sdkCommandCreateNoneNone.fields().list().size());
    IField serialVersionUID15 = SdkAssert.assertFieldExist(sdkCommandCreateNoneNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID15, 26);
    SdkAssert.assertFieldSignature(serialVersionUID15, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateNoneNone'", 1, sdkCommandCreateNoneNone.methods().list().size());
    IMethod sdkCommandCreateNoneNone1 = SdkAssert.assertMethodExist(sdkCommandCreateNoneNone, "SdkCommandCreateNoneNone", new String[]{});
    Assert.assertTrue(sdkCommandCreateNoneNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateNoneNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateNoneNone'", 0, sdkCommandCreateNoneNone.innerTypes().list().size());
    // type SdkCommandCreateNoneUse
    IType sdkCommandCreateNoneUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneUse");
    SdkAssert.assertHasFlags(sdkCommandCreateNoneUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateNoneUse, "QSdkCommandCreateNone;");
    SdkAssert.assertAnnotation(sdkCommandCreateNoneUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateNoneUse
    Assert.assertEquals("field count of 'SdkCommandCreateNoneUse'", 1, sdkCommandCreateNoneUse.fields().list().size());
    IField serialVersionUID16 = SdkAssert.assertFieldExist(sdkCommandCreateNoneUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID16, 26);
    SdkAssert.assertFieldSignature(serialVersionUID16, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateNoneUse'", 1, sdkCommandCreateNoneUse.methods().list().size());
    IMethod sdkCommandCreateNoneUse1 = SdkAssert.assertMethodExist(sdkCommandCreateNoneUse, "SdkCommandCreateNoneUse", new String[]{});
    Assert.assertTrue(sdkCommandCreateNoneUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateNoneUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateNoneUse'", 0, sdkCommandCreateNoneUse.innerTypes().list().size());
    // type SdkCommandCreateUseCreate
    IType sdkCommandCreateUseCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseCreate");
    SdkAssert.assertHasFlags(sdkCommandCreateUseCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateUseCreate, "QSdkCommandCreateUse;");
    SdkAssert.assertAnnotation(sdkCommandCreateUseCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateUseCreate
    Assert.assertEquals("field count of 'SdkCommandCreateUseCreate'", 1, sdkCommandCreateUseCreate.fields().list().size());
    IField serialVersionUID17 = SdkAssert.assertFieldExist(sdkCommandCreateUseCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID17, 26);
    SdkAssert.assertFieldSignature(serialVersionUID17, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateUseCreate'", 1, sdkCommandCreateUseCreate.methods().list().size());
    IMethod sdkCommandCreateUseCreate1 = SdkAssert.assertMethodExist(sdkCommandCreateUseCreate, "SdkCommandCreateUseCreate", new String[]{});
    Assert.assertTrue(sdkCommandCreateUseCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateUseCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateUseCreate'", 0, sdkCommandCreateUseCreate.innerTypes().list().size());
    // type SdkCommandCreateUseIgnore
    IType sdkCommandCreateUseIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseIgnore");
    SdkAssert.assertHasFlags(sdkCommandCreateUseIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateUseIgnore, "QSdkCommandCreateUse;");
    SdkAssert.assertAnnotation(sdkCommandCreateUseIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateUseIgnore
    Assert.assertEquals("field count of 'SdkCommandCreateUseIgnore'", 1, sdkCommandCreateUseIgnore.fields().list().size());
    IField serialVersionUID18 = SdkAssert.assertFieldExist(sdkCommandCreateUseIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID18, 26);
    SdkAssert.assertFieldSignature(serialVersionUID18, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateUseIgnore'", 1, sdkCommandCreateUseIgnore.methods().list().size());
    IMethod sdkCommandCreateUseIgnore1 = SdkAssert.assertMethodExist(sdkCommandCreateUseIgnore, "SdkCommandCreateUseIgnore", new String[]{});
    Assert.assertTrue(sdkCommandCreateUseIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateUseIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateUseIgnore'", 0, sdkCommandCreateUseIgnore.innerTypes().list().size());
    // type SdkCommandCreateUseNone
    IType sdkCommandCreateUseNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseNone");
    SdkAssert.assertHasFlags(sdkCommandCreateUseNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateUseNone, "QSdkCommandCreateUse;");
    SdkAssert.assertAnnotation(sdkCommandCreateUseNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateUseNone
    Assert.assertEquals("field count of 'SdkCommandCreateUseNone'", 1, sdkCommandCreateUseNone.fields().list().size());
    IField serialVersionUID19 = SdkAssert.assertFieldExist(sdkCommandCreateUseNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID19, 26);
    SdkAssert.assertFieldSignature(serialVersionUID19, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateUseNone'", 1, sdkCommandCreateUseNone.methods().list().size());
    IMethod sdkCommandCreateUseNone1 = SdkAssert.assertMethodExist(sdkCommandCreateUseNone, "SdkCommandCreateUseNone", new String[]{});
    Assert.assertTrue(sdkCommandCreateUseNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateUseNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateUseNone'", 0, sdkCommandCreateUseNone.innerTypes().list().size());
    // type SdkCommandCreateUseUse
    IType sdkCommandCreateUseUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseUse");
    SdkAssert.assertHasFlags(sdkCommandCreateUseUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateUseUse, "QSdkCommandCreateUse;");
    SdkAssert.assertAnnotation(sdkCommandCreateUseUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateUseUse
    Assert.assertEquals("field count of 'SdkCommandCreateUseUse'", 1, sdkCommandCreateUseUse.fields().list().size());
    IField serialVersionUID20 = SdkAssert.assertFieldExist(sdkCommandCreateUseUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID20, 26);
    SdkAssert.assertFieldSignature(serialVersionUID20, "J");

    Assert.assertEquals("method count of 'SdkCommandCreateUseUse'", 1, sdkCommandCreateUseUse.methods().list().size());
    IMethod sdkCommandCreateUseUse1 = SdkAssert.assertMethodExist(sdkCommandCreateUseUse, "SdkCommandCreateUseUse", new String[]{});
    Assert.assertTrue(sdkCommandCreateUseUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateUseUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandCreateUseUse'", 0, sdkCommandCreateUseUse.innerTypes().list().size());
    // type SdkCommandIgnoreCreateCreate
    IType sdkCommandIgnoreCreateCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateCreate");
    SdkAssert.assertHasFlags(sdkCommandIgnoreCreateCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreCreateCreate, "QSdkCommandIgnoreCreate;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreCreateCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreCreateCreate
    Assert.assertEquals("field count of 'SdkCommandIgnoreCreateCreate'", 1, sdkCommandIgnoreCreateCreate.fields().list().size());
    IField serialVersionUID21 = SdkAssert.assertFieldExist(sdkCommandIgnoreCreateCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID21, 26);
    SdkAssert.assertFieldSignature(serialVersionUID21, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreCreateCreate'", 1, sdkCommandIgnoreCreateCreate.methods().list().size());
    IMethod sdkCommandIgnoreCreateCreate1 = SdkAssert.assertMethodExist(sdkCommandIgnoreCreateCreate, "SdkCommandIgnoreCreateCreate", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreCreateCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreCreateCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreCreateCreate'", 0, sdkCommandIgnoreCreateCreate.innerTypes().list().size());
    // type SdkCommandIgnoreCreateIgnore
    IType sdkCommandIgnoreCreateIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateIgnore");
    SdkAssert.assertHasFlags(sdkCommandIgnoreCreateIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreCreateIgnore, "QSdkCommandIgnoreCreate;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreCreateIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreCreateIgnore
    Assert.assertEquals("field count of 'SdkCommandIgnoreCreateIgnore'", 1, sdkCommandIgnoreCreateIgnore.fields().list().size());
    IField serialVersionUID22 = SdkAssert.assertFieldExist(sdkCommandIgnoreCreateIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID22, 26);
    SdkAssert.assertFieldSignature(serialVersionUID22, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreCreateIgnore'", 1, sdkCommandIgnoreCreateIgnore.methods().list().size());
    IMethod sdkCommandIgnoreCreateIgnore1 = SdkAssert.assertMethodExist(sdkCommandIgnoreCreateIgnore, "SdkCommandIgnoreCreateIgnore", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreCreateIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreCreateIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreCreateIgnore'", 0, sdkCommandIgnoreCreateIgnore.innerTypes().list().size());
    // type SdkCommandIgnoreCreateNone
    IType sdkCommandIgnoreCreateNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateNone");
    SdkAssert.assertHasFlags(sdkCommandIgnoreCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreCreateNone, "QSdkCommandIgnoreCreate;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreCreateNone
    Assert.assertEquals("field count of 'SdkCommandIgnoreCreateNone'", 1, sdkCommandIgnoreCreateNone.fields().list().size());
    IField serialVersionUID23 = SdkAssert.assertFieldExist(sdkCommandIgnoreCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID23, 26);
    SdkAssert.assertFieldSignature(serialVersionUID23, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreCreateNone'", 1, sdkCommandIgnoreCreateNone.methods().list().size());
    IMethod sdkCommandIgnoreCreateNone1 = SdkAssert.assertMethodExist(sdkCommandIgnoreCreateNone, "SdkCommandIgnoreCreateNone", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreCreateNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreCreateNone'", 0, sdkCommandIgnoreCreateNone.innerTypes().list().size());
    // type SdkCommandIgnoreCreateUse
    IType sdkCommandIgnoreCreateUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateUse");
    SdkAssert.assertHasFlags(sdkCommandIgnoreCreateUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreCreateUse, "QSdkCommandIgnoreCreate;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreCreateUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreCreateUse
    Assert.assertEquals("field count of 'SdkCommandIgnoreCreateUse'", 1, sdkCommandIgnoreCreateUse.fields().list().size());
    IField serialVersionUID24 = SdkAssert.assertFieldExist(sdkCommandIgnoreCreateUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID24, 26);
    SdkAssert.assertFieldSignature(serialVersionUID24, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreCreateUse'", 1, sdkCommandIgnoreCreateUse.methods().list().size());
    IMethod sdkCommandIgnoreCreateUse1 = SdkAssert.assertMethodExist(sdkCommandIgnoreCreateUse, "SdkCommandIgnoreCreateUse", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreCreateUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreCreateUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreCreateUse'", 0, sdkCommandIgnoreCreateUse.innerTypes().list().size());
    // type SdkCommandIgnoreIgnoreCreate
    IType sdkCommandIgnoreIgnoreCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreIgnoreCreate");
    SdkAssert.assertHasFlags(sdkCommandIgnoreIgnoreCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreIgnoreCreate, "QAbstractValueFieldData<QString;>;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreIgnoreCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreIgnoreCreate
    Assert.assertEquals("field count of 'SdkCommandIgnoreIgnoreCreate'", 1, sdkCommandIgnoreIgnoreCreate.fields().list().size());
    IField serialVersionUID25 = SdkAssert.assertFieldExist(sdkCommandIgnoreIgnoreCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID25, 26);
    SdkAssert.assertFieldSignature(serialVersionUID25, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreIgnoreCreate'", 1, sdkCommandIgnoreIgnoreCreate.methods().list().size());
    IMethod sdkCommandIgnoreIgnoreCreate1 = SdkAssert.assertMethodExist(sdkCommandIgnoreIgnoreCreate, "SdkCommandIgnoreIgnoreCreate", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreIgnoreCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreIgnoreCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreIgnoreCreate'", 0, sdkCommandIgnoreIgnoreCreate.innerTypes().list().size());
    // type SdkCommandIgnoreIgnoreUse
    IType sdkCommandIgnoreIgnoreUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreIgnoreUse");
    SdkAssert.assertHasFlags(sdkCommandIgnoreIgnoreUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreIgnoreUse, "QUsingFormFieldData;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreIgnoreUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreIgnoreUse
    Assert.assertEquals("field count of 'SdkCommandIgnoreIgnoreUse'", 1, sdkCommandIgnoreIgnoreUse.fields().list().size());
    IField serialVersionUID26 = SdkAssert.assertFieldExist(sdkCommandIgnoreIgnoreUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID26, 26);
    SdkAssert.assertFieldSignature(serialVersionUID26, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreIgnoreUse'", 1, sdkCommandIgnoreIgnoreUse.methods().list().size());
    IMethod sdkCommandIgnoreIgnoreUse1 = SdkAssert.assertMethodExist(sdkCommandIgnoreIgnoreUse, "SdkCommandIgnoreIgnoreUse", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreIgnoreUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreIgnoreUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreIgnoreUse'", 0, sdkCommandIgnoreIgnoreUse.innerTypes().list().size());
    // type SdkCommandIgnoreNoneCreate
    IType sdkCommandIgnoreNoneCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreNoneCreate");
    SdkAssert.assertHasFlags(sdkCommandIgnoreNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreNoneCreate, "QAbstractValueFieldData<QString;>;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreNoneCreate
    Assert.assertEquals("field count of 'SdkCommandIgnoreNoneCreate'", 1, sdkCommandIgnoreNoneCreate.fields().list().size());
    IField serialVersionUID27 = SdkAssert.assertFieldExist(sdkCommandIgnoreNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID27, 26);
    SdkAssert.assertFieldSignature(serialVersionUID27, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreNoneCreate'", 1, sdkCommandIgnoreNoneCreate.methods().list().size());
    IMethod sdkCommandIgnoreNoneCreate1 = SdkAssert.assertMethodExist(sdkCommandIgnoreNoneCreate, "SdkCommandIgnoreNoneCreate", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreNoneCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreNoneCreate'", 0, sdkCommandIgnoreNoneCreate.innerTypes().list().size());
    // type SdkCommandIgnoreNoneUse
    IType sdkCommandIgnoreNoneUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreNoneUse");
    SdkAssert.assertHasFlags(sdkCommandIgnoreNoneUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreNoneUse, "QUsingFormFieldData;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreNoneUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreNoneUse
    Assert.assertEquals("field count of 'SdkCommandIgnoreNoneUse'", 1, sdkCommandIgnoreNoneUse.fields().list().size());
    IField serialVersionUID28 = SdkAssert.assertFieldExist(sdkCommandIgnoreNoneUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID28, 26);
    SdkAssert.assertFieldSignature(serialVersionUID28, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreNoneUse'", 1, sdkCommandIgnoreNoneUse.methods().list().size());
    IMethod sdkCommandIgnoreNoneUse1 = SdkAssert.assertMethodExist(sdkCommandIgnoreNoneUse, "SdkCommandIgnoreNoneUse", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreNoneUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreNoneUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreNoneUse'", 0, sdkCommandIgnoreNoneUse.innerTypes().list().size());
    // type SdkCommandIgnoreUseCreate
    IType sdkCommandIgnoreUseCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseCreate");
    SdkAssert.assertHasFlags(sdkCommandIgnoreUseCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreUseCreate, "QSdkCommandIgnoreUse;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreUseCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreUseCreate
    Assert.assertEquals("field count of 'SdkCommandIgnoreUseCreate'", 1, sdkCommandIgnoreUseCreate.fields().list().size());
    IField serialVersionUID29 = SdkAssert.assertFieldExist(sdkCommandIgnoreUseCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID29, 26);
    SdkAssert.assertFieldSignature(serialVersionUID29, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreUseCreate'", 1, sdkCommandIgnoreUseCreate.methods().list().size());
    IMethod sdkCommandIgnoreUseCreate1 = SdkAssert.assertMethodExist(sdkCommandIgnoreUseCreate, "SdkCommandIgnoreUseCreate", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreUseCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreUseCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreUseCreate'", 0, sdkCommandIgnoreUseCreate.innerTypes().list().size());
    // type SdkCommandIgnoreUseIgnore
    IType sdkCommandIgnoreUseIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseIgnore");
    SdkAssert.assertHasFlags(sdkCommandIgnoreUseIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreUseIgnore, "QSdkCommandIgnoreUse;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreUseIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreUseIgnore
    Assert.assertEquals("field count of 'SdkCommandIgnoreUseIgnore'", 1, sdkCommandIgnoreUseIgnore.fields().list().size());
    IField serialVersionUID30 = SdkAssert.assertFieldExist(sdkCommandIgnoreUseIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID30, 26);
    SdkAssert.assertFieldSignature(serialVersionUID30, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreUseIgnore'", 1, sdkCommandIgnoreUseIgnore.methods().list().size());
    IMethod sdkCommandIgnoreUseIgnore1 = SdkAssert.assertMethodExist(sdkCommandIgnoreUseIgnore, "SdkCommandIgnoreUseIgnore", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreUseIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreUseIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreUseIgnore'", 0, sdkCommandIgnoreUseIgnore.innerTypes().list().size());
    // type SdkCommandIgnoreUseNone
    IType sdkCommandIgnoreUseNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseNone");
    SdkAssert.assertHasFlags(sdkCommandIgnoreUseNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreUseNone, "QSdkCommandIgnoreUse;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreUseNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreUseNone
    Assert.assertEquals("field count of 'SdkCommandIgnoreUseNone'", 1, sdkCommandIgnoreUseNone.fields().list().size());
    IField serialVersionUID31 = SdkAssert.assertFieldExist(sdkCommandIgnoreUseNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID31, 26);
    SdkAssert.assertFieldSignature(serialVersionUID31, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreUseNone'", 1, sdkCommandIgnoreUseNone.methods().list().size());
    IMethod sdkCommandIgnoreUseNone1 = SdkAssert.assertMethodExist(sdkCommandIgnoreUseNone, "SdkCommandIgnoreUseNone", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreUseNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreUseNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreUseNone'", 0, sdkCommandIgnoreUseNone.innerTypes().list().size());
    // type SdkCommandIgnoreUseUse
    IType sdkCommandIgnoreUseUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseUse");
    SdkAssert.assertHasFlags(sdkCommandIgnoreUseUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreUseUse, "QSdkCommandIgnoreUse;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreUseUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreUseUse
    Assert.assertEquals("field count of 'SdkCommandIgnoreUseUse'", 1, sdkCommandIgnoreUseUse.fields().list().size());
    IField serialVersionUID32 = SdkAssert.assertFieldExist(sdkCommandIgnoreUseUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID32, 26);
    SdkAssert.assertFieldSignature(serialVersionUID32, "J");

    Assert.assertEquals("method count of 'SdkCommandIgnoreUseUse'", 1, sdkCommandIgnoreUseUse.methods().list().size());
    IMethod sdkCommandIgnoreUseUse1 = SdkAssert.assertMethodExist(sdkCommandIgnoreUseUse, "SdkCommandIgnoreUseUse", new String[]{});
    Assert.assertTrue(sdkCommandIgnoreUseUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreUseUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandIgnoreUseUse'", 0, sdkCommandIgnoreUseUse.innerTypes().list().size());
    // type SdkCommandNoneCreateCreate
    IType sdkCommandNoneCreateCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateCreate");
    SdkAssert.assertHasFlags(sdkCommandNoneCreateCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneCreateCreate, "QSdkCommandNoneCreate;");
    SdkAssert.assertAnnotation(sdkCommandNoneCreateCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneCreateCreate
    Assert.assertEquals("field count of 'SdkCommandNoneCreateCreate'", 1, sdkCommandNoneCreateCreate.fields().list().size());
    IField serialVersionUID33 = SdkAssert.assertFieldExist(sdkCommandNoneCreateCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID33, 26);
    SdkAssert.assertFieldSignature(serialVersionUID33, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneCreateCreate'", 1, sdkCommandNoneCreateCreate.methods().list().size());
    IMethod sdkCommandNoneCreateCreate1 = SdkAssert.assertMethodExist(sdkCommandNoneCreateCreate, "SdkCommandNoneCreateCreate", new String[]{});
    Assert.assertTrue(sdkCommandNoneCreateCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneCreateCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneCreateCreate'", 0, sdkCommandNoneCreateCreate.innerTypes().list().size());
    // type SdkCommandNoneCreateIgnore
    IType sdkCommandNoneCreateIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateIgnore");
    SdkAssert.assertHasFlags(sdkCommandNoneCreateIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneCreateIgnore, "QSdkCommandNoneCreate;");
    SdkAssert.assertAnnotation(sdkCommandNoneCreateIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneCreateIgnore
    Assert.assertEquals("field count of 'SdkCommandNoneCreateIgnore'", 1, sdkCommandNoneCreateIgnore.fields().list().size());
    IField serialVersionUID34 = SdkAssert.assertFieldExist(sdkCommandNoneCreateIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID34, 26);
    SdkAssert.assertFieldSignature(serialVersionUID34, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneCreateIgnore'", 1, sdkCommandNoneCreateIgnore.methods().list().size());
    IMethod sdkCommandNoneCreateIgnore1 = SdkAssert.assertMethodExist(sdkCommandNoneCreateIgnore, "SdkCommandNoneCreateIgnore", new String[]{});
    Assert.assertTrue(sdkCommandNoneCreateIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneCreateIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneCreateIgnore'", 0, sdkCommandNoneCreateIgnore.innerTypes().list().size());
    // type SdkCommandNoneCreateNone
    IType sdkCommandNoneCreateNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateNone");
    SdkAssert.assertHasFlags(sdkCommandNoneCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneCreateNone, "QSdkCommandNoneCreate;");
    SdkAssert.assertAnnotation(sdkCommandNoneCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneCreateNone
    Assert.assertEquals("field count of 'SdkCommandNoneCreateNone'", 1, sdkCommandNoneCreateNone.fields().list().size());
    IField serialVersionUID35 = SdkAssert.assertFieldExist(sdkCommandNoneCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID35, 26);
    SdkAssert.assertFieldSignature(serialVersionUID35, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneCreateNone'", 1, sdkCommandNoneCreateNone.methods().list().size());
    IMethod sdkCommandNoneCreateNone1 = SdkAssert.assertMethodExist(sdkCommandNoneCreateNone, "SdkCommandNoneCreateNone", new String[]{});
    Assert.assertTrue(sdkCommandNoneCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneCreateNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneCreateNone'", 0, sdkCommandNoneCreateNone.innerTypes().list().size());
    // type SdkCommandNoneCreateUse
    IType sdkCommandNoneCreateUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateUse");
    SdkAssert.assertHasFlags(sdkCommandNoneCreateUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneCreateUse, "QSdkCommandNoneCreate;");
    SdkAssert.assertAnnotation(sdkCommandNoneCreateUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneCreateUse
    Assert.assertEquals("field count of 'SdkCommandNoneCreateUse'", 1, sdkCommandNoneCreateUse.fields().list().size());
    IField serialVersionUID36 = SdkAssert.assertFieldExist(sdkCommandNoneCreateUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID36, 26);
    SdkAssert.assertFieldSignature(serialVersionUID36, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneCreateUse'", 1, sdkCommandNoneCreateUse.methods().list().size());
    IMethod sdkCommandNoneCreateUse1 = SdkAssert.assertMethodExist(sdkCommandNoneCreateUse, "SdkCommandNoneCreateUse", new String[]{});
    Assert.assertTrue(sdkCommandNoneCreateUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneCreateUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneCreateUse'", 0, sdkCommandNoneCreateUse.innerTypes().list().size());
    // type SdkCommandNoneIgnoreCreate
    IType sdkCommandNoneIgnoreCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreCreate");
    SdkAssert.assertHasFlags(sdkCommandNoneIgnoreCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneIgnoreCreate, "QSdkCommandNoneIgnore;");
    SdkAssert.assertAnnotation(sdkCommandNoneIgnoreCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneIgnoreCreate
    Assert.assertEquals("field count of 'SdkCommandNoneIgnoreCreate'", 1, sdkCommandNoneIgnoreCreate.fields().list().size());
    IField serialVersionUID37 = SdkAssert.assertFieldExist(sdkCommandNoneIgnoreCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID37, 26);
    SdkAssert.assertFieldSignature(serialVersionUID37, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneIgnoreCreate'", 1, sdkCommandNoneIgnoreCreate.methods().list().size());
    IMethod sdkCommandNoneIgnoreCreate1 = SdkAssert.assertMethodExist(sdkCommandNoneIgnoreCreate, "SdkCommandNoneIgnoreCreate", new String[]{});
    Assert.assertTrue(sdkCommandNoneIgnoreCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneIgnoreCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneIgnoreCreate'", 0, sdkCommandNoneIgnoreCreate.innerTypes().list().size());
    // type SdkCommandNoneIgnoreIgnore
    IType sdkCommandNoneIgnoreIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreIgnore");
    SdkAssert.assertHasFlags(sdkCommandNoneIgnoreIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneIgnoreIgnore, "QSdkCommandNoneIgnore;");
    SdkAssert.assertAnnotation(sdkCommandNoneIgnoreIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneIgnoreIgnore
    Assert.assertEquals("field count of 'SdkCommandNoneIgnoreIgnore'", 1, sdkCommandNoneIgnoreIgnore.fields().list().size());
    IField serialVersionUID38 = SdkAssert.assertFieldExist(sdkCommandNoneIgnoreIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID38, 26);
    SdkAssert.assertFieldSignature(serialVersionUID38, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneIgnoreIgnore'", 1, sdkCommandNoneIgnoreIgnore.methods().list().size());
    IMethod sdkCommandNoneIgnoreIgnore1 = SdkAssert.assertMethodExist(sdkCommandNoneIgnoreIgnore, "SdkCommandNoneIgnoreIgnore", new String[]{});
    Assert.assertTrue(sdkCommandNoneIgnoreIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneIgnoreIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneIgnoreIgnore'", 0, sdkCommandNoneIgnoreIgnore.innerTypes().list().size());
    // type SdkCommandNoneIgnoreNone
    IType sdkCommandNoneIgnoreNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreNone");
    SdkAssert.assertHasFlags(sdkCommandNoneIgnoreNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneIgnoreNone, "QSdkCommandNoneIgnore;");
    SdkAssert.assertAnnotation(sdkCommandNoneIgnoreNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneIgnoreNone
    Assert.assertEquals("field count of 'SdkCommandNoneIgnoreNone'", 1, sdkCommandNoneIgnoreNone.fields().list().size());
    IField serialVersionUID39 = SdkAssert.assertFieldExist(sdkCommandNoneIgnoreNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID39, 26);
    SdkAssert.assertFieldSignature(serialVersionUID39, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneIgnoreNone'", 1, sdkCommandNoneIgnoreNone.methods().list().size());
    IMethod sdkCommandNoneIgnoreNone1 = SdkAssert.assertMethodExist(sdkCommandNoneIgnoreNone, "SdkCommandNoneIgnoreNone", new String[]{});
    Assert.assertTrue(sdkCommandNoneIgnoreNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneIgnoreNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneIgnoreNone'", 0, sdkCommandNoneIgnoreNone.innerTypes().list().size());
    // type SdkCommandNoneIgnoreUse
    IType sdkCommandNoneIgnoreUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreUse");
    SdkAssert.assertHasFlags(sdkCommandNoneIgnoreUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneIgnoreUse, "QSdkCommandNoneIgnore;");
    SdkAssert.assertAnnotation(sdkCommandNoneIgnoreUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneIgnoreUse
    Assert.assertEquals("field count of 'SdkCommandNoneIgnoreUse'", 1, sdkCommandNoneIgnoreUse.fields().list().size());
    IField serialVersionUID40 = SdkAssert.assertFieldExist(sdkCommandNoneIgnoreUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID40, 26);
    SdkAssert.assertFieldSignature(serialVersionUID40, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneIgnoreUse'", 1, sdkCommandNoneIgnoreUse.methods().list().size());
    IMethod sdkCommandNoneIgnoreUse1 = SdkAssert.assertMethodExist(sdkCommandNoneIgnoreUse, "SdkCommandNoneIgnoreUse", new String[]{});
    Assert.assertTrue(sdkCommandNoneIgnoreUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneIgnoreUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneIgnoreUse'", 0, sdkCommandNoneIgnoreUse.innerTypes().list().size());
    // type SdkCommandNoneNoneCreate
    IType sdkCommandNoneNoneCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneCreate");
    SdkAssert.assertHasFlags(sdkCommandNoneNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneNoneCreate, "QSdkCommandNoneNone;");
    SdkAssert.assertHasSuperIntefaceSignatures(sdkCommandNoneNoneCreate, new String[]{"QIFormDataInterface03;"});
    SdkAssert.assertAnnotation(sdkCommandNoneNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneNoneCreate
    Assert.assertEquals("field count of 'SdkCommandNoneNoneCreate'", 1, sdkCommandNoneNoneCreate.fields().list().size());
    IField serialVersionUID41 = SdkAssert.assertFieldExist(sdkCommandNoneNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID41, 26);
    SdkAssert.assertFieldSignature(serialVersionUID41, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneNoneCreate'", 1, sdkCommandNoneNoneCreate.methods().list().size());
    IMethod sdkCommandNoneNoneCreate1 = SdkAssert.assertMethodExist(sdkCommandNoneNoneCreate, "SdkCommandNoneNoneCreate", new String[]{});
    Assert.assertTrue(sdkCommandNoneNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneNoneCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneNoneCreate'", 0, sdkCommandNoneNoneCreate.innerTypes().list().size());
    // type SdkCommandNoneNoneIgnore
    IType sdkCommandNoneNoneIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneIgnore");
    SdkAssert.assertHasFlags(sdkCommandNoneNoneIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneNoneIgnore, "QSdkCommandNoneNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneNoneIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneNoneIgnore
    Assert.assertEquals("field count of 'SdkCommandNoneNoneIgnore'", 1, sdkCommandNoneNoneIgnore.fields().list().size());
    IField serialVersionUID42 = SdkAssert.assertFieldExist(sdkCommandNoneNoneIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID42, 26);
    SdkAssert.assertFieldSignature(serialVersionUID42, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneNoneIgnore'", 1, sdkCommandNoneNoneIgnore.methods().list().size());
    IMethod sdkCommandNoneNoneIgnore1 = SdkAssert.assertMethodExist(sdkCommandNoneNoneIgnore, "SdkCommandNoneNoneIgnore", new String[]{});
    Assert.assertTrue(sdkCommandNoneNoneIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneNoneIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneNoneIgnore'", 0, sdkCommandNoneNoneIgnore.innerTypes().list().size());
    // type SdkCommandNoneNoneNone
    IType sdkCommandNoneNoneNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneNone");
    SdkAssert.assertHasFlags(sdkCommandNoneNoneNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneNoneNone, "QSdkCommandNoneNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneNoneNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneNoneNone
    Assert.assertEquals("field count of 'SdkCommandNoneNoneNone'", 1, sdkCommandNoneNoneNone.fields().list().size());
    IField serialVersionUID43 = SdkAssert.assertFieldExist(sdkCommandNoneNoneNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID43, 26);
    SdkAssert.assertFieldSignature(serialVersionUID43, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneNoneNone'", 1, sdkCommandNoneNoneNone.methods().list().size());
    IMethod sdkCommandNoneNoneNone1 = SdkAssert.assertMethodExist(sdkCommandNoneNoneNone, "SdkCommandNoneNoneNone", new String[]{});
    Assert.assertTrue(sdkCommandNoneNoneNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneNoneNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneNoneNone'", 0, sdkCommandNoneNoneNone.innerTypes().list().size());
    // type SdkCommandNoneNoneUse
    IType sdkCommandNoneNoneUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneUse");
    SdkAssert.assertHasFlags(sdkCommandNoneNoneUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneNoneUse, "QSdkCommandNoneNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneNoneUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneNoneUse
    Assert.assertEquals("field count of 'SdkCommandNoneNoneUse'", 1, sdkCommandNoneNoneUse.fields().list().size());
    IField serialVersionUID44 = SdkAssert.assertFieldExist(sdkCommandNoneNoneUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID44, 26);
    SdkAssert.assertFieldSignature(serialVersionUID44, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneNoneUse'", 1, sdkCommandNoneNoneUse.methods().list().size());
    IMethod sdkCommandNoneNoneUse1 = SdkAssert.assertMethodExist(sdkCommandNoneNoneUse, "SdkCommandNoneNoneUse", new String[]{});
    Assert.assertTrue(sdkCommandNoneNoneUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneNoneUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneNoneUse'", 0, sdkCommandNoneNoneUse.innerTypes().list().size());
    // type SdkCommandNoneUseCreate
    IType sdkCommandNoneUseCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseCreate");
    SdkAssert.assertHasFlags(sdkCommandNoneUseCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneUseCreate, "QSdkCommandNoneUse;");
    SdkAssert.assertAnnotation(sdkCommandNoneUseCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneUseCreate
    Assert.assertEquals("field count of 'SdkCommandNoneUseCreate'", 1, sdkCommandNoneUseCreate.fields().list().size());
    IField serialVersionUID45 = SdkAssert.assertFieldExist(sdkCommandNoneUseCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID45, 26);
    SdkAssert.assertFieldSignature(serialVersionUID45, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneUseCreate'", 1, sdkCommandNoneUseCreate.methods().list().size());
    IMethod sdkCommandNoneUseCreate1 = SdkAssert.assertMethodExist(sdkCommandNoneUseCreate, "SdkCommandNoneUseCreate", new String[]{});
    Assert.assertTrue(sdkCommandNoneUseCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneUseCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneUseCreate'", 0, sdkCommandNoneUseCreate.innerTypes().list().size());
    // type SdkCommandNoneUseIgnore
    IType sdkCommandNoneUseIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseIgnore");
    SdkAssert.assertHasFlags(sdkCommandNoneUseIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneUseIgnore, "QSdkCommandNoneUse;");
    SdkAssert.assertAnnotation(sdkCommandNoneUseIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneUseIgnore
    Assert.assertEquals("field count of 'SdkCommandNoneUseIgnore'", 1, sdkCommandNoneUseIgnore.fields().list().size());
    IField serialVersionUID46 = SdkAssert.assertFieldExist(sdkCommandNoneUseIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID46, 26);
    SdkAssert.assertFieldSignature(serialVersionUID46, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneUseIgnore'", 1, sdkCommandNoneUseIgnore.methods().list().size());
    IMethod sdkCommandNoneUseIgnore1 = SdkAssert.assertMethodExist(sdkCommandNoneUseIgnore, "SdkCommandNoneUseIgnore", new String[]{});
    Assert.assertTrue(sdkCommandNoneUseIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneUseIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneUseIgnore'", 0, sdkCommandNoneUseIgnore.innerTypes().list().size());
    // type SdkCommandNoneUseNone
    IType sdkCommandNoneUseNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseNone");
    SdkAssert.assertHasFlags(sdkCommandNoneUseNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneUseNone, "QSdkCommandNoneUse;");
    SdkAssert.assertAnnotation(sdkCommandNoneUseNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneUseNone
    Assert.assertEquals("field count of 'SdkCommandNoneUseNone'", 1, sdkCommandNoneUseNone.fields().list().size());
    IField serialVersionUID47 = SdkAssert.assertFieldExist(sdkCommandNoneUseNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID47, 26);
    SdkAssert.assertFieldSignature(serialVersionUID47, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneUseNone'", 1, sdkCommandNoneUseNone.methods().list().size());
    IMethod sdkCommandNoneUseNone1 = SdkAssert.assertMethodExist(sdkCommandNoneUseNone, "SdkCommandNoneUseNone", new String[]{});
    Assert.assertTrue(sdkCommandNoneUseNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneUseNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneUseNone'", 0, sdkCommandNoneUseNone.innerTypes().list().size());
    // type SdkCommandNoneUseUse
    IType sdkCommandNoneUseUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseUse");
    SdkAssert.assertHasFlags(sdkCommandNoneUseUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneUseUse, "QSdkCommandNoneUse;");
    SdkAssert.assertAnnotation(sdkCommandNoneUseUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneUseUse
    Assert.assertEquals("field count of 'SdkCommandNoneUseUse'", 1, sdkCommandNoneUseUse.fields().list().size());
    IField serialVersionUID48 = SdkAssert.assertFieldExist(sdkCommandNoneUseUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID48, 26);
    SdkAssert.assertFieldSignature(serialVersionUID48, "J");

    Assert.assertEquals("method count of 'SdkCommandNoneUseUse'", 1, sdkCommandNoneUseUse.methods().list().size());
    IMethod sdkCommandNoneUseUse1 = SdkAssert.assertMethodExist(sdkCommandNoneUseUse, "SdkCommandNoneUseUse", new String[]{});
    Assert.assertTrue(sdkCommandNoneUseUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneUseUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandNoneUseUse'", 0, sdkCommandNoneUseUse.innerTypes().list().size());
    // type SdkCommandUseCreateCreate
    IType sdkCommandUseCreateCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateCreate");
    SdkAssert.assertHasFlags(sdkCommandUseCreateCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseCreateCreate, "QSdkCommandUseCreate;");
    SdkAssert.assertAnnotation(sdkCommandUseCreateCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseCreateCreate
    Assert.assertEquals("field count of 'SdkCommandUseCreateCreate'", 1, sdkCommandUseCreateCreate.fields().list().size());
    IField serialVersionUID49 = SdkAssert.assertFieldExist(sdkCommandUseCreateCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID49, 26);
    SdkAssert.assertFieldSignature(serialVersionUID49, "J");

    Assert.assertEquals("method count of 'SdkCommandUseCreateCreate'", 1, sdkCommandUseCreateCreate.methods().list().size());
    IMethod sdkCommandUseCreateCreate1 = SdkAssert.assertMethodExist(sdkCommandUseCreateCreate, "SdkCommandUseCreateCreate", new String[]{});
    Assert.assertTrue(sdkCommandUseCreateCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseCreateCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseCreateCreate'", 0, sdkCommandUseCreateCreate.innerTypes().list().size());
    // type SdkCommandUseCreateIgnore
    IType sdkCommandUseCreateIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateIgnore");
    SdkAssert.assertHasFlags(sdkCommandUseCreateIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseCreateIgnore, "QSdkCommandUseCreate;");
    SdkAssert.assertAnnotation(sdkCommandUseCreateIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseCreateIgnore
    Assert.assertEquals("field count of 'SdkCommandUseCreateIgnore'", 1, sdkCommandUseCreateIgnore.fields().list().size());
    IField serialVersionUID50 = SdkAssert.assertFieldExist(sdkCommandUseCreateIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID50, 26);
    SdkAssert.assertFieldSignature(serialVersionUID50, "J");

    Assert.assertEquals("method count of 'SdkCommandUseCreateIgnore'", 1, sdkCommandUseCreateIgnore.methods().list().size());
    IMethod sdkCommandUseCreateIgnore1 = SdkAssert.assertMethodExist(sdkCommandUseCreateIgnore, "SdkCommandUseCreateIgnore", new String[]{});
    Assert.assertTrue(sdkCommandUseCreateIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseCreateIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseCreateIgnore'", 0, sdkCommandUseCreateIgnore.innerTypes().list().size());
    // type SdkCommandUseCreateNone
    IType sdkCommandUseCreateNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateNone");
    SdkAssert.assertHasFlags(sdkCommandUseCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseCreateNone, "QSdkCommandUseCreate;");
    SdkAssert.assertAnnotation(sdkCommandUseCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseCreateNone
    Assert.assertEquals("field count of 'SdkCommandUseCreateNone'", 1, sdkCommandUseCreateNone.fields().list().size());
    IField serialVersionUID51 = SdkAssert.assertFieldExist(sdkCommandUseCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID51, 26);
    SdkAssert.assertFieldSignature(serialVersionUID51, "J");

    Assert.assertEquals("method count of 'SdkCommandUseCreateNone'", 1, sdkCommandUseCreateNone.methods().list().size());
    IMethod sdkCommandUseCreateNone1 = SdkAssert.assertMethodExist(sdkCommandUseCreateNone, "SdkCommandUseCreateNone", new String[]{});
    Assert.assertTrue(sdkCommandUseCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseCreateNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseCreateNone'", 0, sdkCommandUseCreateNone.innerTypes().list().size());
    // type SdkCommandUseCreateUse
    IType sdkCommandUseCreateUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateUse");
    SdkAssert.assertHasFlags(sdkCommandUseCreateUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseCreateUse, "QSdkCommandUseCreate;");
    SdkAssert.assertAnnotation(sdkCommandUseCreateUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseCreateUse
    Assert.assertEquals("field count of 'SdkCommandUseCreateUse'", 1, sdkCommandUseCreateUse.fields().list().size());
    IField serialVersionUID52 = SdkAssert.assertFieldExist(sdkCommandUseCreateUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID52, 26);
    SdkAssert.assertFieldSignature(serialVersionUID52, "J");

    Assert.assertEquals("method count of 'SdkCommandUseCreateUse'", 1, sdkCommandUseCreateUse.methods().list().size());
    IMethod sdkCommandUseCreateUse1 = SdkAssert.assertMethodExist(sdkCommandUseCreateUse, "SdkCommandUseCreateUse", new String[]{});
    Assert.assertTrue(sdkCommandUseCreateUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseCreateUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseCreateUse'", 0, sdkCommandUseCreateUse.innerTypes().list().size());
    // type SdkCommandUseIgnoreCreate
    IType sdkCommandUseIgnoreCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreCreate");
    SdkAssert.assertHasFlags(sdkCommandUseIgnoreCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseIgnoreCreate, "QSdkCommandUseIgnore;");
    SdkAssert.assertAnnotation(sdkCommandUseIgnoreCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseIgnoreCreate
    Assert.assertEquals("field count of 'SdkCommandUseIgnoreCreate'", 1, sdkCommandUseIgnoreCreate.fields().list().size());
    IField serialVersionUID53 = SdkAssert.assertFieldExist(sdkCommandUseIgnoreCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID53, 26);
    SdkAssert.assertFieldSignature(serialVersionUID53, "J");

    Assert.assertEquals("method count of 'SdkCommandUseIgnoreCreate'", 1, sdkCommandUseIgnoreCreate.methods().list().size());
    IMethod sdkCommandUseIgnoreCreate1 = SdkAssert.assertMethodExist(sdkCommandUseIgnoreCreate, "SdkCommandUseIgnoreCreate", new String[]{});
    Assert.assertTrue(sdkCommandUseIgnoreCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseIgnoreCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseIgnoreCreate'", 0, sdkCommandUseIgnoreCreate.innerTypes().list().size());
    // type SdkCommandUseIgnoreIgnore
    IType sdkCommandUseIgnoreIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreIgnore");
    SdkAssert.assertHasFlags(sdkCommandUseIgnoreIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseIgnoreIgnore, "QSdkCommandUseIgnore;");
    SdkAssert.assertAnnotation(sdkCommandUseIgnoreIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseIgnoreIgnore
    Assert.assertEquals("field count of 'SdkCommandUseIgnoreIgnore'", 1, sdkCommandUseIgnoreIgnore.fields().list().size());
    IField serialVersionUID54 = SdkAssert.assertFieldExist(sdkCommandUseIgnoreIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID54, 26);
    SdkAssert.assertFieldSignature(serialVersionUID54, "J");

    Assert.assertEquals("method count of 'SdkCommandUseIgnoreIgnore'", 1, sdkCommandUseIgnoreIgnore.methods().list().size());
    IMethod sdkCommandUseIgnoreIgnore1 = SdkAssert.assertMethodExist(sdkCommandUseIgnoreIgnore, "SdkCommandUseIgnoreIgnore", new String[]{});
    Assert.assertTrue(sdkCommandUseIgnoreIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseIgnoreIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseIgnoreIgnore'", 0, sdkCommandUseIgnoreIgnore.innerTypes().list().size());
    // type SdkCommandUseIgnoreNone
    IType sdkCommandUseIgnoreNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreNone");
    SdkAssert.assertHasFlags(sdkCommandUseIgnoreNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseIgnoreNone, "QSdkCommandUseIgnore;");
    SdkAssert.assertAnnotation(sdkCommandUseIgnoreNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseIgnoreNone
    Assert.assertEquals("field count of 'SdkCommandUseIgnoreNone'", 1, sdkCommandUseIgnoreNone.fields().list().size());
    IField serialVersionUID55 = SdkAssert.assertFieldExist(sdkCommandUseIgnoreNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID55, 26);
    SdkAssert.assertFieldSignature(serialVersionUID55, "J");

    Assert.assertEquals("method count of 'SdkCommandUseIgnoreNone'", 1, sdkCommandUseIgnoreNone.methods().list().size());
    IMethod sdkCommandUseIgnoreNone1 = SdkAssert.assertMethodExist(sdkCommandUseIgnoreNone, "SdkCommandUseIgnoreNone", new String[]{});
    Assert.assertTrue(sdkCommandUseIgnoreNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseIgnoreNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseIgnoreNone'", 0, sdkCommandUseIgnoreNone.innerTypes().list().size());
    // type SdkCommandUseIgnoreUse
    IType sdkCommandUseIgnoreUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreUse");
    SdkAssert.assertHasFlags(sdkCommandUseIgnoreUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseIgnoreUse, "QSdkCommandUseIgnore;");
    SdkAssert.assertAnnotation(sdkCommandUseIgnoreUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseIgnoreUse
    Assert.assertEquals("field count of 'SdkCommandUseIgnoreUse'", 1, sdkCommandUseIgnoreUse.fields().list().size());
    IField serialVersionUID56 = SdkAssert.assertFieldExist(sdkCommandUseIgnoreUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID56, 26);
    SdkAssert.assertFieldSignature(serialVersionUID56, "J");

    Assert.assertEquals("method count of 'SdkCommandUseIgnoreUse'", 1, sdkCommandUseIgnoreUse.methods().list().size());
    IMethod sdkCommandUseIgnoreUse1 = SdkAssert.assertMethodExist(sdkCommandUseIgnoreUse, "SdkCommandUseIgnoreUse", new String[]{});
    Assert.assertTrue(sdkCommandUseIgnoreUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseIgnoreUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseIgnoreUse'", 0, sdkCommandUseIgnoreUse.innerTypes().list().size());
    // type SdkCommandUseNoneCreate
    IType sdkCommandUseNoneCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneCreate");
    SdkAssert.assertHasFlags(sdkCommandUseNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseNoneCreate, "QSdkCommandUseNone;");
    SdkAssert.assertAnnotation(sdkCommandUseNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseNoneCreate
    Assert.assertEquals("field count of 'SdkCommandUseNoneCreate'", 1, sdkCommandUseNoneCreate.fields().list().size());
    IField serialVersionUID57 = SdkAssert.assertFieldExist(sdkCommandUseNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID57, 26);
    SdkAssert.assertFieldSignature(serialVersionUID57, "J");

    Assert.assertEquals("method count of 'SdkCommandUseNoneCreate'", 1, sdkCommandUseNoneCreate.methods().list().size());
    IMethod sdkCommandUseNoneCreate1 = SdkAssert.assertMethodExist(sdkCommandUseNoneCreate, "SdkCommandUseNoneCreate", new String[]{});
    Assert.assertTrue(sdkCommandUseNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseNoneCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseNoneCreate'", 0, sdkCommandUseNoneCreate.innerTypes().list().size());
    // type SdkCommandUseNoneIgnore
    IType sdkCommandUseNoneIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneIgnore");
    SdkAssert.assertHasFlags(sdkCommandUseNoneIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseNoneIgnore, "QSdkCommandUseNone;");
    SdkAssert.assertAnnotation(sdkCommandUseNoneIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseNoneIgnore
    Assert.assertEquals("field count of 'SdkCommandUseNoneIgnore'", 1, sdkCommandUseNoneIgnore.fields().list().size());
    IField serialVersionUID58 = SdkAssert.assertFieldExist(sdkCommandUseNoneIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID58, 26);
    SdkAssert.assertFieldSignature(serialVersionUID58, "J");

    Assert.assertEquals("method count of 'SdkCommandUseNoneIgnore'", 1, sdkCommandUseNoneIgnore.methods().list().size());
    IMethod sdkCommandUseNoneIgnore1 = SdkAssert.assertMethodExist(sdkCommandUseNoneIgnore, "SdkCommandUseNoneIgnore", new String[]{});
    Assert.assertTrue(sdkCommandUseNoneIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseNoneIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseNoneIgnore'", 0, sdkCommandUseNoneIgnore.innerTypes().list().size());
    // type SdkCommandUseNoneNone
    IType sdkCommandUseNoneNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneNone");
    SdkAssert.assertHasFlags(sdkCommandUseNoneNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseNoneNone, "QSdkCommandUseNone;");
    SdkAssert.assertAnnotation(sdkCommandUseNoneNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseNoneNone
    Assert.assertEquals("field count of 'SdkCommandUseNoneNone'", 1, sdkCommandUseNoneNone.fields().list().size());
    IField serialVersionUID59 = SdkAssert.assertFieldExist(sdkCommandUseNoneNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID59, 26);
    SdkAssert.assertFieldSignature(serialVersionUID59, "J");

    Assert.assertEquals("method count of 'SdkCommandUseNoneNone'", 1, sdkCommandUseNoneNone.methods().list().size());
    IMethod sdkCommandUseNoneNone1 = SdkAssert.assertMethodExist(sdkCommandUseNoneNone, "SdkCommandUseNoneNone", new String[]{});
    Assert.assertTrue(sdkCommandUseNoneNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseNoneNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseNoneNone'", 0, sdkCommandUseNoneNone.innerTypes().list().size());
    // type SdkCommandUseNoneUse
    IType sdkCommandUseNoneUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneUse");
    SdkAssert.assertHasFlags(sdkCommandUseNoneUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseNoneUse, "QSdkCommandUseNone;");
    SdkAssert.assertAnnotation(sdkCommandUseNoneUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseNoneUse
    Assert.assertEquals("field count of 'SdkCommandUseNoneUse'", 1, sdkCommandUseNoneUse.fields().list().size());
    IField serialVersionUID60 = SdkAssert.assertFieldExist(sdkCommandUseNoneUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID60, 26);
    SdkAssert.assertFieldSignature(serialVersionUID60, "J");

    Assert.assertEquals("method count of 'SdkCommandUseNoneUse'", 1, sdkCommandUseNoneUse.methods().list().size());
    IMethod sdkCommandUseNoneUse1 = SdkAssert.assertMethodExist(sdkCommandUseNoneUse, "SdkCommandUseNoneUse", new String[]{});
    Assert.assertTrue(sdkCommandUseNoneUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseNoneUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseNoneUse'", 0, sdkCommandUseNoneUse.innerTypes().list().size());
    // type SdkCommandUseUseCreate
    IType sdkCommandUseUseCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseCreate");
    SdkAssert.assertHasFlags(sdkCommandUseUseCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseUseCreate, "QSdkCommandUseUse;");
    SdkAssert.assertAnnotation(sdkCommandUseUseCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseUseCreate
    Assert.assertEquals("field count of 'SdkCommandUseUseCreate'", 1, sdkCommandUseUseCreate.fields().list().size());
    IField serialVersionUID61 = SdkAssert.assertFieldExist(sdkCommandUseUseCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID61, 26);
    SdkAssert.assertFieldSignature(serialVersionUID61, "J");

    Assert.assertEquals("method count of 'SdkCommandUseUseCreate'", 1, sdkCommandUseUseCreate.methods().list().size());
    IMethod sdkCommandUseUseCreate1 = SdkAssert.assertMethodExist(sdkCommandUseUseCreate, "SdkCommandUseUseCreate", new String[]{});
    Assert.assertTrue(sdkCommandUseUseCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseUseCreate1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseUseCreate'", 0, sdkCommandUseUseCreate.innerTypes().list().size());
    // type SdkCommandUseUseIgnore
    IType sdkCommandUseUseIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseIgnore");
    SdkAssert.assertHasFlags(sdkCommandUseUseIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseUseIgnore, "QSdkCommandUseUse;");
    SdkAssert.assertAnnotation(sdkCommandUseUseIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseUseIgnore
    Assert.assertEquals("field count of 'SdkCommandUseUseIgnore'", 1, sdkCommandUseUseIgnore.fields().list().size());
    IField serialVersionUID62 = SdkAssert.assertFieldExist(sdkCommandUseUseIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID62, 26);
    SdkAssert.assertFieldSignature(serialVersionUID62, "J");

    Assert.assertEquals("method count of 'SdkCommandUseUseIgnore'", 1, sdkCommandUseUseIgnore.methods().list().size());
    IMethod sdkCommandUseUseIgnore1 = SdkAssert.assertMethodExist(sdkCommandUseUseIgnore, "SdkCommandUseUseIgnore", new String[]{});
    Assert.assertTrue(sdkCommandUseUseIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseUseIgnore1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseUseIgnore'", 0, sdkCommandUseUseIgnore.innerTypes().list().size());
    // type SdkCommandUseUseNone
    IType sdkCommandUseUseNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseNone");
    SdkAssert.assertHasFlags(sdkCommandUseUseNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseUseNone, "QSdkCommandUseUse;");
    SdkAssert.assertAnnotation(sdkCommandUseUseNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseUseNone
    Assert.assertEquals("field count of 'SdkCommandUseUseNone'", 1, sdkCommandUseUseNone.fields().list().size());
    IField serialVersionUID63 = SdkAssert.assertFieldExist(sdkCommandUseUseNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID63, 26);
    SdkAssert.assertFieldSignature(serialVersionUID63, "J");

    Assert.assertEquals("method count of 'SdkCommandUseUseNone'", 1, sdkCommandUseUseNone.methods().list().size());
    IMethod sdkCommandUseUseNone1 = SdkAssert.assertMethodExist(sdkCommandUseUseNone, "SdkCommandUseUseNone", new String[]{});
    Assert.assertTrue(sdkCommandUseUseNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseUseNone1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseUseNone'", 0, sdkCommandUseUseNone.innerTypes().list().size());
    // type SdkCommandUseUseUse
    IType sdkCommandUseUseUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseUse");
    SdkAssert.assertHasFlags(sdkCommandUseUseUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseUseUse, "QSdkCommandUseUse;");
    SdkAssert.assertAnnotation(sdkCommandUseUseUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseUseUse
    Assert.assertEquals("field count of 'SdkCommandUseUseUse'", 1, sdkCommandUseUseUse.fields().list().size());
    IField serialVersionUID64 = SdkAssert.assertFieldExist(sdkCommandUseUseUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID64, 26);
    SdkAssert.assertFieldSignature(serialVersionUID64, "J");

    Assert.assertEquals("method count of 'SdkCommandUseUseUse'", 1, sdkCommandUseUseUse.methods().list().size());
    IMethod sdkCommandUseUseUse1 = SdkAssert.assertMethodExist(sdkCommandUseUseUse, "SdkCommandUseUseUse", new String[]{});
    Assert.assertTrue(sdkCommandUseUseUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseUseUse1, null);

    Assert.assertEquals("inner types count of 'SdkCommandUseUseUse'", 0, sdkCommandUseUseUse.innerTypes().list().size());
  }

}
