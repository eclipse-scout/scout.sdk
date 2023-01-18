package org.eclipse.scout.sdk.core.java;

import static org.eclipse.scout.sdk.core.java.JavaUtils.fromStringLiteral;
import static org.eclipse.scout.sdk.core.java.JavaUtils.removeComments;
import static org.eclipse.scout.sdk.core.java.JavaUtils.toStringLiteral;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class JavaUtilsTest {

  @Test
  public void testRemoveComments() {
    assertEquals("  ", removeComments("  "));
    assertNull(removeComments(null));
    assertEquals("first\nsecond", removeComments("first\n/* comment \n multi line \n */second"));
    assertEquals("first\nsecond", removeComments("first\n/* comment \n * multi line \n */second"));
    assertEquals("first second", removeComments("first /* comment \n multi line \n */second"));
    assertEquals("first\n\nsecond", removeComments("first\n/* comment \n multi line \n */\nsecond"));
    assertEquals("first\n \nsecond", removeComments("first\n// comment single line \n \nsecond"));
    assertEquals("first\n second", removeComments("first\n// comment single line \n second"));
  }

  @Test
  public void testFromStringLiteral() {
    assertNull(fromStringLiteral(null));
    assertEquals("a", fromStringLiteral("a").toString());
    assertEquals("\"a\nb", fromStringLiteral("\"a\\nb").toString());
    assertEquals("aaa\"", fromStringLiteral("aaa\"").toString());
    assertEquals("", fromStringLiteral("\"\"").toString());
    assertEquals("a\"b", fromStringLiteral("\"a\\\"b\"").toString());
    assertEquals("a'b", fromStringLiteral("'a\\'b'").toString());
    assertEquals("a\rb", fromStringLiteral("\"a\\rb\"").toString());
    assertEquals("a\nb", fromStringLiteral("\"a\\nb\"").toString());
    assertEquals("a\bb", fromStringLiteral("\"a\\bb\"").toString());
    assertEquals("a\tb", fromStringLiteral("\"a\\tb\"").toString());
    assertEquals("a\fb", fromStringLiteral("\"a\\fb\"").toString());
    assertEquals("a\\b", fromStringLiteral("\"a\\\\b\"").toString());
    assertEquals("a\0b", fromStringLiteral("\"a\\0b\"").toString());
    assertEquals("a\1b", fromStringLiteral("\"a\\1b\"").toString());
    assertEquals("a\2b", fromStringLiteral("\"a\\2b\"").toString());
    assertEquals("a\3b", fromStringLiteral("\"a\\3b\"").toString());
    assertEquals("a\4b", fromStringLiteral("\"a\\4b\"").toString());
    assertEquals("a\5b", fromStringLiteral("\"a\\5b\"").toString());
    assertEquals("a\6b", fromStringLiteral("\"a\\6b\"").toString());
    assertEquals("a\7b", fromStringLiteral("\"a\\7b\"").toString());
    assertEquals("aäb", fromStringLiteral("\"a\\u00E4b\"").toString());
    assertEquals("字", fromStringLiteral("\"字\"").toString());
  }

  @Test
  public void testToStringLiteral() {
    CharSequence in = "teststring \na\"";
    assertNull(toStringLiteral(null));
    assertEquals("\"a\\nb\"", toStringLiteral("a\nb").toString());
    assertEquals("\"a\\bb\"", toStringLiteral("a\bb").toString());
    assertEquals("\"a\\tb\"", toStringLiteral("a\tb").toString());
    assertEquals("\"a\\fb\"", toStringLiteral("a\fb").toString());
    assertEquals("\"a\\rb\"", toStringLiteral("a\rb").toString());
    assertEquals("\"a\\\\b\"", toStringLiteral("a\\b").toString());
    assertEquals("\"a\\u0000b\"", toStringLiteral("a\0b").toString());
    assertEquals("\"a\\\"b\"", toStringLiteral("a\"b").toString());
    assertEquals("a\\\"b", toStringLiteral("a\"b", "\"", false).toString());
    assertEquals("'a\"b'", toStringLiteral("a\"b", "'", true).toString());
    assertEquals("'a\\'b'", toStringLiteral("a'b", "'", true).toString());
    assertEquals("\"字\"", toStringLiteral("字").toString());
    assertEquals(in, fromStringLiteral(toStringLiteral(in)).toString());
  }
}
