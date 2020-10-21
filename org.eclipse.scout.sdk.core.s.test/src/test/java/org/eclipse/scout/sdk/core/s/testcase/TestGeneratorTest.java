/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.testcase;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutClientJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.UsernameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link TestGeneratorTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(UsernameExtension.class)
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutClientJavaEnvironmentFactory.class)
public class TestGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/s/generator/testcase/";

  @Test
  public void testTestSourceBuilderWithDefaultValues(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);

    TestGenerator<?> generator = new TestGenerator<>()
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
    TestGenerator<?> generator = new TestGenerator<>()
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
