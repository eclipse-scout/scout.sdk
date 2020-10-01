/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.util.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.eclipse.scout.sdk.core.util.apidef.ApiVersion;
import org.w3c.dom.Document;

public final class MavenModuleVersion {

  private static final Map<Path, Optional<ApiVersion>> VERSION_CACHE = new ConcurrentHashMap<>();

  private MavenModuleVersion() {
  }

  public static Optional<ApiVersion> get(String moduleName, IJavaEnvironment context) {
    return modulePathIn(moduleName, context).flatMap(MavenModuleVersion::version);
  }

  static Optional<Path> modulePathIn(String moduleName, IJavaEnvironment context) {
    if (context == null || Strings.isBlank(moduleName)) {
      return Optional.empty();
    }
    Optional<Path> jarPath = context.classpath()
        .filter(p -> isModuleJar(moduleName, p))
        .map(IClasspathEntry::path)
        .findAny();
    if (jarPath.isPresent()) {
      return jarPath;
    }
    return context.classpath()
        .filter(p -> isModuleSourceFolder(moduleName, p))
        .map(IClasspathEntry::path)
        .findAny();
  }

  static Optional<ApiVersion> version(Path pathToModule) {
    return VERSION_CACHE.computeIfAbsent(pathToModule, MavenModuleVersion::detectVersion);
  }

  static Optional<ApiVersion> detectVersion(Path pathToModule) {
    if (Files.isRegularFile(pathToModule)) {
      return versionOfJar(pathToModule);
    }
    return versionOfSourceFolder(pathToModule);
  }

  static Optional<ApiVersion> versionOfSourceFolder(Path sourceFolder) {
    Path modulePath = sourceFolder.getParent().getParent().getParent(); // remove src/main/java
    try {
      Document pomContent = Xml.get(modulePath.resolve(IMavenConstants.POM));
      return Pom.version(pomContent).flatMap(ApiVersion::parse);
    }
    catch (IOException e) {
      throw new SdkException("Error parsing version of source folder '{}'.", sourceFolder, e);
    }
  }

  static Optional<ApiVersion> versionOfJar(Path jar) {
    try (JarFile f = new JarFile(jar.toFile(), false, ZipFile.OPEN_READ)) {
      return Optional.ofNullable(f.getManifest())
          .map(mf -> mf.getMainAttributes().getValue(Name.IMPLEMENTATION_VERSION.toString()))
          .flatMap(ApiVersion::parse);
    }
    catch (IOException e) {
      throw new SdkException("Error parsing version of jar '{}'.", jar, e);
    }
  }

  static boolean isModuleSourceFolder(String moduleName, IClasspathEntry entry) {
    if (!entry.isDirectory()) {
      return false;
    }
    return entry.path().endsWith(moduleName + '/' + ISourceFolders.MAIN_JAVA_SOURCE_FOLDER);
  }

  static boolean isModuleJar(String moduleName, IClasspathEntry entry) {
    if (entry.isDirectory()) {
      return false;
    }
    String fileName = entry.path().getFileName().toString().toLowerCase(Locale.ENGLISH);
    // fileName is of form "org.eclipse.scout.rt.platform-10.0.5.jar" or "org.eclipse.scout.rt.platform-10.0.5-sources.jar"
    return fileName.endsWith(".jar")
        && fileName.startsWith(moduleName + '-')
        && !fileName.endsWith("-sources.jar")
        && !fileName.endsWith("-javadoc.jar");
  }
}
