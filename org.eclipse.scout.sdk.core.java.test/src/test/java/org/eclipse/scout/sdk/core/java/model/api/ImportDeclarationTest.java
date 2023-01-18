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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;

import org.eclipse.scout.sdk.core.java.fixture.ChildClass;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class ImportDeclarationTest {
  @Test
  public void testImportDeclaration(IJavaEnvironment env) {
    var childClassIcu = env.requireType(ChildClass.class.getName()).requireCompilationUnit();
    var imp = childClassIcu.imports().findAny().orElseThrow();
    assertEquals(childClassIcu, imp.compilationUnit());
    assertEquals(IOException.class.getPackage().getName(), imp.qualifier());
    assertEquals(IOException.class.getSimpleName(), imp.elementName());
    assertEquals(IOException.class.getName(), imp.name());
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    var childClassIcu = env.requireType(ChildClass.class.getName()).requireCompilationUnit();
    var imp = childClassIcu.imports().findAny().orElseThrow();
    assertFalse(Strings.isBlank(imp.toString()));
  }
}
