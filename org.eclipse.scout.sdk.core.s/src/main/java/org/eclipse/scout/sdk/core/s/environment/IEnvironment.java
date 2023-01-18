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

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.ISourceFolders;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.java.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutSourceFolders;

/**
 * <h3>{@link IEnvironment}</h3>
 * <p>
 * Represents an environment that is required to run Scout operations.
 *
 * @since 7.0.0
 */
public interface IEnvironment extends AutoCloseable {

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
   * @see #rootOfJavaEnvironment(IJavaEnvironment)
   */
  Optional<IJavaEnvironment> findJavaEnvironment(Path root);

  /**
   * Gets the root directory of the module the given {@link IJavaEnvironment} was created on.
   * 
   * @param environment
   *          The {@link IJavaEnvironment} for which the module root path should be returned. Must not be {@code null}.
   * @return The root path of the module the given {@link IJavaEnvironment} was created on.
   * @throws IllegalArgumentException
   *           if the environment is {@code null}.
   * @see #findJavaEnvironment(Path)
   */
  Path rootOfJavaEnvironment(IJavaEnvironment environment);

  /**
   * Tries to find all types with given fully qualified name inside the whole {@link IEnvironment}. This may include
   * matches in {@link IJavaEnvironment}s not yet used.
   * 
   * @param fqn
   *          The fully qualified name to find. Must not be empty.
   * @return All {@link IType}s found. The different instances may belong to different {@link IJavaEnvironment}s.
   */
  Stream<IType> findType(String fqn);

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
   *          The {@link IProgress} monitor. Typically, a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(int, CharSequence, Object...)} on the
   *          argument. May be {@code null} if no progress indication is required.
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
   * <li>The async write operation runs in the same transaction as the calling thread. Therefore at some point the
   * calling thread must wait for the async write operation to complete to ensure its result is registered in the
   * transaction before committing it!</li>
   * <li>The resulting {@link IFuture} can be used to wait for the write operation to complete.<br>
   * </ul>
   *
   * @param generator
   *          The {@link ICompilationUnitGenerator} to execute. Must not be {@code null}.
   * @param targetFolder
   *          The source folder in which the new Java file should be created. Must not be {@code null}.
   *          {@link IClasspathEntry#isSourceFolder()} must be {@code true}.
   * @param progress
   *          The {@link IProgress} monitor. Typically, a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(int, CharSequence, Object...)} on the
   *          argument. May be {@code null} if no progress indication is required.
   * @return An {@link IFuture} that can be used to access the created {@link IType} within the specified
   *         {@link ICompilationUnitGenerator} that has the same {@link IJavaElement#elementName()} as the generator. If
   *         there was an exception writing the Java file, this exception will be thrown on result access of this
   *         {@link IFuture}.
   * @throws RuntimeException
   *           if there is a problem executing the {@link ICompilationUnitGenerator}.
   */
  IFuture<IType> writeCompilationUnitAsync(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder, IProgress progress);

  /**
   * Overwrites the {@link ICompilationUnit} given with the new source given.
   * 
   * @param newSource
   *          The new source. Must not be {@code null}.
   * @param existingCompilationUnit
   *          The existing {@link ICompilationUnit} that should be overwritten. It must not be {@code null} and must be
   *          a non {@link ICompilationUnit#isSynthetic() synthetic} compilation unit stored in a source folder.
   * @param progress
   *          The {@link IProgress} monitor. Typically, a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(int, CharSequence, Object...)} on the
   *          argument. May be {@code null} if no progress indication is required.
   * @return The updated main {@link IType} within the specified {@link ICompilationUnit}.
   * @see ICompilationUnit#containingClasspathFolder()
   */
  IType writeCompilationUnit(CharSequence newSource, ICompilationUnit existingCompilationUnit, IProgress progress);

  /**
   * Asynchronously overwrites the {@link ICompilationUnit} given with the new source given.
   * <p>
   * <b>Notes:</b>
   * <ul>
   * <li>The async write operation runs in the same transaction as the calling thread. Therefore at some point the
   * calling thread must wait for the async write operation to complete to ensure its result is registered in the
   * transaction before committing it!</li>
   * <li>The resulting {@link IFuture} can be used to wait for the write operation to complete.<br>
   * </ul>
   * 
   * @param newSource
   *          The new source. Must not be {@code null}.
   * @param existingCompilationUnit
   *          The existing {@link ICompilationUnit} that should be overwritten. It must not be {@code null} and must be
   *          a non {@link ICompilationUnit#isSynthetic() synthetic} compilation unit stored in a source folder.
   * @param progress
   *          The {@link IProgress} monitor. Typically, a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(int, CharSequence, Object...)} on the
   *          argument. May be {@code null} if no progress indication is required.
   * @return The updated main {@link IType} within the specified {@link ICompilationUnit}.
   * @see ICompilationUnit#containingClasspathFolder()
   */
  IFuture<IType> writeCompilationUnitAsync(CharSequence newSource, ICompilationUnit existingCompilationUnit, IProgress progress);

  /**
   * Writes the source given to a Java file.
   * 
   * @param source
   *          The new source. Must not be {@code null}.
   * @param targetSourceFolder
   *          The absolute source folder in which the new Java file should be created. Must not be {@code null}.
   *          {@link IClasspathEntry#isSourceFolder()} must be {@code true}. E.g.
   *          {@code /dev/myProject/myModule/src/main/java}.
   * @param compilationUnitPath
   *          The file {@link Path} relative to the source folder in which the compilation unit should be created. e.g.
   *          {@code org/eclipse/scout/MyClass.java}.
   * @param progress
   *          The {@link IProgress} monitor. Typically, a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(int, CharSequence, Object...)} on the
   *          argument. May be {@code null} if no progress indication is required.
   * @return The created main {@link IType} within the specified file.
   */
  IType writeCompilationUnit(CharSequence source, IClasspathEntry targetSourceFolder, Path compilationUnitPath, IProgress progress);

  /**
   * Asynchronously writes the source given to a Java file.
   * <p>
   * <b>Notes:</b>
   * <ul>
   * <li>The async write operation runs in the same transaction as the calling thread. Therefore at some point the
   * calling thread must wait for the async write operation to complete to ensure its result is registered in the
   * transaction before committing it!</li>
   * <li>The resulting {@link IFuture} can be used to wait for the write operation to complete.<br>
   * </ul>
   *
   * @param source
   *          The new source. Must not be {@code null}.
   * @param targetSourceFolder
   *          The absolute source folder in which the new Java file should be created. Must not be {@code null}.
   *          {@link IClasspathEntry#isSourceFolder()} must be {@code true}. E.g.
   *          {@code /dev/myProject/myModule/src/main/java}.
   * @param compilationUnitPath
   *          The file {@link Path} relative to the source folder in which the compilation unit should be created. e.g.
   *          {@code org/eclipse/scout/MyClass.java}.
   * @param progress
   *          The {@link IProgress} monitor. Typically, a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(int, CharSequence, Object...)} on the
   *          argument. May be {@code null} if no progress indication is required.
   * @return The created main {@link IType} within the specified file.
   */
  IFuture<IType> writeCompilationUnitAsync(CharSequence source, IClasspathEntry targetSourceFolder, Path compilationUnitPath, IProgress progress);

  /**
   * Executes the specified {@link ISourceGenerator} in the context of the specified {@link IJavaEnvironment}. The
   * result of the {@link ISourceGenerator} is created in memory.
   *
   * @param generator
   *          The {@link ISourceGenerator} to execute. Must not be {@code null}.
   * @param targetFolder
   *          The {@link IClasspathEntry} where the specified {@link ISourceGenerator} will be executed. The
   *          {@link IJavaEnvironment} of the specified {@link IClasspathEntry} is used to resolve imports (decide which
   *          imports are required). Must not be {@code null}.
   * @return A {@link StringBuilder} containing the result of the specified {@link ISourceGenerator}.
   * @see ISourceGenerator#generate(ISourceBuilder)
   * @see MemorySourceBuilder
   * @see IJavaBuilderContext#environment()
   * @see IJavaElementGenerator#toJavaSource(IJavaEnvironment)
   */
  StringBuilder executeGenerator(ISourceGenerator<ISourceBuilder<?>> generator, IClasspathEntry targetFolder);

  /**
   * Writes the specified content to the specified file path.
   *
   * @param content
   *          The new content of the file. Must not be {@code null}.
   * @param filePath
   *          The absolute path to the file to write. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Typically, a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(int, CharSequence, Object...)} on the
   *          argument. May be {@code null} if no progress indication is required.
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
   *          The {@link IProgress} monitor. Typically, a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(int, CharSequence, Object...)} on the
   *          argument. May be {@code null} if no progress indication is required.
   * @throws RuntimeException
   *           if there is a problem writing the {@link ISourceGenerator}.
   */
  void writeResource(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath, IProgress progress);

  /**
   * Executes the specified {@link ISourceGenerator} in the calling thread and asynchronously writes the result to the
   * specified file path.
   * <p>
   * <b>Notes:</b>
   * <ul>
   * <li>The execution of the {@link ISourceGenerator} is done in the calling thread and only the write operation is
   * executed asynchronously!</li>
   * <li>The async write operation runs in the same transaction as the calling thread. Therefore at some point the
   * calling thread must wait for the async write operation to complete to ensure its result is registered in the
   * transaction before committing it!</li>
   * <li>The resulting {@link IFuture} can be used to wait for the write operation to complete.<br>
   * </ul>
   *
   * @param generator
   *          The {@link ISourceGenerator} to use to create the content that should be written to the specified file.
   *          The generator is executed in the calling thread. Must not be {@code null}.
   * @param filePath
   *          The absolute path to the file to write. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Typically, a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(int, CharSequence, Object...)} on the
   *          argument. May be {@code null} if no progress indication is required.
   * @return An {@link IFuture} that can be used to wait until the file has been written. If there was an exception
   *         writing the resource, this exception will be thrown on result access of this {@link IFuture}.
   * @throws RuntimeException
   *           if there is a problem executing the {@link ISourceGenerator}.
   */
  IFuture<Void> writeResourceAsync(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath, IProgress progress);

  /**
   * Asynchronously writes the specified content to the specified file path.
   * <p>
   * <b>Notes:</b>
   * <ul>
   * <li>The execution of the {@link ISourceGenerator} is done in the calling thread and only the write operation is
   * executed asynchronously!</li>
   * <li>The async write operation runs in the same transaction as the calling thread. Therefore at some point the
   * calling thread must wait for the async write operation to complete to ensure its result is registered in the
   * transaction before committing it!</li>
   * <li>The resulting {@link IFuture} can be used to wait for the write operation to complete.<br>
   * </ul>
   *
   * @param content
   *          The new content of the file. Must not be {@code null}.
   * @param filePath
   *          The absolute path to the file to write. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Typically, a {@link IProgress#newChild(int)} should be passed to this
   *          method. The write operation will call {@link IProgress#init(int, CharSequence, Object...)} on the
   *          argument. May be {@code null} if no progress indication is required.
   * @return An {@link IFuture} that can be used to wait until the file has been written. If there was an exception
   *         writing the resource, this exception will be thrown on result access of this {@link IFuture}.
   */
  IFuture<Void> writeResourceAsync(CharSequence content, Path filePath, IProgress progress);

  /**
   * Deletes the directory or file at the {@link Path} given.
   * <p>
   * If the directory is not empty, all content is deleted as well.
   * 
   * @param file
   *          The {@link Path} to delete. Must not be {@code null}.
   */
  void deleteIfExists(Path file);
}
