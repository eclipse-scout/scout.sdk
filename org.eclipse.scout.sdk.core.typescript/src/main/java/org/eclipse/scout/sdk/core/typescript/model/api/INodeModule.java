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
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.query.NodeElementQuery;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.util.SourceRange;

/**
 * Represents a node module (a minimal module is a package.json file).
 */
public interface INodeModule extends INodeElement {
  @Override
  NodeModuleSpi spi();

  /**
   * @return The {@link Path} to the package.json file.
   */
  @Override
  Optional<Path> containingFile();

  /**
   * @return The node module name ("name" attribute in the package.json).
   */
  @Override
  String name();

  /**
   * @return The source of the main file of this module if available (not the package.json).
   */
  @Override
  Optional<SourceRange> source();

  /**
   * Creates a {@link NodeElementQuery} that by default returns all {@link INodeElement}s that exist in this module
   * without the elements of dependencies.
   * 
   * @return A new {@link NodeElementQuery} to access elements in this module (and dependencies if required).
   */
  NodeElementQuery elements();

  /**
   * @return All {@link IES6Class classes} declared in this module.
   */
  Stream<IES6Class> classes();

  /**
   * Gets the {@link INodeElement} that is exported from this {@link INodeModule} with the given name. This must not be
   * the same as {@link INodeElement#name()} if an export alias is used.
   * 
   * @param name
   *          The export name to search. May be {@code null} (then the {@link Optional} will always be empty).
   * @return The {@link INodeElement} that is exported with the name given.
   */
  Optional<INodeElement> export(String name);

  /**
   * @return The {@link IPackageJson} that defines this module.
   */
  IPackageJson packageJson();

  /**
   * @return The {@link INodeElementFactory} for this module.
   */
  INodeElementFactory nodeElementFactory();
}
