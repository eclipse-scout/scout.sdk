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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import nls.TestUiTextContributor;

@ExtendWith(TranslationStoreSupplierExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class UiTextContributorTest {
  @Test
  public void testContributorKeys(TestingEnvironment env) {
    var contributor = new UiTextContributor(env.findType(TestUiTextContributor.class.getName()).findAny().orElseThrow());
    contributor.load(new NullProgress());

    var keys = contributor.keys().collect(toSet());
    assertEquals(new HashSet<>(asList(
        TranslationStoreSupplierExtension.TRANSLATION_KEY_1,
        TranslationStoreSupplierExtension.TRANSLATION_KEY_2,
        TranslationStoreSupplierExtension.TRANSLATION_KEY_3,
        "testKey1FromUiContributor",
        "testKey2FromUiContributor",
        "testKey3FromUiContributor",
        "testKey4FromUiContributor")), keys);
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "EqualsWithItself", "EqualsBetweenInconvertibleTypes"})
  public void testToStringEquals(TestingEnvironment env) {
    var contributor1 = new UiTextContributor(env.findType(TestUiTextContributor.class.getName()).findAny().orElseThrow());
    contributor1.load(new NullProgress());
    var contributor2 = new UiTextContributor(env.findType(TestUiTextContributor.class.getName()).findAny().orElseThrow());
    contributor1.load(new NullProgress());
    var contributor3 = new UiTextContributor(env.findType(Long.class.getName()).findAny().orElseThrow());
    contributor1.load(new NullProgress());

    assertEquals(UiTextContributor.class.getSimpleName() + " [" + TestUiTextContributor.class.getName() + "]", contributor1.toString());
    assertFalse(contributor1.equals(null));
    assertTrue(contributor1.equals(contributor1));
    assertFalse(contributor1.equals(""));
    assertTrue(contributor1.equals(contributor2));
    assertFalse(contributor1.equals(contributor3));
  }
}
