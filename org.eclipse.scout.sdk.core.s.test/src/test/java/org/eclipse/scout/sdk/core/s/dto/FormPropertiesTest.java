/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertTypeExists;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.model.api.IType;
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
    var serialVersionUID = assertFieldExist(propertyTestFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(34, propertyTestFormData.methods().stream().count(), "method count of 'PropertyTestFormData'");
    var getBoolObject = assertMethodExist(propertyTestFormData, "getBoolObject");
    assertMethodReturnType(getBoolObject, Boolean.class.getName());
    var setBoolObject = assertMethodExist(propertyTestFormData, "setBoolObject", new String[]{Boolean.class.getName()});
    assertMethodReturnType(setBoolObject, "void");
    var getBoolObjectProperty = assertMethodExist(propertyTestFormData, "getBoolObjectProperty");
    assertMethodReturnType(getBoolObjectProperty, "formdata.shared.services.process.PropertyTestFormData$BoolObjectProperty");
    var isBoolPrimitive = assertMethodExist(propertyTestFormData, "isBoolPrimitive");
    assertMethodReturnType(isBoolPrimitive, "boolean");
    var setBoolPrimitive = assertMethodExist(propertyTestFormData, "setBoolPrimitive", new String[]{"boolean"});
    assertMethodReturnType(setBoolPrimitive, "void");
    var getBoolPrimitiveProperty = assertMethodExist(propertyTestFormData, "getBoolPrimitiveProperty");
    assertMethodReturnType(getBoolPrimitiveProperty, "formdata.shared.services.process.PropertyTestFormData$BoolPrimitiveProperty");
    var getByteArray = assertMethodExist(propertyTestFormData, "getByteArray");
    assertMethodReturnType(getByteArray, "byte[]");
    var setByteArray = assertMethodExist(propertyTestFormData, "setByteArray", new String[]{"byte[]"});
    assertMethodReturnType(setByteArray, "void");
    var getByteArrayProperty = assertMethodExist(propertyTestFormData, "getByteArrayProperty");
    assertMethodReturnType(getByteArrayProperty, "formdata.shared.services.process.PropertyTestFormData$ByteArrayProperty");
    var getComplexArray = assertMethodExist(propertyTestFormData, "getComplexArray");
    assertMethodReturnType(getComplexArray, "java.util.ArrayList<java.util.List<java.lang.String>>[]");
    var setComplexArray = assertMethodExist(propertyTestFormData, "setComplexArray", new String[]{"java.util.ArrayList<java.util.List<java.lang.String>>[]"});
    assertMethodReturnType(setComplexArray, "void");
    var getComplexArrayProperty = assertMethodExist(propertyTestFormData, "getComplexArrayProperty");
    assertMethodReturnType(getComplexArrayProperty, "formdata.shared.services.process.PropertyTestFormData$ComplexArrayProperty");
    var getComplexInnerArray = assertMethodExist(propertyTestFormData, "getComplexInnerArray");
    assertMethodReturnType(getComplexInnerArray, "java.util.ArrayList<java.util.List<java.lang.String[]>>");
    var setComplexInnerArray = assertMethodExist(propertyTestFormData, "setComplexInnerArray", new String[]{"java.util.ArrayList<java.util.List<java.lang.String[]>>"});
    assertMethodReturnType(setComplexInnerArray, "void");
    var getComplexInnerArrayProperty = assertMethodExist(propertyTestFormData, "getComplexInnerArrayProperty");
    assertMethodReturnType(getComplexInnerArrayProperty, "formdata.shared.services.process.PropertyTestFormData$ComplexInnerArrayProperty");
    var getDoubleArrayProperty = assertMethodExist(propertyTestFormData, "getDoubleArrayProperty");
    assertMethodReturnType(getDoubleArrayProperty, "java.lang.String[][]");
    var setDoubleArrayProperty = assertMethodExist(propertyTestFormData, "setDoubleArrayProperty", new String[]{"java.lang.String[][]"});
    assertMethodReturnType(setDoubleArrayProperty, "void");
    var getDoubleArrayPropertyProperty = assertMethodExist(propertyTestFormData, "getDoubleArrayPropertyProperty");
    assertMethodReturnType(getDoubleArrayPropertyProperty, "formdata.shared.services.process.PropertyTestFormData$DoubleArrayPropertyProperty");
    var getIntPrimitive = assertMethodExist(propertyTestFormData, "getIntPrimitive");
    assertMethodReturnType(getIntPrimitive, "int");
    var setIntPrimitive = assertMethodExist(propertyTestFormData, "setIntPrimitive", new String[]{"int"});
    assertMethodReturnType(setIntPrimitive, "void");
    var getIntPrimitiveProperty = assertMethodExist(propertyTestFormData, "getIntPrimitiveProperty");
    assertMethodReturnType(getIntPrimitiveProperty, "formdata.shared.services.process.PropertyTestFormData$IntPrimitiveProperty");
    var getName = assertMethodExist(propertyTestFormData, "getName");
    assertMethodReturnType(getName, "formdata.shared.services.process.PropertyTestFormData$Name");
    var getObjectProperty = assertMethodExist(propertyTestFormData, "getObjectProperty");
    assertMethodReturnType(getObjectProperty, "java.lang.Object");
    var setObjectProperty = assertMethodExist(propertyTestFormData, "setObjectProperty", new String[]{"java.lang.Object"});
    assertMethodReturnType(setObjectProperty, "void");
    var getObjectPropertyProperty = assertMethodExist(propertyTestFormData, "getObjectPropertyProperty");
    assertMethodReturnType(getObjectPropertyProperty, "formdata.shared.services.process.PropertyTestFormData$ObjectPropertyProperty");
    var getPropertyTestNr = assertMethodExist(propertyTestFormData, "getPropertyTestNr");
    assertMethodReturnType(getPropertyTestNr, Long.class.getName());
    var setPropertyTestNr = assertMethodExist(propertyTestFormData, "setPropertyTestNr", new String[]{Long.class.getName()});
    assertMethodReturnType(setPropertyTestNr, "void");
    var getPropertyTestNrProperty = assertMethodExist(propertyTestFormData, "getPropertyTestNrProperty");
    assertMethodReturnType(getPropertyTestNrProperty, "formdata.shared.services.process.PropertyTestFormData$PropertyTestNrProperty");
    var getSingleArrayProperty = assertMethodExist(propertyTestFormData, "getSingleArrayProperty");
    assertMethodReturnType(getSingleArrayProperty, "java.lang.String[]");
    var setSingleArrayProperty = assertMethodExist(propertyTestFormData, "setSingleArrayProperty", new String[]{"java.lang.String[]"});
    assertMethodReturnType(setSingleArrayProperty, "void");
    var getSingleArrayPropertyProperty = assertMethodExist(propertyTestFormData, "getSingleArrayPropertyProperty");
    assertMethodReturnType(getSingleArrayPropertyProperty, "formdata.shared.services.process.PropertyTestFormData$SingleArrayPropertyProperty");
    var getWizards = assertMethodExist(propertyTestFormData, "getWizards");
    assertMethodReturnType(getWizards, "java.util.HashMap<java.lang.String,java.util.List<org.eclipse.scout.rt.platform.service.IService>>");
    var setWizards = assertMethodExist(propertyTestFormData, "setWizards", new String[]{"java.util.HashMap<java.lang.String,java.util.List<org.eclipse.scout.rt.platform.service.IService>>"});
    assertMethodReturnType(setWizards, "void");
    var getWizardsProperty = assertMethodExist(propertyTestFormData, "getWizardsProperty");
    assertMethodReturnType(getWizardsProperty, "formdata.shared.services.process.PropertyTestFormData$WizardsProperty");

    assertEquals(12, propertyTestFormData.innerTypes().stream().count(), "inner types count of 'PropertyTestFormData'");
    // type BoolObjectProperty
    var boolObjectProperty = assertTypeExists(propertyTestFormData, "BoolObjectProperty");
    assertHasFlags(boolObjectProperty, 9);
    assertHasSuperClass(boolObjectProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Boolean>");

    // fields of BoolObjectProperty
    assertEquals(1, boolObjectProperty.fields().stream().count(), "field count of 'BoolObjectProperty'");
    var serialVersionUID1 = assertFieldExist(boolObjectProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, boolObjectProperty.methods().stream().count(), "method count of 'BoolObjectProperty'");

    assertEquals(0, boolObjectProperty.innerTypes().stream().count(), "inner types count of 'BoolObjectProperty'");
    // type BoolPrimitiveProperty
    var boolPrimitiveProperty = assertTypeExists(propertyTestFormData, "BoolPrimitiveProperty");
    assertHasFlags(boolPrimitiveProperty, 9);
    assertHasSuperClass(boolPrimitiveProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Boolean>");

    // fields of BoolPrimitiveProperty
    assertEquals(1, boolPrimitiveProperty.fields().stream().count(), "field count of 'BoolPrimitiveProperty'");
    var serialVersionUID2 = assertFieldExist(boolPrimitiveProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, boolPrimitiveProperty.methods().stream().count(), "method count of 'BoolPrimitiveProperty'");

    assertEquals(0, boolPrimitiveProperty.innerTypes().stream().count(), "inner types count of 'BoolPrimitiveProperty'");
    // type ByteArrayProperty
    var byteArrayProperty = assertTypeExists(propertyTestFormData, "ByteArrayProperty");
    assertHasFlags(byteArrayProperty, 9);
    assertHasSuperClass(byteArrayProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<byte[]>");

    // fields of ByteArrayProperty
    assertEquals(1, byteArrayProperty.fields().stream().count(), "field count of 'ByteArrayProperty'");
    var serialVersionUID3 = assertFieldExist(byteArrayProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(0, byteArrayProperty.methods().stream().count(), "method count of 'ByteArrayProperty'");

    assertEquals(0, byteArrayProperty.innerTypes().stream().count(), "inner types count of 'ByteArrayProperty'");
    // type ComplexArrayProperty
    var complexArrayProperty = assertTypeExists(propertyTestFormData, "ComplexArrayProperty");
    assertHasFlags(complexArrayProperty, 9);
    assertHasSuperClass(complexArrayProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.util.ArrayList<java.util.List<java.lang.String>>[]>");

    // fields of ComplexArrayProperty
    assertEquals(1, complexArrayProperty.fields().stream().count(), "field count of 'ComplexArrayProperty'");
    var serialVersionUID4 = assertFieldExist(complexArrayProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(0, complexArrayProperty.methods().stream().count(), "method count of 'ComplexArrayProperty'");

    assertEquals(0, complexArrayProperty.innerTypes().stream().count(), "inner types count of 'ComplexArrayProperty'");
    // type ComplexInnerArrayProperty
    var complexInnerArrayProperty = assertTypeExists(propertyTestFormData, "ComplexInnerArrayProperty");
    assertHasFlags(complexInnerArrayProperty, 9);
    assertHasSuperClass(complexInnerArrayProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.util.ArrayList<java.util.List<java.lang.String[]>>>");

    // fields of ComplexInnerArrayProperty
    assertEquals(1, complexInnerArrayProperty.fields().stream().count(), "field count of 'ComplexInnerArrayProperty'");
    var serialVersionUID5 = assertFieldExist(complexInnerArrayProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(0, complexInnerArrayProperty.methods().stream().count(), "method count of 'ComplexInnerArrayProperty'");

    assertEquals(0, complexInnerArrayProperty.innerTypes().stream().count(), "inner types count of 'ComplexInnerArrayProperty'");
    // type DoubleArrayPropertyProperty
    var doubleArrayPropertyProperty = assertTypeExists(propertyTestFormData, "DoubleArrayPropertyProperty");
    assertHasFlags(doubleArrayPropertyProperty, 9);
    assertHasSuperClass(doubleArrayPropertyProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.String[][]>");

    // fields of DoubleArrayPropertyProperty
    assertEquals(1, doubleArrayPropertyProperty.fields().stream().count(), "field count of 'DoubleArrayPropertyProperty'");
    var serialVersionUID6 = assertFieldExist(doubleArrayPropertyProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");

    assertEquals(0, doubleArrayPropertyProperty.methods().stream().count(), "method count of 'DoubleArrayPropertyProperty'");

    assertEquals(0, doubleArrayPropertyProperty.innerTypes().stream().count(), "inner types count of 'DoubleArrayPropertyProperty'");
    // type IntPrimitiveProperty
    var intPrimitiveProperty = assertTypeExists(propertyTestFormData, "IntPrimitiveProperty");
    assertHasFlags(intPrimitiveProperty, 9);
    assertHasSuperClass(intPrimitiveProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Integer>");

    // fields of IntPrimitiveProperty
    assertEquals(1, intPrimitiveProperty.fields().stream().count(), "field count of 'IntPrimitiveProperty'");
    var serialVersionUID7 = assertFieldExist(intPrimitiveProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID7, 26);
    assertFieldType(serialVersionUID7, "long");

    assertEquals(0, intPrimitiveProperty.methods().stream().count(), "method count of 'IntPrimitiveProperty'");

    assertEquals(0, intPrimitiveProperty.innerTypes().stream().count(), "inner types count of 'IntPrimitiveProperty'");
    // type Name
    var name = assertTypeExists(propertyTestFormData, "Name");
    assertHasFlags(name, 9);
    assertHasSuperClass(name, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of Name
    assertEquals(1, name.fields().stream().count(), "field count of 'Name'");
    var serialVersionUID8 = assertFieldExist(name, "serialVersionUID");
    assertHasFlags(serialVersionUID8, 26);
    assertFieldType(serialVersionUID8, "long");

    assertEquals(3, name.methods().stream().count(), "method count of 'Name'");
    var getIntProperty = assertMethodExist(name, "getIntProperty");
    assertMethodReturnType(getIntProperty, "int");
    var setIntProperty = assertMethodExist(name, "setIntProperty", new String[]{"int"});
    assertMethodReturnType(setIntProperty, "void");
    var getIntPropertyProperty = assertMethodExist(name, "getIntPropertyProperty");
    assertMethodReturnType(getIntPropertyProperty, "formdata.shared.services.process.PropertyTestFormData$Name$IntPropertyProperty");

    assertEquals(1, name.innerTypes().stream().count(), "inner types count of 'Name'");
    // type IntPropertyProperty
    var intPropertyProperty = assertTypeExists(name, "IntPropertyProperty");
    assertHasFlags(intPropertyProperty, 9);
    assertHasSuperClass(intPropertyProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Integer>");

    // fields of IntPropertyProperty
    assertEquals(1, intPropertyProperty.fields().stream().count(), "field count of 'IntPropertyProperty'");
    var serialVersionUID9 = assertFieldExist(intPropertyProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID9, 26);
    assertFieldType(serialVersionUID9, "long");

    assertEquals(0, intPropertyProperty.methods().stream().count(), "method count of 'IntPropertyProperty'");

    assertEquals(0, intPropertyProperty.innerTypes().stream().count(), "inner types count of 'IntPropertyProperty'");
    // type ObjectPropertyProperty
    var objectPropertyProperty = assertTypeExists(propertyTestFormData, "ObjectPropertyProperty");
    assertHasFlags(objectPropertyProperty, 9);
    assertHasSuperClass(objectPropertyProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Object>");

    // fields of ObjectPropertyProperty
    assertEquals(1, objectPropertyProperty.fields().stream().count(), "field count of 'ObjectPropertyProperty'");
    var serialVersionUID10 = assertFieldExist(objectPropertyProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID10, 26);
    assertFieldType(serialVersionUID10, "long");

    assertEquals(0, objectPropertyProperty.methods().stream().count(), "method count of 'ObjectPropertyProperty'");

    assertEquals(0, objectPropertyProperty.innerTypes().stream().count(), "inner types count of 'ObjectPropertyProperty'");
    // type PropertyTestNrProperty
    var propertyTestNrProperty = assertTypeExists(propertyTestFormData, "PropertyTestNrProperty");
    assertHasFlags(propertyTestNrProperty, 9);
    assertHasSuperClass(propertyTestNrProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Long>");

    // fields of PropertyTestNrProperty
    assertEquals(1, propertyTestNrProperty.fields().stream().count(), "field count of 'PropertyTestNrProperty'");
    var serialVersionUID11 = assertFieldExist(propertyTestNrProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID11, 26);
    assertFieldType(serialVersionUID11, "long");

    assertEquals(0, propertyTestNrProperty.methods().stream().count(), "method count of 'PropertyTestNrProperty'");

    assertEquals(0, propertyTestNrProperty.innerTypes().stream().count(), "inner types count of 'PropertyTestNrProperty'");
    // type SingleArrayPropertyProperty
    var singleArrayPropertyProperty = assertTypeExists(propertyTestFormData, "SingleArrayPropertyProperty");
    assertHasFlags(singleArrayPropertyProperty, 9);
    assertHasSuperClass(singleArrayPropertyProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.String[]>");

    // fields of SingleArrayPropertyProperty
    assertEquals(1, singleArrayPropertyProperty.fields().stream().count(), "field count of 'SingleArrayPropertyProperty'");
    var serialVersionUID12 = assertFieldExist(singleArrayPropertyProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID12, 26);
    assertFieldType(serialVersionUID12, "long");

    assertEquals(0, singleArrayPropertyProperty.methods().stream().count(), "method count of 'SingleArrayPropertyProperty'");

    assertEquals(0, singleArrayPropertyProperty.innerTypes().stream().count(), "inner types count of 'SingleArrayPropertyProperty'");
    // type WizardsProperty
    var wizardsProperty = assertTypeExists(propertyTestFormData, "WizardsProperty");
    assertHasFlags(wizardsProperty, 9);
    assertHasSuperClass(wizardsProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.util.HashMap<java.lang.String,java.util.List<org.eclipse.scout.rt.platform.service.IService>>>");

    // fields of WizardsProperty
    assertEquals(1, wizardsProperty.fields().stream().count(), "field count of 'WizardsProperty'");
    var serialVersionUID13 = assertFieldExist(wizardsProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID13, 26);
    assertFieldType(serialVersionUID13, "long");

    assertEquals(0, wizardsProperty.methods().stream().count(), "method count of 'WizardsProperty'");

    assertEquals(0, wizardsProperty.innerTypes().stream().count(), "inner types count of 'WizardsProperty'");
  }

}
