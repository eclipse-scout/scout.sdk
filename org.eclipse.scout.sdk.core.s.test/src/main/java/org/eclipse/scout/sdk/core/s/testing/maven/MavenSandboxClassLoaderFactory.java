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
package org.eclipse.scout.sdk.core.s.testing.maven;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.ZipFile;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link MavenSandboxClassLoaderFactory}</h3>
 *
 * @since 5.2.0
 */
public final class MavenSandboxClassLoaderFactory {

  private MavenSandboxClassLoaderFactory() {
  }

  /**
   * @return a new {@link URLClassLoader} capable to run the {@link MavenCliRunner}.
   */
  public static URLClassLoader build() {
    return URLClassLoader.newInstance(getMavenJarsUrls(), ClassLoader.getPlatformClassLoader());
  }

  static URL[] getMavenJarsUrls() {
    // contains a sample class of all jars required by the maven runtime.
    // the code source of these classes will be the source of the classpath of the sandbox classloader.
    Collection<String> baseClasses = Arrays.asList(
        MavenCliRunner.class.getName(),
        "javax.annotation.PostConstruct", // javax.annotation-api
        "org.apache.commons.lang3.StringUtils", // apache-commons-lang3 for guice
        "org.apache.commons.io.DirectoryWalker", // commons-io
        "org.eclipse.scout.sdk.core.log.SdkLog", // sdk.core for logging
        "org.apache.maven.cli.MavenCli", // maven-embedder
        "org.apache.maven.settings.Settings", // maven-settings
        "org.apache.maven.Maven", // maven-core
        "org.apache.maven.model.Site", // maven-model
        "org.apache.maven.settings.building.DefaultSettingsBuilder", // maven-settings-builder
        "org.apache.maven.artifact.repository.metadata.Metadata", // maven-repository-metadata
        "org.apache.maven.artifact.Artifact", // maven-artifact
        "org.apache.maven.repository.internal.MavenAetherModule", // maven-aether-provider
        "org.eclipse.aether.impl.ArtifactResolver", // aether-impl
        "com.google.inject.Guice", // sisu-guice
        "javax.inject.Inject", // javax.inject
        "org.codehaus.plexus.interpolation.Interpolator", // plexus-interpolation
        "org.apache.maven.plugin.AbstractMojo", // maven-plugin-api
        "org.apache.maven.model.building.ModelBuilder", // maven-model-builder
        "org.apache.maven.building.ProblemCollector", // maven-builder-support
        "com.google.common.base.Predicate", // guava
        "com.google.common.util.concurrent.internal.InternalFutureFailureAccess", // guava failureaccess
        "org.apache.maven.artifact.ArtifactStatus", // maven-compat
        "org.apache.maven.shared.utils.logging.MessageUtils", // maven-shared-utils
        "org.codehaus.plexus.util.CachedMap", // plexus-utils
        "org.codehaus.plexus.classworlds.ClassWorld", // plexus-classworlds
        "org.codehaus.plexus.ContainerConfiguration", // org.eclipse.sisu.plexus
        "javax.decorator.Delegate", // cdi-api
        "org.eclipse.sisu.inject.MutableBeanLocator", // org.eclipse.sisu.inject
        "org.codehaus.plexus.component.annotations.Requirement", // plexus-component-annotations
        "org.sonatype.plexus.components.sec.dispatcher.SecDispatcher", // plexus-sec-dispatcher
        "org.sonatype.plexus.components.cipher.PlexusCipher", // plexus-cipher
        "org.apache.commons.cli.CommandLineParser", // commons-cli
        "org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory", // maven-resolver-connector-basic
        "org.eclipse.aether.RepositoryCache", // aether-api
        "org.eclipse.aether.spi.connector.RepositoryConnector", // aether-spi
        "org.eclipse.aether.util.ChecksumUtils", // aether-util
        "org.eclipse.aether.transport.wagon.WagonProvider", // maven-resolver-transport-wagon
        "io.takari.aether.client.AetherClient", // aether-connector-okhttp
        "okhttp3.ConnectionPool", // okhttp
        "okio.Okio", // okio
        "org.slf4j.ILoggerFactory", //slf4j-api
        "org.apache.maven.wagon.AbstractWagon", // wagon-provider-api
        "javax.annotation.processing.RoundEnvironment", // javax.annotations.processing (APT)
        "javax.jws.WebService", // javax.jws-api
        "javax.annotation.PostConstruct" // javax.annotations-api
    );
    return getJarsUrls(baseClasses);
  }

  /**
   * Gets the locations of the jars containing the given classes
   *
   * @param baseClasses
   *          The fqn of the classes
   * @return the {@link URL}s of the jars that contain the given class names.
   */
  static URL[] getJarsUrls(Collection<String> baseClasses) {
    return baseClasses.stream()
        .map(MavenSandboxClassLoaderFactory::getJarContaining)
        .toArray(URL[]::new);
  }

  /**
   * Gets the location of the jar containing the given class name.
   *
   * @param className
   *          the fully qualified class name.
   * @return The {@link URL} of the jar that contains the given class.
   */
  static URL getJarContaining(String className) {
    try {
      var classLoader = MavenSandboxClassLoaderFactory.class.getClassLoader();
      var clazz = classLoader.loadClass(className);
      var url = getJarContaining(clazz);
      if (url == null && classLoader instanceof URLClassLoader) {
        url = getJarContaining(className, (URLClassLoader) classLoader);
      }
      if (url == null) {
        throw new SdkException("Could not find jar of '{}'.", className);
      }
      return url;
    }
    catch (ClassNotFoundException e) {
      throw new SdkException(e);
    }
  }

  static URL getJarContaining(String className, URLClassLoader urlClassLoader) {
    return Arrays.stream(urlClassLoader.getURLs())
        .filter(url -> zipContainsEntry(url, className))
        .findFirst()
        .orElse(null);
  }

  @SuppressWarnings("squid:S1141") // nested try
  static boolean zipContainsEntry(URL zipUrl, String className) {
    try {
      var uri = zipUrl.toURI();
      if (!"file".equals(uri.getScheme())) {
        return false;
      }

      var file = Paths.get(uri);
      if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
        return false;
      }

      //noinspection NestedTryStatement
      try (var zip = new ZipFile(file.toFile())) {
        if (zipContainsEntry(zip, className)) {
          return true;
        }
      }
      catch (IOException e) {
        throw new SdkException(e);
      }
    }
    catch (URISyntaxException use) {
      throw new SdkException(use);
    }

    return false;
  }

  static boolean zipContainsEntry(ZipFile zip, String className) {
    return zip.getEntry(className.replace('.', '/') + JavaTypes.CLASS_FILE_SUFFIX) != null;
  }

  /**
   * @return The {@link URL} of the jar or folder that contains the given {@link Class}.
   */
  static URL getJarContaining(Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    var codeSource = clazz.getProtectionDomain().getCodeSource();
    if (codeSource == null) {
      return null;
    }
    var url = codeSource.getLocation();
    if (url == null) {
      return null;
    }
    if (url.getPath().endsWith(".jar")) {
      return url;
    }

    try {
      var fileUrl = new URL(url, "target/classes/");
      var f = Paths.get(fileUrl.toURI());
      if (Files.isReadable(f) || Files.isDirectory(f)) {
        return fileUrl;
      }

      fileUrl = url;
      f = Paths.get(fileUrl.toURI());
      if (Files.isReadable(f) || Files.isDirectory(f)) {
        return fileUrl;
      }
    }
    catch (MalformedURLException | URISyntaxException e) {
      SdkLog.warning(e);
    }
    return url;
  }
}
