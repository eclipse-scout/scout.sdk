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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.s.nls.query.TranslationKeysQueryTest.searchIn;
import static org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns.HtmlScoutMessagePattern.textToNextNewLine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension;
import org.eclipse.scout.sdk.core.s.nls.Translations;
import org.eclipse.scout.sdk.core.s.nls.Translations.DependencyScope;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class))
public class MissingTranslationQueryTest {

  @Test
  public void testWithTextServicesAvailable() {
    var existingKey = "key";

    var htmlTestFile = "test.html";
    var htmlTestFile4 = "test4.html";
    var javaTestFile2 = "test2.java";
    var javaTestFile3 = "test3.java";
    var javaTestFile4 = "test4.java";
    var javaTestFileWithNonExistingConstant = "test7.java";
    var javaTestFileWithUnresolvableConstant = "test8.java";
    var javaNonExistingVariableTestFile = "test10.java";
    var javaTestFileWithSuffix = "test12.java";

    var jsTestFile1 = "test1.js";
    var javaTestFile1 = "test1.java";
    var jsTestFile2 = "test2.js";
    var jsTestFileWithNonExistingVariable = "test6.js";
    var jsTestFileWithUnresolvableConstant = "test8.js";

    var jsTestFile15 = "test15.js";
    var jsTestFile16 = "test16.js";
    var jsTestFile17 = "test17.js";
    var jsTestFile18 = "test18.js";

    var query = createQueryWithKeys(new String[]{existingKey}, existingKey);

    searchIn(query, javaTestFile1, "abc TEXTS.get(\"aa\") def"); // finding
    searchIn(query, javaTestFile2, "abc TEXTS.get(locale, \"bb\") def"); // finding
    searchIn(query, javaTestFile3, "abc TEXTS.get(\"cc\", arg1, arg2) def"); // finding
    searchIn(query, javaTestFile4, "abc TEXTS.get(locale, \"dd\", arg1, arg2) def"); // finding
    searchIn(query, "test5.java", "abc TEXTS.get(\"" + existingKey + "\") def"); // ignored because key exists
    searchIn(query, "test6.java", "public static final String CONSTANT = \"" + existingKey + "\";\nTEXTS.get(CONSTANT) def"); // ignored because key exists
    searchIn(query, javaTestFileWithNonExistingConstant, "public static final String CONSTANT = \"non_existing\"; TEXTS.get(CONSTANT) def"); // finding
    searchIn(query, javaTestFileWithUnresolvableConstant, "abc TEXTS.get(flag ? \"first\" : \"second\") def"); // ignored because with business logic
    searchIn(query, "test9.java", "String var = \"" + existingKey + "\";\nTEXTS.get(var) def"); // ignored because key exists
    searchIn(query, javaNonExistingVariableTestFile, "String varName = \"non_existing\"; TEXTS.get(varName) def"); // finding
    searchIn(query, "test11.java", "abc \"TEXTS.get(locale, \"bb\")\" def"); // ignored because in string
    searchIn(query, javaTestFileWithSuffix, "abc TEXTS.get(locale, \"bb\" + suffixVariable) def"); // weak finding
    searchIn(query, "test13.java", "abc TEXTS.get(\"bb\" + suffixVariable) def"); // ignore because of suffix concatenation

    searchIn(query, jsTestFile1, "abc '${textKey:ee}' def"); // finding
    searchIn(query, jsTestFile2, "abc session.text('ff') def"); // finding
    searchIn(query, "test3.js", "abc session.text('gg') def " + TranslationPatterns.IGNORE_MARKER); // ignore by marker
    searchIn(query, "test4.js", "abc session.text('hh') def " + TranslationPatterns.IGNORE_MARKER + "\nnextline"); // ignore by marker
    searchIn(query, "test5.js", "abc session.text('ii') def " + TranslationPatterns.IGNORE_MARKER + "\r\nnextline"); // ignore by marker
    searchIn(query, jsTestFileWithNonExistingVariable, "abc session.text(variable) def"); // weak finding
    searchIn(query, "test7.js", "let variable = '" + existingKey + "'; abc; session.text(variable)} def"); // ignored because key exists
    searchIn(query, jsTestFileWithUnresolvableConstant, "let variable = 'non_existing'; abc; session.text(variable)} def"); // finding
    searchIn(query, "test9.js", "abc 'session.text(\\'ii\\')}' def \r\nnextline"); // ignore because in string
    searchIn(query, "test10.js", "abc /*session.text('ii') */def \r\nnextline"); // ignore because in comment
    searchIn(query, "test11.js", "abc session.text('ii'+suffix) def \r\nnextline"); // ignore because of suffix concatenation
    searchIn(query, "test12.js", "abc session.text('" + existingKey + "', param, param2) def \r\nnextline"); // ignored because key exists
    searchIn(query, "test13.js", "abc ${textKey:ee} def"); // ignored because json pattern is not in string
    searchIn(query, "test14.js", "abc '${textKey:ee}' def // " + TranslationPatterns.IGNORE_MARKER); // ignored by marker
    searchIn(query, jsTestFile15, "abc \"${textKey:ee}\" def"); // finding
    searchIn(query, jsTestFile16, "abc session.text(\"ff\") def"); // finding
    searchIn(query, jsTestFile17, "abc `${textKey:ee}` def"); // finding
    searchIn(query, jsTestFile18, "abc session.text(`ff`) def"); // finding

    searchIn(query, htmlTestFile, "abc <scout:message key=\"jj\"> def"); // finding
    searchIn(query, "test2.html", "abc <scout:message key=\"jj\"> def <!-- " + TranslationPatterns.IGNORE_MARKER + " -->"); // ignored by marker
    searchIn(query, "test3.html", "abc <scout:message akey=\"jj\"> def"); // ignored because wrong attribute
    searchIn(query, htmlTestFile4, "abc <scout:message key='jj'> def"); // finding
    searchIn(query, "test.xml", "abc <scout:message key=\"kk\"> def"); // ignore because of file type
    searchIn(query, "abc/src/main/resources/archetype-resources/test.html", "abc <scout:message key=\"ll\"> def"); // path ignored

    // verify all findings
    assertEquals(Stream.of(
        javaTestFile1,
        javaTestFile2,
        javaTestFile3,
        javaTestFile4,
        javaTestFileWithNonExistingConstant,
        javaNonExistingVariableTestFile,
        javaTestFileWithSuffix,
        jsTestFile1,
        jsTestFile2,
        jsTestFileWithNonExistingVariable,
        jsTestFileWithUnresolvableConstant,
        jsTestFile15,
        jsTestFile16,
        jsTestFile17,
        jsTestFile18,
        htmlTestFile,
        htmlTestFile4)
        .sorted()
        .collect(toList()),
        query.result()
            .map(FileRange::file)
            .map(Path::toString)
            .sorted()
            .collect(toList()));

    // verify weak findings
    assertEquals(Stream.of(javaTestFileWithSuffix, jsTestFileWithNonExistingVariable)
        .sorted()
        .collect(toList()),
        query.result()
            .filter(r -> r.severity() <= Level.INFO.intValue())
            .map(FileRange::file)
            .map(Path::toString)
            .sorted()
            .collect(toList()));

    assertEquals(0, query.result(Paths.get("notexisting")).size());
    assertFileRange(query, javaTestFileWithSuffix, 14, 20, Level.INFO.intValue());
    assertFileRange(query, javaTestFileWithNonExistingConstant, 39, 51, Level.WARNING.intValue());
    assertFileRange(query, javaNonExistingVariableTestFile, 18, 30, Level.WARNING.intValue());
    assertFileRange(query, jsTestFileWithUnresolvableConstant, 16, 28, Level.WARNING.intValue());
    assertFileRange(query, htmlTestFile, 24, 26, Level.WARNING.intValue());
  }

  @Test
  public void testWithNoTextServiceAvailable(TestingEnvironment env) {
    // if no service is found: all keys should be accepted because they are considered to not be part of a Scout module
    var query = createQuery(env);
    searchIn(query, "test1.java", "TEXTS.get(\"aa\")");
    assertEquals(0, query.result().count());
  }

  @Test
  public void testTextToNextNewLine() {
    assertEquals("abc", textToNextNewLine("abc\ndd", 0));
    assertEquals("", textToNextNewLine("abc\ndd\r\nxx", 3));
    assertEquals("dd", textToNextNewLine("abc\ndd\r\nxxdd", 4));
    assertEquals("xdd", textToNextNewLine("abc\ndd\r\nxxdd", 9));
  }

  protected static void assertFileRange(MissingTranslationQuery query, String fileName, int expectedStart, int expectedEnd, int expectedSeverity) {
    var htmlFileResult = query.result(Paths.get(fileName));
    assertEquals(1, htmlFileResult.size());

    var finding = htmlFileResult.iterator().next();
    assertEquals(expectedStart, finding.start());
    assertEquals(expectedEnd, finding.end());
    assertEquals(expectedSeverity, finding.severity());
  }

  @Test
  @ExtendWith(TranslationStoreSupplierExtension.class)
  public void testScoutJsModuleKeys(TestingEnvironment env) {
    var query = createQuery(env);
    searchIn(query, "test1.js", "abc; session.text('ui.notExisting.key');", ScoutFixtureHelper.NLS_TEST_DIR);
    searchIn(query, "test2.js", "abc; session.text('" + TranslationStoreSupplierExtension.TRANSLATION_KEY_1 + "');", ScoutFixtureHelper.NLS_TEST_DIR); // exists in the ui contributor (referenced text service)
    searchIn(query, "test3.js", "var test = '" + TranslationStoreSupplierExtension.TRANSLATION_KEY_1 + "'; abc; session.text(test);", ScoutFixtureHelper.NLS_TEST_DIR); // exists in the ui contributor (literal)
    searchIn(query, "test4.js", "var test = 'ui.does.not.exist.key'; abc; session.text(test);", ScoutFixtureHelper.NLS_TEST_DIR);
    searchIn(query, "test5.js", "session.text('ui.from');"); // is not in the scout js module

    assertEquals(Stream.of("test1.js", "test4.js", "test5.js")
        .sorted()
        .collect(toList()),
        query.result()
            .map(FileRange::file)
            .map(Path::toString)
            .sorted()
            .collect(toList()));
  }

  protected static MissingTranslationQuery createQuery(TestingEnvironment env) {
    return new MissingTranslationQuery((p, s) -> Translations.storesForModule(p, env, new NullProgress(), s).collect(toList()));
  }

  protected static MissingTranslationQuery createQueryWithKeys(String[] javaKeys, String... nodeKeys) {
    var spy = Mockito.spy(new MissingTranslationQuery((p, s) -> emptyList()));
    Set<String> existingJavaKeys = new HashSet<>(Arrays.asList(javaKeys));
    Set<String> existingNodeKeys = new HashSet<>(Arrays.asList(nodeKeys));
    doReturn(Optional.of(existingJavaKeys)).when(spy).accessibleKeysForModule(any(), eq(DependencyScope.JAVA));
    doReturn(Optional.of(existingNodeKeys)).when(spy).accessibleKeysForModule(any(), eq(DependencyScope.NODE));
    return spy;
  }
}
