/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import static org.eclipse.scout.sdk.core.util.SourceState.isInCode;
import static org.eclipse.scout.sdk.core.util.SourceState.isInComment;
import static org.eclipse.scout.sdk.core.util.SourceState.isInString;
import static org.eclipse.scout.sdk.core.util.SourceState.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.log.MessageFormatter;
import org.eclipse.scout.sdk.core.util.SourceState.State;
import org.junit.jupiter.api.Test;

public class SourceStateTest {
  @Test
  public void testLineComment() {
    doTestLineComment(getLineCommentSrc('"'));
    doTestLineComment(getLineCommentSrc('\''));
  }

  private static char[] getLineCommentSrc(char stringDelim) {
    return MessageFormatter.arrayFormat("public class test {\n" +
        "  String a = {}inSt\\{}ringComment://no line comment{};\n" +
        "  // line comment\r\n" +
        "  int nextLine = 4;\n" +
        "}", stringDelim, stringDelim, stringDelim).message().toCharArray();
  }

  @Test
  public void testIsInCode() {
    doTestInCode(getLineCommentSrc('"'));
    doTestInCode(getLineCommentSrc('\''));
  }

  private static void doTestInCode(char[] src) {
    assertTrue(isInCode(src, 73));
    assertFalse(isInCode(src, 74)); // /
    assertFalse(isInCode(src, 75)); // /
    assertFalse(isInCode(src, 88));
    assertFalse(isInCode(src, 89)); // \r
    assertTrue(isInCode(src, 90)); // \n
    assertTrue(isInCode(src, 91));
    assertTrue(isInCode(src, 32));
    assertFalse(isInCode(src, 33));
    assertFalse(isInCode(src, 38));
    assertFalse(isInCode(src, 39));
    assertFalse(isInCode(src, 69));
    assertTrue(isInCode(src, 70));

    assertTrue(isInCode("a/b".toCharArray(), 1));
    assertTrue(isInCode("a / b".toCharArray(), 2));
    assertFalse(isInCode("a //".toCharArray(), 2));
    assertFalse(isInCode("a /*".toCharArray(), 2));
    assertFalse(isInCode("a \"".toCharArray(), 2));
    String simpleLineComment = "a //comment\ncode";
    assertFalse(isInCode(simpleLineComment.toCharArray(), 10)); // t
    assertTrue(isInCode(simpleLineComment.toCharArray(), 11)); // \n

    String simpleBlockComment = "a/*comment*/code";
    assertTrue(isInCode(simpleBlockComment.toCharArray(), 0));
    assertFalse(isInCode(simpleBlockComment.toCharArray(), 1));
    assertFalse(isInCode(simpleBlockComment.toCharArray(), 2));
    assertFalse(isInCode(simpleBlockComment.toCharArray(), 3));
    assertFalse(isInCode(simpleBlockComment.toCharArray(), 9));
    assertFalse(isInCode(simpleBlockComment.toCharArray(), 10)); // *
    assertFalse(isInCode(simpleBlockComment.toCharArray(), 11)); // /
    assertTrue(isInCode(simpleBlockComment.toCharArray(), 12)); // c

    assertTrue(isInCode("ab\"st\"cd".toCharArray(), 1));
    assertFalse(isInCode("ab\"st\"cd".toCharArray(), 2));
    assertFalse(isInCode("ab\"st\"cd".toCharArray(), 3));
    assertFalse(isInCode("ab\"st\"cd".toCharArray(), 5));
    assertTrue(isInCode("ab\"st\"cd".toCharArray(), 6));
  }

  private static void doTestLineComment(char[] src) {
    assertEquals(State.Default, parse(src, 0));
    assertEquals(State.Default, parse(src, 31));
    assertEquals(State.InString, parse(src, 33));
    assertEquals(State.EscapeString, parse(src, 38));
    assertEquals(State.InString, parse(src, 39));
    assertEquals(State.InString, parse(src, 56));
    assertEquals(State.Default, parse(src, 69));
    assertEquals(State.AboutToEnterComment, parse(src, 74));
    assertEquals(State.InLineComment, parse(src, 75));
    assertEquals(State.InLineComment, parse(src, 88));
    assertEquals(State.Default, parse(src, 90));
    assertEquals(State.Default, parse(src, src.length));
    assertEquals(State.Default, parse(new char[]{}, 0));
    assertEquals(State.Default, parse(new char[]{}, 1));

    assertFalse(isInComment(src, 73));
    assertTrue(isInComment(src, 74)); // /
    assertTrue(isInComment(src, 75)); // /
    assertTrue(isInComment(src, 88));
    assertTrue(isInComment(src, 89)); // \r
    assertFalse(isInComment(src, 90)); // \n
    assertFalse(isInComment(src, 91));

    assertFalse(isInComment("a/b".toCharArray(), 1));

    String simpleLineComment = "a//c\n";
    assertFalse(isInComment(simpleLineComment.toCharArray(), 0));
    assertTrue(isInComment(simpleLineComment.toCharArray(), 1));
    assertTrue(isInComment(simpleLineComment.toCharArray(), 2));
    assertTrue(isInComment(simpleLineComment.toCharArray(), 3));
    assertFalse(isInComment(simpleLineComment.toCharArray(), 4));

    String simpleBlockComment = "a/*comment*/code";
    assertTrue(isInComment(simpleBlockComment.toCharArray(), 1));
    assertTrue(isInComment(simpleBlockComment.toCharArray(), 2));
    assertTrue(isInComment(simpleBlockComment.toCharArray(), 10));
    assertTrue(isInComment(simpleBlockComment.toCharArray(), 11));
    assertFalse(isInComment(simpleBlockComment.toCharArray(), 12));

    assertFalse(isInString(src, 32));
    assertTrue(isInString(src, 33));
    assertTrue(isInString(src, 38));
    assertTrue(isInString(src, 39));
    assertTrue(isInString(src, 69));
    assertFalse(isInString(src, 70));
  }

  @Test
  public void testInStringWithLimitsJava() {
    char[] src = "\"abc\"".toCharArray();
    assertTrue(isInString(src, 0));
    assertTrue(isInString(src, src.length - 1));
    assertFalse(isInString(new char[]{}, src.length - 1));
  }

  @Test
  public void testInStringWithLimitsJs() {
    char[] src = "'abc'".toCharArray();
    assertTrue(isInString(src, 0));
    assertTrue(isInString(src, src.length - 1));
    assertFalse(isInString(new char[]{}, src.length - 1));
  }

  @Test
  public void testBlockComment() {
    doTestBlockComment(getBlockCommentSrc('"'));
    doTestBlockComment(getBlockCommentSrc('\''));
  }

  private static void doTestBlockComment(char[] src) {
    assertEquals(State.Default, parse(src, 0));
    assertEquals(State.Default, parse(src, 31));

    assertEquals(State.InString, parse(src, 33));
    assertEquals(State.EscapeString, parse(src, 38));
    assertEquals(State.InString, parse(src, 39));
    assertEquals(State.InString, parse(src, 56));
    assertEquals(State.InString, parse(src, 69));
    assertEquals(State.Default, parse(src, 75));
    assertEquals(State.AboutToEnterComment, parse(src, 77));
    assertEquals(State.InBlockComment, parse(src, 78));
    assertEquals(State.InBlockComment, parse(src, 88));
    assertEquals(State.AboutToExitBlockComment, parse(src, 94));
    assertEquals(State.Default, parse(src, 95));
    assertEquals(State.Default, parse(src, src.length));

    assertFalse(isInComment(src, 76));
    assertTrue(isInComment(src, 77)); // /
    assertTrue(isInComment(src, 78)); // *
    assertTrue(isInComment(src, 88));
    assertTrue(isInComment(src, 94)); // *
    assertTrue(isInComment(src, 95)); // /
    assertFalse(isInComment(src, 96));
  }

  private static char[] getBlockCommentSrc(char stringDelim) {
    return MessageFormatter.arrayFormat("public class test {\n" +
        "  String a = {}inSt\\{}ringComment:/*no block comment*/{};\n" +
        "  /* block comment */\n" + //75
        "  int nextLine = 4;\n" +
        "}", stringDelim, stringDelim, stringDelim).message().toCharArray();
  }

  @Test
  public void testOutOfBounds() {
    char[] src = "/*".toCharArray();
    assertEquals(State.AboutToEnterComment, parse(src, 0));
    assertEquals(State.InBlockComment, parse(src, 1));
    assertEquals(State.InBlockComment, parse(src, 100));
    assertEquals(State.Default, parse(src, -1));
  }

  @Test
  public void testJavaDoc() {
    char[] src = ("/****\n" +
        " * multi block\n" +
        " ****/\n" +
        "code\n" +
        "/****\n" +
        " * multi block\n" +
        " */x").toCharArray();

    assertEquals(State.AboutToEnterComment, parse(src, 0));
    assertEquals(State.InBlockComment, parse(src, 5));
    assertEquals(State.AboutToExitBlockComment, parse(src, 7));
    assertEquals(State.InBlockComment, parse(src, 8));
    assertEquals(State.InBlockComment, parse(src, 19));
    assertEquals(State.AboutToExitBlockComment, parse(src, 24));
    assertEquals(State.Default, parse(src, 26));
    assertEquals(State.Default, parse(src, 27));
    assertEquals(State.AboutToEnterComment, parse(src, 33));
    assertEquals(State.InBlockComment, parse(src, 34));
    assertEquals(State.AboutToExitBlockComment, parse(src, 40));
    assertEquals(State.InBlockComment, parse(src, 53));
    assertEquals(State.InBlockComment, parse(src, 54));
    assertEquals(State.AboutToExitBlockComment, parse(src, 55));
    assertEquals(State.Default, parse(src, 56));

    assertTrue(isInComment(src, 0)); // /
    assertTrue(isInComment(src, 1)); // *
    assertTrue(isInComment(src, 3)); // *
    assertTrue(isInComment(src, 15));
    assertTrue(isInComment(src, 25)); // *
    assertTrue(isInComment(src, 26)); // /
    assertFalse(isInComment(src, 30)); // code
    assertTrue(isInComment(src, 33)); // /
    assertTrue(isInComment(src, 34)); // *
    assertTrue(isInComment(src, 40));
    assertTrue(isInComment(src, 55)); // *
    assertTrue(isInComment(src, 56)); // *
    assertFalse(isInComment(src, 57)); // x
  }

  @Test
  public void testLineCommentRemovingBlockComment() {
    char[] src =
        ("// line comment\n" +
            "// /*\n" +
            "12343/12\n" +
            "// */\n").toCharArray();

    assertEquals(State.AboutToEnterComment, parse(src, 0));
    assertEquals(State.InLineComment, parse(src, 1));
    assertEquals(State.Default, parse(src, 15));
    assertEquals(State.AboutToEnterComment, parse(src, 16));
    assertEquals(State.InLineComment, parse(src, 17));
    assertEquals(State.Default, parse(src, 21));
    assertEquals(State.Default, parse(src, 23));
    assertEquals(State.AboutToEnterComment, parse(src, 31));
    assertEquals(State.InLineComment, parse(src, 32));
    assertEquals(State.Default, parse(src, 36));
  }
}
