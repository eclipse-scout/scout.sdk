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

import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreStackEvent.createAddLanguageEvent;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreStackEvent.createAddTranslationEvent;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreStackEvent.createChangeKeyEvent;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreStackEvent.createFlushEvent;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreStackEvent.createReloadEvent;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreStackEvent.createRemoveTranslationEvent;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreStackEvent.createUpdateTranslationEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

public class TranslationStoreStackEventTest {

  @Test
  @SuppressWarnings({"ConstantConditions", "SimplifiableJUnitAssertion", "EqualsBetweenInconvertibleTypes", "EqualsWithItself"})
  public void testEqualsAndHashCode() {
    TranslationStoreStack stack = mock(TranslationStoreStack.class);
    ITranslationEntry translation = mock(ITranslationEntry.class);
    when(translation.key()).thenReturn("key");

    TranslationStoreStackEvent removeTranslationEvent = createRemoveTranslationEvent(stack, translation);
    TranslationStoreStackEvent removeTranslationEvent2 = createRemoveTranslationEvent(stack, translation);
    TranslationStoreStackEvent addTranslationEvent = createAddTranslationEvent(stack, translation);

    assertEquals(removeTranslationEvent.hashCode(), removeTranslationEvent2.hashCode());
    assertFalse(removeTranslationEvent.equals(null));
    assertFalse(removeTranslationEvent.equals(""));
    assertFalse(removeTranslationEvent.equals(addTranslationEvent));
    assertTrue(removeTranslationEvent.equals(removeTranslationEvent2));
    assertTrue(removeTranslationEvent.equals(removeTranslationEvent));
  }

  @Test
  public void testToString() {
    String key = "key";
    String oldKey = "oldKey";
    TranslationStoreStack stack = mock(TranslationStoreStack.class);
    ITranslationEntry translation = mock(ITranslationEntry.class);
    when(translation.key()).thenReturn(key);

    TranslationStoreStackEvent removeTranslationEvent = createRemoveTranslationEvent(stack, translation);
    TranslationStoreStackEvent addTranslationEvent = createAddTranslationEvent(stack, translation);
    TranslationStoreStackEvent addLanguageEvent = createAddLanguageEvent(stack, Language.LANGUAGE_DEFAULT);
    TranslationStoreStackEvent changeKeyEvent = createChangeKeyEvent(stack, translation, oldKey);
    TranslationStoreStackEvent flushEvent = createFlushEvent(stack);
    TranslationStoreStackEvent reloadEvent = createReloadEvent(stack);
    TranslationStoreStackEvent updateTranslationEvent = createUpdateTranslationEvent(stack, translation);

    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=RemoveTranslation, key=" + key + "]", removeTranslationEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=NewTranslation, key=" + key + "]", addTranslationEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=NewLanguage, language=default]", addLanguageEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=KeyChange, key=" + oldKey + ", newKey=" + key + "]", changeKeyEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=Flush]", flushEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=Reload]", reloadEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=UpdateTranslation, key=" + key + "]", updateTranslationEvent.toString());
  }
}
