/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.search;

import static java.util.Collections.unmodifiableCollection;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link JavaProjectsWalker}</h3>
 *
 * @since 7.0.100
 */
public class JavaProjectsWalker {

  private final String m_taskName;
  private final Collection<String> m_fileExtensions;

  private boolean m_skipOutputLocation;
  private boolean m_skipHiddenPaths;
  private BiPredicate<Path, BasicFileAttributes> m_fileFilter;

  public JavaProjectsWalker(final String taskName) {
    m_taskName = Validate.notNull(taskName);
    m_fileExtensions = new ArrayList<>();
    m_skipOutputLocation = true;
    m_skipHiddenPaths = true;
  }

  public void walk(final Consumer<WorkspaceFile> visitor, final IProgressMonitor monitor) throws CoreException {
    final IJavaProject[] javaProjects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
    monitor.beginTask(taskName(), javaProjects.length);
    for (final IJavaProject root : javaProjects) {
      final IProject p = root.getProject();
      final Path outputLocation;
      if (isSkipOutputLocation()) {
        outputLocation = new File(p.getLocation().toOSString(), root.getOutputLocation().removeFirstSegments(1).toOSString()).toPath();
      }
      else {
        outputLocation = null;
      }

      searchInFolder(visitor, p.getLocation().toFile().toPath(), Charset.forName(p.getDefaultCharset()), outputLocation, monitor);

      if (monitor.isCanceled()) {
        return;
      }
      monitor.worked(1);
    }
  }

  protected void searchInFolder(final Consumer<WorkspaceFile> visitor, final Path folder, final Charset charset, final Path outputFolder, final IProgressMonitor monitor) {
    try {
      Files.walkFileTree(folder,
          new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
              if (monitor.isCanceled()) {
                return FileVisitResult.TERMINATE;
              }
              if (dir.equals(outputFolder)) {
                return FileVisitResult.SKIP_SUBTREE;
              }
              if (!hiddenFilterAndCustomFilterAccepted(dir, attrs)) {
                return FileVisitResult.SKIP_SUBTREE;
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
              if (monitor.isCanceled()) {
                return FileVisitResult.TERMINATE;
              }
              if (allFiltersAccepted(file, attrs)) {
                visitor.accept(new WorkspaceFile(file, charset));
              }
              return FileVisitResult.CONTINUE;
            }
          });
    }
    catch (final IOException e) {
      throw new SdkException(e);
    }
  }

  protected boolean hiddenFilterAndCustomFilterAccepted(final Path file, final BasicFileAttributes attrs) {
    if (isSkipHiddenPaths() && isHidden(file)) {
      return false;
    }

    return fileFilter()
        .map(filter -> filter.test(file, attrs))
        .orElse(Boolean.TRUE)
        .booleanValue();
  }

  protected boolean allFiltersAccepted(final Path file, final BasicFileAttributes attrs) {
    if (!acceptFileExtension(file)) {
      return false;
    }
    return hiddenFilterAndCustomFilterAccepted(file, attrs);
  }

  protected boolean acceptFileExtension(final Path file) {
    final Path path = file.getFileName();
    if (path == null) {
      return false;
    }
    final String fileName = path.toString().toLowerCase();
    for (final String extension : extensionsAccepted()) {
      if (fileName.endsWith(extension)) {
        return true;
      }
    }
    return false;
  }

  protected static boolean isHidden(final Path path) {
    final Path fileName = path.getFileName();
    return fileName != null && fileName.toString().startsWith(".");
  }

  public String taskName() {
    return m_taskName;
  }

  public boolean isSkipOutputLocation() {
    return m_skipOutputLocation;
  }

  public JavaProjectsWalker withSkipOutputLocation(final boolean skipOutputLocation) {
    m_skipOutputLocation = skipOutputLocation;
    return this;
  }

  public boolean isSkipHiddenPaths() {
    return m_skipHiddenPaths;
  }

  public JavaProjectsWalker withSkipHiddenPaths(final boolean skipHiddenPaths) {
    m_skipHiddenPaths = skipHiddenPaths;
    return this;
  }

  public Optional<BiPredicate<Path, BasicFileAttributes>> fileFilter() {
    return Optional.ofNullable(m_fileFilter);
  }

  public JavaProjectsWalker withFilter(final BiPredicate<Path, BasicFileAttributes> fileFilter) {
    m_fileFilter = fileFilter;
    return this;
  }

  public Collection<String> extensionsAccepted() {
    return unmodifiableCollection(m_fileExtensions);
  }

  public JavaProjectsWalker withExtensionsAccepted(final String... extensions) {
    final Collection<String> l = extensions == null ? null : Arrays.asList(extensions);
    return withExtensionsAccepted(l);
  }

  public JavaProjectsWalker withExtensionsAccepted(final Collection<String> extensions) {
    m_fileExtensions.clear();
    if (extensions != null && !extensions.isEmpty()) {
      for (final String e : extensions) {
        if (StringUtils.isNotBlank(e)) {
          m_fileExtensions.add(e);
        }
      }
    }
    return this;
  }

  public static class WorkspaceFile {
    private final Path m_file;
    private final Charset m_charset;
    private char[] m_content; // loaded on request
    private Optional<IFile> m_workspaceFile; // loaded on request

    protected WorkspaceFile(final Path file, final Charset charset) {
      m_file = Validate.notNull(file);
      m_charset = Validate.notNull(charset);
    }

    public Charset charset() {
      return m_charset;
    }

    public Path path() {
      return m_file;
    }

    public Optional<IFile> inWorkspace() {
      if (m_workspaceFile == null) {
        m_workspaceFile = Optional.ofNullable(resolveInWorkspace(path()));
      }
      return m_workspaceFile;
    }

    protected static IFile resolveInWorkspace(final Path file) {
      final IFile[] workspaceFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toUri());
      if (workspaceFiles.length < 1) {
        return null;
      }
      final IFile workspaceFile = workspaceFiles[0];
      if (!workspaceFile.exists()) {
        return null;
      }
      return workspaceFile;
    }

    public char[] content() {
      if (m_content == null) {
        try {
          m_content = charset().decode(ByteBuffer.wrap(Files.readAllBytes(path()))).array();
        }
        catch (final IOException e) {
          throw new SdkException("Unable to read content of file '" + path() + "'.", e);
        }
      }
      return m_content;
    }

    @Override
    public String toString() {
      return WorkspaceFile.class.getSimpleName() + ": " + path();
    }

    @Override
    public int hashCode() {
      return m_file.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      final WorkspaceFile other = (WorkspaceFile) obj;
      return m_file.equals(other.m_file);
    }
  }
}
