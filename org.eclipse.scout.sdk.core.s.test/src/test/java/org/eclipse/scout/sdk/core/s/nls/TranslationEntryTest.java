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
package org.eclipse.scout.sdk.core.s.nls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link TranslationEntryTest}</h3>
 *
 * @since 7.0.0
 */
public class TranslationEntryTest {
  @Test
  @SuppressWarnings({"unlikely-arg-type", "ConstantConditions", "SimplifiableJUnitAssertion", "EqualsWithItself", "EqualsBetweenInconvertibleTypes"})
  public void testEntry() {
    var store = mock(ITranslationStore.class);
    var store2 = mock(ITranslationStore.class);
    var a = new TranslationEntry("key", store);

    var template1 = new Translation("otherKey");
    template1.setKey("otherKey2");
    assertEquals("otherKey2", template1.key());
    template1.putText(Language.LANGUAGE_DEFAULT, "def");
    ITranslation template2 = new Translation(template1);
    var template3 = new Translation(template1);
    template3.putText(Language.LANGUAGE_DEFAULT, null); // removes the entry

    assertEquals("def", template2.text(Language.LANGUAGE_DEFAULT).get());
    assertFalse(template2.text(Language.parseThrowingOnError("notexisting")).isPresent());
    assertFalse(template3.text(Language.LANGUAGE_DEFAULT).isPresent());

    var b = new TranslationEntry(template2, store);
    var c = new TranslationEntry("key", store2);
    //noinspection ResultOfObjectAllocationIgnored
    assertThrows(IllegalArgumentException.class, () -> new TranslationEntry("", store));

    assertSame(store, a.store());
    assertNotEquals(a.hashCode(), b.hashCode());
    assertFalse(a.equals(null));
    assertTrue(a.equals(a));
    assertTrue(template1.equals(template1));
    assertFalse(a.equals(""));
    assertFalse(a.equals(c));
    assertNotNull(a.toString());
  }

  @Test
  public void testBestText() {
    var t = new Translation("whatever");
    var defaultText = "default";
    var englishText = "en";
    var germanSwitzerlandText = "de-CH";
    t.putText(Language.LANGUAGE_DEFAULT, defaultText);
    t.putText(Language.parseThrowingOnError("en"), englishText);
    t.putText(Language.parseThrowingOnError("de_CH"), germanSwitzerlandText);
    t.putText(Language.parseThrowingOnError("de_DE"), "de-DE");
    t.putText(Language.parseThrowingOnError("de_DE_x"), "de-DE-x");

    assertEquals(defaultText, t.bestText(null).get());
    assertEquals(defaultText, t.bestText(Language.parseThrowingOnError("es")).get());
    assertEquals(englishText, t.bestText(Language.parseThrowingOnError("en_GB")).get());
    assertEquals(germanSwitzerlandText, t.bestText(Language.parseThrowingOnError("de_CH")).get());
    assertEquals(defaultText, t.bestText(Language.parseThrowingOnError("de")).get()); // return default because it is unspecified which german locale should win
  }
}
