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
package org.eclipse.scout.sdk.core.s.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

public class SimpleFormTest {

  @Test
  public void testCreateFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms.SimpleForm");
    testApiOfSimpleFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfSimpleFormData(IType simpleFormData) {
    // type SimpleFormData
    SdkAssert.assertHasFlags(simpleFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(simpleFormData, "QAbstractFormData;");
    SdkAssert.assertHasSuperIntefaceSignatures(simpleFormData, new String[]{"QIFormDataInterface02;", "QIFormDataInterface03;"});
    SdkAssert.assertAnnotation(simpleFormData, "javax.annotation.Generated");

    // fields of SimpleFormData
    Assert.assertEquals("field count of 'SimpleFormData'", 1, simpleFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(simpleFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'SimpleFormData'", 11, simpleFormData.methods().list().size());
    IMethod simpleFormData1 = SdkAssert.assertMethodExist(simpleFormData, "SimpleFormData", new String[]{});
    Assert.assertTrue(simpleFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(simpleFormData1, null);
    IMethod getDate = SdkAssert.assertMethodExist(simpleFormData, "getDate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getDate, "QDate;");
    IMethod getDouble = SdkAssert.assertMethodExist(simpleFormData, "getDouble", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getDouble, "QDouble;");
    IMethod getMultiTypeArgsBox = SdkAssert.assertMethodExist(simpleFormData, "getMultiTypeArgsBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getMultiTypeArgsBox, "QMultiTypeArgsBox;");
    IMethod getSampleComposer = SdkAssert.assertMethodExist(simpleFormData, "getSampleComposer", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSampleComposer, "QSampleComposer;");
    SdkAssert.assertAnnotation(getSampleComposer, "java.lang.Override");
    IMethod getSampleDate = SdkAssert.assertMethodExist(simpleFormData, "getSampleDate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSampleDate, "QSampleDate;");
    IMethod getSampleSmart = SdkAssert.assertMethodExist(simpleFormData, "getSampleSmart", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSampleSmart, "QSampleSmart;");
    IMethod getSampleString = SdkAssert.assertMethodExist(simpleFormData, "getSampleString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSampleString, "QSampleString;");
    IMethod getSimpleNr = SdkAssert.assertMethodExist(simpleFormData, "getSimpleNr", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSimpleNr, "QLong;");
    IMethod setSimpleNr = SdkAssert.assertMethodExist(simpleFormData, "setSimpleNr", new String[]{"QLong;"});
    SdkAssert.assertMethodReturnTypeSignature(setSimpleNr, "V");
    IMethod getSimpleNrProperty = SdkAssert.assertMethodExist(simpleFormData, "getSimpleNrProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSimpleNrProperty, "QSimpleNrProperty;");

    Assert.assertEquals("inner types count of 'SimpleFormData'", 8, simpleFormData.innerTypes().list().size());
    // type Date
    IType date = SdkAssert.assertTypeExists(simpleFormData, "Date");
    SdkAssert.assertHasFlags(date, 9);
    SdkAssert.assertHasSuperTypeSignature(date, "QAbstractValueFieldData<QInteger;>;");

    // fields of Date
    Assert.assertEquals("field count of 'Date'", 1, date.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(date, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'Date'", 1, date.methods().list().size());
    IMethod date1 = SdkAssert.assertMethodExist(date, "Date", new String[]{});
    Assert.assertTrue(date1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(date1, null);

    Assert.assertEquals("inner types count of 'Date'", 0, date.innerTypes().list().size());
    // type Double
    IType doubleT = SdkAssert.assertTypeExists(simpleFormData, "Double");
    SdkAssert.assertHasFlags(doubleT, 9);
    SdkAssert.assertHasSuperTypeSignature(doubleT, "QAbstractValueFieldData<Ljava.math.BigDecimal;>;");

    // fields of Double
    Assert.assertEquals("field count of 'Double'", 1, doubleT.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(doubleT, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'Double'", 1, doubleT.methods().list().size());
    IMethod double1 = SdkAssert.assertMethodExist(doubleT, "Double", new String[]{});
    Assert.assertTrue(double1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(double1, null);

    Assert.assertEquals("inner types count of 'Double'", 0, doubleT.innerTypes().list().size());
    // type MultiTypeArgsBox
    IType multiTypeArgsBox = SdkAssert.assertTypeExists(simpleFormData, "MultiTypeArgsBox");
    SdkAssert.assertHasFlags(multiTypeArgsBox, 9);
    SdkAssert.assertHasSuperTypeSignature(multiTypeArgsBox, "QAbstractValueFieldData<QTestRunnable;>;");

    // fields of MultiTypeArgsBox
    Assert.assertEquals("field count of 'MultiTypeArgsBox'", 1, multiTypeArgsBox.fields().list().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(multiTypeArgsBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'MultiTypeArgsBox'", 1, multiTypeArgsBox.methods().list().size());
    IMethod multiTypeArgsBox1 = SdkAssert.assertMethodExist(multiTypeArgsBox, "MultiTypeArgsBox", new String[]{});
    Assert.assertTrue(multiTypeArgsBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(multiTypeArgsBox1, null);

    Assert.assertEquals("inner types count of 'MultiTypeArgsBox'", 0, multiTypeArgsBox.innerTypes().list().size());
    // type SampleComposer
    IType sampleComposer = SdkAssert.assertTypeExists(simpleFormData, "SampleComposer");
    SdkAssert.assertHasFlags(sampleComposer, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleComposer, "QAbstractComposerData;");

    // fields of SampleComposer
    Assert.assertEquals("field count of 'SampleComposer'", 1, sampleComposer.fields().list().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(sampleComposer, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    Assert.assertEquals("method count of 'SampleComposer'", 1, sampleComposer.methods().list().size());
    IMethod sampleComposer1 = SdkAssert.assertMethodExist(sampleComposer, "SampleComposer", new String[]{});
    Assert.assertTrue(sampleComposer1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleComposer1, null);

    Assert.assertEquals("inner types count of 'SampleComposer'", 0, sampleComposer.innerTypes().list().size());
    // type SampleDate
    IType sampleDate = SdkAssert.assertTypeExists(simpleFormData, "SampleDate");
    SdkAssert.assertHasFlags(sampleDate, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleDate, "QAbstractValueFieldData<Ljava.util.Date;>;");

    // fields of SampleDate
    Assert.assertEquals("field count of 'SampleDate'", 1, sampleDate.fields().list().size());
    IField serialVersionUID5 = SdkAssert.assertFieldExist(sampleDate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    Assert.assertEquals("method count of 'SampleDate'", 1, sampleDate.methods().list().size());
    IMethod sampleDate1 = SdkAssert.assertMethodExist(sampleDate, "SampleDate", new String[]{});
    Assert.assertTrue(sampleDate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleDate1, null);

    Assert.assertEquals("inner types count of 'SampleDate'", 0, sampleDate.innerTypes().list().size());
    // type SampleSmart
    IType sampleSmart = SdkAssert.assertTypeExists(simpleFormData, "SampleSmart");
    SdkAssert.assertHasFlags(sampleSmart, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleSmart, "QAbstractValueFieldData<QLong;>;");

    // fields of SampleSmart
    Assert.assertEquals("field count of 'SampleSmart'", 1, sampleSmart.fields().list().size());
    IField serialVersionUID6 = SdkAssert.assertFieldExist(sampleSmart, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    Assert.assertEquals("method count of 'SampleSmart'", 1, sampleSmart.methods().list().size());
    IMethod sampleSmart1 = SdkAssert.assertMethodExist(sampleSmart, "SampleSmart", new String[]{});
    Assert.assertTrue(sampleSmart1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleSmart1, null);

    Assert.assertEquals("inner types count of 'SampleSmart'", 0, sampleSmart.innerTypes().list().size());
    // type SampleString
    IType sampleString = SdkAssert.assertTypeExists(simpleFormData, "SampleString");
    SdkAssert.assertHasFlags(sampleString, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleString, "QAbstractValueFieldData<QString;>;");

    // fields of SampleString
    Assert.assertEquals("field count of 'SampleString'", 1, sampleString.fields().list().size());
    IField serialVersionUID7 = SdkAssert.assertFieldExist(sampleString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    Assert.assertEquals("method count of 'SampleString'", 1, sampleString.methods().list().size());
    IMethod sampleString1 = SdkAssert.assertMethodExist(sampleString, "SampleString", new String[]{});
    Assert.assertTrue(sampleString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleString1, null);

    Assert.assertEquals("inner types count of 'SampleString'", 0, sampleString.innerTypes().list().size());
    // type SimpleNrProperty
    IType simpleNrProperty = SdkAssert.assertTypeExists(simpleFormData, "SimpleNrProperty");
    SdkAssert.assertHasFlags(simpleNrProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(simpleNrProperty, "QAbstractPropertyData<QLong;>;");

    // fields of SimpleNrProperty
    Assert.assertEquals("field count of 'SimpleNrProperty'", 1, simpleNrProperty.fields().list().size());
    IField serialVersionUID8 = SdkAssert.assertFieldExist(simpleNrProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");

    Assert.assertEquals("method count of 'SimpleNrProperty'", 1, simpleNrProperty.methods().list().size());
    IMethod simpleNrProperty1 = SdkAssert.assertMethodExist(simpleNrProperty, "SimpleNrProperty", new String[]{});
    Assert.assertTrue(simpleNrProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(simpleNrProperty1, null);

    Assert.assertEquals("inner types count of 'SimpleNrProperty'", 0, simpleNrProperty.innerTypes().list().size());
  }
}
