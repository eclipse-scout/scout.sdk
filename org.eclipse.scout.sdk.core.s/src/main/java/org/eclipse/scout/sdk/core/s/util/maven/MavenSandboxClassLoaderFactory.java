/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.util.maven;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link MavenSandboxClassLoaderFactory}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class MavenSandboxClassLoaderFactory {

  private MavenSandboxClassLoaderFactory() {
  }

  /**
   * @return a new {@link URLClassLoader} capable to run the {@link MavenCliRunner}.
   */
  public static URLClassLoader build() {
    final ClassLoader parent;
    if (isJava8()) {
      // can be removed as soon as java 8 is no longer supported
      parent = null;
    }
    else {
      try {
        parent = (ClassLoader) ClassLoader.class.getMethod("getPlatformClassLoader").invoke(null);
      }
      catch (ReflectiveOperationException e) {
        throw new SdkException(e);
      }
    }
    return URLClassLoader.newInstance(getMavenJarsUrls(), parent);
  }

  static boolean isJava8() {
    // can be removed as soon as Java 8 is no longer supported.
    return "1.8".equals(System.getProperty("java.specification.version"));
  }

  static URL[] getMavenJarsUrls() {
    // contains a sample class of all jars required by the maven runtime.
    // the codesource of these classes will be the source of the classpath of the sandbox classloader.
    final List<String> baseClasses = new ArrayList<>();
    baseClasses.addAll(Arrays.asList(
        MavenCliRunner.class.getName(),
        "org.apache.commons.lang3.StringUtils", // apache-commons-lang3 for guice
        "org.apache.commons.io.DirectoryWalker", // commons-io
        "org.eclipse.scout.sdk.core.util.SdkLog", // sdk.core for logging
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
        "org.apache.maven.wagon.AbstractWagon" // wagon-provider-api
    ));
    if (!isJava8()) {
      baseClasses.addAll(Arrays.asList(
          "javax.annotation.processing.RoundEnvironment", // javax.annotations.processing (APT)
          "javax.jws.WebService", // javax.jws-api
          "javax.annotation.PostConstruct" // javax.annotations-api
      ));
    }

    return getJarsUrls(baseClasses);
  }

  /**
   * Gets the locations of the jars containing the given classes
   *
   * @param baseClasses
   *          The fqn of the classes
   * @return the {@link URL}s of the jars that contain the given class names.
   */
  static URL[] getJarsUrls(final Collection<String> baseClasses) {
    final List<URL> urls = new ArrayList<>(baseClasses.size());
    for (final String className : baseClasses) {
      urls.add(getJarContaining(className));
    }
    return urls.toArray(new URL[0]);
  }

  /**
   * Gets the location of the jar containing the given class name.
   *
   * @param className
   *          the fully qualified class name.
   * @return The {@link URL} of the jar that contains the given class.
   */
  static URL getJarContaining(final String className) {
    try {
      final Class<?> clazz = MavenSandboxClassLoaderFactory.class.getClassLoader().loadClass(className);
      final URL url = getJarContaining(clazz);
      if (url == null) {
        throw new SdkException("Could not find jar of '" + className + "'.");
      }
      return url;
    }
    catch (final ClassNotFoundException e) {
      throw new SdkException(e);
    }
  }

  /**
   * @return The {@link URL} of the jar or folder that contains the given {@link Class}.
   */
  static URL getJarContaining(final Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    final CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
    if (codeSource == null) {
      return null;
    }
    final URL url = codeSource.getLocation();
    if (url == null) {
      return null;
    }
    if (url.getPath().endsWith(".jar")) {
      return url;
    }

    try {
      URL fileUrl = new URL(url, "target/classes/");
      Path f = Paths.get(fileUrl.toURI());
      if (Files.isReadable(f) || Files.isDirectory(f)) {
        return fileUrl;
      }

      fileUrl = url;
      f = Paths.get(fileUrl.toURI());
      if (Files.isReadable(f) || Files.isDirectory(f)) {
        return fileUrl;
      }
    }
    catch (final MalformedURLException | URISyntaxException e) {
      SdkLog.warning(e);
    }
    return url;
  }
}
