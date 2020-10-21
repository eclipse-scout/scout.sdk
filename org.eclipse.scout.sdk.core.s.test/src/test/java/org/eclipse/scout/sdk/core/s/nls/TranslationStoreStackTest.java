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
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension.testingStack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link TranslationStoreStackTest}</h3>
 *
 * @since 7.0.0
 */
@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWith(TranslationStoreSupplierExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class TranslationStoreStackTest {

  @Test
  public void testStackRead(TestingEnvironment env) {
    var stack = testingStack(env);
    assertEquals(3, stack.allWithPrefix("k").count());
    assertEquals(1, stack.allWithPrefix("key1").count());
    assertEquals("key10", stack.generateNewKey("key1"));
    assertEquals("somethingNew", stack.generateNewKey("something,New "));
    assertTrue(stack.isEditable());
    assertEquals("1_en", stack.translation("key1").get().text(Language.parseThrowingOnError("en_US")).get());
    assertFalse(stack.translation("dddd").isPresent());
    assertFalse(stack.containsKey("dddd"));
    assertTrue(stack.containsKey("key1"));
    assertEquals(1, stack.allEditableStores().count());
    assertTrue(stack.primaryEditableStore().isPresent());
    assertEquals(3, stack.allEntries().count());

    assertFalse(stack.isDirty());
    assertEquals(2, stack.allStores().count());
    assertEquals(3, stack.allEditableLanguages().count());
    assertEquals(3, stack.allLanguages().count());
    assertNotNull(stack.toString());
  }

  @Test
  public void testMergeTranslation(TestingEnvironment env) {
    var stack = testingStack(env);

    var key = "added";
    var engb = Language.parseThrowingOnError("en_GB");
    var valAfterInsert = "engb1";
    var valAfterUpdate = "engb2";

    var toAdd = new Translation(key);
    toAdd.putText(Language.LANGUAGE_DEFAULT, "default1");
    toAdd.putText(engb, valAfterInsert);
    stack.mergeTranslation(toAdd, null);
    assertEquals(1, stack.allWithPrefix("add").count());
    assertEquals(valAfterInsert, stack.translation(key).get().text(engb).get());

    var toModify = new Translation(key);
    toModify.putText(Language.LANGUAGE_DEFAULT, "default2");
    toModify.putText(engb, valAfterUpdate);
    stack.mergeTranslation(toModify, null);
    assertEquals(valAfterUpdate, stack.translation(key).get().text(engb).get());
  }

  @Test
  public void testAddNewLanguage(TestingEnvironment env) {
    var stack = testingStack(env);
    var newStore = createSimpleStoreMock();
    var deCh = Language.parseThrowingOnError("de_CH");
    stack.addNewLanguage(deCh, stack.primaryEditableStore().get());
    assertEquals(4, stack.allEditableLanguages().count());
    stack.flush(env, new NullProgress());

    assertThrows(IllegalArgumentException.class, () -> stack.addNewLanguage(deCh, stack.allStores().filter(s -> !s.isEditable()).findAny().get()));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewLanguage(null, stack.primaryEditableStore().get()));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewLanguage(deCh, newStore));
  }

  @Test
  public void testAutoCreateNewLanguage(TestingEnvironment env) {
    var stack = testingStack(env);
    List<TranslationStoreStackEvent> eventProtocol = new ArrayList<>();
    stack.addListener(events -> events.forEach(eventProtocol::add));

    var update = new Translation(TranslationStoreSupplierExtension.TRANSLATION_KEY_2);
    var newLang = Language.parseThrowingOnError("ru_DE");
    update.putText(Language.LANGUAGE_DEFAULT, "default");
    update.putText(newLang, "ru-de");
    stack.updateTranslation(update);

    var newKey = "newKeyWithExtraLang";
    var add = new Translation(newKey);
    var addLang = Language.parseThrowingOnError("tr_DE");
    add.putText(Language.LANGUAGE_DEFAULT, "default");
    add.putText(addLang, "tr-de");
    stack.addNewTranslation(add);

    assertTrue(stack.primaryEditableStore().get().containsLanguage(newLang));
    assertTrue(stack.primaryEditableStore().get().containsLanguage(addLang));

    assertEquals(4, eventProtocol.size());
    var newLangEvents = eventProtocol.stream().filter(e -> e.type() == TranslationStoreStackEvent.TYPE_NEW_LANGUAGE).collect(toList());
    assertEquals(2, newLangEvents.size());
  }

  @Test
  public void testEventsOnKeyChange(TestingEnvironment env) {
    var initialKey = "startKey";
    var changedKey = TranslationStoreSupplierExtension.TRANSLATION_KEY_1;

    var stack = testingStack(env);
    stack.removeTranslations(Stream.of(changedKey));
    var t = new Translation(initialKey);
    t.putText(Language.LANGUAGE_DEFAULT, "whatever");
    stack.addNewTranslation(t);

    List<TranslationStoreStackEvent> eventProtocol = new ArrayList<>();
    stack.addListener(events -> events.forEach(eventProtocol::add));

    stack.changeKey(initialKey, changedKey);
    stack.changeKey(changedKey, initialKey);

    assertEquals(4, eventProtocol.size());
    assertEquals(TranslationStoreStackEvent.TYPE_KEY_CHANGED, eventProtocol.get(0).type()); // for the key change
    assertEquals(TranslationStoreStackEvent.TYPE_REMOVE_TRANSLATION, eventProtocol.get(1).type()); // for the one that is now overridden
    assertEquals(TranslationStoreStackEvent.TYPE_KEY_CHANGED, eventProtocol.get(2).type()); // for the key change
    assertEquals(TranslationStoreStackEvent.TYPE_NEW_TRANSLATION, eventProtocol.get(3).type()); // for the one that is no longer overridden and is therefore visible again
  }

  @Test
  public void testEventsOnDeleteAndCreateTranslationOverridingExisting(TestingEnvironment env) {
    var stack = testingStack(env);
    List<TranslationStoreStackEvent> eventProtocol = new ArrayList<>();
    stack.addListener(events -> events.forEach(eventProtocol::add));

    stack.removeTranslations(Stream.of(TranslationStoreSupplierExtension.TRANSLATION_KEY_1));

    var newValue = "newVal";
    var t = new Translation(TranslationStoreSupplierExtension.TRANSLATION_KEY_1);
    t.putText(Language.LANGUAGE_DEFAULT, newValue);
    stack.addNewTranslation(t);

    assertEquals(4, eventProtocol.size());
    assertEquals(TranslationStoreStackEvent.TYPE_NEW_TRANSLATION, eventProtocol.get(0).type()); // for the one that becomes visible after deleting the overriding one
    assertEquals(TranslationStoreStackEvent.TYPE_REMOVE_TRANSLATION, eventProtocol.get(1).type()); // for the removal
    assertEquals(TranslationStoreStackEvent.TYPE_REMOVE_TRANSLATION, eventProtocol.get(2).type()); // for the one that is no longer visible after creating the overriding one
    assertEquals(TranslationStoreStackEvent.TYPE_NEW_TRANSLATION, eventProtocol.get(3).type()); // for the new one
  }

  @Test
  public void testAddNewTranslation(TestingEnvironment env) {
    var stack = testingStack(env);
    assertFalse(stack.isDirty());
    var t = new Translation("newKey");
    t.putText(Language.LANGUAGE_DEFAULT, "def");
    stack.addNewTranslation(t, null);
    assertTrue(stack.isDirty());
    var t2 = new Translation("newKey2");
    t2.putText(Language.LANGUAGE_DEFAULT, "def2");
    stack.addNewTranslation(t2);

    ITranslation existing = new Translation("key2");
    t2.putText(Language.LANGUAGE_DEFAULT, "def2");

    var newStore = createSimpleStoreMock();

    assertThrows(IllegalArgumentException.class, () -> stack.addNewTranslation(null));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewTranslation(new Translation("key")));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewTranslation(new Translation("")));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewTranslation(existing));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewTranslation(t2, newStore));

    assertEquals(5, stack.allEntries().count());
  }

  @Test
  public void testRemoveTranslation(TestingEnvironment env) {
    var stack = testingStack(env);
    stack.removeTranslations(Stream.of("key1"));
    stack.removeTranslations(Stream.of("key2", "", "key3"));
    assertEquals(0, stack.primaryEditableStore().get().entries().count());
  }

  @Test
  public void testUpdateTranslation(TestingEnvironment env) {
    var stack = testingStack(env);
    var t = new Translation("key1");
    t.putText(Language.LANGUAGE_DEFAULT, "updated");
    t.putText(Language.LANGUAGE_DEFAULT, "updated");

    stack.updateTranslation(t);
    assertEquals("updated", stack.translation("key1").get().text(Language.LANGUAGE_DEFAULT).get());
  }

  @Test
  public void testChangeKey(TestingEnvironment env) {
    var stack = testingStack(env);
    var newKey = "newKey1";
    stack.changeKey(TranslationStoreSupplierExtension.TRANSLATION_KEY_1, newKey);
    assertEquals(TranslationStoreSupplierExtension.KEY_1_VAL_DEFAULT, stack.translation(newKey).get().text(Language.LANGUAGE_DEFAULT).get());

    // change to itself
    stack.changeKey(newKey, newKey);
    assertEquals(TranslationStoreSupplierExtension.KEY_1_VAL_DEFAULT, stack.translation(newKey).get().text(Language.LANGUAGE_DEFAULT).get());

    assertThrows(IllegalArgumentException.class, () -> stack.changeKey(null, "aaa"));
    assertThrows(IllegalArgumentException.class, () -> stack.changeKey("aa", null));
    assertThrows(IllegalArgumentException.class, () -> stack.changeKey(TranslationStoreSupplierExtension.TRANSLATION_KEY_2, TranslationStoreSupplierExtension.TRANSLATION_KEY_3));
  }

  @Test
  @ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class), flushToDisk = true)
  public void testBatchChange(TestingEnvironment env) {
    var counter = new AtomicInteger();
    var stack = testingStack(env);
    ITranslationStoreStackListener l = s -> counter.incrementAndGet();
    stack.addListener(l);
    stack.setChanging(true);
    var textPrefix = "def杉矶";
    try {
      for (var i = 0; i < 10; i++) {
        var t = new Translation("newKey" + i);
        t.putText(Language.LANGUAGE_DEFAULT, textPrefix + i);
        stack.addNewTranslation(t);
      }
    }
    finally {
      stack.setChanging(false);
    }
    stack.removeListener(l);
    assertEquals(1, counter.intValue());
    stack.flush(env, new NullProgress());
    stack.reload(new NullProgress());
    assertEquals(13, stack.allEntries().count());
    assertEquals(textPrefix + '2', stack.translation("newKey2").get().text(Language.LANGUAGE_DEFAULT).get());
  }

  @Test
  public void testKeyChangeOfOverriddenTranslation() {
    Map<String, Map<Language, String>> entries01 = new HashMap<>();
    Map<String, Map<Language, String>> entries02 = new HashMap<>();
    var key = "testKey";
    var newKey = "newKey";
    entries01.computeIfAbsent(key, k -> new HashMap<>()).put(Language.LANGUAGE_DEFAULT, "text01");
    entries02.computeIfAbsent(key, k -> new HashMap<>()).put(Language.LANGUAGE_DEFAULT, "text02");
    var stack = new TranslationStoreStack(
        Stream.of(
            createStoreMock(200.00d, entries02),
            createStoreMock(100.00d, entries01)));

    assertEquals(1, stack.allEntries().count());
    assertEquals("text01", stack.translation(key).get().texts().get(Language.LANGUAGE_DEFAULT));

    List<Integer> eventTypes = new ArrayList<>(); // remember each event type that happens
    stack.addListener(events -> events.forEach(e -> eventTypes.add(e.type())));
    stack.changeKey(key, newKey);

    assertEquals(2, stack.allEntries().count());
    assertEquals("text02", stack.translation(key).get().texts().get(Language.LANGUAGE_DEFAULT));
    assertEquals("text01", stack.translation(newKey).get().texts().get(Language.LANGUAGE_DEFAULT));
    assertEquals(asList(TranslationStoreStackEvent.TYPE_KEY_CHANGED, TranslationStoreStackEvent.TYPE_NEW_TRANSLATION), eventTypes);
  }

  @Test
  public void testImportNoData() {
    var firstStore = createStoreMock(100.00d, emptyMap());
    var stack = new TranslationStoreStack(Stream.of(firstStore));
    var keyColumnName = "Key";

    // only header row
    var data1 = singletonList(asList(Language.LANGUAGE_DEFAULT.displayName(), "it", keyColumnName));
    var info1 = stack.importTranslations(data1, keyColumnName, null);
    assertEquals(ITranslationImportInfo.NO_DATA, info1.result());

    // no rows
    var info2 = stack.importTranslations(emptyList(), keyColumnName, null);
    assertEquals(ITranslationImportInfo.NO_DATA, info2.result());

    // header row with empty columns
    var emptyHeaderCell = "  ";
    var data3 = asList(
        asList(Language.LANGUAGE_DEFAULT.displayName(), emptyHeaderCell, keyColumnName),
        asList("def", "esp", "xx"));

    var info3 = stack.importTranslations(data3, keyColumnName, null);
    Map<Integer, String> expectedIgnoreColumns = new HashMap<>();
    expectedIgnoreColumns.put(1, emptyHeaderCell);
    assertEquals(expectedIgnoreColumns, info3.ignoredColumns());

    // no valid rows
    var data4 = asList(
        asList(Language.LANGUAGE_DEFAULT.displayName(), "es", keyColumnName),
        asList("", "esp", "xx"));
    var info4 = stack.importTranslations(data4, keyColumnName, null);
    assertEquals(ITranslationImportInfo.NO_DATA, info4.result());
  }

  @Test
  public void testImportIgnoringEmptyCells() {
    var store = createStoreMock(100.00d, emptyMap());
    var stack = new TranslationStoreStack(Stream.of(store));
    var keyColumnName = "Key";
    var key1 = "k1";
    var key2 = "k2";
    var key3 = "k3";
    var data = asList(
        asList(Language.LANGUAGE_DEFAULT.displayName(), "es", keyColumnName),
        asList("def1", "", key1),
        asList("def2", null, key2),
        asList("def3", "content", key3));
    var info = stack.importTranslations(data, keyColumnName, null);
    assertEquals(3, info.result());
    var es = Language.parseThrowingOnError("es");
    assertFalse(store.get(key1, es).isPresent());
    assertFalse(store.get(key2, es).isPresent());
    assertTrue(store.get(key3, es).isPresent());
  }

  @Test
  public void testImportWithIncompleteData() {
    var store = createStoreMock(100.00d, emptyMap());
    var stack = new TranslationStoreStack(Stream.of(store));
    var keyColumnName = "Key";
    var validRowKey = "key02";
    List<List<String>> data = asList(
        asList("de", keyColumnName, Language.LANGUAGE_DEFAULT.displayName(), "fr"),
        emptyList(), // no columns at all
        singletonList("d1"), // no column for key
        asList("d2", "key01"), // no column for default lang
        asList("d3", validRowKey, "def")); // no column for language fr
    var info = stack.importTranslations(data, keyColumnName, null);

    assertEquals(1, info.result());
    assertEquals(1, info.importedTranslations().size());
    assertTrue(info.importedTranslations().containsKey(validRowKey));
    assertEquals(asList(2, 3), info.invalidRowIndices());
  }

  @Test
  public void testImportEmptyColumn() {
    var store = createStoreMock(100.00d, emptyMap());
    var stack = new TranslationStoreStack(Stream.of(store));
    var keyColumnName = "Key";
    List<List<String>> data = asList(
        asList(keyColumnName, " ", "default", "en"),
        emptyList(), // empty row
        asList("k0", "\t", "en0"),
        asList("k1", "", "en1"),
        asList("k2", " ", "en2")); // no column for language fr
    var info = stack.importTranslations(data, keyColumnName, null);
    assertEquals(3, info.result());
    assertEquals(3, info.importedTranslations().size());
    assertTrue(info.ignoredColumns().isEmpty());
    assertTrue(info.invalidRowIndices().isEmpty());
    assertTrue(info.duplicateKeys().isEmpty());
  }

  @Test
  public void testImport() {
    var firstStore = createStoreMock(100.00d, emptyMap());
    var secondStore = createStoreMock(200.00d, emptyMap());
    var stack = new TranslationStoreStack(Stream.of(firstStore, secondStore));
    var keyColumnName = "asdf";
    var key01 = "key01";
    var key02 = "key02???**";
    var key03 = "key03";
    var key04 = "key04";

    var data1 = asList(
        asList(Language.LANGUAGE_DEFAULT.displayName(), "es", keyColumnName),
        asList("def", "esp", key01));
    var info1 = stack.importTranslations(data1, keyColumnName, null);
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
    var info2 = stack.importTranslations(data2, keyColumnName, secondStore);
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
    assertEquals(newKey01DefaultText, firstStore.get(key01, Language.LANGUAGE_DEFAULT).get());
    assertEquals(3, firstStore.languages().count());
    assertEquals(1, secondStore.keys().count());
    assertTrue(secondStore.containsKey(key04));
    assertEquals(newKey04DefaultText, secondStore.get(key04, Language.LANGUAGE_DEFAULT).get());
    assertEquals(2, secondStore.languages().count());
  }

  @Test
  public void testImportNoDefaultLangColumn() {
    var firstStore = createStoreMock(100.00d, emptyMap());
    var secondStore = createStoreMock(200.00d, emptyMap());
    var stack = new TranslationStoreStack(Stream.of(firstStore, secondStore));
    var keyColumnName = "key";
    var data = asList(
        asList("xx", "es", keyColumnName),
        asList("def", "esp", "yy"));
    var info1 = stack.importTranslations(data, keyColumnName, null);
    assertEquals(ITranslationImportInfo.NO_KEY_OR_DEFAULT_LANG_COLUMN, info1.result());
    assertEquals(0, info1.importedTranslations().size());
    assertEquals(-1, info1.defaultLanguageColumnIndex());
    assertEquals(-1, info1.keyColumnIndex());
    assertTrue(info1.ignoredColumns().isEmpty());
    assertTrue(info1.duplicateKeys().isEmpty());
    assertTrue(info1.invalidRowIndices().isEmpty());
  }

  @Test
  public void testImportNoKeyColumn() {
    var firstStore = createStoreMock(100.00d, emptyMap());
    var secondStore = createStoreMock(200.00d, emptyMap());
    var stack = new TranslationStoreStack(Stream.of(firstStore, secondStore));

    var data = asList(
        asList("xx", "es", Language.LANGUAGE_DEFAULT.displayName()),
        asList("def", "esp", "yy"));
    var info1 = stack.importTranslations(data, "key", null);
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
    Map<String, Map<Language, String>> entries01 = new HashMap<>();
    var key01 = "key01";
    var key02 = "key02";
    var key03 = "key03";
    var langDe = Language.parseThrowingOnError("de");
    var langEs = Language.parseThrowingOnError("es");

    var key0101 = entries01.computeIfAbsent(key01, k -> new HashMap<>());
    key0101.put(Language.LANGUAGE_DEFAULT, "01_k1_en");
    key0101.put(langEs, "01_k1_es");
    key0101.put(langDe, "01_k1_de");
    var key0102 = entries01.computeIfAbsent(key02, k -> new HashMap<>());
    key0102.put(Language.LANGUAGE_DEFAULT, "01_k2_en");
    key0102.put(langEs, "01_k2_es");
    key0102.put(langDe, "01_k2_de");
    var key0103 = entries01.computeIfAbsent(key03, k -> new HashMap<>());
    key0103.put(Language.LANGUAGE_DEFAULT, "01_k3_en");
    key0103.put(langEs, "01_k3_es");
    key0103.put(langDe, "01_k3_de");

    Map<String, Map<Language, String>> entries02 = new HashMap<>();
    var key0201 = entries02.computeIfAbsent(key01, k -> new HashMap<>());
    key0201.put(Language.LANGUAGE_DEFAULT, "02_k1_en");
    key0201.put(langDe, "02_k1_de");
    var key0203 = entries02.computeIfAbsent(key03, k -> new HashMap<>());
    key0203.put(Language.LANGUAGE_DEFAULT, "02_k3_en");
    key0203.put(langDe, "02_k3_de");

    Map<String, Map<Language, String>> entries03 = new HashMap<>();
    var key0301 = entries03.computeIfAbsent(key01, k -> new HashMap<>());
    key0301.put(Language.LANGUAGE_DEFAULT, "03_k1_en");

    var stack = new TranslationStoreStack(
        Stream.of(
            createStoreMock(200.00d, entries02),
            createStoreMock(300.00d, entries01),
            createStoreMock(100.00d, entries03)));

    var k01Result = stack.allEntries().filter(e -> key01.equals(e.key())).findAny().get();
    var k02Result = stack.allEntries().filter(e -> key02.equals(e.key())).findAny().get();
    var k03Result = stack.allEntries().filter(e -> key03.equals(e.key())).findAny().get();

    assertEquals(1, k01Result.texts().size());
    assertNull(k01Result.texts().get(langDe));
    assertNull(k01Result.texts().get(langEs));
    assertEquals("03_k1_en", k01Result.texts().get(Language.LANGUAGE_DEFAULT));

    assertEquals(3, k02Result.texts().size());
    assertEquals("01_k2_en", k02Result.texts().get(Language.LANGUAGE_DEFAULT));
    assertEquals("01_k2_es", k02Result.texts().get(langEs));
    assertEquals("01_k2_de", k02Result.texts().get(langDe));

    assertEquals(2, k03Result.texts().size());
    assertEquals("02_k3_en", k03Result.texts().get(Language.LANGUAGE_DEFAULT));
    assertNull(k03Result.texts().get(langEs));
    assertEquals("02_k3_de", k03Result.texts().get(langDe));
  }

  private static ITranslationStore createStoreMock(double order, Map<String, Map<Language, String>> entries) {
    var svc = mock(TextProviderService.class);
    when(svc.order()).thenReturn(order);
    when(svc.type()).thenReturn(mock(IType.class));

    Collection<ITranslationEntry> allEntries = new ArrayList<>();
    for (var entry : entries.entrySet()) {
      var entryMock = mock(ITranslationEntry.class);
      when(entryMock.key()).thenReturn(entry.getKey());
      when(entryMock.texts()).thenReturn(entry.getValue());
      allEntries.add(entryMock);
    }

    var mock = mock(IEditableTranslationStore.class);
    when(mock.isEditable()).thenReturn(true);
    when(mock.changeKey(anyString(), anyString())).then(invocation -> {
      String oldKey = invocation.getArgument(0);
      String newKey = invocation.getArgument(1);
      for (var e : allEntries) {
        if (e.key().equals(oldKey)) {
          when(e.key()).thenReturn(newKey);
          return e;
        }
      }
      return null;
    });
    when(mock.get(anyString())).then(invocation -> {
      for (var e : allEntries) {
        String keyToFind = invocation.getArgument(0);
        if (e.key().equals(keyToFind)) {
          return Optional.of(e);
        }
      }
      return Optional.empty();
    });
    when(mock.get(anyString(), any())).then(invocation -> {
      String key = invocation.getArgument(0);
      Language lang = invocation.getArgument(1);
      return allEntries.stream().filter(e -> e.key().equals(key)).findFirst().flatMap(e -> e.text(lang));
    });
    when(mock.containsKey(anyString())).then(invocation -> mock.get(invocation.<String> getArgument(0)).isPresent());
    when(mock.keys()).thenAnswer(invocation -> allEntries.stream().map(ITranslation::key));
    when(mock.languages()).thenAnswer(invocation -> allEntries.stream().flatMap(e -> e.texts().keySet().stream()).distinct());
    when(mock.service()).thenReturn(svc);
    when(mock.entries()).then(invocation -> allEntries.stream());
    when(mock.addNewTranslation(any())).then(invocation -> {
      ITranslation add = invocation.getArgument(0);
      ITranslationEntry newEntry = new TranslationEntry(add, mock);
      allEntries.add(newEntry);
      return newEntry;
    });
    when(mock.updateTranslation(any())).then(invocation -> {
      ITranslation update = invocation.getArgument(0);
      ITranslationEntry newEntry = new TranslationEntry(update, mock);
      allEntries.removeIf(entry -> entry.key().equals(update.key()));
      allEntries.add(newEntry);
      return newEntry;
    });
    return mock;
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
