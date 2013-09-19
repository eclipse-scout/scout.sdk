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
import org.eclipse.scout.sdk.internal.workspace.dto.FormDataDtoUpdateOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

public class FormPropertiesTest extends AbstractSdkTestWithFormDataProject {

  @Test
  public void testCreateFormData() throws Exception {
    IType form = SdkAssert.assertTypeExists("formdata.client.ui.forms.PropertyTestForm");

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(form);

    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, op);

    testApiOfPropertyTestFormData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfPropertyTestFormData() throws Exception {
    // type PropertyTestFormData
    IType propertyTestFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.PropertyTestFormData");
    SdkAssert.assertHasFlags(propertyTestFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(propertyTestFormData, "QAbstractFormData;");

    // fields of PropertyTestFormData
    SdkAssert.assertEquals("field count of 'PropertyTestFormData'", 1, propertyTestFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(propertyTestFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'PropertyTestFormData'", 35, propertyTestFormData.getMethods().length);
    IMethod propertyTestFormData1 = SdkAssert.assertMethodExist(propertyTestFormData, "PropertyTestFormData", new String[]{});
    SdkAssert.assertTrue(propertyTestFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(propertyTestFormData1, "V");
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

    SdkAssert.assertEquals("inner types count of 'PropertyTestFormData'", 12, propertyTestFormData.getTypes().length);
    // type BoolObjectProperty
    IType boolObjectProperty = SdkAssert.assertTypeExists(propertyTestFormData, "BoolObjectProperty");
    SdkAssert.assertHasFlags(boolObjectProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(boolObjectProperty, "QAbstractPropertyData<QBoolean;>;");

    // fields of BoolObjectProperty
    SdkAssert.assertEquals("field count of 'BoolObjectProperty'", 1, boolObjectProperty.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(boolObjectProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'BoolObjectProperty'", 1, boolObjectProperty.getMethods().length);
    IMethod boolObjectProperty1 = SdkAssert.assertMethodExist(boolObjectProperty, "BoolObjectProperty", new String[]{});
    SdkAssert.assertTrue(boolObjectProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(boolObjectProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'BoolObjectProperty'", 0, boolObjectProperty.getTypes().length);
    // type BoolPrimitiveProperty
    IType boolPrimitiveProperty = SdkAssert.assertTypeExists(propertyTestFormData, "BoolPrimitiveProperty");
    SdkAssert.assertHasFlags(boolPrimitiveProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(boolPrimitiveProperty, "QAbstractPropertyData<QBoolean;>;");

    // fields of BoolPrimitiveProperty
    SdkAssert.assertEquals("field count of 'BoolPrimitiveProperty'", 1, boolPrimitiveProperty.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(boolPrimitiveProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'BoolPrimitiveProperty'", 1, boolPrimitiveProperty.getMethods().length);
    IMethod boolPrimitiveProperty1 = SdkAssert.assertMethodExist(boolPrimitiveProperty, "BoolPrimitiveProperty", new String[]{});
    SdkAssert.assertTrue(boolPrimitiveProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(boolPrimitiveProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'BoolPrimitiveProperty'", 0, boolPrimitiveProperty.getTypes().length);
    // type ByteArrayProperty
    IType byteArrayProperty = SdkAssert.assertTypeExists(propertyTestFormData, "ByteArrayProperty");
    SdkAssert.assertHasFlags(byteArrayProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(byteArrayProperty, "QAbstractPropertyData<[B>;");

    // fields of ByteArrayProperty
    SdkAssert.assertEquals("field count of 'ByteArrayProperty'", 1, byteArrayProperty.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(byteArrayProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'ByteArrayProperty'", 1, byteArrayProperty.getMethods().length);
    IMethod byteArrayProperty1 = SdkAssert.assertMethodExist(byteArrayProperty, "ByteArrayProperty", new String[]{});
    SdkAssert.assertTrue(byteArrayProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(byteArrayProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'ByteArrayProperty'", 0, byteArrayProperty.getTypes().length);
    // type ComplexArrayProperty
    IType complexArrayProperty = SdkAssert.assertTypeExists(propertyTestFormData, "ComplexArrayProperty");
    SdkAssert.assertHasFlags(complexArrayProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(complexArrayProperty, "QAbstractPropertyData<[QArrayList<QList<QString;>;>;>;");

    // fields of ComplexArrayProperty
    SdkAssert.assertEquals("field count of 'ComplexArrayProperty'", 1, complexArrayProperty.getFields().length);
    IField serialVersionUID4 = SdkAssert.assertFieldExist(complexArrayProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    SdkAssert.assertEquals("method count of 'ComplexArrayProperty'", 1, complexArrayProperty.getMethods().length);
    IMethod complexArrayProperty1 = SdkAssert.assertMethodExist(complexArrayProperty, "ComplexArrayProperty", new String[]{});
    SdkAssert.assertTrue(complexArrayProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(complexArrayProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'ComplexArrayProperty'", 0, complexArrayProperty.getTypes().length);
    // type ComplexInnerArrayProperty
    IType complexInnerArrayProperty = SdkAssert.assertTypeExists(propertyTestFormData, "ComplexInnerArrayProperty");
    SdkAssert.assertHasFlags(complexInnerArrayProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(complexInnerArrayProperty, "QAbstractPropertyData<QArrayList<QList<[QString;>;>;>;");

    // fields of ComplexInnerArrayProperty
    SdkAssert.assertEquals("field count of 'ComplexInnerArrayProperty'", 1, complexInnerArrayProperty.getFields().length);
    IField serialVersionUID5 = SdkAssert.assertFieldExist(complexInnerArrayProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    SdkAssert.assertEquals("method count of 'ComplexInnerArrayProperty'", 1, complexInnerArrayProperty.getMethods().length);
    IMethod complexInnerArrayProperty1 = SdkAssert.assertMethodExist(complexInnerArrayProperty, "ComplexInnerArrayProperty", new String[]{});
    SdkAssert.assertTrue(complexInnerArrayProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(complexInnerArrayProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'ComplexInnerArrayProperty'", 0, complexInnerArrayProperty.getTypes().length);
    // type DoubleArrayPropertyProperty
    IType doubleArrayPropertyProperty = SdkAssert.assertTypeExists(propertyTestFormData, "DoubleArrayPropertyProperty");
    SdkAssert.assertHasFlags(doubleArrayPropertyProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(doubleArrayPropertyProperty, "QAbstractPropertyData<[[QString;>;");

    // fields of DoubleArrayPropertyProperty
    SdkAssert.assertEquals("field count of 'DoubleArrayPropertyProperty'", 1, doubleArrayPropertyProperty.getFields().length);
    IField serialVersionUID6 = SdkAssert.assertFieldExist(doubleArrayPropertyProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    SdkAssert.assertEquals("method count of 'DoubleArrayPropertyProperty'", 1, doubleArrayPropertyProperty.getMethods().length);
    IMethod doubleArrayPropertyProperty1 = SdkAssert.assertMethodExist(doubleArrayPropertyProperty, "DoubleArrayPropertyProperty", new String[]{});
    SdkAssert.assertTrue(doubleArrayPropertyProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(doubleArrayPropertyProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'DoubleArrayPropertyProperty'", 0, doubleArrayPropertyProperty.getTypes().length);
    // type IntPrimitiveProperty
    IType intPrimitiveProperty = SdkAssert.assertTypeExists(propertyTestFormData, "IntPrimitiveProperty");
    SdkAssert.assertHasFlags(intPrimitiveProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(intPrimitiveProperty, "QAbstractPropertyData<QInteger;>;");

    // fields of IntPrimitiveProperty
    SdkAssert.assertEquals("field count of 'IntPrimitiveProperty'", 1, intPrimitiveProperty.getFields().length);
    IField serialVersionUID7 = SdkAssert.assertFieldExist(intPrimitiveProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    SdkAssert.assertEquals("method count of 'IntPrimitiveProperty'", 1, intPrimitiveProperty.getMethods().length);
    IMethod intPrimitiveProperty1 = SdkAssert.assertMethodExist(intPrimitiveProperty, "IntPrimitiveProperty", new String[]{});
    SdkAssert.assertTrue(intPrimitiveProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(intPrimitiveProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'IntPrimitiveProperty'", 0, intPrimitiveProperty.getTypes().length);
    // type Name
    IType name = SdkAssert.assertTypeExists(propertyTestFormData, "Name");
    SdkAssert.assertHasFlags(name, 9);
    SdkAssert.assertHasSuperTypeSignature(name, "QAbstractValueFieldData<QString;>;");

    // fields of Name
    SdkAssert.assertEquals("field count of 'Name'", 1, name.getFields().length);
    IField serialVersionUID8 = SdkAssert.assertFieldExist(name, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");

    SdkAssert.assertEquals("method count of 'Name'", 5, name.getMethods().length);
    IMethod name1 = SdkAssert.assertMethodExist(name, "Name", new String[]{});
    SdkAssert.assertTrue(name1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(name1, "V");
    IMethod getIntProperty = SdkAssert.assertMethodExist(name, "getIntProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIntProperty, "I");
    IMethod setIntProperty = SdkAssert.assertMethodExist(name, "setIntProperty", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(setIntProperty, "V");
    IMethod getIntPropertyProperty = SdkAssert.assertMethodExist(name, "getIntPropertyProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIntPropertyProperty, "QIntPropertyProperty;");
    IMethod initValidationRules = SdkAssert.assertMethodExist(name, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'Name'", 1, name.getTypes().length);
    // type IntPropertyProperty
    IType intPropertyProperty = SdkAssert.assertTypeExists(name, "IntPropertyProperty");
    SdkAssert.assertHasFlags(intPropertyProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(intPropertyProperty, "QAbstractPropertyData<QInteger;>;");

    // fields of IntPropertyProperty
    SdkAssert.assertEquals("field count of 'IntPropertyProperty'", 1, intPropertyProperty.getFields().length);
    IField serialVersionUID9 = SdkAssert.assertFieldExist(intPropertyProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID9, 26);
    SdkAssert.assertFieldSignature(serialVersionUID9, "J");

    SdkAssert.assertEquals("method count of 'IntPropertyProperty'", 1, intPropertyProperty.getMethods().length);
    IMethod intPropertyProperty1 = SdkAssert.assertMethodExist(intPropertyProperty, "IntPropertyProperty", new String[]{});
    SdkAssert.assertTrue(intPropertyProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(intPropertyProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'IntPropertyProperty'", 0, intPropertyProperty.getTypes().length);
    // type ObjectPropertyProperty
    IType objectPropertyProperty = SdkAssert.assertTypeExists(propertyTestFormData, "ObjectPropertyProperty");
    SdkAssert.assertHasFlags(objectPropertyProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(objectPropertyProperty, "QAbstractPropertyData<QObject;>;");

    // fields of ObjectPropertyProperty
    SdkAssert.assertEquals("field count of 'ObjectPropertyProperty'", 1, objectPropertyProperty.getFields().length);
    IField serialVersionUID10 = SdkAssert.assertFieldExist(objectPropertyProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID10, 26);
    SdkAssert.assertFieldSignature(serialVersionUID10, "J");

    SdkAssert.assertEquals("method count of 'ObjectPropertyProperty'", 1, objectPropertyProperty.getMethods().length);
    IMethod objectPropertyProperty1 = SdkAssert.assertMethodExist(objectPropertyProperty, "ObjectPropertyProperty", new String[]{});
    SdkAssert.assertTrue(objectPropertyProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(objectPropertyProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'ObjectPropertyProperty'", 0, objectPropertyProperty.getTypes().length);
    // type PropertyTestNrProperty
    IType propertyTestNrProperty = SdkAssert.assertTypeExists(propertyTestFormData, "PropertyTestNrProperty");
    SdkAssert.assertHasFlags(propertyTestNrProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(propertyTestNrProperty, "QAbstractPropertyData<QLong;>;");

    // fields of PropertyTestNrProperty
    SdkAssert.assertEquals("field count of 'PropertyTestNrProperty'", 1, propertyTestNrProperty.getFields().length);
    IField serialVersionUID11 = SdkAssert.assertFieldExist(propertyTestNrProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID11, 26);
    SdkAssert.assertFieldSignature(serialVersionUID11, "J");

    SdkAssert.assertEquals("method count of 'PropertyTestNrProperty'", 1, propertyTestNrProperty.getMethods().length);
    IMethod propertyTestNrProperty1 = SdkAssert.assertMethodExist(propertyTestNrProperty, "PropertyTestNrProperty", new String[]{});
    SdkAssert.assertTrue(propertyTestNrProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(propertyTestNrProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'PropertyTestNrProperty'", 0, propertyTestNrProperty.getTypes().length);
    // type SingleArrayPropertyProperty
    IType singleArrayPropertyProperty = SdkAssert.assertTypeExists(propertyTestFormData, "SingleArrayPropertyProperty");
    SdkAssert.assertHasFlags(singleArrayPropertyProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(singleArrayPropertyProperty, "QAbstractPropertyData<[QString;>;");

    // fields of SingleArrayPropertyProperty
    SdkAssert.assertEquals("field count of 'SingleArrayPropertyProperty'", 1, singleArrayPropertyProperty.getFields().length);
    IField serialVersionUID12 = SdkAssert.assertFieldExist(singleArrayPropertyProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID12, 26);
    SdkAssert.assertFieldSignature(serialVersionUID12, "J");

    SdkAssert.assertEquals("method count of 'SingleArrayPropertyProperty'", 1, singleArrayPropertyProperty.getMethods().length);
    IMethod singleArrayPropertyProperty1 = SdkAssert.assertMethodExist(singleArrayPropertyProperty, "SingleArrayPropertyProperty", new String[]{});
    SdkAssert.assertTrue(singleArrayPropertyProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(singleArrayPropertyProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'SingleArrayPropertyProperty'", 0, singleArrayPropertyProperty.getTypes().length);
    // type WizardsProperty
    IType wizardsProperty = SdkAssert.assertTypeExists(propertyTestFormData, "WizardsProperty");
    SdkAssert.assertHasFlags(wizardsProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(wizardsProperty, "QAbstractPropertyData<QHashMap<QString;QList<QIService;>;>;>;");

    // fields of WizardsProperty
    SdkAssert.assertEquals("field count of 'WizardsProperty'", 1, wizardsProperty.getFields().length);
    IField serialVersionUID13 = SdkAssert.assertFieldExist(wizardsProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID13, 26);
    SdkAssert.assertFieldSignature(serialVersionUID13, "J");

    SdkAssert.assertEquals("method count of 'WizardsProperty'", 1, wizardsProperty.getMethods().length);
    IMethod wizardsProperty1 = SdkAssert.assertMethodExist(wizardsProperty, "WizardsProperty", new String[]{});
    SdkAssert.assertTrue(wizardsProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(wizardsProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'WizardsProperty'", 0, wizardsProperty.getTypes().length);
  }

}
