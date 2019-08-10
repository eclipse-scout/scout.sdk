/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class PackageTest {

  private static final String FIXTURE_PACKAGE = ChildClass.class.getPackage().getName();

  @Test
  public void testPackageName(IJavaEnvironment env) {
    ICompilationUnit childClassIcu = env.requireType(ChildClass.class.getName()).requireCompilationUnit();
    IPackage pck = childClassIcu.containingPackage();
    assertEquals(FIXTURE_PACKAGE, pck.elementName());
    assertEquals("package " + FIXTURE_PACKAGE + ';', pck.toString());
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    ICompilationUnit childClassIcu = env.requireType(ChildClass.class.getName()).requireCompilationUnit();
    IPackage pck = childClassIcu.containingPackage();
    assertFalse(Strings.isBlank(pck.toString()));
  }

  @Test
  public void testPackageNameFromType(IJavaEnvironment env) {
    IType childClass = env.requireType(ChildClass.class.getName());
    IPackage pck = childClass.containingPackage();
    assertEquals(FIXTURE_PACKAGE, pck.elementName());
    assertEquals("package " + FIXTURE_PACKAGE + ';', pck.toString());
  }

  @Test
  public void testPackageNameFromSuperType(IJavaEnvironment env) {
    IType childClass = env.requireType(ChildClass.class.getName()).requireSuperClass();
    IPackage pck = childClass.containingPackage();
    assertEquals(FIXTURE_PACKAGE, pck.elementName());
    assertEquals("package " + FIXTURE_PACKAGE + ';', pck.toString());
  }
}
