/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.isContentAvailable;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.registerStoreSupplier;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.removeStoreSupplier;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.storeSuppliers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.IType;
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

    // do not complain if no default language is present
    // if a text from the scout rt is overwritten (e.g. a spanish text is modified), the default text from scout may be fine.
    // therefore only the spanish file may exist and may contain translations
    when(store.languages()).thenAnswer(invocation -> Stream.of(Language.parseThrowingOnError("es")));
    assertTrue(isContentAvailable(store));

    when(store.languages()).thenAnswer(invocation -> Stream.of(Language.LANGUAGE_DEFAULT));
    assertTrue(isContentAvailable(store));

    // do not exclude empty text services. These might have been just created and users might want to fill them e.g. using the translation editor
    when(store.size()).thenReturn(0L);
    assertTrue(isContentAvailable(store));
  }

  @Test
  public void testImplicitOverrides() {
    var s1 = createStore("test.Class1", 100, "a", "b", "c");
    var s2 = createStore("test.Class2", 100, "c", "d");
    var s3 = createStore("test.Class3", 100, "x", "y");
    var s4 = createStore("test.Class4", 100, "a");
    var s5 = createStore("test.Class5", 200, "c", "d");
    var s6 = createStore("test.Class6", 300, "w", "q");
    var s7 = createStore("test.Class7", 200, "a", "c");
    var s8 = createStore("test.Class8", 200, "v", "z");

    var implicitOverrides = TranslationStores.havingImplicitOverrides(List.of(s1, s2, s3, s4, s5, s6, s7, s8));
    var implicitOverrideNames = getImplicitOverrideNames(implicitOverrides);
    assertEquals(Set.of("test.Class1,test.Class2,test.Class4", "test.Class5,test.Class7"), implicitOverrideNames);

    assertEquals(0, TranslationStores.havingImplicitOverrides(List.of(s1, s3)).count());
  }

  private static Set<String> getImplicitOverrideNames(Stream<Set<ITranslationStore>> implicitOverrides) {
    return implicitOverrides.map(stores -> stores.stream()
        .map(ITranslationStore::service)
        .map(TextProviderService::type)
        .map(IType::name)
        .sorted()
        .collect(joining(",")))
        .collect(toSet());
  }

  private static ITranslationStore createStore(String fqn, double order, String... keys) {
    var serviceType = mock(IType.class);
    when(serviceType.name()).thenReturn(fqn);

    var textService = mock(TextProviderService.class);
    when(textService.order()).thenReturn(order);
    when(textService.type()).thenReturn(serviceType);

    var store = mock(ITranslationStore.class);
    when(store.service()).thenReturn(textService);
    when(store.keys()).thenAnswer(a -> Stream.of(keys));
    when(store.containsKey(anyString())).thenAnswer(a -> containsKey(a.getArgument(0), keys));
    return store;
  }

  private static boolean containsKey(String key, String... keys) {
    return Arrays.asList(keys).contains(key);
  }
}
