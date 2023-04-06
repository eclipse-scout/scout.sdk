/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.builder.imports;

import static java.util.Arrays.asList;
import static org.eclipse.scout.sdk.core.typescript.testing.spi.TestingNodeModulesProviderSpi.createCompositeDataType;
import static org.eclipse.scout.sdk.core.typescript.testing.spi.TestingNodeModulesProviderSpi.createDataType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.typescript.builder.imports.IES6ImportCollector.ES6ImportDescriptor;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.junit.jupiter.api.Test;

public class ES6ImportValidatorTest {

  @Test
  public void testDuplicate() {
    var validator = new ES6ImportValidator();
    var first = createDataType("First");
    assertEquals("First", validator.use(first));
    assertEquals("Second", validator.use(createDataType("Second")));
    assertEquals("First0", validator.use(createDataType("First")));
    assertEquals("First1", validator.use(createDataType("First")));
    assertEquals("First", validator.use(first));

    assertCollectorContent(validator, "First", "Second", "First as First0", "First as First1");
  }

  @Test
  public void testWithReserved() {
    var validator = new ES6ImportValidator();
    validator.importCollector().registerReservedName("First");

    var first = createDataType("First");
    assertEquals("First0", validator.use(first));
    assertEquals("Second", validator.use(createDataType("Second")));
    assertEquals("First1", validator.use(createDataType("First")));
    assertEquals("First2", validator.use(createDataType("First")));
    assertEquals("First0", validator.use(first));

    assertCollectorContent(validator, "First as First0", "Second", "First as First1", "First as First2");
  }

  @Test
  public void testComposite() {
    var validator = new ES6ImportValidator();
    var string = createDataType("string");
    var object = createDataType("object");
    var clazz = createDataType("MyClass");
    var clazz0 = createDataType("MyClass");
    var clazz1 = createDataType("MyClass");
    var any = createDataType("any");
    var record = createDataType("Record", string, clazz0);
    var arr = createCompositeDataType(DataTypeFlavor.Array, clazz);
    var intersection = createCompositeDataType(DataTypeFlavor.Intersection, object, record);
    var composite = createCompositeDataType(DataTypeFlavor.Union, arr, intersection, clazz1, any);

    assertEquals("MyClass[] | (object & Record<string, MyClass0>) | MyClass1 | any", validator.use(composite));
    assertCollectorContent(validator, "MyClass", "MyClass as MyClass0", "MyClass as MyClass1");
  }

  protected static void assertCollectorContent(IES6ImportValidator validator, String... expected) {
    var actualImports = validator.importCollector().imports().stream()
        .map(ES6ImportDescriptor::importSpecifier)
        .toList();
    assertEquals(asList(expected), actualImports);
  }
}
