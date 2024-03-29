/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.lookupcall;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertNoCompileErrors;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link LookupCallGeneratorTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class LookupCallGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/s/generator/lookupcall/";

  @Test
  public void testLookupCallAllParams(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);

    // lookup service interface
    var lookupSvcIfc = PrimaryTypeGenerator.create()
        .withPackageName("org.eclipse.scout.sdk.core.s.test")
        .asPublic()
        .asInterface()
        .withElementName("IMyLookupService")
        .withInterface(scoutApi.ILookupService().fqn() + JavaTypes.C_GENERIC_START + String.class.getName() + JavaTypes.C_GENERIC_END);

    assertEqualsRefFile(env, REF_FILE_FOLDER + "LookupCall1.txt", lookupSvcIfc);
    var createdLookupSvcIfc = assertNoCompileErrors(env, lookupSvcIfc);

    // lookup call
    var lookupCallGenerator = new LookupCallGenerator<>()
        .withPackageName("org.eclipse.scout.sdk.core.s.test")
        .withElementName("MyLookupCall")
        .withSuperType(scoutApi.LookupCall().fqn())
        .withKeyType(String.class.getName())
        .withLookupServiceInterface(createdLookupSvcIfc.name());

    assertEqualsRefFile(env, REF_FILE_FOLDER + "LookupCall2.txt", lookupCallGenerator);
    assertNoCompileErrors(env, lookupCallGenerator);
  }
}
