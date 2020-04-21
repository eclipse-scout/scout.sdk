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
package org.eclipse.scout.sdk.s2e.util;

import static java.util.Collections.unmodifiableCollection;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.callInEclipseEnvironment;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.callInEclipseEnvironmentSync;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.toScoutProgress;

import java.io.File;
import java.io.IOException;
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
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput;
import org.eclipse.scout.sdk.core.s.util.search.IFileQuery;
import org.eclipse.scout.sdk.core.s.util.search.IFileQueryResult;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;

/**
 * <h3>{@link EclipseWorkspaceWalker}</h3>
 * <p>
 * Supports visiting all files within a Eclipse workspace.
 * </p>
 * <p>
 * By default project output directories, hidden paths (leading dot) and node_modules folders are skipped. This
 * behaviour may be changed using the methods {@link #withExtensionsAccepted(String...)},
 * {@link #withFilter(BiPredicate)}, {@link #withSkipHiddenPaths(boolean)}, {@link #withSkipNodeModules(boolean)} and
 * {@link #withSkipOutputLocation(boolean)}.
 * </p>
 *
 * @since 7.0.100
 */
public class EclipseWorkspaceWalker {

  private final String m_taskName;
  private final Collection<String> m_fileExtensions;

  private boolean m_skipOutputLocation;
  private boolean m_skipHiddenPaths;
  private boolean m_skipNodeModules;
  private BiPredicate<Path, BasicFileAttributes> m_fileFilter;

  /**
   * @param taskName
   *          The task name of this walker. This name is used in the progress monitor while visiting the workspace.
   */
  public EclipseWorkspaceWalker(String taskName) {
    m_taskName = Ensure.notNull(taskName);
    m_fileExtensions = new ArrayList<>();
    m_skipOutputLocation = true;
    m_skipHiddenPaths = true;
    m_skipNodeModules = true;
  }

  /**
   * Executes the query specified in the current Eclipse workspace. The execution uses default visit settings (see
   * {@link EclipseWorkspaceWalker}).
   *
   * @param query
   *          The {@link IFileQuery} to execute. Must not be {@code null}.
   * @param monitor
   *          For progress indication or cancellation. Must not be {@code null}.
   * @return The {@link IFileQueryResult} after execution.
   */
  public static IFileQueryResult executeQuerySync(IFileQuery query, IProgressMonitor monitor) {
    return callInEclipseEnvironmentSync((e, p) -> executeQueryInWorkspace(query, e, p), monitor);
  }

  /**
   * Asynchronously executes the query specified in the current Eclipse workspace. The execution uses default visit
   * settings (see {@link EclipseWorkspaceWalker}).
   *
   * @param query
   *          The {@link IFileQuery} to execute. Must not be {@code null}.
   * @return An {@link IFuture} to control the asynchronous computation (cancel, wait, retrieve result).
   */
  public static IFuture<IFileQueryResult> executeQuery(IFileQuery query) {
    return callInEclipseEnvironment((e, p) -> executeQueryInWorkspace(query, e, p), null, query.name());
  }

  protected static IFileQuery executeQueryInWorkspace(IFileQuery query, IEnvironment e, EclipseProgress p) {
    try {
      new EclipseWorkspaceWalker(query.name())
          .walk((file, progress) -> executeQueryInFile(query, file, e, progress), p.monitor());
      return query;
    }
    catch (CoreException ex) {
      throw new SdkException(ex);
    }
  }

  protected static void executeQueryInFile(IFileQuery query, WorkspaceFile file, IEnvironment env, IProgress progress) {
    if (!file.inWorkspace().isPresent()) {
      SdkLog.warning("File '{}' could not be found in the current Eclipse Workspace.", file.path());
      return;
    }
    Path modulePath = file.inWorkspace().get().getProject().getLocation().toFile().toPath();
    FileQueryInput candidate = new FileQueryInput(file.path(), modulePath, file::content);
    query.searchIn(candidate, env, progress);
  }

  /**
   * Executes the visitor in the active Eclipse workspace.
   *
   * @param visitor
   *          The visitor to call for each file in the workspace. Must not be {@code null}.
   * @param monitor
   *          For progress monitoring or cancellation. May be {@code null}.
   * @throws CoreException
   */
  public void walk(BiConsumer<WorkspaceFile, IProgress> visitor, IProgressMonitor monitor) throws CoreException {
    Ensure.notNull(visitor);
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    SubMonitor subMonitor = SubMonitor.convert(monitor, taskName(), projects.length * 2);
    for (IProject root : projects) {
      Path outputLocation;
      subMonitor.subTask(root.getName());
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

      Path projectPath = root.getLocation().toFile().toPath();
      if (Files.exists(projectPath)) {
        searchInFolder(visitor, projectPath, Charset.forName(root.getDefaultCharset()), outputLocation, subMonitor.newChild(1));
      }

      if (subMonitor.isCanceled()) {
        return;
      }
      subMonitor.worked(1);
    }
  }

  protected void searchInFolder(BiConsumer<WorkspaceFile, IProgress> visitor, Path folder, Charset charset, Path outputFolder, IProgressMonitor monitor) {
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
              if (!directoryFiltersAccepted(dir, attrs)) {
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
                visitor.accept(new WorkspaceFile(file, charset), toScoutProgress(monitor));
              }
              return FileVisitResult.CONTINUE;
            }
          });
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  protected boolean directoryFiltersAccepted(Path file, BasicFileAttributes attrs) {
    if (isSkipHiddenPaths() && isHidden(file)) {
      return false;
    }

    Path fileName = file.getFileName();
    if (isSkipNodeModules() && fileName != null && "node_modules".equals(fileName.toString())) {
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
    return directoryFiltersAccepted(file, attrs);
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

  /**
   * @return The task name of this walker. This string is used in the progress monitor.
   */
  public String taskName() {
    return m_taskName;
  }

  /**
   * @return {@code true} if module output directories (like target/classes or bin) should e skipped when visiting.
   */
  public boolean isSkipOutputLocation() {
    return m_skipOutputLocation;
  }

  /**
   * @param skipOutputLocation
   *          {@code true} if module output directories (like target/classes or bin) should e skipped when visiting.
   * @return this instance
   */
  public EclipseWorkspaceWalker withSkipOutputLocation(boolean skipOutputLocation) {
    m_skipOutputLocation = skipOutputLocation;
    return this;
  }

  /**
   * @return {@code true} if hidden paths (file or folder names with leading dot) should be skipped when visiting.
   */
  public boolean isSkipHiddenPaths() {
    return m_skipHiddenPaths;
  }

  /**
   * @param skipHiddenPaths
   *          {@code true} if hidden paths (file or folder names with leading dot) should be skipped when visiting.
   * @return this instance
   */
  public EclipseWorkspaceWalker withSkipHiddenPaths(boolean skipHiddenPaths) {
    m_skipHiddenPaths = skipHiddenPaths;
    return this;
  }

  /**
   * @return {@code true} if "node_modules" folders should be skipped when visiting.
   */
  public boolean isSkipNodeModules() {
    return m_skipNodeModules;
  }

  /**
   * @param skipNodeModules
   *          {@code true} if "node_modules" folders should be skipped when visiting.
   * @return this instance
   */
  public EclipseWorkspaceWalker withSkipNodeModules(boolean skipNodeModules) {
    m_skipNodeModules = skipNodeModules;
    return this;
  }

  /**
   * @return A custom file filter if available.
   */
  public Optional<BiPredicate<Path, BasicFileAttributes>> fileFilter() {
    return Optional.ofNullable(m_fileFilter);
  }

  /**
   * @param fileFilter
   *          A custom file filter if available.
   * @return this instance
   */
  public EclipseWorkspaceWalker withFilter(BiPredicate<Path, BasicFileAttributes> fileFilter) {
    m_fileFilter = fileFilter;
    return this;
  }

  /**
   * @return An unmodifiable collection holding the file extensions (with or without extension separator dot) which are
   *         visited or an empty collection if all files should be visited.
   */
  public Collection<String> extensionsAccepted() {
    return unmodifiableCollection(m_fileExtensions);
  }

  /**
   * @param extensions
   *          Sets all file extensions (with or without extension separator dot) which should be visited. May be
   *          {@code null}.
   * @return this instance
   */
  public EclipseWorkspaceWalker withExtensionsAccepted(String... extensions) {
    Collection<String> l = extensions == null ? null : Arrays.asList(extensions);
    return withExtensionsAccepted(l);
  }

  /**
   * @param extensions
   *          Sets all file extensions (with or without extension separator dot) which should be visited. May be
   *          {@code null}.
   * @return this instance
   */
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

  /**
   * Represents a file in an Eclipse workspace
   */
  public static class WorkspaceFile {
    private final Path m_file;
    private final Charset m_charset;
    private final FinalValue<IFile> m_workspaceFile; // loaded on request
    private char[] m_content; // loaded on request

    public WorkspaceFile(Path file, Charset charset) {
      m_file = Ensure.notNull(file);
      m_charset = Ensure.notNull(charset);
      m_workspaceFile = new FinalValue<>();
    }

    /**
     * @return The file encoding
     */
    public Charset charset() {
      return m_charset;
    }

    /**
     * @return The absolute path to the file on the filesystem
     */
    public Path path() {
      return m_file;
    }

    /**
     * @return Resolves the file in the Eclipse workspace. Returns an empty {@link Optional} if it could not be found
     *         (e.g. because the path is not part of the Eclipse workspace).
     */
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

    /**
     * @return The content of the file
     */
    public char[] content() {
      if (m_content == null) {
        try {
          m_content = Strings.fromFileAsChars(path(), charset());
        }
        catch (IOException e) {
          throw new SdkException("Unable to read content of file '{}'.", path(), e);
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
