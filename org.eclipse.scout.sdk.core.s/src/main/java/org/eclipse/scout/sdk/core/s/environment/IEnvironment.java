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
package org.eclipse.scout.sdk.core.s.environment;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutSourceFolders;

/**
 * <h3>{@link IEnvironment}</h3>
 * <p>
 * Represents an environment that is required to run Scout operations.
 *
 * @since 7.0.0
 */
public interface IEnvironment {

  /**
   * Tries to find an {@link IJavaEnvironment} at the location of the specified root {@link Path}.
   * <p>
   * A {@link IJavaEnvironment} exists at the specified location if a module defining a classpath can be found. This can
   * be e.g. if the specified {@link Path} points to a Maven module defining a classpath and/or containing source
   * folders like the ones defined in {@link ISourceFolders} and {@link IScoutSourceFolders}.
   *
   * @param root
   *          The absolute {@link Path} pointing to the root of the module defining the {@link IJavaEnvironment}.
   * @return An {@link Optional} with the {@link IJavaEnvironment} rooting at the specified path or an empty
   *         {@link Optional} if no {@link IJavaEnvironment} exists at this location.
   */
  Optional<IJavaEnvironment> findJavaEnvironment(Path root);

  /**
   * Executes the specified {@link ICompilationUnitGenerator} in the {@link IJavaEnvironment} of the specified
   * {@link IClasspathEntry} and writes the created source into a Java file in a package as specified by
   * {@link ICompilationUnitGenerator#packageName()} within the specified {@link IClasspathEntry}.
   *
   * @param generator
   *          The {@link ICompilationUnitGenerator} to execute. Must not be {@code null}.
   * @param targetFolder
   *          The source folder in which the new Java file should be created. Must not be {@code null}.
   *          {@link IClasspathEntry#isSourceFolder()} must be {@code true}.
   * @return The created {@link IType} within the specified {@link ICompilationUnitGenerator} that has the same
   *         {@link IJavaElement#elementName()} as the generator.
   * @throws RuntimeException
   *           if there is a problem writing the {@link ICompilationUnitGenerator}.
   */
  IType writeCompilationUnit(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder);

  /**
   * Executes the specified {@link ICompilationUnitGenerator} in the {@link IJavaEnvironment} of the specified
   * {@link IClasspathEntry} and writes the created source into a Java file in a package as specified by
   * {@link ICompilationUnitGenerator#packageName()} within the specified {@link IClasspathEntry}.
   *
   * @param generator
   *          The {@link ICompilationUnitGenerator} to execute. Must not be {@code null}.
   * @param targetFolder
   *          The source folder in which the new Java file should be created. Must not be {@code null}.
   *          {@link IClasspathEntry#isSourceFolder()} must be {@code true}.
   * @param progress
   *          The {@link IProgress} monitor. Typically a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(String, int)} on the argument. May be
   *          {@code null} if no progress indication is required.
   * @return The created {@link IType} within the specified {@link ICompilationUnitGenerator} that has the same
   *         {@link IJavaElement#elementName()} as the generator.
   * @throws RuntimeException
   *           if there is a problem writing the {@link ICompilationUnitGenerator}.
   */
  IType writeCompilationUnit(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder, IProgress progress);

  /**
   * Executes the specified {@link ICompilationUnitGenerator} using the {@link IJavaEnvironment} of the specified
   * {@link IClasspathEntry} as context. Afterwards the result is written asynchronously to a Java file in a package as
   * specified by {@link ICompilationUnitGenerator#packageName()} within the specified {@link IClasspathEntry}.
   * <p>
   * <b>Notes:</b>
   * <ul>
   * <li>The execution of the {@link ICompilationUnitGenerator} is done in the calling thread and only the write
   * operation is executed asynchronously!</li>
   * <li>The resulting {@link IFuture} can be used to wait for the write operation to complete.<br>
   * <b>Important:</b> It must be ensured that for async write operations the corresponding {@link IEnvironment} has not
   * yet been closed. Therefore at some point it must be waited for the {@link IFuture futures} to complete before the
   * {@link IEnvironment} will be closed.</li>
   * </ul>
   *
   * @param generator
   *          The {@link ICompilationUnitGenerator} to execute. Must not be {@code null}.
   * @param targetFolder
   *          The source folder in which the new Java file should be created. Must not be {@code null}.
   *          {@link IClasspathEntry#isSourceFolder()} must be {@code true}.
   * @param progress
   *          The {@link IProgress} monitor. Typically a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(String, int)} on the argument. May be
   *          {@code null} if no progress indication is required.
   * @return An {@link IFuture} that can be used to access the created {@link IType} within the specified
   *         {@link ICompilationUnitGenerator} that has the same {@link IJavaElement#elementName()} as the generator. If
   *         there was an exception writing the Java file, this exception will be thrown on result access of this
   *         {@link IFuture}.
   * @throws RuntimeException
   *           if there is a problem executing the {@link ICompilationUnitGenerator}.
   */
  IFuture<IType> writeCompilationUnitAsync(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder, IProgress progress);

  /**
   * Executes the specified {@link ISourceGenerator} in the context of the specified {@link IJavaEnvironment}. The
   * result of the {@link ISourceGenerator} is created in memory.
   *
   * @param generator
   *          The {@link ISourceGenerator} to execute. Must not be {@code null}.
   * @param context
   *          The {@link IJavaEnvironment} to run the specified {@link ISourceGenerator} in. This
   *          {@link IJavaEnvironment} is used to resolve imports (decide which imports are required). Must not be
   *          {@code null}.
   * @return A {@link StringBuilder} containing the result of the specified {@link ISourceGenerator}.
   * @see ISourceGenerator#generate(ISourceBuilder)
   * @see MemorySourceBuilder
   * @see IJavaBuilderContext#environment()
   * @see IJavaElementGenerator#toJavaSource(IJavaEnvironment)
   */
  StringBuilder createResource(ISourceGenerator<ISourceBuilder<?>> generator, IJavaEnvironment context);

  /**
   * Writes the specified content to the specified file path.
   *
   * @param content
   *          The new content of the file. Must not be {@code null}.
   * @param filePath
   *          The absolute path to the file to write. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Typically a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(String, int)} on the argument. May be
   *          {@code null} if no progress indication is required.
   * @throws RuntimeException
   *           if there is a problem writing the resource.
   */
  void writeResource(CharSequence content, Path filePath, IProgress progress);

  /**
   * Executes the specified {@link ISourceGenerator} and writes the result to the specified file path.
   *
   * @param generator
   *          The {@link ISourceGenerator} to use to create the content that should be written to the specified file.
   *          Must not be {@code null}.
   * @param filePath
   *          The absolute path to the file to write. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Typically a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(String, int)} on the argument. May be
   *          {@code null} if no progress indication is required.
   * @throws RuntimeException
   *           if there is a problem writing the {@link ISourceGenerator}.
   */
  void writeResource(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath, IProgress progress);

  /**
   * Executes the specified {@link ISourceGenerator} in the calling thread and asynchronously writes the result to the
   * specified file path.
   * <p>
   * <b>Important:</b> It must be ensured that for async write operations the corresponding {@link IEnvironment} has not
   * yet been closed. Therefore at some point it must be waited for the {@link IFuture futures} to complete before the
   * {@link IEnvironment} will be closed.
   *
   * @param generator
   *          The {@link ISourceGenerator} to use to create the content that should be written to the specified file.
   *          The generator is executed in the calling thread. Must not be {@code null}.
   * @param filePath
   *          The absolute path to the file to write. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Typically a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(String, int)} on the argument. May be
   *          {@code null} if no progress indication is required.
   * @return An {@link IFuture} that can be used to wait until the file has been written. If there was an exception
   *         writing the resource, this exception will be thrown on result access of this {@link IFuture}.
   * @throws RuntimeException
   *           if there is a problem executing the {@link ISourceGenerator}.
   */
  IFuture<Void> writeResourceAsync(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath, IProgress progress);

  /**
   * Asynchronously writes the specified content to the specified file path.
   * <p>
   * <b>Important:</b> It must be ensured that for async write operations the corresponding {@link IEnvironment} has not
   * yet been closed. Therefore at some point it must be waited for the {@link IFuture futures} to complete before the
   * {@link IEnvironment} will be closed.
   *
   * @param content
   *          The new content of the file. Must not be {@code null}.
   * @param filePath
   *          The absolute path to the file to write. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Typically a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(String, int)} on the argument. May be
   *          {@code null} if no progress indication is required.
   * @return An {@link IFuture} that can be used to wait until the file has been written. If there was an exception
   *         writing the resource, this exception will be thrown on result access of this {@link IFuture}.
   */
  IFuture<Void> writeResourceAsync(CharSequence content, Path filePath, IProgress progress);
}