/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link LanguageTest}</h3>
 *
 * @since 7.0.0
 */
public class LanguageTest {
  @Test
  public void testParse() {
    assertSame(Language.LANGUAGE_DEFAULT, Language.parse("default").get());
    assertEquals(new Language(new Locale("test")), Language.parse("test").get());
    assertEquals(new Language(new Locale("de", "FR", "xx")), Language.parse("de_FR_xx").get());
    assertEquals(new Language(new Locale("de", "FR")), Language.parse("de_FR").get());
    assertEquals(new Language(new Locale("de")), Language.parse("de").get());
    assertFalse(Language.parse("").isPresent());
  }

  @Test
  public void testParseOrThrow() {
    assertEquals(new Language(new Locale("test")), Language.parseThrowingOnError("test"));
    assertThrows(IllegalArgumentException.class, () -> Language.parseThrowingOnError("_blub"));
  }

  @Test
  public void testLanguageOrder() {
    Language a = Language.parseThrowingOnError("a");
    Language b = Language.parseThrowingOnError("z");
    Language c = Language.LANGUAGE_DEFAULT;
    Language d = Language.parseThrowingOnError("de_CH_xx");
    Language[] langs = {a, b, c, d};
    Arrays.sort(langs);

    assertSame(c, langs[0]);
    assertSame(d, langs[1]);
    assertSame(a, langs[2]);
    assertSame(b, langs[3]);
  }

  @Test
  @SuppressWarnings("unlikely-arg-type")
  public void testLanguage() {
    Locale locale = new Locale("test");
    Language lang = new Language(locale);
    assertSame(locale, lang.locale());
    assertEquals("test", lang.dispalyName());
    assertEquals("default", Language.LANGUAGE_DEFAULT.dispalyName());
    assertEquals("test", lang.toString());
    assertEquals(locale.getDisplayName(Locale.US).hashCode(), lang.hashCode());
    assertFalse(lang.equals(""));
    assertFalse(lang.equals(null));
    assertTrue(lang.equals(lang));
    assertFalse(lang.equals(Language.LANGUAGE_DEFAULT));
  }
}
