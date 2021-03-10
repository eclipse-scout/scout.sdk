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
package org.eclipse.scout.sdk.core.s.dataobject;

import static org.eclipse.scout.sdk.core.s.dataobject.DataObjectNode.DataObjectNodeKind.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.dataobject.DataObjectNode.DataObjectNodeKind;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class DataObjectNodeTest {
  @Test
  public void testDataObjectNodeKindValueOf(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);
    assertEquals(DataObjectNodeKind.DoList, valueOf(env.requireType(scoutApi.DoList())).get());
    assertEquals(DataObjectNodeKind.DoValue, valueOf(env.requireType(scoutApi.DoValue())).get());
    assertFalse(valueOf(env.requireType(Long.class.getName())).isPresent());
    assertFalse(valueOf((IType) null).isPresent());

    var checkWithoutScout = new EmptyJavaEnvironmentFactory().call(je -> valueOf(je.requireType(Long.class.getName())));
    assertFalse(checkWithoutScout.isPresent());
  }

  @Test
  public void testGetters(IJavaEnvironment env) {
    var name = "myName";
    var dataType = env.requireType(String.class.getName());
    var node1 = new DataObjectNode(DataObjectNodeKind.DoValue, name, dataType, true);
    var node2 = new DataObjectNode(DataObjectNodeKind.DoList, name, dataType, false);

    assertSame(DataObjectNodeKind.DoValue, node1.kind());
    assertSame(name, node1.name());
    assertSame(dataType, node1.dataType());
    assertTrue(node1.isInherited());

    assertSame(DataObjectNodeKind.DoList, node2.kind());
    assertSame(name, node2.name());
    assertSame(dataType, node2.dataType());
    assertFalse(node2.isInherited());
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "EqualsBetweenInconvertibleTypes", "EqualsWithItself"})
  public void testHashCodeEqualsToString(IJavaEnvironment env) {
    var name = "myName";
    var dataType = env.requireType(String.class.getName());
    var node1 = new DataObjectNode(DataObjectNodeKind.DoValue, name, dataType, true);
    var node2 = new DataObjectNode(DataObjectNodeKind.DoList, name, dataType, false);
    var node3 = new DataObjectNode(DataObjectNodeKind.DoValue, "otherName", dataType, true);

    assertFalse(node1.equals(null));
    assertFalse(node1.equals(""));
    assertTrue(node1.equals(node1));
    assertFalse(node1.equals(node2));
    assertNotEquals(node1.hashCode(), node2.hashCode());
    assertNotEquals(node1, node3);

    assertEquals(DataObjectNode.class.getSimpleName() + " [name='myName', kind=DoValue, dataType='java.lang.String', inherited=true]", node1.toString());
  }
}
