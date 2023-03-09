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

import java.util.Map;

import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;

public interface NodeModuleSpi extends NodeElementSpi {
  @Override
  INodeModule api();

  PackageJsonSpi packageJson();

  Map<String, ExportFromSpi> exports();

  NodeElementFactorySpi nodeElementFactory();
}
