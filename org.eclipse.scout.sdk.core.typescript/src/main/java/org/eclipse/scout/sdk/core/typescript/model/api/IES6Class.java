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

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.query.FieldQuery;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;

public interface IES6Class extends INodeElement {
  @Override
  ES6ClassSpi spi();

  FieldQuery fields();

  boolean isEnum();

  Optional<IES6Class> superClass();

  Stream<IES6Class> supers();

  Stream<IES6Class> superInterfaces();

  Optional<IField> field(String name);
}
