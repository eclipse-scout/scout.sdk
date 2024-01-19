/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dataobject;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertNoCompileErrors;
import static org.eclipse.scout.sdk.core.s.testing.ScoutSdkAssertions.assertEqualsVersionedRefFile;

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.apidef.ApiRequirement;
import org.eclipse.scout.sdk.core.java.testing.apidef.EnabledFor;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.dataobject.DataObjectNode.DataObjectNodeKind;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

import dataobject.context.FixtureTypeVersions.SdkFixture_1_0_0_0;

@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class DataObjectGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/s/generator/dataobject/";

  @Test
  public void testEmptyDoGenerator(IJavaEnvironment env) {
    var generator = new DataObjectGenerator<>()
        .withElementName("MyTestDo")
        .withPackageName("test.pck");
    assertNoCompileErrors(env, generator);
    assertEqualsRefFile(env, REF_FILE_FOLDER + "DataObject0.txt", generator);
  }

  @Test
  public void testDoGeneratorWithoutNodes(IJavaEnvironment env) {
    var generator = new DataObjectGenerator<>()
        .withNamespace("sdk")
        .withTypeVersion(SdkFixture_1_0_0_0.class.getName())
        .withElementName("MyTestDo")
        .withPackageName("test.pck");
    if (env.requireApi(IScoutApi.class).maxLevel().major() >= 22) {
      // type version is a class literal
      assertEqualsRefFile(env, REF_FILE_FOLDER + "DataObject1.txt", generator);
    }
    else {
      // type version is a string literal
      assertEqualsRefFile(env, REF_FILE_FOLDER + "DataObject3.txt", generator);
    }
  }

  @Test
  @EnabledFor(api = IScoutApi.class, require = ApiRequirement.MIN, version = 22) // only Scout >= 22 has kind Collection & Set
  public void testDoGenerator(IJavaEnvironment env) {
    var generator = new DataObjectGenerator<>()
        .withNamespace("sdk")
        .withTypeVersion(SdkFixture_1_0_0_0.class.getName())
        .withElementName("MyTestDo")
        .withPackageName("test.pck")
        .withNode("longValue", DataObjectNodeKind.VALUE, Long.class.getName())
        .withNode("stringValues", DataObjectNodeKind.LIST, String.class.getName())
        .withNodeFunc("intValues", DataObjectNodeKind.COLLECTION, c -> Integer.class.getName())
        .withNodeFrom("lookupRowSet", DataObjectNodeKind.SET, IScoutApi.class, api -> api.ILookupRow().fqn());
    assertEqualsVersionedRefFile(env, REF_FILE_FOLDER, "DataObject2.txt", generator);
  }
}
