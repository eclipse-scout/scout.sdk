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
public class NodeElementQueryTest {
  @Test
  public void testSelf(INodeModule root) {
    var directExports = root
        .elements()
        .withExportName("OwnExport2")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("OwnExport2Class"), directExports);

    var allDirectExports = root
        .elements()
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("OwnExport1Class", "OwnExport2Class", "OwnExport3Class"), allDirectExports);

    var directExportsNotFound = root
        .elements()
        .withExportName("Widget")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of(), directExportsNotFound);
  }

  @Test
  public void testRecursive(INodeModule root) {
    var recursiveExports = root
        .elements()
        .withExportName("Widget")
        .withRecursive(true)
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("Widget"), recursiveExports);

    var recursiveExportsNotFound = root
        .elements()
        .withRecursive(true)
        .withExportName("NotExisting")
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of(), recursiveExportsNotFound);

    var allRecursiveExports = root
        .elements()
        .withRecursive(true)
        .stream()
        .map(INodeElement::name)
        .toList();
    assertEquals(List.of("OwnExport1Class", "OwnExport2Class", "OwnExport3Class", "Ex1Class", "Widget", "Ex2Class", "Ex3Class", "Ex4Class"), allRecursiveExports);
  }
}
