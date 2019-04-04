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
  @SuppressWarnings("unlikely-arg-type")
  public void testEntry() {
    ITranslationStore store = mock(ITranslationStore.class);
    ITranslationStore store2 = mock(ITranslationStore.class);
    TranslationEntry a = new TranslationEntry("key", store);

    Translation template1 = new Translation("otherKey");
    template1.setKey("otherKey2");
    assertEquals("otherKey2", template1.key());
    template1.putTranslation(Language.LANGUAGE_DEFAULT, "def");
    ITranslation template2 = new Translation(template1);
    Translation template3 = new Translation(template1);
    template3.putTranslation(Language.LANGUAGE_DEFAULT, null); // removes the entry

    assertEquals("def", template2.translation(Language.LANGUAGE_DEFAULT).get());
    assertFalse(template2.translation(Language.parseThrowingOnError("notexisting")).isPresent());
    assertFalse(template3.translation(Language.LANGUAGE_DEFAULT).isPresent());

    TranslationEntry b = new TranslationEntry(template2, store);
    TranslationEntry c = new TranslationEntry("key", store2);
    assertThrows(IllegalArgumentException.class, () -> new TranslationEntry("", store).toString());

    assertSame(store, a.store());
    assertNotEquals(a.hashCode(), b.hashCode());
    assertFalse(a.equals(null));
    assertTrue(a.equals(a));
    assertTrue(template1.equals(template1));
    assertFalse(a.equals(""));
    assertFalse(a.equals(c));
    assertNotNull(a.toString());
  }
}
