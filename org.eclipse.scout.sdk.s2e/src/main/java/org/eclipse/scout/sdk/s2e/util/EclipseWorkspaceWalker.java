/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.util;

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
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link EclipseWorkspaceWalker}</h3>
 *
 * @since 7.0.100
 */
public class EclipseWorkspaceWalker {

  private final String m_taskName;
  private final Collection<String> m_fileExtensions;

  private boolean m_skipOutputLocation;
  private boolean m_skipHiddenPaths;
  private BiPredicate<Path, BasicFileAttributes> m_fileFilter;

  public EclipseWorkspaceWalker(String taskName) {
    m_taskName = Ensure.notNull(taskName);
    m_fileExtensions = new ArrayList<>();
    m_skipOutputLocation = true;
    m_skipHiddenPaths = true;
  }

  public void walk(Consumer<WorkspaceFile> visitor, IProgressMonitor monitor) throws CoreException {
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    monitor.beginTask(taskName(), projects.length);
    for (IProject root : projects) {
      Path outputLocation;
      if (isSkipOutputLocation()) {
        IJavaProject jp = JavaCore.create(root);
        if (JdtUtils.exists(jp)) {
          outputLocation = new File(root.getLocation().toOSString(), jp.getOutputLocation().removeFirstSegments(1).toOSString()).toPath();
        }
        else {
          outputLocation = null;
        }
      }
      else {
        outputLocation = null;
      }

      searchInFolder(visitor, root.getLocation().toFile().toPath(), Charset.forName(root.getDefaultCharset()), outputLocation, monitor);

      if (monitor.isCanceled()) {
        return;
      }
      monitor.worked(1);
    }
  }

  protected void searchInFolder(Consumer<WorkspaceFile> visitor, Path folder, Charset charset, Path outputFolder, IProgressMonitor monitor) {
    try {
      Files.walkFileTree(folder,
          new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
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
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
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
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  protected boolean hiddenFilterAndCustomFilterAccepted(Path file, BasicFileAttributes attrs) {
    if (isSkipHiddenPaths() && isHidden(file)) {
      return false;
    }

    return fileFilter()
        .map(filter -> filter.test(file, attrs))
        .orElse(Boolean.TRUE);
  }

  protected boolean allFiltersAccepted(Path file, BasicFileAttributes attrs) {
    if (!acceptFileExtension(file)) {
      return false;
    }
    return hiddenFilterAndCustomFilterAccepted(file, attrs);
  }

  protected boolean acceptFileExtension(Path file) {
    if (extensionsAccepted().isEmpty()) {
      return true; // no filter
    }
    Path path = file.getFileName();
    if (path == null) {
      return false;
    }
    String fileName = path.toString().toLowerCase(Locale.ENGLISH);
    for (String extension : extensionsAccepted()) {
      if (fileName.endsWith(extension)) {
        return true;
      }
    }
    return false;
  }

  protected static boolean isHidden(Path path) {
    Path fileName = path.getFileName();
    return fileName != null && fileName.toString().startsWith(".");
  }

  public String taskName() {
    return m_taskName;
  }

  public boolean isSkipOutputLocation() {
    return m_skipOutputLocation;
  }

  public EclipseWorkspaceWalker withSkipOutputLocation(boolean skipOutputLocation) {
    m_skipOutputLocation = skipOutputLocation;
    return this;
  }

  public boolean isSkipHiddenPaths() {
    return m_skipHiddenPaths;
  }

  public EclipseWorkspaceWalker withSkipHiddenPaths(boolean skipHiddenPaths) {
    m_skipHiddenPaths = skipHiddenPaths;
    return this;
  }

  public Optional<BiPredicate<Path, BasicFileAttributes>> fileFilter() {
    return Optional.ofNullable(m_fileFilter);
  }

  public EclipseWorkspaceWalker withFilter(BiPredicate<Path, BasicFileAttributes> fileFilter) {
    m_fileFilter = fileFilter;
    return this;
  }

  public Collection<String> extensionsAccepted() {
    return unmodifiableCollection(m_fileExtensions);
  }

  public EclipseWorkspaceWalker withExtensionsAccepted(String... extensions) {
    Collection<String> l = extensions == null ? null : Arrays.asList(extensions);
    return withExtensionsAccepted(l);
  }

  public EclipseWorkspaceWalker withExtensionsAccepted(Collection<String> extensions) {
    m_fileExtensions.clear();
    if (extensions != null && !extensions.isEmpty()) {
      for (String e : extensions) {
        if (Strings.hasText(e)) {
          m_fileExtensions.add(e);
        }
      }
    }
    return this;
  }

  public static class WorkspaceFile {
    private final Path m_file;
    private final Charset m_charset;
    private final FinalValue<IFile> m_workspaceFile; // loaded on request
    private char[] m_content; // loaded on request

    protected WorkspaceFile(Path file, Charset charset) {
      m_file = Ensure.notNull(file);
      m_charset = Ensure.notNull(charset);
      m_workspaceFile = new FinalValue<>();
    }

    public Charset charset() {
      return m_charset;
    }

    public Path path() {
      return m_file;
    }

    public Optional<IFile> inWorkspace() {
      return Optional.ofNullable(m_workspaceFile.computeIfAbsentAndGet(() -> resolveInWorkspace(path())));
    }

    protected static IFile resolveInWorkspace(Path file) {
      IFile[] workspaceFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toUri());
      if (workspaceFiles.length < 1) {
        return null;
      }
      IFile workspaceFile = workspaceFiles[0];
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
        catch (IOException e) {
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
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      WorkspaceFile other = (WorkspaceFile) obj;
      return m_file.equals(other.m_file);
    }
  }
}
