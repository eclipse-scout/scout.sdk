/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.testing;

import java.nio.file.Path;

import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson;
import org.eclipse.scout.sdk.core.typescript.testing.spi.TestingNodeModuleSpi;

public final class TestingPackageJsonHelper {

  private TestingPackageJsonHelper() {
  }

  public static IPackageJson parse(Path nodeModuleDir) {
    return new TestingNodeModuleSpi(nodeModuleDir).packageJson().api();
  }
}
