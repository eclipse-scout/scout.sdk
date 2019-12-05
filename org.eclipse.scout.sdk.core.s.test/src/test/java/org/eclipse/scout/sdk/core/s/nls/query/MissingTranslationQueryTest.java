/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class MissingTranslationQueryTest {

  @Test
  @SuppressWarnings({"HardcodedLineSeparator", "HardcodedFileSeparator"})
  public void test(TestingEnvironment env) {
    String existing = "key";
    String fileHtml = "test.html";
    MissingTranslationQuery query = createQueryWithKeys(existing);

    searchIn(query, "test1.java", "abc TEXTS.get(\"aa\") def", env);
    searchIn(query, "test2.java", "abc TEXTS.get(locale, \"bb\") def", env);
    searchIn(query, "test3.java", "abc TEXTS.get(\"cc\", arg1, arg2) def", env);
    searchIn(query, "test4.java", "abc TEXTS.get(locale, \"dd\", arg1, arg2) def", env);
    searchIn(query, "test5.java", "abc TEXTS.get(\"" + existing + "\") def", env);

    searchIn(query, "test1.js", "abc '${textKey:ee}' def", env);
    searchIn(query, "test2.js", "abc session.text('ff')} def", env);
    searchIn(query, "test3.js", "abc session.text('gg')} def " + MissingTranslationQuery.IGNORE_MARKER, env); // ignore by marker
    searchIn(query, "test4.js", "abc session.text('hh')} def " + MissingTranslationQuery.IGNORE_MARKER + "\nnextline", env); // ignore by marker
    searchIn(query, "test5.js", "abc session.text('ii')} def " + MissingTranslationQuery.IGNORE_MARKER + "\r\nnextline", env); // ignore by marker
    searchIn(query, "test6.js", "abc session.text(variable)} def", env);

    searchIn(query, fileHtml, "abc <scout:message key=\"jj\"> def", env);
    searchIn(query, "test.xml", "abc <scout:message key=\"kk\"> def", env);
    searchIn(query, "abc/src/test/java/test.html", "abc <scout:message key=\"ll\"> def", env); // ignore path

    assertEquals(8, query.result().count());
    assertEquals(0, query.result(Paths.get("notexisting")).size());

    Set<FileRange> htmlFileResult = query.result(Paths.get(fileHtml));
    assertEquals(1, htmlFileResult.size());

    FileRange finding = htmlFileResult.iterator().next();
    assertEquals(24, finding.start());
    assertEquals(26, finding.end());
  }

  protected static MissingTranslationQuery createQueryWithKeys(String... keys) {
    Set<String> existingKeys = new HashSet<>(Arrays.asList(keys));
    return new MissingTranslationQuery() {
      @Override
      protected Set<String> keysVisibleForModule(Path modulePath, IEnvironment env, IProgress progress) {
        return existingKeys;
      }
    };
  }
}
