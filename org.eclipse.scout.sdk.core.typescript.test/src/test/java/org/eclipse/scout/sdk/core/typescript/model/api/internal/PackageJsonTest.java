/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson;
import org.eclipse.scout.sdk.core.typescript.testing.TestingPackageJsonHelper;
import org.junit.jupiter.api.Test;

public class PackageJsonTest {

  @Test
  public void testPackageJsonParser() {
    var packageJsonDirectory = FixtureHelper.SIMPLE_MODULE_DIR;
    var packageJson = TestingPackageJsonHelper.parse(packageJsonDirectory);
    assertEquals("./src/exports-index.ts", packageJson.main().orElseThrow());
    assertEquals("23.1.0-snapshot", packageJson.version());
    assertEquals("@eclipse-scout/sdk-test-module", packageJson.name());
    assertSame(packageJsonDirectory, packageJson.directory());
    assertEquals(packageJsonDirectory.resolve(IPackageJson.FILE_NAME), packageJson.location());
    assertEquals(1880, packageJson.source().orElseThrow().length());

    assertEquals("./dist/d.ts/src/index.d.ts", packageJson.jsonString("publishConfig", "types").orElseThrow());
    assertTrue(packageJson.jsonObject("nonExisting").isEmpty());
    assertEquals("https://www.eclipse.org/scout", packageJson.jsonString("homepage").orElseThrow());
    assertEquals(17, packageJson.jsonObject("").orElseThrow().size());

    assertThrows(IllegalArgumentException.class, () -> TestingPackageJsonHelper.parse(FixtureHelper.BASE).name()); // no package.json
    assertThrows(IllegalArgumentException.class, () -> TestingPackageJsonHelper.parse(FixtureHelper.EMPTY_MODULE_DIR).name()); // mandatory attributes missing
  }

  @Test
  public void testMain() {
    assertEquals("./src/main-index.ts", TestingPackageJsonHelper.parse(FixtureHelper.CLASSIC_MODULE_DIR).main().orElseThrow());
    assertEquals("./src/module-index.ts", TestingPackageJsonHelper.parse(FixtureHelper.ES_MODULE_DIR).main().orElseThrow());
    assertTrue(TestingPackageJsonHelper.parse(FixtureHelper.MINIMAL_MODULE_DIR).main().isEmpty());
    assertEquals("./dist/eclipse-scout-core.esm.js", TestingPackageJsonHelper.parse(FixtureHelper.NESTED_EXPORTS_MODULE_DIR).main().orElseThrow());
    assertEquals("./src/exports-index.ts", TestingPackageJsonHelper.parse(FixtureHelper.SIMPLE_MODULE_DIR).main().orElseThrow());
    assertEquals("src/index.js", TestingPackageJsonHelper.parse(FixtureHelper.SRC_MODULE_DIR).main().orElseThrow());
    assertEquals(315, TestingPackageJsonHelper.parse(FixtureHelper.SRC_MODULE_DIR).containingModule().source().orElseThrow().length());
  }

  @Test
  @SuppressWarnings({"ConstantConditions", "EqualsBetweenInconvertibleTypes", "SimplifiableAssertion", "EqualsWithItself"})
  public void testPackageHashCodeEqualsToString() {
    var packageJson1 = TestingPackageJsonHelper.parse(FixtureHelper.SIMPLE_MODULE_DIR);
    var packageJson2 = TestingPackageJsonHelper.parse(FixtureHelper.MINIMAL_MODULE_DIR);
    var packageJson3 = TestingPackageJsonHelper.parse(FixtureHelper.SIMPLE_MODULE_DIR);

    assertEquals(packageJson1, packageJson3);
    assertNotEquals(packageJson1, packageJson2);
    assertFalse(packageJson1.equals(null));
    assertTrue(packageJson1.equals(packageJson1));
    assertFalse(packageJson1.equals(""));
    assertNotSame(packageJson1, packageJson3);

    assertEquals("@eclipse-scout/sdk-test-module@23.1.0-snapshot", packageJson1.toString());
    assertNotEquals(packageJson1.toString(), packageJson2.toString());
    assertEquals(packageJson1.toString(), packageJson3.toString());

    assertEquals(packageJson1.hashCode(), packageJson3.hashCode());
    assertNotEquals(packageJson1.hashCode(), packageJson2.hashCode());
  }
}
