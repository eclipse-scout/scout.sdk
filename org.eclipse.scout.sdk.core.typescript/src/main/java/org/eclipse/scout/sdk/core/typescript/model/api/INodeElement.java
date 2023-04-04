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
import java.util.List;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi;
import org.eclipse.scout.sdk.core.util.SourceRange;

public interface INodeElement {
  @SuppressWarnings("ClassReferencesSubclass")
  INodeModule containingModule();

  Optional<Path> containingFile();

  NodeElementSpi spi();

  String name();

  @SuppressWarnings("ClassReferencesSubclass")
  Optional<String> computeImportPathFrom(INodeModule fromModule, Path fromFile);

  Optional<String> computeImportPathFrom(INodeElement queryLocation);

  List<String> moduleExportNames();

  boolean isExportedFromModule();

  ExportType exportType();

  enum ExportType {
    NONE,
    NAMED,
    DEFAULT
  }

  Optional<SourceRange> source();
}
