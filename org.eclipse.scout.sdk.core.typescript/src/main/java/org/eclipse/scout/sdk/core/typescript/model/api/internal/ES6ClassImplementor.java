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

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;
import org.eclipse.scout.sdk.core.typescript.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.typescript.model.api.query.FieldQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.query.FunctionQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.query.SupersQuery;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.TypeParameterSpi;

public class ES6ClassImplementor extends DataTypeImplementor<ES6ClassSpi> implements IES6Class {
  public ES6ClassImplementor(ES6ClassSpi spi) {
    super(spi);
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
  public boolean isTypeAlias() {
    return spi().isTypeAlias();
  }

  @Override
  public Optional<IDataType> aliasedDataType() {
    return spi().aliasedDataType().map(DataTypeSpi::api);
  }

  @Override
  public boolean isInterface() {
    return spi().isInterface();
  }

  @Override
  public boolean hasModifier(Modifier modifier) {
    return spi().hasModifier(modifier);
  }

  @Override
  public IES6Class withoutTypeArguments() {
    return spi().withoutTypeArguments().api();
  }

  @Override
  public boolean isInstanceOf(IES6Class es6Class) {
    if (es6Class == null) {
      return false;
    }
    var me = withoutTypeArguments();
    var raw = es6Class.withoutTypeArguments();
    if (raw == me) {
      return true;
    }
    return supers().stream().anyMatch(s -> s == raw);
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
  public SupersQuery supers() {
    return new SupersQuery(spi());
  }

  @Override
  public Stream<IES6Class> superInterfaces() {
    return spi().superInterfaces()
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

  @Override
  public Stream<IDataType> typeArguments() {
    return spi().typeArguments().stream()
        .map(DataTypeSpi::api);
  }

  @Override
  public Stream<ITypeParameter> typeParameters() {
    return spi().typeParameters().stream()
        .map(TypeParameterSpi::api);
  }
}
