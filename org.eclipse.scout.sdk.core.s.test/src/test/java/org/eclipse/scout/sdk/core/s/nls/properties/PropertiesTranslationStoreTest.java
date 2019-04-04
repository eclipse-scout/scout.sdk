/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.nls.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import formdata.shared.texts.TestTextProviderService;

/**
 * <h3>{@link PropertiesTranslationStoreTest}</h3>
 *
 * @since 7.0.0
 */
@ExtendWith(TestingEnvironmentExtension.class)
public class PropertiesTranslationStoreTest {

  @Test
  @ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class), flushToDisk = true)
  public void testStore(TestingEnvironment env) throws IOException {
    Path tmpDir = Files.createTempDirectory("sdkTest");
    try {
      PropertiesTranslationStore store = createStore(env.primaryEnvironment(), tmpDir);
      Language en = Language.parseThrowingOnError("en_US");
      Language de = Language.parseThrowingOnError("de");

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
      toAdd.putTranslation(Language.LANGUAGE_DEFAULT, "new def");
      toAdd.putTranslation(de, "new de");
      toAdd.putTranslation(Language.parseThrowingOnError("notexisting"), "new not existing");
      store.addNewTranslation(toAdd); // add new translation using the new language
      ITranslation addAndRemove = new Translation("addedAndRemoved");
      store.addNewTranslation(addAndRemove); // add another
      assertEquals(5, store.entries().count());
      store.removeTranslation(addAndRemove.key()); // remove the one just inserted
      assertEquals(4, store.entries().count());
      store.removeTranslation("key2"); // remove an existing one
      assertEquals(3, store.entries().count());
      assertEquals(4, store.languages().count());

      store.flush(env, new NullProgress());

      store.reload(new NullProgress()); // check if it is written
      assertEquals("new de", store.get("added", de).get());
      assertEquals(4, store.languages().count());
      assertEquals(3, store.entries().count());
    }
    finally {
      CoreUtils.deleteDirectory(tmpDir);
    }
  }

  @Test
  @ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class), flushToDisk = true)
  public void testReadOnlyStore(TestingEnvironment env) throws IOException {
    Path tmpDir = Files.createTempDirectory("sdkTest");
    try {
      PropertiesTranslationStore store = createStore(env.primaryEnvironment(), tmpDir, 2, true);
      assertFalse(store.isEditable());
      assertFalse(store.isDirty());
      store.reload(new NullProgress());
      assertFalse(store.isEditable());

      assertThrows(IllegalArgumentException.class, () -> store.addNewLanguage(Language.parseThrowingOnError("de")));

      store.flush(env, new NullProgress());
    }
    finally {
      CoreUtils.deleteDirectory(tmpDir);
    }
  }

  @Test
  @ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class), flushToDisk = true)
  public void testEmptyStore(TestingEnvironment env) {
    PropertiesTranslationStore empty = createStore(env.primaryEnvironment(), null, 0, false);
    assertFalse(empty.isEditable());
    assertFalse(empty.isDirty());
  }

  @Test
  @SuppressWarnings("unlikely-arg-type")
  @ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class), flushToDisk = true)
  public void testStoreEqualsHashCode(TestingEnvironment env) throws IOException {
    Path tmpDir = Files.createTempDirectory("sdkTest");
    try {
      PropertiesTranslationStore store1 = createStore(env.primaryEnvironment(), tmpDir);
      PropertiesTranslationStore store2 = ScoutJavaEnvironmentFactory.call(e -> createStore(e, tmpDir), false, false);

      assertFalse(store1.equals(null));
      assertFalse(store1.equals(store2));
      assertFalse(store1.equals(""));
      assertTrue(store1.equals(store1));

      assertNotNull(store1.toString());
      assertEquals(store1.hashCode(), store2.hashCode());
    }
    finally {
      CoreUtils.deleteDirectory(tmpDir);
    }
  }

  public static PropertiesTranslationStore createStore(IJavaEnvironment sharedEnv, Path dir) {
    return createStore(sharedEnv, dir, 3, false);
  }

  public static PropertiesTranslationStore createStore(IJavaEnvironment sharedEnv, Path dir, int numFiles, boolean readOnly) {
    IType txtSvcType = sharedEnv.requireType(TestTextProviderService.class.getName());
    PropertiesTextProviderService txtSvc = PropertiesTextProviderService.create(txtSvcType).get();
    PropertiesTranslationStore store = new PropertiesTranslationStore(txtSvc);

    Properties def = new Properties();
    def.setProperty("key1", "1_def");
    def.setProperty("key2", "2_def");
    def.setProperty("key3", "3_def");

    Properties en = new Properties();
    en.setProperty("key1", "1_en");
    en.setProperty("key3", "3_en");

    Collection<ITranslationPropertiesFile> translationFiles = new ArrayList<>();
    try {
      if (numFiles > 0) {
        translationFiles.add(TranslationPropertiesFileTest.createTranslationFile(dir.resolve("Prefix.properties"), def));
      }
      if (numFiles > 1) {
        translationFiles.add(TranslationPropertiesFileTest.createTranslationFile(dir.resolve("Prefix_en_US.properties"), en));
      }
      if (numFiles > 2) {
        translationFiles.add(TranslationPropertiesFileTest.createTranslationFile(dir.resolve("Prefix_es.properties")));
      }
      if (readOnly) {
        translationFiles.add(new ReadOnlyTranslationFile(() -> new ByteArrayInputStream(new byte[]{}), Language.parseThrowingOnError("it")));
      }
    }
    catch (IOException e) {
      throw new SdkException(e);
    }

    store.load(translationFiles, new NullProgress());
    return store;
  }
}
