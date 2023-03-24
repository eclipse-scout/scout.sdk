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

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ES6ClassImplementor;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class ES6ClassWithTypeArgumentsSpi extends AbstractNodeElementSpi<IES6Class> implements ES6ClassSpi {

  private final ES6ClassSpi m_class;
  private final List<DataTypeSpi> m_arguments;

  protected ES6ClassWithTypeArgumentsSpi(NodeModuleSpi module, ES6ClassSpi classSpi, List<DataTypeSpi> typeArguments) {
    super(module);
    m_class = classSpi;
    m_arguments = unmodifiableList(new ArrayList<>(typeArguments));
  }

  @Override
  public ES6ClassSpi withoutTypeArguments() {
    return m_class;
  }

  @Override
  public boolean hasModifier(Modifier modifier) {
    return m_class.hasModifier(modifier);
  }

  @Override
  public String name() {
    return m_class.name();
  }

  @Override
  protected IES6Class createApi() {
    return new ES6ClassImplementor(this);
  }

  @Override
  public Optional<SourceRange> source() {
    return m_class.source();
  }

  @Override
  public boolean isEnum() {
    return m_class.isEnum();
  }

  @Override
  public boolean isTypeAlias() {
    return false;
  }

  @Override
  public Optional<DataTypeSpi> aliasedDataType() {
    return m_class.aliasedDataType();
  }

  @Override
  public List<FieldSpi> fields() {
    return m_class.fields();
  }

  @Override
  public List<FunctionSpi> functions() {
    return m_class.functions();
  }

  @Override
  public List<DataTypeSpi> typeArguments() {
    return m_arguments;
  }

  @Override
  public DataTypeSpi createDataType(String name) {
    return m_class.createDataType(name);
  }

  @Override
  public boolean isInterface() {
    return m_class.isInterface();
  }

  @Override
  public Optional<ES6ClassSpi> superClass() {
    return m_class.superClass();
  }

  @Override
  public Stream<ES6ClassSpi> superInterfaces() {
    return m_class.superInterfaces();
  }
}
