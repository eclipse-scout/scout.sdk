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

import static org.eclipse.scout.sdk.core.util.Ensure.failOnDuplicates;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link EnsureTest}</h3>
 *
 * @since 6.1.0
 */
public class EnsureTest {
  @Test
  public void testInstanceOf() {
    var result = Ensure.instanceOf("", CharSequence.class);
    assertNotNull(result);
    Ensure.instanceOf("", String.class);

    assertEquals("a22b", assertThrows(IllegalArgumentException.class, () -> Ensure.instanceOf("", long.class, "a{}b", 22)).getMessage());
    assertThrows(IllegalArgumentException.class, () -> Ensure.instanceOf(null, long.class, "a{}b", 22));
    assertThrows(IllegalArgumentException.class, () -> Ensure.instanceOf("", null, "a{}b", 22));
  }

  @Test
  public void testIsFalse() {
    Ensure.isFalse(false);
    assertEquals("a22b", assertThrows(IllegalArgumentException.class, () -> Ensure.isFalse(true, "a{}b", 22)).getMessage());
  }

  @Test
  public void testIsTrue() {
    Ensure.isTrue(true);
    assertEquals("a22b", assertThrows(IllegalArgumentException.class, () -> Ensure.isTrue(false, "a{}b", 22)).getMessage());
  }

  @Test
  public void testSame() {
    var a = new Object();
    var b = new Object();
    Ensure.same(a, a);
    assertEquals("a22b", assertThrows(IllegalArgumentException.class, () -> Ensure.same(a, b, "a{}b", 22)).getMessage());
  }

  @Test
  public void testIsNotBlank() {
    Ensure.notBlank("a");
    assertEquals("a22b", assertThrows(IllegalArgumentException.class, () -> Ensure.notBlank(" ", "a{}b", 22)).getMessage());
  }

  @Test
  public void testNotNull() {
    Ensure.notNull(new Object());
    assertEquals("a22b", assertThrows(IllegalArgumentException.class, () -> Ensure.notNull(null, "a{}b", 22)).getMessage());
  }

  @Test
  public void testIsFile() throws IOException {
    var tempFile = Files.createTempFile("a", "b");
    try {
      Ensure.isFile(tempFile);
    }
    finally {
      Files.delete(tempFile);
    }

    assertEquals("a22b", assertThrows(IllegalArgumentException.class, () -> Ensure.isFile(Paths.get("a-not-existing-file"), "a{}b", 22)).getMessage());
    assertEquals("a22b", assertThrows(IllegalArgumentException.class, () -> Ensure.isFile(null, "a{}b", 22)).getMessage());
  }

  @Test
  public void testIsDirectory() throws IOException {
    var tempFile = Files.createTempFile("a", "b");
    try {
      Ensure.isDirectory(tempFile.getParent());
    }
    finally {
      Files.delete(tempFile);
    }

    assertEquals("a22b", assertThrows(IllegalArgumentException.class, () -> Ensure.isDirectory(Paths.get("a-not-existing-file"), "a{}b", 22)).getMessage());
    assertEquals("a22b", assertThrows(IllegalArgumentException.class, () -> Ensure.isDirectory(null, "a{}b", 22)).getMessage());
  }

  @Test
  public void testFailOnDuplicates() {
    assertThrows(IllegalArgumentException.class, () -> failOnDuplicates(null, null));
  }
}
