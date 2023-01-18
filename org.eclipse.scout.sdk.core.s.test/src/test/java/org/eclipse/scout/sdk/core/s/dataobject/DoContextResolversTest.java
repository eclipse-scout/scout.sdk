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

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers.selectNamespace;
import static org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers.selectNewestTypeVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IPackage;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.testing.apidef.ApiRequirement;
import org.eclipse.scout.sdk.core.java.testing.apidef.EnabledFor;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithDoContext;
import org.junit.jupiter.api.Test;

import dataobject.context.FixtureNamespace;
import dataobject.context.FixtureTypeVersions.SdkFixture_1_0_0_0;

@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
@ExtendWithDoContext(namespace = FixtureNamespace.class, typeVersion = SdkFixture_1_0_0_0.class)
public class DoContextResolversTest {

  @Test
  @EnabledFor(api = IScoutApi.class, require = ApiRequirement.MIN, version = 22)
  public void testResolve(IJavaEnvironment environment) {
    var context = DoContextResolvers.resolve("dataobject.whatever", environment);
    assertEquals("sdk", context.namespaceId().orElseThrow());
    assertEquals(FixtureNamespace.class.getName(), context.namespace().orElseThrow().name());
    assertTrue(context.typeVersion().isEmpty()); // must be empty because fixture does not implement ITypeVersion
  }

  @Test
  public void testSelectNamespace() {
    assertEquals("d.e", selectNamespace("d.e", createTypeWithPackageMocks("a.b.c", "d.e", "f.g.h.i")).containingPackage().elementName());
    assertEquals("f.g.h.i", selectNamespace("f.g", createTypeWithPackageMocks("a.b.c", "d.e", "f.g.h.i")).containingPackage().elementName());
    assertEquals("abc", selectNamespace("abc.def.ghi.jkl", createTypeWithPackageMocks("xyz.def.ghi.jkl", "abc")).containingPackage().elementName());
  }

  @Test
  public void testSelectNewestTypeVersion() {
    assertEquals("Sdk_22_0_1", selectNewestTypeVersion(createTypeWithNameMocks("", "Abc_Def", "Abc", "Sdk_11_0", "Sdk_2_1_0", "Sdk_22_0_0", "Sdk_22_0_1")).elementName());
  }

  protected static Stream<IType> createTypeWithPackageMocks(String... packageNames) {
    return Stream.of(packageNames).map(DoContextResolversTest::createTypeWithPackageMock);
  }

  protected static IType createTypeWithPackageMock(String packageName) {
    var pck = mock(IPackage.class);
    when(pck.elementName()).thenReturn(packageName);
    var mock = mock(IType.class);
    when(mock.containingPackage()).thenReturn(pck);
    return mock;
  }

  protected static List<IType> createTypeWithNameMocks(String... names) {
    return Stream.of(names)
        .map(DoContextResolversTest::createTypeWithNameMock)
        .collect(toList());
  }

  protected static IType createTypeWithNameMock(String name) {
    var mock = mock(IType.class);
    when(mock.elementName()).thenReturn(name);
    return mock;
  }
}
