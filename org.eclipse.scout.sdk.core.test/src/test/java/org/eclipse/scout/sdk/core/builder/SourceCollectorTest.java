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
package org.eclipse.scout.sdk.core.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link SourceCollectorTest}</h3>
 *
 * @since 6.1.0
 */
public class SourceCollectorTest {
  @Test
  public void testSourceCollectorMultiElement() {
    assertEquals("123456", collectWithMultiElements("", "", ""));
    assertEquals("123456", collectWithMultiElements(null, null, null));
    assertEquals("1 2 3 4 5 6", collectWithMultiElements(null, " ", null));
    assertEquals(" 1 2 3 4 5 6 ", collectWithMultiElements(" ", " ", " "));
    assertEquals("a1b2b3b4b5b6c", collectWithMultiElements("a", "b", "c"));
  }

  @Test
  public void testSourceCollectorSingleElement() {
    assertEquals("1", collectWithSingleElement("", "", ""));
    assertEquals("1", collectWithSingleElement(null, null, null));
    assertEquals("1", collectWithSingleElement(null, " ", null));
    assertEquals(" 1 ", collectWithSingleElement(" ", " ", " "));
    assertEquals("a1c", collectWithSingleElement("a", "b", "c"));
  }

  @Test
  public void testSourceCollectorEmptyElement() {
    assertEquals("", collectWithEmptyElement("", "", ""));
    assertEquals("", collectWithEmptyElement(null, null, null));
    assertEquals("", collectWithEmptyElement(null, " ", null));
    assertEquals("", collectWithEmptyElement(" ", " ", " "));
    assertEquals("", collectWithEmptyElement("a", "b", "c"));
  }

  protected static String collectWithEmptyElement(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    return collectWithData(Stream.empty(), prefix, delimiter, suffix);
  }

  protected static String collectWithSingleElement(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    return collectWithData(Stream.of(b -> b.append("1")), prefix, delimiter, suffix);
  }

  protected static String collectWithMultiElements(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    Stream<ISourceGenerator<ISourceBuilder<?>>> generators = Stream.of("1", "2", "3", "4", "5", "6")
        .map(s -> builder -> builder.append(s));
    return collectWithData(generators, prefix, delimiter, suffix);
  }

  protected static String collectWithData(Stream<ISourceGenerator<ISourceBuilder<?>>> generators, CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    var out = new MemorySourceBuilder();
    out.append(generators, prefix, delimiter, suffix);
    return out.source().toString();
  }
}
