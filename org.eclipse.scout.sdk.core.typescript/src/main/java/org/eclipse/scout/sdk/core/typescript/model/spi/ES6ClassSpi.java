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

import java.util.List;

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;

public interface ES6ClassSpi extends NodeElementSpi {
  @Override
  IES6Class api();

  String name();

  boolean isEnum();

  List<FieldSpi> fields();
}
