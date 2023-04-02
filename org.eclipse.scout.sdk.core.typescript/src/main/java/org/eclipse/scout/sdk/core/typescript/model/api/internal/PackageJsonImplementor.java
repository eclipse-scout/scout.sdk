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

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.IWebConstants;
import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson;
import org.eclipse.scout.sdk.core.typescript.model.api.JsonPointer;
import org.eclipse.scout.sdk.core.typescript.model.api.query.DependencyQuery;
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

public class PackageJsonImplementor extends AbstractNodeElement<PackageJsonSpi> implements IPackageJson {

  private static final JsonPointer EXPORTS_IMPORT = JsonPointer.compile("/exports/./import");
  private static final JsonPointer EXPORTS = JsonPointer.compile("/exports/.");

  private final String m_name;
  private final String m_version;
  private final FinalValue<Optional<String>> m_main;
  private final FinalValue<Optional<CharSequence>> m_mainContent;

  public PackageJsonImplementor(PackageJsonSpi spi) {
    super(spi);
    m_name = Strings.notBlank(spi.getString("name")).orElse("unknown");
    m_version = Strings.notBlank(spi.getString("version")).orElse("0.0.0");
    m_main = new FinalValue<>();
    m_mainContent = new FinalValue<>();
  }

  protected static CharSequence loadContent(Path file) {
    if (!Files.isRegularFile(file)) {
      return null;
    }
    try {
      return Strings.fromFile(file, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new SdkException("Unable to read '{}'.", file, e);
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

  @Override
  public Optional<CharSequence> mainContent() {
    return m_mainContent.computeIfAbsentAndGet(() -> main().map(main -> directory().resolve(main)).map(PackageJsonImplementor::loadContent));
  }

  protected Optional<String> computeMain() {
    // use source index as main if available next to package.json (naming convention)
    var indexFileName = "index";
    var mainOverride = new String[]{
        IWebConstants.JS_SOURCE_FOLDER + "/" + indexFileName + IWebConstants.TS_FILE_SUFFIX,
        IWebConstants.JS_SOURCE_FOLDER + "/" + indexFileName + IWebConstants.JS_FILE_SUFFIX,
        IWebConstants.MAIN_JS_SOURCE_FOLDER + "/" + indexFileName + IWebConstants.TS_FILE_SUFFIX,
        IWebConstants.MAIN_JS_SOURCE_FOLDER + "/" + indexFileName + IWebConstants.JS_FILE_SUFFIX};
    for (var override : mainOverride) {
      if (spi().existsFile(override)) {
        return Optional.of(override);
      }
    }
    return findPropertyAsString(EXPORTS_IMPORT)
        .or(() -> findPropertyAsString(EXPORTS))
        .or(() -> propertyAsString("module"))
        .or(() -> propertyAsString("main"));
  }

  @Override
  public Optional<String> propertyAsString(String name) {
    return Optional.ofNullable(spi().getString(name));
  }

  @Override
  public Optional<String> findPropertyAsString(JsonPointer pointer) {
    return findProperty(pointer, String.class);
  }

  @Override
  public Optional<Boolean> findPropertyAsBoolean(JsonPointer pointer) {
    return findProperty(pointer, Boolean.class);
  }

  @Override
  public Optional<BigDecimal> findPropertyAsNumber(JsonPointer pointer) {
    return findProperty(pointer, BigDecimal.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<List<Object>> findPropertyAsArray(JsonPointer pointer) {
    return findProperty(pointer, List.class)
        .map(m -> (List<Object>) m);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Map<String, Object>> findPropertyAsObject(JsonPointer pointer) {
    return findProperty(pointer, Map.class)
        .map(m -> (Map<String, Object>) m);
  }

  public <T> Optional<T> findProperty(JsonPointer pointer, Class<T> type) {
    return Optional.ofNullable(spi().find(pointer))
        .filter(a -> type.isAssignableFrom(a.getClass()))
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
  public DependencyQuery dependencies() {
    return new DependencyQuery(spi().containingModule());
  }

  @Override
  public Optional<INodeModule> dependency(String name) {
    return dependencies()
        .withName(name)
        .first();
  }

  @Override
  public String toString() {
    return name() + '@' + version();
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
