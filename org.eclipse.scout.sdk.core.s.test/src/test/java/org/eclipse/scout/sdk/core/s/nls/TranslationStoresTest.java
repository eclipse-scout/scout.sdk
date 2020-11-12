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

import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.isContentAvailable;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.registerStoreSupplier;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.removeStoreSupplier;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.storeSuppliers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class TranslationStoresTest {
  @Test
  public void testRegisterStoreSupplier() {
    var supplier = mock(ITranslationStoreSupplier.class);
    var sizeBeforeModification = storeSuppliers().size();
    try {
      assertTrue(registerStoreSupplier(supplier));
      assertEquals(sizeBeforeModification + 1, storeSuppliers().size());

      // second registration of the same supplier has no effect
      assertFalse(registerStoreSupplier(supplier));
      assertEquals(sizeBeforeModification + 1, storeSuppliers().size());
    }
    finally {
      removeStoreSupplier(supplier);
    }
  }

  @Test
  public void testIsContentAvailable() {
    assertFalse(isContentAvailable(null));

    var store = mock(ITranslationStore.class);
    when(store.size()).thenReturn(4L);
    when(store.languages()).thenAnswer(invocation -> Stream.empty());
    assertFalse(isContentAvailable(store));

    when(store.languages()).thenAnswer(invocation -> Stream.of(Language.parseThrowingOnError("es")));
    assertFalse(isContentAvailable(store));

    when(store.languages()).thenAnswer(invocation -> Stream.of(Language.LANGUAGE_DEFAULT));
    assertTrue(isContentAvailable(store));

    when(store.size()).thenReturn(0L);
    assertFalse(isContentAvailable(store));
  }
}
