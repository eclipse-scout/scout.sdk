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

/**
 * Represents a package.json file defining an {@link INodeModule}.
 */
public interface IPackageJson extends INodeElement {

  String FILE_NAME = "package.json";

  @Override
  PackageJsonSpi spi();

  /**
   * @return The absolute {@link Path} to the package.json file.
   */
  Path location();

  /**
   * @return The absolute {@link Path directory} in which the package.json file is stored.
   */
  Path directory();

  /**
   * Gets the "main" entry of the package.json file.<br>
   * <br>
   * It is computed in the following order:
   * <ol>
   * <li>An index file (index.js, index.ts) in src/ or src/main/js</li>
   * <li>The file that the package.json attribute {@code /exports/./import} points to.</li>
   * <li>The file that the package.json attribute {@code /exports/.} points to.</li>
   * <li>The file that the package.json attribute {@code module} points to.</li>
   * <li>The file that the package.json attribute {@code main} points to.</li>
   * </ol>
   * 
   * @return The "main" entry of the package.json pointing to the main file of the {@link INodeModule} as module
   *         relative path.
   */
  Optional<String> main();

  /**
   * @return The absolute path on the filesystem to the main entry of the package.json (see {@link #main()}).
   */
  Optional<Path> mainLocation();

  /**
   * @return The content of the "main" entry of the package.json file. This is the content of a JavaScript or TypeScript
   *         file typically declaring the exports of {@link INodeModule}.
   */
  Optional<CharSequence> mainContent();

  /**
   * Gets the top-level property from the package.json having given name as {@link String}.
   * 
   * @param name
   *          The name of the top-level property.
   * @return The value of the property as {@link String} or an empty {@link Optional} if the property cannot be found.
   */
  Optional<String> propertyAsString(String name);

  /**
   * Gets the value of the property the given {@link JsonPointer} points to as {@link Map} representing the nested
   * object.
   * 
   * @param pointer
   *          The pointer declaring the object property. Must not be {@code null}.
   * @return The value of the object property or an empty {@link Optional} if the property does not exist or is not an
   *         object.
   */
  Optional<Map<String, Object>> findPropertyAsObject(JsonPointer pointer);

  /**
   * Gets the value of the property the given {@link JsonPointer} points to as {@link String}.
   *
   * @param pointer
   *          The pointer declaring the object property. Must not be {@code null}.
   * @return The value of the {@link String} property or an empty {@link Optional} if the property does not exist or is
   *         not a {@link String}.
   */
  Optional<String> findPropertyAsString(JsonPointer pointer);

  /**
   * Gets the value of the property the given {@link JsonPointer} points to as {@link Boolean}.
   *
   * @param pointer
   *          The pointer declaring the object property. Must not be {@code null}.
   * @return The value of the {@link Boolean} property or an empty {@link Optional} if the property does not exist or is
   *         not a {@link Boolean}.
   */
  Optional<Boolean> findPropertyAsBoolean(JsonPointer pointer);

  /**
   * Gets the value of the property the given {@link JsonPointer} points to as {@link BigDecimal}.
   *
   * @param pointer
   *          The pointer declaring the object property. Must not be {@code null}.
   * @return The value of the {@link BigDecimal} property or an empty {@link Optional} if the property does not exist or
   *         is not a {@link BigDecimal}.
   */
  Optional<BigDecimal> findPropertyAsNumber(JsonPointer pointer);

  /**
   * Gets the value of the property the given {@link JsonPointer} points to as {@link List}.
   *
   * @param pointer
   *          The pointer declaring the object property. Must not be {@code null}.
   * @return The value of the array property or an empty {@link Optional} if the property does not exist or is not an
   *         array.
   */
  Optional<List<Object>> findPropertyAsArray(JsonPointer pointer);

  /**
   * @return The value of the "name" attribute of the package.json. Also represents the name of the {@link INodeModule}.
   */
  @Override
  String name();

  /**
   * @return The value of the "version" attribute of the package.json.
   */
  String version();

  /**
   * Gets a {@link DependencyQuery} to retrieve {@link INodeModule}s this package.json depends on.<br>
   * By default, the query returns all {@link INodeModule modules} which are declared in the "dependencies" attribute of
   * this package.json (direct runtime dependencies).<br>
   * Other dependency types like "devDependencies" or "peerDependencies" are not returned.
   *
   * @return A new {@link DependencyQuery} for {@link INodeModule}s this package.json depends on.
   */
  DependencyQuery dependencies();

  /**
   * Gets the direct dependency having the given name.
   * 
   * @param name
   *          The name of the dependency or {@code null} if the first dependency should be returned.
   * @return The dependency with given name as {@link INodeModule}.
   */
  Optional<INodeModule> dependency(String name);
}
