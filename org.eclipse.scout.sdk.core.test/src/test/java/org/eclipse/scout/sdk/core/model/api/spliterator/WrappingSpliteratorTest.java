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
package org.eclipse.scout.sdk.core.model.api.spliterator;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.junit.jupiter.api.Test;

public class WrappingSpliteratorTest {

  @Test
  public void testParallelWrapping() {
    assertEquals(emptySet(), collect(0, 0));
    assertEquals(new HashSet<>(Arrays.asList("0", "1", "2", "3")), collect(4, 0));
    assertEquals(new HashSet<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6")), collect(7, 0));
    assertEquals(emptySet(), collect(0, 1));
    assertEquals(new HashSet<>(Arrays.asList("2", "3")), collect(4, 2));
    assertEquals(new HashSet<>(Arrays.asList("3", "4", "5", "6", "7", "8", "9")), collect(10, 3));
  }

  @Test
  public void testSmallSplit() {
    Spliterator<IJavaElement> orig = new WrappingSpliterator<>(createList(2));
    var split = orig.trySplit();

    var ref = new AtomicReference<String>();
    assertTrue(orig.tryAdvance(t -> ref.set(t.elementName())));
    assertEquals("0", ref.get());
    assertFalse(orig.tryAdvance(t -> {
    }));
    assertNull(orig.trySplit());

    assertTrue(split.tryAdvance(t -> ref.set(t.elementName())));
    assertEquals("1", ref.get());
    assertFalse(split.tryAdvance(t -> {
    }));
    assertNull(split.trySplit());
  }

  protected static Set<String> collect(int numElements, int numAdvance) {
    Spliterator<IJavaElement> spliterator = new WrappingSpliterator<>(createList(numElements));
    for (var i = 0; i < numAdvance; i++) {
      spliterator.tryAdvance(e -> {
      });
    }

    return StreamSupport.stream(spliterator, true)
        .map(IJavaElement::elementName)
        .collect(toSet());
  }

  private static List<JavaElementSpi> createList(int num) {
    List<JavaElementSpi> result = new ArrayList<>(num);
    for (var i = 0; i < num; i++) {
      var api = mock(IJavaElement.class);
      var spi = mock(JavaElementSpi.class);
      when(api.unwrap()).thenReturn(spi);
      when(spi.wrap()).thenReturn(api);
      when(api.elementName()).thenReturn(Integer.toString(i));
      result.add(spi);
    }
    return result;
  }
}
