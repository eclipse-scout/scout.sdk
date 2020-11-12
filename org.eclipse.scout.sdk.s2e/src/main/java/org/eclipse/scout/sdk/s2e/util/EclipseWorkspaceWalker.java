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

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.callInEclipseEnvironmentSync;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.toScoutProgress;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.scout.sdk.s2e.environment.AbstractJob;

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
    return executeQueryInWorkspace(query, monitor);
  }

  /**
   * Executes the query created by the given queryFactory in the current Eclipse workspace. The execution uses default
   * visit settings (see {@link EclipseWorkspaceWalker}).
   * 
   * @param queryFactory
   *          A factory to create queries requiring an {@link IEnvironment}. The {@link IEnvironment} is valid during
   *          the execution of the returned query only.
   * @param monitor
   *          The {@link IProgressMonitor} to use.
   * @return The {@link IFileQueryResult} of the {@link IFileQuery}.
   */
  public static IFileQueryResult executeQuerySync(BiFunction<IEnvironment, IProgress, IFileQuery> queryFactory, IProgressMonitor monitor) {
    return callInEclipseEnvironmentSync((e, p) -> executeQueryInWorkspace(queryFactory.apply(e, p), monitor), monitor);
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
    var result = new AtomicReference<IFileQueryResult>();
    return new AbstractJob(query.name()) {
      @Override
      protected void execute(IProgressMonitor monitor) {
        result.set(executeQueryInWorkspace(query, monitor));
      }
    }.scheduleWithFuture(0, TimeUnit.MILLISECONDS, result::get);
  }

  protected static IFileQuery executeQueryInWorkspace(IFileQuery query, IProgressMonitor monitor) {
    try {
      new EclipseWorkspaceWalker(query.name())
          .walk((file, progress) -> executeQueryInFile(query, file), monitor);
      return query;
    }
    catch (CoreException ex) {
      throw new SdkException(ex);
    }
  }

  protected static void executeQueryInFile(IFileQuery query, WorkspaceFile file) {
    var candidate = new FileQueryInput(file.path(), file.projectPath(), file::content);
    query.searchIn(candidate);
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
    var projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    var subMonitor = SubMonitor.convert(monitor, taskName(), projects.length * 2);
    for (var root : projects) {
      if (!root.isAccessible()) {
        continue;
      }
      Set<Path> outputLocations = emptySet();
      subMonitor.subTask(root.getName());
      if (isSkipOutputLocation()) {
        var jp = JavaCore.create(root);
        outputLocations = getOutputLocations(jp);
      }

      var projectPath = root.getLocation().toFile().toPath();
      if (Files.exists(projectPath)) {
        searchInFolder(visitor, projectPath, Charset.forName(root.getDefaultCharset()), outputLocations, subMonitor.newChild(1));
      }

      if (subMonitor.isCanceled()) {
        return;
      }
      subMonitor.worked(1);
    }
  }

  protected static Set<Path> getOutputLocations(IJavaProject jp) throws JavaModelException {
    if (!JdtUtils.exists(jp)) {
      return emptySet();
    }
    var projectDir = jp.getProject().getLocation().toOSString();
    var rawClasspath = jp.getRawClasspath();
    return Arrays.stream(rawClasspath)
        .map(IClasspathEntry::getOutputLocation)
        .filter(Objects::nonNull)
        .map(location -> location.removeFirstSegments(1).toOSString())
        .map(location -> Paths.get(projectDir, location))
        .collect(toSet());
  }

  protected void searchInFolder(BiConsumer<WorkspaceFile, IProgress> visitor, Path folder, Charset charset, Collection<Path> outputFolders, IProgressMonitor monitor) {
    try {
      Files.walkFileTree(folder,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
              if (monitor.isCanceled()) {
                return FileVisitResult.TERMINATE;
              }
              if (outputFolders.contains(dir)) {
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
                visitor.accept(new WorkspaceFile(file, folder, charset), toScoutProgress(monitor));
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

    var fileName = file.getFileName();
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
    var path = file.getFileName();
    if (path == null) {
      return false;
    }
    var fileName = path.toString().toLowerCase(Locale.US);
    return extensionsAccepted().stream().anyMatch(fileName::endsWith);
  }

  protected static boolean isHidden(Path path) {
    var fileName = path.getFileName();
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
      for (var e : extensions) {
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
    private final Path m_projectPath;
    private final Charset m_charset;
    private final FinalValue<List<IFile>> m_workspaceFiles; // loaded on request
    private CharSequence m_content; // loaded on request

    public WorkspaceFile(Path file, Path projectPath, Charset charset) {
      m_file = Ensure.notNull(file);
      m_projectPath = Ensure.notNull(projectPath);
      m_charset = Ensure.notNull(charset);
      m_workspaceFiles = new FinalValue<>();
    }

    /**
     * @return The file encoding
     */
    public Charset charset() {
      return m_charset;
    }

    /**
     * @return The absolute path to the file on the file-system
     */
    public Path path() {
      return m_file;
    }

    /**
     * @return The absolute path to the root of the module that contains the file.
     */
    public Path projectPath() {
      return m_projectPath;
    }

    /**
     * @return Resolves the {@link IFile IFiles} in the Eclipse workspace pointing to {@link #path()}.
     */
    public List<IFile> inWorkspace() {
      return m_workspaceFiles.computeIfAbsentAndGet(() -> resolveInWorkspace(path()));
    }

    protected static List<IFile> resolveInWorkspace(Path file) {
      return Arrays.stream(ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toUri()))
          .filter(IFile::exists)
          .collect(toUnmodifiableList());
    }

    /**
     * @return The content of the file
     */
    public CharSequence content() {
      if (m_content == null) {
        try {
          m_content = Strings.fromFile(path(), charset());
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

      var other = (WorkspaceFile) obj;
      return m_file.equals(other.m_file);
    }
  }
}
