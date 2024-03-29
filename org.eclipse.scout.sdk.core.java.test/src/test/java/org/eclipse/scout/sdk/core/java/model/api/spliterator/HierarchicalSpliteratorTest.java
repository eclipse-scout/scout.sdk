/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.util.SuperHierarchySpliterator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class HierarchicalSpliteratorTest {
  @Test
  public void testCharacteristicsAndSize() {
    var t = mock(IType.class);
    when(t.superClass()).thenReturn(Optional.empty());
    when(t.superInterfaces()).thenReturn(Stream.empty());

    var levelCharacteristics = 100; // whatever
    var numElements = 4;
    Spliterator<Object> mock = mock(Spliterator.class);
    when(mock.characteristics()).thenReturn(levelCharacteristics);
    when(mock.estimateSize()).thenReturn((long) numElements);

    Function<IType, Spliterator<Object>> f = type -> mock;

    Spliterator<Object> startTypeOnly = new HierarchicalSpliterator<>(t, false, false, true, f);
    assertEquals(levelCharacteristics, startTypeOnly.characteristics());
    assertEquals(numElements, startTypeOnly.estimateSize());

    Spliterator<Object> superClassesOnly = new HierarchicalSpliterator<>(t, true, false, false, f);
    assertEquals(SuperHierarchySpliterator.DEFAULT_CHARACTERISTICS, superClassesOnly.characteristics());
    assertEquals(Long.MAX_VALUE, superClassesOnly.estimateSize());

    Spliterator<Object> superInterfacesOnly = new HierarchicalSpliterator<>(t, false, true, false, f);
    assertEquals(SuperHierarchySpliterator.DEFAULT_CHARACTERISTICS, superInterfacesOnly.characteristics());
    assertEquals(Long.MAX_VALUE, superInterfacesOnly.estimateSize());

    Spliterator<Object> empty = new HierarchicalSpliterator<>(t, false, false, false, f);
    assertEquals(SuperHierarchySpliterator.DEFAULT_CHARACTERISTICS | Spliterator.SIZED | Spliterator.SUBSIZED, empty.characteristics());
    assertEquals(0, empty.estimateSize());
    assertFalse(empty.tryAdvance(o -> {
    }));

    Spliterator<IType> selfOnly = new SuperTypeHierarchySpliterator(t, false, false, true);
    assertEquals(SuperHierarchySpliterator.DEFAULT_CHARACTERISTICS | Spliterator.SIZED | Spliterator.SUBSIZED, selfOnly.characteristics());
    assertEquals(1, selfOnly.estimateSize());
  }
}
