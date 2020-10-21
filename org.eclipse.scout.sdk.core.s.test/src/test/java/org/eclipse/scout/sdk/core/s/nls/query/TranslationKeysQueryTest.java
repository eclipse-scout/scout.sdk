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
package org.eclipse.scout.sdk.core.s.nls.query;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.s.util.search.IFileQuery;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class TranslationKeysQueryTest {

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "EqualsBetweenInconvertibleTypes"})
  public void testWithSingleKey(TestingEnvironment env) {
    var keyToFind1 = "keyToFind1";
    var fileName = "test.java";
    var prefix = "abc";
    var suffix = "abcd";

    var query = new TranslationKeysQuery(keyToFind1, "testquery");
    searchIn(query, fileName, String.join("\"", prefix, keyToFind1, suffix), env);
    assertEquals(1, query.result().count());

    var expectedStart = prefix.length() + 1; /* leading '"' */
    var expectedEnd = expectedStart + keyToFind1.length();
    var finding = query.result(keyToFind1).findAny().get();
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
  public void testWithMultipleKeys(TestingEnvironment env) {
    var keyToFind1 = "keyToFind1";
    var keyToFind2 = "keyToFind2";
    var fileJava = "test.java";
    var fileJs = "test.js";
    var fileHtml = "test.html";

    var query = new TranslationKeysQuery(Arrays.asList(keyToFind1, keyToFind2), "testquery");
    searchIn(query, "test.txt", "", env);
    searchIn(query, "test", "", env);
    searchIn(query, fileJava, "public class Test { public void test() { return TEXTS.get(\"" + keyToFind1 + "\"); } }", env);
    searchIn(query, fileJs, '\'' + keyToFind1 + "', '" + keyToFind2 + '\'', env);
    searchIn(query, fileHtml, "\"" + keyToFind2 + "\"", env);
    searchIn(query, "test2.html", "test content whatever html", env);
    searchIn(query, "test2.xml", "test content whatever xml \"" + keyToFind1 + '"', env);

    searchIn(query, "test2.js", "'${textKey:" + keyToFind1 + "}'", env);
    searchIn(query, "test3.js", "`${textKey:" + keyToFind1 + "}`", env);
    searchIn(query, "test4.js", "\"${textKey:" + keyToFind1 + "}\"", env);
    searchIn(query, "test5.js", '"' + keyToFind1 + '"', env);
    searchIn(query, "test6.js", '`' + keyToFind1 + '`', env);

    assertEquals(9, query.result().count());
    assertEquals(0, query.result(Paths.get("notexisting")).size());
    assertEquals(2, query.result(Paths.get("test.js")).size());

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

  public static void searchIn(IFileQuery query, String fileName, String fileContent, IEnvironment env) {
    searchIn(query, fileName, fileContent, Paths.get("whatever"), env);
  }

  public static void searchIn(IFileQuery query, String fileName, String fileContent, Path modulePath, IEnvironment env) {
    query.searchIn(new FileQueryInput(Paths.get(fileName), modulePath, fileContent::toCharArray), env, new NullProgress());
  }
}
