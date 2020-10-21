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
    var stack = mock(TranslationStoreStack.class);
    var translation = mock(ITranslationEntry.class);
    when(translation.key()).thenReturn("key");

    var removeTranslationEvent = createRemoveTranslationEvent(stack, translation);
    var removeTranslationEvent2 = createRemoveTranslationEvent(stack, translation);
    var addTranslationEvent = createAddTranslationEvent(stack, translation);

    assertEquals(removeTranslationEvent.hashCode(), removeTranslationEvent2.hashCode());
    assertFalse(removeTranslationEvent.equals(null));
    assertFalse(removeTranslationEvent.equals(""));
    assertFalse(removeTranslationEvent.equals(addTranslationEvent));
    assertTrue(removeTranslationEvent.equals(removeTranslationEvent2));
    assertTrue(removeTranslationEvent.equals(removeTranslationEvent));
  }

  @Test
  public void testToString() {
    var key = "key";
    var oldKey = "oldKey";
    var stack = mock(TranslationStoreStack.class);
    var translation = mock(ITranslationEntry.class);
    when(translation.key()).thenReturn(key);

    var removeTranslationEvent = createRemoveTranslationEvent(stack, translation);
    var addTranslationEvent = createAddTranslationEvent(stack, translation);
    var addLanguageEvent = createAddLanguageEvent(stack, Language.LANGUAGE_DEFAULT);
    var changeKeyEvent = createChangeKeyEvent(stack, translation, oldKey);
    var flushEvent = createFlushEvent(stack);
    var reloadEvent = createReloadEvent(stack);
    var updateTranslationEvent = createUpdateTranslationEvent(stack, translation);

    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=RemoveTranslation, key=" + key + "]", removeTranslationEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=NewTranslation, key=" + key + "]", addTranslationEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=NewLanguage, language=default]", addLanguageEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=KeyChange, key=" + oldKey + ", newKey=" + key + "]", changeKeyEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=Flush]", flushEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=Reload]", reloadEvent.toString());
    assertEquals(TranslationStoreStackEvent.class.getSimpleName() + " [type=UpdateTranslation, key=" + key + "]", updateTranslationEvent.toString());
  }
}
