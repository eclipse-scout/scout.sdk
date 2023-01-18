/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dataobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

import dataobject.context.FixtureNamespace;
import dataobject.context.FixtureTypeVersions.SdkFixture_1_0_0_0;

@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class DoContextTest {
  @Test
  public void testParse(IJavaEnvironment env) {
    var doContext = new DoContext(env.requireType(FixtureNamespace.class.getName()), env.requireType(SdkFixture_1_0_0_0.class.getName()));
    assertEquals("sdk", doContext.namespaceId().orElseThrow());
    assertNotNull(doContext.namespace().orElseThrow());
    assertNotNull(doContext.typeVersion().orElseThrow());
    assertEquals("DoContext [namespace=dataobject.context.FixtureNamespace, namespaceId=sdk, typeVersion=dataobject.context.FixtureTypeVersions$SdkFixture_1_0_0_0]", doContext.toString());
  }

  @Test
  public void testEmpty() {
    var doContext = new DoContext();
    assertTrue(doContext.namespaceId().isEmpty());
    assertTrue(doContext.namespace().isEmpty());
    assertTrue(doContext.typeVersion().isEmpty());
    assertEquals("DoContext [namespace=null, namespaceId=null, typeVersion=null]", doContext.toString());
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "EqualsBetweenInconvertibleTypes"})
  public void testEqualsHashCode(IJavaEnvironment env) {
    assertFalse(new DoContext().equals(null));
    assertFalse(new DoContext().equals(""));
    var a = new DoContext();
    var b = new DoContext(env.requireType(FixtureNamespace.class.getName()), env.requireType(SdkFixture_1_0_0_0.class.getName()));
    assertEquals(a, a);
    assertNotEquals(a, b);
    assertEquals(a.hashCode(), a.hashCode());
  }
}
