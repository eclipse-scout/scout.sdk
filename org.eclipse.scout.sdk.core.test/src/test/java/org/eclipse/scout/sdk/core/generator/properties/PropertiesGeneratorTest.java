/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.generator.properties;

import static java.lang.System.lineSeparator;
import static java.util.Collections.singletonMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.junit.jupiter.api.Test;

public class PropertiesGeneratorTest {

  @Test
  public void testCreateEmpty() {
    var generator = PropertiesGenerator.create();
    assertTrue(generator.headerLines().isEmpty());
    assertTrue(generator.properties().isEmpty());
  }

  @Test
  public void testPropertiesLatin1() {
    var key = "key";
    var value = "ö\nä\r\nü";
    var values = singletonMap(key, value);
    var generator = PropertiesGenerator.create(values).withEncoding(StandardCharsets.ISO_8859_1);
    var generatedSource = generator.toSource(identity(), new BuilderContext()).toString();
    assertEquals("key=\\u00F6\\n\\u00E4\\r\\n\\u00FC\n", generatedSource);
  }

  @Test
  public void testPropertiesUtf8() {
    var key = "key";
    var value = "ö\nä\r\nü";
    var values = singletonMap(key, value);
    var generator = PropertiesGenerator.create(values);
    var generatedSource = generator.toSource(identity(), new BuilderContext()).toString();
    assertEquals("key=" + "ö\\nä\\r\\nü\n", generatedSource);
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsWithItself", "EqualsBetweenInconvertibleTypes"})
  public void testGenerator() throws IOException {
    var prop1 = new String[]{"prop1", "1"};
    var prop2 = new String[]{"a-prop2", "2"};
    var lines = new String[]{
        "# comment line 1",
        "!comment line 2",
        "  ",
        prop1[0] + "=" + prop1[1],
        prop2[0] + "=" + prop2[1]
    };

    var origContent = Stream.of(lines).collect(joining(lineSeparator()));
    var generator = PropertiesGenerator.create(new ByteArrayInputStream(origContent.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

    // test content (parse)
    assertEquals(Arrays.asList(lines[0], lines[1], lines[2]), generator.headerLines());
    Map<String, String> expected = new HashMap<>();
    expected.put(prop1[0], prop1[1]);
    expected.put(prop2[0], prop2[1]);
    assertEquals(expected, generator.properties());

    // test write
    var generatedSource = generator.toSource(identity(), new BuilderContext()).toString();
    var expectedSource = IntStream.of(0, 1, 2, 4, 3).mapToObj(i -> lines[i]).collect(joining("\n")) + "\n";
    assertEquals(expectedSource, generatedSource);

    // test compares
    var generator2 = PropertiesGenerator.create(generator.properties(), generator.headerLines());
    assertEquals(generator, generator2);
    assertEquals(generator.hashCode(), generator2.hashCode());
    assertTrue(generator.equals(generator));
    assertFalse(generator.equals(""));
  }
}
