/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.internal;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.CompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.java.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.java.model.api.IImport;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IPackage;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.query.InnerTypeQuery;
import org.eclipse.scout.sdk.core.java.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.java.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.java.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class CompilationUnitImplementor extends AbstractJavaElementImplementor<CompilationUnitSpi> implements ICompilationUnit {

  public CompilationUnitImplementor(CompilationUnitSpi spi) {
    super(spi);
  }

  @Override
  public boolean isSynthetic() {
    return m_spi.isSynthetic();
  }

  @Override
  public IPackage containingPackage() {
    return m_spi.getPackage().wrap();
  }

  @Override
  public Optional<IClasspathEntry> containingClasspathFolder() {
    return Optional.ofNullable(m_spi.getContainingClasspathFolder())
        .map(ClasspathSpi::wrap);
  }

  @Override
  public Optional<IType> resolveTypeBySimpleName(String simpleName) {
    return Optional.ofNullable(m_spi.findTypeBySimpleName(simpleName))
        .map(TypeSpi::wrap);
  }

  @Override
  public Stream<? extends IJavaElement> children() {
    Stream<? extends IJavaElement> packageAndImports = Stream.concat(Stream.of(containingPackage()), imports());
    return Stream.concat(packageAndImports, types().stream());
  }

  @Override
  public Optional<Path> absolutePath() {
    return Optional.ofNullable(m_spi.absolutePath());
  }

  @Override
  public Optional<IType> mainType() {
    return Optional.ofNullable(m_spi.getMainType())
        .map(TypeSpi::wrap);
  }

  @Override
  public IType requireMainType() {
    return mainType()
        .orElseThrow(() -> newFail("Compilation Unit '{}' has no main type.", path()));
  }

  @Override
  public Path path() {
    return containingPackage().asPath().resolve(elementName());
  }

  @Override
  public Stream<IImport> imports() {
    return WrappingSpliterator.stream(m_spi.getImports());
  }

  @Override
  public Optional<SourceRange> javaDoc() {
    return Optional.ofNullable(m_spi.getJavaDoc());
  }

  @Override
  public InnerTypeQuery types() {
    return new InnerTypeQuery(new WrappingSpliterator<>(m_spi.getTypes()));
  }

  @Override
  public ICompilationUnitGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return CompilationUnitGenerator.create(this, transformer);
  }

  @Override
  public ICompilationUnitGenerator<?> toWorkingCopy() {
    return toWorkingCopy(null);
  }

  @Override
  public String toString() {
    return mainType()
        .map(IType::name)
        .map(fqn -> fqn + JavaTypes.JAVA_FILE_SUFFIX)
        .orElseGet(this::elementName);
  }
}
