/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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

public class ReplaceFormFieldTest extends AbstractSdkTestWithFormDataProject {

  private static final String BaseFormFqn = "formdata.client.ui.forms.replace.BaseForm";
  private static final String ExtendedFormFqn = "formdata.client.ui.forms.replace.ExtendedForm";
  private static final String ExtendedExtendedFormFqn = "formdata.client.ui.forms.replace.ExtendedExtendedForm";

  @Test
  public void runTests() throws Exception {
    checkBaseFormData();
    checkExtendedFormData();
    checkExtendedExtendedFormData();
  }

  private void checkBaseFormData() throws Exception {
    IType form = SdkAssert.assertTypeExists(BaseFormFqn);

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(form);
    executeBuildAssertNoCompileErrors(op);

    testApiOfBaseFormData();
  }

  private void checkExtendedFormData() throws Exception {
    IType form = SdkAssert.assertTypeExists(ExtendedFormFqn);

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(form);

    executeBuildAssertNoCompileErrors(op);

    testApiOfExtendedFormData();
  }

  private void checkExtendedExtendedFormData() throws Exception {
    IType form = SdkAssert.assertTypeExists(ExtendedExtendedFormFqn);

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(form);

    executeBuildAssertNoCompileErrors(op);

    testApiOfExtendedExtendedFormData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfBaseFormData() throws Exception {
    // type BaseFormData
    IType baseFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.replace.BaseFormData");
    SdkAssert.assertHasFlags(baseFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(baseFormData, "QAbstractFormData;");

    // fields of BaseFormData
    SdkAssert.assertEquals("field count of 'BaseFormData'", 1, baseFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(baseFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'BaseFormData'", 7, baseFormData.getMethods().length);
    IMethod baseFormData1 = SdkAssert.assertMethodExist(baseFormData, "BaseFormData", new String[]{});
    SdkAssert.assertTrue(baseFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(baseFormData1, "V");
    IMethod getLookup = SdkAssert.assertMethodExist(baseFormData, "getLookup", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getLookup, "QLookup;");
    IMethod getName = SdkAssert.assertMethodExist(baseFormData, "getName", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getName, "QName;");
    IMethod getSdkCommandCreate = SdkAssert.assertMethodExist(baseFormData, "getSdkCommandCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreate, "QSdkCommandCreate;");
    IMethod getSdkCommandNone = SdkAssert.assertMethodExist(baseFormData, "getSdkCommandNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNone, "QSdkCommandNone;");
    IMethod getSdkCommandUse = SdkAssert.assertMethodExist(baseFormData, "getSdkCommandUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUse, "QSdkCommandUse;");
    IMethod getSmart = SdkAssert.assertMethodExist(baseFormData, "getSmart", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSmart, "QSmart;");

    SdkAssert.assertEquals("inner types count of 'BaseFormData'", 6, baseFormData.getTypes().length);
    // type Lookup
    IType lookup = SdkAssert.assertTypeExists(baseFormData, "Lookup");
    SdkAssert.assertHasFlags(lookup, 9);
    SdkAssert.assertHasSuperTypeSignature(lookup, "QAbstractValueFieldData<QLong;>;");

    // fields of Lookup
    SdkAssert.assertEquals("field count of 'Lookup'", 1, lookup.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(lookup, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'Lookup'", 2, lookup.getMethods().length);
    IMethod lookup1 = SdkAssert.assertMethodExist(lookup, "Lookup", new String[]{});
    SdkAssert.assertTrue(lookup1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(lookup1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(lookup, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.LOOKUP_CALL, TestingLookupCall.class);", "ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);"}, true);

    SdkAssert.assertEquals("inner types count of 'Lookup'", 0, lookup.getTypes().length);
    // type Name
    IType name = SdkAssert.assertTypeExists(baseFormData, "Name");
    SdkAssert.assertHasFlags(name, 9);
    SdkAssert.assertHasSuperTypeSignature(name, "QAbstractValueFieldData<QString;>;");

    // fields of Name
    SdkAssert.assertEquals("field count of 'Name'", 1, name.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(name, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'Name'", 2, name.getMethods().length);
    IMethod name1 = SdkAssert.assertMethodExist(name, "Name", new String[]{});
    SdkAssert.assertTrue(name1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(name1, "V");
    IMethod initValidationRules1 = SdkAssert.assertMethodExist(name, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules1, "V");
    SdkAssert.assertAnnotation(initValidationRules1, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules1, new String[]{"ruleMap.put(ValidationRule.MANDATORY, true);", "ruleMap.put(ValidationRule.MAX_LENGTH, 60);"}, true);

    SdkAssert.assertEquals("inner types count of 'Name'", 0, name.getTypes().length);
    // type SdkCommandCreate
    IType sdkCommandCreate = SdkAssert.assertTypeExists(baseFormData, "SdkCommandCreate");
    SdkAssert.assertHasFlags(sdkCommandCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreate, "QAbstractValueFieldData<QString;>;");

    // fields of SdkCommandCreate
    SdkAssert.assertEquals("field count of 'SdkCommandCreate'", 1, sdkCommandCreate.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(sdkCommandCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreate'", 2, sdkCommandCreate.getMethods().length);
    IMethod sdkCommandCreate1 = SdkAssert.assertMethodExist(sdkCommandCreate, "SdkCommandCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreate1, "V");
    IMethod initValidationRules2 = SdkAssert.assertMethodExist(sdkCommandCreate, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules2, "V");
    SdkAssert.assertAnnotation(initValidationRules2, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules2, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreate'", 0, sdkCommandCreate.getTypes().length);
    // type SdkCommandNone
    IType sdkCommandNone = SdkAssert.assertTypeExists(baseFormData, "SdkCommandNone");
    SdkAssert.assertHasFlags(sdkCommandNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNone, "QAbstractValueFieldData<QString;>;");

    // fields of SdkCommandNone
    SdkAssert.assertEquals("field count of 'SdkCommandNone'", 1, sdkCommandNone.getFields().length);
    IField serialVersionUID4 = SdkAssert.assertFieldExist(sdkCommandNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNone'", 2, sdkCommandNone.getMethods().length);
    IMethod sdkCommandNone1 = SdkAssert.assertMethodExist(sdkCommandNone, "SdkCommandNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNone1, "V");
    IMethod initValidationRules3 = SdkAssert.assertMethodExist(sdkCommandNone, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules3, "V");
    SdkAssert.assertAnnotation(initValidationRules3, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules3, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'SdkCommandNone'", 0, sdkCommandNone.getTypes().length);
    // type SdkCommandUse
    IType sdkCommandUse = SdkAssert.assertTypeExists(baseFormData, "SdkCommandUse");
    SdkAssert.assertHasFlags(sdkCommandUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUse, "QUsingFormFieldData;");

    // fields of SdkCommandUse
    SdkAssert.assertEquals("field count of 'SdkCommandUse'", 1, sdkCommandUse.getFields().length);
    IField serialVersionUID5 = SdkAssert.assertFieldExist(sdkCommandUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUse'", 2, sdkCommandUse.getMethods().length);
    IMethod sdkCommandUse1 = SdkAssert.assertMethodExist(sdkCommandUse, "SdkCommandUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUse1, "V");
    IMethod initValidationRules4 = SdkAssert.assertMethodExist(sdkCommandUse, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules4, "V");
    SdkAssert.assertAnnotation(initValidationRules4, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules4, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'SdkCommandUse'", 0, sdkCommandUse.getTypes().length);
    // type Smart
    IType smart = SdkAssert.assertTypeExists(baseFormData, "Smart");
    SdkAssert.assertHasFlags(smart, 9);
    SdkAssert.assertHasSuperTypeSignature(smart, "QAbstractValueFieldData<QLong;>;");

    // fields of Smart
    SdkAssert.assertEquals("field count of 'Smart'", 1, smart.getFields().length);
    IField serialVersionUID6 = SdkAssert.assertFieldExist(smart, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    SdkAssert.assertEquals("method count of 'Smart'", 2, smart.getMethods().length);
    IMethod smart1 = SdkAssert.assertMethodExist(smart, "Smart", new String[]{});
    SdkAssert.assertTrue(smart1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(smart1, "V");
    IMethod initValidationRules5 = SdkAssert.assertMethodExist(smart, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules5, "V");
    SdkAssert.assertAnnotation(initValidationRules5, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules5, new String[]{"ruleMap.put(ValidationRule.CODE_TYPE, TestingCodeType.class);", "ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);"}, true);

    SdkAssert.assertEquals("inner types count of 'Smart'", 0, smart.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfExtendedFormData() throws Exception {
    // type ExtendedFormData
    IType extendedFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.replace.ExtendedFormData");
    SdkAssert.assertHasFlags(extendedFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(extendedFormData, "QBaseFormData;");

    // fields of ExtendedFormData
    SdkAssert.assertEquals("field count of 'ExtendedFormData'", 1, extendedFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(extendedFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'ExtendedFormData'", 20, extendedFormData.getMethods().length);
    IMethod extendedFormData1 = SdkAssert.assertMethodExist(extendedFormData, "ExtendedFormData", new String[]{});
    SdkAssert.assertTrue(extendedFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedFormData1, "V");
    IMethod getFirstName = SdkAssert.assertMethodExist(extendedFormData, "getFirstName", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirstName, "QFirstName;");
    IMethod getIgnoringGroupBoxExCreate = SdkAssert.assertMethodExist(extendedFormData, "getIgnoringGroupBoxExCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIgnoringGroupBoxExCreate, "QIgnoringGroupBoxExCreate;");
    IMethod getIgnoringGroupBoxExUse = SdkAssert.assertMethodExist(extendedFormData, "getIgnoringGroupBoxExUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIgnoringGroupBoxExUse, "QIgnoringGroupBoxExUse;");
    IMethod getNameEx = SdkAssert.assertMethodExist(extendedFormData, "getNameEx", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getNameEx, "QNameEx;");
    IMethod getSdkCommandCreateCreate = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandCreateCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateCreate, "QSdkCommandCreateCreate;");
    IMethod getSdkCommandCreateIgnore = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandCreateIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateIgnore, "QSdkCommandCreateIgnore;");
    IMethod getSdkCommandCreateNone = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateNone, "QSdkCommandCreateNone;");
    IMethod getSdkCommandCreateUse = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandCreateUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateUse, "QSdkCommandCreateUse;");
    IMethod getSdkCommandIgnoreCreate = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandIgnoreCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreCreate, "QSdkCommandIgnoreCreate;");
    IMethod getSdkCommandIgnoreUse = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandIgnoreUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreUse, "QSdkCommandIgnoreUse;");
    IMethod getSdkCommandNoneCreate = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneCreate, "QSdkCommandNoneCreate;");
    IMethod getSdkCommandNoneIgnore = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandNoneIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneIgnore, "QSdkCommandNoneIgnore;");
    IMethod getSdkCommandNoneNone = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandNoneNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneNone, "QSdkCommandNoneNone;");
    IMethod getSdkCommandNoneUse = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandNoneUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneUse, "QSdkCommandNoneUse;");
    IMethod getSdkCommandUseCreate = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandUseCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseCreate, "QSdkCommandUseCreate;");
    IMethod getSdkCommandUseIgnore = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandUseIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseIgnore, "QSdkCommandUseIgnore;");
    IMethod getSdkCommandUseNone = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandUseNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseNone, "QSdkCommandUseNone;");
    IMethod getSdkCommandUseUse = SdkAssert.assertMethodExist(extendedFormData, "getSdkCommandUseUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseUse, "QSdkCommandUseUse;");
    IMethod getSmartEx = SdkAssert.assertMethodExist(extendedFormData, "getSmartEx", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSmartEx, "QSmartEx;");

    SdkAssert.assertEquals("inner types count of 'ExtendedFormData'", 19, extendedFormData.getTypes().length);
    // type FirstName
    IType firstName = SdkAssert.assertTypeExists(extendedFormData, "FirstName");
    SdkAssert.assertHasFlags(firstName, 9);
    SdkAssert.assertHasSuperTypeSignature(firstName, "QAbstractValueFieldData<QString;>;");

    // fields of FirstName
    SdkAssert.assertEquals("field count of 'FirstName'", 1, firstName.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(firstName, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'FirstName'", 2, firstName.getMethods().length);
    IMethod firstName1 = SdkAssert.assertMethodExist(firstName, "FirstName", new String[]{});
    SdkAssert.assertTrue(firstName1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(firstName1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(firstName, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'FirstName'", 0, firstName.getTypes().length);
    // type IgnoringGroupBoxExCreate
    IType ignoringGroupBoxExCreate = SdkAssert.assertTypeExists(extendedFormData, "IgnoringGroupBoxExCreate");
    SdkAssert.assertHasFlags(ignoringGroupBoxExCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(ignoringGroupBoxExCreate, "QAbstractValueFieldData<QString;>;");
    SdkAssert.assertAnnotation(ignoringGroupBoxExCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of IgnoringGroupBoxExCreate
    SdkAssert.assertEquals("field count of 'IgnoringGroupBoxExCreate'", 1, ignoringGroupBoxExCreate.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(ignoringGroupBoxExCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'IgnoringGroupBoxExCreate'", 2, ignoringGroupBoxExCreate.getMethods().length);
    IMethod ignoringGroupBoxExCreate1 = SdkAssert.assertMethodExist(ignoringGroupBoxExCreate, "IgnoringGroupBoxExCreate", new String[]{});
    SdkAssert.assertTrue(ignoringGroupBoxExCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(ignoringGroupBoxExCreate1, "V");
    IMethod initValidationRules1 = SdkAssert.assertMethodExist(ignoringGroupBoxExCreate, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules1, "V");
    SdkAssert.assertAnnotation(initValidationRules1, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules1, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'IgnoringGroupBoxExCreate'", 0, ignoringGroupBoxExCreate.getTypes().length);
    // type IgnoringGroupBoxExUse
    IType ignoringGroupBoxExUse = SdkAssert.assertTypeExists(extendedFormData, "IgnoringGroupBoxExUse");
    SdkAssert.assertHasFlags(ignoringGroupBoxExUse, 9);
    SdkAssert.assertHasSuperTypeSignature(ignoringGroupBoxExUse, "QUsingFormFieldData;");
    SdkAssert.assertAnnotation(ignoringGroupBoxExUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of IgnoringGroupBoxExUse
    SdkAssert.assertEquals("field count of 'IgnoringGroupBoxExUse'", 1, ignoringGroupBoxExUse.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(ignoringGroupBoxExUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'IgnoringGroupBoxExUse'", 2, ignoringGroupBoxExUse.getMethods().length);
    IMethod ignoringGroupBoxExUse1 = SdkAssert.assertMethodExist(ignoringGroupBoxExUse, "IgnoringGroupBoxExUse", new String[]{});
    SdkAssert.assertTrue(ignoringGroupBoxExUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(ignoringGroupBoxExUse1, "V");
    IMethod initValidationRules2 = SdkAssert.assertMethodExist(ignoringGroupBoxExUse, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules2, "V");
    SdkAssert.assertAnnotation(initValidationRules2, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules2, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'IgnoringGroupBoxExUse'", 0, ignoringGroupBoxExUse.getTypes().length);
    // type NameEx
    IType nameEx = SdkAssert.assertTypeExists(extendedFormData, "NameEx");
    SdkAssert.assertHasFlags(nameEx, 9);
    SdkAssert.assertHasSuperTypeSignature(nameEx, "QName;");
    SdkAssert.assertAnnotation(nameEx, "org.eclipse.scout.commons.annotations.Replace");

    // fields of NameEx
    SdkAssert.assertEquals("field count of 'NameEx'", 1, nameEx.getFields().length);
    IField serialVersionUID4 = SdkAssert.assertFieldExist(nameEx, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    SdkAssert.assertEquals("method count of 'NameEx'", 2, nameEx.getMethods().length);
    IMethod nameEx1 = SdkAssert.assertMethodExist(nameEx, "NameEx", new String[]{});
    SdkAssert.assertTrue(nameEx1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(nameEx1, "V");
    IMethod initValidationRules3 = SdkAssert.assertMethodExist(nameEx, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules3, "V");
    SdkAssert.assertAnnotation(initValidationRules3, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules3, new String[]{"ruleMap.remove(ValidationRule.MANDATORY);", "ruleMap.put(ValidationRule.MAX_LENGTH, 100);"}, true);

    SdkAssert.assertEquals("inner types count of 'NameEx'", 0, nameEx.getTypes().length);
    // type SdkCommandCreateCreate
    IType sdkCommandCreateCreate = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandCreateCreate");
    SdkAssert.assertHasFlags(sdkCommandCreateCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateCreate, "QSdkCommandCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateCreate
    SdkAssert.assertEquals("field count of 'SdkCommandCreateCreate'", 1, sdkCommandCreateCreate.getFields().length);
    IField serialVersionUID5 = SdkAssert.assertFieldExist(sdkCommandCreateCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateCreate'", 1, sdkCommandCreateCreate.getMethods().length);
    IMethod sdkCommandCreateCreate1 = SdkAssert.assertMethodExist(sdkCommandCreateCreate, "SdkCommandCreateCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateCreate'", 0, sdkCommandCreateCreate.getTypes().length);
    // type SdkCommandCreateIgnore
    IType sdkCommandCreateIgnore = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandCreateIgnore");
    SdkAssert.assertHasFlags(sdkCommandCreateIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateIgnore, "QSdkCommandCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandCreateIgnore'", 1, sdkCommandCreateIgnore.getFields().length);
    IField serialVersionUID6 = SdkAssert.assertFieldExist(sdkCommandCreateIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateIgnore'", 1, sdkCommandCreateIgnore.getMethods().length);
    IMethod sdkCommandCreateIgnore1 = SdkAssert.assertMethodExist(sdkCommandCreateIgnore, "SdkCommandCreateIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateIgnore'", 0, sdkCommandCreateIgnore.getTypes().length);
    // type SdkCommandCreateNone
    IType sdkCommandCreateNone = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandCreateNone");
    SdkAssert.assertHasFlags(sdkCommandCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateNone, "QSdkCommandCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateNone
    SdkAssert.assertEquals("field count of 'SdkCommandCreateNone'", 1, sdkCommandCreateNone.getFields().length);
    IField serialVersionUID7 = SdkAssert.assertFieldExist(sdkCommandCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateNone'", 1, sdkCommandCreateNone.getMethods().length);
    IMethod sdkCommandCreateNone1 = SdkAssert.assertMethodExist(sdkCommandCreateNone, "SdkCommandCreateNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateNone'", 0, sdkCommandCreateNone.getTypes().length);
    // type SdkCommandCreateUse
    IType sdkCommandCreateUse = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandCreateUse");
    SdkAssert.assertHasFlags(sdkCommandCreateUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateUse, "QSdkCommandCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateUse
    SdkAssert.assertEquals("field count of 'SdkCommandCreateUse'", 1, sdkCommandCreateUse.getFields().length);
    IField serialVersionUID8 = SdkAssert.assertFieldExist(sdkCommandCreateUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateUse'", 1, sdkCommandCreateUse.getMethods().length);
    IMethod sdkCommandCreateUse1 = SdkAssert.assertMethodExist(sdkCommandCreateUse, "SdkCommandCreateUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateUse'", 0, sdkCommandCreateUse.getTypes().length);
    // type SdkCommandIgnoreCreate
    IType sdkCommandIgnoreCreate = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandIgnoreCreate");
    SdkAssert.assertHasFlags(sdkCommandIgnoreCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreCreate, "QAbstractValueFieldData<QString;>;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreCreate
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreCreate'", 1, sdkCommandIgnoreCreate.getFields().length);
    IField serialVersionUID9 = SdkAssert.assertFieldExist(sdkCommandIgnoreCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID9, 26);
    SdkAssert.assertFieldSignature(serialVersionUID9, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreCreate'", 2, sdkCommandIgnoreCreate.getMethods().length);
    IMethod sdkCommandIgnoreCreate1 = SdkAssert.assertMethodExist(sdkCommandIgnoreCreate, "SdkCommandIgnoreCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreCreate1, "V");
    IMethod initValidationRules4 = SdkAssert.assertMethodExist(sdkCommandIgnoreCreate, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules4, "V");
    SdkAssert.assertAnnotation(initValidationRules4, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules4, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreCreate'", 0, sdkCommandIgnoreCreate.getTypes().length);
    // type SdkCommandIgnoreUse
    IType sdkCommandIgnoreUse = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandIgnoreUse");
    SdkAssert.assertHasFlags(sdkCommandIgnoreUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreUse, "QUsingFormFieldData;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreUse
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreUse'", 1, sdkCommandIgnoreUse.getFields().length);
    IField serialVersionUID10 = SdkAssert.assertFieldExist(sdkCommandIgnoreUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID10, 26);
    SdkAssert.assertFieldSignature(serialVersionUID10, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreUse'", 2, sdkCommandIgnoreUse.getMethods().length);
    IMethod sdkCommandIgnoreUse1 = SdkAssert.assertMethodExist(sdkCommandIgnoreUse, "SdkCommandIgnoreUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreUse1, "V");
    IMethod initValidationRules5 = SdkAssert.assertMethodExist(sdkCommandIgnoreUse, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules5, "V");
    SdkAssert.assertAnnotation(initValidationRules5, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules5, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreUse'", 0, sdkCommandIgnoreUse.getTypes().length);
    // type SdkCommandNoneCreate
    IType sdkCommandNoneCreate = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandNoneCreate");
    SdkAssert.assertHasFlags(sdkCommandNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneCreate, "QSdkCommandNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneCreate
    SdkAssert.assertEquals("field count of 'SdkCommandNoneCreate'", 1, sdkCommandNoneCreate.getFields().length);
    IField serialVersionUID11 = SdkAssert.assertFieldExist(sdkCommandNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID11, 26);
    SdkAssert.assertFieldSignature(serialVersionUID11, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneCreate'", 1, sdkCommandNoneCreate.getMethods().length);
    IMethod sdkCommandNoneCreate1 = SdkAssert.assertMethodExist(sdkCommandNoneCreate, "SdkCommandNoneCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneCreate'", 0, sdkCommandNoneCreate.getTypes().length);
    // type SdkCommandNoneIgnore
    IType sdkCommandNoneIgnore = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandNoneIgnore");
    SdkAssert.assertHasFlags(sdkCommandNoneIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneIgnore, "QSdkCommandNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandNoneIgnore'", 1, sdkCommandNoneIgnore.getFields().length);
    IField serialVersionUID12 = SdkAssert.assertFieldExist(sdkCommandNoneIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID12, 26);
    SdkAssert.assertFieldSignature(serialVersionUID12, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneIgnore'", 1, sdkCommandNoneIgnore.getMethods().length);
    IMethod sdkCommandNoneIgnore1 = SdkAssert.assertMethodExist(sdkCommandNoneIgnore, "SdkCommandNoneIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneIgnore'", 0, sdkCommandNoneIgnore.getTypes().length);
    // type SdkCommandNoneNone
    IType sdkCommandNoneNone = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandNoneNone");
    SdkAssert.assertHasFlags(sdkCommandNoneNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneNone, "QSdkCommandNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneNone
    SdkAssert.assertEquals("field count of 'SdkCommandNoneNone'", 1, sdkCommandNoneNone.getFields().length);
    IField serialVersionUID13 = SdkAssert.assertFieldExist(sdkCommandNoneNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID13, 26);
    SdkAssert.assertFieldSignature(serialVersionUID13, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneNone'", 1, sdkCommandNoneNone.getMethods().length);
    IMethod sdkCommandNoneNone1 = SdkAssert.assertMethodExist(sdkCommandNoneNone, "SdkCommandNoneNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneNone'", 0, sdkCommandNoneNone.getTypes().length);
    // type SdkCommandNoneUse
    IType sdkCommandNoneUse = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandNoneUse");
    SdkAssert.assertHasFlags(sdkCommandNoneUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneUse, "QSdkCommandNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneUse
    SdkAssert.assertEquals("field count of 'SdkCommandNoneUse'", 1, sdkCommandNoneUse.getFields().length);
    IField serialVersionUID14 = SdkAssert.assertFieldExist(sdkCommandNoneUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID14, 26);
    SdkAssert.assertFieldSignature(serialVersionUID14, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneUse'", 1, sdkCommandNoneUse.getMethods().length);
    IMethod sdkCommandNoneUse1 = SdkAssert.assertMethodExist(sdkCommandNoneUse, "SdkCommandNoneUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneUse'", 0, sdkCommandNoneUse.getTypes().length);
    // type SdkCommandUseCreate
    IType sdkCommandUseCreate = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandUseCreate");
    SdkAssert.assertHasFlags(sdkCommandUseCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseCreate, "QSdkCommandUse;");
    SdkAssert.assertAnnotation(sdkCommandUseCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseCreate
    SdkAssert.assertEquals("field count of 'SdkCommandUseCreate'", 1, sdkCommandUseCreate.getFields().length);
    IField serialVersionUID15 = SdkAssert.assertFieldExist(sdkCommandUseCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID15, 26);
    SdkAssert.assertFieldSignature(serialVersionUID15, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseCreate'", 1, sdkCommandUseCreate.getMethods().length);
    IMethod sdkCommandUseCreate1 = SdkAssert.assertMethodExist(sdkCommandUseCreate, "SdkCommandUseCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseCreate'", 0, sdkCommandUseCreate.getTypes().length);
    // type SdkCommandUseIgnore
    IType sdkCommandUseIgnore = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandUseIgnore");
    SdkAssert.assertHasFlags(sdkCommandUseIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseIgnore, "QSdkCommandUse;");
    SdkAssert.assertAnnotation(sdkCommandUseIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandUseIgnore'", 1, sdkCommandUseIgnore.getFields().length);
    IField serialVersionUID16 = SdkAssert.assertFieldExist(sdkCommandUseIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID16, 26);
    SdkAssert.assertFieldSignature(serialVersionUID16, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseIgnore'", 1, sdkCommandUseIgnore.getMethods().length);
    IMethod sdkCommandUseIgnore1 = SdkAssert.assertMethodExist(sdkCommandUseIgnore, "SdkCommandUseIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseIgnore'", 0, sdkCommandUseIgnore.getTypes().length);
    // type SdkCommandUseNone
    IType sdkCommandUseNone = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandUseNone");
    SdkAssert.assertHasFlags(sdkCommandUseNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseNone, "QSdkCommandUse;");
    SdkAssert.assertAnnotation(sdkCommandUseNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseNone
    SdkAssert.assertEquals("field count of 'SdkCommandUseNone'", 1, sdkCommandUseNone.getFields().length);
    IField serialVersionUID17 = SdkAssert.assertFieldExist(sdkCommandUseNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID17, 26);
    SdkAssert.assertFieldSignature(serialVersionUID17, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseNone'", 1, sdkCommandUseNone.getMethods().length);
    IMethod sdkCommandUseNone1 = SdkAssert.assertMethodExist(sdkCommandUseNone, "SdkCommandUseNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseNone'", 0, sdkCommandUseNone.getTypes().length);
    // type SdkCommandUseUse
    IType sdkCommandUseUse = SdkAssert.assertTypeExists(extendedFormData, "SdkCommandUseUse");
    SdkAssert.assertHasFlags(sdkCommandUseUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseUse, "QSdkCommandUse;");
    SdkAssert.assertAnnotation(sdkCommandUseUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseUse
    SdkAssert.assertEquals("field count of 'SdkCommandUseUse'", 1, sdkCommandUseUse.getFields().length);
    IField serialVersionUID18 = SdkAssert.assertFieldExist(sdkCommandUseUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID18, 26);
    SdkAssert.assertFieldSignature(serialVersionUID18, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseUse'", 1, sdkCommandUseUse.getMethods().length);
    IMethod sdkCommandUseUse1 = SdkAssert.assertMethodExist(sdkCommandUseUse, "SdkCommandUseUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseUse'", 0, sdkCommandUseUse.getTypes().length);
    // type SmartEx
    IType smartEx = SdkAssert.assertTypeExists(extendedFormData, "SmartEx");
    SdkAssert.assertHasFlags(smartEx, 9);
    SdkAssert.assertHasSuperTypeSignature(smartEx, "QSmart;");
    SdkAssert.assertAnnotation(smartEx, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SmartEx
    SdkAssert.assertEquals("field count of 'SmartEx'", 1, smartEx.getFields().length);
    IField serialVersionUID19 = SdkAssert.assertFieldExist(smartEx, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID19, 26);
    SdkAssert.assertFieldSignature(serialVersionUID19, "J");

    SdkAssert.assertEquals("method count of 'SmartEx'", 2, smartEx.getMethods().length);
    IMethod smartEx1 = SdkAssert.assertMethodExist(smartEx, "SmartEx", new String[]{});
    SdkAssert.assertTrue(smartEx1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(smartEx1, "V");
    IMethod initValidationRules6 = SdkAssert.assertMethodExist(smartEx, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules6, "V");
    SdkAssert.assertAnnotation(initValidationRules6, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules6, new String[]{"ruleMap.remove(ValidationRule.CODE_TYPE);"}, true);

    SdkAssert.assertEquals("inner types count of 'SmartEx'", 0, smartEx.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfExtendedExtendedFormData() throws Exception {
    // type ExtendedExtendedFormData
    IType extendedExtendedFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.replace.ExtendedExtendedFormData");
    SdkAssert.assertHasFlags(extendedExtendedFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(extendedExtendedFormData, "QExtendedFormData;");

    // fields of ExtendedExtendedFormData
    SdkAssert.assertEquals("field count of 'ExtendedExtendedFormData'", 1, extendedExtendedFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(extendedExtendedFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'ExtendedExtendedFormData'", 64, extendedExtendedFormData.getMethods().length);
    IMethod extendedExtendedFormData1 = SdkAssert.assertMethodExist(extendedExtendedFormData, "ExtendedExtendedFormData", new String[]{});
    SdkAssert.assertTrue(extendedExtendedFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedExtendedFormData1, "V");
    IMethod getIgnoringGroupBoxExCreateNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getIgnoringGroupBoxExCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIgnoringGroupBoxExCreateNone, "QIgnoringGroupBoxExCreateNone;");
    IMethod getIgnoringGroupBoxExNoneCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getIgnoringGroupBoxExNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIgnoringGroupBoxExNoneCreate, "QIgnoringGroupBoxExNoneCreate;");
    IMethod getNameExEx = SdkAssert.assertMethodExist(extendedExtendedFormData, "getNameExEx", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getNameExEx, "QNameExEx;");
    IMethod getSdkCommandCreateCreateCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateCreateCreate, "QSdkCommandCreateCreateCreate;");
    IMethod getSdkCommandCreateCreateIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateCreateIgnore, "QSdkCommandCreateCreateIgnore;");
    IMethod getSdkCommandCreateCreateNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateCreateNone, "QSdkCommandCreateCreateNone;");
    IMethod getSdkCommandCreateCreateUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateCreateUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateCreateUse, "QSdkCommandCreateCreateUse;");
    IMethod getSdkCommandCreateIgnoreCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateIgnoreCreate, "QSdkCommandCreateIgnoreCreate;");
    IMethod getSdkCommandCreateIgnoreIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateIgnoreIgnore, "QSdkCommandCreateIgnoreIgnore;");
    IMethod getSdkCommandCreateIgnoreNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateIgnoreNone, "QSdkCommandCreateIgnoreNone;");
    IMethod getSdkCommandCreateIgnoreUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateIgnoreUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateIgnoreUse, "QSdkCommandCreateIgnoreUse;");
    IMethod getSdkCommandCreateNoneCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateNoneCreate, "QSdkCommandCreateNoneCreate;");
    IMethod getSdkCommandCreateNoneIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateNoneIgnore, "QSdkCommandCreateNoneIgnore;");
    IMethod getSdkCommandCreateNoneNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateNoneNone, "QSdkCommandCreateNoneNone;");
    IMethod getSdkCommandCreateNoneUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateNoneUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateNoneUse, "QSdkCommandCreateNoneUse;");
    IMethod getSdkCommandCreateUseCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateUseCreate, "QSdkCommandCreateUseCreate;");
    IMethod getSdkCommandCreateUseIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateUseIgnore, "QSdkCommandCreateUseIgnore;");
    IMethod getSdkCommandCreateUseNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateUseNone, "QSdkCommandCreateUseNone;");
    IMethod getSdkCommandCreateUseUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandCreateUseUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandCreateUseUse, "QSdkCommandCreateUseUse;");
    IMethod getSdkCommandIgnoreCreateCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreCreateCreate, "QSdkCommandIgnoreCreateCreate;");
    IMethod getSdkCommandIgnoreCreateIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreCreateIgnore, "QSdkCommandIgnoreCreateIgnore;");
    IMethod getSdkCommandIgnoreCreateNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreCreateNone, "QSdkCommandIgnoreCreateNone;");
    IMethod getSdkCommandIgnoreCreateUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreCreateUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreCreateUse, "QSdkCommandIgnoreCreateUse;");
    IMethod getSdkCommandIgnoreIgnoreCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreIgnoreCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreIgnoreCreate, "QSdkCommandIgnoreIgnoreCreate;");
    IMethod getSdkCommandIgnoreIgnoreUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreIgnoreUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreIgnoreUse, "QSdkCommandIgnoreIgnoreUse;");
    IMethod getSdkCommandIgnoreNoneCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreNoneCreate, "QSdkCommandIgnoreNoneCreate;");
    IMethod getSdkCommandIgnoreNoneUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreNoneUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreNoneUse, "QSdkCommandIgnoreNoneUse;");
    IMethod getSdkCommandIgnoreUseCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreUseCreate, "QSdkCommandIgnoreUseCreate;");
    IMethod getSdkCommandIgnoreUseIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreUseIgnore, "QSdkCommandIgnoreUseIgnore;");
    IMethod getSdkCommandIgnoreUseNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreUseNone, "QSdkCommandIgnoreUseNone;");
    IMethod getSdkCommandIgnoreUseUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandIgnoreUseUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandIgnoreUseUse, "QSdkCommandIgnoreUseUse;");
    IMethod getSdkCommandNoneCreateCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneCreateCreate, "QSdkCommandNoneCreateCreate;");
    IMethod getSdkCommandNoneCreateIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneCreateIgnore, "QSdkCommandNoneCreateIgnore;");
    IMethod getSdkCommandNoneCreateNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneCreateNone, "QSdkCommandNoneCreateNone;");
    IMethod getSdkCommandNoneCreateUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneCreateUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneCreateUse, "QSdkCommandNoneCreateUse;");
    IMethod getSdkCommandNoneIgnoreCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneIgnoreCreate, "QSdkCommandNoneIgnoreCreate;");
    IMethod getSdkCommandNoneIgnoreIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneIgnoreIgnore, "QSdkCommandNoneIgnoreIgnore;");
    IMethod getSdkCommandNoneIgnoreNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneIgnoreNone, "QSdkCommandNoneIgnoreNone;");
    IMethod getSdkCommandNoneIgnoreUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneIgnoreUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneIgnoreUse, "QSdkCommandNoneIgnoreUse;");
    IMethod getSdkCommandNoneNoneCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneNoneCreate, "QSdkCommandNoneNoneCreate;");
    IMethod getSdkCommandNoneNoneIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneNoneIgnore, "QSdkCommandNoneNoneIgnore;");
    IMethod getSdkCommandNoneNoneNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneNoneNone, "QSdkCommandNoneNoneNone;");
    IMethod getSdkCommandNoneNoneUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneNoneUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneNoneUse, "QSdkCommandNoneNoneUse;");
    IMethod getSdkCommandNoneUseCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneUseCreate, "QSdkCommandNoneUseCreate;");
    IMethod getSdkCommandNoneUseIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneUseIgnore, "QSdkCommandNoneUseIgnore;");
    IMethod getSdkCommandNoneUseNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneUseNone, "QSdkCommandNoneUseNone;");
    IMethod getSdkCommandNoneUseUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandNoneUseUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandNoneUseUse, "QSdkCommandNoneUseUse;");
    IMethod getSdkCommandUseCreateCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseCreateCreate, "QSdkCommandUseCreateCreate;");
    IMethod getSdkCommandUseCreateIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseCreateIgnore, "QSdkCommandUseCreateIgnore;");
    IMethod getSdkCommandUseCreateNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseCreateNone, "QSdkCommandUseCreateNone;");
    IMethod getSdkCommandUseCreateUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseCreateUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseCreateUse, "QSdkCommandUseCreateUse;");
    IMethod getSdkCommandUseIgnoreCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseIgnoreCreate, "QSdkCommandUseIgnoreCreate;");
    IMethod getSdkCommandUseIgnoreIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseIgnoreIgnore, "QSdkCommandUseIgnoreIgnore;");
    IMethod getSdkCommandUseIgnoreNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseIgnoreNone, "QSdkCommandUseIgnoreNone;");
    IMethod getSdkCommandUseIgnoreUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseIgnoreUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseIgnoreUse, "QSdkCommandUseIgnoreUse;");
    IMethod getSdkCommandUseNoneCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseNoneCreate, "QSdkCommandUseNoneCreate;");
    IMethod getSdkCommandUseNoneIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseNoneIgnore, "QSdkCommandUseNoneIgnore;");
    IMethod getSdkCommandUseNoneNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseNoneNone, "QSdkCommandUseNoneNone;");
    IMethod getSdkCommandUseNoneUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseNoneUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseNoneUse, "QSdkCommandUseNoneUse;");
    IMethod getSdkCommandUseUseCreate = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseCreate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseUseCreate, "QSdkCommandUseUseCreate;");
    IMethod getSdkCommandUseUseIgnore = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseIgnore", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseUseIgnore, "QSdkCommandUseUseIgnore;");
    IMethod getSdkCommandUseUseNone = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseNone", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseUseNone, "QSdkCommandUseUseNone;");
    IMethod getSdkCommandUseUseUse = SdkAssert.assertMethodExist(extendedExtendedFormData, "getSdkCommandUseUseUse", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSdkCommandUseUseUse, "QSdkCommandUseUseUse;");

    SdkAssert.assertEquals("inner types count of 'ExtendedExtendedFormData'", 63, extendedExtendedFormData.getTypes().length);
    // type IgnoringGroupBoxExCreateNone
    IType ignoringGroupBoxExCreateNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "IgnoringGroupBoxExCreateNone");
    SdkAssert.assertHasFlags(ignoringGroupBoxExCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(ignoringGroupBoxExCreateNone, "QIgnoringGroupBoxExCreate;");
    SdkAssert.assertAnnotation(ignoringGroupBoxExCreateNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of IgnoringGroupBoxExCreateNone
    SdkAssert.assertEquals("field count of 'IgnoringGroupBoxExCreateNone'", 1, ignoringGroupBoxExCreateNone.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(ignoringGroupBoxExCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'IgnoringGroupBoxExCreateNone'", 1, ignoringGroupBoxExCreateNone.getMethods().length);
    IMethod ignoringGroupBoxExCreateNone1 = SdkAssert.assertMethodExist(ignoringGroupBoxExCreateNone, "IgnoringGroupBoxExCreateNone", new String[]{});
    SdkAssert.assertTrue(ignoringGroupBoxExCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(ignoringGroupBoxExCreateNone1, "V");

    SdkAssert.assertEquals("inner types count of 'IgnoringGroupBoxExCreateNone'", 0, ignoringGroupBoxExCreateNone.getTypes().length);
    // type IgnoringGroupBoxExNoneCreate
    IType ignoringGroupBoxExNoneCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "IgnoringGroupBoxExNoneCreate");
    SdkAssert.assertHasFlags(ignoringGroupBoxExNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(ignoringGroupBoxExNoneCreate, "QAbstractValueFieldData<QString;>;");
    SdkAssert.assertAnnotation(ignoringGroupBoxExNoneCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of IgnoringGroupBoxExNoneCreate
    SdkAssert.assertEquals("field count of 'IgnoringGroupBoxExNoneCreate'", 1, ignoringGroupBoxExNoneCreate.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(ignoringGroupBoxExNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'IgnoringGroupBoxExNoneCreate'", 2, ignoringGroupBoxExNoneCreate.getMethods().length);
    IMethod ignoringGroupBoxExNoneCreate1 = SdkAssert.assertMethodExist(ignoringGroupBoxExNoneCreate, "IgnoringGroupBoxExNoneCreate", new String[]{});
    SdkAssert.assertTrue(ignoringGroupBoxExNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(ignoringGroupBoxExNoneCreate1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(ignoringGroupBoxExNoneCreate, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'IgnoringGroupBoxExNoneCreate'", 0, ignoringGroupBoxExNoneCreate.getTypes().length);
    // type NameExEx
    IType nameExEx = SdkAssert.assertTypeExists(extendedExtendedFormData, "NameExEx");
    SdkAssert.assertHasFlags(nameExEx, 9);
    SdkAssert.assertHasSuperTypeSignature(nameExEx, "QNameEx;");
    SdkAssert.assertAnnotation(nameExEx, "org.eclipse.scout.commons.annotations.Replace");

    // fields of NameExEx
    SdkAssert.assertEquals("field count of 'NameExEx'", 1, nameExEx.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(nameExEx, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'NameExEx'", 5, nameExEx.getMethods().length);
    IMethod nameExEx1 = SdkAssert.assertMethodExist(nameExEx, "NameExEx", new String[]{});
    SdkAssert.assertTrue(nameExEx1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(nameExEx1, "V");
    IMethod getStringProperty = SdkAssert.assertMethodExist(nameExEx, "getStringProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getStringProperty, "QString;");
    IMethod setStringProperty = SdkAssert.assertMethodExist(nameExEx, "setStringProperty", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setStringProperty, "V");
    IMethod getStringPropertyProperty = SdkAssert.assertMethodExist(nameExEx, "getStringPropertyProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getStringPropertyProperty, "QStringPropertyProperty;");
    IMethod initValidationRules1 = SdkAssert.assertMethodExist(nameExEx, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules1, "V");
    SdkAssert.assertAnnotation(initValidationRules1, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules1, new String[]{"ruleMap.put(ValidationRule.MANDATORY, true);", "ruleMap.put(ValidationRule.MAX_LENGTH, 15);"}, true);

    SdkAssert.assertEquals("inner types count of 'NameExEx'", 1, nameExEx.getTypes().length);
    // type StringPropertyProperty
    IType stringPropertyProperty = SdkAssert.assertTypeExists(nameExEx, "StringPropertyProperty");
    SdkAssert.assertHasFlags(stringPropertyProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(stringPropertyProperty, "QAbstractPropertyData<QString;>;");

    // fields of StringPropertyProperty
    SdkAssert.assertEquals("field count of 'StringPropertyProperty'", 1, stringPropertyProperty.getFields().length);
    IField serialVersionUID4 = SdkAssert.assertFieldExist(stringPropertyProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    SdkAssert.assertEquals("method count of 'StringPropertyProperty'", 1, stringPropertyProperty.getMethods().length);
    IMethod stringPropertyProperty1 = SdkAssert.assertMethodExist(stringPropertyProperty, "StringPropertyProperty", new String[]{});
    SdkAssert.assertTrue(stringPropertyProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(stringPropertyProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'StringPropertyProperty'", 0, stringPropertyProperty.getTypes().length);
    // type SdkCommandCreateCreateCreate
    IType sdkCommandCreateCreateCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateCreate");
    SdkAssert.assertHasFlags(sdkCommandCreateCreateCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateCreateCreate, "QSdkCommandCreateCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateCreateCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateCreateCreate
    SdkAssert.assertEquals("field count of 'SdkCommandCreateCreateCreate'", 1, sdkCommandCreateCreateCreate.getFields().length);
    IField serialVersionUID5 = SdkAssert.assertFieldExist(sdkCommandCreateCreateCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateCreateCreate'", 1, sdkCommandCreateCreateCreate.getMethods().length);
    IMethod sdkCommandCreateCreateCreate1 = SdkAssert.assertMethodExist(sdkCommandCreateCreateCreate, "SdkCommandCreateCreateCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateCreateCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateCreateCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateCreateCreate'", 0, sdkCommandCreateCreateCreate.getTypes().length);
    // type SdkCommandCreateCreateIgnore
    IType sdkCommandCreateCreateIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateIgnore");
    SdkAssert.assertHasFlags(sdkCommandCreateCreateIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateCreateIgnore, "QSdkCommandCreateCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateCreateIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateCreateIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandCreateCreateIgnore'", 1, sdkCommandCreateCreateIgnore.getFields().length);
    IField serialVersionUID6 = SdkAssert.assertFieldExist(sdkCommandCreateCreateIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateCreateIgnore'", 1, sdkCommandCreateCreateIgnore.getMethods().length);
    IMethod sdkCommandCreateCreateIgnore1 = SdkAssert.assertMethodExist(sdkCommandCreateCreateIgnore, "SdkCommandCreateCreateIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateCreateIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateCreateIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateCreateIgnore'", 0, sdkCommandCreateCreateIgnore.getTypes().length);
    // type SdkCommandCreateCreateNone
    IType sdkCommandCreateCreateNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateNone");
    SdkAssert.assertHasFlags(sdkCommandCreateCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateCreateNone, "QSdkCommandCreateCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateCreateNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateCreateNone
    SdkAssert.assertEquals("field count of 'SdkCommandCreateCreateNone'", 1, sdkCommandCreateCreateNone.getFields().length);
    IField serialVersionUID7 = SdkAssert.assertFieldExist(sdkCommandCreateCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateCreateNone'", 1, sdkCommandCreateCreateNone.getMethods().length);
    IMethod sdkCommandCreateCreateNone1 = SdkAssert.assertMethodExist(sdkCommandCreateCreateNone, "SdkCommandCreateCreateNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateCreateNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateCreateNone'", 0, sdkCommandCreateCreateNone.getTypes().length);
    // type SdkCommandCreateCreateUse
    IType sdkCommandCreateCreateUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateCreateUse");
    SdkAssert.assertHasFlags(sdkCommandCreateCreateUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateCreateUse, "QSdkCommandCreateCreate;");
    SdkAssert.assertAnnotation(sdkCommandCreateCreateUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateCreateUse
    SdkAssert.assertEquals("field count of 'SdkCommandCreateCreateUse'", 1, sdkCommandCreateCreateUse.getFields().length);
    IField serialVersionUID8 = SdkAssert.assertFieldExist(sdkCommandCreateCreateUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateCreateUse'", 1, sdkCommandCreateCreateUse.getMethods().length);
    IMethod sdkCommandCreateCreateUse1 = SdkAssert.assertMethodExist(sdkCommandCreateCreateUse, "SdkCommandCreateCreateUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateCreateUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateCreateUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateCreateUse'", 0, sdkCommandCreateCreateUse.getTypes().length);
    // type SdkCommandCreateIgnoreCreate
    IType sdkCommandCreateIgnoreCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreCreate");
    SdkAssert.assertHasFlags(sdkCommandCreateIgnoreCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateIgnoreCreate, "QSdkCommandCreateIgnore;");
    SdkAssert.assertAnnotation(sdkCommandCreateIgnoreCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateIgnoreCreate
    SdkAssert.assertEquals("field count of 'SdkCommandCreateIgnoreCreate'", 1, sdkCommandCreateIgnoreCreate.getFields().length);
    IField serialVersionUID9 = SdkAssert.assertFieldExist(sdkCommandCreateIgnoreCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID9, 26);
    SdkAssert.assertFieldSignature(serialVersionUID9, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateIgnoreCreate'", 1, sdkCommandCreateIgnoreCreate.getMethods().length);
    IMethod sdkCommandCreateIgnoreCreate1 = SdkAssert.assertMethodExist(sdkCommandCreateIgnoreCreate, "SdkCommandCreateIgnoreCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateIgnoreCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateIgnoreCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateIgnoreCreate'", 0, sdkCommandCreateIgnoreCreate.getTypes().length);
    // type SdkCommandCreateIgnoreIgnore
    IType sdkCommandCreateIgnoreIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreIgnore");
    SdkAssert.assertHasFlags(sdkCommandCreateIgnoreIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateIgnoreIgnore, "QSdkCommandCreateIgnore;");
    SdkAssert.assertAnnotation(sdkCommandCreateIgnoreIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateIgnoreIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandCreateIgnoreIgnore'", 1, sdkCommandCreateIgnoreIgnore.getFields().length);
    IField serialVersionUID10 = SdkAssert.assertFieldExist(sdkCommandCreateIgnoreIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID10, 26);
    SdkAssert.assertFieldSignature(serialVersionUID10, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateIgnoreIgnore'", 1, sdkCommandCreateIgnoreIgnore.getMethods().length);
    IMethod sdkCommandCreateIgnoreIgnore1 = SdkAssert.assertMethodExist(sdkCommandCreateIgnoreIgnore, "SdkCommandCreateIgnoreIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateIgnoreIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateIgnoreIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateIgnoreIgnore'", 0, sdkCommandCreateIgnoreIgnore.getTypes().length);
    // type SdkCommandCreateIgnoreNone
    IType sdkCommandCreateIgnoreNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreNone");
    SdkAssert.assertHasFlags(sdkCommandCreateIgnoreNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateIgnoreNone, "QSdkCommandCreateIgnore;");
    SdkAssert.assertAnnotation(sdkCommandCreateIgnoreNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateIgnoreNone
    SdkAssert.assertEquals("field count of 'SdkCommandCreateIgnoreNone'", 1, sdkCommandCreateIgnoreNone.getFields().length);
    IField serialVersionUID11 = SdkAssert.assertFieldExist(sdkCommandCreateIgnoreNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID11, 26);
    SdkAssert.assertFieldSignature(serialVersionUID11, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateIgnoreNone'", 1, sdkCommandCreateIgnoreNone.getMethods().length);
    IMethod sdkCommandCreateIgnoreNone1 = SdkAssert.assertMethodExist(sdkCommandCreateIgnoreNone, "SdkCommandCreateIgnoreNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateIgnoreNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateIgnoreNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateIgnoreNone'", 0, sdkCommandCreateIgnoreNone.getTypes().length);
    // type SdkCommandCreateIgnoreUse
    IType sdkCommandCreateIgnoreUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateIgnoreUse");
    SdkAssert.assertHasFlags(sdkCommandCreateIgnoreUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateIgnoreUse, "QSdkCommandCreateIgnore;");
    SdkAssert.assertAnnotation(sdkCommandCreateIgnoreUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateIgnoreUse
    SdkAssert.assertEquals("field count of 'SdkCommandCreateIgnoreUse'", 1, sdkCommandCreateIgnoreUse.getFields().length);
    IField serialVersionUID12 = SdkAssert.assertFieldExist(sdkCommandCreateIgnoreUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID12, 26);
    SdkAssert.assertFieldSignature(serialVersionUID12, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateIgnoreUse'", 1, sdkCommandCreateIgnoreUse.getMethods().length);
    IMethod sdkCommandCreateIgnoreUse1 = SdkAssert.assertMethodExist(sdkCommandCreateIgnoreUse, "SdkCommandCreateIgnoreUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateIgnoreUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateIgnoreUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateIgnoreUse'", 0, sdkCommandCreateIgnoreUse.getTypes().length);
    // type SdkCommandCreateNoneCreate
    IType sdkCommandCreateNoneCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneCreate");
    SdkAssert.assertHasFlags(sdkCommandCreateNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateNoneCreate, "QSdkCommandCreateNone;");
    SdkAssert.assertAnnotation(sdkCommandCreateNoneCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateNoneCreate
    SdkAssert.assertEquals("field count of 'SdkCommandCreateNoneCreate'", 1, sdkCommandCreateNoneCreate.getFields().length);
    IField serialVersionUID13 = SdkAssert.assertFieldExist(sdkCommandCreateNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID13, 26);
    SdkAssert.assertFieldSignature(serialVersionUID13, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateNoneCreate'", 1, sdkCommandCreateNoneCreate.getMethods().length);
    IMethod sdkCommandCreateNoneCreate1 = SdkAssert.assertMethodExist(sdkCommandCreateNoneCreate, "SdkCommandCreateNoneCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateNoneCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateNoneCreate'", 0, sdkCommandCreateNoneCreate.getTypes().length);
    // type SdkCommandCreateNoneIgnore
    IType sdkCommandCreateNoneIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneIgnore");
    SdkAssert.assertHasFlags(sdkCommandCreateNoneIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateNoneIgnore, "QSdkCommandCreateNone;");
    SdkAssert.assertAnnotation(sdkCommandCreateNoneIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateNoneIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandCreateNoneIgnore'", 1, sdkCommandCreateNoneIgnore.getFields().length);
    IField serialVersionUID14 = SdkAssert.assertFieldExist(sdkCommandCreateNoneIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID14, 26);
    SdkAssert.assertFieldSignature(serialVersionUID14, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateNoneIgnore'", 1, sdkCommandCreateNoneIgnore.getMethods().length);
    IMethod sdkCommandCreateNoneIgnore1 = SdkAssert.assertMethodExist(sdkCommandCreateNoneIgnore, "SdkCommandCreateNoneIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateNoneIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateNoneIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateNoneIgnore'", 0, sdkCommandCreateNoneIgnore.getTypes().length);
    // type SdkCommandCreateNoneNone
    IType sdkCommandCreateNoneNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneNone");
    SdkAssert.assertHasFlags(sdkCommandCreateNoneNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateNoneNone, "QSdkCommandCreateNone;");
    SdkAssert.assertAnnotation(sdkCommandCreateNoneNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateNoneNone
    SdkAssert.assertEquals("field count of 'SdkCommandCreateNoneNone'", 1, sdkCommandCreateNoneNone.getFields().length);
    IField serialVersionUID15 = SdkAssert.assertFieldExist(sdkCommandCreateNoneNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID15, 26);
    SdkAssert.assertFieldSignature(serialVersionUID15, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateNoneNone'", 1, sdkCommandCreateNoneNone.getMethods().length);
    IMethod sdkCommandCreateNoneNone1 = SdkAssert.assertMethodExist(sdkCommandCreateNoneNone, "SdkCommandCreateNoneNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateNoneNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateNoneNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateNoneNone'", 0, sdkCommandCreateNoneNone.getTypes().length);
    // type SdkCommandCreateNoneUse
    IType sdkCommandCreateNoneUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateNoneUse");
    SdkAssert.assertHasFlags(sdkCommandCreateNoneUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateNoneUse, "QSdkCommandCreateNone;");
    SdkAssert.assertAnnotation(sdkCommandCreateNoneUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateNoneUse
    SdkAssert.assertEquals("field count of 'SdkCommandCreateNoneUse'", 1, sdkCommandCreateNoneUse.getFields().length);
    IField serialVersionUID16 = SdkAssert.assertFieldExist(sdkCommandCreateNoneUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID16, 26);
    SdkAssert.assertFieldSignature(serialVersionUID16, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateNoneUse'", 1, sdkCommandCreateNoneUse.getMethods().length);
    IMethod sdkCommandCreateNoneUse1 = SdkAssert.assertMethodExist(sdkCommandCreateNoneUse, "SdkCommandCreateNoneUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateNoneUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateNoneUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateNoneUse'", 0, sdkCommandCreateNoneUse.getTypes().length);
    // type SdkCommandCreateUseCreate
    IType sdkCommandCreateUseCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseCreate");
    SdkAssert.assertHasFlags(sdkCommandCreateUseCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateUseCreate, "QSdkCommandCreateUse;");
    SdkAssert.assertAnnotation(sdkCommandCreateUseCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateUseCreate
    SdkAssert.assertEquals("field count of 'SdkCommandCreateUseCreate'", 1, sdkCommandCreateUseCreate.getFields().length);
    IField serialVersionUID17 = SdkAssert.assertFieldExist(sdkCommandCreateUseCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID17, 26);
    SdkAssert.assertFieldSignature(serialVersionUID17, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateUseCreate'", 1, sdkCommandCreateUseCreate.getMethods().length);
    IMethod sdkCommandCreateUseCreate1 = SdkAssert.assertMethodExist(sdkCommandCreateUseCreate, "SdkCommandCreateUseCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateUseCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateUseCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateUseCreate'", 0, sdkCommandCreateUseCreate.getTypes().length);
    // type SdkCommandCreateUseIgnore
    IType sdkCommandCreateUseIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseIgnore");
    SdkAssert.assertHasFlags(sdkCommandCreateUseIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateUseIgnore, "QSdkCommandCreateUse;");
    SdkAssert.assertAnnotation(sdkCommandCreateUseIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateUseIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandCreateUseIgnore'", 1, sdkCommandCreateUseIgnore.getFields().length);
    IField serialVersionUID18 = SdkAssert.assertFieldExist(sdkCommandCreateUseIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID18, 26);
    SdkAssert.assertFieldSignature(serialVersionUID18, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateUseIgnore'", 1, sdkCommandCreateUseIgnore.getMethods().length);
    IMethod sdkCommandCreateUseIgnore1 = SdkAssert.assertMethodExist(sdkCommandCreateUseIgnore, "SdkCommandCreateUseIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateUseIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateUseIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateUseIgnore'", 0, sdkCommandCreateUseIgnore.getTypes().length);
    // type SdkCommandCreateUseNone
    IType sdkCommandCreateUseNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseNone");
    SdkAssert.assertHasFlags(sdkCommandCreateUseNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateUseNone, "QSdkCommandCreateUse;");
    SdkAssert.assertAnnotation(sdkCommandCreateUseNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateUseNone
    SdkAssert.assertEquals("field count of 'SdkCommandCreateUseNone'", 1, sdkCommandCreateUseNone.getFields().length);
    IField serialVersionUID19 = SdkAssert.assertFieldExist(sdkCommandCreateUseNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID19, 26);
    SdkAssert.assertFieldSignature(serialVersionUID19, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateUseNone'", 1, sdkCommandCreateUseNone.getMethods().length);
    IMethod sdkCommandCreateUseNone1 = SdkAssert.assertMethodExist(sdkCommandCreateUseNone, "SdkCommandCreateUseNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateUseNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateUseNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateUseNone'", 0, sdkCommandCreateUseNone.getTypes().length);
    // type SdkCommandCreateUseUse
    IType sdkCommandCreateUseUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandCreateUseUse");
    SdkAssert.assertHasFlags(sdkCommandCreateUseUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandCreateUseUse, "QSdkCommandCreateUse;");
    SdkAssert.assertAnnotation(sdkCommandCreateUseUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandCreateUseUse
    SdkAssert.assertEquals("field count of 'SdkCommandCreateUseUse'", 1, sdkCommandCreateUseUse.getFields().length);
    IField serialVersionUID20 = SdkAssert.assertFieldExist(sdkCommandCreateUseUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID20, 26);
    SdkAssert.assertFieldSignature(serialVersionUID20, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandCreateUseUse'", 1, sdkCommandCreateUseUse.getMethods().length);
    IMethod sdkCommandCreateUseUse1 = SdkAssert.assertMethodExist(sdkCommandCreateUseUse, "SdkCommandCreateUseUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandCreateUseUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandCreateUseUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandCreateUseUse'", 0, sdkCommandCreateUseUse.getTypes().length);
    // type SdkCommandIgnoreCreateCreate
    IType sdkCommandIgnoreCreateCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateCreate");
    SdkAssert.assertHasFlags(sdkCommandIgnoreCreateCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreCreateCreate, "QSdkCommandIgnoreCreate;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreCreateCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreCreateCreate
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreCreateCreate'", 1, sdkCommandIgnoreCreateCreate.getFields().length);
    IField serialVersionUID21 = SdkAssert.assertFieldExist(sdkCommandIgnoreCreateCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID21, 26);
    SdkAssert.assertFieldSignature(serialVersionUID21, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreCreateCreate'", 1, sdkCommandIgnoreCreateCreate.getMethods().length);
    IMethod sdkCommandIgnoreCreateCreate1 = SdkAssert.assertMethodExist(sdkCommandIgnoreCreateCreate, "SdkCommandIgnoreCreateCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreCreateCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreCreateCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreCreateCreate'", 0, sdkCommandIgnoreCreateCreate.getTypes().length);
    // type SdkCommandIgnoreCreateIgnore
    IType sdkCommandIgnoreCreateIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateIgnore");
    SdkAssert.assertHasFlags(sdkCommandIgnoreCreateIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreCreateIgnore, "QSdkCommandIgnoreCreate;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreCreateIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreCreateIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreCreateIgnore'", 1, sdkCommandIgnoreCreateIgnore.getFields().length);
    IField serialVersionUID22 = SdkAssert.assertFieldExist(sdkCommandIgnoreCreateIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID22, 26);
    SdkAssert.assertFieldSignature(serialVersionUID22, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreCreateIgnore'", 1, sdkCommandIgnoreCreateIgnore.getMethods().length);
    IMethod sdkCommandIgnoreCreateIgnore1 = SdkAssert.assertMethodExist(sdkCommandIgnoreCreateIgnore, "SdkCommandIgnoreCreateIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreCreateIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreCreateIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreCreateIgnore'", 0, sdkCommandIgnoreCreateIgnore.getTypes().length);
    // type SdkCommandIgnoreCreateNone
    IType sdkCommandIgnoreCreateNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateNone");
    SdkAssert.assertHasFlags(sdkCommandIgnoreCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreCreateNone, "QSdkCommandIgnoreCreate;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreCreateNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreCreateNone
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreCreateNone'", 1, sdkCommandIgnoreCreateNone.getFields().length);
    IField serialVersionUID23 = SdkAssert.assertFieldExist(sdkCommandIgnoreCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID23, 26);
    SdkAssert.assertFieldSignature(serialVersionUID23, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreCreateNone'", 1, sdkCommandIgnoreCreateNone.getMethods().length);
    IMethod sdkCommandIgnoreCreateNone1 = SdkAssert.assertMethodExist(sdkCommandIgnoreCreateNone, "SdkCommandIgnoreCreateNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreCreateNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreCreateNone'", 0, sdkCommandIgnoreCreateNone.getTypes().length);
    // type SdkCommandIgnoreCreateUse
    IType sdkCommandIgnoreCreateUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreCreateUse");
    SdkAssert.assertHasFlags(sdkCommandIgnoreCreateUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreCreateUse, "QSdkCommandIgnoreCreate;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreCreateUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreCreateUse
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreCreateUse'", 1, sdkCommandIgnoreCreateUse.getFields().length);
    IField serialVersionUID24 = SdkAssert.assertFieldExist(sdkCommandIgnoreCreateUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID24, 26);
    SdkAssert.assertFieldSignature(serialVersionUID24, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreCreateUse'", 1, sdkCommandIgnoreCreateUse.getMethods().length);
    IMethod sdkCommandIgnoreCreateUse1 = SdkAssert.assertMethodExist(sdkCommandIgnoreCreateUse, "SdkCommandIgnoreCreateUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreCreateUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreCreateUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreCreateUse'", 0, sdkCommandIgnoreCreateUse.getTypes().length);
    // type SdkCommandIgnoreIgnoreCreate
    IType sdkCommandIgnoreIgnoreCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreIgnoreCreate");
    SdkAssert.assertHasFlags(sdkCommandIgnoreIgnoreCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreIgnoreCreate, "QAbstractValueFieldData<QString;>;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreIgnoreCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreIgnoreCreate
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreIgnoreCreate'", 1, sdkCommandIgnoreIgnoreCreate.getFields().length);
    IField serialVersionUID25 = SdkAssert.assertFieldExist(sdkCommandIgnoreIgnoreCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID25, 26);
    SdkAssert.assertFieldSignature(serialVersionUID25, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreIgnoreCreate'", 2, sdkCommandIgnoreIgnoreCreate.getMethods().length);
    IMethod sdkCommandIgnoreIgnoreCreate1 = SdkAssert.assertMethodExist(sdkCommandIgnoreIgnoreCreate, "SdkCommandIgnoreIgnoreCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreIgnoreCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreIgnoreCreate1, "V");
    IMethod initValidationRules2 = SdkAssert.assertMethodExist(sdkCommandIgnoreIgnoreCreate, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules2, "V");
    SdkAssert.assertAnnotation(initValidationRules2, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules2, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreIgnoreCreate'", 0, sdkCommandIgnoreIgnoreCreate.getTypes().length);
    // type SdkCommandIgnoreIgnoreUse
    IType sdkCommandIgnoreIgnoreUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreIgnoreUse");
    SdkAssert.assertHasFlags(sdkCommandIgnoreIgnoreUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreIgnoreUse, "QUsingFormFieldData;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreIgnoreUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreIgnoreUse
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreIgnoreUse'", 1, sdkCommandIgnoreIgnoreUse.getFields().length);
    IField serialVersionUID26 = SdkAssert.assertFieldExist(sdkCommandIgnoreIgnoreUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID26, 26);
    SdkAssert.assertFieldSignature(serialVersionUID26, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreIgnoreUse'", 2, sdkCommandIgnoreIgnoreUse.getMethods().length);
    IMethod sdkCommandIgnoreIgnoreUse1 = SdkAssert.assertMethodExist(sdkCommandIgnoreIgnoreUse, "SdkCommandIgnoreIgnoreUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreIgnoreUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreIgnoreUse1, "V");
    IMethod initValidationRules3 = SdkAssert.assertMethodExist(sdkCommandIgnoreIgnoreUse, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules3, "V");
    SdkAssert.assertAnnotation(initValidationRules3, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules3, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreIgnoreUse'", 0, sdkCommandIgnoreIgnoreUse.getTypes().length);
    // type SdkCommandIgnoreNoneCreate
    IType sdkCommandIgnoreNoneCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreNoneCreate");
    SdkAssert.assertHasFlags(sdkCommandIgnoreNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreNoneCreate, "QAbstractValueFieldData<QString;>;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreNoneCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreNoneCreate
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreNoneCreate'", 1, sdkCommandIgnoreNoneCreate.getFields().length);
    IField serialVersionUID27 = SdkAssert.assertFieldExist(sdkCommandIgnoreNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID27, 26);
    SdkAssert.assertFieldSignature(serialVersionUID27, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreNoneCreate'", 2, sdkCommandIgnoreNoneCreate.getMethods().length);
    IMethod sdkCommandIgnoreNoneCreate1 = SdkAssert.assertMethodExist(sdkCommandIgnoreNoneCreate, "SdkCommandIgnoreNoneCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreNoneCreate1, "V");
    IMethod initValidationRules4 = SdkAssert.assertMethodExist(sdkCommandIgnoreNoneCreate, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules4, "V");
    SdkAssert.assertAnnotation(initValidationRules4, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules4, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreNoneCreate'", 0, sdkCommandIgnoreNoneCreate.getTypes().length);
    // type SdkCommandIgnoreNoneUse
    IType sdkCommandIgnoreNoneUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreNoneUse");
    SdkAssert.assertHasFlags(sdkCommandIgnoreNoneUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreNoneUse, "QUsingFormFieldData;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreNoneUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreNoneUse
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreNoneUse'", 1, sdkCommandIgnoreNoneUse.getFields().length);
    IField serialVersionUID28 = SdkAssert.assertFieldExist(sdkCommandIgnoreNoneUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID28, 26);
    SdkAssert.assertFieldSignature(serialVersionUID28, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreNoneUse'", 2, sdkCommandIgnoreNoneUse.getMethods().length);
    IMethod sdkCommandIgnoreNoneUse1 = SdkAssert.assertMethodExist(sdkCommandIgnoreNoneUse, "SdkCommandIgnoreNoneUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreNoneUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreNoneUse1, "V");
    IMethod initValidationRules5 = SdkAssert.assertMethodExist(sdkCommandIgnoreNoneUse, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules5, "V");
    SdkAssert.assertAnnotation(initValidationRules5, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules5, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreNoneUse'", 0, sdkCommandIgnoreNoneUse.getTypes().length);
    // type SdkCommandIgnoreUseCreate
    IType sdkCommandIgnoreUseCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseCreate");
    SdkAssert.assertHasFlags(sdkCommandIgnoreUseCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreUseCreate, "QSdkCommandIgnoreUse;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreUseCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreUseCreate
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreUseCreate'", 1, sdkCommandIgnoreUseCreate.getFields().length);
    IField serialVersionUID29 = SdkAssert.assertFieldExist(sdkCommandIgnoreUseCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID29, 26);
    SdkAssert.assertFieldSignature(serialVersionUID29, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreUseCreate'", 1, sdkCommandIgnoreUseCreate.getMethods().length);
    IMethod sdkCommandIgnoreUseCreate1 = SdkAssert.assertMethodExist(sdkCommandIgnoreUseCreate, "SdkCommandIgnoreUseCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreUseCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreUseCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreUseCreate'", 0, sdkCommandIgnoreUseCreate.getTypes().length);
    // type SdkCommandIgnoreUseIgnore
    IType sdkCommandIgnoreUseIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseIgnore");
    SdkAssert.assertHasFlags(sdkCommandIgnoreUseIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreUseIgnore, "QSdkCommandIgnoreUse;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreUseIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreUseIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreUseIgnore'", 1, sdkCommandIgnoreUseIgnore.getFields().length);
    IField serialVersionUID30 = SdkAssert.assertFieldExist(sdkCommandIgnoreUseIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID30, 26);
    SdkAssert.assertFieldSignature(serialVersionUID30, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreUseIgnore'", 1, sdkCommandIgnoreUseIgnore.getMethods().length);
    IMethod sdkCommandIgnoreUseIgnore1 = SdkAssert.assertMethodExist(sdkCommandIgnoreUseIgnore, "SdkCommandIgnoreUseIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreUseIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreUseIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreUseIgnore'", 0, sdkCommandIgnoreUseIgnore.getTypes().length);
    // type SdkCommandIgnoreUseNone
    IType sdkCommandIgnoreUseNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseNone");
    SdkAssert.assertHasFlags(sdkCommandIgnoreUseNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreUseNone, "QSdkCommandIgnoreUse;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreUseNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreUseNone
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreUseNone'", 1, sdkCommandIgnoreUseNone.getFields().length);
    IField serialVersionUID31 = SdkAssert.assertFieldExist(sdkCommandIgnoreUseNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID31, 26);
    SdkAssert.assertFieldSignature(serialVersionUID31, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreUseNone'", 1, sdkCommandIgnoreUseNone.getMethods().length);
    IMethod sdkCommandIgnoreUseNone1 = SdkAssert.assertMethodExist(sdkCommandIgnoreUseNone, "SdkCommandIgnoreUseNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreUseNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreUseNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreUseNone'", 0, sdkCommandIgnoreUseNone.getTypes().length);
    // type SdkCommandIgnoreUseUse
    IType sdkCommandIgnoreUseUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandIgnoreUseUse");
    SdkAssert.assertHasFlags(sdkCommandIgnoreUseUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandIgnoreUseUse, "QSdkCommandIgnoreUse;");
    SdkAssert.assertAnnotation(sdkCommandIgnoreUseUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandIgnoreUseUse
    SdkAssert.assertEquals("field count of 'SdkCommandIgnoreUseUse'", 1, sdkCommandIgnoreUseUse.getFields().length);
    IField serialVersionUID32 = SdkAssert.assertFieldExist(sdkCommandIgnoreUseUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID32, 26);
    SdkAssert.assertFieldSignature(serialVersionUID32, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandIgnoreUseUse'", 1, sdkCommandIgnoreUseUse.getMethods().length);
    IMethod sdkCommandIgnoreUseUse1 = SdkAssert.assertMethodExist(sdkCommandIgnoreUseUse, "SdkCommandIgnoreUseUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandIgnoreUseUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandIgnoreUseUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandIgnoreUseUse'", 0, sdkCommandIgnoreUseUse.getTypes().length);
    // type SdkCommandNoneCreateCreate
    IType sdkCommandNoneCreateCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateCreate");
    SdkAssert.assertHasFlags(sdkCommandNoneCreateCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneCreateCreate, "QSdkCommandNoneCreate;");
    SdkAssert.assertAnnotation(sdkCommandNoneCreateCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneCreateCreate
    SdkAssert.assertEquals("field count of 'SdkCommandNoneCreateCreate'", 1, sdkCommandNoneCreateCreate.getFields().length);
    IField serialVersionUID33 = SdkAssert.assertFieldExist(sdkCommandNoneCreateCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID33, 26);
    SdkAssert.assertFieldSignature(serialVersionUID33, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneCreateCreate'", 1, sdkCommandNoneCreateCreate.getMethods().length);
    IMethod sdkCommandNoneCreateCreate1 = SdkAssert.assertMethodExist(sdkCommandNoneCreateCreate, "SdkCommandNoneCreateCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneCreateCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneCreateCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneCreateCreate'", 0, sdkCommandNoneCreateCreate.getTypes().length);
    // type SdkCommandNoneCreateIgnore
    IType sdkCommandNoneCreateIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateIgnore");
    SdkAssert.assertHasFlags(sdkCommandNoneCreateIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneCreateIgnore, "QSdkCommandNoneCreate;");
    SdkAssert.assertAnnotation(sdkCommandNoneCreateIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneCreateIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandNoneCreateIgnore'", 1, sdkCommandNoneCreateIgnore.getFields().length);
    IField serialVersionUID34 = SdkAssert.assertFieldExist(sdkCommandNoneCreateIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID34, 26);
    SdkAssert.assertFieldSignature(serialVersionUID34, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneCreateIgnore'", 1, sdkCommandNoneCreateIgnore.getMethods().length);
    IMethod sdkCommandNoneCreateIgnore1 = SdkAssert.assertMethodExist(sdkCommandNoneCreateIgnore, "SdkCommandNoneCreateIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneCreateIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneCreateIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneCreateIgnore'", 0, sdkCommandNoneCreateIgnore.getTypes().length);
    // type SdkCommandNoneCreateNone
    IType sdkCommandNoneCreateNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateNone");
    SdkAssert.assertHasFlags(sdkCommandNoneCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneCreateNone, "QSdkCommandNoneCreate;");
    SdkAssert.assertAnnotation(sdkCommandNoneCreateNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneCreateNone
    SdkAssert.assertEquals("field count of 'SdkCommandNoneCreateNone'", 1, sdkCommandNoneCreateNone.getFields().length);
    IField serialVersionUID35 = SdkAssert.assertFieldExist(sdkCommandNoneCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID35, 26);
    SdkAssert.assertFieldSignature(serialVersionUID35, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneCreateNone'", 1, sdkCommandNoneCreateNone.getMethods().length);
    IMethod sdkCommandNoneCreateNone1 = SdkAssert.assertMethodExist(sdkCommandNoneCreateNone, "SdkCommandNoneCreateNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneCreateNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneCreateNone'", 0, sdkCommandNoneCreateNone.getTypes().length);
    // type SdkCommandNoneCreateUse
    IType sdkCommandNoneCreateUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneCreateUse");
    SdkAssert.assertHasFlags(sdkCommandNoneCreateUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneCreateUse, "QSdkCommandNoneCreate;");
    SdkAssert.assertAnnotation(sdkCommandNoneCreateUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneCreateUse
    SdkAssert.assertEquals("field count of 'SdkCommandNoneCreateUse'", 1, sdkCommandNoneCreateUse.getFields().length);
    IField serialVersionUID36 = SdkAssert.assertFieldExist(sdkCommandNoneCreateUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID36, 26);
    SdkAssert.assertFieldSignature(serialVersionUID36, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneCreateUse'", 1, sdkCommandNoneCreateUse.getMethods().length);
    IMethod sdkCommandNoneCreateUse1 = SdkAssert.assertMethodExist(sdkCommandNoneCreateUse, "SdkCommandNoneCreateUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneCreateUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneCreateUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneCreateUse'", 0, sdkCommandNoneCreateUse.getTypes().length);
    // type SdkCommandNoneIgnoreCreate
    IType sdkCommandNoneIgnoreCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreCreate");
    SdkAssert.assertHasFlags(sdkCommandNoneIgnoreCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneIgnoreCreate, "QSdkCommandNoneIgnore;");
    SdkAssert.assertAnnotation(sdkCommandNoneIgnoreCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneIgnoreCreate
    SdkAssert.assertEquals("field count of 'SdkCommandNoneIgnoreCreate'", 1, sdkCommandNoneIgnoreCreate.getFields().length);
    IField serialVersionUID37 = SdkAssert.assertFieldExist(sdkCommandNoneIgnoreCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID37, 26);
    SdkAssert.assertFieldSignature(serialVersionUID37, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneIgnoreCreate'", 1, sdkCommandNoneIgnoreCreate.getMethods().length);
    IMethod sdkCommandNoneIgnoreCreate1 = SdkAssert.assertMethodExist(sdkCommandNoneIgnoreCreate, "SdkCommandNoneIgnoreCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneIgnoreCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneIgnoreCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneIgnoreCreate'", 0, sdkCommandNoneIgnoreCreate.getTypes().length);
    // type SdkCommandNoneIgnoreIgnore
    IType sdkCommandNoneIgnoreIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreIgnore");
    SdkAssert.assertHasFlags(sdkCommandNoneIgnoreIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneIgnoreIgnore, "QSdkCommandNoneIgnore;");
    SdkAssert.assertAnnotation(sdkCommandNoneIgnoreIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneIgnoreIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandNoneIgnoreIgnore'", 1, sdkCommandNoneIgnoreIgnore.getFields().length);
    IField serialVersionUID38 = SdkAssert.assertFieldExist(sdkCommandNoneIgnoreIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID38, 26);
    SdkAssert.assertFieldSignature(serialVersionUID38, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneIgnoreIgnore'", 1, sdkCommandNoneIgnoreIgnore.getMethods().length);
    IMethod sdkCommandNoneIgnoreIgnore1 = SdkAssert.assertMethodExist(sdkCommandNoneIgnoreIgnore, "SdkCommandNoneIgnoreIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneIgnoreIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneIgnoreIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneIgnoreIgnore'", 0, sdkCommandNoneIgnoreIgnore.getTypes().length);
    // type SdkCommandNoneIgnoreNone
    IType sdkCommandNoneIgnoreNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreNone");
    SdkAssert.assertHasFlags(sdkCommandNoneIgnoreNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneIgnoreNone, "QSdkCommandNoneIgnore;");
    SdkAssert.assertAnnotation(sdkCommandNoneIgnoreNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneIgnoreNone
    SdkAssert.assertEquals("field count of 'SdkCommandNoneIgnoreNone'", 1, sdkCommandNoneIgnoreNone.getFields().length);
    IField serialVersionUID39 = SdkAssert.assertFieldExist(sdkCommandNoneIgnoreNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID39, 26);
    SdkAssert.assertFieldSignature(serialVersionUID39, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneIgnoreNone'", 1, sdkCommandNoneIgnoreNone.getMethods().length);
    IMethod sdkCommandNoneIgnoreNone1 = SdkAssert.assertMethodExist(sdkCommandNoneIgnoreNone, "SdkCommandNoneIgnoreNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneIgnoreNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneIgnoreNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneIgnoreNone'", 0, sdkCommandNoneIgnoreNone.getTypes().length);
    // type SdkCommandNoneIgnoreUse
    IType sdkCommandNoneIgnoreUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneIgnoreUse");
    SdkAssert.assertHasFlags(sdkCommandNoneIgnoreUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneIgnoreUse, "QSdkCommandNoneIgnore;");
    SdkAssert.assertAnnotation(sdkCommandNoneIgnoreUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneIgnoreUse
    SdkAssert.assertEquals("field count of 'SdkCommandNoneIgnoreUse'", 1, sdkCommandNoneIgnoreUse.getFields().length);
    IField serialVersionUID40 = SdkAssert.assertFieldExist(sdkCommandNoneIgnoreUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID40, 26);
    SdkAssert.assertFieldSignature(serialVersionUID40, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneIgnoreUse'", 1, sdkCommandNoneIgnoreUse.getMethods().length);
    IMethod sdkCommandNoneIgnoreUse1 = SdkAssert.assertMethodExist(sdkCommandNoneIgnoreUse, "SdkCommandNoneIgnoreUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneIgnoreUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneIgnoreUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneIgnoreUse'", 0, sdkCommandNoneIgnoreUse.getTypes().length);
    // type SdkCommandNoneNoneCreate
    IType sdkCommandNoneNoneCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneCreate");
    SdkAssert.assertHasFlags(sdkCommandNoneNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneNoneCreate, "QSdkCommandNoneNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneNoneCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneNoneCreate
    SdkAssert.assertEquals("field count of 'SdkCommandNoneNoneCreate'", 1, sdkCommandNoneNoneCreate.getFields().length);
    IField serialVersionUID41 = SdkAssert.assertFieldExist(sdkCommandNoneNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID41, 26);
    SdkAssert.assertFieldSignature(serialVersionUID41, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneNoneCreate'", 1, sdkCommandNoneNoneCreate.getMethods().length);
    IMethod sdkCommandNoneNoneCreate1 = SdkAssert.assertMethodExist(sdkCommandNoneNoneCreate, "SdkCommandNoneNoneCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneNoneCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneNoneCreate'", 0, sdkCommandNoneNoneCreate.getTypes().length);
    // type SdkCommandNoneNoneIgnore
    IType sdkCommandNoneNoneIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneIgnore");
    SdkAssert.assertHasFlags(sdkCommandNoneNoneIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneNoneIgnore, "QSdkCommandNoneNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneNoneIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneNoneIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandNoneNoneIgnore'", 1, sdkCommandNoneNoneIgnore.getFields().length);
    IField serialVersionUID42 = SdkAssert.assertFieldExist(sdkCommandNoneNoneIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID42, 26);
    SdkAssert.assertFieldSignature(serialVersionUID42, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneNoneIgnore'", 1, sdkCommandNoneNoneIgnore.getMethods().length);
    IMethod sdkCommandNoneNoneIgnore1 = SdkAssert.assertMethodExist(sdkCommandNoneNoneIgnore, "SdkCommandNoneNoneIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneNoneIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneNoneIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneNoneIgnore'", 0, sdkCommandNoneNoneIgnore.getTypes().length);
    // type SdkCommandNoneNoneNone
    IType sdkCommandNoneNoneNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneNone");
    SdkAssert.assertHasFlags(sdkCommandNoneNoneNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneNoneNone, "QSdkCommandNoneNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneNoneNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneNoneNone
    SdkAssert.assertEquals("field count of 'SdkCommandNoneNoneNone'", 1, sdkCommandNoneNoneNone.getFields().length);
    IField serialVersionUID43 = SdkAssert.assertFieldExist(sdkCommandNoneNoneNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID43, 26);
    SdkAssert.assertFieldSignature(serialVersionUID43, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneNoneNone'", 1, sdkCommandNoneNoneNone.getMethods().length);
    IMethod sdkCommandNoneNoneNone1 = SdkAssert.assertMethodExist(sdkCommandNoneNoneNone, "SdkCommandNoneNoneNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneNoneNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneNoneNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneNoneNone'", 0, sdkCommandNoneNoneNone.getTypes().length);
    // type SdkCommandNoneNoneUse
    IType sdkCommandNoneNoneUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneNoneUse");
    SdkAssert.assertHasFlags(sdkCommandNoneNoneUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneNoneUse, "QSdkCommandNoneNone;");
    SdkAssert.assertAnnotation(sdkCommandNoneNoneUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneNoneUse
    SdkAssert.assertEquals("field count of 'SdkCommandNoneNoneUse'", 1, sdkCommandNoneNoneUse.getFields().length);
    IField serialVersionUID44 = SdkAssert.assertFieldExist(sdkCommandNoneNoneUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID44, 26);
    SdkAssert.assertFieldSignature(serialVersionUID44, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneNoneUse'", 1, sdkCommandNoneNoneUse.getMethods().length);
    IMethod sdkCommandNoneNoneUse1 = SdkAssert.assertMethodExist(sdkCommandNoneNoneUse, "SdkCommandNoneNoneUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneNoneUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneNoneUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneNoneUse'", 0, sdkCommandNoneNoneUse.getTypes().length);
    // type SdkCommandNoneUseCreate
    IType sdkCommandNoneUseCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseCreate");
    SdkAssert.assertHasFlags(sdkCommandNoneUseCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneUseCreate, "QSdkCommandNoneUse;");
    SdkAssert.assertAnnotation(sdkCommandNoneUseCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneUseCreate
    SdkAssert.assertEquals("field count of 'SdkCommandNoneUseCreate'", 1, sdkCommandNoneUseCreate.getFields().length);
    IField serialVersionUID45 = SdkAssert.assertFieldExist(sdkCommandNoneUseCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID45, 26);
    SdkAssert.assertFieldSignature(serialVersionUID45, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneUseCreate'", 1, sdkCommandNoneUseCreate.getMethods().length);
    IMethod sdkCommandNoneUseCreate1 = SdkAssert.assertMethodExist(sdkCommandNoneUseCreate, "SdkCommandNoneUseCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneUseCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneUseCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneUseCreate'", 0, sdkCommandNoneUseCreate.getTypes().length);
    // type SdkCommandNoneUseIgnore
    IType sdkCommandNoneUseIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseIgnore");
    SdkAssert.assertHasFlags(sdkCommandNoneUseIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneUseIgnore, "QSdkCommandNoneUse;");
    SdkAssert.assertAnnotation(sdkCommandNoneUseIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneUseIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandNoneUseIgnore'", 1, sdkCommandNoneUseIgnore.getFields().length);
    IField serialVersionUID46 = SdkAssert.assertFieldExist(sdkCommandNoneUseIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID46, 26);
    SdkAssert.assertFieldSignature(serialVersionUID46, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneUseIgnore'", 1, sdkCommandNoneUseIgnore.getMethods().length);
    IMethod sdkCommandNoneUseIgnore1 = SdkAssert.assertMethodExist(sdkCommandNoneUseIgnore, "SdkCommandNoneUseIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneUseIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneUseIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneUseIgnore'", 0, sdkCommandNoneUseIgnore.getTypes().length);
    // type SdkCommandNoneUseNone
    IType sdkCommandNoneUseNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseNone");
    SdkAssert.assertHasFlags(sdkCommandNoneUseNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneUseNone, "QSdkCommandNoneUse;");
    SdkAssert.assertAnnotation(sdkCommandNoneUseNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneUseNone
    SdkAssert.assertEquals("field count of 'SdkCommandNoneUseNone'", 1, sdkCommandNoneUseNone.getFields().length);
    IField serialVersionUID47 = SdkAssert.assertFieldExist(sdkCommandNoneUseNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID47, 26);
    SdkAssert.assertFieldSignature(serialVersionUID47, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneUseNone'", 1, sdkCommandNoneUseNone.getMethods().length);
    IMethod sdkCommandNoneUseNone1 = SdkAssert.assertMethodExist(sdkCommandNoneUseNone, "SdkCommandNoneUseNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneUseNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneUseNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneUseNone'", 0, sdkCommandNoneUseNone.getTypes().length);
    // type SdkCommandNoneUseUse
    IType sdkCommandNoneUseUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandNoneUseUse");
    SdkAssert.assertHasFlags(sdkCommandNoneUseUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandNoneUseUse, "QSdkCommandNoneUse;");
    SdkAssert.assertAnnotation(sdkCommandNoneUseUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandNoneUseUse
    SdkAssert.assertEquals("field count of 'SdkCommandNoneUseUse'", 1, sdkCommandNoneUseUse.getFields().length);
    IField serialVersionUID48 = SdkAssert.assertFieldExist(sdkCommandNoneUseUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID48, 26);
    SdkAssert.assertFieldSignature(serialVersionUID48, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandNoneUseUse'", 1, sdkCommandNoneUseUse.getMethods().length);
    IMethod sdkCommandNoneUseUse1 = SdkAssert.assertMethodExist(sdkCommandNoneUseUse, "SdkCommandNoneUseUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandNoneUseUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandNoneUseUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandNoneUseUse'", 0, sdkCommandNoneUseUse.getTypes().length);
    // type SdkCommandUseCreateCreate
    IType sdkCommandUseCreateCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateCreate");
    SdkAssert.assertHasFlags(sdkCommandUseCreateCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseCreateCreate, "QSdkCommandUseCreate;");
    SdkAssert.assertAnnotation(sdkCommandUseCreateCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseCreateCreate
    SdkAssert.assertEquals("field count of 'SdkCommandUseCreateCreate'", 1, sdkCommandUseCreateCreate.getFields().length);
    IField serialVersionUID49 = SdkAssert.assertFieldExist(sdkCommandUseCreateCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID49, 26);
    SdkAssert.assertFieldSignature(serialVersionUID49, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseCreateCreate'", 1, sdkCommandUseCreateCreate.getMethods().length);
    IMethod sdkCommandUseCreateCreate1 = SdkAssert.assertMethodExist(sdkCommandUseCreateCreate, "SdkCommandUseCreateCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseCreateCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseCreateCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseCreateCreate'", 0, sdkCommandUseCreateCreate.getTypes().length);
    // type SdkCommandUseCreateIgnore
    IType sdkCommandUseCreateIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateIgnore");
    SdkAssert.assertHasFlags(sdkCommandUseCreateIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseCreateIgnore, "QSdkCommandUseCreate;");
    SdkAssert.assertAnnotation(sdkCommandUseCreateIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseCreateIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandUseCreateIgnore'", 1, sdkCommandUseCreateIgnore.getFields().length);
    IField serialVersionUID50 = SdkAssert.assertFieldExist(sdkCommandUseCreateIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID50, 26);
    SdkAssert.assertFieldSignature(serialVersionUID50, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseCreateIgnore'", 1, sdkCommandUseCreateIgnore.getMethods().length);
    IMethod sdkCommandUseCreateIgnore1 = SdkAssert.assertMethodExist(sdkCommandUseCreateIgnore, "SdkCommandUseCreateIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseCreateIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseCreateIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseCreateIgnore'", 0, sdkCommandUseCreateIgnore.getTypes().length);
    // type SdkCommandUseCreateNone
    IType sdkCommandUseCreateNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateNone");
    SdkAssert.assertHasFlags(sdkCommandUseCreateNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseCreateNone, "QSdkCommandUseCreate;");
    SdkAssert.assertAnnotation(sdkCommandUseCreateNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseCreateNone
    SdkAssert.assertEquals("field count of 'SdkCommandUseCreateNone'", 1, sdkCommandUseCreateNone.getFields().length);
    IField serialVersionUID51 = SdkAssert.assertFieldExist(sdkCommandUseCreateNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID51, 26);
    SdkAssert.assertFieldSignature(serialVersionUID51, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseCreateNone'", 1, sdkCommandUseCreateNone.getMethods().length);
    IMethod sdkCommandUseCreateNone1 = SdkAssert.assertMethodExist(sdkCommandUseCreateNone, "SdkCommandUseCreateNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseCreateNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseCreateNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseCreateNone'", 0, sdkCommandUseCreateNone.getTypes().length);
    // type SdkCommandUseCreateUse
    IType sdkCommandUseCreateUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseCreateUse");
    SdkAssert.assertHasFlags(sdkCommandUseCreateUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseCreateUse, "QSdkCommandUseCreate;");
    SdkAssert.assertAnnotation(sdkCommandUseCreateUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseCreateUse
    SdkAssert.assertEquals("field count of 'SdkCommandUseCreateUse'", 1, sdkCommandUseCreateUse.getFields().length);
    IField serialVersionUID52 = SdkAssert.assertFieldExist(sdkCommandUseCreateUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID52, 26);
    SdkAssert.assertFieldSignature(serialVersionUID52, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseCreateUse'", 1, sdkCommandUseCreateUse.getMethods().length);
    IMethod sdkCommandUseCreateUse1 = SdkAssert.assertMethodExist(sdkCommandUseCreateUse, "SdkCommandUseCreateUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseCreateUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseCreateUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseCreateUse'", 0, sdkCommandUseCreateUse.getTypes().length);
    // type SdkCommandUseIgnoreCreate
    IType sdkCommandUseIgnoreCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreCreate");
    SdkAssert.assertHasFlags(sdkCommandUseIgnoreCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseIgnoreCreate, "QSdkCommandUseIgnore;");
    SdkAssert.assertAnnotation(sdkCommandUseIgnoreCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseIgnoreCreate
    SdkAssert.assertEquals("field count of 'SdkCommandUseIgnoreCreate'", 1, sdkCommandUseIgnoreCreate.getFields().length);
    IField serialVersionUID53 = SdkAssert.assertFieldExist(sdkCommandUseIgnoreCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID53, 26);
    SdkAssert.assertFieldSignature(serialVersionUID53, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseIgnoreCreate'", 1, sdkCommandUseIgnoreCreate.getMethods().length);
    IMethod sdkCommandUseIgnoreCreate1 = SdkAssert.assertMethodExist(sdkCommandUseIgnoreCreate, "SdkCommandUseIgnoreCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseIgnoreCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseIgnoreCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseIgnoreCreate'", 0, sdkCommandUseIgnoreCreate.getTypes().length);
    // type SdkCommandUseIgnoreIgnore
    IType sdkCommandUseIgnoreIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreIgnore");
    SdkAssert.assertHasFlags(sdkCommandUseIgnoreIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseIgnoreIgnore, "QSdkCommandUseIgnore;");
    SdkAssert.assertAnnotation(sdkCommandUseIgnoreIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseIgnoreIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandUseIgnoreIgnore'", 1, sdkCommandUseIgnoreIgnore.getFields().length);
    IField serialVersionUID54 = SdkAssert.assertFieldExist(sdkCommandUseIgnoreIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID54, 26);
    SdkAssert.assertFieldSignature(serialVersionUID54, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseIgnoreIgnore'", 1, sdkCommandUseIgnoreIgnore.getMethods().length);
    IMethod sdkCommandUseIgnoreIgnore1 = SdkAssert.assertMethodExist(sdkCommandUseIgnoreIgnore, "SdkCommandUseIgnoreIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseIgnoreIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseIgnoreIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseIgnoreIgnore'", 0, sdkCommandUseIgnoreIgnore.getTypes().length);
    // type SdkCommandUseIgnoreNone
    IType sdkCommandUseIgnoreNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreNone");
    SdkAssert.assertHasFlags(sdkCommandUseIgnoreNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseIgnoreNone, "QSdkCommandUseIgnore;");
    SdkAssert.assertAnnotation(sdkCommandUseIgnoreNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseIgnoreNone
    SdkAssert.assertEquals("field count of 'SdkCommandUseIgnoreNone'", 1, sdkCommandUseIgnoreNone.getFields().length);
    IField serialVersionUID55 = SdkAssert.assertFieldExist(sdkCommandUseIgnoreNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID55, 26);
    SdkAssert.assertFieldSignature(serialVersionUID55, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseIgnoreNone'", 1, sdkCommandUseIgnoreNone.getMethods().length);
    IMethod sdkCommandUseIgnoreNone1 = SdkAssert.assertMethodExist(sdkCommandUseIgnoreNone, "SdkCommandUseIgnoreNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseIgnoreNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseIgnoreNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseIgnoreNone'", 0, sdkCommandUseIgnoreNone.getTypes().length);
    // type SdkCommandUseIgnoreUse
    IType sdkCommandUseIgnoreUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseIgnoreUse");
    SdkAssert.assertHasFlags(sdkCommandUseIgnoreUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseIgnoreUse, "QSdkCommandUseIgnore;");
    SdkAssert.assertAnnotation(sdkCommandUseIgnoreUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseIgnoreUse
    SdkAssert.assertEquals("field count of 'SdkCommandUseIgnoreUse'", 1, sdkCommandUseIgnoreUse.getFields().length);
    IField serialVersionUID56 = SdkAssert.assertFieldExist(sdkCommandUseIgnoreUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID56, 26);
    SdkAssert.assertFieldSignature(serialVersionUID56, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseIgnoreUse'", 1, sdkCommandUseIgnoreUse.getMethods().length);
    IMethod sdkCommandUseIgnoreUse1 = SdkAssert.assertMethodExist(sdkCommandUseIgnoreUse, "SdkCommandUseIgnoreUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseIgnoreUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseIgnoreUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseIgnoreUse'", 0, sdkCommandUseIgnoreUse.getTypes().length);
    // type SdkCommandUseNoneCreate
    IType sdkCommandUseNoneCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneCreate");
    SdkAssert.assertHasFlags(sdkCommandUseNoneCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseNoneCreate, "QSdkCommandUseNone;");
    SdkAssert.assertAnnotation(sdkCommandUseNoneCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseNoneCreate
    SdkAssert.assertEquals("field count of 'SdkCommandUseNoneCreate'", 1, sdkCommandUseNoneCreate.getFields().length);
    IField serialVersionUID57 = SdkAssert.assertFieldExist(sdkCommandUseNoneCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID57, 26);
    SdkAssert.assertFieldSignature(serialVersionUID57, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseNoneCreate'", 1, sdkCommandUseNoneCreate.getMethods().length);
    IMethod sdkCommandUseNoneCreate1 = SdkAssert.assertMethodExist(sdkCommandUseNoneCreate, "SdkCommandUseNoneCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseNoneCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseNoneCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseNoneCreate'", 0, sdkCommandUseNoneCreate.getTypes().length);
    // type SdkCommandUseNoneIgnore
    IType sdkCommandUseNoneIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneIgnore");
    SdkAssert.assertHasFlags(sdkCommandUseNoneIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseNoneIgnore, "QSdkCommandUseNone;");
    SdkAssert.assertAnnotation(sdkCommandUseNoneIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseNoneIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandUseNoneIgnore'", 1, sdkCommandUseNoneIgnore.getFields().length);
    IField serialVersionUID58 = SdkAssert.assertFieldExist(sdkCommandUseNoneIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID58, 26);
    SdkAssert.assertFieldSignature(serialVersionUID58, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseNoneIgnore'", 1, sdkCommandUseNoneIgnore.getMethods().length);
    IMethod sdkCommandUseNoneIgnore1 = SdkAssert.assertMethodExist(sdkCommandUseNoneIgnore, "SdkCommandUseNoneIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseNoneIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseNoneIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseNoneIgnore'", 0, sdkCommandUseNoneIgnore.getTypes().length);
    // type SdkCommandUseNoneNone
    IType sdkCommandUseNoneNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneNone");
    SdkAssert.assertHasFlags(sdkCommandUseNoneNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseNoneNone, "QSdkCommandUseNone;");
    SdkAssert.assertAnnotation(sdkCommandUseNoneNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseNoneNone
    SdkAssert.assertEquals("field count of 'SdkCommandUseNoneNone'", 1, sdkCommandUseNoneNone.getFields().length);
    IField serialVersionUID59 = SdkAssert.assertFieldExist(sdkCommandUseNoneNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID59, 26);
    SdkAssert.assertFieldSignature(serialVersionUID59, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseNoneNone'", 1, sdkCommandUseNoneNone.getMethods().length);
    IMethod sdkCommandUseNoneNone1 = SdkAssert.assertMethodExist(sdkCommandUseNoneNone, "SdkCommandUseNoneNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseNoneNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseNoneNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseNoneNone'", 0, sdkCommandUseNoneNone.getTypes().length);
    // type SdkCommandUseNoneUse
    IType sdkCommandUseNoneUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseNoneUse");
    SdkAssert.assertHasFlags(sdkCommandUseNoneUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseNoneUse, "QSdkCommandUseNone;");
    SdkAssert.assertAnnotation(sdkCommandUseNoneUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseNoneUse
    SdkAssert.assertEquals("field count of 'SdkCommandUseNoneUse'", 1, sdkCommandUseNoneUse.getFields().length);
    IField serialVersionUID60 = SdkAssert.assertFieldExist(sdkCommandUseNoneUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID60, 26);
    SdkAssert.assertFieldSignature(serialVersionUID60, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseNoneUse'", 1, sdkCommandUseNoneUse.getMethods().length);
    IMethod sdkCommandUseNoneUse1 = SdkAssert.assertMethodExist(sdkCommandUseNoneUse, "SdkCommandUseNoneUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseNoneUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseNoneUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseNoneUse'", 0, sdkCommandUseNoneUse.getTypes().length);
    // type SdkCommandUseUseCreate
    IType sdkCommandUseUseCreate = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseCreate");
    SdkAssert.assertHasFlags(sdkCommandUseUseCreate, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseUseCreate, "QSdkCommandUseUse;");
    SdkAssert.assertAnnotation(sdkCommandUseUseCreate, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseUseCreate
    SdkAssert.assertEquals("field count of 'SdkCommandUseUseCreate'", 1, sdkCommandUseUseCreate.getFields().length);
    IField serialVersionUID61 = SdkAssert.assertFieldExist(sdkCommandUseUseCreate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID61, 26);
    SdkAssert.assertFieldSignature(serialVersionUID61, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseUseCreate'", 1, sdkCommandUseUseCreate.getMethods().length);
    IMethod sdkCommandUseUseCreate1 = SdkAssert.assertMethodExist(sdkCommandUseUseCreate, "SdkCommandUseUseCreate", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseUseCreate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseUseCreate1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseUseCreate'", 0, sdkCommandUseUseCreate.getTypes().length);
    // type SdkCommandUseUseIgnore
    IType sdkCommandUseUseIgnore = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseIgnore");
    SdkAssert.assertHasFlags(sdkCommandUseUseIgnore, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseUseIgnore, "QSdkCommandUseUse;");
    SdkAssert.assertAnnotation(sdkCommandUseUseIgnore, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseUseIgnore
    SdkAssert.assertEquals("field count of 'SdkCommandUseUseIgnore'", 1, sdkCommandUseUseIgnore.getFields().length);
    IField serialVersionUID62 = SdkAssert.assertFieldExist(sdkCommandUseUseIgnore, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID62, 26);
    SdkAssert.assertFieldSignature(serialVersionUID62, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseUseIgnore'", 1, sdkCommandUseUseIgnore.getMethods().length);
    IMethod sdkCommandUseUseIgnore1 = SdkAssert.assertMethodExist(sdkCommandUseUseIgnore, "SdkCommandUseUseIgnore", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseUseIgnore1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseUseIgnore1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseUseIgnore'", 0, sdkCommandUseUseIgnore.getTypes().length);
    // type SdkCommandUseUseNone
    IType sdkCommandUseUseNone = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseNone");
    SdkAssert.assertHasFlags(sdkCommandUseUseNone, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseUseNone, "QSdkCommandUseUse;");
    SdkAssert.assertAnnotation(sdkCommandUseUseNone, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseUseNone
    SdkAssert.assertEquals("field count of 'SdkCommandUseUseNone'", 1, sdkCommandUseUseNone.getFields().length);
    IField serialVersionUID63 = SdkAssert.assertFieldExist(sdkCommandUseUseNone, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID63, 26);
    SdkAssert.assertFieldSignature(serialVersionUID63, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseUseNone'", 1, sdkCommandUseUseNone.getMethods().length);
    IMethod sdkCommandUseUseNone1 = SdkAssert.assertMethodExist(sdkCommandUseUseNone, "SdkCommandUseUseNone", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseUseNone1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseUseNone1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseUseNone'", 0, sdkCommandUseUseNone.getTypes().length);
    // type SdkCommandUseUseUse
    IType sdkCommandUseUseUse = SdkAssert.assertTypeExists(extendedExtendedFormData, "SdkCommandUseUseUse");
    SdkAssert.assertHasFlags(sdkCommandUseUseUse, 9);
    SdkAssert.assertHasSuperTypeSignature(sdkCommandUseUseUse, "QSdkCommandUseUse;");
    SdkAssert.assertAnnotation(sdkCommandUseUseUse, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SdkCommandUseUseUse
    SdkAssert.assertEquals("field count of 'SdkCommandUseUseUse'", 1, sdkCommandUseUseUse.getFields().length);
    IField serialVersionUID64 = SdkAssert.assertFieldExist(sdkCommandUseUseUse, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID64, 26);
    SdkAssert.assertFieldSignature(serialVersionUID64, "J");

    SdkAssert.assertEquals("method count of 'SdkCommandUseUseUse'", 1, sdkCommandUseUseUse.getMethods().length);
    IMethod sdkCommandUseUseUse1 = SdkAssert.assertMethodExist(sdkCommandUseUseUse, "SdkCommandUseUseUse", new String[]{});
    SdkAssert.assertTrue(sdkCommandUseUseUse1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(sdkCommandUseUseUse1, "V");

    SdkAssert.assertEquals("inner types count of 'SdkCommandUseUseUse'", 0, sdkCommandUseUseUse.getTypes().length);
  }
}
