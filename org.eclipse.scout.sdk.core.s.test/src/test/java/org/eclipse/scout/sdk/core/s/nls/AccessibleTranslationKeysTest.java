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
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.Arrays.asList;
import static org.eclipse.scout.sdk.core.s.nls.AccessibleTranslationKeys.findVisibleUiTextContributorNames;
import static org.eclipse.scout.sdk.core.s.nls.AccessibleTranslationKeys.loadKeysForJsModule;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.keysAccessibleForModule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWith(TranslationStoreSupplierExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class AccessibleTranslationKeysTest {

  @Test
  public void testLoadKeysForJsModule(TestingEnvironment env) {
    Set<String> keysVisibleForJs = loadKeysForJsModule(ScoutFixtureHelper.NLS_TEST_DIR, env, new NullProgress()).get().collect(Collectors.toSet());
    assertEquals(new HashSet<>(asList(
        TranslationStoreSupplierExtension.TRANSLATION_KEY_1,
        TranslationStoreSupplierExtension.TRANSLATION_KEY_2,
        TranslationStoreSupplierExtension.TRANSLATION_KEY_3,
        "testKey1FromUiContributor",
        "testKey2FromUiContributor",
        "testKey3FromUiContributor",
        "testKey4FromUiContributor",
        "testKey5FromUiContributor",
        "testKey6FromUiContributor")), keysVisibleForJs);
  }

  @Test
  public void testAccessibleTranslationKeysMethods(TestingEnvironment env) {
    AccessibleTranslationKeys keys = keysAccessibleForModule(ScoutFixtureHelper.NLS_TEST_DIR, env, new NullProgress()).get();
    assertSame(ScoutFixtureHelper.NLS_TEST_DIR, keys.modulePath());
    assertEquals(3, keys.forJava().size());
    assertEquals(9, keys.forJs().size());
    assertEquals(9, keys.all().size());
    assertEquals(3, keys.forFile(Paths.get("test.html")).size());
    assertEquals(9, keys.forFile(Paths.get("test.js")).size());
    assertEquals(3, keys.forFile(Paths.get("test.java")).size());
    assertEquals(9, keys.forFile(Paths.get("test.json")).size());
    assertEquals(0, keys.forFile(Paths.get("test.xml")).size());
  }

  @Test
  public void testVisibleUiTextContributorNamesReturnsEmptyIfNothingFound() {
    assertFalse(findVisibleUiTextContributorNames("").isPresent());
  }
}
