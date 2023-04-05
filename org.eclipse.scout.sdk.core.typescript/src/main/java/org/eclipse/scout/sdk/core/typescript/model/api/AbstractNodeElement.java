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

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.IWebConstants;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.Strings;

public abstract class AbstractNodeElement<SPI extends NodeElementSpi> implements INodeElement {
  private final SPI m_spi;
  private final FinalValue<Optional<SourceRange>> m_source;

  protected AbstractNodeElement(SPI spi) {
    m_spi = Ensure.notNull(spi);
    m_source = new FinalValue<>();
  }

  @Override
  public SPI spi() {
    return m_spi;
  }

  @Override
  public INodeModule containingModule() {
    return spi().containingModule().api();
  }

  @Override
  public Optional<Path> containingFile() {
    return spi().containingFile();
  }

  @Override
  public ExportType exportType() {
    return spi().exportType();
  }

  @Override
  public Optional<String> computeImportPathFrom(INodeElement fromElement) {
    return Optional.ofNullable(fromElement)
        .flatMap(f -> computeImportPathFrom(f.containingModule(), f.containingFile().orElse(null)));
  }

  @Override
  public Optional<String> computeImportPathFrom(INodeModule fromModule, Path fromFile) {
    if (containingModule() != fromModule) {
      // this element is in another module -> path is the module name. 
      // ignore here the case that this element is not exported from the module. This import will then just be invalid.
      return Optional.ofNullable(containingModule().name());
    }
    if (fromFile == null) {
      return Optional.empty();
    }

    Path importTarget;
    if (this.isExportedFromModule()) {
      // create import to the module main file
      var packageJson = containingModule().packageJson();
      importTarget = packageJson.directory().resolve(packageJson.main().orElse(""));
    }
    else {
      // create relative path import to this file
      importTarget = containingFile().orElse(null);
    }
    if (importTarget == null) {
      return Optional.empty();
    }

    var from = fromFile.getParent().relativize(importTarget).toString().replace('\\', '/');
    if (!Strings.startsWith(from, '.')) {
      from = "./" + from;
    }
    return Optional.of(Strings.removeSuffix(Strings.removeSuffix(from, IWebConstants.TS_FILE_SUFFIX), IWebConstants.JS_FILE_SUFFIX));
  }

  @Override
  public List<String> moduleExportNames() {
    var exportNames = containingModule().spi().elements().get(spi());
    if (exportNames == null) {
      return emptyList();
    }
    return unmodifiableList(exportNames);
  }

  @Override
  public boolean isExportedFromModule() {
    return !moduleExportNames().isEmpty();
  }

  @Override
  public Optional<SourceRange> source() {
    return m_source.computeIfAbsentAndGet(() -> spi().source());
  }
}
