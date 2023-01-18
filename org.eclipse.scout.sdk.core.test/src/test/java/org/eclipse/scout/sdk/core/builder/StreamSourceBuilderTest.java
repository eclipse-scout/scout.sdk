/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link StreamSourceBuilderTest}</h3>
 *
 * @since 9.0.0
 */
public class StreamSourceBuilderTest {

  @Test
  public void testStreamSourceBuilderBuffered() {
    var out = new ByteArrayOutputStream();

    try (var builder = new StreamSourceBuilder(out)) {
      builder.append("My test string");
      builder.append("\n// comment tes".toCharArray());
      builder.append('t');
      assertEquals(0, out.toByteArray().length); // the generated content must not yet be flushed to the stream -> buffered
    }

    var generated = out.toString(StandardCharsets.UTF_8); // here the output is flushed and available
    Assertions.assertEquals("Myteststring//commenttest", CoreTestingUtils.removeWhitespace(generated));
  }
}
