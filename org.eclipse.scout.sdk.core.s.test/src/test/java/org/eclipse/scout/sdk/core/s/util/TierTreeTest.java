/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.util;

import static org.eclipse.scout.rt.platform.util.CollectionUtility.arrayList;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.lastElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TierTreeTest {

  private TierTree m_tierTree;

  @BeforeEach
  public void before() {
    m_tierTree = TierTree.create();

    // Level 1
    m_tierTree.addDependencyImpl(TestTier.A1, TestTier.Root);
    m_tierTree.addDependencyImpl(TestTier.B1, TestTier.Root);
    m_tierTree.addDependencyImpl(TestTier.C1, TestTier.Root);

    // Level 2
    m_tierTree.addDependencyImpl(TestTier.A1_AA1, TestTier.A1);
    m_tierTree.addDependencyImpl(TestTier.A1_AA2, TestTier.A1);
    m_tierTree.addDependencyImpl(TestTier.A1_AA3, TestTier.A1);

    m_tierTree.addDependencyImpl(TestTier.B1_BB1, TestTier.B1);
    m_tierTree.addDependencyImpl(TestTier.B1_BB2, TestTier.B1);
    m_tierTree.addDependencyImpl(TestTier.B1_BB3, TestTier.B1);

    m_tierTree.addDependencyImpl(TestTier.C1_CC1, TestTier.C1);
    m_tierTree.addDependencyImpl(TestTier.C1_CC2, TestTier.C1);
    m_tierTree.addDependencyImpl(TestTier.C1_CC3, TestTier.C1);

    // Level 3
    m_tierTree.addDependencyImpl(TestTier.A1_AA1_AAA1, TestTier.A1_AA1);
    m_tierTree.addDependencyImpl(TestTier.A1_AA1_AAA2, TestTier.A1_AA1);
    m_tierTree.addDependencyImpl(TestTier.A1_AA1_AAA3, TestTier.A1_AA1);

    m_tierTree.addDependencyImpl(TestTier.A1_AA2_AAA4, TestTier.A1_AA2);
    m_tierTree.addDependencyImpl(TestTier.A1_AA2_AAA5, TestTier.A1_AA2);
    m_tierTree.addDependencyImpl(TestTier.A1_AA2_AAA6, TestTier.A1_AA2);

    m_tierTree.addDependencyImpl(TestTier.A1_AA3_AAA7, TestTier.A1_AA3);
    m_tierTree.addDependencyImpl(TestTier.A1_AA3_AAA8, TestTier.A1_AA3);
    m_tierTree.addDependencyImpl(TestTier.A1_AA3_AAA9, TestTier.A1_AA3);

    m_tierTree.addDependencyImpl(TestTier.B1_BB1_BBB1, TestTier.B1_BB1);
    m_tierTree.addDependencyImpl(TestTier.B1_BB1_BBB2, TestTier.B1_BB1);
    m_tierTree.addDependencyImpl(TestTier.B1_BB1_BBB3, TestTier.B1_BB1);

    m_tierTree.addDependencyImpl(TestTier.C1_CC1_CCC1, TestTier.C1_CC1);
    m_tierTree.addDependencyImpl(TestTier.C1_CC1_CCC2, TestTier.C1_CC1);
    m_tierTree.addDependencyImpl(TestTier.C1_CC1_CCC3, TestTier.C1_CC1);
  }

  @Test
  public void testAddDependency() {
    // Tree not empty and both are not already contained
    assertFalse(m_tierTree.addDependencyImpl(AddDependencyTier.C1_CC2_CCC5, AddDependencyTier.C1_CC2_CCC4));

    // Tree not empty and one is already contained
    assertTrue(m_tierTree.addDependencyImpl(AddDependencyTier.C1_CC2_CCC4, TestTier.C1_CC2));
    assertTrue(m_tierTree.addDependencyImpl(AddDependencyTier.C1_CC2_CCC5, TestTier.C1_CC2));
    assertTrue(m_tierTree.addDependencyImpl(AddDependencyTier.C1_CC2_CCC6, TestTier.C1_CC2));

    // Tree not empty and both are already contained
    assertFalse(m_tierTree.addDependencyImpl(AddDependencyTier.C1_CC2_CCC5, TestTier.A1));
  }

  @Test
  public void testHasDependency() {
    assertFalse(m_tierTree.hasDependencyImpl(TestTier.A1, TestTier.A1));

    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA1, TestTier.A1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA2, TestTier.A1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA3, TestTier.A1));

    assertFalse(m_tierTree.hasDependencyImpl(TestTier.A1_AA1, TestTier.A1_AA1));
    assertFalse(m_tierTree.hasDependencyImpl(TestTier.A1_AA2, TestTier.A1_AA2));
    assertFalse(m_tierTree.hasDependencyImpl(TestTier.A1_AA3, TestTier.A1_AA3));

    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA1_AAA1, TestTier.A1_AA1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA1_AAA2, TestTier.A1_AA1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA1_AAA3, TestTier.A1_AA1));

    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA2_AAA4, TestTier.A1_AA2));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA2_AAA5, TestTier.A1_AA2));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA2_AAA6, TestTier.A1_AA2));

    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA3_AAA7, TestTier.A1_AA3));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA3_AAA8, TestTier.A1_AA3));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA3_AAA9, TestTier.A1_AA3));

    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA1_AAA1, TestTier.A1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA1_AAA2, TestTier.A1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA1_AAA3, TestTier.A1));

    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA2_AAA4, TestTier.A1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA2_AAA5, TestTier.A1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA2_AAA6, TestTier.A1));

    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA3_AAA7, TestTier.A1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA3_AAA8, TestTier.A1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.A1_AA3_AAA9, TestTier.A1));

    assertFalse(m_tierTree.hasDependencyImpl(TestTier.B1_BB1, TestTier.A1));
    assertFalse(m_tierTree.hasDependencyImpl(TestTier.B1_BB2, TestTier.A1));
    assertFalse(m_tierTree.hasDependencyImpl(TestTier.B1_BB3, TestTier.A1));

    assertTrue(m_tierTree.hasDependencyImpl(TestTier.B1_BB1, TestTier.B1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.B1_BB2, TestTier.B1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.B1_BB3, TestTier.B1));

    assertFalse(m_tierTree.hasDependencyImpl(TestTier.B1_BB1_BBB1, TestTier.A1));
    assertFalse(m_tierTree.hasDependencyImpl(TestTier.B1_BB1_BBB2, TestTier.A1));
    assertFalse(m_tierTree.hasDependencyImpl(TestTier.B1_BB1_BBB3, TestTier.A1));

    assertTrue(m_tierTree.hasDependencyImpl(TestTier.B1_BB1_BBB1, TestTier.B1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.B1_BB1_BBB2, TestTier.B1));
    assertTrue(m_tierTree.hasDependencyImpl(TestTier.B1_BB1_BBB3, TestTier.B1));

    assertFalse(m_tierTree.hasDependencyImpl(TestTier.B1_BB1_BBB1, TestTier.B1_BB1_BBB1));
    assertFalse(m_tierTree.hasDependencyImpl(TestTier.B1_BB1_BBB2, TestTier.B1_BB1_BBB2));
    assertFalse(m_tierTree.hasDependencyImpl(TestTier.B1_BB1_BBB3, TestTier.B1_BB1_BBB3));
  }

  @Test
  public void testIsAvailable() {
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1, TestTier.A1));

    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA1, TestTier.A1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA2, TestTier.A1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA3, TestTier.A1));

    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA1, TestTier.A1_AA1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA2, TestTier.A1_AA2));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA3, TestTier.A1_AA3));

    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA1_AAA1, TestTier.A1_AA1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA1_AAA2, TestTier.A1_AA1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA1_AAA3, TestTier.A1_AA1));

    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA2_AAA4, TestTier.A1_AA2));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA2_AAA5, TestTier.A1_AA2));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA2_AAA6, TestTier.A1_AA2));

    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA3_AAA7, TestTier.A1_AA3));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA3_AAA8, TestTier.A1_AA3));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA3_AAA9, TestTier.A1_AA3));

    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA1_AAA1, TestTier.A1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA1_AAA2, TestTier.A1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA1_AAA3, TestTier.A1));

    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA2_AAA4, TestTier.A1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA2_AAA5, TestTier.A1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA2_AAA6, TestTier.A1));

    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA3_AAA7, TestTier.A1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA3_AAA8, TestTier.A1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.A1_AA3_AAA9, TestTier.A1));

    assertFalse(m_tierTree.isAvailableImpl(TestTier.B1_BB1, TestTier.A1));
    assertFalse(m_tierTree.isAvailableImpl(TestTier.B1_BB2, TestTier.A1));
    assertFalse(m_tierTree.isAvailableImpl(TestTier.B1_BB3, TestTier.A1));

    assertTrue(m_tierTree.isAvailableImpl(TestTier.B1_BB1, TestTier.B1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.B1_BB2, TestTier.B1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.B1_BB3, TestTier.B1));

    assertFalse(m_tierTree.isAvailableImpl(TestTier.B1_BB1_BBB1, TestTier.A1));
    assertFalse(m_tierTree.isAvailableImpl(TestTier.B1_BB1_BBB2, TestTier.A1));
    assertFalse(m_tierTree.isAvailableImpl(TestTier.B1_BB1_BBB3, TestTier.A1));

    assertTrue(m_tierTree.isAvailableImpl(TestTier.B1_BB1_BBB1, TestTier.B1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.B1_BB1_BBB2, TestTier.B1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.B1_BB1_BBB3, TestTier.B1));

    assertTrue(m_tierTree.isAvailableImpl(TestTier.B1_BB1_BBB1, TestTier.B1_BB1_BBB1));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.B1_BB1_BBB2, TestTier.B1_BB1_BBB2));
    assertTrue(m_tierTree.isAvailableImpl(TestTier.B1_BB1_BBB3, TestTier.B1_BB1_BBB3));
  }

  @Test
  public void testPath() {
    // to is null
    assertEquals(List.of(), m_tierTree.topDownPathImpl(null, null));
    assertEquals(List.of(), m_tierTree.topDownPathImpl(TestTier.A1, null));

    // no path possible                  
    assertEquals(List.of(), m_tierTree.topDownPathImpl(TestTier.A1, TestTier.B1_BB1_BBB1));
    assertEquals(List.of(), m_tierTree.topDownPathImpl(TestTier.A1_AA1_AAA1, TestTier.A1_AA1_AAA3));

    // path possible
    assertEquals(List.of(TestTier.A1, TestTier.A1_AA2, TestTier.A1_AA2_AAA6), m_tierTree.topDownPathImpl(TestTier.A1, TestTier.A1_AA2_AAA6));
    assertEquals(List.of(TestTier.B1, TestTier.B1_BB1, TestTier.B1_BB1_BBB3), m_tierTree.topDownPathImpl(TestTier.B1, TestTier.B1_BB1_BBB3));

    assertEquals(List.of(TestTier.Root, TestTier.A1, TestTier.A1_AA2, TestTier.A1_AA2_AAA6), m_tierTree.topDownPathImpl(null, TestTier.A1_AA2_AAA6));
    assertEquals(List.of(TestTier.Root, TestTier.B1, TestTier.B1_BB1, TestTier.B1_BB1_BBB3), m_tierTree.topDownPathImpl(null, TestTier.B1_BB1_BBB3));
  }

  @Test
  @ExtendWithTestingEnvironment(
      primary = @ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class),
      dto = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
  public void testTierOf(TestingEnvironment env) {
    testTierOf(null, "something else", env);
    testTierOf(TestTier.A1_AA2_AAA6, "AAA6", env);
    testTierOf(TestTier.A1_AA3, "AA3", env);
    testTierOf(TestTier.A1, "A1", env);
  }

  private void testTierOf(ITier<?> expected, String fqn, TestingEnvironment env) {
    assertEquals(expected, m_tierTree.tierOfImpl(fqn::equals, env.primaryEnvironment()::api).orElse(null));
  }

  private enum TestTier implements ITier<IScoutApi> {
    Root,
    // Level 1
    A1, B1, C1,
    // Level 2
    A1_AA1, A1_AA2, A1_AA3,
    B1_BB1, B1_BB2, B1_BB3,
    C1_CC1, C1_CC2, C1_CC3,
    // Level 3
    A1_AA1_AAA1, A1_AA1_AAA2, A1_AA1_AAA3,
    A1_AA2_AAA4, A1_AA2_AAA5, A1_AA2_AAA6,
    A1_AA3_AAA7, A1_AA3_AAA8, A1_AA3_AAA9,
    B1_BB1_BBB1, B1_BB1_BBB2, B1_BB1_BBB3,
    C1_CC1_CCC1, C1_CC1_CCC2, C1_CC1_CCC3;

    @Override
    public String tierName() {
      return name();
    }

    @Override
    public String getLookupFqn(IScoutApi api) {
      return lastElement(arrayList(tierName().split("_")));
    }

    @Override
    public Class<IScoutApi> getApiClass() {
      return IScoutApi.class;
    }
  }

  private enum AddDependencyTier implements ITier<IScoutApi> {
    // Level 3
    C1_CC2_CCC4, C1_CC2_CCC5, C1_CC2_CCC6;

    @Override
    public String tierName() {
      return name();
    }

    @Override
    public String getLookupFqn(IScoutApi api) {
      return null;
    }

    @Override
    public Class<IScoutApi> getApiClass() {
      return IScoutApi.class;
    }
  }
}
