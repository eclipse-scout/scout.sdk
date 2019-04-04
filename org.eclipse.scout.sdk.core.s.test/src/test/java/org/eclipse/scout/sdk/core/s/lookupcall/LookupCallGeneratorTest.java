/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.lookupcall;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;

import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link LookupCallGeneratorTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class LookupCallGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/s/generator/lookupcall/";

  @Test
  public void testLookupCallAllParams(IJavaEnvironment env) {
    // lookup service interface
    PrimaryTypeGenerator<?> lookupSvcIfc = PrimaryTypeGenerator.create()
        .withPackageName("org.eclipse.scout.sdk.core.s.test")
        .asPublic()
        .asInterface()
        .withElementName("IMyLookupService")
        .withInterface(IScoutRuntimeTypes.ILookupService + JavaTypes.C_GENERIC_START + String.class.getName() + JavaTypes.C_GENERIC_END);

    assertEqualsRefFile(env, REF_FILE_FOLDER + "LookupCall1.txt", lookupSvcIfc);
    IType createdLookupSvcIfc = assertNoCompileErrors(env, lookupSvcIfc);

    // lookup call
    LookupCallGenerator<?> lookupCallGenerator = new LookupCallGenerator<>()
        .withPackageName("org.eclipse.scout.sdk.core.s.test")
        .withElementName("MyLookupCall")
        .withSuperClass(IScoutRuntimeTypes.LookupCall + JavaTypes.C_GENERIC_START + String.class.getName() + JavaTypes.C_GENERIC_END)
        .withLookupServiceInterface(createdLookupSvcIfc.name());

    assertEqualsRefFile(env, REF_FILE_FOLDER + "LookupCall2.txt", lookupCallGenerator);
    assertNoCompileErrors(env, lookupCallGenerator);
  }
}
