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

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.PropertyTestForm;

public class FormPropertiesTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(PropertyTestForm.class.getName(), FormPropertiesTest::testApiOfPropertyTestFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfPropertyTestFormData(IType propertyTestFormData) {
    // type PropertyTestFormData
    assertHasFlags(propertyTestFormData, 1);
    assertHasSuperClass(propertyTestFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");

    // fields of PropertyTestFormData
    assertEquals(1, propertyTestFormData.fields().stream().count(), "field count of 'PropertyTestFormData'");
    IField serialVersionUID = assertFieldExist(propertyTestFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(34, propertyTestFormData.methods().stream().count(), "method count of 'PropertyTestFormData'");
    IMethod getBoolObject = assertMethodExist(propertyTestFormData, "getBoolObject", new String[]{});
    assertMethodReturnType(getBoolObject, Boolean.class.getName());
    IMethod setBoolObject = assertMethodExist(propertyTestFormData, "setBoolObject", new String[]{Boolean.class.getName()});
    assertMethodReturnType(setBoolObject, "void");
    IMethod getBoolObjectProperty = assertMethodExist(propertyTestFormData, "getBoolObjectProperty", new String[]{});
    assertMethodReturnType(getBoolObjectProperty, "formdata.shared.services.process.PropertyTestFormData$BoolObjectProperty");
    IMethod isBoolPrimitive = assertMethodExist(propertyTestFormData, "isBoolPrimitive", new String[]{});
    assertMethodReturnType(isBoolPrimitive, "boolean");
    IMethod setBoolPrimitive = assertMethodExist(propertyTestFormData, "setBoolPrimitive", new String[]{"boolean"});
    assertMethodReturnType(setBoolPrimitive, "void");
    IMethod getBoolPrimitiveProperty = assertMethodExist(propertyTestFormData, "getBoolPrimitiveProperty", new String[]{});
    assertMethodReturnType(getBoolPrimitiveProperty, "formdata.shared.services.process.PropertyTestFormData$BoolPrimitiveProperty");
    IMethod getByteArray = assertMethodExist(propertyTestFormData, "getByteArray", new String[]{});
    assertMethodReturnType(getByteArray, "byte[]");
    IMethod setByteArray = assertMethodExist(propertyTestFormData, "setByteArray", new String[]{"byte[]"});
    assertMethodReturnType(setByteArray, "void");
    IMethod getByteArrayProperty = assertMethodExist(propertyTestFormData, "getByteArrayProperty", new String[]{});
    assertMethodReturnType(getByteArrayProperty, "formdata.shared.services.process.PropertyTestFormData$ByteArrayProperty");
    IMethod getComplexArray = assertMethodExist(propertyTestFormData, "getComplexArray", new String[]{});
    assertMethodReturnType(getComplexArray, "java.util.ArrayList<java.util.List<java.lang.String>>[]");
    IMethod setComplexArray = assertMethodExist(propertyTestFormData, "setComplexArray", new String[]{"java.util.ArrayList<java.util.List<java.lang.String>>[]"});
    assertMethodReturnType(setComplexArray, "void");
    IMethod getComplexArrayProperty = assertMethodExist(propertyTestFormData, "getComplexArrayProperty", new String[]{});
    assertMethodReturnType(getComplexArrayProperty, "formdata.shared.services.process.PropertyTestFormData$ComplexArrayProperty");
    IMethod getComplexInnerArray = assertMethodExist(propertyTestFormData, "getComplexInnerArray", new String[]{});
    assertMethodReturnType(getComplexInnerArray, "java.util.ArrayList<java.util.List<java.lang.String[]>>");
    IMethod setComplexInnerArray = assertMethodExist(propertyTestFormData, "setComplexInnerArray", new String[]{"java.util.ArrayList<java.util.List<java.lang.String[]>>"});
    assertMethodReturnType(setComplexInnerArray, "void");
    IMethod getComplexInnerArrayProperty = assertMethodExist(propertyTestFormData, "getComplexInnerArrayProperty", new String[]{});
    assertMethodReturnType(getComplexInnerArrayProperty, "formdata.shared.services.process.PropertyTestFormData$ComplexInnerArrayProperty");
    IMethod getDoubleArrayProperty = assertMethodExist(propertyTestFormData, "getDoubleArrayProperty", new String[]{});
    assertMethodReturnType(getDoubleArrayProperty, "java.lang.String[][]");
    IMethod setDoubleArrayProperty = assertMethodExist(propertyTestFormData, "setDoubleArrayProperty", new String[]{"java.lang.String[][]"});
    assertMethodReturnType(setDoubleArrayProperty, "void");
    IMethod getDoubleArrayPropertyProperty = assertMethodExist(propertyTestFormData, "getDoubleArrayPropertyProperty", new String[]{});
    assertMethodReturnType(getDoubleArrayPropertyProperty, "formdata.shared.services.process.PropertyTestFormData$DoubleArrayPropertyProperty");
    IMethod getIntPrimitive = assertMethodExist(propertyTestFormData, "getIntPrimitive", new String[]{});
    assertMethodReturnType(getIntPrimitive, "int");
    IMethod setIntPrimitive = assertMethodExist(propertyTestFormData, "setIntPrimitive", new String[]{"int"});
    assertMethodReturnType(setIntPrimitive, "void");
    IMethod getIntPrimitiveProperty = assertMethodExist(propertyTestFormData, "getIntPrimitiveProperty", new String[]{});
    assertMethodReturnType(getIntPrimitiveProperty, "formdata.shared.services.process.PropertyTestFormData$IntPrimitiveProperty");
    IMethod getName = assertMethodExist(propertyTestFormData, "getName", new String[]{});
    assertMethodReturnType(getName, "formdata.shared.services.process.PropertyTestFormData$Name");
    IMethod getObjectProperty = assertMethodExist(propertyTestFormData, "getObjectProperty", new String[]{});
    assertMethodReturnType(getObjectProperty, "java.lang.Object");
    IMethod setObjectProperty = assertMethodExist(propertyTestFormData, "setObjectProperty", new String[]{"java.lang.Object"});
    assertMethodReturnType(setObjectProperty, "void");
    IMethod getObjectPropertyProperty = assertMethodExist(propertyTestFormData, "getObjectPropertyProperty", new String[]{});
    assertMethodReturnType(getObjectPropertyProperty, "formdata.shared.services.process.PropertyTestFormData$ObjectPropertyProperty");
    IMethod getPropertyTestNr = assertMethodExist(propertyTestFormData, "getPropertyTestNr", new String[]{});
    assertMethodReturnType(getPropertyTestNr, Long.class.getName());
    IMethod setPropertyTestNr = assertMethodExist(propertyTestFormData, "setPropertyTestNr", new String[]{Long.class.getName()});
    assertMethodReturnType(setPropertyTestNr, "void");
    IMethod getPropertyTestNrProperty = assertMethodExist(propertyTestFormData, "getPropertyTestNrProperty", new String[]{});
    assertMethodReturnType(getPropertyTestNrProperty, "formdata.shared.services.process.PropertyTestFormData$PropertyTestNrProperty");
    IMethod getSingleArrayProperty = assertMethodExist(propertyTestFormData, "getSingleArrayProperty", new String[]{});
    assertMethodReturnType(getSingleArrayProperty, "java.lang.String[]");
    IMethod setSingleArrayProperty = assertMethodExist(propertyTestFormData, "setSingleArrayProperty", new String[]{"java.lang.String[]"});
    assertMethodReturnType(setSingleArrayProperty, "void");
    IMethod getSingleArrayPropertyProperty = assertMethodExist(propertyTestFormData, "getSingleArrayPropertyProperty", new String[]{});
    assertMethodReturnType(getSingleArrayPropertyProperty, "formdata.shared.services.process.PropertyTestFormData$SingleArrayPropertyProperty");
    IMethod getWizards = assertMethodExist(propertyTestFormData, "getWizards", new String[]{});
    assertMethodReturnType(getWizards, "java.util.HashMap<java.lang.String,java.util.List<org.eclipse.scout.rt.platform.service.IService>>");
    IMethod setWizards = assertMethodExist(propertyTestFormData, "setWizards", new String[]{"java.util.HashMap<java.lang.String,java.util.List<org.eclipse.scout.rt.platform.service.IService>>"});
    assertMethodReturnType(setWizards, "void");
    IMethod getWizardsProperty = assertMethodExist(propertyTestFormData, "getWizardsProperty", new String[]{});
    assertMethodReturnType(getWizardsProperty, "formdata.shared.services.process.PropertyTestFormData$WizardsProperty");

    assertEquals(12, propertyTestFormData.innerTypes().stream().count(), "inner types count of 'PropertyTestFormData'");
    // type BoolObjectProperty
    IType boolObjectProperty = assertTypeExists(propertyTestFormData, "BoolObjectProperty");
    assertHasFlags(boolObjectProperty, 9);
    assertHasSuperClass(boolObjectProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Boolean>");

    // fields of BoolObjectProperty
    assertEquals(1, boolObjectProperty.fields().stream().count(), "field count of 'BoolObjectProperty'");
    IField serialVersionUID1 = assertFieldExist(boolObjectProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, boolObjectProperty.methods().stream().count(), "method count of 'BoolObjectProperty'");

    assertEquals(0, boolObjectProperty.innerTypes().stream().count(), "inner types count of 'BoolObjectProperty'");
    // type BoolPrimitiveProperty
    IType boolPrimitiveProperty = assertTypeExists(propertyTestFormData, "BoolPrimitiveProperty");
    assertHasFlags(boolPrimitiveProperty, 9);
    assertHasSuperClass(boolPrimitiveProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Boolean>");

    // fields of BoolPrimitiveProperty
    assertEquals(1, boolPrimitiveProperty.fields().stream().count(), "field count of 'BoolPrimitiveProperty'");
    IField serialVersionUID2 = assertFieldExist(boolPrimitiveProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, boolPrimitiveProperty.methods().stream().count(), "method count of 'BoolPrimitiveProperty'");

    assertEquals(0, boolPrimitiveProperty.innerTypes().stream().count(), "inner types count of 'BoolPrimitiveProperty'");
    // type ByteArrayProperty
    IType byteArrayProperty = assertTypeExists(propertyTestFormData, "ByteArrayProperty");
    assertHasFlags(byteArrayProperty, 9);
    assertHasSuperClass(byteArrayProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<byte[]>");

    // fields of ByteArrayProperty
    assertEquals(1, byteArrayProperty.fields().stream().count(), "field count of 'ByteArrayProperty'");
    IField serialVersionUID3 = assertFieldExist(byteArrayProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(0, byteArrayProperty.methods().stream().count(), "method count of 'ByteArrayProperty'");

    assertEquals(0, byteArrayProperty.innerTypes().stream().count(), "inner types count of 'ByteArrayProperty'");
    // type ComplexArrayProperty
    IType complexArrayProperty = assertTypeExists(propertyTestFormData, "ComplexArrayProperty");
    assertHasFlags(complexArrayProperty, 9);
    assertHasSuperClass(complexArrayProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.util.ArrayList<java.util.List<java.lang.String>>[]>");

    // fields of ComplexArrayProperty
    assertEquals(1, complexArrayProperty.fields().stream().count(), "field count of 'ComplexArrayProperty'");
    IField serialVersionUID4 = assertFieldExist(complexArrayProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(0, complexArrayProperty.methods().stream().count(), "method count of 'ComplexArrayProperty'");

    assertEquals(0, complexArrayProperty.innerTypes().stream().count(), "inner types count of 'ComplexArrayProperty'");
    // type ComplexInnerArrayProperty
    IType complexInnerArrayProperty = assertTypeExists(propertyTestFormData, "ComplexInnerArrayProperty");
    assertHasFlags(complexInnerArrayProperty, 9);
    assertHasSuperClass(complexInnerArrayProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.util.ArrayList<java.util.List<java.lang.String[]>>>");

    // fields of ComplexInnerArrayProperty
    assertEquals(1, complexInnerArrayProperty.fields().stream().count(), "field count of 'ComplexInnerArrayProperty'");
    IField serialVersionUID5 = assertFieldExist(complexInnerArrayProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(0, complexInnerArrayProperty.methods().stream().count(), "method count of 'ComplexInnerArrayProperty'");

    assertEquals(0, complexInnerArrayProperty.innerTypes().stream().count(), "inner types count of 'ComplexInnerArrayProperty'");
    // type DoubleArrayPropertyProperty
    IType doubleArrayPropertyProperty = assertTypeExists(propertyTestFormData, "DoubleArrayPropertyProperty");
    assertHasFlags(doubleArrayPropertyProperty, 9);
    assertHasSuperClass(doubleArrayPropertyProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.String[][]>");

    // fields of DoubleArrayPropertyProperty
    assertEquals(1, doubleArrayPropertyProperty.fields().stream().count(), "field count of 'DoubleArrayPropertyProperty'");
    IField serialVersionUID6 = assertFieldExist(doubleArrayPropertyProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");

    assertEquals(0, doubleArrayPropertyProperty.methods().stream().count(), "method count of 'DoubleArrayPropertyProperty'");

    assertEquals(0, doubleArrayPropertyProperty.innerTypes().stream().count(), "inner types count of 'DoubleArrayPropertyProperty'");
    // type IntPrimitiveProperty
    IType intPrimitiveProperty = assertTypeExists(propertyTestFormData, "IntPrimitiveProperty");
    assertHasFlags(intPrimitiveProperty, 9);
    assertHasSuperClass(intPrimitiveProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Integer>");

    // fields of IntPrimitiveProperty
    assertEquals(1, intPrimitiveProperty.fields().stream().count(), "field count of 'IntPrimitiveProperty'");
    IField serialVersionUID7 = assertFieldExist(intPrimitiveProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID7, 26);
    assertFieldType(serialVersionUID7, "long");

    assertEquals(0, intPrimitiveProperty.methods().stream().count(), "method count of 'IntPrimitiveProperty'");

    assertEquals(0, intPrimitiveProperty.innerTypes().stream().count(), "inner types count of 'IntPrimitiveProperty'");
    // type Name
    IType name = assertTypeExists(propertyTestFormData, "Name");
    assertHasFlags(name, 9);
    assertHasSuperClass(name, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of Name
    assertEquals(1, name.fields().stream().count(), "field count of 'Name'");
    IField serialVersionUID8 = assertFieldExist(name, "serialVersionUID");
    assertHasFlags(serialVersionUID8, 26);
    assertFieldType(serialVersionUID8, "long");

    assertEquals(3, name.methods().stream().count(), "method count of 'Name'");
    IMethod getIntProperty = assertMethodExist(name, "getIntProperty", new String[]{});
    assertMethodReturnType(getIntProperty, "int");
    IMethod setIntProperty = assertMethodExist(name, "setIntProperty", new String[]{"int"});
    assertMethodReturnType(setIntProperty, "void");
    IMethod getIntPropertyProperty = assertMethodExist(name, "getIntPropertyProperty", new String[]{});
    assertMethodReturnType(getIntPropertyProperty, "formdata.shared.services.process.PropertyTestFormData$Name$IntPropertyProperty");

    assertEquals(1, name.innerTypes().stream().count(), "inner types count of 'Name'");
    // type IntPropertyProperty
    IType intPropertyProperty = assertTypeExists(name, "IntPropertyProperty");
    assertHasFlags(intPropertyProperty, 9);
    assertHasSuperClass(intPropertyProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Integer>");

    // fields of IntPropertyProperty
    assertEquals(1, intPropertyProperty.fields().stream().count(), "field count of 'IntPropertyProperty'");
    IField serialVersionUID9 = assertFieldExist(intPropertyProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID9, 26);
    assertFieldType(serialVersionUID9, "long");

    assertEquals(0, intPropertyProperty.methods().stream().count(), "method count of 'IntPropertyProperty'");

    assertEquals(0, intPropertyProperty.innerTypes().stream().count(), "inner types count of 'IntPropertyProperty'");
    // type ObjectPropertyProperty
    IType objectPropertyProperty = assertTypeExists(propertyTestFormData, "ObjectPropertyProperty");
    assertHasFlags(objectPropertyProperty, 9);
    assertHasSuperClass(objectPropertyProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Object>");

    // fields of ObjectPropertyProperty
    assertEquals(1, objectPropertyProperty.fields().stream().count(), "field count of 'ObjectPropertyProperty'");
    IField serialVersionUID10 = assertFieldExist(objectPropertyProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID10, 26);
    assertFieldType(serialVersionUID10, "long");

    assertEquals(0, objectPropertyProperty.methods().stream().count(), "method count of 'ObjectPropertyProperty'");

    assertEquals(0, objectPropertyProperty.innerTypes().stream().count(), "inner types count of 'ObjectPropertyProperty'");
    // type PropertyTestNrProperty
    IType propertyTestNrProperty = assertTypeExists(propertyTestFormData, "PropertyTestNrProperty");
    assertHasFlags(propertyTestNrProperty, 9);
    assertHasSuperClass(propertyTestNrProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Long>");

    // fields of PropertyTestNrProperty
    assertEquals(1, propertyTestNrProperty.fields().stream().count(), "field count of 'PropertyTestNrProperty'");
    IField serialVersionUID11 = assertFieldExist(propertyTestNrProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID11, 26);
    assertFieldType(serialVersionUID11, "long");

    assertEquals(0, propertyTestNrProperty.methods().stream().count(), "method count of 'PropertyTestNrProperty'");

    assertEquals(0, propertyTestNrProperty.innerTypes().stream().count(), "inner types count of 'PropertyTestNrProperty'");
    // type SingleArrayPropertyProperty
    IType singleArrayPropertyProperty = assertTypeExists(propertyTestFormData, "SingleArrayPropertyProperty");
    assertHasFlags(singleArrayPropertyProperty, 9);
    assertHasSuperClass(singleArrayPropertyProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.String[]>");

    // fields of SingleArrayPropertyProperty
    assertEquals(1, singleArrayPropertyProperty.fields().stream().count(), "field count of 'SingleArrayPropertyProperty'");
    IField serialVersionUID12 = assertFieldExist(singleArrayPropertyProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID12, 26);
    assertFieldType(serialVersionUID12, "long");

    assertEquals(0, singleArrayPropertyProperty.methods().stream().count(), "method count of 'SingleArrayPropertyProperty'");

    assertEquals(0, singleArrayPropertyProperty.innerTypes().stream().count(), "inner types count of 'SingleArrayPropertyProperty'");
    // type WizardsProperty
    IType wizardsProperty = assertTypeExists(propertyTestFormData, "WizardsProperty");
    assertHasFlags(wizardsProperty, 9);
    assertHasSuperClass(wizardsProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.util.HashMap<java.lang.String,java.util.List<org.eclipse.scout.rt.platform.service.IService>>>");

    // fields of WizardsProperty
    assertEquals(1, wizardsProperty.fields().stream().count(), "field count of 'WizardsProperty'");
    IField serialVersionUID13 = assertFieldExist(wizardsProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID13, 26);
    assertFieldType(serialVersionUID13, "long");

    assertEquals(0, wizardsProperty.methods().stream().count(), "method count of 'WizardsProperty'");

    assertEquals(0, wizardsProperty.innerTypes().stream().count(), "inner types count of 'WizardsProperty'");
  }

}
