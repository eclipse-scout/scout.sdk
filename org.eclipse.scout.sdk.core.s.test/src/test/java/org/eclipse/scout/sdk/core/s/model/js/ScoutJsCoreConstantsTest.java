/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.eclipse.scout.sdk.core.java.JavaUtils;
import org.eclipse.scout.sdk.core.java.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.typescript.IWebConstants;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Resources;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jakarta.json.Json;
import jakarta.json.JsonObject;

@Tag("IntegrationTest")
public class ScoutJsCoreConstantsTest {

  @TempDir
  private Path m_tempDir;

  @Test
  public void testScoutJsCoreApi() throws IOException {
    var classMap = buildClassMap();
    var location = downloadAndExtractModule(ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME);
    if (location == null) {
      return; // test not supported (Scout RT too old or not yet available)
    }
    assertApiExists(classMap, location);
  }

  private static void assertApiExists(Map<String, MemberNames> classMap, Path moduleDir) throws IOException {
    for (var entry : classMap.entrySet()) {
      var className = entry.getKey();
      var members = entry.getValue();
      var classFileContent = findClassDeclaration(className, moduleDir.resolve("src"));
      members.properties.forEach(prop -> assertPropertyInSource(classFileContent, className, prop));
      members.functions.forEach(prop -> assertFunctionInSource(classFileContent, className, prop));
    }
  }

  private static void assertPropertyInSource(String src, String className, String propertyName) {
    assertTrue(src.contains(" " + propertyName + ":"), "Property '" + propertyName + "' not found in '" + className + "'.");
  }

  private static void assertFunctionInSource(String src, String className, String functionName) {
    assertTrue(src.contains(" " + functionName + "("), "Function '" + functionName + "' not found in '" + className + "'.");
  }

  private static String findClassDeclaration(String classToFind, Path rootDir) throws IOException {
    var result = new ArrayList<String>();
    Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (!file.getFileName().toString().endsWith(IWebConstants.TS_FILE_SUFFIX)) {
          return FileVisitResult.CONTINUE;
        }
        var fileContent = JavaUtils.removeComments(Files.readString(file));
        if (fileContent.contains("export class " + classToFind + " ")
            || fileContent.contains("export class " + classToFind + "<")
            || fileContent.contains("export type " + classToFind + " = ")
            || fileContent.contains("export type " + classToFind + "<")
            || fileContent.contains("export const " + classToFind + " = {")) {
          result.add(fileContent);
        }
        return FileVisitResult.CONTINUE;
      }
    });
    assertFalse(result.isEmpty(), "Could not find class '" + classToFind + "'.");
    assertEquals(1, result.size(), "Multiple results found for class '" + classToFind + "': " + result);
    return result.get(0);
  }

  private static Map<String, MemberNames> buildClassMap() {
    var fields = new ArrayList<>(Arrays.asList(ScoutJsCoreConstants.class.getFields()));
    skipField("MODEL_SUFFIX", fields); // should never change
    skipField("JQUERY", fields); // should never change
    skipField("NAMESPACE", fields); // should never change
    skipField("SCOUT_JS_CHART_MODULE_NAME", fields); // should never change
    skipField("SCOUT_JS_CORE_MODULE_NAME", fields); // should never change

    var classMap = new HashMap<String, MemberNames>();
    processClassNamesModelTypes(fields, classMap);
    processClasses(fields, classMap);
    processFunctions(fields, classMap);
    processProperties(fields, classMap);

    assertTrue(fields.isEmpty(), "Some constants in '" + ScoutJsCoreConstants.class.getName() + "' could not be mapped: " + fields);
    return classMap;
  }

  private static void processClassNamesModelTypes(List<Field> fields, Map<String, MemberNames> classMap) {
    skipField("CLASS_NAMES_MODEL_TYPES", fields);
    ScoutJsCoreConstants.CLASS_NAMES_MODEL_TYPES.forEach(modelType -> classMap.put(modelType, new MemberNames()));
  }

  private static void processProperties(List<Field> fields, Map<String, MemberNames> classMap) {
    var iterator = fields.iterator();
    while (iterator.hasNext()) {
      var field = iterator.next();
      if (field.getName().startsWith("PROPERTY_NAME_")) {
        iterator.remove();
        try {
          var fieldValue = (String) field.get(null);
          String declaringClass;
          if (ScoutJsCoreConstants.PROPERTY_NAME_EVENTS.equals(fieldValue)) {
            declaringClass = "EventEmitter";
            classMap.put(declaringClass, new MemberNames());
          }
          else if (ScoutJsCoreConstants.PROPERTY_NAME_DETAIL_FORM.equals(fieldValue) || ScoutJsCoreConstants.PROPERTY_NAME_DETAIL_TABLE.equals(fieldValue)) {
            declaringClass = ScoutJsCoreConstants.CLASS_NAME_PAGE;
          }
          else if (ScoutJsCoreConstants.PROPERTY_NAME_COLUMNS.equals(fieldValue) || ScoutJsCoreConstants.PROPERTY_NAME_COLUMN_MAP.equals(fieldValue)) {
            declaringClass = ScoutJsCoreConstants.CLASS_NAME_TABLE;
          }
          else {
            declaringClass = ScoutJsCoreConstants.CLASS_NAME_WIDGET;
          }
          classMap.get(declaringClass).properties.add(fieldValue);
        }
        catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private static void processFunctions(List<Field> fields, Map<String, MemberNames> classMap) {
    var iterator = fields.iterator();
    while (iterator.hasNext()) {
      var field = iterator.next();
      if (field.getName().startsWith("FUNCTION_NAME_")) {
        iterator.remove();
        try {
          var fieldValue = (String) field.get(null);
          if (ScoutJsCoreConstants.FUNCTION_NAME_RESOLVE_TEXT_PROPERTY.equals(fieldValue)) {
            var value = new HashSet<String>();
            value.add(fieldValue);
            assertNull(classMap.put("texts", new MemberNames(value, new HashSet<>())));
          }
          else {
            classMap.get(ScoutJsCoreConstants.CLASS_NAME_WIDGET).functions.add(fieldValue);
          }
        }
        catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private static void skipField(String name, List<Field> fields) {
    var found = new AtomicBoolean();
    fields.removeIf(f -> {
      var match = name.equals(f.getName());
      if (match) {
        assertTrue(found.compareAndSet(false, true), "Field '" + name + "' found more than once.");
      }
      return match;
    });
    assertTrue(found.get(), "Field '" + name + "' could not longer be found.");
  }

  private static void processClasses(List<Field> fields, Map<String, MemberNames> classMap) {
    var iterator = fields.iterator();
    while (iterator.hasNext()) {
      var field = iterator.next();
      if (!field.getName().startsWith("CLASS_NAME_")) {
        continue;
      }

      iterator.remove();
      if (field.getName().startsWith("CLASS_NAME_SUFFIX_")) {
        continue;
      }

      try {
        assertNull(classMap.put((String) field.get(null), new MemberNames()));
      }
      catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Path downloadAndExtractModule(String name) throws IOException {
    var url = getNewestModuleUrl(name);
    if (url == null) {
      return null; // module could not be found
    }
    var targetDir = m_tempDir.resolve(name).normalize();
    try (var remote = Resources.httpGet(url); var gzip = new GZIPInputStream(remote); var tar = new TarArchiveInputStream(gzip)) {
      TarArchiveEntry entry;
      while ((entry = tar.getNextEntry()) != null) {
        var extractTo = targetDir.resolve(entry.getName()).normalize();
        if (!extractTo.startsWith(targetDir)) {
          throw new IllegalArgumentException("Bad tar archive entry: " + entry.getName());
        }
        if (entry.isDirectory()) {
          Files.createDirectories(extractTo);
        }
        else {
          Files.createDirectories(extractTo.getParent());
          Files.copy(tar, extractTo);
        }
      }
    }
    return targetDir.resolve("package");
  }

  private static String getNewestModuleUrl(String name) throws IOException {
    var versionSuffix = getCurrentVersionSuffix();
    if (versionSuffix == null) {
      SdkLog.warning("{} skipped for current Scout version '{}'.", ScoutJsCoreConstantsTest.class.getSimpleName(), CoreScoutTestingUtils.currentScoutVersion());
      return null;
    }
    var npmJsUrl = "https://registry.npmjs.com/";
    try (var parser = Json.createReader(Resources.httpGet(npmJsUrl + name))) {
      var obj = parser.readObject();
      var newest = obj.getJsonObject("versions")
          .entrySet().stream()
          .filter(entry -> entry.getKey().startsWith(versionSuffix))
          .collect(Collectors.toMap(entry -> ApiVersion.parse(entry.getKey()).orElseThrow(), entry -> (JsonObject) entry.getValue(), Ensure::failOnDuplicates, TreeMap::new))
          .lastEntry();
      if (newest == null) {
        SdkLog.warning("Could not find any version for Node package '{}' starting with '{}' on '{}'.", name, versionSuffix, npmJsUrl);
        return null; // no version found
      }
      return newest.getValue().getJsonObject("dist").getString("tarball");
    }
  }

  private static String getCurrentVersionSuffix() {
    var version = CoreScoutTestingUtils.currentScoutVersion(); // e.g. 24.1-SNAPSHOT
    var segments = ApiVersion.parse(version).orElseThrow().segments();
    if (segments[0] <= 22) {
      return null; // this test only supports versions >= 23.1 (TypeScript)
    }
    return segments[0] + "." + segments[1] + ".";
  }

  private record MemberNames(Set<String> functions, Set<String> properties) {
    private MemberNames() {
      this(new HashSet<>(), new HashSet<>());
    }
  }
}
