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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Assert;
import org.junit.Test;

public class SimpleFormTest extends AbstractSdkTestWithFormDataProject {

  @Test
  public void testCreateFormData() throws Exception {
    IType form = SdkAssert.assertTypeExists("formdata.client.ui.forms.SimpleForm");

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);
    FormDataAnnotation annotation = ScoutTypeUtility.findFormDataAnnotation(form, TypeUtility.getSuperTypeHierarchy(form));
    ICompilationUnit formDataIcu = SdkAssert.assertTypeExistsBySignature(annotation.getFormDataTypeSignature()).getCompilationUnit();
    FormDataUpdateOperation op = new FormDataUpdateOperation(form, formDataIcu);

    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, op);

    testApiOfSimpleFormData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfSimpleFormData() throws Exception {
    // type SimpleFormData
    IType simpleFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.SimpleFormData");
    SdkAssert.assertHasFlags(simpleFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(simpleFormData, "QAbstractFormData;");

    // fields of SimpleFormData
    SdkAssert.assertEquals("field count of 'SimpleFormData'", 1, simpleFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(simpleFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'SimpleFormData'", 8, simpleFormData.getMethods().length);
    IMethod simpleFormData1 = SdkAssert.assertMethodExist(simpleFormData, "SimpleFormData", new String[]{});
    SdkAssert.assertTrue(simpleFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(simpleFormData1, "V");
    IMethod getSampleComposer = SdkAssert.assertMethodExist(simpleFormData, "getSampleComposer", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSampleComposer, "QSampleComposer;");
    IMethod getSampleDouble = SdkAssert.assertMethodExist(simpleFormData, "getSampleDouble", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSampleDouble, "QSampleDouble;");
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

    SdkAssert.assertEquals("inner types count of 'SimpleFormData'", 5, simpleFormData.getTypes().length);
    // type SampleComposer
    IType sampleComposer = SdkAssert.assertTypeExists(simpleFormData, "SampleComposer");
    SdkAssert.assertHasFlags(sampleComposer, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleComposer, "QAbstractComposerData;");

    // fields of SampleComposer
    SdkAssert.assertEquals("field count of 'SampleComposer'", 1, sampleComposer.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(sampleComposer, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'SampleComposer'", 1, sampleComposer.getMethods().length);
    IMethod sampleComposer1 = SdkAssert.assertMethodExist(sampleComposer, "SampleComposer", new String[]{});
    SdkAssert.assertTrue(sampleComposer1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleComposer1, "V");

    SdkAssert.assertEquals("inner types count of 'SampleComposer'", 0, sampleComposer.getTypes().length);
    // type SampleDouble
    IType sampleDouble = SdkAssert.assertTypeExists(simpleFormData, "SampleDouble");
    SdkAssert.assertHasFlags(sampleDouble, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleDouble, "QAbstractValueFieldData<QDouble;>;");

    // fields of SampleDouble
    SdkAssert.assertEquals("field count of 'SampleDouble'", 1, sampleDouble.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(sampleDouble, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'SampleDouble'", 1, sampleDouble.getMethods().length);
    IMethod sampleDouble1 = SdkAssert.assertMethodExist(sampleDouble, "SampleDouble", new String[]{});
    SdkAssert.assertTrue(sampleDouble1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleDouble1, "V");

    SdkAssert.assertEquals("inner types count of 'SampleDouble'", 0, sampleDouble.getTypes().length);
    // type SampleSmart
    IType sampleSmart = SdkAssert.assertTypeExists(simpleFormData, "SampleSmart");
    SdkAssert.assertHasFlags(sampleSmart, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleSmart, "QAbstractValueFieldData<QLong;>;");

    // fields of SampleSmart
    SdkAssert.assertEquals("field count of 'SampleSmart'", 1, sampleSmart.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(sampleSmart, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'SampleSmart'", 2, sampleSmart.getMethods().length);
    IMethod sampleSmart1 = SdkAssert.assertMethodExist(sampleSmart, "SampleSmart", new String[]{});
    SdkAssert.assertTrue(sampleSmart1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleSmart1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(sampleSmart, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);"}, true);

    SdkAssert.assertEquals("inner types count of 'SampleSmart'", 0, sampleSmart.getTypes().length);
    // type SampleString
    IType sampleString = SdkAssert.assertTypeExists(simpleFormData, "SampleString");
    SdkAssert.assertHasFlags(sampleString, 9);
    SdkAssert.assertHasSuperTypeSignature(sampleString, "QAbstractValueFieldData<QString;>;");

    // fields of SampleString
    SdkAssert.assertEquals("field count of 'SampleString'", 1, sampleString.getFields().length);
    IField serialVersionUID4 = SdkAssert.assertFieldExist(sampleString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    SdkAssert.assertEquals("method count of 'SampleString'", 2, sampleString.getMethods().length);
    IMethod sampleString1 = SdkAssert.assertMethodExist(sampleString, "SampleString", new String[]{});
    SdkAssert.assertTrue(sampleString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sampleString1, "V");
    IMethod initValidationRules1 = SdkAssert.assertMethodExist(sampleString, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules1, "V");
    SdkAssert.assertAnnotation(initValidationRules1, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules1, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'SampleString'", 0, sampleString.getTypes().length);
    // type SimpleNrProperty
    IType simpleNrProperty = SdkAssert.assertTypeExists(simpleFormData, "SimpleNrProperty");
    SdkAssert.assertHasFlags(simpleNrProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(simpleNrProperty, "QAbstractPropertyData<QLong;>;");

    // fields of SimpleNrProperty
    SdkAssert.assertEquals("field count of 'SimpleNrProperty'", 1, simpleNrProperty.getFields().length);
    IField serialVersionUID5 = SdkAssert.assertFieldExist(simpleNrProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    SdkAssert.assertEquals("method count of 'SimpleNrProperty'", 1, simpleNrProperty.getMethods().length);
    IMethod simpleNrProperty1 = SdkAssert.assertMethodExist(simpleNrProperty, "SimpleNrProperty", new String[]{});
    SdkAssert.assertTrue(simpleNrProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(simpleNrProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'SimpleNrProperty'", 0, simpleNrProperty.getTypes().length);
  }

}
