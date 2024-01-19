/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperInterfaces;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertTypeExists;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.junit.jupiter.api.Test;

public class ReplaceFormFieldTest {

  @Test
  public void runTests() {
    checkBaseFormData();
    checkExtendedFormData();
    checkExtendedExtendedFormData();
  }

  private static void checkBaseFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.replace.BaseForm", ReplaceFormFieldTest::testApiOfBaseFormData);
  }

  private static void checkExtendedFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.replace.ExtendedForm", ReplaceFormFieldTest::testApiOfExtendedFormData);
  }

  private static void checkExtendedExtendedFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.replace.ExtendedExtendedForm", ReplaceFormFieldTest::testApiOfExtendedExtendedFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfBaseFormData(IType baseFormData) {
    // type BaseFormData
    assertHasFlags(baseFormData, 1);
    assertHasSuperClass(baseFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");

    // fields of BaseFormData
    assertEquals(1, baseFormData.fields().stream().count(), "field count of 'BaseFormData'");
    var serialVersionUID = assertFieldExist(baseFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(6, baseFormData.methods().stream().count(), "method count of 'BaseFormData'");
    var getLookup = assertMethodExist(baseFormData, "getLookup");
    assertMethodReturnType(getLookup, "formdata.shared.services.process.replace.BaseFormData$Lookup");
    var getName = assertMethodExist(baseFormData, "getName");
    assertMethodReturnType(getName, "formdata.shared.services.process.replace.BaseFormData$Name");
    var getSdkCommandCreate = assertMethodExist(baseFormData, "getSdkCommandCreate");
    assertMethodReturnType(getSdkCommandCreate, "formdata.shared.services.process.replace.BaseFormData$SdkCommandCreate");
    var getSdkCommandNone = assertMethodExist(baseFormData, "getSdkCommandNone");
    assertMethodReturnType(getSdkCommandNone, "formdata.shared.services.process.replace.BaseFormData$SdkCommandNone");
    var getSdkCommandUse = assertMethodExist(baseFormData, "getSdkCommandUse");
    assertMethodReturnType(getSdkCommandUse, "formdata.shared.services.process.replace.BaseFormData$SdkCommandUse");
    var getSmart = assertMethodExist(baseFormData, "getSmart");
    assertMethodReturnType(getSmart, "formdata.shared.services.process.replace.BaseFormData$Smart");

    assertEquals(6, baseFormData.innerTypes().stream().count(), "inner types count of 'BaseFormData'");
    // type Lookup
    var lookup = assertTypeExists(baseFormData, "Lookup");
    assertHasFlags(lookup, 9);
    assertHasSuperClass(lookup, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Long>");

    // fields of Lookup
    assertEquals(1, lookup.fields().stream().count(), "field count of 'Lookup'");
    var serialVersionUID1 = assertFieldExist(lookup, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, lookup.methods().stream().count(), "method count of 'Lookup'");

    assertEquals(0, lookup.innerTypes().stream().count(), "inner types count of 'Lookup'");
    // type Name
    var name = assertTypeExists(baseFormData, "Name");
    assertHasFlags(name, 9);
    assertHasSuperClass(name, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of Name
    assertEquals(1, name.fields().stream().count(), "field count of 'Name'");
    var serialVersionUID2 = assertFieldExist(name, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, name.methods().stream().count(), "method count of 'Name'");

    assertEquals(0, name.innerTypes().stream().count(), "inner types count of 'Name'");
    // type SdkCommandCreate
    var sdkCommandCreate = assertTypeExists(baseFormData, "SdkCommandCreate");
    assertHasFlags(sdkCommandCreate, 9);
    assertHasSuperClass(sdkCommandCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of SdkCommandCreate
    assertEquals(1, sdkCommandCreate.fields().stream().count(), "field count of 'SdkCommandCreate'");
    var serialVersionUID3 = assertFieldExist(sdkCommandCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(0, sdkCommandCreate.methods().stream().count(), "method count of 'SdkCommandCreate'");

    assertEquals(0, sdkCommandCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreate'");
    // type SdkCommandNone
    var sdkCommandNone = assertTypeExists(baseFormData, "SdkCommandNone");
    assertHasFlags(sdkCommandNone, 9);
    assertHasSuperClass(sdkCommandNone, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of SdkCommandNone
    assertEquals(1, sdkCommandNone.fields().stream().count(), "field count of 'SdkCommandNone'");
    var serialVersionUID4 = assertFieldExist(sdkCommandNone, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(0, sdkCommandNone.methods().stream().count(), "method count of 'SdkCommandNone'");

    assertEquals(0, sdkCommandNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNone'");
    // type SdkCommandUse
    var sdkCommandUse = assertTypeExists(baseFormData, "SdkCommandUse");
    assertHasFlags(sdkCommandUse, 9);
    assertHasSuperClass(sdkCommandUse, "formdata.shared.services.process.replace.UsingFormFieldData");

    // fields of SdkCommandUse
    assertEquals(1, sdkCommandUse.fields().stream().count(), "field count of 'SdkCommandUse'");
    var serialVersionUID5 = assertFieldExist(sdkCommandUse, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(0, sdkCommandUse.methods().stream().count(), "method count of 'SdkCommandUse'");

    assertEquals(0, sdkCommandUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUse'");
    // type Smart
    var smart = assertTypeExists(baseFormData, "Smart");
    assertHasFlags(smart, 9);
    assertHasSuperClass(smart, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Long>");

    // fields of Smart
    assertEquals(1, smart.fields().stream().count(), "field count of 'Smart'");
    var serialVersionUID6 = assertFieldExist(smart, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");

    assertEquals(0, smart.methods().stream().count(), "method count of 'Smart'");

    assertEquals(0, smart.innerTypes().stream().count(), "inner types count of 'Smart'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfExtendedFormData(IType extendedFormData) {
    // type ExtendedFormData
    assertHasFlags(extendedFormData, 1);
    assertHasSuperClass(extendedFormData, "formdata.shared.services.process.replace.BaseFormData");

    // fields of ExtendedFormData
    assertEquals(1, extendedFormData.fields().stream().count(), "field count of 'ExtendedFormData'");
    var serialVersionUID = assertFieldExist(extendedFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(19, extendedFormData.methods().stream().count(), "method count of 'ExtendedFormData'");
    var getFirstName = assertMethodExist(extendedFormData, "getFirstName");
    assertMethodReturnType(getFirstName, "formdata.shared.services.process.replace.ExtendedFormData$FirstName");
    var getIgnoringGroupBoxExCreate = assertMethodExist(extendedFormData, "getIgnoringGroupBoxExCreate");
    assertMethodReturnType(getIgnoringGroupBoxExCreate, "formdata.shared.services.process.replace.ExtendedFormData$IgnoringGroupBoxExCreate");
    var getIgnoringGroupBoxExUse = assertMethodExist(extendedFormData, "getIgnoringGroupBoxExUse");
    assertMethodReturnType(getIgnoringGroupBoxExUse, "formdata.shared.services.process.replace.ExtendedFormData$IgnoringGroupBoxExUse");
    var getNameEx = assertMethodExist(extendedFormData, "getNameEx");
    assertMethodReturnType(getNameEx, "formdata.shared.services.process.replace.ExtendedFormData$NameEx");
    var getSdkCommandCreateCreate = assertMethodExist(extendedFormData, "getSdkCommandCreateCreate");
    assertMethodReturnType(getSdkCommandCreateCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateCreate");
    var getSdkCommandCreateIgnore = assertMethodExist(extendedFormData, "getSdkCommandCreateIgnore");
    assertMethodReturnType(getSdkCommandCreateIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateIgnore");
    var getSdkCommandCreateNone = assertMethodExist(extendedFormData, "getSdkCommandCreateNone");
    assertMethodReturnType(getSdkCommandCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateNone");
    var getSdkCommandCreateUse = assertMethodExist(extendedFormData, "getSdkCommandCreateUse");
    assertMethodReturnType(getSdkCommandCreateUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateUse");
    var getSdkCommandIgnoreCreate = assertMethodExist(extendedFormData, "getSdkCommandIgnoreCreate");
    assertMethodReturnType(getSdkCommandIgnoreCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreCreate");
    var getSdkCommandIgnoreUse = assertMethodExist(extendedFormData, "getSdkCommandIgnoreUse");
    assertMethodReturnType(getSdkCommandIgnoreUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreUse");
    var getSdkCommandNoneCreate = assertMethodExist(extendedFormData, "getSdkCommandNoneCreate");
    assertMethodReturnType(getSdkCommandNoneCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneCreate");
    var getSdkCommandNoneIgnore = assertMethodExist(extendedFormData, "getSdkCommandNoneIgnore");
    assertMethodReturnType(getSdkCommandNoneIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneIgnore");
    var getSdkCommandNoneNone = assertMethodExist(extendedFormData, "getSdkCommandNoneNone");
    assertMethodReturnType(getSdkCommandNoneNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneNone");
    var getSdkCommandNoneUse = assertMethodExist(extendedFormData, "getSdkCommandNoneUse");
    assertMethodReturnType(getSdkCommandNoneUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneUse");
    var getSdkCommandUseCreate = assertMethodExist(extendedFormData, "getSdkCommandUseCreate");
    assertMethodReturnType(getSdkCommandUseCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseCreate");
    var getSdkCommandUseIgnore = assertMethodExist(extendedFormData, "getSdkCommandUseIgnore");
    assertMethodReturnType(getSdkCommandUseIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseIgnore");
    var getSdkCommandUseNone = assertMethodExist(extendedFormData, "getSdkCommandUseNone");
    assertMethodReturnType(getSdkCommandUseNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseNone");
    var getSdkCommandUseUse = assertMethodExist(extendedFormData, "getSdkCommandUseUse");
    assertMethodReturnType(getSdkCommandUseUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseUse");
    var getSmartEx = assertMethodExist(extendedFormData, "getSmartEx");
    assertMethodReturnType(getSmartEx, "formdata.shared.services.process.replace.ExtendedFormData$SmartEx");

    assertEquals(19, extendedFormData.innerTypes().stream().count(), "inner types count of 'ExtendedFormData'");
    // type FirstName
    var firstName = assertTypeExists(extendedFormData, "FirstName");
    assertHasFlags(firstName, 9);
    assertHasSuperClass(firstName, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of FirstName
    assertEquals(1, firstName.fields().stream().count(), "field count of 'FirstName'");
    var serialVersionUID1 = assertFieldExist(firstName, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, firstName.methods().stream().count(), "method count of 'FirstName'");

    assertEquals(0, firstName.innerTypes().stream().count(), "inner types count of 'FirstName'");
    // type IgnoringGroupBoxExCreate
    var ignoringGroupBoxExCreate = assertTypeExists(extendedFormData, "IgnoringGroupBoxExCreate");
    assertHasFlags(ignoringGroupBoxExCreate, 9);
    assertHasSuperClass(ignoringGroupBoxExCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertAnnotation(ignoringGroupBoxExCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of IgnoringGroupBoxExCreate
    assertEquals(1, ignoringGroupBoxExCreate.fields().stream().count(), "field count of 'IgnoringGroupBoxExCreate'");
    var serialVersionUID2 = assertFieldExist(ignoringGroupBoxExCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, ignoringGroupBoxExCreate.methods().stream().count(), "method count of 'IgnoringGroupBoxExCreate'");

    assertEquals(0, ignoringGroupBoxExCreate.innerTypes().stream().count(), "inner types count of 'IgnoringGroupBoxExCreate'");
    // type IgnoringGroupBoxExUse
    var ignoringGroupBoxExUse = assertTypeExists(extendedFormData, "IgnoringGroupBoxExUse");
    assertHasFlags(ignoringGroupBoxExUse, 9);
    assertHasSuperClass(ignoringGroupBoxExUse, "formdata.shared.services.process.replace.UsingFormFieldData");
    assertAnnotation(ignoringGroupBoxExUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of IgnoringGroupBoxExUse
    assertEquals(1, ignoringGroupBoxExUse.fields().stream().count(), "field count of 'IgnoringGroupBoxExUse'");
    var serialVersionUID3 = assertFieldExist(ignoringGroupBoxExUse, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(0, ignoringGroupBoxExUse.methods().stream().count(), "method count of 'IgnoringGroupBoxExUse'");

    assertEquals(0, ignoringGroupBoxExUse.innerTypes().stream().count(), "inner types count of 'IgnoringGroupBoxExUse'");
    // type NameEx
    var nameEx = assertTypeExists(extendedFormData, "NameEx");
    assertHasFlags(nameEx, 9);
    assertHasSuperClass(nameEx, "formdata.shared.services.process.replace.BaseFormData$Name");
    assertAnnotation(nameEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of NameEx
    assertEquals(1, nameEx.fields().stream().count(), "field count of 'NameEx'");
    var serialVersionUID4 = assertFieldExist(nameEx, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(0, nameEx.methods().stream().count(), "method count of 'NameEx'");

    assertEquals(0, nameEx.innerTypes().stream().count(), "inner types count of 'NameEx'");
    // type SdkCommandCreateCreate
    var sdkCommandCreateCreate = assertTypeExists(extendedFormData, "SdkCommandCreateCreate");
    assertHasFlags(sdkCommandCreateCreate, 9);
    assertHasSuperClass(sdkCommandCreateCreate, "formdata.shared.services.process.replace.BaseFormData$SdkCommandCreate");
    assertAnnotation(sdkCommandCreateCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateCreate
    assertEquals(1, sdkCommandCreateCreate.fields().stream().count(), "field count of 'SdkCommandCreateCreate'");
    var serialVersionUID5 = assertFieldExist(sdkCommandCreateCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(0, sdkCommandCreateCreate.methods().stream().count(), "method count of 'SdkCommandCreateCreate'");

    assertEquals(0, sdkCommandCreateCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateCreate'");
    // type SdkCommandCreateIgnore
    var sdkCommandCreateIgnore = assertTypeExists(extendedFormData, "SdkCommandCreateIgnore");
    assertHasFlags(sdkCommandCreateIgnore, 9);
    assertHasSuperClass(sdkCommandCreateIgnore, "formdata.shared.services.process.replace.BaseFormData$SdkCommandCreate");
    assertAnnotation(sdkCommandCreateIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateIgnore
    assertEquals(1, sdkCommandCreateIgnore.fields().stream().count(), "field count of 'SdkCommandCreateIgnore'");
    var serialVersionUID6 = assertFieldExist(sdkCommandCreateIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");

    assertEquals(0, sdkCommandCreateIgnore.methods().stream().count(), "method count of 'SdkCommandCreateIgnore'");

    assertEquals(0, sdkCommandCreateIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateIgnore'");
    // type SdkCommandCreateNone
    var sdkCommandCreateNone = assertTypeExists(extendedFormData, "SdkCommandCreateNone");
    assertHasFlags(sdkCommandCreateNone, 9);
    assertHasSuperClass(sdkCommandCreateNone, "formdata.shared.services.process.replace.BaseFormData$SdkCommandCreate");
    assertAnnotation(sdkCommandCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateNone
    assertEquals(1, sdkCommandCreateNone.fields().stream().count(), "field count of 'SdkCommandCreateNone'");
    var serialVersionUID7 = assertFieldExist(sdkCommandCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID7, 26);
    assertFieldType(serialVersionUID7, "long");

    assertEquals(0, sdkCommandCreateNone.methods().stream().count(), "method count of 'SdkCommandCreateNone'");

    assertEquals(0, sdkCommandCreateNone.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateNone'");
    // type SdkCommandCreateUse
    var sdkCommandCreateUse = assertTypeExists(extendedFormData, "SdkCommandCreateUse");
    assertHasFlags(sdkCommandCreateUse, 9);
    assertHasSuperClass(sdkCommandCreateUse, "formdata.shared.services.process.replace.BaseFormData$SdkCommandCreate");
    assertAnnotation(sdkCommandCreateUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateUse
    assertEquals(1, sdkCommandCreateUse.fields().stream().count(), "field count of 'SdkCommandCreateUse'");
    var serialVersionUID8 = assertFieldExist(sdkCommandCreateUse, "serialVersionUID");
    assertHasFlags(serialVersionUID8, 26);
    assertFieldType(serialVersionUID8, "long");

    assertEquals(0, sdkCommandCreateUse.methods().stream().count(), "method count of 'SdkCommandCreateUse'");

    assertEquals(0, sdkCommandCreateUse.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateUse'");
    // type SdkCommandIgnoreCreate
    var sdkCommandIgnoreCreate = assertTypeExists(extendedFormData, "SdkCommandIgnoreCreate");
    assertHasFlags(sdkCommandIgnoreCreate, 9);
    assertHasSuperClass(sdkCommandIgnoreCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertAnnotation(sdkCommandIgnoreCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreCreate
    assertEquals(1, sdkCommandIgnoreCreate.fields().stream().count(), "field count of 'SdkCommandIgnoreCreate'");
    var serialVersionUID9 = assertFieldExist(sdkCommandIgnoreCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID9, 26);
    assertFieldType(serialVersionUID9, "long");

    assertEquals(0, sdkCommandIgnoreCreate.methods().stream().count(), "method count of 'SdkCommandIgnoreCreate'");

    assertEquals(0, sdkCommandIgnoreCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreCreate'");
    // type SdkCommandIgnoreUse
    var sdkCommandIgnoreUse = assertTypeExists(extendedFormData, "SdkCommandIgnoreUse");
    assertHasFlags(sdkCommandIgnoreUse, 9);
    assertHasSuperClass(sdkCommandIgnoreUse, "formdata.shared.services.process.replace.UsingFormFieldData");
    assertAnnotation(sdkCommandIgnoreUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreUse
    assertEquals(1, sdkCommandIgnoreUse.fields().stream().count(), "field count of 'SdkCommandIgnoreUse'");
    var serialVersionUID10 = assertFieldExist(sdkCommandIgnoreUse, "serialVersionUID");
    assertHasFlags(serialVersionUID10, 26);
    assertFieldType(serialVersionUID10, "long");

    assertEquals(0, sdkCommandIgnoreUse.methods().stream().count(), "method count of 'SdkCommandIgnoreUse'");

    assertEquals(0, sdkCommandIgnoreUse.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreUse'");
    // type SdkCommandNoneCreate
    var sdkCommandNoneCreate = assertTypeExists(extendedFormData, "SdkCommandNoneCreate");
    assertHasFlags(sdkCommandNoneCreate, 9);
    assertHasSuperClass(sdkCommandNoneCreate, "formdata.shared.services.process.replace.BaseFormData$SdkCommandNone");
    assertAnnotation(sdkCommandNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneCreate
    assertEquals(1, sdkCommandNoneCreate.fields().stream().count(), "field count of 'SdkCommandNoneCreate'");
    var serialVersionUID11 = assertFieldExist(sdkCommandNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID11, 26);
    assertFieldType(serialVersionUID11, "long");

    assertEquals(0, sdkCommandNoneCreate.methods().stream().count(), "method count of 'SdkCommandNoneCreate'");

    assertEquals(0, sdkCommandNoneCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneCreate'");
    // type SdkCommandNoneIgnore
    var sdkCommandNoneIgnore = assertTypeExists(extendedFormData, "SdkCommandNoneIgnore");
    assertHasFlags(sdkCommandNoneIgnore, 9);
    assertHasSuperClass(sdkCommandNoneIgnore, "formdata.shared.services.process.replace.BaseFormData$SdkCommandNone");
    assertAnnotation(sdkCommandNoneIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneIgnore
    assertEquals(1, sdkCommandNoneIgnore.fields().stream().count(), "field count of 'SdkCommandNoneIgnore'");
    var serialVersionUID12 = assertFieldExist(sdkCommandNoneIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID12, 26);
    assertFieldType(serialVersionUID12, "long");

    assertEquals(0, sdkCommandNoneIgnore.methods().stream().count(), "method count of 'SdkCommandNoneIgnore'");

    assertEquals(0, sdkCommandNoneIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneIgnore'");
    // type SdkCommandNoneNone
    var sdkCommandNoneNone = assertTypeExists(extendedFormData, "SdkCommandNoneNone");
    assertHasFlags(sdkCommandNoneNone, 9);
    assertHasSuperClass(sdkCommandNoneNone, "formdata.shared.services.process.replace.BaseFormData$SdkCommandNone");
    assertAnnotation(sdkCommandNoneNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneNone
    assertEquals(1, sdkCommandNoneNone.fields().stream().count(), "field count of 'SdkCommandNoneNone'");
    var serialVersionUID13 = assertFieldExist(sdkCommandNoneNone, "serialVersionUID");
    assertHasFlags(serialVersionUID13, 26);
    assertFieldType(serialVersionUID13, "long");

    assertEquals(0, sdkCommandNoneNone.methods().stream().count(), "method count of 'SdkCommandNoneNone'");

    assertEquals(0, sdkCommandNoneNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneNone'");
    // type SdkCommandNoneUse
    var sdkCommandNoneUse = assertTypeExists(extendedFormData, "SdkCommandNoneUse");
    assertHasFlags(sdkCommandNoneUse, 9);
    assertHasSuperClass(sdkCommandNoneUse, "formdata.shared.services.process.replace.BaseFormData$SdkCommandNone");
    assertAnnotation(sdkCommandNoneUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneUse
    assertEquals(1, sdkCommandNoneUse.fields().stream().count(), "field count of 'SdkCommandNoneUse'");
    var serialVersionUID14 = assertFieldExist(sdkCommandNoneUse, "serialVersionUID");
    assertHasFlags(serialVersionUID14, 26);
    assertFieldType(serialVersionUID14, "long");

    assertEquals(0, sdkCommandNoneUse.methods().stream().count(), "method count of 'SdkCommandNoneUse'");

    assertEquals(0, sdkCommandNoneUse.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneUse'");
    // type SdkCommandUseCreate
    var sdkCommandUseCreate = assertTypeExists(extendedFormData, "SdkCommandUseCreate");
    assertHasFlags(sdkCommandUseCreate, 9);
    assertHasSuperClass(sdkCommandUseCreate, "formdata.shared.services.process.replace.BaseFormData$SdkCommandUse");
    assertAnnotation(sdkCommandUseCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseCreate
    assertEquals(1, sdkCommandUseCreate.fields().stream().count(), "field count of 'SdkCommandUseCreate'");
    var serialVersionUID15 = assertFieldExist(sdkCommandUseCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID15, 26);
    assertFieldType(serialVersionUID15, "long");

    assertEquals(0, sdkCommandUseCreate.methods().stream().count(), "method count of 'SdkCommandUseCreate'");

    assertEquals(0, sdkCommandUseCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandUseCreate'");
    // type SdkCommandUseIgnore
    var sdkCommandUseIgnore = assertTypeExists(extendedFormData, "SdkCommandUseIgnore");
    assertHasFlags(sdkCommandUseIgnore, 9);
    assertHasSuperClass(sdkCommandUseIgnore, "formdata.shared.services.process.replace.BaseFormData$SdkCommandUse");
    assertAnnotation(sdkCommandUseIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseIgnore
    assertEquals(1, sdkCommandUseIgnore.fields().stream().count(), "field count of 'SdkCommandUseIgnore'");
    var serialVersionUID16 = assertFieldExist(sdkCommandUseIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID16, 26);
    assertFieldType(serialVersionUID16, "long");

    assertEquals(0, sdkCommandUseIgnore.methods().stream().count(), "method count of 'SdkCommandUseIgnore'");

    assertEquals(0, sdkCommandUseIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandUseIgnore'");
    // type SdkCommandUseNone
    var sdkCommandUseNone = assertTypeExists(extendedFormData, "SdkCommandUseNone");
    assertHasFlags(sdkCommandUseNone, 9);
    assertHasSuperClass(sdkCommandUseNone, "formdata.shared.services.process.replace.BaseFormData$SdkCommandUse");
    assertAnnotation(sdkCommandUseNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseNone
    assertEquals(1, sdkCommandUseNone.fields().stream().count(), "field count of 'SdkCommandUseNone'");
    var serialVersionUID17 = assertFieldExist(sdkCommandUseNone, "serialVersionUID");
    assertHasFlags(serialVersionUID17, 26);
    assertFieldType(serialVersionUID17, "long");

    assertEquals(0, sdkCommandUseNone.methods().stream().count(), "method count of 'SdkCommandUseNone'");

    assertEquals(0, sdkCommandUseNone.innerTypes().stream().count(), "inner types count of 'SdkCommandUseNone'");
    // type SdkCommandUseUse
    var sdkCommandUseUse = assertTypeExists(extendedFormData, "SdkCommandUseUse");
    assertHasFlags(sdkCommandUseUse, 9);
    assertHasSuperClass(sdkCommandUseUse, "formdata.shared.services.process.replace.BaseFormData$SdkCommandUse");
    assertAnnotation(sdkCommandUseUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseUse
    assertEquals(1, sdkCommandUseUse.fields().stream().count(), "field count of 'SdkCommandUseUse'");
    var serialVersionUID18 = assertFieldExist(sdkCommandUseUse, "serialVersionUID");
    assertHasFlags(serialVersionUID18, 26);
    assertFieldType(serialVersionUID18, "long");

    assertEquals(0, sdkCommandUseUse.methods().stream().count(), "method count of 'SdkCommandUseUse'");

    assertEquals(0, sdkCommandUseUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUseUse'");
    // type SmartEx
    var smartEx = assertTypeExists(extendedFormData, "SmartEx");
    assertHasFlags(smartEx, 9);
    assertHasSuperClass(smartEx, "formdata.shared.services.process.replace.BaseFormData$Smart");
    assertAnnotation(smartEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of SmartEx
    assertEquals(1, smartEx.fields().stream().count(), "field count of 'SmartEx'");
    var serialVersionUID19 = assertFieldExist(smartEx, "serialVersionUID");
    assertHasFlags(serialVersionUID19, 26);
    assertFieldType(serialVersionUID19, "long");

    assertEquals(0, smartEx.methods().stream().count(), "method count of 'SmartEx'");

    assertEquals(0, smartEx.innerTypes().stream().count(), "inner types count of 'SmartEx'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfExtendedExtendedFormData(IType extendedExtendedFormData) {
    var scoutApi = extendedExtendedFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(extendedExtendedFormData, Flags.AccPublic);
    assertHasSuperClass(extendedExtendedFormData, "formdata.shared.services.process.replace.ExtendedFormData");
    assertEquals(1, extendedExtendedFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(extendedExtendedFormData, scoutApi.Generated());

    // fields of ExtendedExtendedFormData
    assertEquals(1, extendedExtendedFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData'");
    var serialVersionUID = assertFieldExist(extendedExtendedFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(63, extendedExtendedFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData'");
    var getIgnoringGroupBoxExCreateNone = assertMethodExist(extendedExtendedFormData, "getIgnoringGroupBoxExCreateNone");
    assertMethodReturnType(getIgnoringGroupBoxExCreateNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$IgnoringGroupBoxExCreateNone");
    assertEquals(0, getIgnoringGroupBoxExCreateNone.annotations().stream().count(), "annotation count");
    var getIgnoringGroupBoxExNoneCreate = assertMethodExist(extendedExtendedFormData, "getIgnoringGroupBoxExNoneCreate");
    assertMethodReturnType(getIgnoringGroupBoxExNoneCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$IgnoringGroupBoxExNoneCreate");
    assertEquals(0, getIgnoringGroupBoxExNoneCreate.annotations().stream().count(), "annotation count");
    var getNameExEx = assertMethodExist(extendedExtendedFormData, "getNameExEx");
    assertMethodReturnType(getNameExEx, "formdata.shared.services.process.replace.ExtendedExtendedFormData$NameExEx");
    assertEquals(0, getNameExEx.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateCreateCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateCreate");
    assertMethodReturnType(getSdkCommandCreateCreateCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateCreate");
    assertEquals(0, getSdkCommandCreateCreateCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateCreateIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateIgnore");
    assertMethodReturnType(getSdkCommandCreateCreateIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateIgnore");
    assertEquals(0, getSdkCommandCreateCreateIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateCreateNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateNone");
    assertMethodReturnType(getSdkCommandCreateCreateNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateNone");
    assertEquals(0, getSdkCommandCreateCreateNone.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateCreateUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateUse");
    assertMethodReturnType(getSdkCommandCreateCreateUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateUse");
    assertEquals(0, getSdkCommandCreateCreateUse.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateIgnoreCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreCreate");
    assertMethodReturnType(getSdkCommandCreateIgnoreCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreCreate");
    assertEquals(0, getSdkCommandCreateIgnoreCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateIgnoreIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreIgnore");
    assertMethodReturnType(getSdkCommandCreateIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreIgnore");
    assertEquals(0, getSdkCommandCreateIgnoreIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateIgnoreNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreNone");
    assertMethodReturnType(getSdkCommandCreateIgnoreNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreNone");
    assertEquals(0, getSdkCommandCreateIgnoreNone.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateIgnoreUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreUse");
    assertMethodReturnType(getSdkCommandCreateIgnoreUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreUse");
    assertEquals(0, getSdkCommandCreateIgnoreUse.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateNoneCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneCreate");
    assertMethodReturnType(getSdkCommandCreateNoneCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneCreate");
    assertEquals(0, getSdkCommandCreateNoneCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateNoneIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneIgnore");
    assertMethodReturnType(getSdkCommandCreateNoneIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneIgnore");
    assertEquals(0, getSdkCommandCreateNoneIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateNoneNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneNone");
    assertMethodReturnType(getSdkCommandCreateNoneNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneNone");
    assertEquals(0, getSdkCommandCreateNoneNone.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateNoneUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneUse");
    assertMethodReturnType(getSdkCommandCreateNoneUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneUse");
    assertEquals(0, getSdkCommandCreateNoneUse.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateUseCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseCreate");
    assertMethodReturnType(getSdkCommandCreateUseCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseCreate");
    assertEquals(0, getSdkCommandCreateUseCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateUseIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseIgnore");
    assertMethodReturnType(getSdkCommandCreateUseIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseIgnore");
    assertEquals(0, getSdkCommandCreateUseIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateUseNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseNone");
    assertMethodReturnType(getSdkCommandCreateUseNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseNone");
    assertEquals(0, getSdkCommandCreateUseNone.annotations().stream().count(), "annotation count");
    var getSdkCommandCreateUseUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseUse");
    assertMethodReturnType(getSdkCommandCreateUseUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseUse");
    assertEquals(0, getSdkCommandCreateUseUse.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreCreateCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateCreate");
    assertMethodReturnType(getSdkCommandIgnoreCreateCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateCreate");
    assertEquals(0, getSdkCommandIgnoreCreateCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreCreateIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateIgnore");
    assertMethodReturnType(getSdkCommandIgnoreCreateIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateIgnore");
    assertEquals(0, getSdkCommandIgnoreCreateIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreCreateNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateNone");
    assertMethodReturnType(getSdkCommandIgnoreCreateNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateNone");
    assertEquals(0, getSdkCommandIgnoreCreateNone.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreCreateUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateUse");
    assertMethodReturnType(getSdkCommandIgnoreCreateUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateUse");
    assertEquals(0, getSdkCommandIgnoreCreateUse.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreIgnoreCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreIgnoreCreate");
    assertMethodReturnType(getSdkCommandIgnoreIgnoreCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreIgnoreCreate");
    assertEquals(0, getSdkCommandIgnoreIgnoreCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreIgnoreUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreIgnoreUse");
    assertMethodReturnType(getSdkCommandIgnoreIgnoreUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreIgnoreUse");
    assertEquals(0, getSdkCommandIgnoreIgnoreUse.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreNoneCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreNoneCreate");
    assertMethodReturnType(getSdkCommandIgnoreNoneCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreNoneCreate");
    assertEquals(0, getSdkCommandIgnoreNoneCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreNoneUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreNoneUse");
    assertMethodReturnType(getSdkCommandIgnoreNoneUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreNoneUse");
    assertEquals(0, getSdkCommandIgnoreNoneUse.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreUseCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseCreate");
    assertMethodReturnType(getSdkCommandIgnoreUseCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseCreate");
    assertEquals(0, getSdkCommandIgnoreUseCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreUseIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseIgnore");
    assertMethodReturnType(getSdkCommandIgnoreUseIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseIgnore");
    assertEquals(0, getSdkCommandIgnoreUseIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreUseNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseNone");
    assertMethodReturnType(getSdkCommandIgnoreUseNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseNone");
    assertEquals(0, getSdkCommandIgnoreUseNone.annotations().stream().count(), "annotation count");
    var getSdkCommandIgnoreUseUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseUse");
    assertMethodReturnType(getSdkCommandIgnoreUseUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseUse");
    assertEquals(0, getSdkCommandIgnoreUseUse.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneCreateCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateCreate");
    assertMethodReturnType(getSdkCommandNoneCreateCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateCreate");
    assertEquals(0, getSdkCommandNoneCreateCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneCreateIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateIgnore");
    assertMethodReturnType(getSdkCommandNoneCreateIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateIgnore");
    assertEquals(0, getSdkCommandNoneCreateIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneCreateNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateNone");
    assertMethodReturnType(getSdkCommandNoneCreateNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateNone");
    assertEquals(0, getSdkCommandNoneCreateNone.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneCreateUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateUse");
    assertMethodReturnType(getSdkCommandNoneCreateUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateUse");
    assertEquals(0, getSdkCommandNoneCreateUse.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneIgnoreCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreCreate");
    assertMethodReturnType(getSdkCommandNoneIgnoreCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreCreate");
    assertEquals(0, getSdkCommandNoneIgnoreCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneIgnoreIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreIgnore");
    assertMethodReturnType(getSdkCommandNoneIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreIgnore");
    assertEquals(0, getSdkCommandNoneIgnoreIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneIgnoreNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreNone");
    assertMethodReturnType(getSdkCommandNoneIgnoreNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreNone");
    assertEquals(0, getSdkCommandNoneIgnoreNone.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneIgnoreUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreUse");
    assertMethodReturnType(getSdkCommandNoneIgnoreUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreUse");
    assertEquals(0, getSdkCommandNoneIgnoreUse.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneNoneCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneCreate");
    assertMethodReturnType(getSdkCommandNoneNoneCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneCreate");
    assertEquals(0, getSdkCommandNoneNoneCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneNoneIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneIgnore");
    assertMethodReturnType(getSdkCommandNoneNoneIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneIgnore");
    assertEquals(0, getSdkCommandNoneNoneIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneNoneNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneNone");
    assertMethodReturnType(getSdkCommandNoneNoneNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneNone");
    assertEquals(0, getSdkCommandNoneNoneNone.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneNoneUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneUse");
    assertMethodReturnType(getSdkCommandNoneNoneUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneUse");
    assertEquals(0, getSdkCommandNoneNoneUse.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneUseCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseCreate");
    assertMethodReturnType(getSdkCommandNoneUseCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseCreate");
    assertEquals(0, getSdkCommandNoneUseCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneUseIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseIgnore");
    assertMethodReturnType(getSdkCommandNoneUseIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseIgnore");
    assertEquals(0, getSdkCommandNoneUseIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneUseNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseNone");
    assertMethodReturnType(getSdkCommandNoneUseNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseNone");
    assertEquals(0, getSdkCommandNoneUseNone.annotations().stream().count(), "annotation count");
    var getSdkCommandNoneUseUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseUse");
    assertMethodReturnType(getSdkCommandNoneUseUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseUse");
    assertEquals(0, getSdkCommandNoneUseUse.annotations().stream().count(), "annotation count");
    var getSdkCommandUseCreateCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateCreate");
    assertMethodReturnType(getSdkCommandUseCreateCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateCreate");
    assertEquals(0, getSdkCommandUseCreateCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandUseCreateIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateIgnore");
    assertMethodReturnType(getSdkCommandUseCreateIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateIgnore");
    assertEquals(0, getSdkCommandUseCreateIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandUseCreateNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateNone");
    assertMethodReturnType(getSdkCommandUseCreateNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateNone");
    assertEquals(0, getSdkCommandUseCreateNone.annotations().stream().count(), "annotation count");
    var getSdkCommandUseCreateUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateUse");
    assertMethodReturnType(getSdkCommandUseCreateUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateUse");
    assertEquals(0, getSdkCommandUseCreateUse.annotations().stream().count(), "annotation count");
    var getSdkCommandUseIgnoreCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreCreate");
    assertMethodReturnType(getSdkCommandUseIgnoreCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreCreate");
    assertEquals(0, getSdkCommandUseIgnoreCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandUseIgnoreIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreIgnore");
    assertMethodReturnType(getSdkCommandUseIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreIgnore");
    assertEquals(0, getSdkCommandUseIgnoreIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandUseIgnoreNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreNone");
    assertMethodReturnType(getSdkCommandUseIgnoreNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreNone");
    assertEquals(0, getSdkCommandUseIgnoreNone.annotations().stream().count(), "annotation count");
    var getSdkCommandUseIgnoreUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreUse");
    assertMethodReturnType(getSdkCommandUseIgnoreUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreUse");
    assertEquals(0, getSdkCommandUseIgnoreUse.annotations().stream().count(), "annotation count");
    var getSdkCommandUseNoneCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneCreate");
    assertMethodReturnType(getSdkCommandUseNoneCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneCreate");
    assertEquals(0, getSdkCommandUseNoneCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandUseNoneIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneIgnore");
    assertMethodReturnType(getSdkCommandUseNoneIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneIgnore");
    assertEquals(0, getSdkCommandUseNoneIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandUseNoneNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneNone");
    assertMethodReturnType(getSdkCommandUseNoneNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneNone");
    assertEquals(0, getSdkCommandUseNoneNone.annotations().stream().count(), "annotation count");
    var getSdkCommandUseNoneUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneUse");
    assertMethodReturnType(getSdkCommandUseNoneUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneUse");
    assertEquals(0, getSdkCommandUseNoneUse.annotations().stream().count(), "annotation count");
    var getSdkCommandUseUseCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseCreate");
    assertMethodReturnType(getSdkCommandUseUseCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseCreate");
    assertEquals(0, getSdkCommandUseUseCreate.annotations().stream().count(), "annotation count");
    var getSdkCommandUseUseIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseIgnore");
    assertMethodReturnType(getSdkCommandUseUseIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseIgnore");
    assertEquals(0, getSdkCommandUseUseIgnore.annotations().stream().count(), "annotation count");
    var getSdkCommandUseUseNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseNone");
    assertMethodReturnType(getSdkCommandUseUseNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseNone");
    assertEquals(0, getSdkCommandUseUseNone.annotations().stream().count(), "annotation count");
    var getSdkCommandUseUseUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseUse");
    assertMethodReturnType(getSdkCommandUseUseUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseUse");
    assertEquals(0, getSdkCommandUseUseUse.annotations().stream().count(), "annotation count");

    assertEquals(63, extendedExtendedFormData.innerTypes().stream().count(), "inner types count of 'ExtendedExtendedFormData'");
    // type IgnoringGroupBoxExCreateNone
    var ignoringGroupBoxExCreateNone = assertTypeExists(extendedExtendedFormData, "IgnoringGroupBoxExCreateNone");
    assertHasFlags(ignoringGroupBoxExCreateNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(ignoringGroupBoxExCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$IgnoringGroupBoxExCreate");
    assertEquals(1, ignoringGroupBoxExCreateNone.annotations().stream().count(), "annotation count");
    assertAnnotation(ignoringGroupBoxExCreateNone, scoutApi.Replace());

    // fields of IgnoringGroupBoxExCreateNone
    assertEquals(1, ignoringGroupBoxExCreateNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$IgnoringGroupBoxExCreateNone'");
    var serialVersionUID1 = assertFieldExist(ignoringGroupBoxExCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, ignoringGroupBoxExCreateNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$IgnoringGroupBoxExCreateNone'");

    assertEquals(0, ignoringGroupBoxExCreateNone.innerTypes().stream().count(), "inner types count of 'IgnoringGroupBoxExCreateNone'");
    // type IgnoringGroupBoxExNoneCreate
    var ignoringGroupBoxExNoneCreate = assertTypeExists(extendedExtendedFormData, "IgnoringGroupBoxExNoneCreate");
    assertHasFlags(ignoringGroupBoxExNoneCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(ignoringGroupBoxExNoneCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(1, ignoringGroupBoxExNoneCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(ignoringGroupBoxExNoneCreate, scoutApi.Replace());

    // fields of IgnoringGroupBoxExNoneCreate
    assertEquals(1, ignoringGroupBoxExNoneCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$IgnoringGroupBoxExNoneCreate'");
    var serialVersionUID2 = assertFieldExist(ignoringGroupBoxExNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, ignoringGroupBoxExNoneCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$IgnoringGroupBoxExNoneCreate'");

    assertEquals(0, ignoringGroupBoxExNoneCreate.innerTypes().stream().count(), "inner types count of 'IgnoringGroupBoxExNoneCreate'");
    // type NameExEx
    var nameExEx = assertTypeExists(extendedExtendedFormData, "NameExEx");
    assertHasFlags(nameExEx, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(nameExEx, "formdata.shared.services.process.replace.ExtendedFormData$NameEx");
    assertEquals(1, nameExEx.annotations().stream().count(), "annotation count");
    assertAnnotation(nameExEx, scoutApi.Replace());

    // fields of NameExEx
    assertEquals(1, nameExEx.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$NameExEx'");
    var serialVersionUID3 = assertFieldExist(nameExEx, "serialVersionUID");
    assertHasFlags(serialVersionUID3, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID3, "long");
    assertEquals(0, serialVersionUID3.annotations().stream().count(), "annotation count");

    assertEquals(3, nameExEx.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$NameExEx'");
    var getStringProperty = assertMethodExist(nameExEx, "getStringProperty");
    assertMethodReturnType(getStringProperty, "java.lang.String");
    assertEquals(0, getStringProperty.annotations().stream().count(), "annotation count");
    var setStringProperty = assertMethodExist(nameExEx, "setStringProperty", new String[]{"java.lang.String"});
    assertMethodReturnType(setStringProperty, "void");
    assertEquals(0, setStringProperty.annotations().stream().count(), "annotation count");
    var getStringPropertyProperty = assertMethodExist(nameExEx, "getStringPropertyProperty");
    assertMethodReturnType(getStringPropertyProperty, "formdata.shared.services.process.replace.ExtendedExtendedFormData$NameExEx$StringPropertyProperty");
    assertEquals(0, getStringPropertyProperty.annotations().stream().count(), "annotation count");

    assertEquals(1, nameExEx.innerTypes().stream().count(), "inner types count of 'NameExEx'");
    // type StringPropertyProperty
    var stringPropertyProperty = assertTypeExists(nameExEx, "StringPropertyProperty");
    assertHasFlags(stringPropertyProperty, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(stringPropertyProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.String>");
    assertEquals(0, stringPropertyProperty.annotations().stream().count(), "annotation count");

    // fields of StringPropertyProperty
    assertEquals(1, stringPropertyProperty.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$NameExEx$StringPropertyProperty'");
    var serialVersionUID4 = assertFieldExist(stringPropertyProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID4, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID4, "long");
    assertEquals(0, serialVersionUID4.annotations().stream().count(), "annotation count");

    assertEquals(0, stringPropertyProperty.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$NameExEx$StringPropertyProperty'");

    assertEquals(0, stringPropertyProperty.innerTypes().stream().count(), "inner types count of 'StringPropertyProperty'");
    // type SdkCommandCreateCreateCreate
    var sdkCommandCreateCreateCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateCreate");
    assertHasFlags(sdkCommandCreateCreateCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateCreateCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateCreate");
    assertEquals(1, sdkCommandCreateCreateCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateCreateCreate, scoutApi.Replace());

    // fields of SdkCommandCreateCreateCreate
    assertEquals(1, sdkCommandCreateCreateCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateCreate'");
    var serialVersionUID5 = assertFieldExist(sdkCommandCreateCreateCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID5, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID5, "long");
    assertEquals(0, serialVersionUID5.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateCreateCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateCreate'");

    assertEquals(0, sdkCommandCreateCreateCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateCreateCreate'");
    // type SdkCommandCreateCreateIgnore
    var sdkCommandCreateCreateIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateIgnore");
    assertHasFlags(sdkCommandCreateCreateIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateCreateIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateCreate");
    assertEquals(1, sdkCommandCreateCreateIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateCreateIgnore, scoutApi.Replace());

    // fields of SdkCommandCreateCreateIgnore
    assertEquals(1, sdkCommandCreateCreateIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateIgnore'");
    var serialVersionUID6 = assertFieldExist(sdkCommandCreateCreateIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID6, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID6, "long");
    assertEquals(0, serialVersionUID6.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateCreateIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateIgnore'");

    assertEquals(0, sdkCommandCreateCreateIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateCreateIgnore'");
    // type SdkCommandCreateCreateNone
    var sdkCommandCreateCreateNone = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateNone");
    assertHasFlags(sdkCommandCreateCreateNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateCreate");
    assertEquals(1, sdkCommandCreateCreateNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateCreateNone, scoutApi.Replace());

    // fields of SdkCommandCreateCreateNone
    assertEquals(1, sdkCommandCreateCreateNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateNone'");
    var serialVersionUID7 = assertFieldExist(sdkCommandCreateCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID7, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID7, "long");
    assertEquals(0, serialVersionUID7.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateCreateNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateNone'");

    assertEquals(0, sdkCommandCreateCreateNone.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateCreateNone'");
    // type SdkCommandCreateCreateUse
    var sdkCommandCreateCreateUse = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateUse");
    assertHasFlags(sdkCommandCreateCreateUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateCreateUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateCreate");
    assertEquals(1, sdkCommandCreateCreateUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateCreateUse, scoutApi.Replace());

    // fields of SdkCommandCreateCreateUse
    assertEquals(1, sdkCommandCreateCreateUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateUse'");
    var serialVersionUID8 = assertFieldExist(sdkCommandCreateCreateUse, "serialVersionUID");
    assertHasFlags(serialVersionUID8, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID8, "long");
    assertEquals(0, serialVersionUID8.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateCreateUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateUse'");

    assertEquals(0, sdkCommandCreateCreateUse.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateCreateUse'");
    // type SdkCommandCreateIgnoreCreate
    var sdkCommandCreateIgnoreCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreCreate");
    assertHasFlags(sdkCommandCreateIgnoreCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateIgnoreCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateIgnore");
    assertEquals(1, sdkCommandCreateIgnoreCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateIgnoreCreate, scoutApi.Replace());

    // fields of SdkCommandCreateIgnoreCreate
    assertEquals(1, sdkCommandCreateIgnoreCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreCreate'");
    var serialVersionUID9 = assertFieldExist(sdkCommandCreateIgnoreCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID9, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID9, "long");
    assertEquals(0, serialVersionUID9.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateIgnoreCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreCreate'");

    assertEquals(0, sdkCommandCreateIgnoreCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateIgnoreCreate'");
    // type SdkCommandCreateIgnoreIgnore
    var sdkCommandCreateIgnoreIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreIgnore");
    assertHasFlags(sdkCommandCreateIgnoreIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateIgnore");
    assertEquals(1, sdkCommandCreateIgnoreIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateIgnoreIgnore, scoutApi.Replace());

    // fields of SdkCommandCreateIgnoreIgnore
    assertEquals(1, sdkCommandCreateIgnoreIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreIgnore'");
    var serialVersionUID10 = assertFieldExist(sdkCommandCreateIgnoreIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID10, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID10, "long");
    assertEquals(0, serialVersionUID10.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateIgnoreIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreIgnore'");

    assertEquals(0, sdkCommandCreateIgnoreIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateIgnoreIgnore'");
    // type SdkCommandCreateIgnoreNone
    var sdkCommandCreateIgnoreNone = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreNone");
    assertHasFlags(sdkCommandCreateIgnoreNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateIgnoreNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateIgnore");
    assertEquals(1, sdkCommandCreateIgnoreNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateIgnoreNone, scoutApi.Replace());

    // fields of SdkCommandCreateIgnoreNone
    assertEquals(1, sdkCommandCreateIgnoreNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreNone'");
    var serialVersionUID11 = assertFieldExist(sdkCommandCreateIgnoreNone, "serialVersionUID");
    assertHasFlags(serialVersionUID11, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID11, "long");
    assertEquals(0, serialVersionUID11.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateIgnoreNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreNone'");

    assertEquals(0, sdkCommandCreateIgnoreNone.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateIgnoreNone'");
    // type SdkCommandCreateIgnoreUse
    var sdkCommandCreateIgnoreUse = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreUse");
    assertHasFlags(sdkCommandCreateIgnoreUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateIgnoreUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateIgnore");
    assertEquals(1, sdkCommandCreateIgnoreUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateIgnoreUse, scoutApi.Replace());

    // fields of SdkCommandCreateIgnoreUse
    assertEquals(1, sdkCommandCreateIgnoreUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreUse'");
    var serialVersionUID12 = assertFieldExist(sdkCommandCreateIgnoreUse, "serialVersionUID");
    assertHasFlags(serialVersionUID12, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID12, "long");
    assertEquals(0, serialVersionUID12.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateIgnoreUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreUse'");

    assertEquals(0, sdkCommandCreateIgnoreUse.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateIgnoreUse'");
    // type SdkCommandCreateNoneCreate
    var sdkCommandCreateNoneCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneCreate");
    assertHasFlags(sdkCommandCreateNoneCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateNoneCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateNone");
    assertEquals(1, sdkCommandCreateNoneCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateNoneCreate, scoutApi.Replace());

    // fields of SdkCommandCreateNoneCreate
    assertEquals(1, sdkCommandCreateNoneCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneCreate'");
    var serialVersionUID13 = assertFieldExist(sdkCommandCreateNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID13, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID13, "long");
    assertEquals(0, serialVersionUID13.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateNoneCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneCreate'");

    assertEquals(0, sdkCommandCreateNoneCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateNoneCreate'");
    // type SdkCommandCreateNoneIgnore
    var sdkCommandCreateNoneIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneIgnore");
    assertHasFlags(sdkCommandCreateNoneIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateNoneIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateNone");
    assertEquals(1, sdkCommandCreateNoneIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateNoneIgnore, scoutApi.Replace());

    // fields of SdkCommandCreateNoneIgnore
    assertEquals(1, sdkCommandCreateNoneIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneIgnore'");
    var serialVersionUID14 = assertFieldExist(sdkCommandCreateNoneIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID14, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID14, "long");
    assertEquals(0, serialVersionUID14.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateNoneIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneIgnore'");

    assertEquals(0, sdkCommandCreateNoneIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateNoneIgnore'");
    // type SdkCommandCreateNoneNone
    var sdkCommandCreateNoneNone = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneNone");
    assertHasFlags(sdkCommandCreateNoneNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateNoneNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateNone");
    assertEquals(1, sdkCommandCreateNoneNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateNoneNone, scoutApi.Replace());

    // fields of SdkCommandCreateNoneNone
    assertEquals(1, sdkCommandCreateNoneNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneNone'");
    var serialVersionUID15 = assertFieldExist(sdkCommandCreateNoneNone, "serialVersionUID");
    assertHasFlags(serialVersionUID15, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID15, "long");
    assertEquals(0, serialVersionUID15.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateNoneNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneNone'");

    assertEquals(0, sdkCommandCreateNoneNone.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateNoneNone'");
    // type SdkCommandCreateNoneUse
    var sdkCommandCreateNoneUse = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneUse");
    assertHasFlags(sdkCommandCreateNoneUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateNoneUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateNone");
    assertEquals(1, sdkCommandCreateNoneUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateNoneUse, scoutApi.Replace());

    // fields of SdkCommandCreateNoneUse
    assertEquals(1, sdkCommandCreateNoneUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneUse'");
    var serialVersionUID16 = assertFieldExist(sdkCommandCreateNoneUse, "serialVersionUID");
    assertHasFlags(serialVersionUID16, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID16, "long");
    assertEquals(0, serialVersionUID16.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateNoneUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneUse'");

    assertEquals(0, sdkCommandCreateNoneUse.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateNoneUse'");
    // type SdkCommandCreateUseCreate
    var sdkCommandCreateUseCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseCreate");
    assertHasFlags(sdkCommandCreateUseCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateUseCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateUse");
    assertEquals(1, sdkCommandCreateUseCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateUseCreate, scoutApi.Replace());

    // fields of SdkCommandCreateUseCreate
    assertEquals(1, sdkCommandCreateUseCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseCreate'");
    var serialVersionUID17 = assertFieldExist(sdkCommandCreateUseCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID17, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID17, "long");
    assertEquals(0, serialVersionUID17.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateUseCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseCreate'");

    assertEquals(0, sdkCommandCreateUseCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateUseCreate'");
    // type SdkCommandCreateUseIgnore
    var sdkCommandCreateUseIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseIgnore");
    assertHasFlags(sdkCommandCreateUseIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateUseIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateUse");
    assertEquals(1, sdkCommandCreateUseIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateUseIgnore, scoutApi.Replace());

    // fields of SdkCommandCreateUseIgnore
    assertEquals(1, sdkCommandCreateUseIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseIgnore'");
    var serialVersionUID18 = assertFieldExist(sdkCommandCreateUseIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID18, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID18, "long");
    assertEquals(0, serialVersionUID18.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateUseIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseIgnore'");

    assertEquals(0, sdkCommandCreateUseIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateUseIgnore'");
    // type SdkCommandCreateUseNone
    var sdkCommandCreateUseNone = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseNone");
    assertHasFlags(sdkCommandCreateUseNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateUseNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateUse");
    assertEquals(1, sdkCommandCreateUseNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateUseNone, scoutApi.Replace());

    // fields of SdkCommandCreateUseNone
    assertEquals(1, sdkCommandCreateUseNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseNone'");
    var serialVersionUID19 = assertFieldExist(sdkCommandCreateUseNone, "serialVersionUID");
    assertHasFlags(serialVersionUID19, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID19, "long");
    assertEquals(0, serialVersionUID19.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateUseNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseNone'");

    assertEquals(0, sdkCommandCreateUseNone.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateUseNone'");
    // type SdkCommandCreateUseUse
    var sdkCommandCreateUseUse = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseUse");
    assertHasFlags(sdkCommandCreateUseUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandCreateUseUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateUse");
    assertEquals(1, sdkCommandCreateUseUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandCreateUseUse, scoutApi.Replace());

    // fields of SdkCommandCreateUseUse
    assertEquals(1, sdkCommandCreateUseUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseUse'");
    var serialVersionUID20 = assertFieldExist(sdkCommandCreateUseUse, "serialVersionUID");
    assertHasFlags(serialVersionUID20, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID20, "long");
    assertEquals(0, serialVersionUID20.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandCreateUseUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseUse'");

    assertEquals(0, sdkCommandCreateUseUse.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateUseUse'");
    // type SdkCommandIgnoreCreateCreate
    var sdkCommandIgnoreCreateCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateCreate");
    assertHasFlags(sdkCommandIgnoreCreateCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreCreateCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreCreate");
    assertEquals(1, sdkCommandIgnoreCreateCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreCreateCreate, scoutApi.Replace());

    // fields of SdkCommandIgnoreCreateCreate
    assertEquals(1, sdkCommandIgnoreCreateCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateCreate'");
    var serialVersionUID21 = assertFieldExist(sdkCommandIgnoreCreateCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID21, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID21, "long");
    assertEquals(0, serialVersionUID21.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreCreateCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateCreate'");

    assertEquals(0, sdkCommandIgnoreCreateCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreCreateCreate'");
    // type SdkCommandIgnoreCreateIgnore
    var sdkCommandIgnoreCreateIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateIgnore");
    assertHasFlags(sdkCommandIgnoreCreateIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreCreateIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreCreate");
    assertEquals(1, sdkCommandIgnoreCreateIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreCreateIgnore, scoutApi.Replace());

    // fields of SdkCommandIgnoreCreateIgnore
    assertEquals(1, sdkCommandIgnoreCreateIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateIgnore'");
    var serialVersionUID22 = assertFieldExist(sdkCommandIgnoreCreateIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID22, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID22, "long");
    assertEquals(0, serialVersionUID22.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreCreateIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateIgnore'");

    assertEquals(0, sdkCommandIgnoreCreateIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreCreateIgnore'");
    // type SdkCommandIgnoreCreateNone
    var sdkCommandIgnoreCreateNone = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateNone");
    assertHasFlags(sdkCommandIgnoreCreateNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreCreate");
    assertEquals(1, sdkCommandIgnoreCreateNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreCreateNone, scoutApi.Replace());

    // fields of SdkCommandIgnoreCreateNone
    assertEquals(1, sdkCommandIgnoreCreateNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateNone'");
    var serialVersionUID23 = assertFieldExist(sdkCommandIgnoreCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID23, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID23, "long");
    assertEquals(0, serialVersionUID23.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreCreateNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateNone'");

    assertEquals(0, sdkCommandIgnoreCreateNone.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreCreateNone'");
    // type SdkCommandIgnoreCreateUse
    var sdkCommandIgnoreCreateUse = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateUse");
    assertHasFlags(sdkCommandIgnoreCreateUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreCreateUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreCreate");
    assertEquals(1, sdkCommandIgnoreCreateUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreCreateUse, scoutApi.Replace());

    // fields of SdkCommandIgnoreCreateUse
    assertEquals(1, sdkCommandIgnoreCreateUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateUse'");
    var serialVersionUID24 = assertFieldExist(sdkCommandIgnoreCreateUse, "serialVersionUID");
    assertHasFlags(serialVersionUID24, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID24, "long");
    assertEquals(0, serialVersionUID24.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreCreateUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateUse'");

    assertEquals(0, sdkCommandIgnoreCreateUse.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreCreateUse'");
    // type SdkCommandIgnoreIgnoreCreate
    var sdkCommandIgnoreIgnoreCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreIgnoreCreate");
    assertHasFlags(sdkCommandIgnoreIgnoreCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreIgnoreCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(1, sdkCommandIgnoreIgnoreCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreIgnoreCreate, scoutApi.Replace());

    // fields of SdkCommandIgnoreIgnoreCreate
    assertEquals(1, sdkCommandIgnoreIgnoreCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreIgnoreCreate'");
    var serialVersionUID25 = assertFieldExist(sdkCommandIgnoreIgnoreCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID25, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID25, "long");
    assertEquals(0, serialVersionUID25.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreIgnoreCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreIgnoreCreate'");

    assertEquals(0, sdkCommandIgnoreIgnoreCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreIgnoreCreate'");
    // type SdkCommandIgnoreIgnoreUse
    var sdkCommandIgnoreIgnoreUse = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreIgnoreUse");
    assertHasFlags(sdkCommandIgnoreIgnoreUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreIgnoreUse, "formdata.shared.services.process.replace.UsingFormFieldData");
    assertEquals(1, sdkCommandIgnoreIgnoreUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreIgnoreUse, scoutApi.Replace());

    // fields of SdkCommandIgnoreIgnoreUse
    assertEquals(1, sdkCommandIgnoreIgnoreUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreIgnoreUse'");
    var serialVersionUID26 = assertFieldExist(sdkCommandIgnoreIgnoreUse, "serialVersionUID");
    assertHasFlags(serialVersionUID26, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID26, "long");
    assertEquals(0, serialVersionUID26.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreIgnoreUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreIgnoreUse'");

    assertEquals(0, sdkCommandIgnoreIgnoreUse.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreIgnoreUse'");
    // type SdkCommandIgnoreNoneCreate
    var sdkCommandIgnoreNoneCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreNoneCreate");
    assertHasFlags(sdkCommandIgnoreNoneCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreNoneCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(1, sdkCommandIgnoreNoneCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreNoneCreate, scoutApi.Replace());

    // fields of SdkCommandIgnoreNoneCreate
    assertEquals(1, sdkCommandIgnoreNoneCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreNoneCreate'");
    var serialVersionUID27 = assertFieldExist(sdkCommandIgnoreNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID27, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID27, "long");
    assertEquals(0, serialVersionUID27.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreNoneCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreNoneCreate'");

    assertEquals(0, sdkCommandIgnoreNoneCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreNoneCreate'");
    // type SdkCommandIgnoreNoneUse
    var sdkCommandIgnoreNoneUse = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreNoneUse");
    assertHasFlags(sdkCommandIgnoreNoneUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreNoneUse, "formdata.shared.services.process.replace.UsingFormFieldData");
    assertEquals(1, sdkCommandIgnoreNoneUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreNoneUse, scoutApi.Replace());

    // fields of SdkCommandIgnoreNoneUse
    assertEquals(1, sdkCommandIgnoreNoneUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreNoneUse'");
    var serialVersionUID28 = assertFieldExist(sdkCommandIgnoreNoneUse, "serialVersionUID");
    assertHasFlags(serialVersionUID28, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID28, "long");
    assertEquals(0, serialVersionUID28.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreNoneUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreNoneUse'");

    assertEquals(0, sdkCommandIgnoreNoneUse.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreNoneUse'");
    // type SdkCommandIgnoreUseCreate
    var sdkCommandIgnoreUseCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseCreate");
    assertHasFlags(sdkCommandIgnoreUseCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreUseCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreUse");
    assertEquals(1, sdkCommandIgnoreUseCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreUseCreate, scoutApi.Replace());

    // fields of SdkCommandIgnoreUseCreate
    assertEquals(1, sdkCommandIgnoreUseCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseCreate'");
    var serialVersionUID29 = assertFieldExist(sdkCommandIgnoreUseCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID29, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID29, "long");
    assertEquals(0, serialVersionUID29.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreUseCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseCreate'");

    assertEquals(0, sdkCommandIgnoreUseCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreUseCreate'");
    // type SdkCommandIgnoreUseIgnore
    var sdkCommandIgnoreUseIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseIgnore");
    assertHasFlags(sdkCommandIgnoreUseIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreUseIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreUse");
    assertEquals(1, sdkCommandIgnoreUseIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreUseIgnore, scoutApi.Replace());

    // fields of SdkCommandIgnoreUseIgnore
    assertEquals(1, sdkCommandIgnoreUseIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseIgnore'");
    var serialVersionUID30 = assertFieldExist(sdkCommandIgnoreUseIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID30, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID30, "long");
    assertEquals(0, serialVersionUID30.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreUseIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseIgnore'");

    assertEquals(0, sdkCommandIgnoreUseIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreUseIgnore'");
    // type SdkCommandIgnoreUseNone
    var sdkCommandIgnoreUseNone = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseNone");
    assertHasFlags(sdkCommandIgnoreUseNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreUseNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreUse");
    assertEquals(1, sdkCommandIgnoreUseNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreUseNone, scoutApi.Replace());

    // fields of SdkCommandIgnoreUseNone
    assertEquals(1, sdkCommandIgnoreUseNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseNone'");
    var serialVersionUID31 = assertFieldExist(sdkCommandIgnoreUseNone, "serialVersionUID");
    assertHasFlags(serialVersionUID31, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID31, "long");
    assertEquals(0, serialVersionUID31.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreUseNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseNone'");

    assertEquals(0, sdkCommandIgnoreUseNone.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreUseNone'");
    // type SdkCommandIgnoreUseUse
    var sdkCommandIgnoreUseUse = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseUse");
    assertHasFlags(sdkCommandIgnoreUseUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandIgnoreUseUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreUse");
    assertEquals(1, sdkCommandIgnoreUseUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandIgnoreUseUse, scoutApi.Replace());

    // fields of SdkCommandIgnoreUseUse
    assertEquals(1, sdkCommandIgnoreUseUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseUse'");
    var serialVersionUID32 = assertFieldExist(sdkCommandIgnoreUseUse, "serialVersionUID");
    assertHasFlags(serialVersionUID32, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID32, "long");
    assertEquals(0, serialVersionUID32.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandIgnoreUseUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseUse'");

    assertEquals(0, sdkCommandIgnoreUseUse.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreUseUse'");
    // type SdkCommandNoneCreateCreate
    var sdkCommandNoneCreateCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateCreate");
    assertHasFlags(sdkCommandNoneCreateCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneCreateCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneCreate");
    assertEquals(1, sdkCommandNoneCreateCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneCreateCreate, scoutApi.Replace());

    // fields of SdkCommandNoneCreateCreate
    assertEquals(1, sdkCommandNoneCreateCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateCreate'");
    var serialVersionUID33 = assertFieldExist(sdkCommandNoneCreateCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID33, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID33, "long");
    assertEquals(0, serialVersionUID33.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneCreateCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateCreate'");

    assertEquals(0, sdkCommandNoneCreateCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneCreateCreate'");
    // type SdkCommandNoneCreateIgnore
    var sdkCommandNoneCreateIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateIgnore");
    assertHasFlags(sdkCommandNoneCreateIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneCreateIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneCreate");
    assertEquals(1, sdkCommandNoneCreateIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneCreateIgnore, scoutApi.Replace());

    // fields of SdkCommandNoneCreateIgnore
    assertEquals(1, sdkCommandNoneCreateIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateIgnore'");
    var serialVersionUID34 = assertFieldExist(sdkCommandNoneCreateIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID34, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID34, "long");
    assertEquals(0, serialVersionUID34.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneCreateIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateIgnore'");

    assertEquals(0, sdkCommandNoneCreateIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneCreateIgnore'");
    // type SdkCommandNoneCreateNone
    var sdkCommandNoneCreateNone = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateNone");
    assertHasFlags(sdkCommandNoneCreateNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneCreate");
    assertEquals(1, sdkCommandNoneCreateNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneCreateNone, scoutApi.Replace());

    // fields of SdkCommandNoneCreateNone
    assertEquals(1, sdkCommandNoneCreateNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateNone'");
    var serialVersionUID35 = assertFieldExist(sdkCommandNoneCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID35, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID35, "long");
    assertEquals(0, serialVersionUID35.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneCreateNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateNone'");

    assertEquals(0, sdkCommandNoneCreateNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneCreateNone'");
    // type SdkCommandNoneCreateUse
    var sdkCommandNoneCreateUse = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateUse");
    assertHasFlags(sdkCommandNoneCreateUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneCreateUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneCreate");
    assertEquals(1, sdkCommandNoneCreateUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneCreateUse, scoutApi.Replace());

    // fields of SdkCommandNoneCreateUse
    assertEquals(1, sdkCommandNoneCreateUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateUse'");
    var serialVersionUID36 = assertFieldExist(sdkCommandNoneCreateUse, "serialVersionUID");
    assertHasFlags(serialVersionUID36, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID36, "long");
    assertEquals(0, serialVersionUID36.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneCreateUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateUse'");

    assertEquals(0, sdkCommandNoneCreateUse.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneCreateUse'");
    // type SdkCommandNoneIgnoreCreate
    var sdkCommandNoneIgnoreCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreCreate");
    assertHasFlags(sdkCommandNoneIgnoreCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneIgnoreCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneIgnore");
    assertEquals(1, sdkCommandNoneIgnoreCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneIgnoreCreate, scoutApi.Replace());

    // fields of SdkCommandNoneIgnoreCreate
    assertEquals(1, sdkCommandNoneIgnoreCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreCreate'");
    var serialVersionUID37 = assertFieldExist(sdkCommandNoneIgnoreCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID37, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID37, "long");
    assertEquals(0, serialVersionUID37.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneIgnoreCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreCreate'");

    assertEquals(0, sdkCommandNoneIgnoreCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneIgnoreCreate'");
    // type SdkCommandNoneIgnoreIgnore
    var sdkCommandNoneIgnoreIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreIgnore");
    assertHasFlags(sdkCommandNoneIgnoreIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneIgnore");
    assertEquals(1, sdkCommandNoneIgnoreIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneIgnoreIgnore, scoutApi.Replace());

    // fields of SdkCommandNoneIgnoreIgnore
    assertEquals(1, sdkCommandNoneIgnoreIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreIgnore'");
    var serialVersionUID38 = assertFieldExist(sdkCommandNoneIgnoreIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID38, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID38, "long");
    assertEquals(0, serialVersionUID38.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneIgnoreIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreIgnore'");

    assertEquals(0, sdkCommandNoneIgnoreIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneIgnoreIgnore'");
    // type SdkCommandNoneIgnoreNone
    var sdkCommandNoneIgnoreNone = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreNone");
    assertHasFlags(sdkCommandNoneIgnoreNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneIgnoreNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneIgnore");
    assertEquals(1, sdkCommandNoneIgnoreNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneIgnoreNone, scoutApi.Replace());

    // fields of SdkCommandNoneIgnoreNone
    assertEquals(1, sdkCommandNoneIgnoreNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreNone'");
    var serialVersionUID39 = assertFieldExist(sdkCommandNoneIgnoreNone, "serialVersionUID");
    assertHasFlags(serialVersionUID39, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID39, "long");
    assertEquals(0, serialVersionUID39.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneIgnoreNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreNone'");

    assertEquals(0, sdkCommandNoneIgnoreNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneIgnoreNone'");
    // type SdkCommandNoneIgnoreUse
    var sdkCommandNoneIgnoreUse = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreUse");
    assertHasFlags(sdkCommandNoneIgnoreUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneIgnoreUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneIgnore");
    assertEquals(1, sdkCommandNoneIgnoreUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneIgnoreUse, scoutApi.Replace());

    // fields of SdkCommandNoneIgnoreUse
    assertEquals(1, sdkCommandNoneIgnoreUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreUse'");
    var serialVersionUID40 = assertFieldExist(sdkCommandNoneIgnoreUse, "serialVersionUID");
    assertHasFlags(serialVersionUID40, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID40, "long");
    assertEquals(0, serialVersionUID40.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneIgnoreUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreUse'");

    assertEquals(0, sdkCommandNoneIgnoreUse.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneIgnoreUse'");
    // type SdkCommandNoneNoneCreate
    var sdkCommandNoneNoneCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneCreate");
    assertHasFlags(sdkCommandNoneNoneCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneNoneCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneNone");
    assertHasSuperInterfaces(sdkCommandNoneNoneCreate, new String[]{"formdata.shared.IFormDataInterface03"});
    assertEquals(1, sdkCommandNoneNoneCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneNoneCreate, scoutApi.Replace());

    // fields of SdkCommandNoneNoneCreate
    assertEquals(1, sdkCommandNoneNoneCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneCreate'");
    var serialVersionUID41 = assertFieldExist(sdkCommandNoneNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID41, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID41, "long");
    assertEquals(0, serialVersionUID41.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneNoneCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneCreate'");

    assertEquals(0, sdkCommandNoneNoneCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneNoneCreate'");
    // type SdkCommandNoneNoneIgnore
    var sdkCommandNoneNoneIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneIgnore");
    assertHasFlags(sdkCommandNoneNoneIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneNoneIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneNone");
    assertEquals(1, sdkCommandNoneNoneIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneNoneIgnore, scoutApi.Replace());

    // fields of SdkCommandNoneNoneIgnore
    assertEquals(1, sdkCommandNoneNoneIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneIgnore'");
    var serialVersionUID42 = assertFieldExist(sdkCommandNoneNoneIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID42, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID42, "long");
    assertEquals(0, serialVersionUID42.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneNoneIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneIgnore'");

    assertEquals(0, sdkCommandNoneNoneIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneNoneIgnore'");
    // type SdkCommandNoneNoneNone
    var sdkCommandNoneNoneNone = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneNone");
    assertHasFlags(sdkCommandNoneNoneNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneNoneNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneNone");
    assertEquals(1, sdkCommandNoneNoneNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneNoneNone, scoutApi.Replace());

    // fields of SdkCommandNoneNoneNone
    assertEquals(1, sdkCommandNoneNoneNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneNone'");
    var serialVersionUID43 = assertFieldExist(sdkCommandNoneNoneNone, "serialVersionUID");
    assertHasFlags(serialVersionUID43, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID43, "long");
    assertEquals(0, serialVersionUID43.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneNoneNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneNone'");

    assertEquals(0, sdkCommandNoneNoneNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneNoneNone'");
    // type SdkCommandNoneNoneUse
    var sdkCommandNoneNoneUse = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneUse");
    assertHasFlags(sdkCommandNoneNoneUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneNoneUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneNone");
    assertEquals(1, sdkCommandNoneNoneUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneNoneUse, scoutApi.Replace());

    // fields of SdkCommandNoneNoneUse
    assertEquals(1, sdkCommandNoneNoneUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneUse'");
    var serialVersionUID44 = assertFieldExist(sdkCommandNoneNoneUse, "serialVersionUID");
    assertHasFlags(serialVersionUID44, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID44, "long");
    assertEquals(0, serialVersionUID44.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneNoneUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneUse'");

    assertEquals(0, sdkCommandNoneNoneUse.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneNoneUse'");
    // type SdkCommandNoneUseCreate
    var sdkCommandNoneUseCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseCreate");
    assertHasFlags(sdkCommandNoneUseCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneUseCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneUse");
    assertEquals(1, sdkCommandNoneUseCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneUseCreate, scoutApi.Replace());

    // fields of SdkCommandNoneUseCreate
    assertEquals(1, sdkCommandNoneUseCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseCreate'");
    var serialVersionUID45 = assertFieldExist(sdkCommandNoneUseCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID45, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID45, "long");
    assertEquals(0, serialVersionUID45.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneUseCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseCreate'");

    assertEquals(0, sdkCommandNoneUseCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneUseCreate'");
    // type SdkCommandNoneUseIgnore
    var sdkCommandNoneUseIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseIgnore");
    assertHasFlags(sdkCommandNoneUseIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneUseIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneUse");
    assertEquals(1, sdkCommandNoneUseIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneUseIgnore, scoutApi.Replace());

    // fields of SdkCommandNoneUseIgnore
    assertEquals(1, sdkCommandNoneUseIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseIgnore'");
    var serialVersionUID46 = assertFieldExist(sdkCommandNoneUseIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID46, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID46, "long");
    assertEquals(0, serialVersionUID46.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneUseIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseIgnore'");

    assertEquals(0, sdkCommandNoneUseIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneUseIgnore'");
    // type SdkCommandNoneUseNone
    var sdkCommandNoneUseNone = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseNone");
    assertHasFlags(sdkCommandNoneUseNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneUseNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneUse");
    assertEquals(1, sdkCommandNoneUseNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneUseNone, scoutApi.Replace());

    // fields of SdkCommandNoneUseNone
    assertEquals(1, sdkCommandNoneUseNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseNone'");
    var serialVersionUID47 = assertFieldExist(sdkCommandNoneUseNone, "serialVersionUID");
    assertHasFlags(serialVersionUID47, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID47, "long");
    assertEquals(0, serialVersionUID47.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneUseNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseNone'");

    assertEquals(0, sdkCommandNoneUseNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneUseNone'");
    // type SdkCommandNoneUseUse
    var sdkCommandNoneUseUse = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseUse");
    assertHasFlags(sdkCommandNoneUseUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandNoneUseUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneUse");
    assertEquals(1, sdkCommandNoneUseUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandNoneUseUse, scoutApi.Replace());

    // fields of SdkCommandNoneUseUse
    assertEquals(1, sdkCommandNoneUseUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseUse'");
    var serialVersionUID48 = assertFieldExist(sdkCommandNoneUseUse, "serialVersionUID");
    assertHasFlags(serialVersionUID48, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID48, "long");
    assertEquals(0, serialVersionUID48.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandNoneUseUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseUse'");

    assertEquals(0, sdkCommandNoneUseUse.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneUseUse'");
    // type SdkCommandUseCreateCreate
    var sdkCommandUseCreateCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateCreate");
    assertHasFlags(sdkCommandUseCreateCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseCreateCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseCreate");
    assertEquals(1, sdkCommandUseCreateCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseCreateCreate, scoutApi.Replace());

    // fields of SdkCommandUseCreateCreate
    assertEquals(1, sdkCommandUseCreateCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateCreate'");
    var serialVersionUID49 = assertFieldExist(sdkCommandUseCreateCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID49, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID49, "long");
    assertEquals(0, serialVersionUID49.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseCreateCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateCreate'");

    assertEquals(0, sdkCommandUseCreateCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandUseCreateCreate'");
    // type SdkCommandUseCreateIgnore
    var sdkCommandUseCreateIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateIgnore");
    assertHasFlags(sdkCommandUseCreateIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseCreateIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseCreate");
    assertEquals(1, sdkCommandUseCreateIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseCreateIgnore, scoutApi.Replace());

    // fields of SdkCommandUseCreateIgnore
    assertEquals(1, sdkCommandUseCreateIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateIgnore'");
    var serialVersionUID50 = assertFieldExist(sdkCommandUseCreateIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID50, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID50, "long");
    assertEquals(0, serialVersionUID50.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseCreateIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateIgnore'");

    assertEquals(0, sdkCommandUseCreateIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandUseCreateIgnore'");
    // type SdkCommandUseCreateNone
    var sdkCommandUseCreateNone = assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateNone");
    assertHasFlags(sdkCommandUseCreateNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseCreate");
    assertEquals(1, sdkCommandUseCreateNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseCreateNone, scoutApi.Replace());

    // fields of SdkCommandUseCreateNone
    assertEquals(1, sdkCommandUseCreateNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateNone'");
    var serialVersionUID51 = assertFieldExist(sdkCommandUseCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID51, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID51, "long");
    assertEquals(0, serialVersionUID51.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseCreateNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateNone'");

    assertEquals(0, sdkCommandUseCreateNone.innerTypes().stream().count(), "inner types count of 'SdkCommandUseCreateNone'");
    // type SdkCommandUseCreateUse
    var sdkCommandUseCreateUse = assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateUse");
    assertHasFlags(sdkCommandUseCreateUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseCreateUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseCreate");
    assertEquals(1, sdkCommandUseCreateUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseCreateUse, scoutApi.Replace());

    // fields of SdkCommandUseCreateUse
    assertEquals(1, sdkCommandUseCreateUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateUse'");
    var serialVersionUID52 = assertFieldExist(sdkCommandUseCreateUse, "serialVersionUID");
    assertHasFlags(serialVersionUID52, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID52, "long");
    assertEquals(0, serialVersionUID52.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseCreateUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateUse'");

    assertEquals(0, sdkCommandUseCreateUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUseCreateUse'");
    // type SdkCommandUseIgnoreCreate
    var sdkCommandUseIgnoreCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreCreate");
    assertHasFlags(sdkCommandUseIgnoreCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseIgnoreCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseIgnore");
    assertEquals(1, sdkCommandUseIgnoreCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseIgnoreCreate, scoutApi.Replace());

    // fields of SdkCommandUseIgnoreCreate
    assertEquals(1, sdkCommandUseIgnoreCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreCreate'");
    var serialVersionUID53 = assertFieldExist(sdkCommandUseIgnoreCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID53, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID53, "long");
    assertEquals(0, serialVersionUID53.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseIgnoreCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreCreate'");

    assertEquals(0, sdkCommandUseIgnoreCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandUseIgnoreCreate'");
    // type SdkCommandUseIgnoreIgnore
    var sdkCommandUseIgnoreIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreIgnore");
    assertHasFlags(sdkCommandUseIgnoreIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseIgnore");
    assertEquals(1, sdkCommandUseIgnoreIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseIgnoreIgnore, scoutApi.Replace());

    // fields of SdkCommandUseIgnoreIgnore
    assertEquals(1, sdkCommandUseIgnoreIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreIgnore'");
    var serialVersionUID54 = assertFieldExist(sdkCommandUseIgnoreIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID54, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID54, "long");
    assertEquals(0, serialVersionUID54.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseIgnoreIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreIgnore'");

    assertEquals(0, sdkCommandUseIgnoreIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandUseIgnoreIgnore'");
    // type SdkCommandUseIgnoreNone
    var sdkCommandUseIgnoreNone = assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreNone");
    assertHasFlags(sdkCommandUseIgnoreNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseIgnoreNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseIgnore");
    assertEquals(1, sdkCommandUseIgnoreNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseIgnoreNone, scoutApi.Replace());

    // fields of SdkCommandUseIgnoreNone
    assertEquals(1, sdkCommandUseIgnoreNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreNone'");
    var serialVersionUID55 = assertFieldExist(sdkCommandUseIgnoreNone, "serialVersionUID");
    assertHasFlags(serialVersionUID55, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID55, "long");
    assertEquals(0, serialVersionUID55.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseIgnoreNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreNone'");

    assertEquals(0, sdkCommandUseIgnoreNone.innerTypes().stream().count(), "inner types count of 'SdkCommandUseIgnoreNone'");
    // type SdkCommandUseIgnoreUse
    var sdkCommandUseIgnoreUse = assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreUse");
    assertHasFlags(sdkCommandUseIgnoreUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseIgnoreUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseIgnore");
    assertEquals(1, sdkCommandUseIgnoreUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseIgnoreUse, scoutApi.Replace());

    // fields of SdkCommandUseIgnoreUse
    assertEquals(1, sdkCommandUseIgnoreUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreUse'");
    var serialVersionUID56 = assertFieldExist(sdkCommandUseIgnoreUse, "serialVersionUID");
    assertHasFlags(serialVersionUID56, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID56, "long");
    assertEquals(0, serialVersionUID56.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseIgnoreUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreUse'");

    assertEquals(0, sdkCommandUseIgnoreUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUseIgnoreUse'");
    // type SdkCommandUseNoneCreate
    var sdkCommandUseNoneCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneCreate");
    assertHasFlags(sdkCommandUseNoneCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseNoneCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseNone");
    assertEquals(1, sdkCommandUseNoneCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseNoneCreate, scoutApi.Replace());

    // fields of SdkCommandUseNoneCreate
    assertEquals(1, sdkCommandUseNoneCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneCreate'");
    var serialVersionUID57 = assertFieldExist(sdkCommandUseNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID57, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID57, "long");
    assertEquals(0, serialVersionUID57.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseNoneCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneCreate'");

    assertEquals(0, sdkCommandUseNoneCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandUseNoneCreate'");
    // type SdkCommandUseNoneIgnore
    var sdkCommandUseNoneIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneIgnore");
    assertHasFlags(sdkCommandUseNoneIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseNoneIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseNone");
    assertEquals(1, sdkCommandUseNoneIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseNoneIgnore, scoutApi.Replace());

    // fields of SdkCommandUseNoneIgnore
    assertEquals(1, sdkCommandUseNoneIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneIgnore'");
    var serialVersionUID58 = assertFieldExist(sdkCommandUseNoneIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID58, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID58, "long");
    assertEquals(0, serialVersionUID58.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseNoneIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneIgnore'");

    assertEquals(0, sdkCommandUseNoneIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandUseNoneIgnore'");
    // type SdkCommandUseNoneNone
    var sdkCommandUseNoneNone = assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneNone");
    assertHasFlags(sdkCommandUseNoneNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseNoneNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseNone");
    assertEquals(1, sdkCommandUseNoneNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseNoneNone, scoutApi.Replace());

    // fields of SdkCommandUseNoneNone
    assertEquals(1, sdkCommandUseNoneNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneNone'");
    var serialVersionUID59 = assertFieldExist(sdkCommandUseNoneNone, "serialVersionUID");
    assertHasFlags(serialVersionUID59, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID59, "long");
    assertEquals(0, serialVersionUID59.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseNoneNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneNone'");

    assertEquals(0, sdkCommandUseNoneNone.innerTypes().stream().count(), "inner types count of 'SdkCommandUseNoneNone'");
    // type SdkCommandUseNoneUse
    var sdkCommandUseNoneUse = assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneUse");
    assertHasFlags(sdkCommandUseNoneUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseNoneUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseNone");
    assertEquals(1, sdkCommandUseNoneUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseNoneUse, scoutApi.Replace());

    // fields of SdkCommandUseNoneUse
    assertEquals(1, sdkCommandUseNoneUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneUse'");
    var serialVersionUID60 = assertFieldExist(sdkCommandUseNoneUse, "serialVersionUID");
    assertHasFlags(serialVersionUID60, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID60, "long");
    assertEquals(0, serialVersionUID60.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseNoneUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneUse'");

    assertEquals(0, sdkCommandUseNoneUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUseNoneUse'");
    // type SdkCommandUseUseCreate
    var sdkCommandUseUseCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseCreate");
    assertHasFlags(sdkCommandUseUseCreate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseUseCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseUse");
    assertEquals(1, sdkCommandUseUseCreate.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseUseCreate, scoutApi.Replace());

    // fields of SdkCommandUseUseCreate
    assertEquals(1, sdkCommandUseUseCreate.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseCreate'");
    var serialVersionUID61 = assertFieldExist(sdkCommandUseUseCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID61, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID61, "long");
    assertEquals(0, serialVersionUID61.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseUseCreate.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseCreate'");

    assertEquals(0, sdkCommandUseUseCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandUseUseCreate'");
    // type SdkCommandUseUseIgnore
    var sdkCommandUseUseIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseIgnore");
    assertHasFlags(sdkCommandUseUseIgnore, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseUseIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseUse");
    assertEquals(1, sdkCommandUseUseIgnore.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseUseIgnore, scoutApi.Replace());

    // fields of SdkCommandUseUseIgnore
    assertEquals(1, sdkCommandUseUseIgnore.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseIgnore'");
    var serialVersionUID62 = assertFieldExist(sdkCommandUseUseIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID62, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID62, "long");
    assertEquals(0, serialVersionUID62.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseUseIgnore.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseIgnore'");

    assertEquals(0, sdkCommandUseUseIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandUseUseIgnore'");
    // type SdkCommandUseUseNone
    var sdkCommandUseUseNone = assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseNone");
    assertHasFlags(sdkCommandUseUseNone, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseUseNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseUse");
    assertEquals(1, sdkCommandUseUseNone.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseUseNone, scoutApi.Replace());

    // fields of SdkCommandUseUseNone
    assertEquals(1, sdkCommandUseUseNone.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseNone'");
    var serialVersionUID63 = assertFieldExist(sdkCommandUseUseNone, "serialVersionUID");
    assertHasFlags(serialVersionUID63, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID63, "long");
    assertEquals(0, serialVersionUID63.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseUseNone.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseNone'");

    assertEquals(0, sdkCommandUseUseNone.innerTypes().stream().count(), "inner types count of 'SdkCommandUseUseNone'");
    // type SdkCommandUseUseUse
    var sdkCommandUseUseUse = assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseUse");
    assertHasFlags(sdkCommandUseUseUse, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sdkCommandUseUseUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseUse");
    assertEquals(1, sdkCommandUseUseUse.annotations().stream().count(), "annotation count");
    assertAnnotation(sdkCommandUseUseUse, scoutApi.Replace());

    // fields of SdkCommandUseUseUse
    assertEquals(1, sdkCommandUseUseUse.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseUse'");
    var serialVersionUID64 = assertFieldExist(sdkCommandUseUseUse, "serialVersionUID");
    assertHasFlags(serialVersionUID64, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID64, "long");
    assertEquals(0, serialVersionUID64.annotations().stream().count(), "annotation count");

    assertEquals(0, sdkCommandUseUseUse.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseUse'");

    assertEquals(0, sdkCommandUseUseUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUseUseUse'");
  }
}
