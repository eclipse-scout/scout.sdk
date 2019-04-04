/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util;

import static org.eclipse.scout.sdk.core.util.CoreUtils.getParentURI;
import static org.eclipse.scout.sdk.core.util.CoreUtils.isDoubleDifferent;
import static org.eclipse.scout.sdk.core.util.CoreUtils.relativizeURI;
import static org.eclipse.scout.sdk.core.util.CoreUtils.removeComments;
import static org.eclipse.scout.sdk.core.util.CoreUtils.toStringIfOverwritten;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

public class CoreUtilsTest {

  @Test
  public void testRelativizeURI() {
    assertEquals("../../e/f/another.test", relativizeURI(URI.create("a/b/c/d/test.txt"), URI.create("a/b/e/f/another.test")).toString());
    assertEquals("sub/sub2", relativizeURI(URI.create("a/b/c/d/"), URI.create("a/b/c/d/sub/sub2")).toString());
    assertEquals("../../e/f/another.test", relativizeURI(URI.create("http://user:pw@host:port/a/b/c/d/test.txt"), URI.create("http://user:pw@host:port/a/b/e/f/another.test")).toString());
    assertEquals("sub/sub2", relativizeURI(URI.create("http://user:pw@host:port/a/b/c/d/"), URI.create("http://user:pw@host:port/a/b/c/d/sub/sub2")).toString());
    assertEquals("../../../../e/f/g", relativizeURI(URI.create("/a/b/c/d/test.txt"), URI.create("/e/f/g")).toString());
    assertEquals("/a/b/c/d", relativizeURI(URI.create("http://user:pw@host:port"), URI.create("http://user:pw@host:port/a/b/c/d")).toString());
    assertEquals("/a/b/c/d", relativizeURI(URI.create("http://user:pw@host:port/"), URI.create("http://user:pw@host:port/a/b/c/d")).toString());
    assertEquals("../../../a/b", relativizeURI(URI.create("http://user:pw@host:port/d/e/f/g"), URI.create("http://user:pw@host:port/a/b")).toString());
    assertEquals("../../../../a/b", relativizeURI(URI.create("http://user:pw@host:port/d/e/f/g/"), URI.create("http://user:pw@host:port/a/b")).toString());

    assertEquals("../", relativizeURI(URI.create("http://user:pw@host:port/g/h/i/j/k/"), URI.create("http://user:pw@host:port/g/h/i/j/")).toString());
    assertEquals("../", relativizeURI(URI.create("http://user:pw@host:port/g/h/i/j/k/"), URI.create("http://user:pw@host:port/g/h/i/j")).toString());
    assertEquals("", relativizeURI(URI.create("http://user:pw@host:port/g/h/i/j/k"), URI.create("http://user:pw@host:port/g/h/i/j")).toString());

    // dif scheme or authority
    String child1 = "http://user:pw@host2:port/a/b/e/f/another.test";
    assertEquals(child1, relativizeURI(URI.create("http://user:pw@host:port/a/b/c/d/test.txt"), URI.create(child1)).toString());
    String child2 = "http://user:pw@host2:port/a/b/c/d/sub/sub2";
    assertEquals(child2, relativizeURI(URI.create("http://user:pw@host:port/a/b/c/d/"), URI.create(child2)).toString());
    String child3 = "file://user:pw@host:port/a/b/c/d/sub/sub2";
    assertEquals(child3, relativizeURI(URI.create("http://user:pw@host:port/a/b/c/d/"), URI.create(child3)).toString());
  }

  @Test
  public void testToStringIfOverwritten() {
    BiConsumer<?, ?> op = (a, b) -> {
    };
    assertFalse(toStringIfOverwritten(op).isPresent());
    assertFalse(toStringIfOverwritten(new Object()).isPresent());
    assertFalse(toStringIfOverwritten(null).isPresent());
    assertFalse(toStringIfOverwritten("").isPresent());
    assertTrue(toStringIfOverwritten("test obj").isPresent());
  }

  @Test
  public void testGetParentURI() throws URISyntaxException {
    assertNull(getParentURI(null));
    assertEquals(new URI(""), getParentURI(new URI("")));
    assertEquals(new URI("http://www.test.com/"), getParentURI(new URI("http://www.test.com/myFile.txt")));
    assertEquals(new URI("http://www.test.com/sub1/sub2/"), getParentURI(new URI("http://www.test.com//sub1/sub2/myFile.txt")));
    assertEquals(new URI("http://www.test.com/sub1/"), getParentURI(new URI("http://www.test.com//sub1/sub2/")));
    assertEquals(new URI("one/two/three/"), getParentURI(new URI("one/two/three/four")));
    assertEquals(new URI("one/two/three/"), getParentURI(new URI("one/two/three/file.ext")));
    assertEquals(new URI(""), getParentURI(new URI("one")));
    assertEquals(new URI(""), getParentURI(new URI("one/")));
  }

  @Test
  public void testRemoveComments() {
    assertEquals("  ", removeComments("  "));
    assertNull(removeComments(null));
    assertEquals("first\nsecond", removeComments("first\n/* comment \n multi line \n */second"));
    assertEquals("first\nsecond", removeComments("first\n/* comment \n * multi line \n */second"));
    assertEquals("first second", removeComments("first /* comment \n multi line \n */second"));
    assertEquals("first\n\nsecond", removeComments("first\n/* comment \n multi line \n */\nsecond"));
    assertEquals("first\n \nsecond", removeComments("first\n// comment single line \n \nsecond"));
    assertEquals("first\n second", removeComments("first\n// comment single line \n second"));
  }

  @Test
  public void testIsDoubleDifferent() {
    assertTrue(isDoubleDifferent(1.1113d, 1.1115d, 0.0001d));
    assertTrue(isDoubleDifferent(1.0d, 2.0d, 0.9d));
    assertFalse(isDoubleDifferent(1.111d, 1.112d, 0.01d));
    assertFalse(isDoubleDifferent(-0.0d, 0.0d, 0.000000001d));
    assertFalse(isDoubleDifferent(-0.0d, 0.0d, 1.0d));
    assertFalse(isDoubleDifferent(-0.0d, 0.0d, 0.0d));

    // min/max values
    assertTrue(isDoubleDifferent(-Double.MAX_VALUE, Double.MAX_VALUE, 10000.0d));
    assertTrue(isDoubleDifferent(Double.MAX_VALUE, Double.MIN_VALUE, 10000.0d));
    assertTrue(isDoubleDifferent(Double.MIN_VALUE, Double.MAX_VALUE, 10000.0d));
    assertTrue(isDoubleDifferent(Double.MAX_VALUE, -Double.MAX_VALUE, 10000.0d));
    assertTrue(isDoubleDifferent(Double.MIN_VALUE, -Double.MIN_VALUE, 0.0d));
    assertFalse(isDoubleDifferent(Double.MAX_VALUE, Double.MAX_VALUE, 10000.0d));
    assertFalse(isDoubleDifferent(Double.MIN_VALUE, Double.MIN_VALUE, 10000.0d));
    assertFalse(isDoubleDifferent(-Double.MAX_VALUE, -Double.MAX_VALUE, 10000.0d));
    assertFalse(isDoubleDifferent(-Double.MIN_VALUE, -Double.MIN_VALUE, 10000.0d));

    // infinity comparisons
    assertFalse(isDoubleDifferent(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 10000.0d));
    assertFalse(isDoubleDifferent(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 10000.0d));
    assertTrue(isDoubleDifferent(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 10000.0d));
    assertTrue(isDoubleDifferent(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 10000.0d));
    assertTrue(isDoubleDifferent(Double.NEGATIVE_INFINITY, 100.0d, 10000.0d));
    assertTrue(isDoubleDifferent(Double.POSITIVE_INFINITY, 100.0d, 10000.0d));

    // NaN comparisons
    assertTrue(isDoubleDifferent(1.0d, Double.NaN, 1.0d));
    assertTrue(isDoubleDifferent(Double.NaN, 1.0d, 1.0d));
    assertFalse(isDoubleDifferent(Double.NaN, Double.NaN, 1.0d));
    assertFalse(isDoubleDifferent(Float.NaN, Float.NaN, Float.POSITIVE_INFINITY));
    assertFalse(isDoubleDifferent(Double.NaN, Double.NaN, Double.POSITIVE_INFINITY));
    assertFalse(isDoubleDifferent(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
  }

  @Test
  public void testDirectoryMoveAndDeleteSameFileSystem() throws IOException {
    Path folderToMove = Files.createTempDirectory("folderToMove");
    Path targetDirectory = Files.createTempDirectory("targetDir");

    try {
      String subDirs = "dir/anotherdir/whateverdir/";
      Path subFolder = folderToMove.resolve(subDirs);
      Files.createDirectories(subFolder);
      String fileName = "content.txt";
      Files.createFile(subFolder.resolve(fileName));
      assertTrue(Files.isDirectory(folderToMove));

      CoreUtils.moveDirectory(folderToMove, targetDirectory);

      assertFalse(Files.isDirectory(folderToMove));
      Path[] newContent = Files.list(targetDirectory).toArray(Path[]::new);
      assertEquals(1, newContent.length);
      Path movedDir = newContent[0];
      assertEquals(folderToMove.getFileName().toString(), movedDir.getFileName().toString());
      assertTrue(Files.isReadable(movedDir.resolve(subDirs + fileName)));
    }
    finally {
      CoreUtils.deleteDirectory(folderToMove);
      CoreUtils.deleteDirectory(targetDirectory);
    }
  }
}
