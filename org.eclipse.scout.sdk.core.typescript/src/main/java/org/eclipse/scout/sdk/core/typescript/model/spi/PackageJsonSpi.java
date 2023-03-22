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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson;
import org.eclipse.scout.sdk.core.typescript.model.api.JsonPointer;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.Strings;

public interface PackageJsonSpi extends NodeElementSpi {
  @Override
  IPackageJson api();

  InputStream content();

  Path containingDir();

  boolean existsFile(String relPath);

  Collection<NodeModuleSpi> dependencies();

  Object find(JsonPointer pointer);

  String getString(String name);

  @Override
  default Optional<SourceRange> source() {
    try (var in = content()) {
      var content = Strings.fromInputStream(in, StandardCharsets.UTF_8);
      return Optional.of(new SourceRange(content, 0));
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }
}
