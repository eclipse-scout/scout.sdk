/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link TypeNameDescriptorTest}</h3>
 *
 * @since 7.0.0
 */
public class TypeNameDescriptorTest {

  @Test
  @SuppressWarnings({"unlikely-arg-type", "ConstantConditions", "SimplifiableJUnitAssertion", "EqualsBetweenInconvertibleTypes"})
  public void testEquals() {
    var ref = "a.b.TestClass$Inner[][]";
    assertEquals(TypeNameDescriptor.of(ref), TypeNameDescriptor.of(ref));
    assertNotEquals(TypeNameDescriptor.of(ref), TypeNameDescriptor.of("a.b.TestClass$Inner[]"));
    assertNotEquals(TypeNameDescriptor.of(ref), TypeNameDescriptor.of("a.b.TestClass.Inner[][]"));
    assertNotEquals(TypeNameDescriptor.of(ref), TypeNameDescriptor.of("a.c.TestClass$Inner[][]"));

    var d = TypeNameDescriptor.of(ref);
    assertEquals(d, d);
    assertFalse(d.equals(null));
    assertFalse(d.equals(ref));
  }

  @Test
  public void testHashCode() {
    assertEquals(TypeNameDescriptor.of("a.b.TestClass$Inner[][]").hashCode(), TypeNameDescriptor.of("a.b.TestClass$Inner[][]").hashCode());
    assertNotEquals(TypeNameDescriptor.of("a.b.TestClass$Inner[][]").hashCode(), TypeNameDescriptor.of("a.b.TestClass$Inner[]").hashCode());
    assertNotEquals(TypeNameDescriptor.of("a.b.TestClass$Inner[][]").hashCode(), TypeNameDescriptor.of("a.b.TestClass.Inner[][]").hashCode());
    assertNotEquals(TypeNameDescriptor.of("a.b.TestClass$Inner[][]").hashCode(), TypeNameDescriptor.of("a.c.TestClass$Inner[][]").hashCode());
  }

  @Test
  public void testParsing() {
    assertEquals("TypeNameDescriptor [primaryTypeName=a.b.TestClass, innerTypeNames=Inner, arrayDimension=2]", TypeNameDescriptor.of("a.b.TestClass$Inner[][]").toString());
    assertEquals("TypeNameDescriptor [primaryTypeName=a.b.TestClass, innerTypeNames=Inner$SecondInner, arrayDimension=2]", TypeNameDescriptor.of("a.b.TestClass$Inner$SecondInner[][]").toString());
    assertEquals("TypeNameDescriptor [primaryTypeName=a.b.TestClass.Inner, arrayDimension=2]", TypeNameDescriptor.of("a.b.TestClass.Inner[][]").toString());
    assertEquals("TypeNameDescriptor [primaryTypeName=a.b.TestClass.Main, arrayDimension=0]", TypeNameDescriptor.of("a.b.TestClass.Main").toString());

    assertTrue(TypeNameDescriptor.of("a.b.TestClass$Main").hasInnerType());
    assertFalse(TypeNameDescriptor.of("a.b.TestClass.Main[][][]").hasInnerType());
  }
}
