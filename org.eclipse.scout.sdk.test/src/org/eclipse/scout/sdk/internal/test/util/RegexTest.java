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

import org.eclipse.scout.sdk.util.Regex;
import org.junit.Assert;
import org.junit.Test;

public class RegexTest {

  @Test
  public void testRegexFieldDeclaration_validFieldDeclarations() {
    checkVariableDeclarationRightHandSide("int i = 5;", "5");
    checkVariableDeclarationRightHandSide("  int     i    =     5     ;", "5");
    checkVariableDeclarationRightHandSide("@Deprecated private int i = 5;", "5");
    checkVariableDeclarationRightHandSide("private int i = 5;", "5");
    checkVariableDeclarationRightHandSide("private static String foo = \"String\";", "\"String\"");
    checkVariableDeclarationRightHandSide("private final static String foo = \"test string with \\\" \";", "\"test string with \\\" \"");
    checkVariableDeclarationRightHandSide("private final static Map<String, Integer> foo = new HashMap<String, Integer>();", "new HashMap<String, Integer>()");
    checkVariableDeclarationRightHandSide("private final static volatile transient Map<String, Integer> foo = new HashMap<String, Integer>();", "new HashMap<String, Integer>()");
    checkVariableDeclarationRightHandSide("private String s;", null);
    checkVariableDeclarationRightHandSide("public static final String docOut=\"docOut\";  //TODO: [jbr] use inverse icon", "\"docOut\"");
    checkVariableDeclarationRightHandSide("public static final String docOut=\"docOut;\";  //TODO: [jbr] use inverse icon", "\"docOut;\"");
    checkVariableDeclarationRightHandSide("public static final String docOut=\"docOut;\";  /* TODO: \n[jbr] use inverse icon*/", "\"docOut;\"");
    checkVariableDeclarationRightHandSide("private String s = \"first \" + \" second part\";", "\"first \" + \" second part\"");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRegexFieldDeclaration_invalidFieldDeclaration() {
    checkVariableDeclarationRightHandSide("private int i j = 5;", "5");
  }

  @Test
  public void testMethodPresenterValue() {
    checkMethodPresenter("{ int a=0; return abcdefg.aaa; } ", "abcdefg.aaa");
    checkMethodPresenter("{ int a=0; return \"abcdefg;aaa\"; } ", "\"abcdefg;aaa\"");
    checkMethodPresenter("{ int a=0; return \"abcdefg.aaa\"; } ", "\"abcdefg.aaa\"");
    checkMethodPresenter("{ int a=0; return \"abcdefg.aaa; } ", null);
    checkMethodPresenter("{ int a=0; return abcdefg.aaa\"; } ", null);
    checkMethodPresenter("{ int a=0; return \"abcde\\\"fg.aaa\"; } ", "\"abcde\\\"fg.aaa\"");
    checkMethodPresenter("{ int a=0; return \"abcde\\\"jk\\\".aaa\"; } ", "\"abcde\\\"jk\\\".aaa\"");
    checkMethodPresenter("{ int a=0; return TEXTS.get(\"InvalidPhoneNumberMessageX\"); } ", "TEXTS.get(\"InvalidPhoneNumberMessageX\")");
    checkMethodPresenter("{ int a=0; return TEXTS.get(\"InvalidPhoneNumber;MessageX\"); } ", "TEXTS.get(\"InvalidPhoneNumber;MessageX\")");
  }

  private void checkMethodPresenter(String valToCheck, String expectedVal) {
    Matcher m = Regex.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE.matcher(valToCheck);
    boolean found = m.find();
    Assert.assertTrue(found || expectedVal == null);
    if (found) {
      Assert.assertEquals(expectedVal, m.group(1).trim());
    }
  }

  private void checkVariableDeclarationRightHandSide(String fieldDeclaration, String expectedRightHandSide) {
    Assert.assertEquals(expectedRightHandSide, Regex.getFieldDeclarationRightHandSide(fieldDeclaration));
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
