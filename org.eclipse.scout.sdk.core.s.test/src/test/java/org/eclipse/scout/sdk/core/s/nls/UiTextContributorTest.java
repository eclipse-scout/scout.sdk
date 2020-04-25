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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import nls.TestUiTextContributor;

@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWith(TranslationStoreSupplierExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class UiTextContributorTest {
  @Test
  public void testContributorKeys(TestingEnvironment env) {
    UiTextContributor contributor = new UiTextContributor(env.findType(TestUiTextContributor.class.getName()).findAny().get());
    contributor.load(new NullProgress());

    Set<String> keys = contributor.keys().collect(Collectors.toSet());
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
    UiTextContributor contributor1 = new UiTextContributor(env.findType(TestUiTextContributor.class.getName()).findAny().get());
    contributor1.load(new NullProgress());
    UiTextContributor contributor2 = new UiTextContributor(env.findType(TestUiTextContributor.class.getName()).findAny().get());
    contributor1.load(new NullProgress());
    UiTextContributor contributor3 = new UiTextContributor(env.findType(Long.class.getName()).findAny().get());
    contributor1.load(new NullProgress());

    assertEquals(UiTextContributor.class.getSimpleName() + " [" + TestUiTextContributor.class.getName() + "]", contributor1.toString());
    assertFalse(contributor1.equals(null));
    assertTrue(contributor1.equals(contributor1));
    assertFalse(contributor1.equals(""));
    assertTrue(contributor1.equals(contributor2));
    assertFalse(contributor1.equals(contributor3));
  }
}
