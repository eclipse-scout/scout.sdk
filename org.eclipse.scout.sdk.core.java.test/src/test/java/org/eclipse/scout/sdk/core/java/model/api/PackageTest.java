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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Paths;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.fixture.AbstractBaseClass;
import org.eclipse.scout.sdk.core.java.fixture.ChildClass;
import org.eclipse.scout.sdk.core.java.fixture.sub.ImportTestClass2;
import org.eclipse.scout.sdk.core.java.fixture.sub.PackageAnnotation;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class PackageTest {

  private static final String FIXTURE_PACKAGE = ChildClass.class.getPackage().getName();

  @Test
  public void testPackageName(IJavaEnvironment env) {
    var childClassIcu = env.requireType(ChildClass.class.getName()).requireCompilationUnit();
    var pck = childClassIcu.containingPackage();
    assertEquals(FIXTURE_PACKAGE, pck.elementName());
    assertEquals("package " + FIXTURE_PACKAGE + ';', pck.toString());
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    var childClassIcu = env.requireType(ChildClass.class.getName()).requireCompilationUnit();
    var pck = childClassIcu.containingPackage();
    assertFalse(Strings.isBlank(pck.toString()));
  }

  @Test
  public void testPackageNameFromType(IJavaEnvironment env) {
    var childClass = env.requireType(ChildClass.class.getName());
    var pck = childClass.containingPackage();
    assertEquals(FIXTURE_PACKAGE, pck.elementName());
    assertEquals("package " + FIXTURE_PACKAGE + ';', pck.toString());
  }

  @Test
  public void testPackageAnnotationNotExisting(IJavaEnvironment env) {
    var testClass = env.requireType(AbstractBaseClass.class.getName());
    var pck = testClass.containingPackage();
    assertEquals(0, pck.annotations().stream().count());
  }

  @Test
  public void testPackageAnnotation(IJavaEnvironment env) {
    var testClass = env.requireType(ImportTestClass2.class.getName());
    var pck = testClass.containingPackage();
    var expectedPackageName = ImportTestClass2.class.getPackage().getName();
    assertEquals(expectedPackageName, pck.elementName());
    assertNotNull(pck.packageInfo().orElse(null));
    var org = pck.parent()
        .flatMap(IPackage::parent)
        .flatMap(IPackage::parent)
        .flatMap(IPackage::parent)
        .flatMap(IPackage::parent)
        .flatMap(IPackage::parent)
        .flatMap(IPackage::parent)
        .orElseThrow();
    assertEquals("org", org.elementName());
    assertNull(org.parent().orElseThrow().elementName());
    assertFalse(org.parent().orElseThrow().parent().isPresent());
    assertEquals(1, pck.children().count());
    assertEquals(Paths.get(expectedPackageName.replace(JavaTypes.C_DOT, '/')), pck.asPath());
    var pckAnnotation = pck.annotations().withName(PackageAnnotation.class.getName()).first().orElseThrow();
    assertEquals("testValue", pckAnnotation.element("testAttrib").orElseThrow().value().as(String.class));
  }

  @Test
  public void testPackageNameFromSuperType(IJavaEnvironment env) {
    var childClass = env.requireType(ChildClass.class.getName()).requireSuperClass();
    var pck = childClass.containingPackage();
    assertEquals(FIXTURE_PACKAGE, pck.elementName());
    assertEquals("package " + FIXTURE_PACKAGE + ';', pck.toString());
  }
}
