/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.environment.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.ecj.ClasspathEntry;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcj;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link JavaEnvironmentWithJdt}</h3>
 *
 * @since 7.0.0
 */
public class JavaEnvironmentWithJdt extends JavaEnvironmentWithEcj {

  private final IJavaProject m_project;
  @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
  private FinalValue<Map<IPackageFragmentRoot, ClasspathSpi>> m_classpath;

  public JavaEnvironmentWithJdt(IJavaProject project) {
    this(project, false);
  }

  public JavaEnvironmentWithJdt(IJavaProject project, boolean excludeTestCode) {
    this(project, javaHomeOf(project), excludeTestCode);
  }

  public JavaEnvironmentWithJdt(IJavaProject project, Path javaHome) {
    this(project, javaHome, false);
  }

  public JavaEnvironmentWithJdt(IJavaProject project, Path javaHome, boolean excludeTestCode) {
    this(project, javaHome, toClasspathEntries(project, excludeTestCode));
  }

  protected JavaEnvironmentWithJdt(IJavaProject project, Path javaHome, Collection<ClasspathEntryWithJdt> cp) {
    super(javaHome, cp, null /* use defaults */);
    m_project = project;
    m_classpath = new FinalValue<>();
  }

  @Override
  protected ClasspathSpi classpathEntryToSpi(ClasspathEntry entry) {
    return new ClasspathWithJdt((ClasspathEntryWithJdt) entry, this);
  }

  protected static Path javaHomeOf(IJavaProject javaProject) {
    try {
      var vmInstall = JavaRuntime.getVMInstall(javaProject);
      if (vmInstall != null) {
        var javaInstallLocation = vmInstall.getInstallLocation();
        if (javaInstallLocation != null) {
          var javaInstallPath = javaInstallLocation.toPath();
          if (Files.isDirectory(javaInstallPath.resolve("jre/lib"))) {
            // the install location points to a JDK that contains a JRE! Use the JRE as Java home
            return javaInstallPath.resolve("jre");
          }
          return javaInstallPath;
        }
      }
      SdkLog.info("Unable to find Java home location for project '{}'. Using running Java home as fallback.", javaProject.getElementName());
      return null; // use the running JRE as fallback
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
  }

  protected static List<ClasspathEntryWithJdt> toClasspathEntries(IJavaProject javaProject, boolean excludeTestCode) {
    try {
      var allPackageFragmentRoots = javaProject.getAllPackageFragmentRoots();
      List<ClasspathEntryWithJdt> result = new ArrayList<>(allPackageFragmentRoots.length * 2);
      for (var cpRoot : allPackageFragmentRoots) {
        if (excludeTestCode && cpRoot.getRawClasspathEntry().isTest()) {
          continue;
        }

        var encoding = getEncoding(cpRoot);
        if (cpRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
          result.add(new ClasspathEntryWithJdt(cpRoot, cpRoot.getResource().getLocation().toFile().toPath(), ClasspathSpi.MODE_SOURCE, encoding));
        }
        else if (!isJreContainerElement(cpRoot)) {
          var cpLocation = cpRoot.getPath().toFile().toPath();
          var cpSourceLocation = cpRoot.getSourceAttachmentPath();
          if (cpSourceLocation != null) {
            result.add(new ClasspathEntryWithJdt(cpRoot, cpSourceLocation.toFile().toPath(), ClasspathSpi.MODE_SOURCE, encoding));
            result.add(new ClasspathEntryWithJdt(cpRoot, cpLocation, ClasspathSpi.MODE_BINARY, null));
          }
          else {
            result.add(new ClasspathEntryWithJdt(cpRoot, cpLocation, ClasspathSpi.MODE_SOURCE | ClasspathSpi.MODE_BINARY, encoding));
          }
        }
      }
      return result;
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
  }

  protected static boolean isJreContainerElement(IPackageFragmentRoot root) throws JavaModelException {
    var entry = root.getRawClasspathEntry();
    if (entry.getEntryKind() != IClasspathEntry.CPE_CONTAINER) {
      return false;
    }
    var type = entry.getPath().segment(0);
    return JavaRuntime.JRE_CONTAINER.equals(type);
  }

  protected static String getEncoding(IJavaElement root) throws CoreException {
    var resource = root.getResource();
    if (resource != null && resource.exists()) {
      // check file
      if (resource instanceof IFile f) {
        var charset = f.getCharset(true);
        if (isValidEncoding(charset, root)) {
          return charset;
        }
      }
      else if (resource instanceof IContainer c) {
        // check folder
        var charset = c.getDefaultCharset(true);
        if (isValidEncoding(charset, root)) {
          return charset;
        }
      }
    }

    // check project settings
    var preferencesService = Platform.getPreferencesService();
    if (preferencesService != null) {
      IScopeContext[] scopeContext = {new ProjectScope(root.getJavaProject().getProject())};
      var encoding = preferencesService.getString(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PREF_ENCODING, null, scopeContext);
      if (isValidEncoding(encoding, root)) {
        return encoding;
      }

      // check workspace settings
      scopeContext = new IScopeContext[]{InstanceScope.INSTANCE};
      encoding = preferencesService.getString(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PREF_ENCODING, null, scopeContext);
      if (isValidEncoding(encoding, root)) {
        return encoding;
      }
    }

    //noinspection AccessOfSystemProperties
    var systemEncoding = System.getProperty("file.encoding");
    if (systemEncoding != null) {
      return systemEncoding;
    }

    return StandardCharsets.UTF_8.name();
  }

  protected static boolean isValidEncoding(String encoding, IJavaElement root) {
    if (Strings.isBlank(encoding)) {
      return false;
    }

    if (!Charset.isSupported(encoding)) {
      SdkLog.warning("Charset '{}' of classpath entry '{}' is not supported by this platform. Trying to decode using default charset.", encoding, root.getElementName());
      return false;
    }

    return true;
  }

  protected Map<IPackageFragmentRoot, ClasspathSpi> getClasspathMap() {
    return m_classpath.computeIfAbsentAndGet(() -> {
      var classpath = getClasspath();
      Map<IPackageFragmentRoot, ClasspathSpi> result = new HashMap<>(classpath.size());
      for (var cp : classpath) {
        var jdtCp = (ClasspathWithJdt) cp;
        result.put(jdtCp.getRoot(), cp);
      }
      return result;
    });
  }

  @Override
  protected void cleanup() {
    m_classpath = new FinalValue<>();
    super.cleanup();
  }

  public IJavaProject javaProject() {
    return m_project;
  }

  public ClasspathSpi getClasspathFor(IPackageFragmentRoot root) {
    return getClasspathMap().get(root);
  }

  @Override
  public void close() {
    synchronized (lock()) {
      m_classpath.opt().ifPresent(Map::clear);
      super.close();
    }
  }
}
