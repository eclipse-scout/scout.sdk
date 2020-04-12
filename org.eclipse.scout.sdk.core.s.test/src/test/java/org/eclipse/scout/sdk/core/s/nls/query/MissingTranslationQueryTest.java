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

import static org.eclipse.scout.sdk.core.s.nls.query.TranslationKeysQueryTest.searchIn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

@SuppressWarnings({"HardcodedFileSeparator", "HardcodedLineSeparator"})
@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class))
public class MissingTranslationQueryTest {

  @Test
  public void testWithTextServicesAvailable(TestingEnvironment env) {
    String existing = "key";
    String fileHtml = "test.html";
    String nonExistingConstantJavaTestFile = "test7.java";
    String unresolvableConstantJavaTestFile = "test8.java";
    String nonExistingVariableJavaTestFile = "test10.java";
    String unresolvableConstantJsTestFile = "test8.js";
    MissingTranslationQuery query = createQueryWithKeys(existing);

    searchIn(query, "test1.java", "abc TEXTS.get(\"aa\") def", env);
    searchIn(query, "test2.java", "abc TEXTS.get(locale, \"bb\") def", env);
    searchIn(query, "test3.java", "abc TEXTS.get(\"cc\", arg1, arg2) def", env);
    searchIn(query, "test4.java", "abc TEXTS.get(locale, \"dd\", arg1, arg2) def", env);
    searchIn(query, "test5.java", "abc TEXTS.get(\"" + existing + "\") def", env);
    searchIn(query, "test6.java", "public static final String CONSTANT = \"" + existing + "\";\nTEXTS.get(CONSTANT) def", env);
    searchIn(query, nonExistingConstantJavaTestFile, "public static final String CONSTANT = \"non_existing\"; TEXTS.get(CONSTANT) def", env);
    searchIn(query, unresolvableConstantJavaTestFile, "abc TEXTS.get(flag ? \"first\" : \"second\") def", env);
    searchIn(query, "test9.java", "String var = \"" + existing + "\";\nTEXTS.get(var) def", env);
    searchIn(query, nonExistingVariableJavaTestFile, "String varName = \"non_existing\"; TEXTS.get(varName) def", env);

    searchIn(query, "test1.js", "abc '${textKey:ee}' def", env);
    searchIn(query, "test2.js", "abc session.text('ff')} def", env);
    searchIn(query, "test3.js", "abc session.text('gg')} def " + MissingTranslationQuery.IGNORE_MARKER, env); // ignore by marker
    searchIn(query, "test4.js", "abc session.text('hh')} def " + MissingTranslationQuery.IGNORE_MARKER + "\nnextline", env); // ignore by marker
    searchIn(query, "test5.js", "abc session.text('ii')} def " + MissingTranslationQuery.IGNORE_MARKER + "\r\nnextline", env); // ignore by marker
    searchIn(query, "test6.js", "abc session.text(variable)} def", env);
    searchIn(query, "test7.js", "let variable = '" + existing + "'; abc; session.text(variable)} def", env);
    searchIn(query, unresolvableConstantJsTestFile, "let variable = 'non_existing'; abc; session.text(variable)} def", env);

    searchIn(query, fileHtml, "abc <scout:message key=\"jj\"> def", env);
    searchIn(query, "test.xml", "abc <scout:message key=\"kk\"> def", env);
    searchIn(query, "abc/src/main/resources/archetype-resources/test.html", "abc <scout:message key=\"ll\"> def", env); // path ignored

    assertEquals(12, query.result().count());
    assertEquals(0, query.result(Paths.get("notexisting")).size());

    assertFileRange(query, unresolvableConstantJavaTestFile, 14, 18, Level.INFO.intValue());
    assertFileRange(query, nonExistingConstantJavaTestFile, 39, 51, Level.WARNING.intValue());
    assertFileRange(query, nonExistingVariableJavaTestFile, 18, 30, Level.WARNING.intValue());
    assertFileRange(query, unresolvableConstantJsTestFile, 16, 28, Level.WARNING.intValue());
    assertFileRange(query, fileHtml, 24, 26, Level.WARNING.intValue());
  }

  @Test
  public void testWithNoTextServiceAvailable(TestingEnvironment env) {
    // if no service is found: all keys should be accepted because they are considered to not be part of a Scout module
    MissingTranslationQuery query = new MissingTranslationQuery();
    searchIn(query, "test1.java", "TEXTS.get(\"aa\")", env);
    assertEquals(0, query.result().count());
  }

  @Test
  public void testScoutJsModuleKeys(TestingEnvironment env) {
    MissingTranslationQuery query = createQueryWithKeys();
    searchIn(query, "test1.js", "abc; session.text('ui.notExisting.key');", MissingTranslationQuery.SCOUT_RT_JS_MODULE_PATH, env);
    searchIn(query, "test2.js", "abc; session.text('ui.Copy');", MissingTranslationQuery.SCOUT_RT_JS_MODULE_PATH, env); // exists in the ui contributor
    searchIn(query, "test3.js", "var test = 'ui.from'; abc; session.text(test);", MissingTranslationQuery.SCOUT_RT_JS_MODULE_PATH, env); // exists in the ui contributor
    searchIn(query, "test4.js", "var test = 'ui.does.not.exist.key'; abc; session.text(test);", MissingTranslationQuery.SCOUT_RT_JS_MODULE_PATH, env);
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
    doReturn(Optional.of(existingKeys)).when(spy).loadAllKeysForJavaModule(any(), any(), any());
    return spy;
  }
}
