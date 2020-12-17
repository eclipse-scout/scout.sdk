/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls.properties;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension.testingStore;
import static org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile.getPropertiesFileName;
import static org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile.parseLanguageFromFileName;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Locale;

import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link TranslationPropertiesFileTest}</h3>
 *
 * @since 7.0.0
 */
@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWith(TranslationStoreSupplierExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class TranslationPropertiesFileTest {

  @Test
  public void testTranslationPropertiesFile(TestingEnvironment env) {
    var store = testingStore(env);
    var props0 = store.translationFiles().get(Language.LANGUAGE_DEFAULT);
    var props1 = store.translationFiles().get(TranslationStoreSupplierExtension.EN);
    var props2 = store.translationFiles().get(TranslationStoreSupplierExtension.ES);
    var readOnly = TranslationStoreSupplierExtension.createReadOnlyStore(env).translationFiles().get(Language.LANGUAGE_DEFAULT);

    // test editable files
    assertFalse(props1.load(new NullProgress())); // load a second time
    assertFalse(props2.load(new NullProgress())); // load a second time
    assertEquals(2, props1.allEntries().size());
    assertEquals(0, props2.allEntries().size());
    assertEquals(2, props1.allKeys().count());
    assertEquals(0, props2.allKeys().count());
    assertEquals(TranslationStoreSupplierExtension.KEY_1_VAL_EN, props1.translation(TranslationStoreSupplierExtension.TRANSLATION_KEY_1).get());
    assertFalse(props2.translation(TranslationStoreSupplierExtension.TRANSLATION_KEY_1).isPresent());
    assertEquals(TranslationStoreSupplierExtension.EN, props1.language());
    assertEquals(Language.LANGUAGE_DEFAULT, props0.language());
    assertTrue(props1.isEditable());
    assertTrue(props2.isEditable());

    // test read only file
    assertFalse(readOnly.isEditable());
    assertThrows(UnsupportedOperationException.class, () -> readOnly.setTranslation("a", "b"));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.removeTranslation("a"));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.flush(null, null));
  }

  @Test
  public void testParseWithAllLocales() {
    var prefix = "prefix";
    var availableLocaleNames = Arrays.stream(Locale.getAvailableLocales())
        .map(Locale::toString)
        .toArray(String[]::new);
    var languages = Arrays.stream(availableLocaleNames)
        .map(locale -> Strings.notBlank(locale).orElseGet(() -> Language.LANGUAGE_DEFAULT.locale().toString()))
        .map(Language::parseThrowingOnError)
        .collect(toList());

    var parsedLanguages = languages.stream()
        .map(lang -> getPropertiesFileName(prefix, lang))
        .map(fileName -> parseLanguageFromFileName(fileName, prefix).get())
        .collect(toList());
    var parsedLocaleNames = parsedLanguages.stream()
        .map(lang -> lang == Language.LANGUAGE_DEFAULT ? Locale.ROOT : lang.locale())
        .map(Locale::toString)
        .toArray(String[]::new);

    assertEquals(languages, parsedLanguages);
    assertArrayEquals(availableLocaleNames, parsedLocaleNames);
  }

  @Test
  public void testParse() {
    assertSame(Language.LANGUAGE_DEFAULT, parseLanguageFromFileName("prefix.properties", "prefix").get());
    assertEquals(new Language(new Locale("test")), parseLanguageFromFileName("prefix_test.properties", "prefix").get());
    assertEquals(new Language(new Locale("de", "FR", "xx")), parseLanguageFromFileName("prefix_de_FR_xx.properties", "prefix").get());
    assertEquals(new Language(new Locale("de", "FR")), parseLanguageFromFileName("prefix_de_FR.properties", "prefix").get());
    assertEquals(new Language(new Locale("de")), parseLanguageFromFileName("prefix_de.properties", "prefix").get());
    assertFalse(parseLanguageFromFileName("abc", "abc").isPresent());
    assertFalse(parseLanguageFromFileName(null, "").isPresent());
    assertFalse(parseLanguageFromFileName("prefix_test.properties", "text").isPresent());
  }
}
