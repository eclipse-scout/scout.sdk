/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
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
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutClientJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.UsernameExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
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
    TestGenerator<?> generator = new TestGenerator<>()
        .asClientTest(true)
        .withElementName("MyTest")
        .withPackageName("org.eclipse.scout.sdk.core.s.test")
        .withRunner(IScoutRuntimeTypes.ClientTestRunner)
        .withSession(null)
        .withRunWithSubjectValueBuilder(null);

    StringBuilder source = generator.toJavaSource(env);
    assertTrue(source.indexOf('@' + JavaTypes.simpleName(IScoutRuntimeTypes.RunWithSubject) + "(\"anonymous") >= 0);
    assertTrue(source.indexOf('@' + JavaTypes.simpleName(IScoutRuntimeTypes.RunWithClientSession) + '(' + JavaTypes.simpleName(IScoutRuntimeTypes.TestEnvironmentClientSession)) >= 0);
    assertNoCompileErrors(env, generator);
    assertEqualsRefFile(env, REF_FILE_FOLDER + "TestGeneratorTest1.txt", generator);
  }

  @Test
  public void testTestSourceBuilderWithSpecificValues(IJavaEnvironment env) {

    String subjectValue = "myvalue";
    TestGenerator<?> generator = new TestGenerator<>()
        .asClientTest(true)
        .withElementName("MyTest")
        .withPackageName("org.eclipse.scout.sdk.core.s.test")
        .withRunner(IScoutRuntimeTypes.ClientTestRunner)
        .withSession(IScoutRuntimeTypes.IClientSession)
        .withRunWithSubjectValueBuilder(b -> b.stringLiteral(subjectValue));

    StringBuilder source = generator.toJavaSource(env);
    assertTrue(source.indexOf('@' + JavaTypes.simpleName(IScoutRuntimeTypes.RunWithSubject) + "(\"" + subjectValue) >= 0);
    assertTrue(source.indexOf('@' + JavaTypes.simpleName(IScoutRuntimeTypes.RunWithClientSession) + '(' + JavaTypes.simpleName(IScoutRuntimeTypes.IClientSession)) >= 0);
    assertNoCompileErrors(env, generator);
    assertEqualsRefFile(env, REF_FILE_FOLDER + "TestGeneratorTest2.txt", generator);
  }
}
