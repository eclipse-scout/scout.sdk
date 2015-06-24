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
package org.eclipse.scout.sdk.dto.test;

import org.eclipse.scout.sdk.core.model.IField;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.dto.test.util.CoreScoutTestingUtils;
import org.junit.Test;

public class SimpleFormTest {

  @Test
  public void testCreateFormData() throws Exception {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms.SimpleForm");
    testApiOfSimpleFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfSimpleFormData(IType simpleFormData) throws Exception {
    // type SimpleFormData
    SdkAssert.assertHasFlags(simpleFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(simpleFormData, "QAbstractFormData;");
    SdkAssert.assertHasSuperIntefaceSignatures(simpleFormData, new String[]{"QIFormDataInterface02;", "QIFormDataInterface03;"});
    SdkAssert.assertAnnotation(simpleFormData, "javax.annotation.Generated");

    // fields of SimpleFormData
    SdkAssert.assertEquals("field count of 'SimpleFormData'", 1, simpleFormData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(simpleFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'SimpleFormData'", 11, simpleFormData.getMethods().size());
    IMethod simpleFormData1 = SdkAssert.assertMethodExist(simpleFormData, "SimpleFormData", new String[]{});
    SdkAssert.assertTrue(simpleFormData1.isConstructor());
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

    SdkAssert.assertEquals("inner types count of 'SimpleFormData'", 8, simpleFormData.getTypes().size());
    // type Date
    IType date = SdkAssert.assertTypeExists(simpleFormData, "Date");
    SdkAssert.assertHasFlags(date, 9);
    SdkAssert.assertHasSuperTypeSignature(date, "QAbstractValueFieldData<QInteger;>;");

    // fields of Date
    SdkAssert.assertEquals("field count of 'Date'", 1, date.getFields().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(date, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'Date'", 1, date.getMethods().size());
    IMethod date1 = SdkAssert.assertMethodExist(date, "Date", new String[]{});
    SdkAssert.assertTrue(date1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(date1, null);

    SdkAssert.assertEquals("inner types count of 'Date'", 0, date.getTypes().size());
    // type Double
    IType doubleT = SdkAssert.assertTypeExists(simpleFormData, "Double");
    SdkAssert.assertHasFlags(doubleT, 9);
    SdkAssert.assertHasSuperTypeSignature(doubleT, "QAbstractValueFieldData<Ljava.math.BigDecimal;>;");

    // fields of Double
    SdkAssert.assertEquals("field count of 'Double'", 1, doubleT.getFields().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(doubleT, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'Double'", 1, doubleT.getMethods().size());
    IMethod double1 = SdkAssert.assertMethodExist(doubleT, "Double", new String[]{});
    SdkAssert.assertTrue(double1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(double1, null);

    SdkAssert.assertEquals("inner types count of 'Double'", 0, doubleT.getTypes().size());
    // type MultiTypeArgsBox
    IType multiTypeArgsBox = SdkAssert.assertTypeExists(simpleFormData, "MultiTypeArgsBox");
    SdkAssert.assertHasFlags(multiTypeArgsBox, 9);
    SdkAssert.assertHasSuperTypeSignature(multiTypeArgsBox, "QAbstractValueFieldData<QTestRunnable;>;");

    // fields of MultiTypeArgsBox
    SdkAssert.assertEquals("field count of 'MultiTypeArgsBox'", 1, multiTypeArgsBox.getFields().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(multiTypeArgsBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'MultiTypeArgsBox'", 1, multiTypeArgsBox.getMethods().size());
    IMethod multiTypeArgsBox1 = SdkAssert.assertMethodExist(multiTypeArgsBox, "MultiTypeArgsBox", new String[]{});
    SdkAssert.assertTrue(multiTypeArgsBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(multiTypeArgsBox1, null);

    SdkAssert.assertEquals("inner types count of 'MultiTypeArgsBox'", 0, multiTypeArgsBox.getTypes().size());
    // type SampleComposer
    IType sampleComposer = SdkAssert.assertTypeExists(simpleFormData, "SampleComposer");
    SdkAssert.assertHasFlags(sampleComposer, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleComposer, "QAbstractComposerData;");

    // fields of SampleComposer
    SdkAssert.assertEquals("field count of 'SampleComposer'", 1, sampleComposer.getFields().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(sampleComposer, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    SdkAssert.assertEquals("method count of 'SampleComposer'", 1, sampleComposer.getMethods().size());
    IMethod sampleComposer1 = SdkAssert.assertMethodExist(sampleComposer, "SampleComposer", new String[]{});
    SdkAssert.assertTrue(sampleComposer1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleComposer1, null);

    SdkAssert.assertEquals("inner types count of 'SampleComposer'", 0, sampleComposer.getTypes().size());
    // type SampleDate
    IType sampleDate = SdkAssert.assertTypeExists(simpleFormData, "SampleDate");
    SdkAssert.assertHasFlags(sampleDate, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleDate, "QAbstractValueFieldData<Ljava.util.Date;>;");

    // fields of SampleDate
    SdkAssert.assertEquals("field count of 'SampleDate'", 1, sampleDate.getFields().size());
    IField serialVersionUID5 = SdkAssert.assertFieldExist(sampleDate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    SdkAssert.assertEquals("method count of 'SampleDate'", 1, sampleDate.getMethods().size());
    IMethod sampleDate1 = SdkAssert.assertMethodExist(sampleDate, "SampleDate", new String[]{});
    SdkAssert.assertTrue(sampleDate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleDate1, null);

    SdkAssert.assertEquals("inner types count of 'SampleDate'", 0, sampleDate.getTypes().size());
    // type SampleSmart
    IType sampleSmart = SdkAssert.assertTypeExists(simpleFormData, "SampleSmart");
    SdkAssert.assertHasFlags(sampleSmart, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleSmart, "QAbstractValueFieldData<QLong;>;");

    // fields of SampleSmart
    SdkAssert.assertEquals("field count of 'SampleSmart'", 1, sampleSmart.getFields().size());
    IField serialVersionUID6 = SdkAssert.assertFieldExist(sampleSmart, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    SdkAssert.assertEquals("method count of 'SampleSmart'", 1, sampleSmart.getMethods().size());
    IMethod sampleSmart1 = SdkAssert.assertMethodExist(sampleSmart, "SampleSmart", new String[]{});
    SdkAssert.assertTrue(sampleSmart1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleSmart1, null);

    SdkAssert.assertEquals("inner types count of 'SampleSmart'", 0, sampleSmart.getTypes().size());
    // type SampleString
    IType sampleString = SdkAssert.assertTypeExists(simpleFormData, "SampleString");
    SdkAssert.assertHasFlags(sampleString, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleString, "QAbstractValueFieldData<QString;>;");

    // fields of SampleString
    SdkAssert.assertEquals("field count of 'SampleString'", 1, sampleString.getFields().size());
    IField serialVersionUID7 = SdkAssert.assertFieldExist(sampleString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    SdkAssert.assertEquals("method count of 'SampleString'", 1, sampleString.getMethods().size());
    IMethod sampleString1 = SdkAssert.assertMethodExist(sampleString, "SampleString", new String[]{});
    SdkAssert.assertTrue(sampleString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleString1, null);

    SdkAssert.assertEquals("inner types count of 'SampleString'", 0, sampleString.getTypes().size());
    // type SimpleNrProperty
    IType simpleNrProperty = SdkAssert.assertTypeExists(simpleFormData, "SimpleNrProperty");
    SdkAssert.assertHasFlags(simpleNrProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(simpleNrProperty, "QAbstractPropertyData<QLong;>;");

    // fields of SimpleNrProperty
    SdkAssert.assertEquals("field count of 'SimpleNrProperty'", 1, simpleNrProperty.getFields().size());
    IField serialVersionUID8 = SdkAssert.assertFieldExist(simpleNrProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");

    SdkAssert.assertEquals("method count of 'SimpleNrProperty'", 1, simpleNrProperty.getMethods().size());
    IMethod simpleNrProperty1 = SdkAssert.assertMethodExist(simpleNrProperty, "SimpleNrProperty", new String[]{});
    SdkAssert.assertTrue(simpleNrProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(simpleNrProperty1, null);

    SdkAssert.assertEquals("inner types count of 'SimpleNrProperty'", 0, simpleNrProperty.getTypes().size());
  }
}
