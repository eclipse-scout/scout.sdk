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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReplaceFormFiledTest extends AbstractScoutSdkTest {

  private static IType s_baseFormData;
  private static IType s_extendedFormData;
  private static IType s_extendedExtendedFormData;
  private static Map<String, String> s_validationRules;

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("operation/formData", "formdata.shared", "formdata.client");

    IProject sharedProject = getProject("formdata.shared");
    assertNotNull(sharedProject);

    s_baseFormData = updateFormData(sharedProject, "formdata.client.ui.forms.replace.BaseForm", "formdata.shared.services.process.replace.BaseFormData", "AbstractFormData");
    s_extendedFormData = updateFormData(sharedProject, "formdata.client.ui.forms.replace.ExtendedForm", "formdata.shared.services.process.replace.ExtendedFormData", "BaseFormData");
    s_extendedExtendedFormData = updateFormData(sharedProject, "formdata.client.ui.forms.replace.ExtendedExtendedForm", "formdata.shared.services.process.replace.ExtendedExtendedFormData", "ExtendedFormData");

    s_validationRules = new HashMap<String, String>();
    s_validationRules.put("ValidationRule.MANDATORY", "\"mandatory\"");
    s_validationRules.put("ValidationRule.MIN_VALUE", "\"minValue\"");
    s_validationRules.put("ValidationRule.MAX_VALUE", "\"maxValue\"");
    s_validationRules.put("ValidationRule.MIN_LENGTH", "\"minLength\"");
    s_validationRules.put("ValidationRule.MAX_LENGTH", "\"maxLength\"");
    s_validationRules.put("ValidationRule.CODE_TYPE", "\"codeType\"");
    s_validationRules.put("ValidationRule.LOOKUP_CALL", "\"lookupCall\"");
    s_validationRules.put("ValidationRule.REGEX", "\"regex\"");
    s_validationRules.put("ValidationRule.MASTER_VALUE_FIELD", "\"masterValueField\"");
    s_validationRules.put("ValidationRule.MASTER_VALUE_REQUIRED", "\"masterValueRequired\"");
    s_validationRules.put("ValidationRule.ZERO_NULL_EQUALITY", "\"zeroNullEquality\"");
  }

  private static IType updateFormData(IProject sharedProject, String formFqcn, String formDataFqcn, String formDataSuperClassName) throws Exception {
    IType form = TypeUtility.getType(formFqcn);
    assertTrue(TypeUtility.exists(form));

    FormDataUpdateOperation op = new FormDataUpdateOperation(form);
    OperationJob job = new OperationJob(op);
    job.schedule();
    job.join();
    buildWorkspace();
    IType formData = op.getFormDataType();
    assertTrue(TypeUtility.exists(formData));
    assertEquals(formDataFqcn, formData.getFullyQualifiedName());
    assertEquals(formDataSuperClassName, formData.getSuperclassName());
    return formData;
  }

  private static void assertValidationRules(IType type, String... expectedLines) throws Exception {
    IMethod initValidationRulesMethod = TypeUtility.getMethod(type, "initValidationRules");

    if (expectedLines == null || expectedLines.length == 0) {
      assertFalse(TypeUtility.exists(initValidationRulesMethod));
      return;
    }

    // validation rules are expected. Check contents.
    assertTrue(TypeUtility.exists(initValidationRulesMethod));

    String source = initValidationRulesMethod.getSource();
    ISourceRange range = TypeUtility.getContentRange(initValidationRulesMethod);
    source = initValidationRulesMethod.getOpenable().getBuffer().getText(range.getOffset(), range.getLength());

    assertNotNull(source);

    source = source.replace("super.initValidationRules(ruleMap);", "");
    for (String line : expectedLines) {
      source = source.replace(line, "");

      // inline constants
      for (Map.Entry<String, String> rule : s_validationRules.entrySet()) {
        line = line.replace(rule.getKey(), rule.getValue());
      }
      source = source.replace(line, "");
    }

    source = source.trim();
    assertEquals("", source);
  }

  /* ##########################################################################
   * BaseForm tests
   * ##########################################################################
   */
  @Test
  public void testBaseFormName() throws Exception {
    IType type = s_baseFormData.getType("Name");
    assertTrue(TypeUtility.exists(type));
    assertEquals("AbstractValueFieldData<String>", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MANDATORY, true);",
        "ruleMap.put(ValidationRule.MAX_LENGTH, 60);");
  }

  @Test
  public void testBaseFormSmart() throws Exception {
    IType type = s_baseFormData.getType("Smart");
    assertTrue(TypeUtility.exists(type));
    assertEquals("AbstractValueFieldData<Long>", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.CODE_TYPE, TestingCodeType.class);",
        "ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);");
  }

  @Test
  public void testBaseFormLookup() throws Exception {
    IType type = s_baseFormData.getType("Lookup");
    assertTrue(TypeUtility.exists(type));
    assertEquals("AbstractValueFieldData<Long>", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.LOOKUP_CALL, TestingLookupCall.class);",
        "ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);");
  }

  @Test
  public void testBaseFormDataIgnoringGroupBox() throws Exception {
    IType type = s_baseFormData.getType("IgnoringGroupBox");
    assertFalse(TypeUtility.exists(type));
  }

  @Test
  public void testBaseFormDataClose() throws Exception {
    IType type = s_baseFormData.getType("Close");
    assertFalse(TypeUtility.exists(type));
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testBaseFormDataSdkCommandNone() throws Exception {
    IType type = s_baseFormData.getType("SdkCommandNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("AbstractValueFieldData<String>", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testBaseFormDataSdkCommandCreate() throws Exception {
    IType type = s_baseFormData.getType("SdkCommandCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("AbstractValueFieldData<String>", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testBaseFormDataSdkCommandUse() throws Exception {
    IType type = s_baseFormData.getType("SdkCommandUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("UsingFormFieldData", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testBaseFormDataSdkCommandIgnore() throws Exception {
    IType type = s_baseFormData.getType("SdkCommandIgnore");
    assertFalse(TypeUtility.exists(type));
  }

  /* ##########################################################################
   * ExtendedForm tests
   * ##########################################################################
   */
  @Test
  public void testExtendedFormFirstname() throws Exception {
    IType type = s_extendedFormData.getType("FirstName");
    assertTrue(TypeUtility.exists(type));
    assertEquals("AbstractValueFieldData<String>", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  public void testExtendedFormNameEx() throws Exception {
    IType type = s_extendedFormData.getType("NameEx");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.Name", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MANDATORY, false);",
        "ruleMap.put(ValidationRule.MAX_LENGTH, 100);");
  }

  @Test
  public void testExtendedFormSmartEx() throws Exception {
    IType type = s_extendedFormData.getType("SmartEx");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.Smart", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.remove(ValidationRule.CODE_TYPE);");
  }

  @Test
  public void testExtendedFormDataIgnoringGroupBoxExNone() throws Exception {
    IType type = s_extendedFormData.getType("IgnoringGroupBoxExNone");
    assertFalse(TypeUtility.exists(type));
  }

  @Test
  public void testExtendedFormDataIgnoringGroupBoxExWithCreate() throws Exception {
    IType type = s_extendedFormData.getType("IgnoringGroupBoxExCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("AbstractValueFieldData<String>", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  public void testExtendedFormDataIgnoringGroupBoxExUse() throws Exception {
    IType type = s_extendedFormData.getType("IgnoringGroupBoxExUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("UsingFormFieldData", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  public void testExtendedFormDataIgnoringGroupBoxExIgnore() throws Exception {
    IType type = s_extendedFormData.getType("IgnoringGroupBoxExIgnore");
    assertFalse(TypeUtility.exists(type));
  }

  @Test
  public void testExtendedFormDataCloseEx() throws Exception {
    IType type = s_extendedFormData.getType("CloseEx");
    assertFalse(TypeUtility.exists(type));
  }

  @Test
  public void testExtendedFormIgnoringGroupBoxExNone() throws Exception {
    IType type = s_extendedFormData.getType("IgnoringGroupBoxExNone");
    assertFalse(TypeUtility.exists(type));
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandNoneNone() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandNoneNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandNoneCreate() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandNoneCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandNoneUse() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandNoneUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandNoneIgnore() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandNoneIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandCreateNone() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandCreateNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandCreateCreate() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandCreateCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandCreateUse() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandCreateUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandCreateIgnore() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandCreateIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandUseNone() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandUseNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandUseCreate() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandUseCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandUseUse() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandUseUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandUseIgnore() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandUseIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("BaseFormData.SdkCommandUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandIgnoreNone() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandIgnoreNone");
    assertFalse(TypeUtility.exists(type));
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandIgnoreCreate() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandIgnoreCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("AbstractValueFieldData<String>", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandIgnoreUse() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandIgnoreUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("UsingFormFieldData", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedFormDataSdkCommandIgnoreIgnore() throws Exception {
    IType type = s_extendedFormData.getType("SdkCommandIgnoreIgnore");
    assertFalse(TypeUtility.exists(type));
  }

  /* ##########################################################################
   * ExtendedExtendedForm tests
   * ##########################################################################
   */
  @Test
  public void testExtendedExtendedFormNameExEx() throws Exception {
    IType type = s_extendedExtendedFormData.getType("NameExEx");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.NameEx", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 15);");
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneNoneNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneNoneNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneNoneCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneNoneCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneNoneUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneNoneUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneNoneIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneNoneIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneCreateNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneCreateNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneCreateCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneCreateCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneCreateUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneCreateUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneCreateIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneCreateIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneUseNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneUseNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneUseCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneUseCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneUseUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneUseUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneUseIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneUseIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneIgnoreNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneIgnoreNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneIgnoreCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneIgnoreCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneIgnoreUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneIgnoreUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandNoneIgnoreIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandNoneIgnoreIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandNoneIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateNoneNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateNoneNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateNoneCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateNoneCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateNoneUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateNoneUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateNoneIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateNoneIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateCreateNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateCreateNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateCreateCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateCreateCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateCreateUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateCreateUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateCreateIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateCreateIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateUseNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateUseNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateUseCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateUseCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateUseUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateUseUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateUseIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateUseIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateIgnoreNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateIgnoreNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateIgnoreCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateIgnoreCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateIgnoreUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateIgnoreUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandCreateIgnoreIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandCreateIgnoreIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandCreateIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseNoneNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseNoneNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseNoneCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseNoneCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseNoneUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseNoneUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseNoneIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseNoneIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseNone", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseCreateNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseCreateNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseCreateCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseCreateCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseCreateUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseCreateUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseCreateIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseCreateIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseUseNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseUseNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseUseCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseUseCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseUseUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseUseUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseUseIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseUseIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseIgnoreNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseIgnoreNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseIgnoreCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseIgnoreCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseIgnoreUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseIgnoreUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandUseIgnoreIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandUseIgnoreIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandUseIgnore", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreNoneNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreNoneNone");
    assertFalse(TypeUtility.exists(type));
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreNoneCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreNoneCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("AbstractValueFieldData<String>", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreNoneUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreNoneUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("UsingFormFieldData", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreNoneIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreNoneIgnore");
    assertFalse(TypeUtility.exists(type));
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreCreateNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreCreateNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandIgnoreCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreCreateCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreCreateCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandIgnoreCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreCreateUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreCreateUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandIgnoreCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreCreateIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreCreateIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandIgnoreCreate", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreUseNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreUseNone");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandIgnoreUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreUseCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreUseCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandIgnoreUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreUseUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreUseUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandIgnoreUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreUseIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreUseIgnore");
    assertTrue(TypeUtility.exists(type));
    assertEquals("ExtendedFormData.SdkCommandIgnoreUse", type.getSuperclassName());
    assertValidationRules(type);
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreIgnoreNone() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreIgnoreNone");
    assertFalse(TypeUtility.exists(type));
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreIgnoreCreate() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreIgnoreCreate");
    assertTrue(TypeUtility.exists(type));
    assertEquals("AbstractValueFieldData<String>", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreIgnoreUse() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreIgnoreUse");
    assertTrue(TypeUtility.exists(type));
    assertEquals("UsingFormFieldData", type.getSuperclassName());
    assertValidationRules(type,
        "ruleMap.put(ValidationRule.MAX_LENGTH, 4000);");
  }

  @Test
  @Generated("Replace Field Test Generator")
  public void testExtendedExtendedFormDataSdkCommandIgnoreIgnoreIgnore() throws Exception {
    IType type = s_extendedExtendedFormData.getType("SdkCommandIgnoreIgnoreIgnore");
    assertFalse(TypeUtility.exists(type));
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}
