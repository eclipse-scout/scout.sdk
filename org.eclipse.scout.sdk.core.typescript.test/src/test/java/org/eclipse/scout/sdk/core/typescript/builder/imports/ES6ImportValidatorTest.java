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
import static org.eclipse.scout.sdk.core.typescript.testing.spi.TestingNodeElementFactorySpi.newDataType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.scout.sdk.core.typescript.builder.imports.IES6ImportCollector.ES6ImportDescriptor;
import org.eclipse.scout.sdk.core.typescript.testing.spi.TestingNodeElementFactorySpi;
import org.junit.jupiter.api.Test;

public class ES6ImportValidatorTest {

  @Test
  public void testDuplicate() {
    var validator = new ES6ImportValidator();
    var first = newDataType("First");
    assertEquals("First", validator.use(first));
    assertEquals("Second", validator.use(newDataType("Second")));
    assertEquals("First0", validator.use(newDataType("First")));
    assertEquals("First1", validator.use(newDataType("First")));
    assertEquals("First", validator.use(first));

    assertCollectorContent(validator, "First", "Second", "First as First0", "First as First1");
  }

  @Test
  public void testWithReserved() {
    var validator = new ES6ImportValidator();
    validator.importCollector().registerReservedName("First");

    var first = newDataType("First");
    assertEquals("First0", validator.use(first));
    assertEquals("Second", validator.use(newDataType("Second")));
    assertEquals("First1", validator.use(newDataType("First")));
    assertEquals("First2", validator.use(newDataType("First")));
    assertEquals("First0", validator.use(first));

    assertCollectorContent(validator, "First as First0", "Second", "First as First1", "First as First2");
  }

  @Test
  public void testComposite() {
    var validator = new ES6ImportValidator();
    var factory = new TestingNodeElementFactorySpi();
    var string = newDataType("string");
    var object = newDataType("object").spi();
    var clazz = newDataType("MyClass").spi();
    var clazz0 = newDataType("MyClass");
    var clazz1 = newDataType("MyClass").spi();
    var any = newDataType("any").spi();
    var record = newDataType("Record", string, clazz0).spi();
    var arr = factory.createArrayDataType(clazz, 2);
    var intersection = factory.createIntersectionDataType(List.of(object, record));
    var composite = factory.createUnionDataType(List.of(arr, intersection, clazz1, any));

    assertEquals("MyClass[][] | (object & Record<string, MyClass0>) | MyClass1 | any", validator.use(composite.api()));
    assertCollectorContent(validator, "MyClass", "MyClass as MyClass0", "MyClass as MyClass1");
  }

  protected static void assertCollectorContent(IES6ImportValidator validator, String... expected) {
    var actualImports = validator.importCollector().imports().stream()
        .map(ES6ImportDescriptor::importSpecifier)
        .toList();
    assertEquals(asList(expected), actualImports);
  }
}
