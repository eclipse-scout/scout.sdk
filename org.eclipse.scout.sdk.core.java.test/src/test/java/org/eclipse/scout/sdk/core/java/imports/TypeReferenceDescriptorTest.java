/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.imports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link TypeReferenceDescriptorTest}</h3>
 *
 * @since 7.0.0
 */
public class TypeReferenceDescriptorTest {
  @Test
  @SuppressWarnings({"unlikely-arg-type", "ConstantConditions", "EqualsBetweenInconvertibleTypes", "SimplifiableJUnitAssertion"})
  public void testEquals() {
    var ref = "a.b.TestClass$Inner";
    assertEquals(new TypeReferenceDescriptor(ref), new TypeReferenceDescriptor(ref));
    assertNotEquals(new TypeReferenceDescriptor(ref), new TypeReferenceDescriptor("a.b.TestClass.Inner"));
    assertNotEquals(new TypeReferenceDescriptor(ref), new TypeReferenceDescriptor("boolean"));
    assertNotEquals(new TypeReferenceDescriptor(ref), new TypeReferenceDescriptor("a.c.TestClass$Inner"));

    var d = new TypeReferenceDescriptor(ref);
    assertEquals(d, d);
    assertFalse(d.equals(null));
    assertFalse(d.equals(ref));
  }

  @Test
  public void testHashCode() {
    var ref = "a.b.TestClass$Inner";
    assertEquals(new TypeReferenceDescriptor(ref).hashCode(), new TypeReferenceDescriptor(ref).hashCode());
    assertNotEquals(new TypeReferenceDescriptor(ref).hashCode(), new TypeReferenceDescriptor("a.b.TestClass.Inner").hashCode());
    assertNotEquals(new TypeReferenceDescriptor(ref).hashCode(), new TypeReferenceDescriptor("boolean").hashCode());
    assertNotEquals(new TypeReferenceDescriptor(ref).hashCode(), new TypeReferenceDescriptor("a.c.TestClass$Inner").hashCode());
  }

  @Test
  public void testParsing() {
    assertEquals("TypeReferenceDescriptor [packageName=a.b.c, simpleName=SecondInner, qualifier=a.b.c.MyClass.InnerClass, qualifiedName=a.b.c.MyClass.InnerClass.SecondInner, isTypeArg=false, isBaseType=false]",
        new TypeReferenceDescriptor("a.b.c.MyClass$InnerClass$SecondInner").toString());
    assertEquals("TypeReferenceDescriptor [packageName=a.b.c.MyClass.InnerClass, simpleName=SecondInner, qualifier=a.b.c.MyClass.InnerClass, qualifiedName=a.b.c.MyClass.InnerClass.SecondInner, isTypeArg=false, isBaseType=false]",
        new TypeReferenceDescriptor("a.b.c.MyClass.InnerClass.SecondInner").toString());
    assertEquals("TypeReferenceDescriptor [simpleName=SecondInner, qualifier=MyClass.InnerClass, qualifiedName=MyClass.InnerClass.SecondInner, isTypeArg=false, isBaseType=false]",
        new TypeReferenceDescriptor("MyClass$InnerClass$SecondInner").toString());
    assertEquals("TypeReferenceDescriptor [simpleName=MyClass, qualifiedName=MyClass, isTypeArg=false, isBaseType=false]",
        new TypeReferenceDescriptor("MyClass").toString());
    assertEquals("TypeReferenceDescriptor [simpleName=boolean, qualifiedName=boolean, isTypeArg=false, isBaseType=true]",
        new TypeReferenceDescriptor("boolean").toString());
  }
}
