/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.environment;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.currentWorkingCopyManager;
import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.runWithWorkingCopyManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.CompilationUnitInfoWithClasspath;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.s.environment.AbstractEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.environment.SdkFuture;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.model.ClasspathWithJdt;
import org.eclipse.scout.sdk.s2e.environment.model.JavaEnvironmentWithJdt;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link EclipseEnvironment}</h3>
 *
 * @since 7.0.0
 */
@SuppressWarnings("MethodMayBeStatic")
public class EclipseEnvironment extends AbstractEnvironment {

  private final Map<IJavaProject, JavaEnvironmentWithJdt> m_envs;

  protected EclipseEnvironment() {
    m_envs = new ConcurrentHashMap<>();
  }

  @Override
  protected StringBuilder runGenerator(ISourceGenerator<ISourceBuilder<?>> generator, IJavaEnvironment context, Path targetPath) {
    return doCreateResource(generator, jdtJavaProjectOf(context), targetPath, context);
  }

  @Override
  protected IFuture<Void> doWriteResource(CharSequence content, Path filePath, IProgress progress, boolean sync) {
    IResourceWriteOperation writeFile = new ResourceWriteOperation(pathToWorkspaceFile(filePath), content);
    return doRunResourceTask(writeFile, null, toScoutProgress(progress), sync);
  }

  @Override
  protected IFuture<IType> doWriteCompilationUnit(CharSequence source, CompilationUnitInfoWithClasspath cuInfo, IProgress progress, boolean sync) {
    var sourceFolder = cuInfo.classpathEntry();
    var packageFragmentRoot = ((ClasspathWithJdt) sourceFolder.unwrap()).getRoot();
    var writeIcu = new CompilationUnitWriteOperation(packageFragmentRoot, cuInfo.packageName(), cuInfo.fileName(), source);
    return doRunResourceTask(writeIcu, () -> registerCompilationUnit(writeIcu.getFormattedSource()/*use formatted source here*/, cuInfo), toScoutProgress(progress), sync);
  }

  protected IJavaProject jdtJavaProjectOf(IJavaEnvironment env) {
    return ((JavaEnvironmentWithJdt) env.unwrap()).javaProject();
  }

  protected StringBuilder doCreateResource(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath) {
    var javaProject = findJavaProject(filePath)
        .orElseThrow(() -> newFail("Cannot find a Java project for path '{}'.", filePath));
    return doCreateResource(generator, javaProject, filePath, toScoutJavaEnvironment(javaProject));
  }

  protected static StringBuilder doCreateResource(ISourceGenerator<ISourceBuilder<?>> generator, IJavaProject javaProject, Path targetPath, IJavaEnvironment je) {
    var ctx = createBuilderContextFor(javaProject, targetPath);
    var builder = MemorySourceBuilder.create(new JavaBuilderContext(ctx, je));
    Ensure.notNull(generator).generate(builder);
    return builder.source();
  }

  protected IFile pathToWorkspaceFile(Path filePath) {
    var uri = Ensure.notNull(filePath).toUri();
    return S2eUtils.findFileInWorkspace(uri)
        .orElseThrow(() -> newFail("Could not find of workspace files for URI '{}'.", uri));
  }

  protected static BuilderContext createBuilderContextFor(IJavaProject javaProject, Path targetPath) {
    return new BuilderContext(Util.getLineSeparator(null, javaProject), S2eUtils.propertyMap(javaProject, targetPath));
  }

  protected <T> IFuture<T> doRunResourceTask(IResourceWriteOperation task, Supplier<T> resultExtractor, EclipseProgress progress, boolean syncRun) {
    Ensure.notNull(task);
    Ensure.notNull(progress);

    ISchedulingRule rule = task.getAffectedResource();
    if (syncRun && isCurrentContextContaining(rule)) {
      return doRunSync(task, resultExtractor, progress);
    }

    return doRunAsync(task, rule, resultExtractor);
  }

  protected <T> IFuture<T> doRunSync(Consumer<? super EclipseProgress> task, Supplier<T> resultExtractor, EclipseProgress progress) {
    Throwable t = null;
    try {
      task.accept(progress);
    }
    catch (RuntimeException e) {
      t = e;
    }
    return SdkFuture.completed(resultExtractor, t);
  }

  protected <T> IFuture<T> doRunAsync(Consumer<? super EclipseProgress> operation, ISchedulingRule rule, Supplier<T> resultExtractor) {
    var workingCopyManager = currentWorkingCopyManager();
    var job = new AbstractJob(OperationJob.getJobName(operation)) {
      @Override
      protected void execute(IProgressMonitor monitor) {
        runWithWorkingCopyManager(() -> operation.accept(toScoutProgress(monitor)), workingCopyManager);
      }
    };
    job.setRule(rule);
    return job.scheduleWithFuture(0L, TimeUnit.MILLISECONDS, resultExtractor);
  }

  protected static boolean isCurrentContextContaining(ISchedulingRule rule) {
    if (rule == null) {
      return false;
    }

    var currentRule = Job.getJobManager().currentRule();
    if (currentRule == null) {
      return false;
    }
    return currentRule.contains(rule);
  }

  @Override
  public Optional<IJavaEnvironment> findJavaEnvironment(Path root) {
    return findJavaProject(root).map(this::toScoutJavaEnvironment);
  }

  @Override
  public Path rootOfJavaEnvironment(IJavaEnvironment environment) {
    var javaEnv = (JavaEnvironmentWithJdt) Ensure.notNull(environment).unwrap();
    return javaEnv.javaProject().getProject().getLocation().toFile().toPath();
  }

  public Optional<IJavaProject> findJavaProject(Path root) {
    if (root == null) {
      return Optional.empty();
    }

    try {
      var javaProjects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
      for (var jp : javaProjects) {
        var location = jp.getProject().getLocation();
        if (location != null && root.startsWith(location.toFile().toPath())) {
          return Optional.of(jp);
        }
      }
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
    return Optional.empty();
  }

  @Override
  public Stream<IType> findType(String fqn) {
    Ensure.notBlank(fqn);
    return JdtUtils.resolveJdtTypes(fqn).stream()
        .map(this::toScoutType);
  }

  @Override
  public void deleteIfExists(Path file) {
    IResource toDelete;
    if (Files.isRegularFile(file)) {
      toDelete = pathToWorkspaceFile(file);
    }
    else {
      var containers = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(file.toUri());
      if (containers.length < 1) {
        return;
      }
      toDelete = containers[0];
    }

    if (!toDelete.exists()) {
      return;
    }
    try {
      toDelete.delete(true, null);
    }
    catch (CoreException e) {
      throw new SdkException("Unable to delete '{}'.", file, e);
    }
  }

  protected JavaEnvironmentWithJdt getOrCreateEnv(IJavaProject jdtProject) {
    return m_envs.computeIfAbsent(jdtProject, this::createNewJavaEnvironmentFor);
  }

  protected JavaEnvironmentWithJdt createNewJavaEnvironmentFor(IJavaProject jdtProject) {
    return initNewJavaEnvironment(new JavaEnvironmentWithJdt(jdtProject));
  }

  @Override
  public void close() {
    super.close();
    var iterator = m_envs.values().iterator();
    while (iterator.hasNext()) {
      try {
        iterator.next().close();
      }
      catch (RuntimeException e) {
        SdkLog.warning("Unable to close java environment.", e);
      }
      iterator.remove();
    }
  }

  /**
   * Converts the specified {@link IJavaProject} to a {@link IJavaEnvironment} with the same classpath and source
   * folders. If for this {@link IJavaProject} already an {@link IJavaEnvironment} has been used, this cached instance
   * is returned.
   *
   * @param jdtProject
   *          The {@link IJavaProject} that should be converted. Must not be {@code null}.
   * @return The {@link IJavaEnvironment} for the specified {@link IJavaProject}.
   */
  public IJavaEnvironment toScoutJavaEnvironment(IJavaProject jdtProject) {
    return getOrCreateEnv(Ensure.notNull(jdtProject)).wrap();
  }

  /**
   * Converts the specified {@link org.eclipse.jdt.core.IType} to an {@link IType}. If the corresponding
   * {@link IJavaProject} has no {@link IJavaEnvironment} associated yet, a new one will be created. Otherwise the
   * existing {@link IJavaEnvironment} will be used.
   *
   * @param jdtType
   *          The JDT {@link org.eclipse.jdt.core.IType} to convert. May be {@code null}.
   * @return The matching {@link IType} or {@code null} if the JDT type is {@code null} or the type could not be found.
   */
  public IType toScoutType(org.eclipse.jdt.core.IType jdtType) {
    if (!JdtUtils.exists(jdtType)) {
      return null;
    }
    return toScoutType(jdtType, getOrCreateEnv(jdtType.getJavaProject()));
  }

  protected static IType toScoutType(org.eclipse.jdt.core.IType jdtType, JavaEnvironmentSpi env) {
    if (jdtType == null || env == null) {
      return null;
    }
    var typeSpi = env.findType(jdtType.getFullyQualifiedName());
    if (typeSpi == null) {
      return null;
    }
    return typeSpi.wrap();
  }

  /**
   * Converts the specified {@link IPackageFragmentRoot} to an {@link IClasspathEntry}. If the corresponding
   * {@link IJavaProject} has no {@link IJavaEnvironment} associated yet, a new one will be created. Otherwise the
   * existing {@link IJavaEnvironment} will be used.
   *
   * @param root
   *          The {@link IPackageFragmentRoot} to convert. May be {@code null}.
   * @return The matching {@link IClasspathEntry} or {@code null} if no matching entry could be found or the input is
   *         {@code null}.
   */
  public IClasspathEntry toScoutSourceFolder(IPackageFragmentRoot root) {
    if (!JdtUtils.exists(root)) {
      return null;
    }
    var classpath = getOrCreateEnv(root.getJavaProject()).getClasspathFor(root);
    if (classpath == null) {
      return null;
    }
    return classpath.wrap();
  }

  /**
   * Narrows the specified {@link IProgress} to an {@link EclipseProgress} throwing an {@link IllegalArgumentException}
   * if it is a non {@link EclipseProgress} instance.
   *
   * @param progress
   *          The {@link IProgress} to narrow or {@code null} if an empty {@link IProgress} should be returned.
   * @return The narrowed progress. Never returns {@code null}.
   * @see NullProgressMonitor
   * @see #toScoutProgress(IProgressMonitor)
   */
  public static EclipseProgress toScoutProgress(IProgress progress) {
    if (progress == null) {
      return toScoutProgress((IProgressMonitor) null);
    }
    if (progress instanceof EclipseProgress) {
      return (EclipseProgress) progress;
    }
    throw newFail("Not in an Eclipse context: {}", progress.getClass().getName(), new Exception("origin"));
  }

  /**
   * Converts the specified {@link IProgressMonitor} to an {@link IProgress}.
   *
   * @param m
   *          The {@link IProgressMonitor} to convert or {@code null} if an empty {@link IProgress} should be returned.
   * @return An {@link IProgress} wrapping the specified {@link IProgressMonitor}.
   * @see NullProgressMonitor
   * @see #toScoutProgress(IProgress)
   */
  public static EclipseProgress toScoutProgress(IProgressMonitor m) {
    return new EclipseProgress(m);
  }

  /**
   * Narrows the specified {@link IEnvironment} to an {@link EclipseEnvironment} throwing an
   * {@link IllegalArgumentException} if it is not an instance of {@link EclipseEnvironment}.
   *
   * @param env
   *          The {@link IEnvironment} to narrow.
   * @return The casted {@link EclipseEnvironment} or an {@link IllegalArgumentException} if it is no instance of
   *         {@link EclipseEnvironment}. Never returns {@code null}.
   */
  public static EclipseEnvironment narrow(IEnvironment env) {
    if (env instanceof EclipseEnvironment) {
      return (EclipseEnvironment) env;
    }
    throw newFail("Not an Eclipse environment: {}", env.getClass().getName(), new Exception("origin"));
  }

  /**
   * Converts the specified {@link IType} to an {@link org.eclipse.jdt.core.IType}.
   *
   * @param scoutType
   *          The {@link IType} to convert. Must not be {@code null}.
   * @return The corresponding {@link org.eclipse.jdt.core.IType} or {@code null} if it could not be found.
   */
  public static org.eclipse.jdt.core.IType toJdtType(IType scoutType) {
    var javaProject = ((JavaEnvironmentWithJdt) Ensure.notNull(scoutType).javaEnvironment().unwrap()).javaProject();
    try {
      return javaProject.findType(scoutType.name().replace(JavaTypes.C_DOLLAR, JavaTypes.C_DOT), (IProgressMonitor) null);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  @Override
  protected Collection<? extends JavaEnvironmentSpi> javaEnvironments() {
    return m_envs.values();
  }

  public static IFuture<Void> runInEclipseEnvironment(BiConsumer<? super EclipseEnvironment, ? super EclipseProgress> task) {
    return runInEclipseEnvironment(task, null);
  }

  public static IFuture<Void> runInEclipseEnvironment(BiConsumer<? super EclipseEnvironment, ? super EclipseProgress> task, ISchedulingRule rule) {
    return callInEclipseEnvironment((e, p) -> {
      task.accept(e, p);
      return null;
    }, rule, OperationJob.getJobName(task));
  }

  public static <T> IFuture<T> callInEclipseEnvironment(BiFunction<? super EclipseEnvironment, ? super EclipseProgress, T> task) {
    return callInEclipseEnvironment(task, null);
  }

  public static <T> T callInEclipseEnvironmentSync(BiFunction<? super EclipseEnvironment, ? super EclipseProgress, T> task, IProgressMonitor monitor) {
    Ensure.notNull(monitor);
    try (var eclipseEnvironment = new EclipseEnvironment()) {
      return task.apply(eclipseEnvironment, toScoutProgress(monitor));
    }
  }

  public static <T> IFuture<T> callInEclipseEnvironment(BiFunction<? super EclipseEnvironment, ? super EclipseProgress, T> task, ISchedulingRule rule) {
    return callInEclipseEnvironment(task, rule, OperationJob.getJobName(task));
  }

  public static <T> IFuture<T> callInEclipseEnvironment(BiFunction<? super EclipseEnvironment, ? super EclipseProgress, T> task, ISchedulingRule rule, String jobName) {
    var result = new AtomicReference<T>();
    var job = new OperationJob((env, progress) -> result.set(task.apply(env, progress)), jobName);
    job.setRule(rule);
    return job.scheduleWithFuture(0, TimeUnit.MILLISECONDS, result::get);
  }

  public static EclipseEnvironment createUnsafe(Consumer<EclipseEnvironment> registerCloseCallback) {
    var eclipseEnvironment = new EclipseEnvironment();
    Ensure.notNull(registerCloseCallback).accept(eclipseEnvironment);
    return eclipseEnvironment;
  }
}
