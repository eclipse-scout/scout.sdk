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

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.s.nls.query.TranslationKeysQueryTest.searchIn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class))
public class MissingTranslationQueryTest {

  @Test
  public void testWithTextServicesAvailable(TestingEnvironment env) {
    String existingKey = "key";

    String htmlTestFile = "test.html";
    String javaTestFile2 = "test2.java";
    String javaTestFile3 = "test3.java";
    String javaTestFile4 = "test4.java";
    String javaTestFileWithNonExistingConstant = "test7.java";
    String javaTestFileWithUnresolvableConstant = "test8.java";
    String javaNonExistingVariableTestFile = "test10.java";
    String javaTestFileWithSuffix = "test12.java";

    String jsTestFile1 = "test1.js";
    String javaTestFile1 = "test1.java";
    String jsTestFile2 = "test2.js";
    String jsTestFileWithNonExistingVariable = "test6.js";
    String jsTestFileWithUnresolvableConstant = "test8.js";

    MissingTranslationQuery query = createQueryWithKeys(existingKey);

    searchIn(query, javaTestFile1, "abc TEXTS.get(\"aa\") def", env); // finding
    searchIn(query, javaTestFile2, "abc TEXTS.get(locale, \"bb\") def", env); // finding
    searchIn(query, javaTestFile3, "abc TEXTS.get(\"cc\", arg1, arg2) def", env); // finding
    searchIn(query, javaTestFile4, "abc TEXTS.get(locale, \"dd\", arg1, arg2) def", env); // finding
    searchIn(query, "test5.java", "abc TEXTS.get(\"" + existingKey + "\") def", env); // ignored because key exists
    searchIn(query, "test6.java", "public static final String CONSTANT = \"" + existingKey + "\";\nTEXTS.get(CONSTANT) def", env); // ignored because key exists
    searchIn(query, javaTestFileWithNonExistingConstant, "public static final String CONSTANT = \"non_existing\"; TEXTS.get(CONSTANT) def", env); // finding
    searchIn(query, javaTestFileWithUnresolvableConstant, "abc TEXTS.get(flag ? \"first\" : \"second\") def", env); // ignored because with business logic
    searchIn(query, "test9.java", "String var = \"" + existingKey + "\";\nTEXTS.get(var) def", env); // ignored because key exists
    searchIn(query, javaNonExistingVariableTestFile, "String varName = \"non_existing\"; TEXTS.get(varName) def", env); // finding
    searchIn(query, "test11.java", "abc \"TEXTS.get(locale, \"bb\")\" def", env); // ignored because in string
    searchIn(query, javaTestFileWithSuffix, "abc TEXTS.get(locale, \"bb\" + suffixVariable) def", env); // weak finding
    searchIn(query, "test13.java", "abc TEXTS.get(\"bb\" + suffixVariable) def", env); // ignore because of suffix concatenation

    searchIn(query, jsTestFile1, "abc '${textKey:ee}' def", env); // finding
    searchIn(query, jsTestFile2, "abc session.text('ff') def", env); // finding
    searchIn(query, "test3.js", "abc session.text('gg') def " + MissingTranslationQuery.IGNORE_MARKER, env); // ignore by marker
    searchIn(query, "test4.js", "abc session.text('hh') def " + MissingTranslationQuery.IGNORE_MARKER + "\nnextline", env); // ignore by marker
    searchIn(query, "test5.js", "abc session.text('ii') def " + MissingTranslationQuery.IGNORE_MARKER + "\r\nnextline", env); // ignore by marker
    searchIn(query, jsTestFileWithNonExistingVariable, "abc session.text(variable) def", env); // weak finding
    searchIn(query, "test7.js", "let variable = '" + existingKey + "'; abc; session.text(variable)} def", env); // ignored because key exists
    searchIn(query, jsTestFileWithUnresolvableConstant, "let variable = 'non_existing'; abc; session.text(variable)} def", env); // finding
    searchIn(query, "test9.js", "abc 'session.text(\\'ii\\')}' def \r\nnextline", env); // ignore because in string
    searchIn(query, "test10.js", "abc /*session.text('ii') */def \r\nnextline", env); // ignore because in comment
    searchIn(query, "test11.js", "abc session.text('ii'+suffix) def \r\nnextline", env); // ignore because of suffix concatenation
    searchIn(query, "test12.js", "abc session.text('" + existingKey + "', param, param2) def \r\nnextline", env); // ignored because key exists
    searchIn(query, "test13.js", "abc ${textKey:ee} def", env); // ignored because json pattern is not in string
    searchIn(query, "test14.js", "abc '${textKey:ee}' def // " + MissingTranslationQuery.IGNORE_MARKER, env); // ignored by marker

    searchIn(query, htmlTestFile, "abc <scout:message key=\"jj\"> def", env); // finding
    searchIn(query, "test2.html", "abc <scout:message key=\"jj\"> def <!-- " + MissingTranslationQuery.IGNORE_MARKER + " -->", env); // ignored by marker
    searchIn(query, "test3.html", "abc <scout:message akey=\"jj\"> def", env); // ignored because wrong attribute
    searchIn(query, "test.xml", "abc <scout:message key=\"kk\"> def", env); // ignore because of file type
    searchIn(query, "abc/src/main/resources/archetype-resources/test.html", "abc <scout:message key=\"ll\"> def", env); // path ignored

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
        htmlTestFile)
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
    MissingTranslationQuery query = new MissingTranslationQuery();
    searchIn(query, "test1.java", "TEXTS.get(\"aa\")", env);
    assertEquals(0, query.result().count());
  }

  @Test
  @ExtendWith(TranslationStoreSupplierExtension.class)
  public void testScoutJsModuleKeys(TestingEnvironment env) {
    MissingTranslationQuery query = new MissingTranslationQuery();
    searchIn(query, "test1.js", "abc; session.text('ui.notExisting.key');", ScoutFixtureHelper.NLS_TEST_DIR, env);
    searchIn(query, "test2.js", "abc; session.text('" + TranslationStoreSupplierExtension.TRANSLATION_KEY_1 + "');", ScoutFixtureHelper.NLS_TEST_DIR, env); // exists in the ui contributor (referenced text service)
    searchIn(query, "test3.js", "var test = 'testKey1FromUiContributor'; abc; session.text(test);", ScoutFixtureHelper.NLS_TEST_DIR, env); // exists in the ui contributor (literal)
    searchIn(query, "test4.js", "var test = 'ui.does.not.exist.key'; abc; session.text(test);", ScoutFixtureHelper.NLS_TEST_DIR, env);
    searchIn(query, "test5.js", "session.text('ui.from');", env); // is not in the scout js module
    assertEquals(3, query.result().count());
  }

  protected static void assertFileRange(MissingTranslationQuery query, String fileName, int expectedStart, int expectedEnd, int expectedSeverity) {
    Set<FileRange> htmlFileResult = query.result(Paths.get(fileName));
    assertEquals(1, htmlFileResult.size());

    FileRange finding = htmlFileResult.iterator().next();
    assertEquals(expectedStart, finding.start());
    assertEquals(expectedEnd, finding.end());
    assertEquals(expectedSeverity, finding.severity());
  }

  protected static MissingTranslationQuery createQueryWithKeys(String... keys) {
    MissingTranslationQuery spy = Mockito.spy(MissingTranslationQuery.class);
    Set<String> existingKeys = new HashSet<>(Arrays.asList(keys));
    doReturn(Optional.of(existingKeys)).when(spy).accessibleKeysForModule(any(), any(), any());
    return spy;
  }
}
