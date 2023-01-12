/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension.testingStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TranslationStoreSupplierExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class FilteredTranslationStoreTest {
  @Test
  public void testFilter(TestingEnvironment env) {
    var addedKey = "added";
    var notExistingKey = "notexisting";
    var inner = testingStore(env);
    var store = new FilteredTranslationStore(inner, asList(TranslationStoreSupplierExtension.TRANSLATION_KEY_1, TranslationStoreSupplierExtension.TRANSLATION_KEY_2, addedKey, notExistingKey));

    assertEquals(2, store.keys().count());
    assertFalse(store.containsKey("whatever")); // does not exist in filter and store
    assertFalse(store.containsKey(TranslationStoreSupplierExtension.TRANSLATION_KEY_3)); // does not exist in filter
    assertFalse(store.containsKey(addedKey)); // does not exist in store
    assertTrue(store.containsKey(TranslationStoreSupplierExtension.TRANSLATION_KEY_1)); // exists in both
    assertEquals(2, store.entries().count());
    assertEquals(2, store.languages().count());

    var toAdd = new Translation(addedKey);
    toAdd.putText(Language.LANGUAGE_DEFAULT, "test");
    store.setTranslation(toAdd);

    var newLangKey = "newLang";
    var toAddDifferentLang = new Translation(newLangKey);
    toAddDifferentLang.putText(Language.LANGUAGE_DEFAULT, "test2");
    var langDe = Language.parseThrowingOnError("de");
    toAddDifferentLang.putText(langDe, "test2");
    store.setTranslation(toAddDifferentLang);

    assertEquals(2, store.languages().count()); // unchanged language because the one added above is not in the filter
    assertEquals(3, store.keys().count());
    assertTrue(store.containsKey(addedKey));
    assertEquals(3, store.entries().count());

    assertFalse(store.get("whatever").isPresent()); // does not exist in filter and store
    assertFalse(store.get(TranslationStoreSupplierExtension.TRANSLATION_KEY_3).isPresent()); // does not exist in filter
    assertFalse(store.get(notExistingKey).isPresent()); // does not exist in store
    assertTrue(store.get(TranslationStoreSupplierExtension.TRANSLATION_KEY_1).isPresent()); // exists in both

    assertTrue(store.get(langDe).isPresent());
    assertEquals(0, store.get(langDe).orElseThrow().size());
    assertEquals(3, store.get(Language.LANGUAGE_DEFAULT).orElseThrow().size());

    assertFalse(store.get("whatever", Language.LANGUAGE_DEFAULT).isPresent()); // does not exist in filter and store
    assertFalse(store.get(TranslationStoreSupplierExtension.TRANSLATION_KEY_3, Language.LANGUAGE_DEFAULT).isPresent()); // does not exist in filter
    assertFalse(store.get(notExistingKey, Language.LANGUAGE_DEFAULT).isPresent()); // does not exist in store
    assertTrue(store.get(TranslationStoreSupplierExtension.TRANSLATION_KEY_1, Language.LANGUAGE_DEFAULT).isPresent()); // exists in both

    store.flush(env, new NullProgress());

    store.removeTranslation(addedKey);
    assertEquals(2, store.entries().count());
    store.addNewLanguage(Language.parseThrowingOnError("es_AR"));
    assertTrue(store.isDirty());
    assertTrue(store.isEditable());

    var changedKey = "changed";
    store.changeKey(TranslationStoreSupplierExtension.TRANSLATION_KEY_1, changedKey);
    assertFalse(store.containsKey(changedKey)); // because not part of the filter
    store.changeKey(changedKey, notExistingKey);
    assertTrue(store.containsKey(notExistingKey));

    store.setTranslation(toAddDifferentLang);
    assertFalse(store.containsKey(changedKey));
    assertNotNull(store.service());
    store.reload(new NullProgress());
  }

  @Test
  @SuppressWarnings({"ConstantConditions", "SimplifiableJUnitAssertion", "EqualsBetweenInconvertibleTypes", "EqualsWithItself"})
  public void testEqualsAndToString(TestingEnvironment env) {
    var inner = testingStore(env);

    var store1 = new FilteredTranslationStore(inner, asList(TranslationStoreSupplierExtension.TRANSLATION_KEY_1, TranslationStoreSupplierExtension.TRANSLATION_KEY_2));
    var store2 = new FilteredTranslationStore(inner, singletonList(TranslationStoreSupplierExtension.TRANSLATION_KEY_2));
    var store3 = new FilteredTranslationStore(inner, singletonList(TranslationStoreSupplierExtension.TRANSLATION_KEY_2));

    assertEquals(FilteredTranslationStore.class.getSimpleName() + " [" + store1.service().type().name() + ']', store1.toString());
    assertFalse(store1.equals(null));
    assertFalse(store1.equals(""));
    assertFalse(store1.equals(store2));
    assertTrue(store1.equals(store1));
    assertTrue(store3.equals(store2));
  }
}
