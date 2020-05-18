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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.junit.jupiter.api.Test;

public class PropertiesGeneratorTest {

  @Test
  public void testCreateEmpty() {
    PropertiesGenerator generator = PropertiesGenerator.create();
    assertTrue(generator.headerLines().isEmpty());
    assertTrue(generator.properties().isEmpty());
  }

  @Test
  public void testPropertiesEncode() {
    String key = "key";
    String value = "ö\nä\r\nü";
    Map<String, String> values = singletonMap(key, value);
    PropertiesGenerator generator = PropertiesGenerator.create(values);
    String generatedSource = generator.toSource(identity(), new BuilderContext()).toString();
    assertEquals("key=\\u00F6\\n\\u00E4\\r\\n\\u00FC\n", generatedSource);
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsWithItself", "EqualsBetweenInconvertibleTypes"})
  public void testGenerator() throws IOException {
    String[] prop1 = new String[]{"prop1", "1"};
    String[] prop2 = new String[]{"a-prop2", "2"};
    String[] lines = new String[]{
        "# comment line 1",
        "!comment line 2",
        "  ",
        prop1[0] + "=" + prop1[1],
        prop2[0] + "=" + prop2[1]
    };

    String origContent = Stream.of(lines).collect(joining(lineSeparator()));
    PropertiesGenerator generator = PropertiesGenerator.create(new ByteArrayInputStream(origContent.getBytes(PropertiesGenerator.ENCODING)));

    // test content (parse)
    assertEquals(Arrays.asList(lines[0], lines[1], lines[2]), generator.headerLines());
    Map<String, String> expected = new HashMap<>();
    expected.put(prop1[0], prop1[1]);
    expected.put(prop2[0], prop2[1]);
    assertEquals(expected, generator.properties());

    // test write
    String generatedSource = generator.toSource(identity(), new BuilderContext()).toString();
    String expectedSource = lines[0] + "\n" +
        lines[1] + "\n" +
        lines[2] + "\n" +
        lines[4] + "\n" +
        lines[3] + "\n";
    assertEquals(expectedSource, generatedSource);

    // test compares
    PropertiesGenerator generator2 = PropertiesGenerator.create(generator.properties(), generator.headerLines());
    assertEquals(generator, generator2);
    assertEquals(generator.hashCode(), generator2.hashCode());
    assertTrue(generator.equals(generator));
    assertFalse(generator.equals(""));
  }
}
