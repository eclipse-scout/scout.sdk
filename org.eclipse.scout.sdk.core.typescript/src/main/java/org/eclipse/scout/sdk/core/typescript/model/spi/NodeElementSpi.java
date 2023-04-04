/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.spi;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement.ExportType;
import org.eclipse.scout.sdk.core.util.SourceRange;

public interface NodeElementSpi {
  INodeElement api();

  @SuppressWarnings("ClassReferencesSubclass")
  NodeModuleSpi containingModule();

  Optional<Path> containingFile();

  ExportType exportType();

  Optional<SourceRange> source();
}
