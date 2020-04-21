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
package org.eclipse.scout.sdk.s2e.environment;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.currentWorkingCopyManager;
import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.runWithWorkingCopyManager;

import java.net.URI;
import java.nio.file.Path;
import java.util.Iterator;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
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
import org.eclipse.scout.sdk.core.generator.compilationunit.CompilationUnitPath;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
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
public class EclipseEnvironment implements IEnvironment, AutoCloseable {

  private final Map<IJavaProject, JavaEnvironmentWithJdt> m_envs;

  protected EclipseEnvironment() {
    m_envs = new ConcurrentHashMap<>();
  }

  @Override
  public IType writeCompilationUnit(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder) {
    return writeCompilationUnit(generator, targetFolder, null);
  }

  @Override
  public IType writeCompilationUnit(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder, IProgress p) {
    return doWriteCompilationUnit(generator, targetFolder, toScoutProgress(p), true).result();
  }

  @Override
  public IFuture<IType> writeCompilationUnitAsync(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder, IProgress p) {
    return doWriteCompilationUnit(generator, targetFolder, toScoutProgress(p), false);
  }

  @Override
  public StringBuilder createResource(ISourceGenerator<ISourceBuilder<?>> generator, IClasspathEntry targetFolder) {
    IJavaEnvironment context = targetFolder.javaEnvironment();
    return doCreateResource(generator, jdtJavaProjectOf(context), targetFolder.path(), context);
  }

  protected IJavaProject jdtJavaProjectOf(IJavaEnvironment env) {
    return ((JavaEnvironmentWithJdt) env.unwrap()).javaProject();
  }

  protected StringBuilder doCreateResource(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath) {
    IJavaProject javaProject = findJavaProject(filePath)
        .orElseThrow(() -> newFail("Cannot find a Java project for path '{}'.", filePath));
    return doCreateResource(generator, javaProject, filePath, toScoutJavaEnvironment(javaProject));
  }

  protected static StringBuilder doCreateResource(ISourceGenerator<ISourceBuilder<?>> generator, IJavaProject javaProject, Path targetPath, IJavaEnvironment je) {
    BuilderContext ctx = createBuilderContextFor(javaProject, targetPath);
    MemorySourceBuilder builder = new MemorySourceBuilder(new JavaBuilderContext(ctx, je));
    Ensure.notNull(generator).generate(builder);
    return builder.source();
  }

  @Override
  public void writeResource(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath, IProgress progress) {
    writeResource(doCreateResource(generator, filePath), filePath, progress);
  }

  @Override
  public void writeResource(CharSequence content, Path filePath, IProgress progress) {
    doWriteResource(filePath, content, progress, true).awaitDoneThrowingOnErrorOrCancel();
  }

  @Override
  public IFuture<Void> writeResourceAsync(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath, IProgress progress) {
    return writeResourceAsync(doCreateResource(generator, filePath), filePath, progress);
  }

  @Override
  public IFuture<Void> writeResourceAsync(CharSequence content, Path filePath, IProgress progress) {
    return doWriteResource(filePath, content, progress, false);
  }

  protected IFuture<Void> doWriteResource(Path filePath, CharSequence content, IProgress progress, boolean syncRun) {
    return doWriteResource(pathToWorkspaceFile(filePath), content, toScoutProgress(progress), syncRun);
  }

  protected IFile pathToWorkspaceFile(Path filePath) {
    URI uri = Ensure.notNull(filePath).toUri();
    return S2eUtils.findFileInWorkspace(uri)
        .orElseThrow(() -> newFail("Could not find of workspace files for URI '{}'.", uri));
  }

  protected static BuilderContext createBuilderContextFor(IJavaProject javaProject, Path targetPath) {
    return new BuilderContext(Util.getLineSeparator(null, javaProject), S2eUtils.propertyMap(javaProject, targetPath));
  }

  protected IFuture<IType> doWriteCompilationUnit(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder, EclipseProgress progress, boolean syncRun) {
    Ensure.isTrue(Ensure.notNull(targetFolder).isSourceFolder(), "{} is no source folder. It is only allowed to generate new source into source folders.", targetFolder);

    // generate new code
    CompilationUnitPath path = new CompilationUnitPath(generator, targetFolder);
    IJavaEnvironment env = targetFolder.javaEnvironment();
    StringBuilder code = doCreateResource(generator, jdtJavaProjectOf(env), path.targetFile(), env);

    // write to disk
    IPackageFragmentRoot sourceFolder = ((ClasspathWithJdt) targetFolder.unwrap()).getRoot();
    String packageName = generator.packageName().orElse(null);
    String javaFileName = generator.fileName().get();
    CompilationUnitWriteOperation writeIcu = new CompilationUnitWriteOperation(sourceFolder, packageName, javaFileName, code);
    return doRunResourceTask(writeIcu, () -> {
      ICompilationUnit compilationUnit = writeIcu.getCreatedCompilationUnit();
      if (compilationUnit == null) {
        return null; // may happen if the asynchronous write operation is canceled
      }

      String formattedSource;
      try {
        formattedSource = compilationUnit.getSource();
      }
      catch (JavaModelException e) {
        throw new SdkException(e);
      }

      // return primary type
      org.eclipse.jdt.core.IType jdtType = compilationUnit.getType(generator.mainType().get().elementName().get());

      IJavaEnvironment javaEnvironment = targetFolder.javaEnvironment();
      boolean reloadRequired = javaEnvironment.registerCompilationUnitOverride(packageName, javaFileName, formattedSource);
      if (reloadRequired) {
        javaEnvironment.reload();
      }
      return toScoutType(jdtType, javaEnvironment.unwrap());
    }, progress, syncRun);
  }

  /**
   * Writes the specified content to the specified {@link IFile}.
   *
   * @param content
   *          The new content of the file. Must not be {@code null}.
   * @param file
   *          The {@link IFile} to write the content to. Must not be {@code null}. If the {@link IFile} does not exist,
   *          it is created.
   * @param progress
   *          The {@link EclipseProgress} monitor. Typically a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(String, int)} on the argument. Must not be
   *          {@code null}.
   */
  public void writeResource(CharSequence content, IFile file, EclipseProgress progress) {
    doWriteResource(file, content, progress, true).awaitDoneThrowingOnErrorOrCancel();
  }

  /**
   * Asynchronously writes the specified content to the specified {@link IFile}.
   * <p>
   * <b>Important:</b> It must be ensured that for async write operations the corresponding {@link IEnvironment} has not
   * yet been closed. Therefore at some point it must be waited for the {@link IFuture futures} to complete before the
   * {@link IEnvironment} will be closed.
   *
   * @param content
   *          The new content of the file. Must not be {@code null}.
   * @param file
   *          The {@link IFile} to write the content to. Must not be {@code null}. If the {@link IFile} does not exist,
   *          it is created.
   * @param progress
   *          The {@link EclipseProgress} monitor. Typically a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(String, int)} on the argument. Must not be
   *          {@code null}.
   * @return An {@link IFuture} that can be used to wait until the file has been written. If there was an exception
   *         writing the resource, this exception will be thrown on result access of this {@link IFuture}.
   */
  public IFuture<Void> writeResourceAsync(CharSequence content, IFile file, EclipseProgress progress) {
    return doWriteResource(file, content, progress, false);
  }

  protected IFuture<Void> doWriteResource(IFile file, CharSequence content, EclipseProgress progress, boolean syncRun) {
    IResourceWriteOperation writeFile = new ResourceWriteOperation(file, content);
    return doRunResourceTask(writeFile, null, progress, syncRun);
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
    IWorkingCopyManager workingCopyManager = currentWorkingCopyManager();
    AbstractJob job = new AbstractJob(OperationJob.getJobName(operation)) {
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

    ISchedulingRule currentRule = Job.getJobManager().currentRule();
    if (currentRule == null) {
      return false;
    }
    return currentRule.contains(rule);
  }

  @Override
  public Optional<IJavaEnvironment> findJavaEnvironment(Path root) {
    return findJavaProject(root).map(this::toScoutJavaEnvironment);
  }

  public Optional<IJavaProject> findJavaProject(Path root) {
    if (root == null) {
      return Optional.empty();
    }

    try {
      IJavaProject[] javaProjects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
      for (IJavaProject jp : javaProjects) {
        IPath location = jp.getProject().getLocation();
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

  protected static boolean hasJavaNature(IProject p) {
    if (p == null || !p.isOpen()) {
      return false;
    }
    try {
      return p.hasNature(JavaCore.NATURE_ID);
    }
    catch (CoreException e) {
      SdkLog.warning("Unable to check for natures of project {}.", p, e);
      return false;
    }
  }

  protected JavaEnvironmentWithJdt getOrCreateEnv(IJavaProject jdtProject) {
    return m_envs.computeIfAbsent(jdtProject, this::createNewJavaEnvironmentFor);
  }

  protected JavaEnvironmentWithJdt createNewJavaEnvironmentFor(IJavaProject jdtProject) {
    return new JavaEnvironmentWithJdt(jdtProject);
  }

  @Override
  public void close() {
    Iterator<JavaEnvironmentWithJdt> iterator = m_envs.values().iterator();
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
    TypeSpi typeSpi = env.findType(jdtType.getFullyQualifiedName());
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
    ClasspathSpi classpath = getOrCreateEnv(root.getJavaProject()).getClasspathFor(root);
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
    IJavaProject javaProject = ((JavaEnvironmentWithJdt) Ensure.notNull(scoutType).javaEnvironment().unwrap()).javaProject();
    try {
      return javaProject.findType(scoutType.name().replace(JavaTypes.C_DOLLAR, JavaTypes.C_DOT), (IProgressMonitor) null);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
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
    try (EclipseEnvironment eclipseEnvironment = new EclipseEnvironment()) {
      return task.apply(eclipseEnvironment, toScoutProgress(monitor));
    }
  }

  public static <T> IFuture<T> callInEclipseEnvironment(BiFunction<? super EclipseEnvironment, ? super EclipseProgress, T> task, ISchedulingRule rule) {
    return callInEclipseEnvironment(task, rule, OperationJob.getJobName(task));
  }

  public static <T> IFuture<T> callInEclipseEnvironment(BiFunction<? super EclipseEnvironment, ? super EclipseProgress, T> task, ISchedulingRule rule, String jobName) {
    AtomicReference<T> result = new AtomicReference<>();
    OperationJob job = new OperationJob((env, progress) -> result.set(task.apply(env, progress)), jobName);
    job.setRule(rule);
    return job.scheduleWithFuture(0, TimeUnit.MILLISECONDS, result::get);
  }

  public static EclipseEnvironment createUnsafe(Consumer<EclipseEnvironment> registerCloseCallback) {
    EclipseEnvironment eclipseEnvironment = new EclipseEnvironment();
    Ensure.notNull(registerCloseCallback).accept(eclipseEnvironment);
    return eclipseEnvironment;
  }
}
