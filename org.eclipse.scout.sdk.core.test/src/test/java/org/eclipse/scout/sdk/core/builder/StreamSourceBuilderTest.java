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

import static org.eclipse.scout.sdk.core.testing.CoreTestingUtils.removeWhitespace;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.util.JavaTypes;
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
      PrimaryTypeGenerator.create()
          .withElementName("TestClass")
          .withMethod(MethodGenerator.create()
              .asPublic()
              .asStatic()
              .withElementName("testMethod")
              .withReturnType(JavaTypes._boolean))
          .generate(builder);
      builder.append("\n// comment tes".toCharArray());
      builder.append('t');
      assertEquals(0, out.toByteArray().length); // the generated content must not yet be flushed to the stream -> buffered
    }

    var generated = out.toString(StandardCharsets.UTF_8); // here the output is flushed and available
    assertEquals("publicclassTestClass{publicstaticbooleantestMethod(){}}//commenttest", removeWhitespace(generated));
  }
}
