/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class FixtureHelper {

  public static final Path ROOT = Paths.get("").toAbsolutePath().resolve("src/test/resources");
  public static final Path BASE = forClass(FixtureHelper.class);

  public static final Path CLASSIC_MODULE_DIR = BASE.resolve("classicModule");
  public static final Path EMPTY_MODULE_DIR = BASE.resolve("emptyModule");
  public static final Path ES_MODULE_DIR = BASE.resolve("esModule");
  public static final Path MINIMAL_MODULE_DIR = BASE.resolve("minimalModule");
  public static final Path NESTED_EXPORTS_MODULE_DIR = BASE.resolve("nestedExportsModule");
  public static final Path SIMPLE_MODULE_DIR = BASE.resolve("simpleModule");
  public static final Path SRC_MODULE_DIR = BASE.resolve("srcModule");

  private FixtureHelper() {
  }

  public static Path forClass(Class<?> testClass) {
    return ROOT.resolve(testClass.getPackageName().replace('.', '/'));
  }
}
