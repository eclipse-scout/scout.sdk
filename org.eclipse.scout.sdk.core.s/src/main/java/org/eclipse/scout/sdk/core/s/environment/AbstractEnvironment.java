/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.environment;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.model.CompilationUnitInfo;
import org.eclipse.scout.sdk.core.java.model.CompilationUnitInfoWithClasspath;
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.java.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public abstract class AbstractEnvironment implements IEnvironment {

  private final Map<CompilationUnitInfo, CharSequence> m_createdCompilationUnits = new ConcurrentHashMap<>();
  private final Map<IFuture<?>, Boolean> m_uncompletedFutures = new ConcurrentHashMap<>();

  @Override
  public IType writeCompilationUnit(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder) {
    return writeCompilationUnit(generator, targetFolder, null);
  }

  @Override
  public IType writeCompilationUnit(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder, IProgress progress) {
    return writeCompilationUnitGenerator(generator, targetFolder, progress, true).result();
  }

  @Override
  public IFuture<IType> writeCompilationUnitAsync(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder, IProgress progress) {
    return writeCompilationUnitGenerator(generator, targetFolder, progress, false);
  }

  public StringBuilder createResource(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath) {
    var context = findJavaEnvironment(filePath).orElseThrow(() -> newFail("Cannot find Java environment for path '{}'.", filePath));
    return runGenerator(generator, context, filePath);
  }

  @Override
  public IFuture<IType> writeCompilationUnitAsync(CharSequence newSource, ICompilationUnit existingCompilationUnit, IProgress progress) {
    return overwriteExistingCu(newSource, existingCompilationUnit, progress, false);
  }

  @Override
  public IType writeCompilationUnit(CharSequence newSource, ICompilationUnit existingCompilationUnit, IProgress progress) {
    return overwriteExistingCu(newSource, existingCompilationUnit, progress, true).result();
  }

  protected IFuture<IType> overwriteExistingCu(CharSequence newSource, ICompilationUnit existingCompilationUnit, IProgress progress, boolean sync) {
    var sourceFolder = existingCompilationUnit.containingClasspathFolder().orElseThrow(() -> newFail("Compilation unit '{}' cannot be updated because the containing source folder could not be computed.", existingCompilationUnit));
    return writeCuWithExistingSource(newSource, sourceFolder, existingCompilationUnit.path(), progress, sync);
  }

  @Override
  public IFuture<IType> writeCompilationUnitAsync(CharSequence source, IClasspathEntry targetSourceFolder, Path sourceFolderRelPath, IProgress progress) {
    return writeCuWithExistingSource(source, targetSourceFolder, sourceFolderRelPath, progress, false);
  }

  @Override
  public IType writeCompilationUnit(CharSequence source, IClasspathEntry targetSourceFolder, Path sourceFolderRelPath, IProgress progress) {
    return writeCuWithExistingSource(source, targetSourceFolder, sourceFolderRelPath, progress, true).result();
  }

  protected IFuture<IType> writeCuWithExistingSource(CharSequence source, IClasspathEntry targetSourceFolder, Path sourceFolderRelPath, IProgress progress, boolean sync) {
    var cuInfo = new CompilationUnitInfoWithClasspath(targetSourceFolder, sourceFolderRelPath);
    return writeCompilationUnit(source, cuInfo, progress, sync);
  }

  @Override
  public void writeResource(CharSequence content, Path filePath, IProgress progress) {
    writeResource(content, filePath, progress, true).awaitDoneThrowingOnErrorOrCancel();
  }

  @Override
  public void writeResource(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath, IProgress progress) {
    writeResource(createResource(generator, filePath), filePath, progress);
  }

  @Override
  public IFuture<Void> writeResourceAsync(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath, IProgress progress) {
    return writeResourceAsync(createResource(generator, filePath), filePath, progress);
  }

  @Override
  public IFuture<Void> writeResourceAsync(CharSequence content, Path filePath, IProgress progress) {
    return writeResource(content, filePath, progress, false);
  }

  protected IFuture<Void> writeResource(CharSequence content, Path filePath, IProgress progress, boolean sync) {
    return handleUncompletedFuture(doWriteResource(content, filePath, progress, sync));
  }

  protected IFuture<IType> writeCompilationUnitGenerator(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder, IProgress progress, boolean sync) {
    Ensure.isTrue(Ensure.notNull(targetFolder).isSourceFolder(), "{} is no source folder. It is only allowed to generate new source into source folders.", targetFolder);
    var info = new CompilationUnitInfoWithClasspath(targetFolder, generator);
    var code = runGenerator(generator, targetFolder.javaEnvironment(), info.targetFile());
    return writeCompilationUnit(code, info, progress, sync);
  }

  protected IFuture<IType> writeCompilationUnit(CharSequence source, CompilationUnitInfoWithClasspath cuInfo, IProgress progress, boolean sync) {
    return handleUncompletedFuture(doWriteCompilationUnit(source, cuInfo, progress, sync));
  }

  protected <T> IFuture<T> handleUncompletedFuture(IFuture<T> future) {
    m_uncompletedFutures.put(future, Boolean.FALSE);
    future.whenComplete((f, e) -> m_uncompletedFutures.remove(future));
    return future;
  }

  protected IType registerCompilationUnit(CharSequence code, CompilationUnitInfoWithClasspath cuInfo) {
    // remember for future Java environments
    m_createdCompilationUnits.put(cuInfo, code);

    // register in existing environments
    registerInJavaEnvironments(code, cuInfo);

    return cuInfo
        .classpathEntry()
        .javaEnvironment()
        .findType(cuInfo.mainTypeFullyQualifiedName())
        .orElse(null);
  }

  protected void registerInJavaEnvironments(CharSequence code, CompilationUnitInfo cuInfo) {
    var cuPath = cuInfo.targetFile();
    javaEnvironments().stream()
        .map(JavaEnvironmentSpi::wrap)
        .filter(je -> je.classpathContains(cuPath))
        .filter(je -> je.registerCompilationUnitOverride(code, cuInfo))
        .forEach(IJavaEnvironment::reload);
  }

  protected <T extends JavaEnvironmentSpi> T initNewJavaEnvironment(T javaEnvironment) {
    if (m_createdCompilationUnits.isEmpty()) {
      return javaEnvironment;
    }

    var env = javaEnvironment.wrap();
    m_createdCompilationUnits.entrySet().stream()
        .filter(e -> env.classpathContains(e.getKey().targetFile()))
        .forEach(e -> env.registerCompilationUnitOverride(e.getValue(), e.getKey()));
    // no need to reload as it must be a new environment
    return javaEnvironment;
  }

  @Override
  public StringBuilder executeGenerator(ISourceGenerator<ISourceBuilder<?>> generator, IClasspathEntry targetFolder) {
    return runGenerator(generator, targetFolder.javaEnvironment(), targetFolder.path());
  }

  @Override
  public void close() {
    SdkFuture.awaitAllLoggingOnError(m_uncompletedFutures.keySet());
    m_uncompletedFutures.clear();
    m_createdCompilationUnits.clear();
  }

  protected abstract Collection<? extends JavaEnvironmentSpi> javaEnvironments();

  protected abstract StringBuilder runGenerator(ISourceGenerator<ISourceBuilder<?>> generator, IJavaEnvironment context, Path filePath);

  protected abstract IFuture<Void> doWriteResource(CharSequence content, Path filePath, IProgress progress, boolean sync);

  protected abstract IFuture<IType> doWriteCompilationUnit(CharSequence source, CompilationUnitInfoWithClasspath cuInfo, IProgress progress, boolean sync);
}
