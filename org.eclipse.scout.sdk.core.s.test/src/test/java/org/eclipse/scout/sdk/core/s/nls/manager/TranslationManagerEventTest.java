/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls.manager;

import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createAddLanguageEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createAddTranslationEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createChangeKeyEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createFlushEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createReloadEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createRemoveTranslationEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createUpdateTranslationEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.sdk.core.s.nls.Language;
import org.junit.jupiter.api.Test;

public class TranslationManagerEventTest {

  @Test
  @SuppressWarnings({"ConstantConditions", "SimplifiableJUnitAssertion", "EqualsBetweenInconvertibleTypes", "EqualsWithItself"})
  public void testEqualsAndHashCode() {
    var manager = mock(TranslationManager.class);
    var translation = mock(IStackedTranslation.class);
    when(translation.key()).thenReturn("key");

    var removeTranslationEvent = createRemoveTranslationEvent(manager, translation);
    var removeTranslationEvent2 = createRemoveTranslationEvent(manager, translation);
    var addTranslationEvent = createAddTranslationEvent(manager, translation);

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
    var manager = mock(TranslationManager.class);
    var translation = mock(IStackedTranslation.class);
    when(translation.key()).thenReturn(key);

    var removeTranslationEvent = createRemoveTranslationEvent(manager, translation);
    var addTranslationEvent = createAddTranslationEvent(manager, translation);
    var addLanguageEvent = createAddLanguageEvent(manager, Language.LANGUAGE_DEFAULT);
    var changeKeyEvent = createChangeKeyEvent(manager, translation, oldKey);
    var flushEvent = createFlushEvent(manager);
    var reloadEvent = createReloadEvent(manager);
    var updateTranslationEvent = createUpdateTranslationEvent(manager, translation);

    assertEquals(TranslationManagerEvent.class.getSimpleName() + " [type=RemoveTranslation, key=" + key + "]", removeTranslationEvent.toString());
    assertEquals(TranslationManagerEvent.class.getSimpleName() + " [type=NewTranslation, key=" + key + "]", addTranslationEvent.toString());
    assertEquals(TranslationManagerEvent.class.getSimpleName() + " [type=NewLanguage, language=default]", addLanguageEvent.toString());
    assertEquals(TranslationManagerEvent.class.getSimpleName() + " [type=KeyChange, key=" + oldKey + ", newKey=" + key + "]", changeKeyEvent.toString());
    assertEquals(TranslationManagerEvent.class.getSimpleName() + " [type=Flush]", flushEvent.toString());
    assertEquals(TranslationManagerEvent.class.getSimpleName() + " [type=Reload]", reloadEvent.toString());
    assertEquals(TranslationManagerEvent.class.getSimpleName() + " [type=UpdateTranslation, key=" + key + "]", updateTranslationEvent.toString());
  }
}
