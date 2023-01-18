/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Spliterator;

import org.eclipse.scout.sdk.core.java.fixture.ChildClass;
import org.eclipse.scout.sdk.core.java.fixture.ClassWithMultipleIfc;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link SuperTypeHierarchySpliteratorTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class SuperTypeHierarchySpliteratorTest {
  @Test
  public void testEmptySpliterator(IJavaEnvironment env) {
    Spliterator<IType> s = new SuperTypeHierarchySpliterator(env.requireType(ChildClass.class.getName()), false, false, false);
    assertEquals("", InnerTypeSpliteratorTest.traverse(s));
  }

  @Test
  public void testSuperClasses(IJavaEnvironment env) {
    Spliterator<IType> s = new SuperTypeHierarchySpliterator(env.requireType(ChildClass.class.getName()), true, false, false);
    assertEquals("BaseClass,Object", InnerTypeSpliteratorTest.traverse(s));
  }

  @Test
  public void testSuperInterfaces(IJavaEnvironment env) {
    Spliterator<IType> s = new SuperTypeHierarchySpliterator(env.requireType(ChildClass.class.getName()), false, true, false);
    assertEquals("InterfaceLevel0,InterfaceLevel1,InterfaceLevel2", InnerTypeSpliteratorTest.traverse(s));
  }

  @Test
  public void testAllSuperTypesWithSelf(IJavaEnvironment env) {
    Spliterator<IType> s = new SuperTypeHierarchySpliterator(env.requireType(ChildClass.class.getName()), true, true, true);
    assertEquals("ChildClass,BaseClass,InterfaceLevel0,Object,InterfaceLevel1,InterfaceLevel2", InnerTypeSpliteratorTest.traverse(s));
  }

  @Test
  public void testAllSuperTypes(IJavaEnvironment env) {
    Spliterator<IType> s = new SuperTypeHierarchySpliterator(env.requireType(ChildClass.class.getName()), true, true, false);
    assertEquals("BaseClass,InterfaceLevel0,Object,InterfaceLevel1,InterfaceLevel2", InnerTypeSpliteratorTest.traverse(s));
  }

  @Test
  public void testMultiSuperInterfaces(IJavaEnvironment env) {
    var t = env.requireType(ClassWithMultipleIfc.class.getName());
    Spliterator<IType> s = new SuperTypeHierarchySpliterator(t, false, true, false);
    assertEquals("InterfaceLevel0,InterfaceLevel1,InterfaceLevel2", InnerTypeSpliteratorTest.traverse(s));
  }

  @Test
  public void testAllSuperTypesMultiSuperInterfaces(IJavaEnvironment env) {
    var t = env.requireType(ClassWithMultipleIfc.class.getName());
    Spliterator<IType> s = new SuperTypeHierarchySpliterator(t, true, true, false);
    assertEquals("BaseClassWithMultipleIfc,InterfaceLevel0,InterfaceLevel1,Object,InterfaceLevel2", InnerTypeSpliteratorTest.traverse(s));
  }
}
