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
import org.eclipse.scout.sdk.core.util.FinalValue;

public abstract class AbstractNodeElementSpi<API extends INodeElement> implements NodeElementSpi {
  private final FinalValue<API> m_api;
  private final FinalValue<Optional<Path>> m_containingFile;
  private final NodeModuleSpi m_module;

  protected AbstractNodeElementSpi(NodeModuleSpi module) {
    m_module = module;
    m_api = new FinalValue<>();
    m_containingFile = new FinalValue<>();
  }

  @Override
  public final Optional<Path> containingFile() {
    return m_containingFile.computeIfAbsentAndGet(() -> Optional.ofNullable(resolveContainingFile()));
  }

  protected abstract Path resolveContainingFile();

  protected abstract API createApi();

  @Override
  public final API api() {
    return m_api.computeIfAbsentAndGet(this::createApi);
  }

  @Override
  public NodeModuleSpi containingModule() {
    return m_module;
  }
}
