/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.query.DependencyQuery;
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.SimpleNodeModuleSpi;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public interface IPackageJson extends INodeElement {

  String FILE_NAME = "package.json";

  @Override
  PackageJsonSpi spi();

  Path location();

  Path directory();

  Optional<String> main();

  Optional<CharSequence> mainContent();

  Optional<String> jsonString(String... pathSegments);

  Optional<? extends JsonObject> jsonObject(String... pathSegments);

  <T extends JsonValue> Optional<T> jsonValue(Class<T> type, String... pathSegments);

  String version();

  DependencyQuery dependencies();

  Optional<INodeModule> dependency(String name);

  static IPackageJson parse(Path nodeModuleDir) {
    return new SimpleNodeModuleSpi(nodeModuleDir).packageJson().api();
  }
}
