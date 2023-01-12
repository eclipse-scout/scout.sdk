/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls.query;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.s.util.search.IFileQuery;
import org.junit.jupiter.api.Test;

public class TranslationKeysQueryTest {

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "EqualsBetweenInconvertibleTypes"})
  public void testWithSingleKey() {
    var keyToFind1 = "keyToFind1";
    var fileName = "test.java";
    var prefix = "abc";
    var suffix = "abcd";

    var query = new TranslationKeysQuery();
    searchIn(query, fileName, String.join("\"", prefix, keyToFind1, suffix));
    assertEquals(1, query.result().count());

    var expectedStart = prefix.length() + 1; /* leading '"' */
    var expectedEnd = expectedStart + keyToFind1.length();
    var finding = query.result(keyToFind1).findAny().orElseThrow();
    assertEquals(fileName, finding.file().toString());
    assertEquals(keyToFind1.length(), finding.length());
    assertEquals(expectedStart, finding.start());
    assertEquals(expectedEnd, finding.end());
    assertEquals("FileRange [file=test.java, text=" + keyToFind1 + ", start=" + expectedStart + ", end=" + expectedEnd + "]", finding.toString());

    assertEquals(finding, finding);
    assertFalse(finding.equals(null));
    assertFalse(finding.equals(""));
  }

  @Test
  public void testWithMultipleKeys() {
    var keyToFind1 = "keyToFind1";
    var keyToFind2 = "keyToFind2";
    var fileJava = "test.java";
    var fileJs = "test.js";
    var fileHtml = "test.html";
    var queryName = "testquery";
    var query = new TranslationKeysQuery(queryName);
    searchIn(query, "test.txt", "");
    searchIn(query, "test", "");
    searchIn(query, fileJava, "public class Test { public void test() { return TEXTS.get(\"" + keyToFind1 + "\"); } }");
    searchIn(query, fileJs, '\'' + keyToFind1 + "', '" + keyToFind2 + '\'');
    searchIn(query, fileHtml, "\"" + keyToFind2 + "\"");
    searchIn(query, "test2.html", "test content whatever html");
    searchIn(query, "test2.xml", "test content whatever xml \"" + keyToFind1 + '"');

    searchIn(query, "test2.js", "'${textKey:" + keyToFind1 + "}'");
    searchIn(query, "test3.js", "`${textKey:" + keyToFind1 + "}`");
    searchIn(query, "test4.js", "\"${textKey:" + keyToFind1 + "}\"");
    searchIn(query, "test5.js", '"' + keyToFind1 + '"');
    searchIn(query, "test6.js", '`' + keyToFind1 + '`');

    assertEquals(queryName, query.name());
    assertEquals(9, query.result().count());
    assertEquals(0, query.result(Paths.get("notexisting")).size());
    assertEquals(2, query.result(Paths.get("test.js")).size());
    assertEquals(2, query.keysFound().count());

    var files = query.result(keyToFind1)
        .map(FileRange::file)
        .map(Path::toString)
        .collect(toSet());

    assertEquals(7, files.size());
    assertTrue(files.contains(fileJs));
    assertTrue(files.contains(fileJava));

    var resultByKey = query.resultByKey();
    assertEquals(2, resultByKey.size());

    var find2 = resultByKey.get(keyToFind2).stream().map(FileRange::file).map(Path::toString).collect(toSet());
    assertTrue(find2.contains(fileHtml));
  }

  public static void searchIn(IFileQuery query, String fileName, String fileContent) {
    searchIn(query, fileName, fileContent, Paths.get("whatever"));
  }

  public static void searchIn(IFileQuery query, String fileName, String fileContent, Path modulePath) {
    query.searchIn(new FileQueryInput(Paths.get(fileName), modulePath, () -> fileContent));
  }

}
