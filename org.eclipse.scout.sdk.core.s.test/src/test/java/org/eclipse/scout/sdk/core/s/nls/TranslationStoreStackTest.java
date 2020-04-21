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

import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension.testingStack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    TranslationStoreStack stack = testingStack(env);
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
    TranslationStoreStack stack = testingStack(env);

    String key = "added";
    Language engb = Language.parseThrowingOnError("en_GB");
    String valAfterInsert = "engb1";
    String valAfterUpdate = "engb2";

    Translation toAdd = new Translation(key);
    toAdd.putText(Language.LANGUAGE_DEFAULT, "default1");
    toAdd.putText(engb, valAfterInsert);
    stack.mergeTranslation(toAdd, null);
    assertEquals(1, stack.allWithPrefix("add").count());
    assertEquals(valAfterInsert, stack.translation(key).get().text(engb).get());

    Translation toModify = new Translation(key);
    toModify.putText(Language.LANGUAGE_DEFAULT, "default2");
    toModify.putText(engb, valAfterUpdate);
    stack.mergeTranslation(toModify, null);
    assertEquals(valAfterUpdate, stack.translation(key).get().text(engb).get());
  }

  @Test
  public void testAddNewLanguage(TestingEnvironment env) {
    TranslationStoreStack stack = testingStack(env);
    ITranslationStore newStore = createMockStore();
    Language deCh = Language.parseThrowingOnError("de_CH");
    stack.addNewLanguage(deCh, stack.primaryEditableStore().get());
    assertEquals(4, stack.allEditableLanguages().count());
    stack.flush(env, new NullProgress());

    assertThrows(IllegalArgumentException.class, () -> stack.addNewLanguage(deCh, stack.allStores().filter(s -> !s.isEditable()).findAny().get()));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewLanguage(null, stack.primaryEditableStore().get()));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewLanguage(deCh, newStore));
  }

  @Test
  public void testAddNewTranslation(TestingEnvironment env) {
    TranslationStoreStack stack = testingStack(env);
    assertFalse(stack.isDirty());
    Translation t = new Translation("newKey");
    t.putText(Language.LANGUAGE_DEFAULT, "def");
    stack.addNewTranslation(t, null);
    assertTrue(stack.isDirty());
    Translation t2 = new Translation("newKey2");
    t2.putText(Language.LANGUAGE_DEFAULT, "def2");
    stack.addNewTranslation(t2);

    ITranslation existing = new Translation("key2");
    t2.putText(Language.LANGUAGE_DEFAULT, "def2");

    ITranslationStore newStore = createMockStore();

    assertThrows(IllegalArgumentException.class, () -> stack.addNewTranslation(null));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewTranslation(new Translation("key")));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewTranslation(new Translation("")));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewTranslation(existing));
    assertThrows(IllegalArgumentException.class, () -> stack.addNewTranslation(t2, newStore));

    assertEquals(5, stack.allEntries().count());
  }

  @Test
  public void testRemoveTranslation(TestingEnvironment env) {
    TranslationStoreStack stack = testingStack(env);
    stack.removeTranslations(Stream.of("key1"));
    stack.removeTranslations(Stream.of("key2", "", "key3"));
    assertEquals(0, stack.primaryEditableStore().get().entries().count());
  }

  @Test
  public void testUpdateTranslation(TestingEnvironment env) {
    TranslationStoreStack stack = testingStack(env);
    Translation t = new Translation("key1");
    t.putText(Language.LANGUAGE_DEFAULT, "updated");
    t.putText(Language.LANGUAGE_DEFAULT, "updated");

    stack.updateTranslation(t);
    assertEquals("updated", stack.translation("key1").get().text(Language.LANGUAGE_DEFAULT).get());
  }

  @Test
  public void testChangeKey(TestingEnvironment env) {
    TranslationStoreStack stack = testingStack(env);
    stack.changeKey("key1", "newKey1");
    assertEquals("1_def", stack.translation("newKey1").get().text(Language.LANGUAGE_DEFAULT).get());

    assertThrows(IllegalArgumentException.class, () -> stack.changeKey(null, "aaa"));
    assertThrows(IllegalArgumentException.class, () -> stack.changeKey("aa", null));
    assertThrows(IllegalArgumentException.class, () -> stack.changeKey("aa", "key1"));
  }

  @Test
  @ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class), flushToDisk = true)
  public void testBatchChange(TestingEnvironment env) {
    AtomicInteger counter = new AtomicInteger();
    TranslationStoreStack stack = testingStack(env);
    ITranslationStoreStackListener l = s -> counter.incrementAndGet();
    stack.addListener(l);
    stack.setChanging(true);
    String textPrefix = "def杉矶";
    try {
      for (int i = 0; i < 10; i++) {
        Translation t = new Translation("newKey" + i);
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
    String key = "testKey";
    String newKey = "newKey";
    entries01.computeIfAbsent(key, k -> new HashMap<>()).put(Language.LANGUAGE_DEFAULT, "text01");
    entries02.computeIfAbsent(key, k -> new HashMap<>()).put(Language.LANGUAGE_DEFAULT, "text02");
    TranslationStoreStack stack = new TranslationStoreStack(
        Arrays.asList(
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
    assertEquals(Arrays.asList(TranslationStoreStackEvent.TYPE_KEY_CHANGED, TranslationStoreStackEvent.TYPE_NEW_TRANSLATION), eventTypes);
  }

  /**
   * Tests that the inheritance behaves the same as in the Scout runtime:<br>
   * The first text-provider-service that has an entry for a key wins. Even if another one would have a better language
   * match.
   */
  @Test
  public void testInheritance() {
    Map<String, Map<Language, String>> entries01 = new HashMap<>();
    String key01 = "key01";
    String key02 = "key02";
    String key03 = "key03";
    Language langDe = Language.parseThrowingOnError("de");
    Language langEs = Language.parseThrowingOnError("es");

    Map<Language, String> key0101 = entries01.computeIfAbsent(key01, k -> new HashMap<>());
    key0101.put(Language.LANGUAGE_DEFAULT, "01_k1_en");
    key0101.put(langEs, "01_k1_es");
    key0101.put(langDe, "01_k1_de");
    Map<Language, String> key0102 = entries01.computeIfAbsent(key02, k -> new HashMap<>());
    key0102.put(Language.LANGUAGE_DEFAULT, "01_k2_en");
    key0102.put(langEs, "01_k2_es");
    key0102.put(langDe, "01_k2_de");
    Map<Language, String> key0103 = entries01.computeIfAbsent(key03, k -> new HashMap<>());
    key0103.put(Language.LANGUAGE_DEFAULT, "01_k3_en");
    key0103.put(langEs, "01_k3_es");
    key0103.put(langDe, "01_k3_de");

    Map<String, Map<Language, String>> entries02 = new HashMap<>();
    Map<Language, String> key0201 = entries02.computeIfAbsent(key01, k -> new HashMap<>());
    key0201.put(Language.LANGUAGE_DEFAULT, "02_k1_en");
    key0201.put(langDe, "02_k1_de");
    Map<Language, String> key0203 = entries02.computeIfAbsent(key03, k -> new HashMap<>());
    key0203.put(Language.LANGUAGE_DEFAULT, "02_k3_en");
    key0203.put(langDe, "02_k3_de");

    Map<String, Map<Language, String>> entries03 = new HashMap<>();
    Map<Language, String> key0301 = entries03.computeIfAbsent(key01, k -> new HashMap<>());
    key0301.put(Language.LANGUAGE_DEFAULT, "03_k1_en");

    TranslationStoreStack stack = new TranslationStoreStack(
        Arrays.asList(
            createStoreMock(200.00d, entries02),
            createStoreMock(300.00d, entries01),
            createStoreMock(100.00d, entries03)));

    ITranslationEntry k01Result = stack.allEntries().filter(e -> key01.equals(e.key())).findAny().get();
    ITranslationEntry k02Result = stack.allEntries().filter(e -> key02.equals(e.key())).findAny().get();
    ITranslationEntry k03Result = stack.allEntries().filter(e -> key03.equals(e.key())).findAny().get();

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
    TextProviderService svc = mock(TextProviderService.class);
    when(svc.order()).thenReturn(order);
    when(svc.type()).thenReturn(mock(IType.class));

    Collection<ITranslationEntry> allEntries = new ArrayList<>();
    for (Entry<String, Map<Language, String>> entry : entries.entrySet()) {
      ITranslationEntry entryMock = mock(ITranslationEntry.class);
      when(entryMock.key()).thenReturn(entry.getKey());
      when(entryMock.texts()).thenReturn(entry.getValue());
      allEntries.add(entryMock);
    }

    IEditableTranslationStore mock = mock(IEditableTranslationStore.class);
    when(mock.isEditable()).thenReturn(true);
    when(mock.changeKey(anyString(), anyString())).then(invocation -> {
      String oldKey = invocation.getArgument(0);
      String newKey = invocation.getArgument(1);
      for (ITranslationEntry e : allEntries) {
        if (e.key().equals(oldKey)) {
          when(e.key()).thenReturn(newKey);
          return e;
        }
      }
      return null;
    });
    when(mock.get(anyString())).then(invocation -> {
      for (ITranslationEntry e : allEntries) {
        String keyToFind = invocation.getArgument(0);
        if (e.key().equals(keyToFind)) {
          return Optional.of(e);
        }
      }
      return Optional.empty();
    });
    when(mock.containsKey(anyString())).then(invocation -> mock.get(invocation.<String> getArgument(0)).isPresent());
    when(mock.service()).thenReturn(svc);
    when(mock.entries()).then(invocation -> allEntries.stream());
    return mock;
  }

  private static ITranslationStore createMockStore() {
    IType type = mock(IType.class);
    when(type.name()).thenReturn("test.type");

    TextProviderService svc = mock(TextProviderService.class);
    when(svc.type()).thenReturn(type);

    IEditableTranslationStore store = mock(IEditableTranslationStore.class);
    when(store.isDirty()).thenReturn(true);
    when(store.service()).thenReturn(svc);
    return store;
  }
}
