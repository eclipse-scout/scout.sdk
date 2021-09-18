/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.util.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;

/**
 * Helper class to compute the version of a Maven module within an {@link IJavaEnvironment}.<br>
 * The module can be a binary jar or a source folder within the {@link IJavaEnvironment}.
 */
public final class MavenModuleVersion {

  @SuppressWarnings("StaticCollection")
  private static final Map<Path, Optional<ApiVersion>> VERSION_CACHE = new ConcurrentHashMap<>();

  private MavenModuleVersion() {
  }

  /**
   * Tries to find a module within the given {@link IJavaEnvironment} and computes its Maven version.
   * <p>
   * For binary jars the version is taken from the {@link Name#IMPLEMENTATION_VERSION} manifest header.<br>
   * For source folders it is taken from the parent pom.xml<br>
   * Source or javadoc jar files are not supported.
   * </p>
   * 
   * @param moduleName
   *          The name of the Maven module. This is equal to the artifactId of the module. May be {@code null} but then
   *          the resulting {@link Optional} will always be empty.
   * @param context
   *          The {@link IJavaEnvironment} in which the module should be searched. May be {@code null} but then the
   *          resulting {@link Optional} will always be empty.
   * @return An {@link Optional} holding the Maven version of the given module or an empty {@link Optional} if the
   *         module could not be found, the version cannot be parsed or one of the parameters is {@code null}.
   */
  public static Optional<ApiVersion> get(String moduleName, IJavaEnvironment context) {
    return modulePathIn(moduleName, context).flatMap(MavenModuleVersion::version);
  }

  static Optional<Path> modulePathIn(String moduleName, IJavaEnvironment context) {
    if (context == null || Strings.isBlank(moduleName)) {
      return Optional.empty();
    }
    var jarPath = context.classpath()
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
    // remove src/main/java
    var mainFolder = sourceFolder.getParent();
    if (mainFolder == null) {
      return Optional.empty();
    }
    var srcFolder = mainFolder.getParent();
    if (srcFolder == null) {
      return Optional.empty();
    }
    var modulePath = srcFolder.getParent();
    if (modulePath == null) {
      return Optional.empty();
    }

    try {
      var pomContent = Xml.get(modulePath.resolve(IMavenConstants.POM));
      return Pom.version(pomContent).flatMap(ApiVersion::parse);
    }
    catch (IOException e) {
      throw new SdkException("Error parsing version of source folder '{}'.", sourceFolder, e);
    }
  }

  static Optional<ApiVersion> versionOfJar(Path jar) {
    try (var f = new JarFile(jar.toFile(), false, ZipFile.OPEN_READ)) {
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
    var lastSegment = entry.path().getFileName();
    if (lastSegment == null) {
      return false;
    }
    var fileName = lastSegment.toString();
    // fileName is of form "org.eclipse.scout.rt.platform-10.0.5.jar" or "org.eclipse.scout.rt.platform-10.0.5-sources.jar"
    return Strings.endsWith(fileName, ".jar", false)
        && Strings.startsWith(fileName, moduleName + '-')
        && !Strings.endsWith(fileName, "-sources.jar", false)
        && !Strings.endsWith(fileName, "-javadoc.jar", false);
  }
}
