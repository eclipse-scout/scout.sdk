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
package org.eclipse.scout.sdk.core.s.testing.context;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWithTestingEnvironment(
    primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class),
    dto = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class TestingEnvironmentTest {

  @Test
  public void testAbsolutePathWithInMemoryCu(TestingEnvironment env) {
    var je = env.primaryEnvironment();
    var sourceFolder = je.primarySourceFolder().get();
    var writtenCu = env
        .writeCompilationUnit(PrimaryTypeGenerator.create().withPackageName("").asPublic().withElementName("TestClass"), sourceFolder)
        .requireCompilationUnit();
    assertSame(sourceFolder, writtenCu.containingClasspathFolder().get());
  }

  @Test
  public void testNewEnvironmentKnowsNewlyCreatedCompilationUnits(TestingEnvironment env) {
    var pckName = "org.eclipse.scout.sdk.test";
    var className = "TestClass";
    var generator = PrimaryTypeGenerator.create()
        .withPackageName(pckName)
        .asPublic()
        .withElementName(className);
    var targetSourceFolder = env.primarySourceFolder();
    var createdInMemoryType = env.writeCompilationUnit(generator, targetSourceFolder, null);
    var fqn = pckName + JavaTypes.C_DOT + className;

    // create a new Java environment with the primary source folder of the first environment on the classpath
    // this environment should automatically get the newly created unit when initialized by the environment

    new ScoutSharedJavaEnvironmentFactory().accept(newEnvWithUnitOnCp -> {
      assertNotSame(newEnvWithUnitOnCp, createdInMemoryType.javaEnvironment()); // check it is a new environment
      assertTrue(newEnvWithUnitOnCp.classpathContains(targetSourceFolder.path())); // check it contains the classpath as well
      env.initNewJavaEnvironment(newEnvWithUnitOnCp.unwrap()); // simulate the init
      assertNoCompileErrors(newEnvWithUnitOnCp.requireType(fqn));
    });

    // create a new Java environment not having the path. It should not get the unit
    var root = Paths.get("").toAbsolutePath();
    var newEnvWithoutHavingTheUnitOnThePath = env.findJavaEnvironment(root).get();
    assertNotSame(newEnvWithoutHavingTheUnitOnThePath, createdInMemoryType.javaEnvironment()); // check it is a new environment
    assertFalse(newEnvWithoutHavingTheUnitOnThePath.classpathContains(targetSourceFolder.path())); // check it does not contain the classpath
    assertTrue(newEnvWithoutHavingTheUnitOnThePath.findType(fqn).isEmpty());
  }
}