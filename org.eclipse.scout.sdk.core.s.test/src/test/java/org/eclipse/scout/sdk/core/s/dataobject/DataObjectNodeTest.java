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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
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
    assertEquals(DataObjectNodeKind.LIST, valueOf(env.requireType(scoutApi.DoList())).orElseThrow());
    assertEquals(DataObjectNodeKind.VALUE, valueOf(env.requireType(scoutApi.DoValue())).orElseThrow());
    assertFalse(valueOf(env.requireType(Long.class.getName())).isPresent());
    assertFalse(valueOf((IType) null).isPresent());

    var checkWithoutScout = new EmptyJavaEnvironmentFactory().call(je -> valueOf(je.requireType(Long.class.getName())));
    assertFalse(checkWithoutScout.isPresent());
  }

  @Test
  public void testGetters() {
    var name = "myName";
    var dataType = mock(IType.class);
    var method = mock(IMethod.class);
    when(method.elementName()).thenReturn(name);
    var node1 = new DataObjectNode(DataObjectNodeKind.VALUE, method, dataType, true, true);
    var node2 = new DataObjectNode(DataObjectNodeKind.LIST, method, dataType, false, false);

    assertSame(DataObjectNodeKind.VALUE, node1.kind());
    assertEquals(name, node1.name());
    assertSame(method, node1.method());
    assertSame(dataType, node1.dataType());
    assertTrue(node1.isInherited());
    assertTrue(node1.hasJavaDoc());

    assertSame(DataObjectNodeKind.LIST, node2.kind());
    assertEquals(name, node2.name());
    assertSame(method, node2.method());
    assertSame(dataType, node2.dataType());
    assertFalse(node2.isInherited());
    assertFalse(node2.hasJavaDoc());
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "EqualsBetweenInconvertibleTypes", "EqualsWithItself"})
  public void testHashCodeEqualsToString() {
    var name = "myName";
    var otherName = "otherName";
    var dataType = mock(IType.class);
    when(dataType.toString()).thenReturn(String.class.getName());
    var method1 = mock(IMethod.class);
    when(method1.elementName()).thenReturn(name);
    var method2 = mock(IMethod.class);
    when(method2.elementName()).thenReturn(otherName);

    var node1 = new DataObjectNode(DataObjectNodeKind.VALUE, method1, dataType, true, false);
    var node2 = new DataObjectNode(DataObjectNodeKind.LIST, method1, dataType, false, true);
    var node3 = new DataObjectNode(DataObjectNodeKind.VALUE, method2, dataType, true, true);

    assertFalse(node1.equals(null));
    assertFalse(node1.equals(""));
    assertTrue(node1.equals(node1));
    assertFalse(node1.equals(node2));
    assertNotEquals(node1.hashCode(), node2.hashCode());
    assertNotEquals(node1, node3);

    assertEquals(DataObjectNode.class.getSimpleName() + " [name='myName', kind=VALUE, dataType='java.lang.String', inherited=true, hasJavaDoc=false]", node1.toString());
  }
}
