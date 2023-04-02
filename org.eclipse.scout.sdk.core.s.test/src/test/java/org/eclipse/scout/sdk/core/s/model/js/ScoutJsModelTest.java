/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.testing.ExtendWithNodeModules;
import org.junit.jupiter.api.Test;

public class ScoutJsModelTest {

  @Test
  @ExtendWithNodeModules("ScoutJsModelTestEmpty")
  public void testScoutJsDependenciesRecursivelyEmpty(INodeModule withoutDependencies) {
    assertTrue(ScoutJsModels.create(withoutDependencies).isEmpty());
  }

  @Test
  @ExtendWithNodeModules("ScoutJsModelTestSingle")
  public void testScoutJsDependenciesRecursivelySingle(INodeModule withOnlyScoutCoreDependency) {
    assertTransitiveScoutDependencies(withOnlyScoutCoreDependency, ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME);
  }

  @Test
  @ExtendWithNodeModules("ScoutJsModelTestOnlyScout")
  public void testScoutJsDependenciesRecursivelyOnlyScout(INodeModule scoutCore) {
    var model = ScoutJsModels.create(scoutCore).orElseThrow();
    var widgetObject = model.scoutObjects().stream()
        .filter(o -> ScoutJsCoreConstants.CLASS_NAME_WIDGET.equals(o.name()))
        .findAny()
        .orElse(null);
    assertNotNull(widgetObject);
  }

  @Test
  @ExtendWithNodeModules("ScoutJsModelTestCycle")
  public void testScoutJsDependenciesRecursivelyWithCycle(INodeModule root) {
    // build cycle between b and d
    var b = root.packageJson().dependencies().withRecursive(true).withName("b").first().orElseThrow();
    root.packageJson().dependencies().withRecursive(true).withName("d").first().orElseThrow().packageJson().spi().dependencies().add(b.spi());
    assertTransitiveScoutDependencies(root, ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME, "d", "c", "b");
  }

  @Test
  @ExtendWithNodeModules("ScoutJsModelTestTreeWithDuplicates")
  public void testScoutJsDependenciesRecursivelyMultipleTree(INodeModule root) {
    assertTransitiveScoutDependencies(root, ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME, "1_3", "1_3_3", "1", "b", "3_2", "3", "d");
  }

  private static void assertTransitiveScoutDependencies(INodeModule root, String... expectedDependencyNames) {
    var dependencyNames = ScoutJsModels.create(root).orElseThrow()
        .scoutJsDependenciesRecursively()
        .map(INodeElement::name)
        .toList();
    assertEquals(Arrays.asList(expectedDependencyNames), dependencyNames);
  }
}
