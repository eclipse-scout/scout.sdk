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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.scout.sdk.core.model.api.IFileLocator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.internal.ClasspathEntry;
import org.eclipse.scout.sdk.core.model.spi.internal.JavaEnvironmentWithJdt;
import org.eclipse.scout.sdk.core.model.spi.internal.WorkspaceFileSystem;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.internal.WorkingCopyManager;
import org.eclipse.scout.sdk.s2e.trigger.IDerivedResourceManager;
import org.eclipse.scout.sdk.s2e.workspace.IWorkingCopyManager;

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
    return S2ESdkActivator.getDefault().getDerivedResourceManager();
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
    return new JavaEnvironmentWithJdt(createFileLocator(javaProject), createClasspaths(javaProject)).wrap();
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

  private static ClasspathEntry[] createClasspaths(IJavaProject javaProject) {
    Set<ClasspathEntry> paths = new LinkedHashSet<>();
    appendPaths(paths, javaProject);
    return paths.toArray(new ClasspathEntry[paths.size()]);
  }

  private static void appendPaths(Collection<ClasspathEntry> paths, IJavaProject javaProject) {
    try {
      IPackageFragmentRoot[] allPackageFragmentRoots = javaProject.getAllPackageFragmentRoots();
      for (IPackageFragmentRoot cpRoot : allPackageFragmentRoots) {
        String encoding = getEncoding(cpRoot);
        if (cpRoot.getSourceAttachmentPath() != null) {
          appendPath(paths, cpRoot.getSourceAttachmentPath().toFile(), true, encoding);
        }
        if (cpRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
          appendPath(paths, cpRoot.getResource().getLocation().toFile(), true, encoding);
        }
        else {
          appendPath(paths, cpRoot.getPath().toFile(), true, encoding);//add also as source in case the .java files are in the same jar
          appendPath(paths, cpRoot.getPath().toFile(), false, encoding);//and binary
        }
      }
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
  }

  private static void appendPath(Collection<ClasspathEntry> paths, File f, boolean source, String encoding) {
    Classpath classpath = WorkspaceFileSystem.createClasspath(f, source, encoding);
    if (classpath != null) {
      paths.add(new ClasspathEntry(classpath, encoding));
    }
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

    return System.getProperty("file.encoding", StandardCharsets.UTF_8.name());
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
