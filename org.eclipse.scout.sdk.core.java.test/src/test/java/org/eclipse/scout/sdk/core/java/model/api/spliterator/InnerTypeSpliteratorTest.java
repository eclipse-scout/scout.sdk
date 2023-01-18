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

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.fixture.AbstractBaseClass;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link InnerTypeSpliteratorTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class InnerTypeSpliteratorTest {
  @Test
  public void testInnerTypes(IJavaEnvironment env) {
    var abc = env.requireType(AbstractBaseClass.class.getName());
    Spliterator<IType> ts = new InnerTypeSpliterator(abc, false);
    assertEquals("InnerOne,Leaf", traverse(ts));
  }

  @Test
  public void testInnerTypesRecursive(IJavaEnvironment env) {
    var abc = env.requireType(AbstractBaseClass.class.getName());
    Spliterator<IType> ts = new InnerTypeSpliterator(abc, true);
    assertEquals("InnerOne,Leaf,Leaf2,InnerTwo,Leaf3,Leaf4", traverse(ts));
  }

  @Test
  public void testInnerTypesFromList(IJavaEnvironment env) {
    var owner = env.requireType(AbstractBaseClass.class.getName());
    Spliterator<IType> ts = new InnerTypeSpliterator(InnerTypeSpliterator.innerTypesOf(owner), false);
    assertEquals("InnerOne,Leaf", traverse(ts));
  }

  @Test
  public void testInnerTypesFromListRecursive(IJavaEnvironment env) {
    var owner = env.requireType(AbstractBaseClass.class.getName());
    Spliterator<IType> ts = new InnerTypeSpliterator(InnerTypeSpliterator.innerTypesOf(owner), true);
    assertEquals("InnerOne,Leaf,Leaf2,InnerTwo,Leaf3,Leaf4", traverse(ts));
  }

  static String traverse(Spliterator<IType> ts) {
    var b = new StringBuilder();
    //noinspection StatementWithEmptyBody
    while (ts.tryAdvance(a -> b.append(a.elementName()).append(JavaTypes.C_COMMA))) {
    }
    if (b.length() > 0) {
      b.deleteCharAt(b.length() - 1);
    }
    return b.toString();
  }
}
