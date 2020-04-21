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
package org.eclipse.scout.sdk.core.s.nls.properties;

import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension.createEmptyStore;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension.createReadOnlyStore;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension.testingStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link PropertiesTranslationStoreTest}</h3>
 *
 * @since 7.0.0
 */
@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWith(TranslationStoreSupplierExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class), flushToDisk = true)
public class PropertiesTranslationStoreTest {

  @Test
  public void testStore(TestingEnvironment env) {
    Language en = TranslationStoreSupplierExtension.EN;
    Language de = Language.parseThrowingOnError("de");
    PropertiesTranslationStore store = testingStore(env);

    assertFalse(store.isDirty());
    assertTrue(store.isEditable());
    assertTrue(store.containsKey("key2"));
    assertFalse(store.containsKey("key__4"));
    assertEquals(3, store.keys().count());
    assertTrue(store.get("key1").isPresent());
    assertFalse(store.get("key__4").isPresent());
    assertTrue(store.get("key2", Language.LANGUAGE_DEFAULT).isPresent());
    assertFalse(store.get("key2", en).isPresent());
    assertEquals(3, store.get(Language.LANGUAGE_DEFAULT).get().size());
    assertFalse(store.get(de).isPresent());
    assertEquals(2, store.get(en).get().size());
    assertEquals(3, store.entries().count());
    assertEquals(3, store.languages().count());
    assertNotNull(store.service());

    // add language
    store.addNewLanguage(de);
    assertTrue(store.isDirty());
    store.reload(new NullProgress()); // revert
    assertFalse(store.isDirty());
    store.reload(new NullProgress()); // revert (should have no effect)
    assertEquals(3, store.languages().count());

    // modify and flush
    store.addNewLanguage(de); // add new language
    Translation toAdd = new Translation("added");
    toAdd.putText(Language.LANGUAGE_DEFAULT, "new def");
    toAdd.putText(de, "new de");
    toAdd.putText(Language.parseThrowingOnError("notexisting"), "new not existing");
    store.addNewTranslation(toAdd); // add new translation using the new language
    ITranslation addAndRemove = new Translation("addedAndRemoved");
    store.addNewTranslation(addAndRemove); // add another
    assertEquals(5, store.entries().count());
    store.removeTranslation(addAndRemove.key()); // remove the one just inserted
    assertEquals(4, store.entries().count());
    store.removeTranslation("key2"); // remove an existing one
    assertEquals(3, store.entries().count());
    assertEquals(5, store.languages().count());

    store.flush(env, new NullProgress());

    store.reload(new NullProgress()); // check if it is written
    assertEquals("new de", store.get("added", de).get());
    assertEquals(5, store.languages().count());
    assertEquals(3, store.entries().count());
  }

  @Test
  public void testReadOnlyStore(TestingEnvironment env) {
    PropertiesTranslationStore store = createReadOnlyStore(env);
    assertFalse(store.isEditable());
    assertFalse(store.isDirty());
    store.reload(new NullProgress());
    assertFalse(store.isEditable());

    assertThrows(IllegalArgumentException.class, () -> store.addNewLanguage(Language.parseThrowingOnError("de")));

    store.flush(env, new NullProgress());
  }

  @Test
  public void testEmptyStore(TestingEnvironment env) {
    PropertiesTranslationStore empty = createEmptyStore(env.primaryEnvironment());

    assertFalse(empty.keys().findAny().isPresent());
    assertFalse(empty.isEditable());
    assertFalse(empty.isDirty());
  }

  @Test
  @SuppressWarnings({"unlikely-arg-type", "ConstantConditions", "EqualsBetweenInconvertibleTypes", "SimplifiableJUnitAssertion", "EqualsWithItself"})
  public void testStoreEqualsHashCode() {
    PropertiesTranslationStore store1 = ScoutJavaEnvironmentFactory.call(TranslationStoreSupplierExtension::createEmptyStore, false, false);
    PropertiesTranslationStore store2 = ScoutJavaEnvironmentFactory.call(TranslationStoreSupplierExtension::createEmptyStore, false, false);

    assertFalse(store1.equals(null));
    assertFalse(store1.equals(store2));
    assertFalse(store1.equals(""));
    assertTrue(store1.equals(store1));

    assertNotNull(store1.toString());
    assertEquals(store1.hashCode(), store2.hashCode());
  }
}
