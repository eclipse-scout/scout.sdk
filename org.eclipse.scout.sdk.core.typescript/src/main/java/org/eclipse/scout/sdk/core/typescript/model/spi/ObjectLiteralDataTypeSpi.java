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

import static java.util.Collections.emptyList;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement.ExportType;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.DataTypeImplementor;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class ObjectLiteralDataTypeSpi extends AbstractNodeElementSpi<IDataType> implements DataTypeSpi {

  private final String m_name;
  private final ObjectLiteralSpi m_objectLiteral;

  protected ObjectLiteralDataTypeSpi(NodeModuleSpi module, String name, ObjectLiteralSpi objectLiteral) {
    super(module);
    m_name = Ensure.notNull(name);
    m_objectLiteral = Ensure.notNull(objectLiteral);
  }

  @Override
  public ExportType exportType() {
    return ExportType.NONE;
  }

  @Override
  protected Path resolveContainingFile() {
    return m_objectLiteral.containingFile().orElse(null);
  }

  @Override
  protected IDataType createApi() {
    return new DataTypeImplementor<>(this);
  }

  @Override
  public String name() {
    return m_name;
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public Collection<DataTypeSpi> childTypes() {
    return emptyList();
  }

  @Override
  public Optional<ObjectLiteralSpi> objectLiteral() {
    return Optional.of(m_objectLiteral);
  }

  @Override
  public Optional<SourceRange> source() {
    return m_objectLiteral.source();
  }
}
