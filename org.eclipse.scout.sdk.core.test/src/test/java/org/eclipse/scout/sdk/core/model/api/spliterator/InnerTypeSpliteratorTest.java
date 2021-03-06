/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api.spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Spliterator;

import org.eclipse.scout.sdk.core.fixture.AbstractBaseClass;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
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
