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

import java.io.BufferedInputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson;
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class PackageJsonImplementor extends AbstractNodeElement<PackageJsonSpi> implements IPackageJson {

  private final String m_name;
  private final String m_version;
  private final JsonObject m_jsonRoot;
  private final FinalValue<Optional<String>> m_main;

  public PackageJsonImplementor(PackageJsonSpi spi) {
    super(spi);
    m_jsonRoot = loadContent(spi);
    m_name = Ensure.notBlank(m_jsonRoot.getString("name", null), "Name missing in '{}'.", spi.containingDir());
    m_version = Ensure.notBlank(m_jsonRoot.getString("version", null), "Version missing in '{}'.", spi.containingDir());
    m_main = new FinalValue<>();
  }

  protected static JsonObject loadContent(PackageJsonSpi spi) {
    try (var parser = Json.createReader(new BufferedInputStream(spi.content()))) {
      return parser.readObject();
    }
  }

  @Override
  public Path location() {
    return directory().resolve(FILE_NAME);
  }

  @Override
  public Path directory() {
    return spi().containingDir();
  }

  @Override
  public Optional<String> main() {
    return m_main.computeIfAbsentAndGet(this::computeMain);
  }

  protected Optional<String> computeMain() {
    // use source index as main if available next to package.json (naming convention)
    var mainOverride = new String[]{"src/index.ts", "src/index.js", "src/main/js/index.ts", "src/main/js/index.js"};
    for (var override : mainOverride) {
      if (spi().existsFile(override)) {
        return Optional.of(override);
      }
    }

    return jsonString("exports", ".", "import")
        .or(() -> jsonString("exports", "."))
        .or(() -> jsonString("module"))
        .or(() -> jsonString("main"));
  }

  @Override
  public Optional<? extends JsonObject> jsonObject(String... pathSegments) {
    return jsonValue(JsonObject.class, pathSegments);
  }

  @Override
  public Optional<String> jsonString(String... pathSegments) {
    return this.jsonValue(JsonString.class, pathSegments)
        .map(JsonString::getString);
  }

  @Override
  public <T extends JsonValue> Optional<T> jsonValue(Class<T> type, String... pathSegments) {
    JsonValue result;
    if (pathSegments == null || pathSegments.length < 1 || Strings.isEmpty(pathSegments[0])) {
      result = m_jsonRoot;
    }
    else {
      result = m_jsonRoot.get(pathSegments[0]);
      for (var i = 1; i < pathSegments.length; i++) {
        if (result instanceof JsonObject o) {
          result = o.get(pathSegments[i]);
        }
        else {
          result = null;
          break;
        }
      }
    }

    return Optional.ofNullable(result)
        .filter(v -> type.isAssignableFrom(v.getClass()))
        .map(type::cast);
  }

  @Override
  public String version() {
    return m_version;
  }

  @Override
  public String name() {
    return m_name;
  }

  @Override
  public String toString() {
    return name() + "@" + version();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var that = (PackageJsonImplementor) o;
    return m_name.equals(that.m_name) && m_version.equals(that.m_version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_name, m_version);
  }
}
