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
package org.eclipse.scout.sdk.internal.test.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.util.method.SimpleMethodReturnValueParser;
import org.junit.Assert;
import org.junit.Test;

public class RegexTest {

  @Test
  public void testTypeReferenceRegex() {
    checkRegex("-Double.MAX_VALUE", SimpleMethodReturnValueParser.REGEX_TYPE_REFERENCE, "Double");
    checkRegex("TestClass.methodcall();", SimpleMethodReturnValueParser.REGEX_TYPE_REFERENCE, "TestClass");
    checkRegex("45.43", SimpleMethodReturnValueParser.REGEX_TYPE_REFERENCE, null);
    checkRegex("m_member.getValue()", SimpleMethodReturnValueParser.REGEX_TYPE_REFERENCE, "m_member");
    checkRegex("true", SimpleMethodReturnValueParser.REGEX_TYPE_REFERENCE, null);
    checkRegex("null", SimpleMethodReturnValueParser.REGEX_TYPE_REFERENCE, null);
  }

  @Test
  public void testMethodPresenterValue() {
    checkRegex("{ int a=0; return abcdefg.aaa; } ", SimpleMethodReturnValueParser.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE, "abcdefg.aaa");
    checkRegex("{ int a=0; return \"abcdefg;aaa\"; } ", SimpleMethodReturnValueParser.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE, "\"abcdefg;aaa\"");
    checkRegex("{ int a=0; return \"abcdefg.aaa\"; } ", SimpleMethodReturnValueParser.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE, "\"abcdefg.aaa\"");
    checkRegex("{ int a=0; return \"abcdefg.aaa; } ", SimpleMethodReturnValueParser.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE, null);
    checkRegex("{ int a=0; return abcdefg.aaa\"; } ", SimpleMethodReturnValueParser.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE, null);
    checkRegex("{ int a=0; return \"abcde\\\"fg.aaa\"; } ", SimpleMethodReturnValueParser.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE, "\"abcde\\\"fg.aaa\"");
    checkRegex("{ int a=0; return \"abcde\\\"jk\\\".aaa\"; } ", SimpleMethodReturnValueParser.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE, "\"abcde\\\"jk\\\".aaa\"");
    checkRegex("{ int a=0; return TEXTS.get(\"InvalidPhoneNumberMessageX\"); } ", SimpleMethodReturnValueParser.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE, "TEXTS.get(\"InvalidPhoneNumberMessageX\")");
    checkRegex("{ int a=0; return TEXTS.get(\"InvalidPhoneNumber;MessageX\"); } ", SimpleMethodReturnValueParser.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE, "TEXTS.get(\"InvalidPhoneNumber;MessageX\")");
  }

  private void checkRegex(String valToCheck, Pattern regex, String expectedVal) {
    Matcher m = regex.matcher(valToCheck);
    boolean found = m.find();
    Assert.assertTrue(found || expectedVal == null);
    if (found) {
      Assert.assertEquals(expectedVal, m.group(1).trim());
    }
  }

  @Test
  public void checkValidationRulePatterns() {
    checkValidationRulePatternImpl("@ValidationRule(ValidationRule.MANDATORY)", "ValidationRule.MANDATORY");
    checkValidationRulePatternImpl("@ValidationRule(MANDATORY)", "MANDATORY");
    checkValidationRulePatternImpl("@ValidationRule(\"mandatory\")", "\"mandatory\"");
    checkValidationRulePatternImpl("@ValidationRule(MyValidationRules.CUSTOM)", "MyValidationRules.CUSTOM");
    checkValidationRulePatternImpl("@ValidationRule(\"custom\")", "\"custom\"");
    checkValidationRulePatternImpl("@ValidationRule( value = ValidationRule.MANDATORY ,\n generatedSourceCode=\"dgsfdgasfgasdfdsaf\")", "ValidationRule.MANDATORY ");
    checkValidationRulePatternImpl("@ValidationRule(generatedSourceCode=\"dgsfdgasfgasdfdsaf\", value = ValidationRule.MANDATORY )", "ValidationRule.MANDATORY ");
  }

  private void checkValidationRulePatternImpl(String text, String group2) {
    //copied from SdkTypeUtility.VALIDATION_RULE_PATTERN
    Pattern VALIDATION_RULE_PATTERN = Pattern.compile("[@]ValidationRule\\s*[(]\\s*([^)]*value\\s*=)?\\s*([^,)]+)([,][^)]*)?[)]", Pattern.DOTALL);
    Matcher m = VALIDATION_RULE_PATTERN.matcher(text);
    Assert.assertTrue(m.find());
    Assert.assertEquals(group2, m.group(2));
  }

}
