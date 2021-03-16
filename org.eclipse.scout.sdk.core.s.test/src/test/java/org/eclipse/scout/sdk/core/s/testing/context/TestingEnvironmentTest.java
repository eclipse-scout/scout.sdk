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

import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
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
}
