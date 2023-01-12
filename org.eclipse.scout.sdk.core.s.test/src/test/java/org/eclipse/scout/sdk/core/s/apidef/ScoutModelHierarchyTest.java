/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.apidef;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ScoutModelHierarchyTest}</h3>
 *
 * @since 7.0.0
 */
@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class ScoutModelHierarchyTest {
  @Test
  public void testPossibleChildrenFor(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);
    var scoutModelHierarchy = scoutApi.hierarchy();
    assertEquals(singleton(scoutApi.IMenu().fqn()), scoutModelHierarchy.possibleChildrenFor(singletonList(scoutApi.AbstractValueField().fqn())));
    assertEquals(emptySet(), scoutModelHierarchy.possibleChildrenFor(singletonList(JavaTypes.Float)));
    assertTrue(scoutModelHierarchy.possibleChildrenFor(singletonList(scoutApi.AbstractCompositeField().fqn())).size() > 0);
  }

  @Test
  public void testTabBox(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);
    var scoutModelHierarchy = scoutApi.hierarchy();
    assertEquals(3, scoutModelHierarchy.possibleChildrenFor(singletonList(scoutApi.AbstractTabBox().fqn())).size());
    assertEquals(3, scoutModelHierarchy.possibleChildrenFor(singletonList(scoutApi.AbstractTabBoxExtension().fqn())).size());
  }

  @Test
  public void testRadioButtonGroup(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);
    var scoutModelHierarchy = scoutApi.hierarchy();
    assertEquals(3, scoutModelHierarchy.possibleChildrenFor(singletonList(scoutApi.AbstractRadioButtonGroup().fqn())).size());
    assertEquals(3, scoutModelHierarchy.possibleChildrenFor(singletonList(scoutApi.AbstractRadioButtonGroupExtension().fqn())).size());
  }

  @Test
  public void testListTreeBox(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);
    var scoutModelHierarchy = scoutApi.hierarchy();
    assertEquals(2, scoutModelHierarchy.possibleChildrenFor(singletonList(scoutApi.AbstractListBox().fqn())).size());
    assertEquals(2, scoutModelHierarchy.possibleChildrenFor(singletonList(scoutApi.AbstractListBoxExtension().fqn())).size());
    assertEquals(2, scoutModelHierarchy.possibleChildrenFor(singletonList(scoutApi.AbstractTreeBox().fqn())).size());
    assertEquals(2, scoutModelHierarchy.possibleChildrenFor(singletonList(scoutApi.AbstractTreeBoxExtension().fqn())).size());
  }

  @Test
  public void testIsSubtypeOf(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);
    var scoutModelHierarchy = scoutApi.hierarchy();
    var iTabBox = scoutApi.ITabBox().fqn();
    assertTrue(scoutModelHierarchy.isSubtypeOf(iTabBox, scoutApi.ICompositeField().fqn()));
    assertTrue(scoutModelHierarchy.isSubtypeOf(iTabBox, iTabBox));
    assertTrue(scoutModelHierarchy.isSubtypeOf(iTabBox, scoutApi.IOrdered().fqn()));
  }
}
