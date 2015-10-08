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

public class FormPropertiesTest {

  @Test
  public void testCreateFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms.PropertyTestForm");
    testApiOfPropertyTestFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfPropertyTestFormData(IType propertyTestFormData) {
    // type PropertyTestFormData
    SdkAssert.assertHasFlags(propertyTestFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(propertyTestFormData, "QAbstractFormData;");

    // fields of PropertyTestFormData
    Assert.assertEquals("field count of 'PropertyTestFormData'", 1, propertyTestFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(propertyTestFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'PropertyTestFormData'", 35, propertyTestFormData.methods().list().size());
    IMethod propertyTestFormData1 = SdkAssert.assertMethodExist(propertyTestFormData, "PropertyTestFormData", new String[]{});
    Assert.assertTrue(propertyTestFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(propertyTestFormData1, null);
    IMethod getBoolObject = SdkAssert.assertMethodExist(propertyTestFormData, "getBoolObject", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getBoolObject, "QBoolean;");
    IMethod setBoolObject = SdkAssert.assertMethodExist(propertyTestFormData, "setBoolObject", new String[]{"QBoolean;"});
    SdkAssert.assertMethodReturnTypeSignature(setBoolObject, "V");
    IMethod getBoolObjectProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getBoolObjectProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getBoolObjectProperty, "QBoolObjectProperty;");
    IMethod isBoolPrimitive = SdkAssert.assertMethodExist(propertyTestFormData, "isBoolPrimitive", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(isBoolPrimitive, "Z");
    IMethod setBoolPrimitive = SdkAssert.assertMethodExist(propertyTestFormData, "setBoolPrimitive", new String[]{"Z"});
    SdkAssert.assertMethodReturnTypeSignature(setBoolPrimitive, "V");
    IMethod getBoolPrimitiveProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getBoolPrimitiveProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getBoolPrimitiveProperty, "QBoolPrimitiveProperty;");
    IMethod getByteArray = SdkAssert.assertMethodExist(propertyTestFormData, "getByteArray", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getByteArray, "[B");
    IMethod setByteArray = SdkAssert.assertMethodExist(propertyTestFormData, "setByteArray", new String[]{"[B"});
    SdkAssert.assertMethodReturnTypeSignature(setByteArray, "V");
    IMethod getByteArrayProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getByteArrayProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getByteArrayProperty, "QByteArrayProperty;");
    IMethod getComplexArray = SdkAssert.assertMethodExist(propertyTestFormData, "getComplexArray", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getComplexArray, "[QArrayList<QList<QString;>;>;");
    IMethod setComplexArray = SdkAssert.assertMethodExist(propertyTestFormData, "setComplexArray", new String[]{"[QArrayList<QList<QString;>;>;"});
    SdkAssert.assertMethodReturnTypeSignature(setComplexArray, "V");
    IMethod getComplexArrayProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getComplexArrayProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getComplexArrayProperty, "QComplexArrayProperty;");
    IMethod getComplexInnerArray = SdkAssert.assertMethodExist(propertyTestFormData, "getComplexInnerArray", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getComplexInnerArray, "QArrayList<QList<[QString;>;>;");
    IMethod setComplexInnerArray = SdkAssert.assertMethodExist(propertyTestFormData, "setComplexInnerArray", new String[]{"QArrayList<QList<[QString;>;>;"});
    SdkAssert.assertMethodReturnTypeSignature(setComplexInnerArray, "V");
    IMethod getComplexInnerArrayProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getComplexInnerArrayProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getComplexInnerArrayProperty, "QComplexInnerArrayProperty;");
    IMethod getDoubleArrayProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getDoubleArrayProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getDoubleArrayProperty, "[[QString;");
    IMethod setDoubleArrayProperty = SdkAssert.assertMethodExist(propertyTestFormData, "setDoubleArrayProperty", new String[]{"[[QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setDoubleArrayProperty, "V");
    IMethod getDoubleArrayPropertyProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getDoubleArrayPropertyProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getDoubleArrayPropertyProperty, "QDoubleArrayPropertyProperty;");
    IMethod getIntPrimitive = SdkAssert.assertMethodExist(propertyTestFormData, "getIntPrimitive", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIntPrimitive, "I");
    IMethod setIntPrimitive = SdkAssert.assertMethodExist(propertyTestFormData, "setIntPrimitive", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(setIntPrimitive, "V");
    IMethod getIntPrimitiveProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getIntPrimitiveProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIntPrimitiveProperty, "QIntPrimitiveProperty;");
    IMethod getName = SdkAssert.assertMethodExist(propertyTestFormData, "getName", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getName, "QName;");
    IMethod getObjectProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getObjectProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getObjectProperty, "QObject;");
    IMethod setObjectProperty = SdkAssert.assertMethodExist(propertyTestFormData, "setObjectProperty", new String[]{"QObject;"});
    SdkAssert.assertMethodReturnTypeSignature(setObjectProperty, "V");
    IMethod getObjectPropertyProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getObjectPropertyProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getObjectPropertyProperty, "QObjectPropertyProperty;");
    IMethod getPropertyTestNr = SdkAssert.assertMethodExist(propertyTestFormData, "getPropertyTestNr", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getPropertyTestNr, "QLong;");
    IMethod setPropertyTestNr = SdkAssert.assertMethodExist(propertyTestFormData, "setPropertyTestNr", new String[]{"QLong;"});
    SdkAssert.assertMethodReturnTypeSignature(setPropertyTestNr, "V");
    IMethod getPropertyTestNrProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getPropertyTestNrProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getPropertyTestNrProperty, "QPropertyTestNrProperty;");
    IMethod getSingleArrayProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getSingleArrayProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSingleArrayProperty, "[QString;");
    IMethod setSingleArrayProperty = SdkAssert.assertMethodExist(propertyTestFormData, "setSingleArrayProperty", new String[]{"[QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setSingleArrayProperty, "V");
    IMethod getSingleArrayPropertyProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getSingleArrayPropertyProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSingleArrayPropertyProperty, "QSingleArrayPropertyProperty;");
    IMethod getWizards = SdkAssert.assertMethodExist(propertyTestFormData, "getWizards", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getWizards, "QHashMap<QString;QList<QIService;>;>;");
    IMethod setWizards = SdkAssert.assertMethodExist(propertyTestFormData, "setWizards", new String[]{"QHashMap<QString;QList<QIService;>;>;"});
    SdkAssert.assertMethodReturnTypeSignature(setWizards, "V");
    IMethod getWizardsProperty = SdkAssert.assertMethodExist(propertyTestFormData, "getWizardsProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getWizardsProperty, "QWizardsProperty;");

    Assert.assertEquals("inner types count of 'PropertyTestFormData'", 12, propertyTestFormData.innerTypes().list().size());
    // type BoolObjectProperty
    IType boolObjectProperty = SdkAssert.assertTypeExists(propertyTestFormData, "BoolObjectProperty");
    SdkAssert.assertHasFlags(boolObjectProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(boolObjectProperty, "QAbstractPropertyData<QBoolean;>;");

    // fields of BoolObjectProperty
    Assert.assertEquals("field count of 'BoolObjectProperty'", 1, boolObjectProperty.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(boolObjectProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'BoolObjectProperty'", 1, boolObjectProperty.methods().list().size());
    IMethod boolObjectProperty1 = SdkAssert.assertMethodExist(boolObjectProperty, "BoolObjectProperty", new String[]{});
    Assert.assertTrue(boolObjectProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(boolObjectProperty1, null);

    Assert.assertEquals("inner types count of 'BoolObjectProperty'", 0, boolObjectProperty.innerTypes().list().size());
    // type BoolPrimitiveProperty
    IType boolPrimitiveProperty = SdkAssert.assertTypeExists(propertyTestFormData, "BoolPrimitiveProperty");
    SdkAssert.assertHasFlags(boolPrimitiveProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(boolPrimitiveProperty, "QAbstractPropertyData<QBoolean;>;");

    // fields of BoolPrimitiveProperty
    Assert.assertEquals("field count of 'BoolPrimitiveProperty'", 1, boolPrimitiveProperty.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(boolPrimitiveProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'BoolPrimitiveProperty'", 1, boolPrimitiveProperty.methods().list().size());
    IMethod boolPrimitiveProperty1 = SdkAssert.assertMethodExist(boolPrimitiveProperty, "BoolPrimitiveProperty", new String[]{});
    Assert.assertTrue(boolPrimitiveProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(boolPrimitiveProperty1, null);

    Assert.assertEquals("inner types count of 'BoolPrimitiveProperty'", 0, boolPrimitiveProperty.innerTypes().list().size());
    // type ByteArrayProperty
    IType byteArrayProperty = SdkAssert.assertTypeExists(propertyTestFormData, "ByteArrayProperty");
    SdkAssert.assertHasFlags(byteArrayProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(byteArrayProperty, "QAbstractPropertyData<[B>;");

    // fields of ByteArrayProperty
    Assert.assertEquals("field count of 'ByteArrayProperty'", 1, byteArrayProperty.fields().list().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(byteArrayProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'ByteArrayProperty'", 1, byteArrayProperty.methods().list().size());
    IMethod byteArrayProperty1 = SdkAssert.assertMethodExist(byteArrayProperty, "ByteArrayProperty", new String[]{});
    Assert.assertTrue(byteArrayProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(byteArrayProperty1, null);

    Assert.assertEquals("inner types count of 'ByteArrayProperty'", 0, byteArrayProperty.innerTypes().list().size());
    // type ComplexArrayProperty
    IType complexArrayProperty = SdkAssert.assertTypeExists(propertyTestFormData, "ComplexArrayProperty");
    SdkAssert.assertHasFlags(complexArrayProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(complexArrayProperty, "QAbstractPropertyData<[QArrayList<QList<QString;>;>;>;");

    // fields of ComplexArrayProperty
    Assert.assertEquals("field count of 'ComplexArrayProperty'", 1, complexArrayProperty.fields().list().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(complexArrayProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    Assert.assertEquals("method count of 'ComplexArrayProperty'", 1, complexArrayProperty.methods().list().size());
    IMethod complexArrayProperty1 = SdkAssert.assertMethodExist(complexArrayProperty, "ComplexArrayProperty", new String[]{});
    Assert.assertTrue(complexArrayProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(complexArrayProperty1, null);

    Assert.assertEquals("inner types count of 'ComplexArrayProperty'", 0, complexArrayProperty.innerTypes().list().size());
    // type ComplexInnerArrayProperty
    IType complexInnerArrayProperty = SdkAssert.assertTypeExists(propertyTestFormData, "ComplexInnerArrayProperty");
    SdkAssert.assertHasFlags(complexInnerArrayProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(complexInnerArrayProperty, "QAbstractPropertyData<QArrayList<QList<[QString;>;>;>;");

    // fields of ComplexInnerArrayProperty
    Assert.assertEquals("field count of 'ComplexInnerArrayProperty'", 1, complexInnerArrayProperty.fields().list().size());
    IField serialVersionUID5 = SdkAssert.assertFieldExist(complexInnerArrayProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    Assert.assertEquals("method count of 'ComplexInnerArrayProperty'", 1, complexInnerArrayProperty.methods().list().size());
    IMethod complexInnerArrayProperty1 = SdkAssert.assertMethodExist(complexInnerArrayProperty, "ComplexInnerArrayProperty", new String[]{});
    Assert.assertTrue(complexInnerArrayProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(complexInnerArrayProperty1, null);

    Assert.assertEquals("inner types count of 'ComplexInnerArrayProperty'", 0, complexInnerArrayProperty.innerTypes().list().size());
    // type DoubleArrayPropertyProperty
    IType doubleArrayPropertyProperty = SdkAssert.assertTypeExists(propertyTestFormData, "DoubleArrayPropertyProperty");
    SdkAssert.assertHasFlags(doubleArrayPropertyProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(doubleArrayPropertyProperty, "QAbstractPropertyData<[[QString;>;");

    // fields of DoubleArrayPropertyProperty
    Assert.assertEquals("field count of 'DoubleArrayPropertyProperty'", 1, doubleArrayPropertyProperty.fields().list().size());
    IField serialVersionUID6 = SdkAssert.assertFieldExist(doubleArrayPropertyProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    Assert.assertEquals("method count of 'DoubleArrayPropertyProperty'", 1, doubleArrayPropertyProperty.methods().list().size());
    IMethod doubleArrayPropertyProperty1 = SdkAssert.assertMethodExist(doubleArrayPropertyProperty, "DoubleArrayPropertyProperty", new String[]{});
    Assert.assertTrue(doubleArrayPropertyProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(doubleArrayPropertyProperty1, null);

    Assert.assertEquals("inner types count of 'DoubleArrayPropertyProperty'", 0, doubleArrayPropertyProperty.innerTypes().list().size());
    // type IntPrimitiveProperty
    IType intPrimitiveProperty = SdkAssert.assertTypeExists(propertyTestFormData, "IntPrimitiveProperty");
    SdkAssert.assertHasFlags(intPrimitiveProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(intPrimitiveProperty, "QAbstractPropertyData<QInteger;>;");

    // fields of IntPrimitiveProperty
    Assert.assertEquals("field count of 'IntPrimitiveProperty'", 1, intPrimitiveProperty.fields().list().size());
    IField serialVersionUID7 = SdkAssert.assertFieldExist(intPrimitiveProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    Assert.assertEquals("method count of 'IntPrimitiveProperty'", 1, intPrimitiveProperty.methods().list().size());
    IMethod intPrimitiveProperty1 = SdkAssert.assertMethodExist(intPrimitiveProperty, "IntPrimitiveProperty", new String[]{});
    Assert.assertTrue(intPrimitiveProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(intPrimitiveProperty1, null);

    Assert.assertEquals("inner types count of 'IntPrimitiveProperty'", 0, intPrimitiveProperty.innerTypes().list().size());
    // type Name
    IType name = SdkAssert.assertTypeExists(propertyTestFormData, "Name");
    SdkAssert.assertHasFlags(name, 9);
    SdkAssert.assertHasSuperTypeSignature(name, "QAbstractValueFieldData<QString;>;");

    // fields of Name
    Assert.assertEquals("field count of 'Name'", 1, name.fields().list().size());
    IField serialVersionUID8 = SdkAssert.assertFieldExist(name, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");

    Assert.assertEquals("method count of 'Name'", 4, name.methods().list().size());
    IMethod name1 = SdkAssert.assertMethodExist(name, "Name", new String[]{});
    Assert.assertTrue(name1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(name1, null);
    IMethod getIntProperty = SdkAssert.assertMethodExist(name, "getIntProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIntProperty, "I");
    IMethod setIntProperty = SdkAssert.assertMethodExist(name, "setIntProperty", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(setIntProperty, "V");
    IMethod getIntPropertyProperty = SdkAssert.assertMethodExist(name, "getIntPropertyProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIntPropertyProperty, "QIntPropertyProperty;");

    Assert.assertEquals("inner types count of 'Name'", 1, name.innerTypes().list().size());
    // type IntPropertyProperty
    IType intPropertyProperty = SdkAssert.assertTypeExists(name, "IntPropertyProperty");
    SdkAssert.assertHasFlags(intPropertyProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(intPropertyProperty, "QAbstractPropertyData<QInteger;>;");

    // fields of IntPropertyProperty
    Assert.assertEquals("field count of 'IntPropertyProperty'", 1, intPropertyProperty.fields().list().size());
    IField serialVersionUID9 = SdkAssert.assertFieldExist(intPropertyProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID9, 26);
    SdkAssert.assertFieldSignature(serialVersionUID9, "J");

    Assert.assertEquals("method count of 'IntPropertyProperty'", 1, intPropertyProperty.methods().list().size());
    IMethod intPropertyProperty1 = SdkAssert.assertMethodExist(intPropertyProperty, "IntPropertyProperty", new String[]{});
    Assert.assertTrue(intPropertyProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(intPropertyProperty1, null);

    Assert.assertEquals("inner types count of 'IntPropertyProperty'", 0, intPropertyProperty.innerTypes().list().size());
    // type ObjectPropertyProperty
    IType objectPropertyProperty = SdkAssert.assertTypeExists(propertyTestFormData, "ObjectPropertyProperty");
    SdkAssert.assertHasFlags(objectPropertyProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(objectPropertyProperty, "QAbstractPropertyData<QObject;>;");

    // fields of ObjectPropertyProperty
    Assert.assertEquals("field count of 'ObjectPropertyProperty'", 1, objectPropertyProperty.fields().list().size());
    IField serialVersionUID10 = SdkAssert.assertFieldExist(objectPropertyProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID10, 26);
    SdkAssert.assertFieldSignature(serialVersionUID10, "J");

    Assert.assertEquals("method count of 'ObjectPropertyProperty'", 1, objectPropertyProperty.methods().list().size());
    IMethod objectPropertyProperty1 = SdkAssert.assertMethodExist(objectPropertyProperty, "ObjectPropertyProperty", new String[]{});
    Assert.assertTrue(objectPropertyProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(objectPropertyProperty1, null);

    Assert.assertEquals("inner types count of 'ObjectPropertyProperty'", 0, objectPropertyProperty.innerTypes().list().size());
    // type PropertyTestNrProperty
    IType propertyTestNrProperty = SdkAssert.assertTypeExists(propertyTestFormData, "PropertyTestNrProperty");
    SdkAssert.assertHasFlags(propertyTestNrProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(propertyTestNrProperty, "QAbstractPropertyData<QLong;>;");

    // fields of PropertyTestNrProperty
    Assert.assertEquals("field count of 'PropertyTestNrProperty'", 1, propertyTestNrProperty.fields().list().size());
    IField serialVersionUID11 = SdkAssert.assertFieldExist(propertyTestNrProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID11, 26);
    SdkAssert.assertFieldSignature(serialVersionUID11, "J");

    Assert.assertEquals("method count of 'PropertyTestNrProperty'", 1, propertyTestNrProperty.methods().list().size());
    IMethod propertyTestNrProperty1 = SdkAssert.assertMethodExist(propertyTestNrProperty, "PropertyTestNrProperty", new String[]{});
    Assert.assertTrue(propertyTestNrProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(propertyTestNrProperty1, null);

    Assert.assertEquals("inner types count of 'PropertyTestNrProperty'", 0, propertyTestNrProperty.innerTypes().list().size());
    // type SingleArrayPropertyProperty
    IType singleArrayPropertyProperty = SdkAssert.assertTypeExists(propertyTestFormData, "SingleArrayPropertyProperty");
    SdkAssert.assertHasFlags(singleArrayPropertyProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(singleArrayPropertyProperty, "QAbstractPropertyData<[QString;>;");

    // fields of SingleArrayPropertyProperty
    Assert.assertEquals("field count of 'SingleArrayPropertyProperty'", 1, singleArrayPropertyProperty.fields().list().size());
    IField serialVersionUID12 = SdkAssert.assertFieldExist(singleArrayPropertyProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID12, 26);
    SdkAssert.assertFieldSignature(serialVersionUID12, "J");

    Assert.assertEquals("method count of 'SingleArrayPropertyProperty'", 1, singleArrayPropertyProperty.methods().list().size());
    IMethod singleArrayPropertyProperty1 = SdkAssert.assertMethodExist(singleArrayPropertyProperty, "SingleArrayPropertyProperty", new String[]{});
    Assert.assertTrue(singleArrayPropertyProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(singleArrayPropertyProperty1, null);

    Assert.assertEquals("inner types count of 'SingleArrayPropertyProperty'", 0, singleArrayPropertyProperty.innerTypes().list().size());
    // type WizardsProperty
    IType wizardsProperty = SdkAssert.assertTypeExists(propertyTestFormData, "WizardsProperty");
    SdkAssert.assertHasFlags(wizardsProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(wizardsProperty, "QAbstractPropertyData<QHashMap<QString;QList<QIService;>;>;>;");

    // fields of WizardsProperty
    Assert.assertEquals("field count of 'WizardsProperty'", 1, wizardsProperty.fields().list().size());
    IField serialVersionUID13 = SdkAssert.assertFieldExist(wizardsProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID13, 26);
    SdkAssert.assertFieldSignature(serialVersionUID13, "J");

    Assert.assertEquals("method count of 'WizardsProperty'", 1, wizardsProperty.methods().list().size());
    IMethod wizardsProperty1 = SdkAssert.assertMethodExist(wizardsProperty, "WizardsProperty", new String[]{});
    Assert.assertTrue(wizardsProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(wizardsProperty1, null);

    Assert.assertEquals("inner types count of 'WizardsProperty'", 0, wizardsProperty.innerTypes().list().size());
  }

}
