/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls;

import static org.eclipse.scout.sdk.core.s.nls.TranslationTestsHelper.createStore;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateDefaultText;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateKey;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateText;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.junit.jupiter.api.Test;

public class TranslationValidatorTest {

  @Test
  public void testValidateDefaultTextSimple() {
    assertEquals(TranslationValidator.OK, validateDefaultText("txt", null, null));
    assertEquals(TranslationValidator.DEFAULT_TRANSLATION_MISSING_ERROR, validateDefaultText((CharSequence) null, null, null));
  }

  @Test
  public void testValidateDefaultTextWithTranslation() {
    var translation1 = new Translation("key");
    translation1.putText(Language.LANGUAGE_DEFAULT, "def");
    assertEquals(TranslationValidator.OK, validateDefaultText(translation1, null, null));
    assertEquals(TranslationValidator.DEFAULT_TRANSLATION_MISSING_ERROR, validateDefaultText(new Translation("key"), null, null));
  }

  @Test
  public void testValidateDefaultTextWithManager() {
    var key1 = "key1";
    var entries01 = Map.of(key1, Map.of(Language.LANGUAGE_DEFAULT, "text_def"));
    var entries02 = Map.of(key1, Map.of(Language.LANGUAGE_DEFAULT, "text_def_orig"));
    var manager = TranslationManager.create(Paths.get(""), Stream.of(
        createStore("a", 100.00d, entries01),
        createStore("b", 200.00d, false, entries02)))
        .orElseThrow();
    assertEquals(TranslationValidator.TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING, validateDefaultText(null, key1, manager, manager.primaryEditableStore().orElseThrow()));
    assertEquals(TranslationValidator.DEFAULT_TRANSLATION_MISSING_ERROR, validateDefaultText(null, key1, manager, manager.allStores().skip(1).findAny().orElseThrow()));
  }

  @Test
  public void testValidateText() {
    var key1 = "key1";
    var entries01 = Map.of(key1, Map.of(Language.LANGUAGE_DEFAULT, "text_def"));
    var entries02 = Map.of(key1, Map.of(Language.LANGUAGE_DEFAULT, "text_def_orig"));
    var manager = TranslationManager.create(Paths.get(""), Stream.of(
        createStore("a", 100.00d, entries01),
        createStore("b", 200.00d, false, entries02)))
        .orElseThrow();
    var translation = manager.translation(key1).orElseThrow();

    assertEquals(TranslationValidator.OK, validateText("txt", null, Language.LANGUAGE_DEFAULT));
    assertEquals(TranslationValidator.OK, validateText(null, null, null));
    assertEquals(TranslationValidator.TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING, validateText("", translation, Language.LANGUAGE_DEFAULT));
    assertEquals(TranslationValidator.TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING, validateText(null, translation, Language.LANGUAGE_DEFAULT));
  }

  @Test
  public void testValidateKeySimple() {
    assertEquals(TranslationValidator.OK, validateKey("abc.def"));
    assertEquals(TranslationValidator.OK, validateKey("abc-def"));
    assertEquals(TranslationValidator.OK, validateKey("ABc-def"));

    assertEquals(TranslationValidator.KEY_EMPTY_ERROR, validateKey(""));
    assertEquals(TranslationValidator.KEY_EMPTY_ERROR, validateKey(" "));
    assertEquals(TranslationValidator.KEY_EMPTY_ERROR, validateKey("\t"));
    assertEquals(TranslationValidator.KEY_EMPTY_ERROR, validateKey(null));
    assertEquals(TranslationValidator.KEY_EMPTY_ERROR, validateKey("    "));

    assertEquals(TranslationValidator.KEY_INVALID_ERROR, validateKey(".abc"));
    assertEquals(TranslationValidator.KEY_INVALID_ERROR, validateKey("11abc"));
  }

  @Test
  public void testValidateKeyWithStore() {
    var key1 = "key1";
    var key2 = "key2";
    var en = Language.parseThrowingOnError("en");
    var entries01 = Map.of(key1, Map.of(Language.LANGUAGE_DEFAULT, "text_def", en, "text_en"));
    var entries02 = Map.of(key2, Map.of(Language.LANGUAGE_DEFAULT, "text_def_02"));
    var manager = TranslationManager.create(Paths.get(""), Stream.of(
        createStore("a", 100.00d, entries01),
        createStore("b", 200.00d, false, entries02)))
        .orElseThrow();

    var primaryStore = manager.primaryEditableStore().orElseThrow();
    assertEquals(TranslationValidator.KEY_ALREADY_EXISTS_ERROR, validateKey(null, primaryStore, key1));
    assertEquals(TranslationValidator.OK, validateKey(null, primaryStore, "abc"));
    assertEquals(TranslationValidator.OK, validateKey(manager, null, "abc"));
    assertEquals(TranslationValidator.KEY_ALREADY_EXISTS_ERROR, validateKey(manager, primaryStore, key1));
    assertEquals(TranslationValidator.OK, validateKey(manager, primaryStore, key1, Collections.singleton(key1)));
    assertEquals(TranslationValidator.KEY_ALREADY_EXISTS_ERROR, validateKey(manager, primaryStore, key1, Collections.singleton(key2)));

    assertEquals(TranslationValidator.KEY_OVERRIDES_OTHER_STORE_WARNING, validateKey(manager, primaryStore, key2));
    assertEquals(TranslationValidator.OK, validateKey(manager, primaryStore, "newKey"));
    assertEquals(TranslationValidator.KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING, validateKey(manager, manager.allStores().skip(1).findAny().orElseThrow(), key1));
  }
}
