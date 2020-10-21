/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.SimpleForm;

public class SimpleFormTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(SimpleForm.class.getName(), SimpleFormTest::testApiOfSimpleFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfSimpleFormData(IType simpleFormData) {
    assertHasFlags(simpleFormData, Flags.AccPublic);
    assertHasSuperClass(simpleFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");
    assertHasSuperInterfaces(simpleFormData, new String[]{"formdata.shared.IFormDataInterface02", "formdata.shared.IFormDataInterface03"});
    assertEquals(2, simpleFormData.annotations().stream().count(), "annotation count");
    assertValidDtoClassIdValue(assertAnnotation(simpleFormData, "org.eclipse.scout.rt.platform.classid.ClassId"));
    assertAnnotation(simpleFormData, "javax.annotation.Generated");

    // fields of SimpleFormData
    assertEquals(1, simpleFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.SimpleFormData'");
    var serialVersionUID = assertFieldExist(simpleFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(10, simpleFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.SimpleFormData'");
    var getDate = assertMethodExist(simpleFormData, "getDate", new String[]{});
    assertMethodReturnType(getDate, "formdata.shared.services.process.SimpleFormData$Date");
    assertEquals(0, getDate.annotations().stream().count(), "annotation count");
    var getDouble = assertMethodExist(simpleFormData, "getDouble", new String[]{});
    assertMethodReturnType(getDouble, "formdata.shared.services.process.SimpleFormData$Double");
    assertEquals(0, getDouble.annotations().stream().count(), "annotation count");
    var getMultiTypeArgsBox = assertMethodExist(simpleFormData, "getMultiTypeArgsBox", new String[]{});
    assertMethodReturnType(getMultiTypeArgsBox, "formdata.shared.services.process.SimpleFormData$MultiTypeArgsBox");
    assertEquals(0, getMultiTypeArgsBox.annotations().stream().count(), "annotation count");
    var getSampleComposer = assertMethodExist(simpleFormData, "getSampleComposer", new String[]{});
    assertMethodReturnType(getSampleComposer, "formdata.shared.services.process.SimpleFormData$SampleComposer");
    assertEquals(1, getSampleComposer.annotations().stream().count(), "annotation count");
    assertAnnotation(getSampleComposer, "java.lang.Override");
    var getSampleDate = assertMethodExist(simpleFormData, "getSampleDate", new String[]{});
    assertMethodReturnType(getSampleDate, "formdata.shared.services.process.SimpleFormData$SampleDate");
    assertEquals(0, getSampleDate.annotations().stream().count(), "annotation count");
    var getSampleSmart = assertMethodExist(simpleFormData, "getSampleSmart", new String[]{});
    assertMethodReturnType(getSampleSmart, "formdata.shared.services.process.SimpleFormData$SampleSmart");
    assertEquals(0, getSampleSmart.annotations().stream().count(), "annotation count");
    var getSampleString = assertMethodExist(simpleFormData, "getSampleString", new String[]{});
    assertMethodReturnType(getSampleString, "formdata.shared.services.process.SimpleFormData$SampleString");
    assertEquals(0, getSampleString.annotations().stream().count(), "annotation count");
    var getSimpleNr = assertMethodExist(simpleFormData, "getSimpleNr", new String[]{});
    assertMethodReturnType(getSimpleNr, "java.lang.Long");
    assertEquals(0, getSimpleNr.annotations().stream().count(), "annotation count");
    var getSimpleNrProperty = assertMethodExist(simpleFormData, "getSimpleNrProperty", new String[]{});
    assertMethodReturnType(getSimpleNrProperty, "formdata.shared.services.process.SimpleFormData$SimpleNrProperty");
    assertEquals(0, getSimpleNrProperty.annotations().stream().count(), "annotation count");
    var setSimpleNr = assertMethodExist(simpleFormData, "setSimpleNr", new String[]{"java.lang.Long"});
    assertMethodReturnType(setSimpleNr, "void");
    assertEquals(0, setSimpleNr.annotations().stream().count(), "annotation count");

    assertEquals(8, simpleFormData.innerTypes().stream().count(), "inner types count of 'SimpleFormData'");
    // type SimpleNrProperty
    var simpleNrProperty = assertTypeExists(simpleFormData, "SimpleNrProperty");
    assertHasFlags(simpleNrProperty, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(simpleNrProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Long>");
    assertEquals(0, simpleNrProperty.annotations().stream().count(), "annotation count");

    // fields of SimpleNrProperty
    assertEquals(1, simpleNrProperty.fields().stream().count(), "field count of 'formdata.shared.services.process.SimpleFormData$SimpleNrProperty'");
    var serialVersionUID1 = assertFieldExist(simpleNrProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, simpleNrProperty.methods().stream().count(), "method count of 'formdata.shared.services.process.SimpleFormData$SimpleNrProperty'");

    assertEquals(0, simpleNrProperty.innerTypes().stream().count(), "inner types count of 'SimpleNrProperty'");
    // type Date
    var date = assertTypeExists(simpleFormData, "Date");
    assertHasFlags(date, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(date, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Integer>");
    assertEquals(1, date.annotations().stream().count(), "annotation count");
    assertValidDtoClassIdValue(assertAnnotation(date, "org.eclipse.scout.rt.platform.classid.ClassId"));

    // fields of Date
    assertEquals(1, date.fields().stream().count(), "field count of 'formdata.shared.services.process.SimpleFormData$Date'");
    var serialVersionUID2 = assertFieldExist(date, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, date.methods().stream().count(), "method count of 'formdata.shared.services.process.SimpleFormData$Date'");

    assertEquals(0, date.innerTypes().stream().count(), "inner types count of 'Date'");
    // type Double
    var double1 = assertTypeExists(simpleFormData, "Double");
    assertHasFlags(double1, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(double1, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.math.BigDecimal>");
    assertEquals(1, double1.annotations().stream().count(), "annotation count");
    assertValidDtoClassIdValue(assertAnnotation(double1, "org.eclipse.scout.rt.platform.classid.ClassId"));

    // fields of Double
    assertEquals(1, double1.fields().stream().count(), "field count of 'formdata.shared.services.process.SimpleFormData$Double'");
    var serialVersionUID3 = assertFieldExist(double1, "serialVersionUID");
    assertHasFlags(serialVersionUID3, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID3, "long");
    assertEquals(0, serialVersionUID3.annotations().stream().count(), "annotation count");

    assertEquals(0, double1.methods().stream().count(), "method count of 'formdata.shared.services.process.SimpleFormData$Double'");

    assertEquals(0, double1.innerTypes().stream().count(), "inner types count of 'Double'");
    // type MultiTypeArgsBox
    var multiTypeArgsBox = assertTypeExists(simpleFormData, "MultiTypeArgsBox");
    assertHasFlags(multiTypeArgsBox, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(multiTypeArgsBox, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<formdata.shared.TestRunnable>");
    assertEquals(1, multiTypeArgsBox.annotations().stream().count(), "annotation count");
    assertValidDtoClassIdValue(assertAnnotation(multiTypeArgsBox, "org.eclipse.scout.rt.platform.classid.ClassId"));

    // fields of MultiTypeArgsBox
    assertEquals(1, multiTypeArgsBox.fields().stream().count(), "field count of 'formdata.shared.services.process.SimpleFormData$MultiTypeArgsBox'");
    var serialVersionUID4 = assertFieldExist(multiTypeArgsBox, "serialVersionUID");
    assertHasFlags(serialVersionUID4, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID4, "long");
    assertEquals(0, serialVersionUID4.annotations().stream().count(), "annotation count");

    assertEquals(0, multiTypeArgsBox.methods().stream().count(), "method count of 'formdata.shared.services.process.SimpleFormData$MultiTypeArgsBox'");

    assertEquals(0, multiTypeArgsBox.innerTypes().stream().count(), "inner types count of 'MultiTypeArgsBox'");
    // type SampleComposer
    var sampleComposer = assertTypeExists(simpleFormData, "SampleComposer");
    assertHasFlags(sampleComposer, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sampleComposer, "org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerData");
    assertEquals(1, sampleComposer.annotations().stream().count(), "annotation count");
    assertValidDtoClassIdValue(assertAnnotation(sampleComposer, "org.eclipse.scout.rt.platform.classid.ClassId"));

    // fields of SampleComposer
    assertEquals(1, sampleComposer.fields().stream().count(), "field count of 'formdata.shared.services.process.SimpleFormData$SampleComposer'");
    var serialVersionUID5 = assertFieldExist(sampleComposer, "serialVersionUID");
    assertHasFlags(serialVersionUID5, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID5, "long");
    assertEquals(0, serialVersionUID5.annotations().stream().count(), "annotation count");

    assertEquals(0, sampleComposer.methods().stream().count(), "method count of 'formdata.shared.services.process.SimpleFormData$SampleComposer'");

    assertEquals(0, sampleComposer.innerTypes().stream().count(), "inner types count of 'SampleComposer'");
    // type SampleDate
    var sampleDate = assertTypeExists(simpleFormData, "SampleDate");
    assertHasFlags(sampleDate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sampleDate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.util.Date>");
    assertEquals(1, sampleDate.annotations().stream().count(), "annotation count");
    assertValidDtoClassIdValue(assertAnnotation(sampleDate, "org.eclipse.scout.rt.platform.classid.ClassId"));

    // fields of SampleDate
    assertEquals(1, sampleDate.fields().stream().count(), "field count of 'formdata.shared.services.process.SimpleFormData$SampleDate'");
    var serialVersionUID6 = assertFieldExist(sampleDate, "serialVersionUID");
    assertHasFlags(serialVersionUID6, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID6, "long");
    assertEquals(0, serialVersionUID6.annotations().stream().count(), "annotation count");

    assertEquals(0, sampleDate.methods().stream().count(), "method count of 'formdata.shared.services.process.SimpleFormData$SampleDate'");

    assertEquals(0, sampleDate.innerTypes().stream().count(), "inner types count of 'SampleDate'");
    // type SampleSmart
    var sampleSmart = assertTypeExists(simpleFormData, "SampleSmart");
    assertHasFlags(sampleSmart, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sampleSmart, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Long>");
    assertEquals(1, sampleSmart.annotations().stream().count(), "annotation count");
    assertValidDtoClassIdValue(assertAnnotation(sampleSmart, "org.eclipse.scout.rt.platform.classid.ClassId"));

    // fields of SampleSmart
    assertEquals(1, sampleSmart.fields().stream().count(), "field count of 'formdata.shared.services.process.SimpleFormData$SampleSmart'");
    var serialVersionUID7 = assertFieldExist(sampleSmart, "serialVersionUID");
    assertHasFlags(serialVersionUID7, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID7, "long");
    assertEquals(0, serialVersionUID7.annotations().stream().count(), "annotation count");

    assertEquals(0, sampleSmart.methods().stream().count(), "method count of 'formdata.shared.services.process.SimpleFormData$SampleSmart'");

    assertEquals(0, sampleSmart.innerTypes().stream().count(), "inner types count of 'SampleSmart'");
    // type SampleString
    var sampleString = assertTypeExists(simpleFormData, "SampleString");
    assertHasFlags(sampleString, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(sampleString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(1, sampleString.annotations().stream().count(), "annotation count");
    assertValidDtoClassIdValue(assertAnnotation(sampleString, "org.eclipse.scout.rt.platform.classid.ClassId"));

    // fields of SampleString
    assertEquals(1, sampleString.fields().stream().count(), "field count of 'formdata.shared.services.process.SimpleFormData$SampleString'");
    var serialVersionUID8 = assertFieldExist(sampleString, "serialVersionUID");
    assertHasFlags(serialVersionUID8, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID8, "long");
    assertEquals(0, serialVersionUID8.annotations().stream().count(), "annotation count");

    assertEquals(0, sampleString.methods().stream().count(), "method count of 'formdata.shared.services.process.SimpleFormData$SampleString'");

    assertEquals(0, sampleString.innerTypes().stream().count(), "inner types count of 'SampleString'");
  }

  private static void assertValidDtoClassIdValue(IAnnotation classId) {
    assertTrue(classId.element("value").get().value().as(String.class).endsWith("-formdata"));
  }
}
