/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.util.maven;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.scout.sdk.core.java.ISourceFolders;
import org.eclipse.scout.sdk.core.java.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.w3c.dom.Node;

/**
 * Helper class to get version of a Maven artifacts.<br>
 */
public final class MavenArtifactVersions {

  @SuppressWarnings("StaticCollection")
  private static final Map<Path, Optional<ApiVersion>> VERSION_CACHE = new ConcurrentHashMap<>();

  private MavenArtifactVersions() {
  }

  /**
   * Tries to find the module specified in the given {@link IJavaEnvironment} and computes its Maven version.
   * <p>
   * For binary jars the version is taken from the {@link Name#IMPLEMENTATION_VERSION} manifest header.<br>
   * For source folders it is taken from the parent pom.xml<br>
   * Source or javadoc jar files are not supported.
   * </p>
   * 
   * @param artifactId
   *          The name of the Maven module. This is the name of the jar file (without any suffixes) and the name of the
   *          directory that contains the source folders. Typically, this is equal to the artifactId of the module. May
   *          be {@code null} but then the resulting {@link Optional} will always be empty.
   * @param context
   *          The {@link IJavaEnvironment} in which the module should be searched. May be {@code null} but then the
   *          resulting {@link Optional} will always be empty.
   * @return An {@link Optional} holding the Maven version of the given module or an empty {@link Optional} if the
   *         module could not be found, the version cannot be parsed or one of the parameters is {@code null}.
   */
  public static Optional<ApiVersion> usedIn(String artifactId, IJavaEnvironment context) {
    return modulePathIn(artifactId, context).flatMap(MavenArtifactVersions::version);
  }

  /**
   * Gets the newest 200 (limitation by Maven central) versions of given artifact that can be found on Maven-central.
   * 
   * @param groupId
   *          The groupId of the artifact. Must not be {@code null}.
   * @param artifactId
   *          The artifactId of the artifact. Must not be {@code null}.
   * @return A {@link Stream} with all versions found on Maven-central.
   * @throws IOException
   *           if there is an error reading the value from Maven-central.
   */
  public static Stream<String> allOnCentral(String groupId, String artifactId) throws IOException {
    try {
      var g = URLEncoder.encode(groupId, StandardCharsets.UTF_8);
      var a = URLEncoder.encode(artifactId, StandardCharsets.UTF_8);
      var uri = new URI("https://search.maven.org/solrsearch/select?q=g:" + g + "+AND+a:" + a + "&core=gav&rows=100&wt=xml");
      var dom = Xml.get(uri);
      return Xml.evaluateXPath("result/doc/str[@name='v']", dom.getDocumentElement()).stream()
          .map(Node::getTextContent)
          .filter(Strings::hasText);
    }
    catch (URISyntaxException | XPathExpressionException e) {
      throw new IOException(e);
    }
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
    return VERSION_CACHE.computeIfAbsent(pathToModule, MavenArtifactVersions::detectVersion);
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
