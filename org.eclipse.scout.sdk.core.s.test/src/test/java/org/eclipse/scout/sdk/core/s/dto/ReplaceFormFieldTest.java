/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperInterfaces;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.replace.BaseForm;
import formdata.client.ui.forms.replace.ExtendedExtendedForm;
import formdata.client.ui.forms.replace.ExtendedForm;

public class ReplaceFormFieldTest {

  @Test
  public void runTests() {
    checkBaseFormData();
    checkExtendedFormData();
    checkExtendedExtendedFormData();
  }

  private static void checkBaseFormData() {
    createFormDataAssertNoCompileErrors(BaseForm.class.getName(), ReplaceFormFieldTest::testApiOfBaseFormData);
  }

  private static void checkExtendedFormData() {
    createFormDataAssertNoCompileErrors(ExtendedForm.class.getName(), ReplaceFormFieldTest::testApiOfExtendedFormData);
  }

  private static void checkExtendedExtendedFormData() {
    createFormDataAssertNoCompileErrors(ExtendedExtendedForm.class.getName(), ReplaceFormFieldTest::testApiOfExtendedExtendedFormData);
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
    IField serialVersionUID = assertFieldExist(baseFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(6, baseFormData.methods().stream().count(), "method count of 'BaseFormData'");
    IMethod getLookup = assertMethodExist(baseFormData, "getLookup", new String[]{});
    assertMethodReturnType(getLookup, "formdata.shared.services.process.replace.BaseFormData$Lookup");
    IMethod getName = assertMethodExist(baseFormData, "getName", new String[]{});
    assertMethodReturnType(getName, "formdata.shared.services.process.replace.BaseFormData$Name");
    IMethod getSdkCommandCreate = assertMethodExist(baseFormData, "getSdkCommandCreate", new String[]{});
    assertMethodReturnType(getSdkCommandCreate, "formdata.shared.services.process.replace.BaseFormData$SdkCommandCreate");
    IMethod getSdkCommandNone = assertMethodExist(baseFormData, "getSdkCommandNone", new String[]{});
    assertMethodReturnType(getSdkCommandNone, "formdata.shared.services.process.replace.BaseFormData$SdkCommandNone");
    IMethod getSdkCommandUse = assertMethodExist(baseFormData, "getSdkCommandUse", new String[]{});
    assertMethodReturnType(getSdkCommandUse, "formdata.shared.services.process.replace.BaseFormData$SdkCommandUse");
    IMethod getSmart = assertMethodExist(baseFormData, "getSmart", new String[]{});
    assertMethodReturnType(getSmart, "formdata.shared.services.process.replace.BaseFormData$Smart");

    assertEquals(6, baseFormData.innerTypes().stream().count(), "inner types count of 'BaseFormData'");
    // type Lookup
    IType lookup = assertTypeExists(baseFormData, "Lookup");
    assertHasFlags(lookup, 9);
    assertHasSuperClass(lookup, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Long>");

    // fields of Lookup
    assertEquals(1, lookup.fields().stream().count(), "field count of 'Lookup'");
    IField serialVersionUID1 = assertFieldExist(lookup, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, lookup.methods().stream().count(), "method count of 'Lookup'");

    assertEquals(0, lookup.innerTypes().stream().count(), "inner types count of 'Lookup'");
    // type Name
    IType name = assertTypeExists(baseFormData, "Name");
    assertHasFlags(name, 9);
    assertHasSuperClass(name, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of Name
    assertEquals(1, name.fields().stream().count(), "field count of 'Name'");
    IField serialVersionUID2 = assertFieldExist(name, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, name.methods().stream().count(), "method count of 'Name'");

    assertEquals(0, name.innerTypes().stream().count(), "inner types count of 'Name'");
    // type SdkCommandCreate
    IType sdkCommandCreate = assertTypeExists(baseFormData, "SdkCommandCreate");
    assertHasFlags(sdkCommandCreate, 9);
    assertHasSuperClass(sdkCommandCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of SdkCommandCreate
    assertEquals(1, sdkCommandCreate.fields().stream().count(), "field count of 'SdkCommandCreate'");
    IField serialVersionUID3 = assertFieldExist(sdkCommandCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(0, sdkCommandCreate.methods().stream().count(), "method count of 'SdkCommandCreate'");

    assertEquals(0, sdkCommandCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreate'");
    // type SdkCommandNone
    IType sdkCommandNone = assertTypeExists(baseFormData, "SdkCommandNone");
    assertHasFlags(sdkCommandNone, 9);
    assertHasSuperClass(sdkCommandNone, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of SdkCommandNone
    assertEquals(1, sdkCommandNone.fields().stream().count(), "field count of 'SdkCommandNone'");
    IField serialVersionUID4 = assertFieldExist(sdkCommandNone, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(0, sdkCommandNone.methods().stream().count(), "method count of 'SdkCommandNone'");

    assertEquals(0, sdkCommandNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNone'");
    // type SdkCommandUse
    IType sdkCommandUse = assertTypeExists(baseFormData, "SdkCommandUse");
    assertHasFlags(sdkCommandUse, 9);
    assertHasSuperClass(sdkCommandUse, "formdata.shared.services.process.replace.UsingFormFieldData");

    // fields of SdkCommandUse
    assertEquals(1, sdkCommandUse.fields().stream().count(), "field count of 'SdkCommandUse'");
    IField serialVersionUID5 = assertFieldExist(sdkCommandUse, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(0, sdkCommandUse.methods().stream().count(), "method count of 'SdkCommandUse'");

    assertEquals(0, sdkCommandUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUse'");
    // type Smart
    IType smart = assertTypeExists(baseFormData, "Smart");
    assertHasFlags(smart, 9);
    assertHasSuperClass(smart, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Long>");

    // fields of Smart
    assertEquals(1, smart.fields().stream().count(), "field count of 'Smart'");
    IField serialVersionUID6 = assertFieldExist(smart, "serialVersionUID");
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
    IField serialVersionUID = assertFieldExist(extendedFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(19, extendedFormData.methods().stream().count(), "method count of 'ExtendedFormData'");
    IMethod getFirstName = assertMethodExist(extendedFormData, "getFirstName", new String[]{});
    assertMethodReturnType(getFirstName, "formdata.shared.services.process.replace.ExtendedFormData$FirstName");
    IMethod getIgnoringGroupBoxExCreate = assertMethodExist(extendedFormData, "getIgnoringGroupBoxExCreate", new String[]{});
    assertMethodReturnType(getIgnoringGroupBoxExCreate, "formdata.shared.services.process.replace.ExtendedFormData$IgnoringGroupBoxExCreate");
    IMethod getIgnoringGroupBoxExUse = assertMethodExist(extendedFormData, "getIgnoringGroupBoxExUse", new String[]{});
    assertMethodReturnType(getIgnoringGroupBoxExUse, "formdata.shared.services.process.replace.ExtendedFormData$IgnoringGroupBoxExUse");
    IMethod getNameEx = assertMethodExist(extendedFormData, "getNameEx", new String[]{});
    assertMethodReturnType(getNameEx, "formdata.shared.services.process.replace.ExtendedFormData$NameEx");
    IMethod getSdkCommandCreateCreate = assertMethodExist(extendedFormData, "getSdkCommandCreateCreate", new String[]{});
    assertMethodReturnType(getSdkCommandCreateCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateCreate");
    IMethod getSdkCommandCreateIgnore = assertMethodExist(extendedFormData, "getSdkCommandCreateIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandCreateIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateIgnore");
    IMethod getSdkCommandCreateNone = assertMethodExist(extendedFormData, "getSdkCommandCreateNone", new String[]{});
    assertMethodReturnType(getSdkCommandCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateNone");
    IMethod getSdkCommandCreateUse = assertMethodExist(extendedFormData, "getSdkCommandCreateUse", new String[]{});
    assertMethodReturnType(getSdkCommandCreateUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateUse");
    IMethod getSdkCommandIgnoreCreate = assertMethodExist(extendedFormData, "getSdkCommandIgnoreCreate", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreCreate");
    IMethod getSdkCommandIgnoreUse = assertMethodExist(extendedFormData, "getSdkCommandIgnoreUse", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreUse");
    IMethod getSdkCommandNoneCreate = assertMethodExist(extendedFormData, "getSdkCommandNoneCreate", new String[]{});
    assertMethodReturnType(getSdkCommandNoneCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneCreate");
    IMethod getSdkCommandNoneIgnore = assertMethodExist(extendedFormData, "getSdkCommandNoneIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandNoneIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneIgnore");
    IMethod getSdkCommandNoneNone = assertMethodExist(extendedFormData, "getSdkCommandNoneNone", new String[]{});
    assertMethodReturnType(getSdkCommandNoneNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneNone");
    IMethod getSdkCommandNoneUse = assertMethodExist(extendedFormData, "getSdkCommandNoneUse", new String[]{});
    assertMethodReturnType(getSdkCommandNoneUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneUse");
    IMethod getSdkCommandUseCreate = assertMethodExist(extendedFormData, "getSdkCommandUseCreate", new String[]{});
    assertMethodReturnType(getSdkCommandUseCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseCreate");
    IMethod getSdkCommandUseIgnore = assertMethodExist(extendedFormData, "getSdkCommandUseIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandUseIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseIgnore");
    IMethod getSdkCommandUseNone = assertMethodExist(extendedFormData, "getSdkCommandUseNone", new String[]{});
    assertMethodReturnType(getSdkCommandUseNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseNone");
    IMethod getSdkCommandUseUse = assertMethodExist(extendedFormData, "getSdkCommandUseUse", new String[]{});
    assertMethodReturnType(getSdkCommandUseUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseUse");
    IMethod getSmartEx = assertMethodExist(extendedFormData, "getSmartEx", new String[]{});
    assertMethodReturnType(getSmartEx, "formdata.shared.services.process.replace.ExtendedFormData$SmartEx");

    assertEquals(19, extendedFormData.innerTypes().stream().count(), "inner types count of 'ExtendedFormData'");
    // type FirstName
    IType firstName = assertTypeExists(extendedFormData, "FirstName");
    assertHasFlags(firstName, 9);
    assertHasSuperClass(firstName, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of FirstName
    assertEquals(1, firstName.fields().stream().count(), "field count of 'FirstName'");
    IField serialVersionUID1 = assertFieldExist(firstName, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, firstName.methods().stream().count(), "method count of 'FirstName'");

    assertEquals(0, firstName.innerTypes().stream().count(), "inner types count of 'FirstName'");
    // type IgnoringGroupBoxExCreate
    IType ignoringGroupBoxExCreate = assertTypeExists(extendedFormData, "IgnoringGroupBoxExCreate");
    assertHasFlags(ignoringGroupBoxExCreate, 9);
    assertHasSuperClass(ignoringGroupBoxExCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertAnnotation(ignoringGroupBoxExCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of IgnoringGroupBoxExCreate
    assertEquals(1, ignoringGroupBoxExCreate.fields().stream().count(), "field count of 'IgnoringGroupBoxExCreate'");
    IField serialVersionUID2 = assertFieldExist(ignoringGroupBoxExCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, ignoringGroupBoxExCreate.methods().stream().count(), "method count of 'IgnoringGroupBoxExCreate'");

    assertEquals(0, ignoringGroupBoxExCreate.innerTypes().stream().count(), "inner types count of 'IgnoringGroupBoxExCreate'");
    // type IgnoringGroupBoxExUse
    IType ignoringGroupBoxExUse = assertTypeExists(extendedFormData, "IgnoringGroupBoxExUse");
    assertHasFlags(ignoringGroupBoxExUse, 9);
    assertHasSuperClass(ignoringGroupBoxExUse, "formdata.shared.services.process.replace.UsingFormFieldData");
    assertAnnotation(ignoringGroupBoxExUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of IgnoringGroupBoxExUse
    assertEquals(1, ignoringGroupBoxExUse.fields().stream().count(), "field count of 'IgnoringGroupBoxExUse'");
    IField serialVersionUID3 = assertFieldExist(ignoringGroupBoxExUse, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(0, ignoringGroupBoxExUse.methods().stream().count(), "method count of 'IgnoringGroupBoxExUse'");

    assertEquals(0, ignoringGroupBoxExUse.innerTypes().stream().count(), "inner types count of 'IgnoringGroupBoxExUse'");
    // type NameEx
    IType nameEx = assertTypeExists(extendedFormData, "NameEx");
    assertHasFlags(nameEx, 9);
    assertHasSuperClass(nameEx, "formdata.shared.services.process.replace.BaseFormData$Name");
    assertAnnotation(nameEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of NameEx
    assertEquals(1, nameEx.fields().stream().count(), "field count of 'NameEx'");
    IField serialVersionUID4 = assertFieldExist(nameEx, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(0, nameEx.methods().stream().count(), "method count of 'NameEx'");

    assertEquals(0, nameEx.innerTypes().stream().count(), "inner types count of 'NameEx'");
    // type SdkCommandCreateCreate
    IType sdkCommandCreateCreate = assertTypeExists(extendedFormData, "SdkCommandCreateCreate");
    assertHasFlags(sdkCommandCreateCreate, 9);
    assertHasSuperClass(sdkCommandCreateCreate, "formdata.shared.services.process.replace.BaseFormData$SdkCommandCreate");
    assertAnnotation(sdkCommandCreateCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateCreate
    assertEquals(1, sdkCommandCreateCreate.fields().stream().count(), "field count of 'SdkCommandCreateCreate'");
    IField serialVersionUID5 = assertFieldExist(sdkCommandCreateCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(0, sdkCommandCreateCreate.methods().stream().count(), "method count of 'SdkCommandCreateCreate'");

    assertEquals(0, sdkCommandCreateCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateCreate'");
    // type SdkCommandCreateIgnore
    IType sdkCommandCreateIgnore = assertTypeExists(extendedFormData, "SdkCommandCreateIgnore");
    assertHasFlags(sdkCommandCreateIgnore, 9);
    assertHasSuperClass(sdkCommandCreateIgnore, "formdata.shared.services.process.replace.BaseFormData$SdkCommandCreate");
    assertAnnotation(sdkCommandCreateIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateIgnore
    assertEquals(1, sdkCommandCreateIgnore.fields().stream().count(), "field count of 'SdkCommandCreateIgnore'");
    IField serialVersionUID6 = assertFieldExist(sdkCommandCreateIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");

    assertEquals(0, sdkCommandCreateIgnore.methods().stream().count(), "method count of 'SdkCommandCreateIgnore'");

    assertEquals(0, sdkCommandCreateIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateIgnore'");
    // type SdkCommandCreateNone
    IType sdkCommandCreateNone = assertTypeExists(extendedFormData, "SdkCommandCreateNone");
    assertHasFlags(sdkCommandCreateNone, 9);
    assertHasSuperClass(sdkCommandCreateNone, "formdata.shared.services.process.replace.BaseFormData$SdkCommandCreate");
    assertAnnotation(sdkCommandCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateNone
    assertEquals(1, sdkCommandCreateNone.fields().stream().count(), "field count of 'SdkCommandCreateNone'");
    IField serialVersionUID7 = assertFieldExist(sdkCommandCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID7, 26);
    assertFieldType(serialVersionUID7, "long");

    assertEquals(0, sdkCommandCreateNone.methods().stream().count(), "method count of 'SdkCommandCreateNone'");

    assertEquals(0, sdkCommandCreateNone.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateNone'");
    // type SdkCommandCreateUse
    IType sdkCommandCreateUse = assertTypeExists(extendedFormData, "SdkCommandCreateUse");
    assertHasFlags(sdkCommandCreateUse, 9);
    assertHasSuperClass(sdkCommandCreateUse, "formdata.shared.services.process.replace.BaseFormData$SdkCommandCreate");
    assertAnnotation(sdkCommandCreateUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateUse
    assertEquals(1, sdkCommandCreateUse.fields().stream().count(), "field count of 'SdkCommandCreateUse'");
    IField serialVersionUID8 = assertFieldExist(sdkCommandCreateUse, "serialVersionUID");
    assertHasFlags(serialVersionUID8, 26);
    assertFieldType(serialVersionUID8, "long");

    assertEquals(0, sdkCommandCreateUse.methods().stream().count(), "method count of 'SdkCommandCreateUse'");

    assertEquals(0, sdkCommandCreateUse.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateUse'");
    // type SdkCommandIgnoreCreate
    IType sdkCommandIgnoreCreate = assertTypeExists(extendedFormData, "SdkCommandIgnoreCreate");
    assertHasFlags(sdkCommandIgnoreCreate, 9);
    assertHasSuperClass(sdkCommandIgnoreCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertAnnotation(sdkCommandIgnoreCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreCreate
    assertEquals(1, sdkCommandIgnoreCreate.fields().stream().count(), "field count of 'SdkCommandIgnoreCreate'");
    IField serialVersionUID9 = assertFieldExist(sdkCommandIgnoreCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID9, 26);
    assertFieldType(serialVersionUID9, "long");

    assertEquals(0, sdkCommandIgnoreCreate.methods().stream().count(), "method count of 'SdkCommandIgnoreCreate'");

    assertEquals(0, sdkCommandIgnoreCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreCreate'");
    // type SdkCommandIgnoreUse
    IType sdkCommandIgnoreUse = assertTypeExists(extendedFormData, "SdkCommandIgnoreUse");
    assertHasFlags(sdkCommandIgnoreUse, 9);
    assertHasSuperClass(sdkCommandIgnoreUse, "formdata.shared.services.process.replace.UsingFormFieldData");
    assertAnnotation(sdkCommandIgnoreUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreUse
    assertEquals(1, sdkCommandIgnoreUse.fields().stream().count(), "field count of 'SdkCommandIgnoreUse'");
    IField serialVersionUID10 = assertFieldExist(sdkCommandIgnoreUse, "serialVersionUID");
    assertHasFlags(serialVersionUID10, 26);
    assertFieldType(serialVersionUID10, "long");

    assertEquals(0, sdkCommandIgnoreUse.methods().stream().count(), "method count of 'SdkCommandIgnoreUse'");

    assertEquals(0, sdkCommandIgnoreUse.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreUse'");
    // type SdkCommandNoneCreate
    IType sdkCommandNoneCreate = assertTypeExists(extendedFormData, "SdkCommandNoneCreate");
    assertHasFlags(sdkCommandNoneCreate, 9);
    assertHasSuperClass(sdkCommandNoneCreate, "formdata.shared.services.process.replace.BaseFormData$SdkCommandNone");
    assertAnnotation(sdkCommandNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneCreate
    assertEquals(1, sdkCommandNoneCreate.fields().stream().count(), "field count of 'SdkCommandNoneCreate'");
    IField serialVersionUID11 = assertFieldExist(sdkCommandNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID11, 26);
    assertFieldType(serialVersionUID11, "long");

    assertEquals(0, sdkCommandNoneCreate.methods().stream().count(), "method count of 'SdkCommandNoneCreate'");

    assertEquals(0, sdkCommandNoneCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneCreate'");
    // type SdkCommandNoneIgnore
    IType sdkCommandNoneIgnore = assertTypeExists(extendedFormData, "SdkCommandNoneIgnore");
    assertHasFlags(sdkCommandNoneIgnore, 9);
    assertHasSuperClass(sdkCommandNoneIgnore, "formdata.shared.services.process.replace.BaseFormData$SdkCommandNone");
    assertAnnotation(sdkCommandNoneIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneIgnore
    assertEquals(1, sdkCommandNoneIgnore.fields().stream().count(), "field count of 'SdkCommandNoneIgnore'");
    IField serialVersionUID12 = assertFieldExist(sdkCommandNoneIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID12, 26);
    assertFieldType(serialVersionUID12, "long");

    assertEquals(0, sdkCommandNoneIgnore.methods().stream().count(), "method count of 'SdkCommandNoneIgnore'");

    assertEquals(0, sdkCommandNoneIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneIgnore'");
    // type SdkCommandNoneNone
    IType sdkCommandNoneNone = assertTypeExists(extendedFormData, "SdkCommandNoneNone");
    assertHasFlags(sdkCommandNoneNone, 9);
    assertHasSuperClass(sdkCommandNoneNone, "formdata.shared.services.process.replace.BaseFormData$SdkCommandNone");
    assertAnnotation(sdkCommandNoneNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneNone
    assertEquals(1, sdkCommandNoneNone.fields().stream().count(), "field count of 'SdkCommandNoneNone'");
    IField serialVersionUID13 = assertFieldExist(sdkCommandNoneNone, "serialVersionUID");
    assertHasFlags(serialVersionUID13, 26);
    assertFieldType(serialVersionUID13, "long");

    assertEquals(0, sdkCommandNoneNone.methods().stream().count(), "method count of 'SdkCommandNoneNone'");

    assertEquals(0, sdkCommandNoneNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneNone'");
    // type SdkCommandNoneUse
    IType sdkCommandNoneUse = assertTypeExists(extendedFormData, "SdkCommandNoneUse");
    assertHasFlags(sdkCommandNoneUse, 9);
    assertHasSuperClass(sdkCommandNoneUse, "formdata.shared.services.process.replace.BaseFormData$SdkCommandNone");
    assertAnnotation(sdkCommandNoneUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneUse
    assertEquals(1, sdkCommandNoneUse.fields().stream().count(), "field count of 'SdkCommandNoneUse'");
    IField serialVersionUID14 = assertFieldExist(sdkCommandNoneUse, "serialVersionUID");
    assertHasFlags(serialVersionUID14, 26);
    assertFieldType(serialVersionUID14, "long");

    assertEquals(0, sdkCommandNoneUse.methods().stream().count(), "method count of 'SdkCommandNoneUse'");

    assertEquals(0, sdkCommandNoneUse.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneUse'");
    // type SdkCommandUseCreate
    IType sdkCommandUseCreate = assertTypeExists(extendedFormData, "SdkCommandUseCreate");
    assertHasFlags(sdkCommandUseCreate, 9);
    assertHasSuperClass(sdkCommandUseCreate, "formdata.shared.services.process.replace.BaseFormData$SdkCommandUse");
    assertAnnotation(sdkCommandUseCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseCreate
    assertEquals(1, sdkCommandUseCreate.fields().stream().count(), "field count of 'SdkCommandUseCreate'");
    IField serialVersionUID15 = assertFieldExist(sdkCommandUseCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID15, 26);
    assertFieldType(serialVersionUID15, "long");

    assertEquals(0, sdkCommandUseCreate.methods().stream().count(), "method count of 'SdkCommandUseCreate'");

    assertEquals(0, sdkCommandUseCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandUseCreate'");
    // type SdkCommandUseIgnore
    IType sdkCommandUseIgnore = assertTypeExists(extendedFormData, "SdkCommandUseIgnore");
    assertHasFlags(sdkCommandUseIgnore, 9);
    assertHasSuperClass(sdkCommandUseIgnore, "formdata.shared.services.process.replace.BaseFormData$SdkCommandUse");
    assertAnnotation(sdkCommandUseIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseIgnore
    assertEquals(1, sdkCommandUseIgnore.fields().stream().count(), "field count of 'SdkCommandUseIgnore'");
    IField serialVersionUID16 = assertFieldExist(sdkCommandUseIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID16, 26);
    assertFieldType(serialVersionUID16, "long");

    assertEquals(0, sdkCommandUseIgnore.methods().stream().count(), "method count of 'SdkCommandUseIgnore'");

    assertEquals(0, sdkCommandUseIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandUseIgnore'");
    // type SdkCommandUseNone
    IType sdkCommandUseNone = assertTypeExists(extendedFormData, "SdkCommandUseNone");
    assertHasFlags(sdkCommandUseNone, 9);
    assertHasSuperClass(sdkCommandUseNone, "formdata.shared.services.process.replace.BaseFormData$SdkCommandUse");
    assertAnnotation(sdkCommandUseNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseNone
    assertEquals(1, sdkCommandUseNone.fields().stream().count(), "field count of 'SdkCommandUseNone'");
    IField serialVersionUID17 = assertFieldExist(sdkCommandUseNone, "serialVersionUID");
    assertHasFlags(serialVersionUID17, 26);
    assertFieldType(serialVersionUID17, "long");

    assertEquals(0, sdkCommandUseNone.methods().stream().count(), "method count of 'SdkCommandUseNone'");

    assertEquals(0, sdkCommandUseNone.innerTypes().stream().count(), "inner types count of 'SdkCommandUseNone'");
    // type SdkCommandUseUse
    IType sdkCommandUseUse = assertTypeExists(extendedFormData, "SdkCommandUseUse");
    assertHasFlags(sdkCommandUseUse, 9);
    assertHasSuperClass(sdkCommandUseUse, "formdata.shared.services.process.replace.BaseFormData$SdkCommandUse");
    assertAnnotation(sdkCommandUseUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseUse
    assertEquals(1, sdkCommandUseUse.fields().stream().count(), "field count of 'SdkCommandUseUse'");
    IField serialVersionUID18 = assertFieldExist(sdkCommandUseUse, "serialVersionUID");
    assertHasFlags(serialVersionUID18, 26);
    assertFieldType(serialVersionUID18, "long");

    assertEquals(0, sdkCommandUseUse.methods().stream().count(), "method count of 'SdkCommandUseUse'");

    assertEquals(0, sdkCommandUseUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUseUse'");
    // type SmartEx
    IType smartEx = assertTypeExists(extendedFormData, "SmartEx");
    assertHasFlags(smartEx, 9);
    assertHasSuperClass(smartEx, "formdata.shared.services.process.replace.BaseFormData$Smart");
    assertAnnotation(smartEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of SmartEx
    assertEquals(1, smartEx.fields().stream().count(), "field count of 'SmartEx'");
    IField serialVersionUID19 = assertFieldExist(smartEx, "serialVersionUID");
    assertHasFlags(serialVersionUID19, 26);
    assertFieldType(serialVersionUID19, "long");

    assertEquals(0, smartEx.methods().stream().count(), "method count of 'SmartEx'");

    assertEquals(0, smartEx.innerTypes().stream().count(), "inner types count of 'SmartEx'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfExtendedExtendedFormData(IType extendedExtendedFormData) {
    // type ExtendedExtendedFormData
    assertHasFlags(extendedExtendedFormData, 1);
    assertHasSuperClass(extendedExtendedFormData, "formdata.shared.services.process.replace.ExtendedFormData");
    assertAnnotation(extendedExtendedFormData, "javax.annotation.Generated");

    // fields of ExtendedExtendedFormData
    assertEquals(1, extendedExtendedFormData.fields().stream().count(), "field count of 'ExtendedExtendedFormData'");
    IField serialVersionUID = assertFieldExist(extendedExtendedFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(63, extendedExtendedFormData.methods().stream().count(), "method count of 'ExtendedExtendedFormData'");
    IMethod getIgnoringGroupBoxExCreateNone = assertMethodExist(extendedExtendedFormData, "getIgnoringGroupBoxExCreateNone", new String[]{});
    assertMethodReturnType(getIgnoringGroupBoxExCreateNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$IgnoringGroupBoxExCreateNone");
    IMethod getIgnoringGroupBoxExNoneCreate = assertMethodExist(extendedExtendedFormData, "getIgnoringGroupBoxExNoneCreate", new String[]{});
    assertMethodReturnType(getIgnoringGroupBoxExNoneCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$IgnoringGroupBoxExNoneCreate");
    IMethod getNameExEx = assertMethodExist(extendedExtendedFormData, "getNameExEx", new String[]{});
    assertMethodReturnType(getNameExEx, "formdata.shared.services.process.replace.ExtendedExtendedFormData$NameExEx");
    IMethod getSdkCommandCreateCreateCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateCreate", new String[]{});
    assertMethodReturnType(getSdkCommandCreateCreateCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateCreate");
    IMethod getSdkCommandCreateCreateIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandCreateCreateIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateIgnore");
    IMethod getSdkCommandCreateCreateNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateNone", new String[]{});
    assertMethodReturnType(getSdkCommandCreateCreateNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateNone");
    IMethod getSdkCommandCreateCreateUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateUse", new String[]{});
    assertMethodReturnType(getSdkCommandCreateCreateUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateCreateUse");
    IMethod getSdkCommandCreateIgnoreCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreCreate", new String[]{});
    assertMethodReturnType(getSdkCommandCreateIgnoreCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreCreate");
    IMethod getSdkCommandCreateIgnoreIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandCreateIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreIgnore");
    IMethod getSdkCommandCreateIgnoreNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreNone", new String[]{});
    assertMethodReturnType(getSdkCommandCreateIgnoreNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreNone");
    IMethod getSdkCommandCreateIgnoreUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreUse", new String[]{});
    assertMethodReturnType(getSdkCommandCreateIgnoreUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateIgnoreUse");
    IMethod getSdkCommandCreateNoneCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneCreate", new String[]{});
    assertMethodReturnType(getSdkCommandCreateNoneCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneCreate");
    IMethod getSdkCommandCreateNoneIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandCreateNoneIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneIgnore");
    IMethod getSdkCommandCreateNoneNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneNone", new String[]{});
    assertMethodReturnType(getSdkCommandCreateNoneNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneNone");
    IMethod getSdkCommandCreateNoneUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneUse", new String[]{});
    assertMethodReturnType(getSdkCommandCreateNoneUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateNoneUse");
    IMethod getSdkCommandCreateUseCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseCreate", new String[]{});
    assertMethodReturnType(getSdkCommandCreateUseCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseCreate");
    IMethod getSdkCommandCreateUseIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandCreateUseIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseIgnore");
    IMethod getSdkCommandCreateUseNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseNone", new String[]{});
    assertMethodReturnType(getSdkCommandCreateUseNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseNone");
    IMethod getSdkCommandCreateUseUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseUse", new String[]{});
    assertMethodReturnType(getSdkCommandCreateUseUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandCreateUseUse");
    IMethod getSdkCommandIgnoreCreateCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateCreate", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreCreateCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateCreate");
    IMethod getSdkCommandIgnoreCreateIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreCreateIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateIgnore");
    IMethod getSdkCommandIgnoreCreateNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateNone", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreCreateNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateNone");
    IMethod getSdkCommandIgnoreCreateUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateUse", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreCreateUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreCreateUse");
    IMethod getSdkCommandIgnoreIgnoreCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreIgnoreCreate", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreIgnoreCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreIgnoreCreate");
    IMethod getSdkCommandIgnoreIgnoreUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreIgnoreUse", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreIgnoreUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreIgnoreUse");
    IMethod getSdkCommandIgnoreNoneCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreNoneCreate", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreNoneCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreNoneCreate");
    IMethod getSdkCommandIgnoreNoneUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreNoneUse", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreNoneUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreNoneUse");
    IMethod getSdkCommandIgnoreUseCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseCreate", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreUseCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseCreate");
    IMethod getSdkCommandIgnoreUseIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreUseIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseIgnore");
    IMethod getSdkCommandIgnoreUseNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseNone", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreUseNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseNone");
    IMethod getSdkCommandIgnoreUseUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseUse", new String[]{});
    assertMethodReturnType(getSdkCommandIgnoreUseUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandIgnoreUseUse");
    IMethod getSdkCommandNoneCreateCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateCreate", new String[]{});
    assertMethodReturnType(getSdkCommandNoneCreateCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateCreate");
    IMethod getSdkCommandNoneCreateIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandNoneCreateIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateIgnore");
    IMethod getSdkCommandNoneCreateNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateNone", new String[]{});
    assertMethodReturnType(getSdkCommandNoneCreateNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateNone");
    IMethod getSdkCommandNoneCreateUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateUse", new String[]{});
    assertMethodReturnType(getSdkCommandNoneCreateUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneCreateUse");
    IMethod getSdkCommandNoneIgnoreCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreCreate", new String[]{});
    assertMethodReturnType(getSdkCommandNoneIgnoreCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreCreate");
    IMethod getSdkCommandNoneIgnoreIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandNoneIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreIgnore");
    IMethod getSdkCommandNoneIgnoreNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreNone", new String[]{});
    assertMethodReturnType(getSdkCommandNoneIgnoreNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreNone");
    IMethod getSdkCommandNoneIgnoreUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreUse", new String[]{});
    assertMethodReturnType(getSdkCommandNoneIgnoreUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneIgnoreUse");
    IMethod getSdkCommandNoneNoneCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneCreate", new String[]{});
    assertMethodReturnType(getSdkCommandNoneNoneCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneCreate");
    IMethod getSdkCommandNoneNoneIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandNoneNoneIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneIgnore");
    IMethod getSdkCommandNoneNoneNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneNone", new String[]{});
    assertMethodReturnType(getSdkCommandNoneNoneNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneNone");
    IMethod getSdkCommandNoneNoneUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneUse", new String[]{});
    assertMethodReturnType(getSdkCommandNoneNoneUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneNoneUse");
    IMethod getSdkCommandNoneUseCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseCreate", new String[]{});
    assertMethodReturnType(getSdkCommandNoneUseCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseCreate");
    IMethod getSdkCommandNoneUseIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandNoneUseIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseIgnore");
    IMethod getSdkCommandNoneUseNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseNone", new String[]{});
    assertMethodReturnType(getSdkCommandNoneUseNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseNone");
    IMethod getSdkCommandNoneUseUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseUse", new String[]{});
    assertMethodReturnType(getSdkCommandNoneUseUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandNoneUseUse");
    IMethod getSdkCommandUseCreateCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateCreate", new String[]{});
    assertMethodReturnType(getSdkCommandUseCreateCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateCreate");
    IMethod getSdkCommandUseCreateIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandUseCreateIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateIgnore");
    IMethod getSdkCommandUseCreateNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateNone", new String[]{});
    assertMethodReturnType(getSdkCommandUseCreateNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateNone");
    IMethod getSdkCommandUseCreateUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateUse", new String[]{});
    assertMethodReturnType(getSdkCommandUseCreateUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseCreateUse");
    IMethod getSdkCommandUseIgnoreCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreCreate", new String[]{});
    assertMethodReturnType(getSdkCommandUseIgnoreCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreCreate");
    IMethod getSdkCommandUseIgnoreIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandUseIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreIgnore");
    IMethod getSdkCommandUseIgnoreNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreNone", new String[]{});
    assertMethodReturnType(getSdkCommandUseIgnoreNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreNone");
    IMethod getSdkCommandUseIgnoreUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreUse", new String[]{});
    assertMethodReturnType(getSdkCommandUseIgnoreUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseIgnoreUse");
    IMethod getSdkCommandUseNoneCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneCreate", new String[]{});
    assertMethodReturnType(getSdkCommandUseNoneCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneCreate");
    IMethod getSdkCommandUseNoneIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandUseNoneIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneIgnore");
    IMethod getSdkCommandUseNoneNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneNone", new String[]{});
    assertMethodReturnType(getSdkCommandUseNoneNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneNone");
    IMethod getSdkCommandUseNoneUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneUse", new String[]{});
    assertMethodReturnType(getSdkCommandUseNoneUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseNoneUse");
    IMethod getSdkCommandUseUseCreate = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseCreate", new String[]{});
    assertMethodReturnType(getSdkCommandUseUseCreate, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseCreate");
    IMethod getSdkCommandUseUseIgnore = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseIgnore", new String[]{});
    assertMethodReturnType(getSdkCommandUseUseIgnore, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseIgnore");
    IMethod getSdkCommandUseUseNone = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseNone", new String[]{});
    assertMethodReturnType(getSdkCommandUseUseNone, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseNone");
    IMethod getSdkCommandUseUseUse = assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseUse", new String[]{});
    assertMethodReturnType(getSdkCommandUseUseUse, "formdata.shared.services.process.replace.ExtendedExtendedFormData$SdkCommandUseUseUse");

    assertEquals(63, extendedExtendedFormData.innerTypes().stream().count(), "inner types count of 'ExtendedExtendedFormData'");
    // type IgnoringGroupBoxExCreateNone
    IType ignoringGroupBoxExCreateNone = assertTypeExists(extendedExtendedFormData, "IgnoringGroupBoxExCreateNone");
    assertHasFlags(ignoringGroupBoxExCreateNone, 9);
    assertHasSuperClass(ignoringGroupBoxExCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$IgnoringGroupBoxExCreate");
    assertAnnotation(ignoringGroupBoxExCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of IgnoringGroupBoxExCreateNone
    assertEquals(1, ignoringGroupBoxExCreateNone.fields().stream().count(), "field count of 'IgnoringGroupBoxExCreateNone'");
    IField serialVersionUID1 = assertFieldExist(ignoringGroupBoxExCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, ignoringGroupBoxExCreateNone.methods().stream().count(), "method count of 'IgnoringGroupBoxExCreateNone'");

    assertEquals(0, ignoringGroupBoxExCreateNone.innerTypes().stream().count(), "inner types count of 'IgnoringGroupBoxExCreateNone'");
    // type IgnoringGroupBoxExNoneCreate
    IType ignoringGroupBoxExNoneCreate = assertTypeExists(extendedExtendedFormData, "IgnoringGroupBoxExNoneCreate");
    assertHasFlags(ignoringGroupBoxExNoneCreate, 9);
    assertHasSuperClass(ignoringGroupBoxExNoneCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertAnnotation(ignoringGroupBoxExNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of IgnoringGroupBoxExNoneCreate
    assertEquals(1, ignoringGroupBoxExNoneCreate.fields().stream().count(), "field count of 'IgnoringGroupBoxExNoneCreate'");
    IField serialVersionUID2 = assertFieldExist(ignoringGroupBoxExNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, ignoringGroupBoxExNoneCreate.methods().stream().count(), "method count of 'IgnoringGroupBoxExNoneCreate'");

    assertEquals(0, ignoringGroupBoxExNoneCreate.innerTypes().stream().count(), "inner types count of 'IgnoringGroupBoxExNoneCreate'");
    // type NameExEx
    IType nameExEx = assertTypeExists(extendedExtendedFormData, "NameExEx");
    assertHasFlags(nameExEx, 9);
    assertHasSuperClass(nameExEx, "formdata.shared.services.process.replace.ExtendedFormData$NameEx");
    assertAnnotation(nameExEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of NameExEx
    assertEquals(1, nameExEx.fields().stream().count(), "field count of 'NameExEx'");
    IField serialVersionUID3 = assertFieldExist(nameExEx, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(3, nameExEx.methods().stream().count(), "method count of 'NameExEx'");
    IMethod getStringProperty = assertMethodExist(nameExEx, "getStringProperty", new String[]{});
    assertMethodReturnType(getStringProperty, String.class.getName());
    IMethod setStringProperty = assertMethodExist(nameExEx, "setStringProperty", new String[]{String.class.getName()});
    assertMethodReturnType(setStringProperty, "void");
    IMethod getStringPropertyProperty = assertMethodExist(nameExEx, "getStringPropertyProperty", new String[]{});
    assertMethodReturnType(getStringPropertyProperty, "formdata.shared.services.process.replace.ExtendedExtendedFormData$NameExEx$StringPropertyProperty");

    assertEquals(1, nameExEx.innerTypes().stream().count(), "inner types count of 'NameExEx'");
    // type StringPropertyProperty
    IType stringPropertyProperty = assertTypeExists(nameExEx, "StringPropertyProperty");
    assertHasFlags(stringPropertyProperty, 9);
    assertHasSuperClass(stringPropertyProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.String>");

    // fields of StringPropertyProperty
    assertEquals(1, stringPropertyProperty.fields().stream().count(), "field count of 'StringPropertyProperty'");
    IField serialVersionUID4 = assertFieldExist(stringPropertyProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(0, stringPropertyProperty.methods().stream().count(), "method count of 'StringPropertyProperty'");

    assertEquals(0, stringPropertyProperty.innerTypes().stream().count(), "inner types count of 'StringPropertyProperty'");
    // type SdkCommandCreateCreateCreate
    IType sdkCommandCreateCreateCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateCreate");
    assertHasFlags(sdkCommandCreateCreateCreate, 9);
    assertHasSuperClass(sdkCommandCreateCreateCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateCreate");
    assertAnnotation(sdkCommandCreateCreateCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateCreateCreate
    assertEquals(1, sdkCommandCreateCreateCreate.fields().stream().count(), "field count of 'SdkCommandCreateCreateCreate'");
    IField serialVersionUID5 = assertFieldExist(sdkCommandCreateCreateCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(0, sdkCommandCreateCreateCreate.methods().stream().count(), "method count of 'SdkCommandCreateCreateCreate'");

    assertEquals(0, sdkCommandCreateCreateCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateCreateCreate'");
    // type SdkCommandCreateCreateIgnore
    IType sdkCommandCreateCreateIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateIgnore");
    assertHasFlags(sdkCommandCreateCreateIgnore, 9);
    assertHasSuperClass(sdkCommandCreateCreateIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateCreate");
    assertAnnotation(sdkCommandCreateCreateIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateCreateIgnore
    assertEquals(1, sdkCommandCreateCreateIgnore.fields().stream().count(), "field count of 'SdkCommandCreateCreateIgnore'");
    IField serialVersionUID6 = assertFieldExist(sdkCommandCreateCreateIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");

    assertEquals(0, sdkCommandCreateCreateIgnore.methods().stream().count(), "method count of 'SdkCommandCreateCreateIgnore'");

    assertEquals(0, sdkCommandCreateCreateIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateCreateIgnore'");
    // type SdkCommandCreateCreateNone
    IType sdkCommandCreateCreateNone = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateNone");
    assertHasFlags(sdkCommandCreateCreateNone, 9);
    assertHasSuperClass(sdkCommandCreateCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateCreate");
    assertAnnotation(sdkCommandCreateCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateCreateNone
    assertEquals(1, sdkCommandCreateCreateNone.fields().stream().count(), "field count of 'SdkCommandCreateCreateNone'");
    IField serialVersionUID7 = assertFieldExist(sdkCommandCreateCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID7, 26);
    assertFieldType(serialVersionUID7, "long");

    assertEquals(0, sdkCommandCreateCreateNone.methods().stream().count(), "method count of 'SdkCommandCreateCreateNone'");

    assertEquals(0, sdkCommandCreateCreateNone.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateCreateNone'");
    // type SdkCommandCreateCreateUse
    IType sdkCommandCreateCreateUse = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateUse");
    assertHasFlags(sdkCommandCreateCreateUse, 9);
    assertHasSuperClass(sdkCommandCreateCreateUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateCreate");
    assertAnnotation(sdkCommandCreateCreateUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateCreateUse
    assertEquals(1, sdkCommandCreateCreateUse.fields().stream().count(), "field count of 'SdkCommandCreateCreateUse'");
    IField serialVersionUID8 = assertFieldExist(sdkCommandCreateCreateUse, "serialVersionUID");
    assertHasFlags(serialVersionUID8, 26);
    assertFieldType(serialVersionUID8, "long");

    assertEquals(0, sdkCommandCreateCreateUse.methods().stream().count(), "method count of 'SdkCommandCreateCreateUse'");

    assertEquals(0, sdkCommandCreateCreateUse.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateCreateUse'");
    // type SdkCommandCreateIgnoreCreate
    IType sdkCommandCreateIgnoreCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreCreate");
    assertHasFlags(sdkCommandCreateIgnoreCreate, 9);
    assertHasSuperClass(sdkCommandCreateIgnoreCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateIgnore");
    assertAnnotation(sdkCommandCreateIgnoreCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateIgnoreCreate
    assertEquals(1, sdkCommandCreateIgnoreCreate.fields().stream().count(), "field count of 'SdkCommandCreateIgnoreCreate'");
    IField serialVersionUID9 = assertFieldExist(sdkCommandCreateIgnoreCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID9, 26);
    assertFieldType(serialVersionUID9, "long");

    assertEquals(0, sdkCommandCreateIgnoreCreate.methods().stream().count(), "method count of 'SdkCommandCreateIgnoreCreate'");

    assertEquals(0, sdkCommandCreateIgnoreCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateIgnoreCreate'");
    // type SdkCommandCreateIgnoreIgnore
    IType sdkCommandCreateIgnoreIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreIgnore");
    assertHasFlags(sdkCommandCreateIgnoreIgnore, 9);
    assertHasSuperClass(sdkCommandCreateIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateIgnore");
    assertAnnotation(sdkCommandCreateIgnoreIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateIgnoreIgnore
    assertEquals(1, sdkCommandCreateIgnoreIgnore.fields().stream().count(), "field count of 'SdkCommandCreateIgnoreIgnore'");
    IField serialVersionUID10 = assertFieldExist(sdkCommandCreateIgnoreIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID10, 26);
    assertFieldType(serialVersionUID10, "long");

    assertEquals(0, sdkCommandCreateIgnoreIgnore.methods().stream().count(), "method count of 'SdkCommandCreateIgnoreIgnore'");

    assertEquals(0, sdkCommandCreateIgnoreIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateIgnoreIgnore'");
    // type SdkCommandCreateIgnoreNone
    IType sdkCommandCreateIgnoreNone = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreNone");
    assertHasFlags(sdkCommandCreateIgnoreNone, 9);
    assertHasSuperClass(sdkCommandCreateIgnoreNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateIgnore");
    assertAnnotation(sdkCommandCreateIgnoreNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateIgnoreNone
    assertEquals(1, sdkCommandCreateIgnoreNone.fields().stream().count(), "field count of 'SdkCommandCreateIgnoreNone'");
    IField serialVersionUID11 = assertFieldExist(sdkCommandCreateIgnoreNone, "serialVersionUID");
    assertHasFlags(serialVersionUID11, 26);
    assertFieldType(serialVersionUID11, "long");

    assertEquals(0, sdkCommandCreateIgnoreNone.methods().stream().count(), "method count of 'SdkCommandCreateIgnoreNone'");

    assertEquals(0, sdkCommandCreateIgnoreNone.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateIgnoreNone'");
    // type SdkCommandCreateIgnoreUse
    IType sdkCommandCreateIgnoreUse = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreUse");
    assertHasFlags(sdkCommandCreateIgnoreUse, 9);
    assertHasSuperClass(sdkCommandCreateIgnoreUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateIgnore");
    assertAnnotation(sdkCommandCreateIgnoreUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateIgnoreUse
    assertEquals(1, sdkCommandCreateIgnoreUse.fields().stream().count(), "field count of 'SdkCommandCreateIgnoreUse'");
    IField serialVersionUID12 = assertFieldExist(sdkCommandCreateIgnoreUse, "serialVersionUID");
    assertHasFlags(serialVersionUID12, 26);
    assertFieldType(serialVersionUID12, "long");

    assertEquals(0, sdkCommandCreateIgnoreUse.methods().stream().count(), "method count of 'SdkCommandCreateIgnoreUse'");

    assertEquals(0, sdkCommandCreateIgnoreUse.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateIgnoreUse'");
    // type SdkCommandCreateNoneCreate
    IType sdkCommandCreateNoneCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneCreate");
    assertHasFlags(sdkCommandCreateNoneCreate, 9);
    assertHasSuperClass(sdkCommandCreateNoneCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateNone");
    assertAnnotation(sdkCommandCreateNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateNoneCreate
    assertEquals(1, sdkCommandCreateNoneCreate.fields().stream().count(), "field count of 'SdkCommandCreateNoneCreate'");
    IField serialVersionUID13 = assertFieldExist(sdkCommandCreateNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID13, 26);
    assertFieldType(serialVersionUID13, "long");

    assertEquals(0, sdkCommandCreateNoneCreate.methods().stream().count(), "method count of 'SdkCommandCreateNoneCreate'");

    assertEquals(0, sdkCommandCreateNoneCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateNoneCreate'");
    // type SdkCommandCreateNoneIgnore
    IType sdkCommandCreateNoneIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneIgnore");
    assertHasFlags(sdkCommandCreateNoneIgnore, 9);
    assertHasSuperClass(sdkCommandCreateNoneIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateNone");
    assertAnnotation(sdkCommandCreateNoneIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateNoneIgnore
    assertEquals(1, sdkCommandCreateNoneIgnore.fields().stream().count(), "field count of 'SdkCommandCreateNoneIgnore'");
    IField serialVersionUID14 = assertFieldExist(sdkCommandCreateNoneIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID14, 26);
    assertFieldType(serialVersionUID14, "long");

    assertEquals(0, sdkCommandCreateNoneIgnore.methods().stream().count(), "method count of 'SdkCommandCreateNoneIgnore'");

    assertEquals(0, sdkCommandCreateNoneIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateNoneIgnore'");
    // type SdkCommandCreateNoneNone
    IType sdkCommandCreateNoneNone = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneNone");
    assertHasFlags(sdkCommandCreateNoneNone, 9);
    assertHasSuperClass(sdkCommandCreateNoneNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateNone");
    assertAnnotation(sdkCommandCreateNoneNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateNoneNone
    assertEquals(1, sdkCommandCreateNoneNone.fields().stream().count(), "field count of 'SdkCommandCreateNoneNone'");
    IField serialVersionUID15 = assertFieldExist(sdkCommandCreateNoneNone, "serialVersionUID");
    assertHasFlags(serialVersionUID15, 26);
    assertFieldType(serialVersionUID15, "long");

    assertEquals(0, sdkCommandCreateNoneNone.methods().stream().count(), "method count of 'SdkCommandCreateNoneNone'");

    assertEquals(0, sdkCommandCreateNoneNone.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateNoneNone'");
    // type SdkCommandCreateNoneUse
    IType sdkCommandCreateNoneUse = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneUse");
    assertHasFlags(sdkCommandCreateNoneUse, 9);
    assertHasSuperClass(sdkCommandCreateNoneUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateNone");
    assertAnnotation(sdkCommandCreateNoneUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateNoneUse
    assertEquals(1, sdkCommandCreateNoneUse.fields().stream().count(), "field count of 'SdkCommandCreateNoneUse'");
    IField serialVersionUID16 = assertFieldExist(sdkCommandCreateNoneUse, "serialVersionUID");
    assertHasFlags(serialVersionUID16, 26);
    assertFieldType(serialVersionUID16, "long");

    assertEquals(0, sdkCommandCreateNoneUse.methods().stream().count(), "method count of 'SdkCommandCreateNoneUse'");

    assertEquals(0, sdkCommandCreateNoneUse.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateNoneUse'");
    // type SdkCommandCreateUseCreate
    IType sdkCommandCreateUseCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseCreate");
    assertHasFlags(sdkCommandCreateUseCreate, 9);
    assertHasSuperClass(sdkCommandCreateUseCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateUse");
    assertAnnotation(sdkCommandCreateUseCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateUseCreate
    assertEquals(1, sdkCommandCreateUseCreate.fields().stream().count(), "field count of 'SdkCommandCreateUseCreate'");
    IField serialVersionUID17 = assertFieldExist(sdkCommandCreateUseCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID17, 26);
    assertFieldType(serialVersionUID17, "long");

    assertEquals(0, sdkCommandCreateUseCreate.methods().stream().count(), "method count of 'SdkCommandCreateUseCreate'");

    assertEquals(0, sdkCommandCreateUseCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateUseCreate'");
    // type SdkCommandCreateUseIgnore
    IType sdkCommandCreateUseIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseIgnore");
    assertHasFlags(sdkCommandCreateUseIgnore, 9);
    assertHasSuperClass(sdkCommandCreateUseIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateUse");
    assertAnnotation(sdkCommandCreateUseIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateUseIgnore
    assertEquals(1, sdkCommandCreateUseIgnore.fields().stream().count(), "field count of 'SdkCommandCreateUseIgnore'");
    IField serialVersionUID18 = assertFieldExist(sdkCommandCreateUseIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID18, 26);
    assertFieldType(serialVersionUID18, "long");

    assertEquals(0, sdkCommandCreateUseIgnore.methods().stream().count(), "method count of 'SdkCommandCreateUseIgnore'");

    assertEquals(0, sdkCommandCreateUseIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateUseIgnore'");
    // type SdkCommandCreateUseNone
    IType sdkCommandCreateUseNone = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseNone");
    assertHasFlags(sdkCommandCreateUseNone, 9);
    assertHasSuperClass(sdkCommandCreateUseNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateUse");
    assertAnnotation(sdkCommandCreateUseNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateUseNone
    assertEquals(1, sdkCommandCreateUseNone.fields().stream().count(), "field count of 'SdkCommandCreateUseNone'");
    IField serialVersionUID19 = assertFieldExist(sdkCommandCreateUseNone, "serialVersionUID");
    assertHasFlags(serialVersionUID19, 26);
    assertFieldType(serialVersionUID19, "long");

    assertEquals(0, sdkCommandCreateUseNone.methods().stream().count(), "method count of 'SdkCommandCreateUseNone'");

    assertEquals(0, sdkCommandCreateUseNone.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateUseNone'");
    // type SdkCommandCreateUseUse
    IType sdkCommandCreateUseUse = assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseUse");
    assertHasFlags(sdkCommandCreateUseUse, 9);
    assertHasSuperClass(sdkCommandCreateUseUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandCreateUse");
    assertAnnotation(sdkCommandCreateUseUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandCreateUseUse
    assertEquals(1, sdkCommandCreateUseUse.fields().stream().count(), "field count of 'SdkCommandCreateUseUse'");
    IField serialVersionUID20 = assertFieldExist(sdkCommandCreateUseUse, "serialVersionUID");
    assertHasFlags(serialVersionUID20, 26);
    assertFieldType(serialVersionUID20, "long");

    assertEquals(0, sdkCommandCreateUseUse.methods().stream().count(), "method count of 'SdkCommandCreateUseUse'");

    assertEquals(0, sdkCommandCreateUseUse.innerTypes().stream().count(), "inner types count of 'SdkCommandCreateUseUse'");
    // type SdkCommandIgnoreCreateCreate
    IType sdkCommandIgnoreCreateCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateCreate");
    assertHasFlags(sdkCommandIgnoreCreateCreate, 9);
    assertHasSuperClass(sdkCommandIgnoreCreateCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreCreate");
    assertAnnotation(sdkCommandIgnoreCreateCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreCreateCreate
    assertEquals(1, sdkCommandIgnoreCreateCreate.fields().stream().count(), "field count of 'SdkCommandIgnoreCreateCreate'");
    IField serialVersionUID21 = assertFieldExist(sdkCommandIgnoreCreateCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID21, 26);
    assertFieldType(serialVersionUID21, "long");

    assertEquals(0, sdkCommandIgnoreCreateCreate.methods().stream().count(), "method count of 'SdkCommandIgnoreCreateCreate'");

    assertEquals(0, sdkCommandIgnoreCreateCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreCreateCreate'");
    // type SdkCommandIgnoreCreateIgnore
    IType sdkCommandIgnoreCreateIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateIgnore");
    assertHasFlags(sdkCommandIgnoreCreateIgnore, 9);
    assertHasSuperClass(sdkCommandIgnoreCreateIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreCreate");
    assertAnnotation(sdkCommandIgnoreCreateIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreCreateIgnore
    assertEquals(1, sdkCommandIgnoreCreateIgnore.fields().stream().count(), "field count of 'SdkCommandIgnoreCreateIgnore'");
    IField serialVersionUID22 = assertFieldExist(sdkCommandIgnoreCreateIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID22, 26);
    assertFieldType(serialVersionUID22, "long");

    assertEquals(0, sdkCommandIgnoreCreateIgnore.methods().stream().count(), "method count of 'SdkCommandIgnoreCreateIgnore'");

    assertEquals(0, sdkCommandIgnoreCreateIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreCreateIgnore'");
    // type SdkCommandIgnoreCreateNone
    IType sdkCommandIgnoreCreateNone = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateNone");
    assertHasFlags(sdkCommandIgnoreCreateNone, 9);
    assertHasSuperClass(sdkCommandIgnoreCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreCreate");
    assertAnnotation(sdkCommandIgnoreCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreCreateNone
    assertEquals(1, sdkCommandIgnoreCreateNone.fields().stream().count(), "field count of 'SdkCommandIgnoreCreateNone'");
    IField serialVersionUID23 = assertFieldExist(sdkCommandIgnoreCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID23, 26);
    assertFieldType(serialVersionUID23, "long");

    assertEquals(0, sdkCommandIgnoreCreateNone.methods().stream().count(), "method count of 'SdkCommandIgnoreCreateNone'");

    assertEquals(0, sdkCommandIgnoreCreateNone.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreCreateNone'");
    // type SdkCommandIgnoreCreateUse
    IType sdkCommandIgnoreCreateUse = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateUse");
    assertHasFlags(sdkCommandIgnoreCreateUse, 9);
    assertHasSuperClass(sdkCommandIgnoreCreateUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreCreate");
    assertAnnotation(sdkCommandIgnoreCreateUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreCreateUse
    assertEquals(1, sdkCommandIgnoreCreateUse.fields().stream().count(), "field count of 'SdkCommandIgnoreCreateUse'");
    IField serialVersionUID24 = assertFieldExist(sdkCommandIgnoreCreateUse, "serialVersionUID");
    assertHasFlags(serialVersionUID24, 26);
    assertFieldType(serialVersionUID24, "long");

    assertEquals(0, sdkCommandIgnoreCreateUse.methods().stream().count(), "method count of 'SdkCommandIgnoreCreateUse'");

    assertEquals(0, sdkCommandIgnoreCreateUse.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreCreateUse'");
    // type SdkCommandIgnoreIgnoreCreate
    IType sdkCommandIgnoreIgnoreCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreIgnoreCreate");
    assertHasFlags(sdkCommandIgnoreIgnoreCreate, 9);
    assertHasSuperClass(sdkCommandIgnoreIgnoreCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertAnnotation(sdkCommandIgnoreIgnoreCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreIgnoreCreate
    assertEquals(1, sdkCommandIgnoreIgnoreCreate.fields().stream().count(), "field count of 'SdkCommandIgnoreIgnoreCreate'");
    IField serialVersionUID25 = assertFieldExist(sdkCommandIgnoreIgnoreCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID25, 26);
    assertFieldType(serialVersionUID25, "long");

    assertEquals(0, sdkCommandIgnoreIgnoreCreate.methods().stream().count(), "method count of 'SdkCommandIgnoreIgnoreCreate'");

    assertEquals(0, sdkCommandIgnoreIgnoreCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreIgnoreCreate'");
    // type SdkCommandIgnoreIgnoreUse
    IType sdkCommandIgnoreIgnoreUse = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreIgnoreUse");
    assertHasFlags(sdkCommandIgnoreIgnoreUse, 9);
    assertHasSuperClass(sdkCommandIgnoreIgnoreUse, "formdata.shared.services.process.replace.UsingFormFieldData");
    assertAnnotation(sdkCommandIgnoreIgnoreUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreIgnoreUse
    assertEquals(1, sdkCommandIgnoreIgnoreUse.fields().stream().count(), "field count of 'SdkCommandIgnoreIgnoreUse'");
    IField serialVersionUID26 = assertFieldExist(sdkCommandIgnoreIgnoreUse, "serialVersionUID");
    assertHasFlags(serialVersionUID26, 26);
    assertFieldType(serialVersionUID26, "long");

    assertEquals(0, sdkCommandIgnoreIgnoreUse.methods().stream().count(), "method count of 'SdkCommandIgnoreIgnoreUse'");

    assertEquals(0, sdkCommandIgnoreIgnoreUse.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreIgnoreUse'");
    // type SdkCommandIgnoreNoneCreate
    IType sdkCommandIgnoreNoneCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreNoneCreate");
    assertHasFlags(sdkCommandIgnoreNoneCreate, 9);
    assertHasSuperClass(sdkCommandIgnoreNoneCreate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertAnnotation(sdkCommandIgnoreNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreNoneCreate
    assertEquals(1, sdkCommandIgnoreNoneCreate.fields().stream().count(), "field count of 'SdkCommandIgnoreNoneCreate'");
    IField serialVersionUID27 = assertFieldExist(sdkCommandIgnoreNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID27, 26);
    assertFieldType(serialVersionUID27, "long");

    assertEquals(0, sdkCommandIgnoreNoneCreate.methods().stream().count(), "method count of 'SdkCommandIgnoreNoneCreate'");

    assertEquals(0, sdkCommandIgnoreNoneCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreNoneCreate'");
    // type SdkCommandIgnoreNoneUse
    IType sdkCommandIgnoreNoneUse = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreNoneUse");
    assertHasFlags(sdkCommandIgnoreNoneUse, 9);
    assertHasSuperClass(sdkCommandIgnoreNoneUse, "formdata.shared.services.process.replace.UsingFormFieldData");
    assertAnnotation(sdkCommandIgnoreNoneUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreNoneUse
    assertEquals(1, sdkCommandIgnoreNoneUse.fields().stream().count(), "field count of 'SdkCommandIgnoreNoneUse'");
    IField serialVersionUID28 = assertFieldExist(sdkCommandIgnoreNoneUse, "serialVersionUID");
    assertHasFlags(serialVersionUID28, 26);
    assertFieldType(serialVersionUID28, "long");

    assertEquals(0, sdkCommandIgnoreNoneUse.methods().stream().count(), "method count of 'SdkCommandIgnoreNoneUse'");

    assertEquals(0, sdkCommandIgnoreNoneUse.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreNoneUse'");
    // type SdkCommandIgnoreUseCreate
    IType sdkCommandIgnoreUseCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseCreate");
    assertHasFlags(sdkCommandIgnoreUseCreate, 9);
    assertHasSuperClass(sdkCommandIgnoreUseCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreUse");
    assertAnnotation(sdkCommandIgnoreUseCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreUseCreate
    assertEquals(1, sdkCommandIgnoreUseCreate.fields().stream().count(), "field count of 'SdkCommandIgnoreUseCreate'");
    IField serialVersionUID29 = assertFieldExist(sdkCommandIgnoreUseCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID29, 26);
    assertFieldType(serialVersionUID29, "long");

    assertEquals(0, sdkCommandIgnoreUseCreate.methods().stream().count(), "method count of 'SdkCommandIgnoreUseCreate'");

    assertEquals(0, sdkCommandIgnoreUseCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreUseCreate'");
    // type SdkCommandIgnoreUseIgnore
    IType sdkCommandIgnoreUseIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseIgnore");
    assertHasFlags(sdkCommandIgnoreUseIgnore, 9);
    assertHasSuperClass(sdkCommandIgnoreUseIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreUse");
    assertAnnotation(sdkCommandIgnoreUseIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreUseIgnore
    assertEquals(1, sdkCommandIgnoreUseIgnore.fields().stream().count(), "field count of 'SdkCommandIgnoreUseIgnore'");
    IField serialVersionUID30 = assertFieldExist(sdkCommandIgnoreUseIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID30, 26);
    assertFieldType(serialVersionUID30, "long");

    assertEquals(0, sdkCommandIgnoreUseIgnore.methods().stream().count(), "method count of 'SdkCommandIgnoreUseIgnore'");

    assertEquals(0, sdkCommandIgnoreUseIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreUseIgnore'");
    // type SdkCommandIgnoreUseNone
    IType sdkCommandIgnoreUseNone = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseNone");
    assertHasFlags(sdkCommandIgnoreUseNone, 9);
    assertHasSuperClass(sdkCommandIgnoreUseNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreUse");
    assertAnnotation(sdkCommandIgnoreUseNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreUseNone
    assertEquals(1, sdkCommandIgnoreUseNone.fields().stream().count(), "field count of 'SdkCommandIgnoreUseNone'");
    IField serialVersionUID31 = assertFieldExist(sdkCommandIgnoreUseNone, "serialVersionUID");
    assertHasFlags(serialVersionUID31, 26);
    assertFieldType(serialVersionUID31, "long");

    assertEquals(0, sdkCommandIgnoreUseNone.methods().stream().count(), "method count of 'SdkCommandIgnoreUseNone'");

    assertEquals(0, sdkCommandIgnoreUseNone.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreUseNone'");
    // type SdkCommandIgnoreUseUse
    IType sdkCommandIgnoreUseUse = assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseUse");
    assertHasFlags(sdkCommandIgnoreUseUse, 9);
    assertHasSuperClass(sdkCommandIgnoreUseUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandIgnoreUse");
    assertAnnotation(sdkCommandIgnoreUseUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandIgnoreUseUse
    assertEquals(1, sdkCommandIgnoreUseUse.fields().stream().count(), "field count of 'SdkCommandIgnoreUseUse'");
    IField serialVersionUID32 = assertFieldExist(sdkCommandIgnoreUseUse, "serialVersionUID");
    assertHasFlags(serialVersionUID32, 26);
    assertFieldType(serialVersionUID32, "long");

    assertEquals(0, sdkCommandIgnoreUseUse.methods().stream().count(), "method count of 'SdkCommandIgnoreUseUse'");

    assertEquals(0, sdkCommandIgnoreUseUse.innerTypes().stream().count(), "inner types count of 'SdkCommandIgnoreUseUse'");
    // type SdkCommandNoneCreateCreate
    IType sdkCommandNoneCreateCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateCreate");
    assertHasFlags(sdkCommandNoneCreateCreate, 9);
    assertHasSuperClass(sdkCommandNoneCreateCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneCreate");
    assertAnnotation(sdkCommandNoneCreateCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneCreateCreate
    assertEquals(1, sdkCommandNoneCreateCreate.fields().stream().count(), "field count of 'SdkCommandNoneCreateCreate'");
    IField serialVersionUID33 = assertFieldExist(sdkCommandNoneCreateCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID33, 26);
    assertFieldType(serialVersionUID33, "long");

    assertEquals(0, sdkCommandNoneCreateCreate.methods().stream().count(), "method count of 'SdkCommandNoneCreateCreate'");

    assertEquals(0, sdkCommandNoneCreateCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneCreateCreate'");
    // type SdkCommandNoneCreateIgnore
    IType sdkCommandNoneCreateIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateIgnore");
    assertHasFlags(sdkCommandNoneCreateIgnore, 9);
    assertHasSuperClass(sdkCommandNoneCreateIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneCreate");
    assertAnnotation(sdkCommandNoneCreateIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneCreateIgnore
    assertEquals(1, sdkCommandNoneCreateIgnore.fields().stream().count(), "field count of 'SdkCommandNoneCreateIgnore'");
    IField serialVersionUID34 = assertFieldExist(sdkCommandNoneCreateIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID34, 26);
    assertFieldType(serialVersionUID34, "long");

    assertEquals(0, sdkCommandNoneCreateIgnore.methods().stream().count(), "method count of 'SdkCommandNoneCreateIgnore'");

    assertEquals(0, sdkCommandNoneCreateIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneCreateIgnore'");
    // type SdkCommandNoneCreateNone
    IType sdkCommandNoneCreateNone = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateNone");
    assertHasFlags(sdkCommandNoneCreateNone, 9);
    assertHasSuperClass(sdkCommandNoneCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneCreate");
    assertAnnotation(sdkCommandNoneCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneCreateNone
    assertEquals(1, sdkCommandNoneCreateNone.fields().stream().count(), "field count of 'SdkCommandNoneCreateNone'");
    IField serialVersionUID35 = assertFieldExist(sdkCommandNoneCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID35, 26);
    assertFieldType(serialVersionUID35, "long");

    assertEquals(0, sdkCommandNoneCreateNone.methods().stream().count(), "method count of 'SdkCommandNoneCreateNone'");

    assertEquals(0, sdkCommandNoneCreateNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneCreateNone'");
    // type SdkCommandNoneCreateUse
    IType sdkCommandNoneCreateUse = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateUse");
    assertHasFlags(sdkCommandNoneCreateUse, 9);
    assertHasSuperClass(sdkCommandNoneCreateUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneCreate");
    assertAnnotation(sdkCommandNoneCreateUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneCreateUse
    assertEquals(1, sdkCommandNoneCreateUse.fields().stream().count(), "field count of 'SdkCommandNoneCreateUse'");
    IField serialVersionUID36 = assertFieldExist(sdkCommandNoneCreateUse, "serialVersionUID");
    assertHasFlags(serialVersionUID36, 26);
    assertFieldType(serialVersionUID36, "long");

    assertEquals(0, sdkCommandNoneCreateUse.methods().stream().count(), "method count of 'SdkCommandNoneCreateUse'");

    assertEquals(0, sdkCommandNoneCreateUse.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneCreateUse'");
    // type SdkCommandNoneIgnoreCreate
    IType sdkCommandNoneIgnoreCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreCreate");
    assertHasFlags(sdkCommandNoneIgnoreCreate, 9);
    assertHasSuperClass(sdkCommandNoneIgnoreCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneIgnore");
    assertAnnotation(sdkCommandNoneIgnoreCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneIgnoreCreate
    assertEquals(1, sdkCommandNoneIgnoreCreate.fields().stream().count(), "field count of 'SdkCommandNoneIgnoreCreate'");
    IField serialVersionUID37 = assertFieldExist(sdkCommandNoneIgnoreCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID37, 26);
    assertFieldType(serialVersionUID37, "long");

    assertEquals(0, sdkCommandNoneIgnoreCreate.methods().stream().count(), "method count of 'SdkCommandNoneIgnoreCreate'");

    assertEquals(0, sdkCommandNoneIgnoreCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneIgnoreCreate'");
    // type SdkCommandNoneIgnoreIgnore
    IType sdkCommandNoneIgnoreIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreIgnore");
    assertHasFlags(sdkCommandNoneIgnoreIgnore, 9);
    assertHasSuperClass(sdkCommandNoneIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneIgnore");
    assertAnnotation(sdkCommandNoneIgnoreIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneIgnoreIgnore
    assertEquals(1, sdkCommandNoneIgnoreIgnore.fields().stream().count(), "field count of 'SdkCommandNoneIgnoreIgnore'");
    IField serialVersionUID38 = assertFieldExist(sdkCommandNoneIgnoreIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID38, 26);
    assertFieldType(serialVersionUID38, "long");

    assertEquals(0, sdkCommandNoneIgnoreIgnore.methods().stream().count(), "method count of 'SdkCommandNoneIgnoreIgnore'");

    assertEquals(0, sdkCommandNoneIgnoreIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneIgnoreIgnore'");
    // type SdkCommandNoneIgnoreNone
    IType sdkCommandNoneIgnoreNone = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreNone");
    assertHasFlags(sdkCommandNoneIgnoreNone, 9);
    assertHasSuperClass(sdkCommandNoneIgnoreNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneIgnore");
    assertAnnotation(sdkCommandNoneIgnoreNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneIgnoreNone
    assertEquals(1, sdkCommandNoneIgnoreNone.fields().stream().count(), "field count of 'SdkCommandNoneIgnoreNone'");
    IField serialVersionUID39 = assertFieldExist(sdkCommandNoneIgnoreNone, "serialVersionUID");
    assertHasFlags(serialVersionUID39, 26);
    assertFieldType(serialVersionUID39, "long");

    assertEquals(0, sdkCommandNoneIgnoreNone.methods().stream().count(), "method count of 'SdkCommandNoneIgnoreNone'");

    assertEquals(0, sdkCommandNoneIgnoreNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneIgnoreNone'");
    // type SdkCommandNoneIgnoreUse
    IType sdkCommandNoneIgnoreUse = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreUse");
    assertHasFlags(sdkCommandNoneIgnoreUse, 9);
    assertHasSuperClass(sdkCommandNoneIgnoreUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneIgnore");
    assertAnnotation(sdkCommandNoneIgnoreUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneIgnoreUse
    assertEquals(1, sdkCommandNoneIgnoreUse.fields().stream().count(), "field count of 'SdkCommandNoneIgnoreUse'");
    IField serialVersionUID40 = assertFieldExist(sdkCommandNoneIgnoreUse, "serialVersionUID");
    assertHasFlags(serialVersionUID40, 26);
    assertFieldType(serialVersionUID40, "long");

    assertEquals(0, sdkCommandNoneIgnoreUse.methods().stream().count(), "method count of 'SdkCommandNoneIgnoreUse'");

    assertEquals(0, sdkCommandNoneIgnoreUse.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneIgnoreUse'");
    // type SdkCommandNoneNoneCreate
    IType sdkCommandNoneNoneCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneCreate");
    assertHasFlags(sdkCommandNoneNoneCreate, 9);
    assertHasSuperClass(sdkCommandNoneNoneCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneNone");
    assertHasSuperInterfaces(sdkCommandNoneNoneCreate, new String[]{"formdata.shared.IFormDataInterface03"});
    assertAnnotation(sdkCommandNoneNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneNoneCreate
    assertEquals(1, sdkCommandNoneNoneCreate.fields().stream().count(), "field count of 'SdkCommandNoneNoneCreate'");
    IField serialVersionUID41 = assertFieldExist(sdkCommandNoneNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID41, 26);
    assertFieldType(serialVersionUID41, "long");

    assertEquals(0, sdkCommandNoneNoneCreate.methods().stream().count(), "method count of 'SdkCommandNoneNoneCreate'");

    assertEquals(0, sdkCommandNoneNoneCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneNoneCreate'");
    // type SdkCommandNoneNoneIgnore
    IType sdkCommandNoneNoneIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneIgnore");
    assertHasFlags(sdkCommandNoneNoneIgnore, 9);
    assertHasSuperClass(sdkCommandNoneNoneIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneNone");
    assertAnnotation(sdkCommandNoneNoneIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneNoneIgnore
    assertEquals(1, sdkCommandNoneNoneIgnore.fields().stream().count(), "field count of 'SdkCommandNoneNoneIgnore'");
    IField serialVersionUID42 = assertFieldExist(sdkCommandNoneNoneIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID42, 26);
    assertFieldType(serialVersionUID42, "long");

    assertEquals(0, sdkCommandNoneNoneIgnore.methods().stream().count(), "method count of 'SdkCommandNoneNoneIgnore'");

    assertEquals(0, sdkCommandNoneNoneIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneNoneIgnore'");
    // type SdkCommandNoneNoneNone
    IType sdkCommandNoneNoneNone = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneNone");
    assertHasFlags(sdkCommandNoneNoneNone, 9);
    assertHasSuperClass(sdkCommandNoneNoneNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneNone");
    assertAnnotation(sdkCommandNoneNoneNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneNoneNone
    assertEquals(1, sdkCommandNoneNoneNone.fields().stream().count(), "field count of 'SdkCommandNoneNoneNone'");
    IField serialVersionUID43 = assertFieldExist(sdkCommandNoneNoneNone, "serialVersionUID");
    assertHasFlags(serialVersionUID43, 26);
    assertFieldType(serialVersionUID43, "long");

    assertEquals(0, sdkCommandNoneNoneNone.methods().stream().count(), "method count of 'SdkCommandNoneNoneNone'");

    assertEquals(0, sdkCommandNoneNoneNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneNoneNone'");
    // type SdkCommandNoneNoneUse
    IType sdkCommandNoneNoneUse = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneUse");
    assertHasFlags(sdkCommandNoneNoneUse, 9);
    assertHasSuperClass(sdkCommandNoneNoneUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneNone");
    assertAnnotation(sdkCommandNoneNoneUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneNoneUse
    assertEquals(1, sdkCommandNoneNoneUse.fields().stream().count(), "field count of 'SdkCommandNoneNoneUse'");
    IField serialVersionUID44 = assertFieldExist(sdkCommandNoneNoneUse, "serialVersionUID");
    assertHasFlags(serialVersionUID44, 26);
    assertFieldType(serialVersionUID44, "long");

    assertEquals(0, sdkCommandNoneNoneUse.methods().stream().count(), "method count of 'SdkCommandNoneNoneUse'");

    assertEquals(0, sdkCommandNoneNoneUse.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneNoneUse'");
    // type SdkCommandNoneUseCreate
    IType sdkCommandNoneUseCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseCreate");
    assertHasFlags(sdkCommandNoneUseCreate, 9);
    assertHasSuperClass(sdkCommandNoneUseCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneUse");
    assertAnnotation(sdkCommandNoneUseCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneUseCreate
    assertEquals(1, sdkCommandNoneUseCreate.fields().stream().count(), "field count of 'SdkCommandNoneUseCreate'");
    IField serialVersionUID45 = assertFieldExist(sdkCommandNoneUseCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID45, 26);
    assertFieldType(serialVersionUID45, "long");

    assertEquals(0, sdkCommandNoneUseCreate.methods().stream().count(), "method count of 'SdkCommandNoneUseCreate'");

    assertEquals(0, sdkCommandNoneUseCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneUseCreate'");
    // type SdkCommandNoneUseIgnore
    IType sdkCommandNoneUseIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseIgnore");
    assertHasFlags(sdkCommandNoneUseIgnore, 9);
    assertHasSuperClass(sdkCommandNoneUseIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneUse");
    assertAnnotation(sdkCommandNoneUseIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneUseIgnore
    assertEquals(1, sdkCommandNoneUseIgnore.fields().stream().count(), "field count of 'SdkCommandNoneUseIgnore'");
    IField serialVersionUID46 = assertFieldExist(sdkCommandNoneUseIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID46, 26);
    assertFieldType(serialVersionUID46, "long");

    assertEquals(0, sdkCommandNoneUseIgnore.methods().stream().count(), "method count of 'SdkCommandNoneUseIgnore'");

    assertEquals(0, sdkCommandNoneUseIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneUseIgnore'");
    // type SdkCommandNoneUseNone
    IType sdkCommandNoneUseNone = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseNone");
    assertHasFlags(sdkCommandNoneUseNone, 9);
    assertHasSuperClass(sdkCommandNoneUseNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneUse");
    assertAnnotation(sdkCommandNoneUseNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneUseNone
    assertEquals(1, sdkCommandNoneUseNone.fields().stream().count(), "field count of 'SdkCommandNoneUseNone'");
    IField serialVersionUID47 = assertFieldExist(sdkCommandNoneUseNone, "serialVersionUID");
    assertHasFlags(serialVersionUID47, 26);
    assertFieldType(serialVersionUID47, "long");

    assertEquals(0, sdkCommandNoneUseNone.methods().stream().count(), "method count of 'SdkCommandNoneUseNone'");

    assertEquals(0, sdkCommandNoneUseNone.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneUseNone'");
    // type SdkCommandNoneUseUse
    IType sdkCommandNoneUseUse = assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseUse");
    assertHasFlags(sdkCommandNoneUseUse, 9);
    assertHasSuperClass(sdkCommandNoneUseUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandNoneUse");
    assertAnnotation(sdkCommandNoneUseUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandNoneUseUse
    assertEquals(1, sdkCommandNoneUseUse.fields().stream().count(), "field count of 'SdkCommandNoneUseUse'");
    IField serialVersionUID48 = assertFieldExist(sdkCommandNoneUseUse, "serialVersionUID");
    assertHasFlags(serialVersionUID48, 26);
    assertFieldType(serialVersionUID48, "long");

    assertEquals(0, sdkCommandNoneUseUse.methods().stream().count(), "method count of 'SdkCommandNoneUseUse'");

    assertEquals(0, sdkCommandNoneUseUse.innerTypes().stream().count(), "inner types count of 'SdkCommandNoneUseUse'");
    // type SdkCommandUseCreateCreate
    IType sdkCommandUseCreateCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateCreate");
    assertHasFlags(sdkCommandUseCreateCreate, 9);
    assertHasSuperClass(sdkCommandUseCreateCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseCreate");
    assertAnnotation(sdkCommandUseCreateCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseCreateCreate
    assertEquals(1, sdkCommandUseCreateCreate.fields().stream().count(), "field count of 'SdkCommandUseCreateCreate'");
    IField serialVersionUID49 = assertFieldExist(sdkCommandUseCreateCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID49, 26);
    assertFieldType(serialVersionUID49, "long");

    assertEquals(0, sdkCommandUseCreateCreate.methods().stream().count(), "method count of 'SdkCommandUseCreateCreate'");

    assertEquals(0, sdkCommandUseCreateCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandUseCreateCreate'");
    // type SdkCommandUseCreateIgnore
    IType sdkCommandUseCreateIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateIgnore");
    assertHasFlags(sdkCommandUseCreateIgnore, 9);
    assertHasSuperClass(sdkCommandUseCreateIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseCreate");
    assertAnnotation(sdkCommandUseCreateIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseCreateIgnore
    assertEquals(1, sdkCommandUseCreateIgnore.fields().stream().count(), "field count of 'SdkCommandUseCreateIgnore'");
    IField serialVersionUID50 = assertFieldExist(sdkCommandUseCreateIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID50, 26);
    assertFieldType(serialVersionUID50, "long");

    assertEquals(0, sdkCommandUseCreateIgnore.methods().stream().count(), "method count of 'SdkCommandUseCreateIgnore'");

    assertEquals(0, sdkCommandUseCreateIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandUseCreateIgnore'");
    // type SdkCommandUseCreateNone
    IType sdkCommandUseCreateNone = assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateNone");
    assertHasFlags(sdkCommandUseCreateNone, 9);
    assertHasSuperClass(sdkCommandUseCreateNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseCreate");
    assertAnnotation(sdkCommandUseCreateNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseCreateNone
    assertEquals(1, sdkCommandUseCreateNone.fields().stream().count(), "field count of 'SdkCommandUseCreateNone'");
    IField serialVersionUID51 = assertFieldExist(sdkCommandUseCreateNone, "serialVersionUID");
    assertHasFlags(serialVersionUID51, 26);
    assertFieldType(serialVersionUID51, "long");

    assertEquals(0, sdkCommandUseCreateNone.methods().stream().count(), "method count of 'SdkCommandUseCreateNone'");

    assertEquals(0, sdkCommandUseCreateNone.innerTypes().stream().count(), "inner types count of 'SdkCommandUseCreateNone'");
    // type SdkCommandUseCreateUse
    IType sdkCommandUseCreateUse = assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateUse");
    assertHasFlags(sdkCommandUseCreateUse, 9);
    assertHasSuperClass(sdkCommandUseCreateUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseCreate");
    assertAnnotation(sdkCommandUseCreateUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseCreateUse
    assertEquals(1, sdkCommandUseCreateUse.fields().stream().count(), "field count of 'SdkCommandUseCreateUse'");
    IField serialVersionUID52 = assertFieldExist(sdkCommandUseCreateUse, "serialVersionUID");
    assertHasFlags(serialVersionUID52, 26);
    assertFieldType(serialVersionUID52, "long");

    assertEquals(0, sdkCommandUseCreateUse.methods().stream().count(), "method count of 'SdkCommandUseCreateUse'");

    assertEquals(0, sdkCommandUseCreateUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUseCreateUse'");
    // type SdkCommandUseIgnoreCreate
    IType sdkCommandUseIgnoreCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreCreate");
    assertHasFlags(sdkCommandUseIgnoreCreate, 9);
    assertHasSuperClass(sdkCommandUseIgnoreCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseIgnore");
    assertAnnotation(sdkCommandUseIgnoreCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseIgnoreCreate
    assertEquals(1, sdkCommandUseIgnoreCreate.fields().stream().count(), "field count of 'SdkCommandUseIgnoreCreate'");
    IField serialVersionUID53 = assertFieldExist(sdkCommandUseIgnoreCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID53, 26);
    assertFieldType(serialVersionUID53, "long");

    assertEquals(0, sdkCommandUseIgnoreCreate.methods().stream().count(), "method count of 'SdkCommandUseIgnoreCreate'");

    assertEquals(0, sdkCommandUseIgnoreCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandUseIgnoreCreate'");
    // type SdkCommandUseIgnoreIgnore
    IType sdkCommandUseIgnoreIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreIgnore");
    assertHasFlags(sdkCommandUseIgnoreIgnore, 9);
    assertHasSuperClass(sdkCommandUseIgnoreIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseIgnore");
    assertAnnotation(sdkCommandUseIgnoreIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseIgnoreIgnore
    assertEquals(1, sdkCommandUseIgnoreIgnore.fields().stream().count(), "field count of 'SdkCommandUseIgnoreIgnore'");
    IField serialVersionUID54 = assertFieldExist(sdkCommandUseIgnoreIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID54, 26);
    assertFieldType(serialVersionUID54, "long");

    assertEquals(0, sdkCommandUseIgnoreIgnore.methods().stream().count(), "method count of 'SdkCommandUseIgnoreIgnore'");

    assertEquals(0, sdkCommandUseIgnoreIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandUseIgnoreIgnore'");
    // type SdkCommandUseIgnoreNone
    IType sdkCommandUseIgnoreNone = assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreNone");
    assertHasFlags(sdkCommandUseIgnoreNone, 9);
    assertHasSuperClass(sdkCommandUseIgnoreNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseIgnore");
    assertAnnotation(sdkCommandUseIgnoreNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseIgnoreNone
    assertEquals(1, sdkCommandUseIgnoreNone.fields().stream().count(), "field count of 'SdkCommandUseIgnoreNone'");
    IField serialVersionUID55 = assertFieldExist(sdkCommandUseIgnoreNone, "serialVersionUID");
    assertHasFlags(serialVersionUID55, 26);
    assertFieldType(serialVersionUID55, "long");

    assertEquals(0, sdkCommandUseIgnoreNone.methods().stream().count(), "method count of 'SdkCommandUseIgnoreNone'");

    assertEquals(0, sdkCommandUseIgnoreNone.innerTypes().stream().count(), "inner types count of 'SdkCommandUseIgnoreNone'");
    // type SdkCommandUseIgnoreUse
    IType sdkCommandUseIgnoreUse = assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreUse");
    assertHasFlags(sdkCommandUseIgnoreUse, 9);
    assertHasSuperClass(sdkCommandUseIgnoreUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseIgnore");
    assertAnnotation(sdkCommandUseIgnoreUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseIgnoreUse
    assertEquals(1, sdkCommandUseIgnoreUse.fields().stream().count(), "field count of 'SdkCommandUseIgnoreUse'");
    IField serialVersionUID56 = assertFieldExist(sdkCommandUseIgnoreUse, "serialVersionUID");
    assertHasFlags(serialVersionUID56, 26);
    assertFieldType(serialVersionUID56, "long");

    assertEquals(0, sdkCommandUseIgnoreUse.methods().stream().count(), "method count of 'SdkCommandUseIgnoreUse'");

    assertEquals(0, sdkCommandUseIgnoreUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUseIgnoreUse'");
    // type SdkCommandUseNoneCreate
    IType sdkCommandUseNoneCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneCreate");
    assertHasFlags(sdkCommandUseNoneCreate, 9);
    assertHasSuperClass(sdkCommandUseNoneCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseNone");
    assertAnnotation(sdkCommandUseNoneCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseNoneCreate
    assertEquals(1, sdkCommandUseNoneCreate.fields().stream().count(), "field count of 'SdkCommandUseNoneCreate'");
    IField serialVersionUID57 = assertFieldExist(sdkCommandUseNoneCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID57, 26);
    assertFieldType(serialVersionUID57, "long");

    assertEquals(0, sdkCommandUseNoneCreate.methods().stream().count(), "method count of 'SdkCommandUseNoneCreate'");

    assertEquals(0, sdkCommandUseNoneCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandUseNoneCreate'");
    // type SdkCommandUseNoneIgnore
    IType sdkCommandUseNoneIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneIgnore");
    assertHasFlags(sdkCommandUseNoneIgnore, 9);
    assertHasSuperClass(sdkCommandUseNoneIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseNone");
    assertAnnotation(sdkCommandUseNoneIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseNoneIgnore
    assertEquals(1, sdkCommandUseNoneIgnore.fields().stream().count(), "field count of 'SdkCommandUseNoneIgnore'");
    IField serialVersionUID58 = assertFieldExist(sdkCommandUseNoneIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID58, 26);
    assertFieldType(serialVersionUID58, "long");

    assertEquals(0, sdkCommandUseNoneIgnore.methods().stream().count(), "method count of 'SdkCommandUseNoneIgnore'");

    assertEquals(0, sdkCommandUseNoneIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandUseNoneIgnore'");
    // type SdkCommandUseNoneNone
    IType sdkCommandUseNoneNone = assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneNone");
    assertHasFlags(sdkCommandUseNoneNone, 9);
    assertHasSuperClass(sdkCommandUseNoneNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseNone");
    assertAnnotation(sdkCommandUseNoneNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseNoneNone
    assertEquals(1, sdkCommandUseNoneNone.fields().stream().count(), "field count of 'SdkCommandUseNoneNone'");
    IField serialVersionUID59 = assertFieldExist(sdkCommandUseNoneNone, "serialVersionUID");
    assertHasFlags(serialVersionUID59, 26);
    assertFieldType(serialVersionUID59, "long");

    assertEquals(0, sdkCommandUseNoneNone.methods().stream().count(), "method count of 'SdkCommandUseNoneNone'");

    assertEquals(0, sdkCommandUseNoneNone.innerTypes().stream().count(), "inner types count of 'SdkCommandUseNoneNone'");
    // type SdkCommandUseNoneUse
    IType sdkCommandUseNoneUse = assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneUse");
    assertHasFlags(sdkCommandUseNoneUse, 9);
    assertHasSuperClass(sdkCommandUseNoneUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseNone");
    assertAnnotation(sdkCommandUseNoneUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseNoneUse
    assertEquals(1, sdkCommandUseNoneUse.fields().stream().count(), "field count of 'SdkCommandUseNoneUse'");
    IField serialVersionUID60 = assertFieldExist(sdkCommandUseNoneUse, "serialVersionUID");
    assertHasFlags(serialVersionUID60, 26);
    assertFieldType(serialVersionUID60, "long");

    assertEquals(0, sdkCommandUseNoneUse.methods().stream().count(), "method count of 'SdkCommandUseNoneUse'");

    assertEquals(0, sdkCommandUseNoneUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUseNoneUse'");
    // type SdkCommandUseUseCreate
    IType sdkCommandUseUseCreate = assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseCreate");
    assertHasFlags(sdkCommandUseUseCreate, 9);
    assertHasSuperClass(sdkCommandUseUseCreate, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseUse");
    assertAnnotation(sdkCommandUseUseCreate, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseUseCreate
    assertEquals(1, sdkCommandUseUseCreate.fields().stream().count(), "field count of 'SdkCommandUseUseCreate'");
    IField serialVersionUID61 = assertFieldExist(sdkCommandUseUseCreate, "serialVersionUID");
    assertHasFlags(serialVersionUID61, 26);
    assertFieldType(serialVersionUID61, "long");

    assertEquals(0, sdkCommandUseUseCreate.methods().stream().count(), "method count of 'SdkCommandUseUseCreate'");

    assertEquals(0, sdkCommandUseUseCreate.innerTypes().stream().count(), "inner types count of 'SdkCommandUseUseCreate'");
    // type SdkCommandUseUseIgnore
    IType sdkCommandUseUseIgnore = assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseIgnore");
    assertHasFlags(sdkCommandUseUseIgnore, 9);
    assertHasSuperClass(sdkCommandUseUseIgnore, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseUse");
    assertAnnotation(sdkCommandUseUseIgnore, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseUseIgnore
    assertEquals(1, sdkCommandUseUseIgnore.fields().stream().count(), "field count of 'SdkCommandUseUseIgnore'");
    IField serialVersionUID62 = assertFieldExist(sdkCommandUseUseIgnore, "serialVersionUID");
    assertHasFlags(serialVersionUID62, 26);
    assertFieldType(serialVersionUID62, "long");

    assertEquals(0, sdkCommandUseUseIgnore.methods().stream().count(), "method count of 'SdkCommandUseUseIgnore'");

    assertEquals(0, sdkCommandUseUseIgnore.innerTypes().stream().count(), "inner types count of 'SdkCommandUseUseIgnore'");
    // type SdkCommandUseUseNone
    IType sdkCommandUseUseNone = assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseNone");
    assertHasFlags(sdkCommandUseUseNone, 9);
    assertHasSuperClass(sdkCommandUseUseNone, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseUse");
    assertAnnotation(sdkCommandUseUseNone, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseUseNone
    assertEquals(1, sdkCommandUseUseNone.fields().stream().count(), "field count of 'SdkCommandUseUseNone'");
    IField serialVersionUID63 = assertFieldExist(sdkCommandUseUseNone, "serialVersionUID");
    assertHasFlags(serialVersionUID63, 26);
    assertFieldType(serialVersionUID63, "long");

    assertEquals(0, sdkCommandUseUseNone.methods().stream().count(), "method count of 'SdkCommandUseUseNone'");

    assertEquals(0, sdkCommandUseUseNone.innerTypes().stream().count(), "inner types count of 'SdkCommandUseUseNone'");
    // type SdkCommandUseUseUse
    IType sdkCommandUseUseUse = assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseUse");
    assertHasFlags(sdkCommandUseUseUse, 9);
    assertHasSuperClass(sdkCommandUseUseUse, "formdata.shared.services.process.replace.ExtendedFormData$SdkCommandUseUse");
    assertAnnotation(sdkCommandUseUseUse, "org.eclipse.scout.rt.platform.Replace");

    // fields of SdkCommandUseUseUse
    assertEquals(1, sdkCommandUseUseUse.fields().stream().count(), "field count of 'SdkCommandUseUseUse'");
    IField serialVersionUID64 = assertFieldExist(sdkCommandUseUseUse, "serialVersionUID");
    assertHasFlags(serialVersionUID64, 26);
    assertFieldType(serialVersionUID64, "long");

    assertEquals(0, sdkCommandUseUseUse.methods().stream().count(), "method count of 'SdkCommandUseUseUse'");

    assertEquals(0, sdkCommandUseUseUse.innerTypes().stream().count(), "inner types count of 'SdkCommandUseUseUse'");
  }

}
