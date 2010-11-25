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
package org.eclipse.scout.sdk.test.util;

import org.eclipse.scout.sdk.util.Regex;
import org.junit.Assert;
import org.junit.Test;

public class RegexTest {

  @Test
  public void testQuoteRegexSpecialCharacters_grouping() {
    Assert.assertEquals("\\(\\)", Regex.quoteRegexSpecialCharacters("()"));
    Assert.assertEquals("\\(test\\)\\{3,6\\}", Regex.quoteRegexSpecialCharacters("(test){3,6}"));
  }

  @Test
  public void testQuoteRegexSpecialCharacters_characterClass() {
    Assert.assertEquals("\\[\\]", Regex.quoteRegexSpecialCharacters("[]"));
    Assert.assertEquals("\\[a-z,A-Z&&\\\\p\\{Sc\\}\\]", Regex.quoteRegexSpecialCharacters("[a-z,A-Z&&\\p{Sc}]"));
  }

  @Test
  public void testQuoteRegexSpecialCharacters_quantifiers() {
    Assert.assertEquals("\\*", Regex.quoteRegexSpecialCharacters("*"));
    Assert.assertEquals("\\+", Regex.quoteRegexSpecialCharacters("+"));
    Assert.assertEquals("\\?", Regex.quoteRegexSpecialCharacters("?"));
    Assert.assertEquals("\\{\\}", Regex.quoteRegexSpecialCharacters("{}"));
  }

  @Test
  public void testQuoteRegexSpecialCharacters_others() {
    Assert.assertEquals("\\|", Regex.quoteRegexSpecialCharacters("|"));
    Assert.assertEquals("\\\\", Regex.quoteRegexSpecialCharacters("\\"));
    Assert.assertEquals("\\^", Regex.quoteRegexSpecialCharacters("^"));
    Assert.assertEquals("\\$", Regex.quoteRegexSpecialCharacters("$"));
  }

  @Test
  public void testRegexFieldDeclaration_validFieldDeclarations() {
    checkVariableDeclarationRightHandSide("int i = 5;", "5");
    checkVariableDeclarationRightHandSide("  int     i    =     5     ;", "5");
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

  private void checkVariableDeclarationRightHandSide(String fieldDeclaration, String expectedRightHandSide) {
    Assert.assertEquals(expectedRightHandSide, Regex.getFieldDeclarationRightHandSide(fieldDeclaration));
  }
}
