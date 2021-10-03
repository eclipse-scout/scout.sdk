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
package org.eclipse.scout.sdk.core.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
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
