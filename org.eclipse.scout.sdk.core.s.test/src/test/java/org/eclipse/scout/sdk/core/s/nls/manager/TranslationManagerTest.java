/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls.manager;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension.testingManager;
import static org.eclipse.scout.sdk.core.s.nls.TranslationTestsHelper.createStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.nls.IEditableTranslationStore;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.ITranslationImportInfo;
import org.eclipse.scout.sdk.core.s.nls.ITranslationManagerListener;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TextProviderService;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link TranslationManagerTest}</h3>
 *
 * @since 7.0.0
 */
@ExtendWith(TranslationStoreSupplierExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class TranslationManagerTest {

  @Test
  public void testManagerRead(TestingEnvironment env) {
    var manager = testingManager(env);
    assertEquals(3, manager.allTranslationsWithPrefix("k").count());
    assertEquals(1, manager.allTranslationsWithPrefix("key1").count());
    assertEquals("key10", manager.generateNewKey("key1"));
    assertEquals("somethingNew", manager.generateNewKey("something,New "));
    assertTrue(manager.isEditable());
    assertEquals("1_en", manager.translation("key1").orElseThrow().text(Language.parseThrowingOnError("en_US")).orElseThrow());
    assertFalse(manager.translation("dddd").isPresent());
    assertFalse(manager.containsKey("dddd"));
    assertTrue(manager.containsKey("key1"));
    assertEquals(1, manager.allEditableStores().count());
    assertTrue(manager.primaryEditableStore().isPresent());
    assertEquals(3, manager.allTranslations().count());

    assertFalse(manager.isDirty());
    assertEquals(2, manager.allStores().count());
    assertEquals(3, manager.allLanguages().count());
    assertNotNull(manager.toString());
  }

  @Test
  public void testGenerateNewKey(TestingEnvironment env) {
    var manager = testingManager(env);
    assertEquals("", manager.generateNewKey(null));
    assertEquals("", manager.generateNewKey("  \t\n\r "));
    assertEquals("", manager.generateNewKey("  ??? "));
    assertEquals("AbcTest", manager.generateNewKey("  \t\n\r,/(*0abc test- "));
    assertEquals("Key10", manager.generateNewKey("*key 1"));
    assertEquals("key10", manager.generateNewKey(TranslationStoreSupplierExtension.TRANSLATION_KEY_1));
  }

  @Test
  public void testMergeTranslation(TestingEnvironment env) {
    var manager = testingManager(env);

    var key = "added";
    var en = Language.parseThrowingOnError("en");
    var es = Language.parseThrowingOnError("es");

    var defaultAfterInsert = "default1";
    var enAfterInsert = "en1";
    var esAfterInsert = "es1";

    var toAdd = new Translation(key);
    toAdd.putText(Language.LANGUAGE_DEFAULT, defaultAfterInsert);
    toAdd.putText(en, enAfterInsert);
    toAdd.putText(es, esAfterInsert);
    manager.mergeTranslation(toAdd, null);
    assertEquals(1, manager.allTranslationsWithPrefix("add").count());
    assertEquals(defaultAfterInsert, manager.translation(key).orElseThrow().text(Language.LANGUAGE_DEFAULT).orElseThrow());
    assertEquals(enAfterInsert, manager.translation(key).orElseThrow().text(en).orElseThrow());
    assertEquals(esAfterInsert, manager.translation(key).orElseThrow().text(es).orElseThrow());

    var defaultAfterModify = "default2";
    var esAfterModify = "es2";
    var toModify = new Translation(key);
    toModify.putText(Language.LANGUAGE_DEFAULT, defaultAfterModify);
    toModify.putText(es, esAfterModify);
    manager.mergeTranslation(toModify, null);
    assertEquals(defaultAfterModify, manager.translation(key).orElseThrow().text(Language.LANGUAGE_DEFAULT).orElseThrow());
    assertEquals(enAfterInsert, manager.translation(key).orElseThrow().text(en).orElseThrow());
    assertEquals(esAfterModify, manager.translation(key).orElseThrow().text(es).orElseThrow());
  }

  @Test
  public void testAddNewLanguage(TestingEnvironment env) {
    var manager = testingManager(env);
    var newStore = createSimpleStoreMock();
    var deCh = Language.parseThrowingOnError("de_CH");
    manager.addNewLanguage(deCh, manager.primaryEditableStore().orElseThrow());
    manager.flush(env, new NullProgress());

    assertThrows(IllegalArgumentException.class, () -> manager.addNewLanguage(deCh, manager.allStores().filter(s -> !s.isEditable()).findAny().orElseThrow()));
    assertThrows(IllegalArgumentException.class, () -> manager.addNewLanguage(null, manager.primaryEditableStore().orElseThrow()));
    assertThrows(IllegalArgumentException.class, () -> manager.addNewLanguage(deCh, newStore));
  }

  @Test
  public void testAutoCreateNewLanguage(TestingEnvironment env) {
    var manager = testingManager(env);
    List<TranslationManagerEvent> eventProtocol = new ArrayList<>();
    manager.addListener(events -> events.forEach(eventProtocol::add));

    var update = new Translation(TranslationStoreSupplierExtension.TRANSLATION_KEY_2);
    var newLang = Language.parseThrowingOnError("ru_DE");
    update.putText(Language.LANGUAGE_DEFAULT, "default");
    update.putText(newLang, "ru-de");
    manager.setTranslation(update);

    var newKey = "newKeyWithExtraLang";
    var add = new Translation(newKey);
    var addLang = Language.parseThrowingOnError("tr_DE");
    add.putText(Language.LANGUAGE_DEFAULT, "default");
    add.putText(addLang, "tr-de");
    manager.setTranslation(add);

    assertTrue(manager.primaryEditableStore().orElseThrow().containsLanguage(newLang));
    assertTrue(manager.primaryEditableStore().orElseThrow().containsLanguage(addLang));

    assertEquals(4, eventProtocol.size());
    var newLangEvents = eventProtocol.stream().filter(e -> e.type() == TranslationManagerEvent.TYPE_NEW_LANGUAGE).collect(toList());
    assertEquals(2, newLangEvents.size());
  }

  @Test
  public void testSetTranslation(TestingEnvironment env) {
    var manager = testingManager(env);
    assertFalse(manager.isDirty());
    var t = new Translation("newKey");
    t.putText(Language.LANGUAGE_DEFAULT, "def");
    manager.setTranslation(t, null);
    assertTrue(manager.isDirty());
    var t2 = new Translation("newKey2");
    t2.putText(Language.LANGUAGE_DEFAULT, "def2");
    manager.setTranslation(t2);

    ITranslation existing = new Translation("key2");
    t2.putText(Language.LANGUAGE_DEFAULT, "def2");

    var newStore = createSimpleStoreMock();

    assertThrows(IllegalArgumentException.class, () -> manager.setTranslation(null));
    assertThrows(IllegalArgumentException.class, () -> manager.setTranslation(new Translation("key")));
    assertThrows(IllegalArgumentException.class, () -> manager.setTranslation(new Translation("")));
    assertThrows(IllegalArgumentException.class, () -> manager.setTranslation(existing));
    assertThrows(IllegalArgumentException.class, () -> manager.setTranslation(t2, newStore));

    assertEquals(5, manager.allTranslations().count());
  }

  @Test
  public void testSetTranslationToStore() {
    var key = "key1";
    var origVal = "base1";
    var store1 = createStore("override", 100.0, emptyMap());
    var store2 = createStore("base", 200.0, Map.of(key, Map.of(Language.LANGUAGE_DEFAULT, origVal)));
    var manager = new TranslationManager(Stream.of(store1, store2));

    assertEquals(0, store1.size());
    assertEquals(origVal, manager.translation(key).orElseThrow().text(Language.LANGUAGE_DEFAULT).orElseThrow());

    var newTranslation = new Translation(key);
    var newContent = "1_def_added";
    newTranslation.putText(Language.LANGUAGE_DEFAULT, newContent);
    var newStack = manager.setTranslationToStore(newTranslation, store1);

    assertEquals(2, newStack.stores().count());
    assertEquals(1, store1.size()); // ensure new text is added to store1 even it already exists in store2 (and could be updated there)
    assertEquals(newContent, newStack.text(Language.LANGUAGE_DEFAULT).orElseThrow());
    assertEquals(newContent, manager.translation(key).orElseThrow().text(Language.LANGUAGE_DEFAULT).orElseThrow());
  }

  @Test
  public void testRemoveTranslation(TestingEnvironment env) {
    var store1 = createStore("a", 100.0, "key1", "key2", "key3", "key4");
    var store2 = createStore("b", 200.0, "key2", "key3", "key5");
    var writeableManager = new TranslationManager(Stream.of(store1, store2));

    writeableManager.removeTranslations(null);
    writeableManager.removeTranslations(Stream.of("key1", "", "key2", null, "key5", "not-existing"));
    assertEquals("key3,key4", writeableManager.allTranslations().map(ITranslation::key).sorted().collect(joining(",")));

    var managerWithReadOnly = testingManager(env);
    assertThrows(IllegalArgumentException.class, () -> managerWithReadOnly.removeTranslations(Stream.of("key1")));
  }

  @Test
  public void testUpdateTranslation(TestingEnvironment env) {
    var manager = testingManager(env);
    var t = new Translation("key1");
    t.putText(Language.LANGUAGE_DEFAULT, "updated");

    manager.setTranslation(t);
    assertEquals("updated", manager.translation("key1").orElseThrow().text(Language.LANGUAGE_DEFAULT).orElseThrow());
  }

  @Test
  public void testSetTranslationWhenKeyExists() {
    var store1 = createStore("a", 100.0, "key1", "key2", "key3", "key4");
    var store2 = createStore("b", 200.0, "key2", "key3", "key5");
    var writeableManager = new TranslationManager(Stream.of(store1, store2));
    var t = new Translation("key5");
    t.putText(Language.LANGUAGE_DEFAULT, "updated");
    writeableManager.setTranslation(t);
    assertEquals("updated", writeableManager.translation("key5").orElseThrow().text(Language.LANGUAGE_DEFAULT).orElseThrow());
  }

  @Test
  public void testUpdateTranslationHavingReadOnlyTexts() {
    var key = "key";
    var en = Language.parseThrowingOnError("en");
    var es = Language.parseThrowingOnError("es");
    var de = Language.parseThrowingOnError("de");
    var it = Language.parseThrowingOnError("it");
    var entries01 = Map.of(key, Map.of(Language.LANGUAGE_DEFAULT, "a_def", en, "a_en", es, "a_es"));
    var entries02 = Map.of(key, Map.of(Language.LANGUAGE_DEFAULT, "b_def", es, "b_es"));
    var entries03 = Map.of(key, Map.of(de, "c_de"));
    var storeA = createStore("a", 300.00d, false, entries01);
    var storeB = createStore("b", 200.00d, entries02);
    var storeC = createStore("c", 100.00d, entries03);
    var manager = new TranslationManager(Stream.of(storeA, storeB, storeC));

    var newEntry = new Translation(key);
    newEntry.putText(Language.LANGUAGE_DEFAULT, "b_def"); // do not change
    newEntry.putText(en, "changed_en"); // modify text defined in read-only store: should be added to primary store (creates override)
    newEntry.putText(es, "changed_es"); // change entry that exists in several stores. only the top store having that entry must be modified
    newEntry.putText(de, null); // remove the language
    newEntry.putText(it, "added_it"); // add new language

    var updatedEntry = manager.setTranslation(newEntry);
    assertEquals(Map.of(Language.LANGUAGE_DEFAULT, "b_def", en, "changed_en", es, "changed_es", it, "added_it"), updatedEntry.texts());

    // read-only store must not be touched
    assertEquals(entries01.get(key), storeA.get(key).orElseThrow().texts());

    // only contains the changes of the own languages
    assertEquals(Map.of(Language.LANGUAGE_DEFAULT, "b_def", es, "changed_es"), storeB.get(key).orElseThrow().texts());

    // gets all changes of own languages and new ones, "c_de" is removed
    assertEquals(Map.of(en, "changed_en", it, "added_it"), storeC.get(key).orElseThrow().texts());
  }

  @Test
  public void testChangeKey() {
    var key1 = "key1";
    var key2 = "key2";
    var entries01 = Map.of(key1, Map.of(Language.LANGUAGE_DEFAULT, "text01"), key2, Map.of(Language.LANGUAGE_DEFAULT, "text03"));
    var entries02 = Map.of(key2, Map.of(Language.LANGUAGE_DEFAULT, "text02"));
    var manager = new TranslationManager(Stream.of(
        createStore("a", 200.00d, entries02),
        createStore("b", 100.00d, entries01)));

    // newKey is invalid
    assertThrows(IllegalArgumentException.class, () -> manager.changeKey(key1, null));

    // newKey already exists
    assertThrows(IllegalArgumentException.class, () -> manager.changeKey(key1, key2));
  }

  @Test
  public void testChangeKeyToExisting() {
    var key1 = "key1";
    var key2 = "key2";
    var en = Language.parseThrowingOnError("en");
    var entries01 = Map.of(key1, Map.of(Language.LANGUAGE_DEFAULT, "text_def", en, "text_en"));
    var entries02 = Map.of(key2, Map.of(Language.LANGUAGE_DEFAULT, "text_def_02"));
    var manager = new TranslationManager(Stream.of(
        createStore("a", 100.00d, entries02),
        createStore("b", 200.00d, false, entries01)));
    manager.changeKey(key2, key1);
    assertTrue(manager.translation(key2).isEmpty()); // old key does not longer exist

    // existing read-only entry and renamed one have been merged to one manager
    assertEquals(Map.of(Language.LANGUAGE_DEFAULT, "text_def_02", en, "text_en"), manager.translation(key1).orElseThrow().texts());
  }

  @Test
  public void testChangeKeyWithReadOnlyStores(TestingEnvironment env) {
    var managerWithReadOnly = testingManager(env);
    var key = TranslationStoreSupplierExtension.TRANSLATION_KEY_1;

    // cannot change because there are read-only stores holding that key
    assertThrows(IllegalArgumentException.class, () -> managerWithReadOnly.changeKey(key, "whatever"));

    // change to itself: nothing is changed
    managerWithReadOnly.changeKey(key, key);
    assertEquals(TranslationStoreSupplierExtension.KEY_1_VAL_DEFAULT, managerWithReadOnly.translation(key).orElseThrow().text(Language.LANGUAGE_DEFAULT).orElseThrow());

    // the oldKey cannot be found: nothing is changed
    managerWithReadOnly.changeKey(null, "aaa");
  }

  @Test
  @ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class), flushToDisk = true)
  public void testBatchChange(TestingEnvironment env) {
    var counter = new AtomicInteger();
    var manager = testingManager(env);
    var l = (ITranslationManagerListener) s -> counter.incrementAndGet();
    manager.addListener(l);
    manager.setChanging(true);
    var textPrefix = "def杉矶";
    try {
      for (var i = 0; i < 10; i++) {
        var t = new Translation("newKey" + i);
        t.putText(Language.LANGUAGE_DEFAULT, textPrefix + i);
        manager.setTranslation(t);
      }
    }
    finally {
      manager.setChanging(false);
    }
    manager.removeListener(l);
    assertEquals(1, counter.intValue());
    manager.flush(env, new NullProgress());
    manager.reload(env, new NullProgress());
    assertEquals(13, manager.allTranslations().count());
    assertEquals(textPrefix + '2', manager.translation("newKey2").orElseThrow().text(Language.LANGUAGE_DEFAULT).orElseThrow());
  }

  @Test
  public void testKeyChangeOfOverriddenTranslation() {
    var key = "testKey";
    var newKey = "newKey";
    var entries01 = Map.of(key, Map.of(Language.LANGUAGE_DEFAULT, "text01"));
    var entries02 = Map.of(key, Map.of(Language.LANGUAGE_DEFAULT, "text02"));
    var manager = new TranslationManager(
        Stream.of(
            createStore("a", 200.00d, entries02),
            createStore("b", 100.00d, entries01)));

    assertEquals(1, manager.allTranslations().count());
    assertEquals("text01", manager.translation(key).orElseThrow().texts().get(Language.LANGUAGE_DEFAULT));

    List<Integer> eventTypes = new ArrayList<>(); // remember each event type that happens
    manager.addListener(events -> events.forEach(e -> eventTypes.add(e.type())));
    manager.changeKey(key, newKey);

    assertEquals(1, manager.allTranslations().count());
    assertTrue(manager.translation(key).isEmpty());
    assertEquals("text01", manager.translation(newKey).orElseThrow().texts().get(Language.LANGUAGE_DEFAULT));
    assertEquals(singletonList(TranslationManagerEvent.TYPE_KEY_CHANGED), eventTypes);
  }

  @Test
  public void testImportNoData() {
    var firstStore = createStore("a", 100.00d, emptyMap());
    var manager = new TranslationManager(Stream.of(firstStore));
    var keyColumnName = "Key";

    // only header row
    var data1 = singletonList(asList(Language.LANGUAGE_DEFAULT.displayName(), "it", keyColumnName));
    var info1 = manager.importTranslations(data1, keyColumnName, null);
    Assertions.assertEquals(ITranslationImportInfo.NO_DATA, info1.result());

    // no rows
    var info2 = manager.importTranslations(emptyList(), keyColumnName, null);
    assertEquals(ITranslationImportInfo.NO_DATA, info2.result());

    // header row with empty columns
    var emptyHeaderCell = "  ";
    var data3 = asList(
        asList(Language.LANGUAGE_DEFAULT.displayName(), emptyHeaderCell, keyColumnName),
        asList("def", "esp", "xx"));

    var info3 = manager.importTranslations(data3, keyColumnName, null);
    Map<Integer, String> expectedIgnoreColumns = new HashMap<>();
    expectedIgnoreColumns.put(1, emptyHeaderCell);
    assertEquals(expectedIgnoreColumns, info3.ignoredColumns());

    // no valid rows
    var data4 = asList(
        asList(Language.LANGUAGE_DEFAULT.displayName(), "es", keyColumnName),
        asList("", "esp", "xx"));
    var info4 = manager.importTranslations(data4, keyColumnName, null);
    assertEquals(ITranslationImportInfo.NO_DATA, info4.result());
  }

  @Test
  public void testImportIgnoringEmptyCells() {
    var store = createStore("a", 100.00d, emptyMap());
    var manager = new TranslationManager(Stream.of(store));
    var keyColumnName = "Key";
    var key1 = "k1";
    var key2 = "k2";
    var key3 = "k3";
    var data = asList(
        asList(Language.LANGUAGE_DEFAULT.displayName(), "es", keyColumnName),
        asList("def1", "", key1),
        asList("def2", null, key2),
        asList("def3", "content", key3));
    var info = manager.importTranslations(data, keyColumnName, null);
    assertEquals(3, info.result());
    var es = Language.parseThrowingOnError("es");
    assertFalse(store.get(key1, es).isPresent());
    assertFalse(store.get(key2, es).isPresent());
    assertTrue(store.get(key3, es).isPresent());
  }

  @Test
  public void testImportWithIncompleteData() {
    var store = createStore("a", 100.00d, emptyMap());
    var manager = new TranslationManager(Stream.of(store));
    var keyColumnName = "Key";
    var validRowKey = "key02";
    List<List<String>> data = asList(
        asList("de", keyColumnName, Language.LANGUAGE_DEFAULT.displayName(), "fr"),
        emptyList(), // no columns at all
        singletonList("d1"), // no column for key
        asList("d2", "key01"), // no column for default lang
        asList("d3", validRowKey, "def")); // no column for language fr
    var info = manager.importTranslations(data, keyColumnName, null);

    assertEquals(1, info.result());
    assertEquals(1, info.importedTranslations().size());
    assertTrue(info.importedTranslations().containsKey(validRowKey));
    assertEquals(asList(2, 3), info.invalidRowIndices());
  }

  @Test
  public void testImportEmptyColumn() {
    var store = createStore("a", 100.00d, emptyMap());
    var manager = new TranslationManager(Stream.of(store));
    var keyColumnName = "Key";
    List<List<String>> data = asList(
        asList(keyColumnName, " ", "default", "en"),
        emptyList(), // empty row
        asList("k0", "\t", "en0"),
        asList("k1", "", "en1"),
        asList("k2", " ", "en2")); // no column for language fr
    var info = manager.importTranslations(data, keyColumnName, null);
    assertEquals(3, info.result());
    assertEquals(3, info.importedTranslations().size());
    assertTrue(info.ignoredColumns().isEmpty());
    assertTrue(info.invalidRowIndices().isEmpty());
    assertTrue(info.duplicateKeys().isEmpty());
  }

  @Test
  public void testImport() {
    var firstStore = createStore("a", 100.00d, emptyMap());
    var secondStore = createStore("b", 200.00d, emptyMap());
    var manager = new TranslationManager(Stream.of(firstStore, secondStore));
    var keyColumnName = "asdf";
    var key01 = "key01";
    var key02 = "key02???**";
    var key03 = "key03";
    var key04 = "key04";

    var data1 = asList(
        asList(Language.LANGUAGE_DEFAULT.displayName(), "es", keyColumnName),
        asList("def", "esp", key01));
    var info1 = manager.importTranslations(data1, keyColumnName, null);
    assertEquals(1, info1.result());
    assertEquals(1, info1.importedTranslations().size());
    assertTrue(info1.importedTranslations().containsKey(key01));
    assertEquals(0, info1.defaultLanguageColumnIndex());
    assertEquals(2, info1.keyColumnIndex());
    assertTrue(info1.ignoredColumns().isEmpty());
    assertTrue(info1.duplicateKeys().isEmpty());
    assertTrue(info1.invalidRowIndices().isEmpty());
    assertEquals(1, firstStore.keys().count());
    assertTrue(firstStore.containsKey(key01));
    assertEquals(2, firstStore.languages().count());

    var invalidColumnName = "_invalid_lang_";
    var newKey01DefaultText = "def1";
    var newKey04DefaultText = "def4_4";
    var data2 = asList(
        asList(Language.LANGUAGE_DEFAULT.displayName(), "fr", keyColumnName, invalidColumnName),
        asList(newKey01DefaultText, "fr1", key01, "xx"), // update existing
        asList("def2", "fr1", key02, "xx"), // invalid row by invalid key
        asList("def0", "fr1", "", "xx"), // invalid row by missing key
        asList("", "fr1", key03, "xx"), // invalid row by missing default lang
        asList("def4", "fr2", key04, "yy"), // new row (ignored duplicate)
        asList(newKey04DefaultText, "fr2_2", key04, "yy")); // new row (winning duplicate)
    var info2 = manager.importTranslations(data2, keyColumnName, secondStore);
    assertEquals(2, info2.result());
    assertEquals(2, info2.importedTranslations().size());
    assertTrue(info2.importedTranslations().containsKey(key01));
    assertTrue(info2.importedTranslations().containsKey(key04));
    assertEquals(0, info2.defaultLanguageColumnIndex());
    assertEquals(2, info2.keyColumnIndex());
    Map<Integer, String> expectedIgnoreColumns = new HashMap<>();
    expectedIgnoreColumns.put(3, invalidColumnName);
    assertEquals(expectedIgnoreColumns, info2.ignoredColumns());
    assertEquals(singleton(key04), info2.duplicateKeys());
    assertEquals(asList(2, 3, 4), info2.invalidRowIndices());
    assertEquals(1, firstStore.keys().count());
    assertTrue(firstStore.containsKey(key01));
    assertEquals(newKey01DefaultText, firstStore.get(key01, Language.LANGUAGE_DEFAULT).orElseThrow());
    assertEquals(2, firstStore.languages().count());
    assertEquals(2, secondStore.keys().count());
    assertTrue(secondStore.containsKey(key04));
    assertEquals(newKey04DefaultText, secondStore.get(key04, Language.LANGUAGE_DEFAULT).orElseThrow());
    assertEquals(2, secondStore.languages().count());
  }

  @Test
  public void testImportNoDefaultLangColumn() {
    var firstStore = createStore("a", 100.00d, emptyMap());
    var secondStore = createStore("b", 200.00d, emptyMap());
    var manager = new TranslationManager(Stream.of(firstStore, secondStore));
    var keyColumnName = "key";
    var data = asList(
        asList("xx", "es", keyColumnName),
        asList("def", "esp", "yy"));
    var info1 = manager.importTranslations(data, keyColumnName, null);
    assertEquals(ITranslationImportInfo.NO_KEY_OR_DEFAULT_LANG_COLUMN, info1.result());
    assertEquals(0, info1.importedTranslations().size());
    assertEquals(-1, info1.defaultLanguageColumnIndex());
    assertEquals(-1, info1.keyColumnIndex());
    assertTrue(info1.ignoredColumns().isEmpty());
    assertTrue(info1.duplicateKeys().isEmpty());
    assertTrue(info1.invalidRowIndices().isEmpty());
  }

  @Test
  public void testContentEquals() {
    var firstStore = createStore("a", 100.00d, Map.of("k1", Map.of(Language.LANGUAGE_DEFAULT, "a_k1")));
    var secondStore = createStore("b", 200.00d, Map.of("k2", Map.of(Language.LANGUAGE_DEFAULT, "b_k2")));
    var thirdStore = createStore("c", 300.00d, Map.of("k1", Map.of(Language.LANGUAGE_DEFAULT, "c_k1")));

    var manager1 = new TranslationManager(Stream.of(firstStore, secondStore));
    var manager2 = new TranslationManager(Stream.of(firstStore, secondStore, thirdStore));
    var manager3 = new TranslationManager(Stream.of(firstStore, secondStore));
    var manager4 = new TranslationManager(Stream.of(firstStore, thirdStore));

    assertTrue(manager1.contentEquals(manager1));
    assertFalse(manager1.contentEquals(null));
    assertTrue(manager1.contentEquals(manager3));
    assertFalse(manager1.contentEquals(manager2));
    assertFalse(manager1.contentEquals(manager4));
  }

  @Test
  public void testImportNoKeyColumn() {
    var firstStore = createStore("a", 100.00d, emptyMap());
    var secondStore = createStore("b", 200.00d, emptyMap());
    var manager = new TranslationManager(Stream.of(firstStore, secondStore));

    var data = asList(
        asList("xx", "es", Language.LANGUAGE_DEFAULT.displayName()),
        asList("def", "esp", "yy"));
    var info1 = manager.importTranslations(data, "key", null);
    assertEquals(ITranslationImportInfo.NO_KEY_OR_DEFAULT_LANG_COLUMN, info1.result());
    assertEquals(0, info1.importedTranslations().size());
    assertEquals(-1, info1.defaultLanguageColumnIndex());
    assertEquals(-1, info1.keyColumnIndex());
    assertTrue(info1.ignoredColumns().isEmpty());
    assertTrue(info1.duplicateKeys().isEmpty());
    assertTrue(info1.invalidRowIndices().isEmpty());
  }

  /**
   * Tests that the inheritance behaves the same as in the Scout runtime:<br>
   * The first text-provider-service that has an entry for a key wins. Even if another one would have a better language
   * match.
   */
  @Test
  public void testInheritance() {
    var key01 = "key01";
    var key02 = "key02";
    var key03 = "key03";
    var langDe = Language.parseThrowingOnError("de");
    var langEs = Language.parseThrowingOnError("es");
    var entries01 = Map.of(
        key01, Map.of(Language.LANGUAGE_DEFAULT, "01_k1_en", langEs, "01_k1_es", langDe, "01_k1_de"),
        key02, Map.of(Language.LANGUAGE_DEFAULT, "01_k2_en", langEs, "01_k2_es", langDe, "01_k2_de"),
        key03, Map.of(Language.LANGUAGE_DEFAULT, "01_k3_en", langEs, "01_k3_es", langDe, "01_k3_de"));
    var entries02 = Map.of(
        key01, Map.of(Language.LANGUAGE_DEFAULT, "02_k1_en", langDe, "02_k1_de"),
        key03, Map.of(Language.LANGUAGE_DEFAULT, "02_k3_en", langDe, "02_k3_de"));
    var entries03 = Map.of(
        key01, Map.of(Language.LANGUAGE_DEFAULT, "03_k1_en"));

    var manager = new TranslationManager(
        Stream.of(
            createStore("a", 200.00d, entries02),
            createStore("b", 300.00d, entries01),
            createStore("c", 100.00d, entries03)));

    var k01Result = manager.allTranslations().filter(e -> key01.equals(e.key())).findAny().orElseThrow();
    var k02Result = manager.allTranslations().filter(e -> key02.equals(e.key())).findAny().orElseThrow();
    var k03Result = manager.allTranslations().filter(e -> key03.equals(e.key())).findAny().orElseThrow();

    assertEquals(3, k01Result.texts().size());
    assertEquals("02_k1_de", k01Result.texts().get(langDe));
    assertEquals("01_k1_es", k01Result.texts().get(langEs));
    assertEquals("03_k1_en", k01Result.texts().get(Language.LANGUAGE_DEFAULT));

    assertEquals(3, k02Result.texts().size());
    assertEquals("01_k2_en", k02Result.texts().get(Language.LANGUAGE_DEFAULT));
    assertEquals("01_k2_es", k02Result.texts().get(langEs));
    assertEquals("01_k2_de", k02Result.texts().get(langDe));

    assertEquals(3, k03Result.texts().size());
    assertEquals("02_k3_en", k03Result.texts().get(Language.LANGUAGE_DEFAULT));
    assertEquals("01_k3_es", k03Result.texts().get(langEs));
    assertEquals("02_k3_de", k03Result.texts().get(langDe));
  }

  private static ITranslationStore createSimpleStoreMock() {
    var type = mock(IType.class);
    when(type.name()).thenReturn("test.type");

    var svc = mock(TextProviderService.class);
    when(svc.type()).thenReturn(type);

    var store = mock(IEditableTranslationStore.class);
    when(store.isDirty()).thenReturn(true);
    when(store.service()).thenReturn(svc);
    return store;
  }
}
