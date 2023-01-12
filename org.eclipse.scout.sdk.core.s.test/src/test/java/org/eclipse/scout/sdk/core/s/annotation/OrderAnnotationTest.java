/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.annotation;

import static org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation.getNewViewOrderValue;
import static org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation.getOrderValueInBetween;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutClientJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.FormWithHighOrders;

/**
 * <h3>{@link OrderAnnotationTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWithJavaEnvironmentFactory(ScoutClientJavaEnvironmentFactory.class)
public class OrderAnnotationTest {

  private static final double DELTA = 0.00000000001;

  @Test
  public void testNewViewOrderValue(IJavaEnvironment env) {
    var type = env.requireType("formdata.client.ui.forms.IgnoredFieldsForm$MainBox$AGroupBox");
    var first = type.innerTypes().first().orElseThrow();
    var second = type.innerTypes().item(1).orElseThrow();
    var scoutApi = env.requireApi(IScoutApi.class);
    assertEquals(-1000.0, getNewViewOrderValue(type, scoutApi.IFormField(), first.source().orElseThrow().start() - 1), DELTA);
    assertEquals(15.0, getNewViewOrderValue(type, scoutApi.IFormField(), first.source().orElseThrow().end() + 1), DELTA);
    assertEquals(2000.0, getNewViewOrderValue(type, scoutApi.IFormField(), second.source().orElseThrow().end() + 1), DELTA);

    var formWithHighOrders = env.requireType(FormWithHighOrders.class.getName());
    var mainBox = formWithHighOrders.innerTypes().first().orElseThrow();
    var aGroupBox = mainBox.innerTypes().first().orElseThrow();
    assertEquals(99382716061728384.0d, getNewViewOrderValue(mainBox, scoutApi.IFormField(), aGroupBox.source().orElseThrow().end() + 1), DELTA);
    assertEquals(1000, getNewViewOrderValue(aGroupBox, scoutApi.IFormField(), aGroupBox.source().orElseThrow().start() + 1), DELTA);
  }

  @Test
  public void testValueInBetween() {
    assertEquals(1500, getOrderValueInBetween(1000, 2000), DELTA);
    assertEquals(1000, getOrderValueInBetween(0, 2000), DELTA);
    assertEquals(50, getOrderValueInBetween(100, 0), DELTA);
    assertEquals(1.5, getOrderValueInBetween(1, 2), DELTA);
    assertEquals(0.5, getOrderValueInBetween(0, 1), DELTA);
    assertEquals(2, getOrderValueInBetween(1, 3), DELTA);
    assertEquals(2.7, getOrderValueInBetween(2.4, 3), DELTA);
    assertEquals(3, getOrderValueInBetween(2, 3.1), DELTA);
    assertEquals(2.05, getOrderValueInBetween(2, 2.1), DELTA);
    assertEquals(2.2, getOrderValueInBetween(2.1, 2.3), DELTA);
    assertEquals(4, getOrderValueInBetween(2.1, 5.7), DELTA);
    assertEquals(4, getOrderValueInBetween(2.1, 6.7), DELTA);
    assertEquals(5, getOrderValueInBetween(2.1, 7.7), DELTA);
    assertEquals(3, getOrderValueInBetween(2.6, 3.7), DELTA);
    assertEquals(187, getOrderValueInBetween(125, 250), DELTA);
    assertEquals(2000, getOrderValueInBetween(1000, 100000), DELTA);
  }
}
