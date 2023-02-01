/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.internal;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;
import org.eclipse.scout.sdk.core.typescript.model.api.query.FieldQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.query.FunctionQuery;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;

public class ES6ClassImplementor extends AbstractNodeElement<ES6ClassSpi> implements IES6Class, IDataType {
  public ES6ClassImplementor(ES6ClassSpi spi) {
    super(spi);
  }

  @Override
  public String name() {
    return spi().name();
  }

  @Override
  public FieldQuery fields() {
    return new FieldQuery(spi());
  }

  @Override
  public boolean isEnum() {
    return spi().isEnum();
  }

  @Override
  public FunctionQuery functions() {
    return new FunctionQuery(spi());
  }

  @Override
  public Optional<IFunction> function(String name) {
    return functions()
        .withName(name)
        .first();
  }

  @Override
  public Optional<IES6Class> superClass() {
    return spi().superClass().map(ES6ClassSpi::api);
  }

  @Override
  public Stream<IES6Class> supers() {
    return Stream.concat(spi().superClass().stream(), spi().superInterfaces().stream())
        .map(ES6ClassSpi::api);
  }

  @Override
  public Stream<IES6Class> superInterfaces() {
    return spi().superInterfaces().stream()
        .map(ES6ClassSpi::api);
  }

  @Override
  public Optional<IField> field(String name) {
    return fields()
        .withName(name)
        .first();
  }

  @Override
  public boolean isPrimitive() {
    return spi().isPrimitive();
  }
}
