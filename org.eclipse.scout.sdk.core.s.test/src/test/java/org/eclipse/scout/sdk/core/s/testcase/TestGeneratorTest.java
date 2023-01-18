/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testcase;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.testing.context.UsernameExtension;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutClientJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link TestGeneratorTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(UsernameExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutClientJavaEnvironmentFactory.class)
public class TestGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/s/generator/testcase/";

  @Test
  public void testTestSourceBuilderWithDefaultValues(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);

    var generator = new TestGenerator<>()
        .asClientTest(true)
        .withElementName("MyTest")
        .withPackageName("org.eclipse.scout.sdk.core.s.test")
        .withRunner(scoutApi.ClientTestRunner().fqn())
        .withSession(null)
        .withRunWithSubjectValueBuilder(null);

    var source = generator.toJavaSource(env);
    assertTrue(source.indexOf('@' + scoutApi.RunWithSubject().simpleName() + "(\"anonymous") >= 0);
    assertTrue(source.indexOf('@' + scoutApi.RunWithClientSession().simpleName() + '(' + scoutApi.TestEnvironmentClientSession().simpleName()) >= 0);
    assertNoCompileErrors(env, generator);
    assertEqualsRefFile(env, REF_FILE_FOLDER + "TestGeneratorTest1.txt", generator);
  }

  @Test
  public void testTestSourceBuilderWithSpecificValues(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);
    var subjectValue = "myvalue";
    var iClientSession = scoutApi.IClientSession();
    var generator = new TestGenerator<>()
        .asClientTest(true)
        .withElementName("MyTest")
        .withPackageName("org.eclipse.scout.sdk.core.s.test")
        .withRunner(scoutApi.ClientTestRunner().fqn())
        .withSession(iClientSession.fqn())
        .withRunWithSubjectValueBuilder(b -> b.stringLiteral(subjectValue));

    var source = generator.toJavaSource(env);
    assertTrue(source.indexOf('@' + scoutApi.RunWithSubject().simpleName() + "(\"" + subjectValue) >= 0);
    assertTrue(source.indexOf('@' + scoutApi.RunWithClientSession().simpleName() + '(' + iClientSession.simpleName()) >= 0);
    assertNoCompileErrors(env, generator);
    assertEqualsRefFile(env, REF_FILE_FOLDER + "TestGeneratorTest2.txt", generator);
  }
}
