/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;

import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TranslationStoreSupplierExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class WebModuleTranslationStoresTest {

  @Test
  public void testLoadKeysForJsModule(TestingEnvironment env) {
    var keysVisibleForJs = WebModuleTranslationStores
        .allForNodeModule(ScoutFixtureHelper.NLS_TEST_DIR, env, new NullProgress())
        .flatMap(ITranslationStore::keys)
        .collect(toSet());
    assertEquals(new HashSet<>(asList(
        TranslationStoreSupplierExtension.TRANSLATION_KEY_1,
        TranslationStoreSupplierExtension.TRANSLATION_KEY_2,
        TranslationStoreSupplierExtension.TRANSLATION_KEY_3)), keysVisibleForJs);
  }

  @Test
  public void testVisibleUiTextContributorNamesReturnsEmptyIfNothingFound() {
    assertEquals(0, WebModuleTranslationStores.getTextContributorsReferencedInPackageJson("").count());
  }
}
