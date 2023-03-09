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

@ExtendWithNodeModules("ExportFromQueryTest")
public class ExportFromQueryTest {
  @Test
  public void testSelf(INodeModule root) {
    var directExports = root
        .exports()
        .withName("OwnExport2")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("OwnExport2"), directExports);

    var allDirectExports = root
        .exports()
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("OwnExport1", "OwnExport2", "OwnExport3"), allDirectExports);

    var directExportsNotFound = root
        .exports()
        .withName("Widget")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of(), directExportsNotFound);
  }

  @Test
  public void testRecursive(INodeModule root) {
    var recursiveExports = root
        .exports()
        .withName("Widget")
        .withRecursive(true)
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("Widget"), recursiveExports);

    var recursiveExportsNotFound = root
        .exports()
        .withRecursive(true)
        .withName("NotExisting")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of(), recursiveExportsNotFound);

    var allRecursiveExports = root
        .exports()
        .withRecursive(true)
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("OwnExport1", "OwnExport2", "OwnExport3", "Ex1", "Widget", "Ex2", "Ex3", "Ex4"), allRecursiveExports);
  }
}
