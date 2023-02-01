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
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.PackageJsonImplementor;
import org.eclipse.scout.sdk.core.util.Ensure;

public class LocalPackageJsonSpi extends AbstractNodeElementSpi<IPackageJson> implements PackageJsonSpi {

  private final Path m_directory;

  public LocalPackageJsonSpi(NodeModuleSpi module, Path directory) {
    super(module);
    m_directory = directory;
  }

  @Override
  protected IPackageJson createApi() {
    return new PackageJsonImplementor(this);
  }

  @Override
  public InputStream content() {
    try {
      return Files.newInputStream(containingDir().resolve(IPackageJson.FILE_NAME));
    }
    catch (IOException e) {
      throw Ensure.newFail("Unable to parse package.json at location '{}'.", containingDir(), e);
    }
  }

  @Override
  public Path containingDir() {
    return m_directory;
  }

  @Override
  public boolean existsFile(String relPath) {
    return Files.exists(containingDir().resolve(relPath));
  }
}