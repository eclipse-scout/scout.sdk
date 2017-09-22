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
package org.eclipse.scout.sdk.s2e;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.scout.sdk.core.model.api.IFileLocator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.model.spi.internal.ClasspathEntry;
import org.eclipse.scout.sdk.core.model.spi.internal.JavaEnvironmentWithJdt;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.internal.WorkingCopyManager;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.trigger.IDerivedResourceManager;

/**
 * Main class to access Scout2Eclipse core components.
 */
public final class ScoutSdkCore {

  private ScoutSdkCore() {
  }

  /**
   * Gets the {@link IDerivedResourceManager} responsible for automatically update Scout DTOs in the Eclipse IDE.
   *
   * @return
   */
  public static IDerivedResourceManager getDerivedResourceManager() {
    S2ESdkActivator plugin = S2ESdkActivator.getDefault();
    if (plugin == null) {
      return null;
    }
    return plugin.getDerivedResourceManager();
  }

  /**
   * @return A new created {@link IWorkingCopyManager}.
   */
  public static IWorkingCopyManager createWorkingCopyManager() {
    return new WorkingCopyManager();
  }

  /**
   * Creates an {@link IJavaEnvironment} based on an Eclipse {@link IJavaProject} and its classpath.
   *
   * @param javaProject
   *          the {@link IJavaProject} used to create the {@link IJavaEnvironment} for.
   * @return The new created {@link IJavaEnvironment}.
   */
  public static IJavaEnvironment createJavaEnvironment(IJavaProject javaProject) {
    Validate.notNull(javaProject);
    return new JavaEnvironmentWithJdt(createFileLocator(javaProject), javaHomeOf(javaProject), getClasspathEntries(javaProject)).wrap();
  }

  private static Path javaHomeOf(final IJavaProject javaProject) {
    try {
      final IVMInstall vmInstall = JavaRuntime.getVMInstall(javaProject);
      if (vmInstall != null) {
        final File javaInstallLocation = vmInstall.getInstallLocation();
        if (javaInstallLocation != null) {
          final Path javaInstallPath = javaInstallLocation.toPath();
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
    catch (final CoreException e) {
      throw new SdkException(e);
    }
  }

  private static IFileLocator createFileLocator(IJavaProject javaProject) {
    final IProject project = javaProject.getProject();
    return new IFileLocator() {
      @Override
      public File getFile(String path) {
        return project.getFile(path).getRawLocation().toFile();
      }
    };
  }

  private static List<ClasspathEntry> getClasspathEntries(IJavaProject javaProject) {
    try {
      IPackageFragmentRoot[] allPackageFragmentRoots = javaProject.getAllPackageFragmentRoots();
      List<ClasspathEntry> result = new ArrayList<>(allPackageFragmentRoots.length);
      for (IPackageFragmentRoot cpRoot : allPackageFragmentRoots) {
        String encoding = getEncoding(cpRoot);
        if (cpRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
          result.add(new ClasspathEntry(cpRoot.getResource().getLocation().toFile().toPath(), ClasspathSpi.MODE_SOURCE, encoding));
        }
        else if (!isJreContainerElement(cpRoot)) {
          final Path cpLocation = cpRoot.getPath().toFile().toPath();
          final IPath cpSourceLocation = cpRoot.getSourceAttachmentPath();
          if (cpSourceLocation != null) {
            result.add(new ClasspathEntry(cpSourceLocation.toFile().toPath(), ClasspathSpi.MODE_SOURCE, encoding));
            result.add(new ClasspathEntry(cpLocation, ClasspathSpi.MODE_BINARY, null));
          }
          else {
            result.add(new ClasspathEntry(cpLocation, ClasspathSpi.MODE_SOURCE | ClasspathSpi.MODE_BINARY, encoding));
          }
        }
      }
      return result;
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
  }

  private static boolean isJreContainerElement(final IPackageFragmentRoot root) throws JavaModelException {
    final IClasspathEntry entry = root.getRawClasspathEntry();
    if (entry.getEntryKind() != IClasspathEntry.CPE_CONTAINER) {
      return false;
    }
    final String type = entry.getPath().segment(0);
    return JavaRuntime.JRE_CONTAINER.equals(type);
  }

  private static String getEncoding(IPackageFragmentRoot root) throws CoreException {
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
      IScopeContext[] scopeContext = new IScopeContext[]{new ProjectScope(root.getJavaProject().getProject())};
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

    String systemEncoding = SystemUtils.FILE_ENCODING;
    if (systemEncoding != null) {
      return systemEncoding;
    }

    return StandardCharsets.UTF_8.name();
  }

  private static boolean isValidEncoding(String encoding, IPackageFragmentRoot root) {
    if (StringUtils.isNotBlank(encoding)) {
      if (Charset.isSupported(encoding)) {
        return true;
      }
      SdkLog.warning("Charset '{}' of classpath entry '{}' is not supported by this platform. Trying to decode using default charset.", encoding, root.getElementName());
    }
    return false;
  }
}
