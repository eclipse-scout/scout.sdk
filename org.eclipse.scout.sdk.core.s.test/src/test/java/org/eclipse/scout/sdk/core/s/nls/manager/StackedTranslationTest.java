/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls.manager;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.s.nls.TranslationTestsHelper.createStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.nls.Language;
import org.junit.jupiter.api.Test;

public class StackedTranslationTest {

  private static final String KEY_01 = "key1";
  private static final String KEY_02 = "key2";
  private static final String KEY_03 = "key3";
  private static final Language LANGUAGE_EN = Language.parseThrowingOnError("en");
  private static final Language LANGUAGE_DE = Language.parseThrowingOnError("de");
  private static final Language LANGUAGE_FR = Language.parseThrowingOnError("fr");
  private static final Language LANGUAGE_ES = Language.parseThrowingOnError("es");
  private static final String STORE_A_NAME = "x";
  private static final String STORE_B_NAME = "b";
  private static final String STORE_C_NAME = "c";

  @Test
  public void testPartialOverride() {
    var entry01 = createStackedTranslation(KEY_01);
    assertEquals(KEY_01, entry01.key());
    assertTrue(entry01.hasEditableStores());
    assertFalse(entry01.hasOnlyEditableStores());
    assertEquals("b,x", entry01.stores()
        .map(s -> s.service().type().elementName())
        .sorted()
        .collect(joining(",")));
    assertEquals(STORE_B_NAME, entry01.primaryEditableStore().orElseThrow().service().type().elementName());
    assertEquals(STORE_B_NAME, entry01.entry(Language.LANGUAGE_DEFAULT).orElseThrow().store().service().type().elementName());
    assertEquals(STORE_B_NAME, entry01.entry(LANGUAGE_EN).orElseThrow().store().service().type().elementName());
    assertEquals(STORE_B_NAME, entry01.entry(LANGUAGE_DE).orElseThrow().store().service().type().elementName());
    assertEquals(STORE_A_NAME, entry01.entry(LANGUAGE_FR).orElseThrow().store().service().type().elementName());
    assertEquals(STORE_B_NAME, entry01.entry(LANGUAGE_ES).orElseThrow().store().service().type().elementName());
    assertEquals("B_1_def", entry01.text(Language.LANGUAGE_DEFAULT).orElseThrow());
    assertEquals("B_1_de", entry01.text(LANGUAGE_DE).orElseThrow());
    assertEquals("B_1_es", entry01.text(LANGUAGE_ES).orElseThrow());
    assertEquals("A_1_fr", entry01.text(LANGUAGE_FR).orElseThrow());
  }

  @Test
  public void testOnlyReadOnly() {
    var entry02 = createStackedTranslation(KEY_02);
    assertEquals(KEY_02, entry02.key());
    assertFalse(entry02.hasEditableStores());
    assertFalse(entry02.hasOnlyEditableStores());
    assertEquals(STORE_A_NAME, entry02.stores()
        .map(s -> s.service().type().elementName())
        .sorted()
        .collect(joining(",")));
    assertTrue(entry02.primaryEditableStore().isEmpty());
    assertEquals(STORE_A_NAME, entry02.entry(Language.LANGUAGE_DEFAULT).orElseThrow().store().service().type().elementName());
    assertEquals(STORE_A_NAME, entry02.entry(LANGUAGE_EN).orElseThrow().store().service().type().elementName());
    assertEquals(STORE_A_NAME, entry02.entry(LANGUAGE_DE).orElseThrow().store().service().type().elementName());
    assertEquals(STORE_A_NAME, entry02.entry(LANGUAGE_FR).orElseThrow().store().service().type().elementName());
    assertTrue(entry02.entry(LANGUAGE_ES).isEmpty());
  }

  @Test
  public void testNoOverrideAllEditable() {
    var entry03 = createStackedTranslation(KEY_03);
    assertEquals(KEY_03, entry03.key());
    assertTrue(entry03.hasEditableStores());
    assertTrue(entry03.hasOnlyEditableStores());
    assertEquals("b,c", entry03.stores()
        .map(s -> s.service().type().elementName())
        .sorted()
        .collect(joining(",")));
    assertEquals(STORE_C_NAME, entry03.primaryEditableStore().orElseThrow().service().type().elementName());
    assertEquals(STORE_B_NAME, entry03.entry(Language.LANGUAGE_DEFAULT).orElseThrow().store().service().type().elementName());
    assertTrue(entry03.entry(LANGUAGE_EN).isEmpty());
    assertTrue(entry03.entry(LANGUAGE_DE).isEmpty());
    assertEquals(STORE_C_NAME, entry03.entry(LANGUAGE_FR).orElseThrow().store().service().type().elementName());
    assertEquals(STORE_B_NAME, entry03.entry(LANGUAGE_ES).orElseThrow().store().service().type().elementName());
  }

  protected static StackedTranslation createStackedTranslation(String key) {
    var storeA = createStore(STORE_A_NAME, 5000.0, getContentForA());
    when(storeA.isEditable()).thenReturn(false);
    var storeB = createStore(STORE_B_NAME, 4000.0, getContentForB());
    var storeC = createStore(STORE_C_NAME, 3000.0, getContentForC());

    var entries = Stream.of(storeA, storeB, storeC)
        .flatMap(s -> s.get(key).stream())
        .collect(toList());
    return new StackedTranslation(entries);
  }

  protected static Map<String, Map<Language, String>> getContentForA() {
    return Map.of(
        KEY_01, Map.of(Language.LANGUAGE_DEFAULT, "A_1_def", LANGUAGE_EN, "A_1_en", LANGUAGE_FR, "A_1_fr"),
        KEY_02, Map.of(Language.LANGUAGE_DEFAULT, "A_2_def", LANGUAGE_EN, "A_2_en", LANGUAGE_DE, "A_2_de", LANGUAGE_FR, "A_2_fr"));
  }

  protected static Map<String, Map<Language, String>> getContentForB() {
    return Map.of(
        KEY_01, Map.of(Language.LANGUAGE_DEFAULT, "B_1_def", LANGUAGE_EN, "B_1_en", LANGUAGE_DE, "B_1_de", LANGUAGE_ES, "B_1_es"),
        KEY_03, Map.of(Language.LANGUAGE_DEFAULT, "B_3_def", LANGUAGE_ES, "B_3_es"));
  }

  protected static Map<String, Map<Language, String>> getContentForC() {
    return Map.of(KEY_03, Map.of(LANGUAGE_FR, "C_3_fr"));
  }
}
