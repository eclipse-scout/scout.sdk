/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.testing;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.scout.sdk.core.model.api.IFileLocator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.internal.ClasspathEntry;
import org.eclipse.scout.sdk.core.model.spi.internal.JavaEnvironmentWithJdt;
import org.eclipse.scout.sdk.core.model.spi.internal.WorkspaceFileSystem;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link JavaEnvironmentBuilder}</h3> Contains helpers used in order to create a {@link IJavaEnvironment}.
 *
 * @author Ivan Motsch
 * @since 5.2.0
 */
public class JavaEnvironmentBuilder {
  private final File m_curDir = new File("").getAbsoluteFile();
  private final List<Pattern> m_sourceExcludes = new ArrayList<>();
  private final List<Pattern> m_binaryExcludes = new ArrayList<>();
  private final Map<CompositeObject, ClasspathEntry> m_srcPaths = new TreeMap<>();
  private final Map<CompositeObject, ClasspathEntry> m_binPaths = new TreeMap<>();
  private final Set<File> m_findSourceAttachmentFor = new LinkedHashSet<>();

  private IFileLocator m_fileLocator;
  private boolean m_includeRunningClasspath = true;
  private boolean m_includeSources = true;

  /**
   * Include current running classpath, default is true
   *
   * @return this
   */
  public JavaEnvironmentBuilder withRunningClasspath(boolean b) {
    m_includeRunningClasspath = b;
    return this;
  }

  /**
   * Exclude classes paths containing <code>.scout.sdk.</code>
   *
   * @return this
   */
  public JavaEnvironmentBuilder withoutScoutSdk() {
    return without(".*" + Pattern.quote(".scout.sdk.") + ".*" + "target/classes");
  }

  /**
   * Exclude some paths
   *
   * @param regex
   *          file path pattern with '/' as delimiter
   * @return this
   */
  public JavaEnvironmentBuilder without(String regex) {
    m_sourceExcludes.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    m_binaryExcludes.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    return this;
  }

  /**
   * Exclude all source paths
   *
   * @return this
   */
  public JavaEnvironmentBuilder withoutAllSources() {
    m_includeSources = false;
    return this;
  }

  /**
   * Exclude specific source paths
   *
   * @return this
   */
  public JavaEnvironmentBuilder withoutSources(String regex) {
    m_sourceExcludes.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    return this;
  }

  /**
   * @param sourceFolder
   *          is a project relative path such as src/main/java
   * @return this
   */
  public JavaEnvironmentBuilder withSourceFolder(String sourceFolder) {
    if (sourceFolder != null) {
      appendSourcePath(new File(m_curDir, sourceFolder));
    }
    return this;
  }

  /**
   * @param classesFolder
   *          is a project relative path such as target/classes
   * @return this
   */
  public JavaEnvironmentBuilder withClassesFolder(String classesFolder) {
    if (classesFolder != null) {
      appendBinaryPath(new File(m_curDir, classesFolder));
    }
    return this;
  }

  /**
   * @param sourcePath
   *          is an absolute source path
   * @return this
   */
  public JavaEnvironmentBuilder withAbsoluteSourcePath(String sourcePath) {
    if (sourcePath != null) {
      appendSourcePath(new File(sourcePath));
    }
    return this;
  }

  public JavaEnvironmentBuilder withFileLocator(IFileLocator fileLocator) {
    m_fileLocator = fileLocator;
    return this;
  }

  /**
   * @param binaryPath
   *          is an absolute binary path
   * @return this
   */
  public JavaEnvironmentBuilder withAbsoluteBinaryPath(String binaryPath) {
    if (binaryPath != null) {
      appendBinaryPath(new File(binaryPath));
    }
    return this;
  }

  public File currentDirectory() {
    return m_curDir;
  }

  protected void collectBootstrapClassPath() {
    try {
      Class<?> launcher = Class.forName("sun.misc.Launcher");
      Object urlClassPath = launcher.getMethod("getBootstrapClassPath").invoke(null);
      URL[] urls = (URL[]) urlClassPath.getClass().getMethod("getURLs").invoke(urlClassPath);
      for (URL url : urls) {
        filterAndAppendBinaryPath(new File(url.toURI()), true);
      }
    }
    catch (Exception e) {
      SdkLog.info("Unable to read running bootstrap classpath. Fallback to minimal bootstrap classpath. Nested exception: ", e);

      String javaHome = SystemUtils.JAVA_HOME;
      if (StringUtils.isNotBlank(javaHome)) {
        File javaLocation = new File(javaHome);
        if (javaLocation.isDirectory()) {
          File rtJar = new File(javaLocation, "lib/rt.jar");
          filterAndAppendBinaryPath(rtJar, true);
        }
      }
    }
  }

  protected void collectRunningClassPath() {
    String javaClassPathRaw = SystemUtils.JAVA_CLASS_PATH;
    if (javaClassPathRaw != null && !javaClassPathRaw.isEmpty()) {
      String separator = File.pathSeparator;
      String[] elements = javaClassPathRaw.split(separator);
      for (String cpElement : elements) {
        filterAndAppendBinaryPath(new File(cpElement), true);
      }
    }
  }

  protected void collectCurrentClassLoaderUrls() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader instanceof URLClassLoader) {
      URL[] urls = ((URLClassLoader) contextClassLoader).getURLs();
      if (urls != null) {
        for (URL u : urls) {
          try {
            filterAndAppendBinaryPath(new File(u.toURI()), true);
          }
          catch (URISyntaxException e) {
            throw new SdkException("Invalid URI: '" + u.toString() + "'.", e);
          }
        }
      }
    }
  }

  /**
   * Check exclude filters and append path to collector using {@link #appendBinaryPath(File)}
   */
  protected void filterAndAppendBinaryPath(File f, boolean findSourceAttachments) {
    if (findSourceAttachments) {
      m_findSourceAttachmentFor.add(f);
    }
    if (!m_binaryExcludes.isEmpty()) {
      String s = f.getAbsolutePath().replace(File.separatorChar, '/');
      for (Pattern p : m_binaryExcludes) {
        if (p.matcher(s).matches()) {
          return;
        }
      }
    }
    appendBinaryPath(f);
  }

  /**
   * Check exclude filters and append path to collector using {@link #appendBinaryPath(File)}
   */
  protected void filterAndAppendSourcePath(File f) {
    if (!m_sourceExcludes.isEmpty()) {
      String s = f.getAbsolutePath().replace(File.separatorChar, '/');
      for (Pattern p : m_sourceExcludes) {
        if (p.matcher(s).matches()) {
          return;
        }
      }
    }
    appendSourcePath(f);
  }

  /**
   * Append path to binary path collector. Only append if the path exists
   */
  protected void appendBinaryPath(File f) {
    Classpath cp = WorkspaceFileSystem.createClasspath(f, false, StandardCharsets.UTF_8.name());
    if (cp == null) {
      return;
    }
    CompositeObject key = new CompositeObject(f.isDirectory() ? 1 : 2, cp.getPath());
    m_binPaths.put(key, new ClasspathEntry(cp, StandardCharsets.UTF_8.name()));
  }

  /**
   * Append path to source path collector. Only append if the path exists
   */
  protected void appendSourcePath(File f) {
    Classpath cp = WorkspaceFileSystem.createClasspath(f, true, null);
    if (cp == null) {
      return;
    }
    CompositeObject key = new CompositeObject(f.isDirectory() ? 1 : 2, cp.getPath());
    m_srcPaths.put(key, new ClasspathEntry(cp, StandardCharsets.UTF_8.name()));
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected void findSourceAttachments() {
    if (!m_includeSources) {
      return;
    }
    for (File f : m_findSourceAttachmentFor) {
      Path path = f.toPath();
      if (path.endsWith("jre/lib/rt.jar")) {
        filterAndAppendSourcePath(new File(path.getParent().getParent().getParent().toFile(), "src.zip"));
      }
      else if (path.endsWith("target/classes")) {
        filterAndAppendSourcePath(new File(path.getParent().getParent().toFile(), "src/main/java"));
        filterAndAppendSourcePath(new File(path.getParent().getParent().toFile(), "target/generated-sources/annotations"));
      }
      else if (f.getName().endsWith(".zip") || f.getName().endsWith(".jar")) {
        String s = f.getName();
        s = s.substring(0, s.length() - 4) + "-sources" + s.substring(s.length() - 4);
        filterAndAppendSourcePath(new File(f.getParentFile(), s));
      }
    }
  }

  protected IFileLocator createFileLocator() {
    if (m_fileLocator != null) {
      return m_fileLocator;
    }
    final File projectDir = currentDirectory();
    return new IFileLocator() {
      @Override
      public File getFile(String path) {
        return new File(projectDir, path);
      }
    };
  }

  public IJavaEnvironment build() {
    if (m_includeRunningClasspath) {
      collectBootstrapClassPath();
      collectRunningClassPath();
      collectCurrentClassLoaderUrls();

      findSourceAttachments();
    }

    List<ClasspathEntry> all = new ArrayList<>(m_srcPaths.size() + m_binPaths.size());
    all.addAll(m_srcPaths.values());
    all.addAll(m_binPaths.values());
    return new JavaEnvironmentWithJdt(createFileLocator(), all).wrap();
  }

}
