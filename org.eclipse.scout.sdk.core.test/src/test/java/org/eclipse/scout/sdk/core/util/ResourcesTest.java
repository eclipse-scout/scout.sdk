/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.util;

import static org.eclipse.scout.sdk.core.util.Resources.toSimple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class ResourcesTest {
  @Test
  public void testHttpGet() throws IOException {
    var uri = "https://www.eclipse.org/scout/";
    try (var in = Resources.httpGet(uri)) {
      assertTrue(in.read() >= 0);
    }
    try (var in = Resources.openStream(uri)) {
      assertTrue(in.read() >= 0);
    }
    var url = new URL(uri);
    try (var in = Resources.openStream(url)) {
      assertTrue(in.read() >= 0);
    }
    try (var in = Resources.httpGet(url)) {
      assertTrue(in.read() >= 0);
    }
  }

  @Test
  public void testToSimple() throws URISyntaxException {
    assertEquals("https://test.ch/scout/index.html", toSimple(new URI("https://test.ch/scout/index.html")));
    assertEquals("https://test.ch/scout/index.html (url shortened)", toSimple(new URI("https://test.ch/scout/index.html?a=b&c=d#xyz")));
  }

  @Test
  public void testFile() throws IOException {
    var tempFile = Files.createTempFile("scout-sdk-ResourcesTest", ".tmp");
    var content = "test-contentäöu";
    var bytes = content.getBytes(StandardCharsets.UTF_8);
    Files.write(tempFile, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    try (var in = Resources.openStream(tempFile.toUri())) {
      var buf = new byte[bytes.length * 2];
      var read = in.read(buf);
      var fileContent = new String(buf, 0, read, StandardCharsets.UTF_8);
      assertEquals(content, fileContent);
    }
    finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testFailure() {
    // not existing host (connect exception)
    assertThrows(IOException.class, () -> {
      try (var in = Resources.httpGet("https://www.eclipse.orgggg/scout-not-existing-xyzabc/")) {
        fail("Cannot be found " + in);
      }
    });

    // there is a response (eclipse 404 page), but error status code
    assertThrows(IOException.class, () -> {
      try (var in = Resources.httpGet("https://www.eclipse.org/scout-not-existing-xyzabc/")) {
        fail("Cannot be found " + in);
      }
    });
  }
}
