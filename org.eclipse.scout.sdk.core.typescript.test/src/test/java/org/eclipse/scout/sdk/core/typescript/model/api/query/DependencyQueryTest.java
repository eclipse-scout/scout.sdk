/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.testing.ExtendWithNodeModules;
import org.junit.jupiter.api.Test;

@ExtendWithNodeModules("DependencyQueryTest")
public class DependencyQueryTest {

  @Test
  public void testWithSelf(INodeModule root) {
    var directDependenciesWithSelf = root.packageJson()
        .dependencies()
        .withSelf(true)
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("root", "a", "b", "c", "d"), directDependenciesWithSelf);

    var directDependenciesWithSelfAndName = root.packageJson()
        .dependencies()
        .withSelf(true)
        .withName("root")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("root"), directDependenciesWithSelfAndName);

    var directDependenciesWithSelfAndNameNotFound = root.packageJson()
        .dependencies()
        .withSelf(true)
        .withName("notExisting")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of(), directDependenciesWithSelfAndNameNotFound);
  }

  @Test
  public void testOnlyDirect(INodeModule root) {
    var directDependencies = root.packageJson()
        .dependencies()
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("a", "b", "c", "d"), directDependencies);

    var directDependenciesWithName = root.packageJson()
        .dependencies()
        .withName("d")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("d"), directDependenciesWithName);

    var directDependenciesWithNameNotFound = root.packageJson()
        .dependencies()
        .withName("notExisting")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of(), directDependenciesWithNameNotFound);
  }

  @Test
  public void testRecursive(INodeModule root) {
    var recursiveDependencies = root.packageJson()
        .dependencies()
        .withRecursive(true)
        .withSelf(true)
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("root", "a", "b", "c", "d", "1", "2", "3", "1_1",
        "1_2", "1_3", "3_1", "3_2", "1_3_1", "@eclipse-scout/core", "1_3_3", "1_3_2_1", "1_3_2_2", "1_3_3_1"), recursiveDependencies);

    var recursiveDependenciesWithName = root.packageJson()
        .dependencies()
        .withRecursive(true)
        .withSelf(true)
        .withName("1_3_3_1")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("1_3_3_1"), recursiveDependenciesWithName);

    var recursiveDependenciesWithNameNotFound = root.packageJson()
        .dependencies()
        .withRecursive(true)
        .withSelf(true)
        .withName("notExisting")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of(), recursiveDependenciesWithNameNotFound);
  }

  @Test
  public void testWithCycle(INodeModule root) {
    var core = root.packageJson()
        .dependencies()
        .withRecursive(true)
        .withSelf(true)
        .withName("@eclipse-scout/core")
        .first().orElseThrow();
    core.packageJson().spi().dependencies().add(root.spi()); // build cycle
    var recursiveDependencies = root.packageJson()
        .dependencies()
        .withRecursive(true)
        .withSelf(true)
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("root", "a", "b", "c", "d", "1", "2", "3", "1_1",
        "1_2", "1_3", "3_1", "3_2", "1_3_1", "@eclipse-scout/core", "1_3_3", "1_3_2_1", "1_3_2_2", "1_3_3_1"), recursiveDependencies);
  }
}
