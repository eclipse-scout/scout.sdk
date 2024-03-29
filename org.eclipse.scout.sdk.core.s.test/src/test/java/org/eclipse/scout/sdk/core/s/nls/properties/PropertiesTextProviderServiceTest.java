/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.nls.TextProviderService;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

import formdata.shared.texts.SecondTestTextProviderService;
import formdata.shared.texts.TestTextProviderService;

/**
 * <h3>{@link PropertiesTextProviderServiceTest}</h3>
 *
 * @since 7.0.0
 */
@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class PropertiesTextProviderServiceTest {

  @Test
  public void testTextProviderService(IJavaEnvironment env) {
    var txtSvcType = env.requireType(TestTextProviderService.class.getName());
    var txtSvc = PropertiesTextProviderService.create(txtSvcType).orElseThrow();

    assertEquals("Prefix", txtSvc.filePrefix());
    assertEquals("formdata/shared/texts", txtSvc.folder());
    assertEquals(11.2, txtSvc.order(), 0.001);
    assertSame(txtSvcType, txtSvc.type());
  }

  @Test
  public void testResourceBundleGetterRegex() {
    assertTrue(PropertiesTextProviderService.REGEX_RESOURCE_BUNDLE_GETTER.matcher("return \"abc.def\";").matches());
    assertTrue(PropertiesTextProviderService.REGEX_RESOURCE_BUNDLE_GETTER.matcher("return\t\"abc.def\"\t;").matches());
  }

  @Test
  public void testFromSegments(IJavaEnvironment env) {
    ScoutJavaEnvironmentFactory.run(secondSharedEnv -> testFromSegments(env, secondSharedEnv), false, false);
  }

  @SuppressWarnings({"unlikely-arg-type", "SimplifiableJUnitAssertion", "EqualsWithItself", "ConstantConditions", "EqualsBetweenInconvertibleTypes"})
  private static void testFromSegments(IJavaEnvironment first, IJavaEnvironment second) {

    var txtSvcType = first.requireType(TestTextProviderService.class.getName());
    var txtSvcType2 = second.requireType(SecondTestTextProviderService.class.getName());

    assertFalse(PropertiesTextProviderService.fromSegments(new String[]{}, txtSvcType).isPresent());

    var a = PropertiesTextProviderService.fromSegments(new String[]{"Prefix"}, txtSvcType).orElseThrow();
    var b = PropertiesTextProviderService.fromSegments(new String[]{"org", "eclipse", "Prefix"}, txtSvcType).orElseThrow();
    var c = PropertiesTextProviderService.fromSegments(new String[]{"org", "eclipse", "OtherPrefix"}, txtSvcType).orElseThrow();
    var d = PropertiesTextProviderService.fromSegments(new String[]{"org", "eclipse", "OtherPrefix"}, txtSvcType).orElseThrow();

    var nonPropService1 = new TextProviderService(txtSvcType);
    var nonPropService2 = new TextProviderService(txtSvcType2);

    assertFalse(nonPropService1.equals(nonPropService2));
    assertFalse(a.equals(nonPropService1));
    assertTrue(nonPropService1.equals(nonPropService1));
    assertFalse(a.equals(b));
    assertTrue(a.equals(a));
    assertFalse(a.equals(null));
    assertFalse(a.equals(""));
    assertFalse(b.equals(c));
    assertTrue(c.equals(d));
    assertNotEquals(a.hashCode(), b.hashCode());
    assertNotNull(a.toString());
  }

}
