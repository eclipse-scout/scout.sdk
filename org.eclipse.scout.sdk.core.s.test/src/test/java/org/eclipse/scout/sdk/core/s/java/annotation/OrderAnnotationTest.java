/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.annotation;

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutClientJavaEnvironmentFactory;
import org.junit.jupiter.api.Assertions;
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
    Assertions.assertEquals(-1000.0, OrderAnnotation.getNewViewOrderValue(type, scoutApi.IFormField(), first.source().orElseThrow().start() - 1), DELTA);
    Assertions.assertEquals(15.0, OrderAnnotation.getNewViewOrderValue(type, scoutApi.IFormField(), first.source().orElseThrow().end() + 1), DELTA);
    Assertions.assertEquals(2000.0, OrderAnnotation.getNewViewOrderValue(type, scoutApi.IFormField(), second.source().orElseThrow().end() + 1), DELTA);

    var formWithHighOrders = env.requireType(FormWithHighOrders.class.getName());
    var mainBox = formWithHighOrders.innerTypes().first().orElseThrow();
    var aGroupBox = mainBox.innerTypes().first().orElseThrow();
    Assertions.assertEquals(99382716061728384.0d, OrderAnnotation.getNewViewOrderValue(mainBox, scoutApi.IFormField(), aGroupBox.source().orElseThrow().end() + 1), DELTA);
    Assertions.assertEquals(1000, OrderAnnotation.getNewViewOrderValue(aGroupBox, scoutApi.IFormField(), aGroupBox.source().orElseThrow().start() + 1), DELTA);
  }

  @Test
  public void testValueInBetween() {
    Assertions.assertEquals(1500, OrderAnnotation.getOrderValueInBetween(1000, 2000), DELTA);
    Assertions.assertEquals(1000, OrderAnnotation.getOrderValueInBetween(0, 2000), DELTA);
    Assertions.assertEquals(50, OrderAnnotation.getOrderValueInBetween(100, 0), DELTA);
    Assertions.assertEquals(1.5, OrderAnnotation.getOrderValueInBetween(1, 2), DELTA);
    Assertions.assertEquals(0.5, OrderAnnotation.getOrderValueInBetween(0, 1), DELTA);
    Assertions.assertEquals(2, OrderAnnotation.getOrderValueInBetween(1, 3), DELTA);
    Assertions.assertEquals(2.7, OrderAnnotation.getOrderValueInBetween(2.4, 3), DELTA);
    Assertions.assertEquals(3, OrderAnnotation.getOrderValueInBetween(2, 3.1), DELTA);
    Assertions.assertEquals(2.05, OrderAnnotation.getOrderValueInBetween(2, 2.1), DELTA);
    Assertions.assertEquals(2.2, OrderAnnotation.getOrderValueInBetween(2.1, 2.3), DELTA);
    Assertions.assertEquals(4, OrderAnnotation.getOrderValueInBetween(2.1, 5.7), DELTA);
    Assertions.assertEquals(4, OrderAnnotation.getOrderValueInBetween(2.1, 6.7), DELTA);
    Assertions.assertEquals(5, OrderAnnotation.getOrderValueInBetween(2.1, 7.7), DELTA);
    Assertions.assertEquals(3, OrderAnnotation.getOrderValueInBetween(2.6, 3.7), DELTA);
    Assertions.assertEquals(187, OrderAnnotation.getOrderValueInBetween(125, 250), DELTA);
    Assertions.assertEquals(2000, OrderAnnotation.getOrderValueInBetween(1000, 100000), DELTA);
  }
}
