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

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.query.DependencyQuery;
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi;

public interface IPackageJson extends INodeElement {

  String FILE_NAME = "package.json";

  @Override
  PackageJsonSpi spi();

  Path location();

  Path directory();

  Optional<String> main();

  Optional<CharSequence> mainContent();

  Optional<String> propertyAsString(String name);

  Optional<Map<String, Object>> findPropertyAsObject(JsonPointer pointer);

  Optional<String> findPropertyAsString(JsonPointer pointer);

  Optional<Boolean> findPropertyAsBoolean(JsonPointer pointer);

  Optional<BigDecimal> findPropertyAsNumber(JsonPointer pointer);

  Optional<List<Object>> findPropertyAsArray(JsonPointer pointer);

  String version();

  DependencyQuery dependencies();

  Optional<INodeModule> dependency(String name);
}
