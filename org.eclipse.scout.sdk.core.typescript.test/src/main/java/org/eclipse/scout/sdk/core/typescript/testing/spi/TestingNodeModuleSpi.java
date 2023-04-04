/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.testing.spi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement.ExportType;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.NodeModuleImplementor;
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementFactorySpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class TestingNodeModuleSpi extends AbstractNodeElementSpi<INodeModule> implements NodeModuleSpi {
  private final FinalValue<PackageJsonSpi> m_packageJson;
  private final Path m_directory;

  public TestingNodeModuleSpi(Path packageJsonDirectory) {
    super(null);
    m_directory = packageJsonDirectory;
    m_packageJson = new FinalValue<>();
  }

  @Override
  protected Path resolveContainingFile() {
    return packageJson().containingFile().orElse(null);
  }

  @Override
  public PackageJsonSpi packageJson() {
    return m_packageJson.computeIfAbsentAndGet(() -> new TestingPackageJsonSpi(this, m_directory));
  }

  @Override
  public Map<NodeElementSpi, List<String>> elements() {
    // simple local implementation cannot parse elements
    return Collections.emptyMap();
  }

  @Override
  public List<ES6ClassSpi> classes() {
    return Collections.emptyList();
  }

  @Override
  public Map<String, NodeElementSpi> exports() {
    // simple local implementation cannot parse exports
    return Collections.emptyMap();
  }

  @Override
  public Optional<SourceRange> source() {
    return packageJson().api().main()
        .map(main -> packageJson().containingDir().resolve(main))
        .map(TestingNodeModuleSpi::readContent)
        .map(content -> new SourceRange(content, 0));
  }

  private static String readContent(Path file) {
    try {
      return Files.readString(file, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new SdkException("Unable to read '{}'.", file, e);
    }
  }

  @Override
  public NodeModuleSpi containingModule() {
    return this;
  }

  @Override
  public ExportType exportType() {
    return INodeElement.ExportType.NAMED;
  }

  @Override
  public NodeElementFactorySpi nodeElementFactory() {
    // simple local implementation cannot create node elements
    return null;
  }

  @Override
  protected INodeModule createApi() {
    return new NodeModuleImplementor(this, packageJson());
  }
}
