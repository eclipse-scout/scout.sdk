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
import java.util.Set;

import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi;
import org.eclipse.scout.sdk.core.util.SourceRange;

/**
 * Represents a Node element (like variable, class, function)
 */
public interface INodeElement {
  /**
   * @return The {@link INodeModule} this element belongs to.
   */
  @SuppressWarnings("ClassReferencesSubclass")
  INodeModule containingModule();

  /**
   * @return A {@link Path} to the file containing this element. The resulting {@link Optional} is empty if the element
   *         does not exist in a file (e.g. synthetic types). The containing file for {@link INodeModule} is the
   *         package.json file.
   */
  Optional<Path> containingFile();

  /**
   * @return The name of the file (including extensions) containing this element (last segment in the
   *         {@link #containingFile()}).
   * @see #containingFile()
   */
  Optional<String> containingFileName();

  /**
   * @return {@code true} if this element is contained in a TypeScript file (see {@link #containingFile()}),
   *         {@code false} otherwise (e.g. if it is a JavaScript file or there is no containing file at all).
   */
  boolean isTypeScript();

  /**
   * @return The SPI (service provider interface) for this element.
   */
  NodeElementSpi spi();

  /**
   * @return The name of this element
   */
  String name();

  /**
   * Creates an import path for this element that can be used in the given file within the given module. <br>
   * The created path may be relative ({@code import xy from './rel/path/ThisElement'}) or absolute ({@code import {ab}
   * from '@my-module/name'}) depending on the fromModule. <br>
   * The created path therefore represents the path from the given file to this element.
   * 
   * @param fromModule
   *          The module from where the import should point to this file. This module may then use the resulting path.
   * @param fromFile
   *          The file (within the given module) from where the import should be computed. This file may then use the
   *          resulting path.
   * @return The import path or an empty optional if the fromFile is null or this element is not contained in a file
   *         ({@link #containingFile()}).
   */
  @SuppressWarnings("ClassReferencesSubclass")
  Optional<String> computeImportPathFrom(INodeModule fromModule, Path fromFile);

  /**
   * Creates an import path for this element that can be used in the given element. <br>
   * The created path may be relative ({@code import xy from './rel/path/ThisElement'}) or absolute ({@code import {ab}
   * from '@my-module/name'}) depending on the containing module. <br>
   * The created path therefore represents the path from the given element to this element.
   * 
   * @param queryLocation
   *          The source location from where the path to this element should be computed.
   * @return The import path or an empty optional if the given queryLocation or this element is not contained in a file
   *         ({@link #containingFile()}).
   */
  Optional<String> computeImportPathFrom(INodeElement queryLocation);

  /**
   * @return An unmodifiable {@link Set} holding all names under which this element is exported from the
   *         {@link #containingModule()}.
   */
  Set<String> moduleExportNames();

  /**
   * @return {@code true} if this element is exported from the {@link #containingModule()}. {@code false} otherwise.
   */
  boolean isExportedFromModule();

  /**
   * @return The {@link ExportType} of this element.
   */
  ExportType exportType();

  /**
   * Describes the types of exports
   */
  enum ExportType {
    /**
     * The element is not exported at all.
     */
    NONE,
    /**
     * Describes a named export like {@code export class MyClass}
     */
    NAMED,
    /**
     * Describes a default export like {@code export default class MyClass}.
     */
    DEFAULT
  }

  /**
   * @return The {@link SourceRange} of this element if it is part of a file or an empty {@link Optional} if it has no
   *         underlying source.
   */
  Optional<SourceRange> source();
}
