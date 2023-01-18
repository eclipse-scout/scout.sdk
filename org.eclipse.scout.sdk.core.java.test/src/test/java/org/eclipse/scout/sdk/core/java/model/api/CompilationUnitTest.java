/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.fixture.BaseClass;
import org.eclipse.scout.sdk.core.java.fixture.Long;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class CompilationUnitTest {
  @Test
  public void testIcu(IJavaEnvironment env) {
    var baseClassIcu = env.requireType(BaseClass.class.getName()).requireCompilationUnit();
    assertEquals(BaseClass.class.getName(), baseClassIcu.requireMainType().name());
    assertEquals(BaseClass.class.getSimpleName() + JavaTypes.JAVA_FILE_SUFFIX, baseClassIcu.toWorkingCopy().fileName().orElseThrow());

    var firstImport = baseClassIcu.imports().findAny().orElseThrow();
    assertEquals("import java.io.FileNotFoundException;", firstImport.toWorkingCopy().toSource(identity(), new BuilderContext()).toString());
    assertEquals("package org.eclipse.scout.sdk.core.java.fixture;", baseClassIcu.containingPackage().toWorkingCopy().toSource(identity(), new BuilderContext()).toString());

    assertEquals(5, baseClassIcu.imports().count());
    assertEquals(1, baseClassIcu.types().stream().count());
    assertEquals(2, baseClassIcu.requireMainType().innerTypes().stream().count());

    assertEquals(BaseClass.class.getSimpleName() + JavaTypes.JAVA_FILE_SUFFIX, baseClassIcu.elementName());
    assertEquals(Paths.get(BaseClass.class.getName().replace('.', '/') + JavaTypes.JAVA_FILE_SUFFIX), baseClassIcu.path());

    assertTrue(baseClassIcu.containingClasspathFolder().orElseThrow().path().endsWith(Paths.get(FixtureHelper.FIXTURE_PATH)));
    assertTrue(baseClassIcu.absolutePath().orElseThrow().toString().replace('\\', '/').endsWith("org/eclipse/scout/sdk/core/java/fixture/BaseClass.java"));
  }

  @Test
  public void testFindTypeBySimpleName(IJavaEnvironment env) {
    var baseClassIcu = env.requireType(BaseClass.class.getName()).requireCompilationUnit();
    var sdkLong = baseClassIcu.resolveTypeBySimpleName(Long.class.getSimpleName());

    assertTrue(sdkLong.isPresent());
    assertEquals(org.eclipse.scout.sdk.core.java.fixture.Long.class.getName(), sdkLong.orElseThrow().name());
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    var baseClassIcu = env.requireType(BaseClass.class.getName()).requireCompilationUnit();
    assertEquals(BaseClass.class.getName() + JavaTypes.JAVA_FILE_SUFFIX, baseClassIcu.toString());
  }
}
