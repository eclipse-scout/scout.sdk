/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dataobject;

import static java.util.stream.Collectors.joining;
import static org.eclipse.scout.sdk.core.s.dataobject.DataObjectModel.wrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

import dataobject.BaseDo;
import dataobject.ChildDo;
import dataobject.IgnoredDo;
import dataobject.SampleDo;

@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class DataObjectModelTest {

  @Test
  public void test(IJavaEnvironment env) {
    assertFalse(wrap(env.requireType(List.class.getName())).isPresent());
    assertFalse(wrap(env.requireType(ArrayList.class.getName())).isPresent());
    assertFalse(new EmptyJavaEnvironmentFactory().call(je -> wrap(je.requireType(Long.class.getName()))).isPresent());

    var baseDo = env.requireType(BaseDo.class.getName());
    var baseDoModel = wrap(baseDo).get();
    assertEquals("DataObjectModel [source=dataobject.BaseDo, nodes=[" +
        "DataObjectNode [name='id', kind=VALUE, dataType='java.lang.CharSequence', inherited=false, hasJavaDoc=false], " +
        "DataObjectNode [name='versions', kind=LIST, dataType='java.lang.Long', inherited=false, hasJavaDoc=false], " +
        "DataObjectNode [name='enabled', kind=VALUE, dataType='java.lang.Boolean', inherited=false, hasJavaDoc=true], " +
        "DataObjectNode [name='abstractNodeTestingInherit', kind=VALUE, dataType='java.lang.String', inherited=false, hasJavaDoc=false]]]", baseDoModel.toString());

    var childDo = env.requireType(ChildDo.class.getName());
    var model = wrap(childDo).orElseThrow();
    assertSame(childDo, model.unwrap());
    assertEquals(4, model.nodes().size());
    assertEquals(
        "DataObjectModel [source=" + childDo.name() + ", nodes=[" +
            "DataObjectNode [name='abstractNodeTestingInherit', kind=VALUE, dataType='java.lang.String', inherited=true, hasJavaDoc=false], " +
            "DataObjectNode [name='id', kind=VALUE, dataType='java.lang.CharSequence', inherited=true, hasJavaDoc=false], " +
            "DataObjectNode [name='versions', kind=LIST, dataType='java.lang.Long', inherited=true, hasJavaDoc=false], " +
            "DataObjectNode [name='enabled', kind=VALUE, dataType='java.lang.Boolean', inherited=true, hasJavaDoc=true]]]",
        model.toString());
  }

  @Test
  public void testIgnore(IJavaEnvironment env) {
    assertFalse(wrap(env.requireType(IgnoredDo.class.getName())).isPresent());
    var model = wrap(env.requireType(SampleDo.class.getName())).orElseThrow();
    var nodeNames = model.nodes().stream()
        .map(DataObjectNode::name)
        .sorted()
        .collect(joining(","));
    assertEquals("enabled,stringAttribute,versions", nodeNames);
  }
}
