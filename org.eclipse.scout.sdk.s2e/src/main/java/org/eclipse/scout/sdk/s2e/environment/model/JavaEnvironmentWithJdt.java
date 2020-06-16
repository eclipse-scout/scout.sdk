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
package org.eclipse.scout.sdk.s2e.environment.model;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
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
      IVMInstall vmInstall = JavaRuntime.getVMInstall(javaProject);
      if (vmInstall != null) {
        File javaInstallLocation = vmInstall.getInstallLocation();
        if (javaInstallLocation != null) {
          Path javaInstallPath = javaInstallLocation.toPath();
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
      IPackageFragmentRoot[] allPackageFragmentRoots = javaProject.getAllPackageFragmentRoots();
      List<ClasspathEntryWithJdt> result = new ArrayList<>(allPackageFragmentRoots.length * 2);
      for (IPackageFragmentRoot cpRoot : allPackageFragmentRoots) {
        if (excludeTestCode && cpRoot.getRawClasspathEntry().isTest()) {
          continue;
        }

        String encoding = getEncoding(cpRoot);
        if (cpRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
          result.add(new ClasspathEntryWithJdt(cpRoot, cpRoot.getResource().getLocation().toFile().toPath(), ClasspathSpi.MODE_SOURCE, encoding));
        }
        else if (!isJreContainerElement(cpRoot)) {
          Path cpLocation = cpRoot.getPath().toFile().toPath();
          IPath cpSourceLocation = cpRoot.getSourceAttachmentPath();
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
    IClasspathEntry entry = root.getRawClasspathEntry();
    if (entry.getEntryKind() != IClasspathEntry.CPE_CONTAINER) {
      return false;
    }
    String type = entry.getPath().segment(0);
    return JavaRuntime.JRE_CONTAINER.equals(type);
  }

  protected static String getEncoding(IJavaElement root) throws CoreException {
    IResource resource = root.getResource();
    if (resource != null && resource.exists()) {
      // check file
      if (resource instanceof IFile) {
        IFile f = (IFile) resource;
        String charset = f.getCharset(true);
        if (isValidEncoding(charset, root)) {
          return charset;
        }
      }
      else if (resource instanceof IContainer) {
        // check folder
        IContainer c = (IContainer) resource;
        String charset = c.getDefaultCharset(true);
        if (isValidEncoding(charset, root)) {
          return charset;
        }
      }
    }

    // check project settings
    IPreferencesService preferencesService = Platform.getPreferencesService();
    if (preferencesService != null) {
      IScopeContext[] scopeContext = {new ProjectScope(root.getJavaProject().getProject())};
      String encoding = preferencesService.getString(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PREF_ENCODING, null, scopeContext);
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
    String systemEncoding = System.getProperty("file.encoding");
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
      List<ClasspathSpi> classpath = getClasspath();
      Map<IPackageFragmentRoot, ClasspathSpi> result = new HashMap<>(classpath.size());
      for (ClasspathSpi cp : classpath) {
        ClasspathWithJdt jdtCp = (ClasspathWithJdt) cp;
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

  @Override
  protected JavaEnvironmentWithJdt emptyCopy() {
    @SuppressWarnings("unchecked")
    Set<ClasspathEntryWithJdt> classpath = (Set<ClasspathEntryWithJdt>) getNameEnvironment().classpath();
    JavaEnvironmentWithJdt newEnv = new JavaEnvironmentWithJdt(javaProject(), javaHome(), classpath);
    runPreservingOverrides(this, newEnv, null);
    return newEnv;
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
