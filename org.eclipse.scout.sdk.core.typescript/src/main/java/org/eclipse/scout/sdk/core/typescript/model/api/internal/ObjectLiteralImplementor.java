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

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue.ConstantValueType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.typescript.model.api.JsonPointer;
import org.eclipse.scout.sdk.core.typescript.model.spi.ObjectLiteralSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class ObjectLiteralImplementor extends AbstractNodeElement<ObjectLiteralSpi> implements IObjectLiteral {
  public ObjectLiteralImplementor(ObjectLiteralSpi spi) {
    super(spi);
  }

  @Override
  public String name() {
    return spi().name();
  }

  @Override
  public Optional<IConstantValue> find(CharSequence jsonPointer) {
    return find(JsonPointer.compile(jsonPointer));
  }

  @Override
  public Optional<IConstantValue> find(JsonPointer pointer) {
    var adapter = new ConstantValuePointerElement(new P_ConstantValueAdapter(this));
    return Optional.ofNullable(pointer.find(adapter))
        .map(r -> (ConstantValuePointerElement) r)
        .map(ConstantValuePointerElement::getValue);
  }

  @Override
  public Map<String, IConstantValue> properties() {
    return spi().properties();
  }

  @Override
  public IDataType createDataType(String name) {
    return spi().createDataType(name).api();
  }

  @Override
  public Optional<IConstantValue> property(String name) {
    return Optional.ofNullable(properties().get(name));
  }

  @Override
  public Optional<IObjectLiteral> propertyAsObjectLiteral(String name) {
    return propertyAs(name, IObjectLiteral.class);
  }

  @Override
  public Optional<IES6Class> propertyAsES6Class(String name) {
    return propertyAs(name, IES6Class.class);
  }

  @Override
  public Optional<String> propertyAsString(String name) {
    return propertyAs(name, String.class);
  }

  @Override
  public <T> Optional<T> propertyAs(String name, Class<T> type) {
    return property(name)
        .flatMap(v -> v.convertTo(type));
  }

  @Override
  public Stream<IObjectLiteral> childObjectLiterals() {
    return properties().values().stream()
        .flatMap(constantValue -> {
          if (constantValue.type() == ConstantValueType.ObjectLiteral) {
            return constantValue.asObjectLiteral().stream();
          }
          if (constantValue.type() == ConstantValueType.Array) {
            return constantValue.asArray()
                .stream()
                .flatMap(Stream::of)
                .filter(cv -> cv.type() == ConstantValueType.ObjectLiteral)
                .flatMap(cv -> cv.asObjectLiteral().stream());
          }
          return Stream.empty();
        });
  }

  private static final class P_ConstantValueAdapter implements IConstantValue {

    private final IObjectLiteral m_literal;

    public P_ConstantValueAdapter(IObjectLiteral literal) {
      m_literal = Ensure.notNull(literal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> convertTo(Class<T> expectedType) {
      if (expectedType == IObjectLiteral.class) {
        return (Optional<T>) asObjectLiteral();
      }
      return Optional.empty();
    }

    @Override
    public Optional<Path> containingFile() {
      return m_literal.containingFile();
    }

    @Override
    public Optional<IObjectLiteral> asObjectLiteral() {
      return Optional.of(m_literal);
    }

    @Override
    public ConstantValueType type() {
      return ConstantValueType.ObjectLiteral;
    }

    @Override
    public Optional<IDataType> dataType() {
      return Optional.empty();
    }
  }
}
