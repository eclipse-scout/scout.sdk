/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.testing.spi;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement.ExportType;
import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson;
import org.eclipse.scout.sdk.core.typescript.model.api.JsonPointer;
import org.eclipse.scout.sdk.core.typescript.model.api.JsonPointer.IJsonPointerElement;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.PackageJsonImplementor;
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

import jakarta.json.Json;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

public class TestingPackageJsonSpi extends AbstractNodeElementSpi<IPackageJson> implements PackageJsonSpi {

  private final Path m_directory;
  private final FinalValue<JsonObject> m_json;

  public TestingPackageJsonSpi(NodeModuleSpi module, Path directory) {
    super(module);
    m_directory = directory;
    m_json = new FinalValue<>();
  }

  @Override
  protected Path resolveContainingFile() {
    return api().location();
  }

  @Override
  protected IPackageJson createApi() {
    return new PackageJsonImplementor(this);
  }

  @Override
  public InputStream content() {
    try {
      return Files.newInputStream(containingDir().resolve(IPackageJson.FILE_NAME));
    }
    catch (IOException e) {
      throw Ensure.newFail("Unable to parse package.json at location '{}'.", containingDir(), e);
    }
  }

  @Override
  public Path containingDir() {
    return m_directory;
  }

  @Override
  public boolean existsFile(String relPath) {
    return Files.exists(containingDir().resolve(relPath));
  }

  @Override
  public Collection<NodeModuleSpi> dependencies() {
    // simple local implementation cannot parse dependencies
    return emptySet();
  }

  @Override
  public Object find(JsonPointer pointer) {
    return Optional.ofNullable(pointer.find(new P_JakartaJsonPointerElement(getJson())))
        .map(r -> (P_JakartaJsonPointerElement) r)
        .map(r -> extractValue(r.m_value))
        .orElse(null);
  }

  private Object extractValue(JsonValue val) {
    if (val == null || val.getValueType() == ValueType.NULL) {
      return null;
    }
    if (val.getValueType() == ValueType.OBJECT) {
      return val.asJsonObject()
          .entrySet().stream()
          .collect(toMap(Entry::getKey, v -> extractValue(v.getValue()), Ensure::failOnDuplicates));
    }
    if (val.getValueType() == ValueType.ARRAY) {
      return val.asJsonArray().stream()
          .map(this::extractValue)
          .toList();
    }
    if (val.getValueType() == ValueType.STRING) {
      return ((JsonString) val).getString();
    }
    if (val.getValueType() == ValueType.NUMBER) {
      return ((JsonNumber) val).bigDecimalValue();
    }
    return val.getValueType() != ValueType.FALSE;
  }

  @Override
  public String getString(String name) {
    return getJson().getString(name, null);
  }

  protected JsonObject getJson() {
    return m_json.computeIfAbsentAndGet(() -> {
      try (var parser = Json.createReader(content())) {
        return parser.readObject();
      }
    });
  }

  @Override
  public ExportType exportType() {
    return INodeElement.ExportType.NONE;
  }

  private static final class P_JakartaJsonPointerElement implements IJsonPointerElement {

    private final JsonValue m_value;

    public P_JakartaJsonPointerElement(JsonValue value) {
      m_value = value;
    }

    @Override
    public int arrayLength() {
      if (m_value.getValueType() != ValueType.ARRAY) {
        return 0;
      }
      return m_value.asJsonArray().size();
    }

    @Override
    public boolean isObject() {
      return m_value.getValueType() == ValueType.OBJECT;
    }

    @Override
    public IJsonPointerElement element(String name) {
      var value = m_value.asJsonObject().get(name);
      if (value == null) {
        return null;
      }
      return new P_JakartaJsonPointerElement(value);
    }

    @Override
    public IJsonPointerElement element(int index) {
      var arr = m_value.asJsonArray();
      if (index >= arr.size()) {
        return null;
      }
      var value = arr.get(index);
      if (value == null) {
        return null;
      }
      return new P_JakartaJsonPointerElement(value);
    }
  }
}
